#!/usr/bin/env python3
"""Keeps the source files that the OpenModular mods share in step.

The mods copy a number of files (platform services, compat helpers, config annotations ...) that only differ by
the mod's package, id, name and class prefix (OMT, OML ...).
`shared-sources.txt` lists them; this tool normalises those four things and compares the copies.

  shared_sources.py check <mod root> <mod root> ...        report files that drifted apart (exit 1 if any)
  shared_sources.py discover <mod root> <mod root> ...     list files that exist in all mods and are not listed yet
  shared_sources.py discover --add ...                      ... and add the identical ones to the list
  shared_sources.py sync --from <mod root> <mod root> ...   copy the listed files from one mod into the others
  shared_sources.py sync --write ...                        ... for real (default is a dry run)

Run with `python3 -I` from a mod root, e.g. `python3 -I omlib/tools/shared_sources.py check . ../OpenModularLighting`.
"""
import argparse
import difflib
import re
import sys
from pathlib import Path

MANIFEST = Path(__file__).resolve().parent.parent / "shared-sources.txt"
SKIP_DIRS = {"build", ".gradle", ".git", "run", "omlib", "oldmod", "oldmod2", "oldlib", "node_modules", "generated"}
TEXT_SUFFIXES = {".java", ".kt", ".kts", ".json", ".toml", ".xml", ".md", ".properties"}


class Mod:
    def __init__(self, root: Path):
        self.root = root.resolve()
        props = {}
        for line in (self.root / "gradle.properties").read_text().splitlines():
            if "=" in line and not line.lstrip().startswith("#"):
                key, value = line.split("=", 1)
                props[key.strip()] = value.strip()
        self.mod_id = props["mod_id"]
        self.name = props["mod_name"]
        self.package = props["maven_group"]
        self.abbreviation = "".join(word[0] for word in self.name.split()).upper()

    def path(self, template: str) -> Path:
        return self.root / template.replace("@pkg@", self.package.replace(".", "/"))

    def normalise(self, text: str) -> str:
        text = text.replace(self.package, "@pkg@").replace(self.name, "@mod_name@").replace(self.mod_id, "@mod_id@")
        return re.sub(rf"\b{self.abbreviation}(?![a-z])", "@abbr@", text)

    def denormalise(self, text: str) -> str:
        return (text.replace("@pkg@", self.package).replace("@mod_name@", self.name)
                .replace("@mod_id@", self.mod_id).replace("@abbr@", self.abbreviation))


class Entry:
    def __init__(self, template: str, mod_ids):
        self.template = template
        self.mod_ids = mod_ids  # None = every mod

    def applies(self, mod: Mod) -> bool:
        return self.mod_ids is None or mod.mod_id in self.mod_ids

    def line(self) -> str:
        return self.template if self.mod_ids is None else f"{self.template} | {','.join(sorted(self.mod_ids))}"


def read_manifest():
    entries = []
    for raw in MANIFEST.read_text().splitlines():
        line = raw.split("#", 1)[0].strip()
        if not line:
            continue
        template, _, ids = line.partition("|")
        entries.append(Entry(template.strip(), {i.strip() for i in ids.split(",")} if ids.strip() else None))
    return entries


def files_of(mod: Mod):
    """All candidate files of a mod as templates (package folder replaced by @pkg@)."""
    package_path = mod.package.replace(".", "/")
    found = set()
    for path in mod.root.rglob("*"):
        relative = path.relative_to(mod.root)
        if not path.is_file() or path.suffix not in TEXT_SUFFIXES or SKIP_DIRS & set(relative.parts):
            continue
        if relative.parts[0] not in ("src", "gradle"):
            continue
        found.add(relative.as_posix().replace(package_path, "@pkg@"))
    return found


def diff(a_name, a_text, b_name, b_text):
    return "".join(difflib.unified_diff(a_text.splitlines(True), b_text.splitlines(True), a_name, b_name, n=2))


def check(mods):
    problems = 0
    for entry in read_manifest():
        members = [m for m in mods if entry.applies(m)]
        if len(members) < 2:
            continue
        texts = {}
        for mod in members:
            path = mod.path(entry.template)
            if not path.is_file():
                print(f"MISSING  {entry.template}  in {mod.mod_id}")
                problems += 1
            else:
                texts[mod] = mod.normalise(path.read_text())
        reference = next(iter(texts), None)
        for mod, text in texts.items():
            if reference is not None and text != texts[reference]:
                print(f"DRIFT    {entry.template}  {reference.mod_id} <> {mod.mod_id}")
                print(diff(reference.mod_id, texts[reference], mod.mod_id, text))
                problems += 1
    print("shared sources are in step" if problems == 0 else f"{problems} problem(s)")
    return 1 if problems else 0


def discover(mods, add):
    listed = {e.template for e in read_manifest()}
    common = set.intersection(*(files_of(m) for m in mods)) - listed
    identical, near, other = [], [], []
    for template in sorted(common):
        texts = [m.normalise(m.path(template).read_text()) for m in mods]
        if all(t == texts[0] for t in texts):
            identical.append(template)
        else:
            changed = sum(1 for line in difflib.unified_diff(texts[0].splitlines(), texts[1].splitlines(), lineterm="", n=0)
                          if line[:1] in "+-" and line[:3] not in ("+++", "---"))
            (near if changed <= 20 else other).append((template, changed))
    print(f"identical, not listed ({len(identical)}):")
    for template in identical:
        print("  " + template)
    print(f"near-identical, up to 20 changed lines ({len(near)}), candidates to converge:")
    for template, changed in near:
        print(f"  {changed:3d}  {template}")
    print(f"different ({len(other)}), not shown")
    if add and identical:
        with MANIFEST.open("a") as out:
            out.write("".join(t + "\n" for t in identical))
        print(f"added {len(identical)} file(s) to {MANIFEST.name}")


def sync(mods, source_root, write):
    source = next(m for m in mods if m.root == Path(source_root).resolve())
    for entry in read_manifest():
        if not entry.applies(source) or not source.path(entry.template).is_file():
            continue
        normalised = source.normalise(source.path(entry.template).read_text())
        for mod in mods:
            if mod is source or not entry.applies(mod):
                continue
            target = mod.path(entry.template)
            text = mod.denormalise(normalised)
            if target.is_file() and target.read_text() == text:
                continue
            print(f"{'WRITE' if write else 'would write'}  {mod.mod_id}: {entry.template}")
            if write:
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(text)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("command", choices=["check", "discover", "sync"])
    parser.add_argument("roots", nargs="+", help="mod roots (directories with gradle.properties)")
    parser.add_argument("--add", action="store_true", help="discover: add the identical files to shared-sources.txt")
    parser.add_argument("--from", dest="source", help="sync: the mod root whose files are copied")
    parser.add_argument("--write", action="store_true", help="sync: write the files (default is a dry run)")
    args = parser.parse_args()
    mods = [Mod(Path(r)) for r in args.roots]
    if len(mods) < 2:
        parser.error("give at least two mod roots")
    if args.command == "check":
        sys.exit(check(mods))
    elif args.command == "discover":
        discover(mods, args.add)
    else:
        if not args.source:
            parser.error("sync needs --from <mod root>")
        sync(mods, args.source, args.write)


if __name__ == "__main__":
    main()
