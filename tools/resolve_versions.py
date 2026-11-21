#!/usr/bin/env python3
"""Removes the Minecraft-version conditions of Stonecutter from a repo that now holds one Minecraft version.

Written for splitting the branch `1.20.1+` (six nodes, three Minecraft versions) into one branch per Minecraft version
(migration step 3 in the CLAUDE.md of the OMM root). Run it after switching Stonecutter to the branch's Fabric node
(`./gradlew "Set active project to <version>-fabric"`): every branch that becomes unconditional is then active and
every branch that goes is commented out, so the tool only removes markers and dead branches and never uncomments.

Each condition is evaluated with the branch's Minecraft version; a loader the branch does not build (`neoforge` on
1.20.1, `forge` on 1.21.x) counts as false. A chain whose result does not depend on the loader is removed (its body
stays if it holds, everything goes if it does not); a chain that does is rewritten with plain loader conditions.

  resolve_versions.py <repo root> <minecraft version> <loader,loader>           rewrite the files
  resolve_versions.py <repo root> <minecraft version> <loader,loader> --check   only report

Example, from a mod root: `python3 -I ../OMLib/tools/resolve_versions.py . 1.21.1 fabric,neoforge --check`.
Files: every tracked *.java file outside omlib/. Exit code 1 if anything cannot be resolved or a Minecraft-version
condition is left.
"""
import argparse
import re
import subprocess
import sys
from pathlib import Path

LOADERS = ("fabric", "neoforge", "forge")
# A marker that ends a commented branch starts with */, or with ^/ when that branch sits inside an outer branch that
# is commented too (Stonecutter escapes the inner comment delimiters as /^* and ^/)
MARKER = re.compile(r"^(?P<indent>[ \t]*)(?P<close>\*/|\^/)?//\?(?P<rest>.*)$")
IF = re.compile(r"^ if (?P<cond>.+) \{$")
ELSE_IF = re.compile(r"^\} else if (?P<cond>.+) \{$")
ELSE = re.compile(r"^\} else \{$")
END = re.compile(r"^\}$")
TOKEN = re.compile(r"\s*(?:(?P<op>&&|\|\||\(|\))|(?P<ver>(?:>=|<)\d+(?:\.\d+)*)|(?P<name>[a-z]+))")
UNSUPPORTED = re.compile(r"/\*\?|//\$|/\*\$")


class ResolveError(Exception):
    pass


def version(text):
    return tuple(int(part) for part in text.split("."))


# --- conditions -----------------------------------------------------------------------------------------------------

def tokenize(cond):
    tokens, pos = [], 0
    while pos < len(cond):
        match = TOKEN.match(cond, pos)
        if not match or match.end() == pos:
            raise ResolveError(f"unsupported condition syntax at '{cond[pos:]}' in '{cond}'")
        pos = match.end()
        if match.group("name") and match.group("name") not in LOADERS:
            raise ResolveError(f"unknown constant '{match.group('name')}' in '{cond}'")
        tokens.append(match.group("op") or match.group("ver") or match.group("name"))
    return tokens


def parse(cond):
    """Parses a condition into a nested tuple: ('or', a, b), ('and', a, b), ('ver', op, version), ('loader', name)."""
    tokens = tokenize(cond.strip())
    pos = 0

    def peek():
        return tokens[pos] if pos < len(tokens) else None

    def take():
        nonlocal pos
        pos += 1
        return tokens[pos - 1]

    def expr_or():
        left = expr_and()
        while peek() == "||":
            take()
            left = ("or", left, expr_and())
        return left

    def expr_and():
        left = atom()
        while peek() == "&&":
            take()
            left = ("and", left, atom())
        return left

    def atom():
        token = take() if peek() is not None else None
        if token == "(":
            inner = expr_or()
            if peek() != ")":
                raise ResolveError(f"missing ')' in '{cond}'")
            take()
            return inner
        if token in LOADERS:
            return ("loader", token)
        if token and token[0] in "<>":
            op = ">=" if token.startswith(">=") else "<"
            return ("ver", op, version(token[len(op):]))
        raise ResolveError(f"unexpected '{token}' in '{cond}'")

    tree = expr_or()
    if pos != len(tokens):
        raise ResolveError(f"unexpected '{tokens[pos]}' in '{cond}'")
    return tree


def has_version(tree):
    if tree[0] == "ver":
        return True
    if tree[0] == "loader":
        return False
    return has_version(tree[1]) or has_version(tree[2])


def holds(tree, mc, loader):
    kind = tree[0]
    if kind == "ver":
        return mc >= tree[2] if tree[1] == ">=" else mc < tree[2]
    if kind == "loader":
        return tree[1] == loader
    if kind == "and":
        return holds(tree[1], mc, loader) and holds(tree[2], mc, loader)
    return holds(tree[1], mc, loader) or holds(tree[2], mc, loader)


def loaders_where(cond, mc, loaders):
    """The branch's loaders for which the condition holds (the other loaders count as false)."""
    tree = parse(cond)
    return frozenset(loader for loader in loaders if holds(tree, mc, loader)), has_version(tree)


# --- structure ------------------------------------------------------------------------------------------------------

class Branch:
    def __init__(self, line, kind, cond):
        self.line = line  # index of its opening marker
        self.kind = kind  # "if", "else if", "else"
        self.cond = cond
        self.items = []  # body: line indexes and nested chains
        self.inactive = False  # set from the marker that ends it


class Chain:
    def __init__(self, indent):
        self.indent = indent
        self.branches = []
        self.end = None


def parse_file(lines, name):
    root, stack = [], []

    def body():
        return stack[-1].branches[-1].items if stack else root

    for i, line in enumerate(lines):
        if UNSUPPORTED.search(line):
            raise ResolveError(f"{name}:{i + 1}: unsupported Stonecutter syntax: {line.strip()}")
        match = MARKER.match(line)
        if not match:
            if "//?" in line:
                raise ResolveError(f"{name}:{i + 1}: marker not at the start of the line: {line.strip()}")
            body().append(i)
            continue
        rest, closes = match.group("rest").rstrip(), bool(match.group("close"))
        if m := IF.match(rest):
            if closes:
                raise ResolveError(f"{name}:{i + 1}: '*/' before an opening marker")
            chain = Chain(match.group("indent"))
            chain.branches.append(Branch(i, "if", m.group("cond")))
            body().append(chain)
            stack.append(chain)
            continue
        if not stack:
            raise ResolveError(f"{name}:{i + 1}: marker without an open '//? if': {line.strip()}")
        chain = stack[-1]
        chain.branches[-1].inactive = closes
        if m := ELSE_IF.match(rest):
            chain.branches.append(Branch(i, "else if", m.group("cond")))
        elif ELSE.match(rest):
            chain.branches.append(Branch(i, "else", None))
        elif END.match(rest):
            chain.end = i
            stack.pop()
        else:
            raise ResolveError(f"{name}:{i + 1}: unsupported marker: {line.strip()}")
    if stack:
        raise ResolveError(f"{name}: '//? if' at line {stack[-1].branches[0].line + 1} is never closed")
    return root


# --- resolving ------------------------------------------------------------------------------------------------------

class Stats:
    def __init__(self):
        self.unconditional = self.dropped = self.loader = self.unchanged = 0

    def add(self, other):
        for key in vars(self):
            setattr(self, key, getattr(self, key) + getattr(other, key))

    def __str__(self):
        return (f"{self.unconditional} unconditional, {self.dropped} dropped, {self.loader} left as loader chains, "
                f"{self.unchanged} unchanged")


def render(items, lines, mc, loaders, stats, name, out):
    for item in items:
        if isinstance(item, int):
            out.append(lines[item])
        else:
            render_chain(item, lines, mc, loaders, stats, name, out)


def render_chain(chain, lines, mc, loaders, stats, name, out):
    where = f"{name}:{chain.branches[0].line + 1}"
    remaining = frozenset(loaders)
    kept, any_version = [], False  # (branch, loader condition or None for else)
    for branch in chain.branches:
        if branch.kind == "else":
            taken = remaining
        else:
            true_for, versioned = loaders_where(branch.cond, mc, loaders)
            any_version |= versioned
            taken = true_for & remaining
        active_on_fabric = "fabric" in taken
        if not taken:
            if not branch.inactive:
                raise ResolveError(f"{where}: branch at line {branch.line + 1} is dropped but active; "
                                   f"switch to the {mc_text(mc)}-fabric node first")
            continue
        if active_on_fabric == branch.inactive:
            raise ResolveError(f"{where}: branch at line {branch.line + 1} is "
                               f"{'inactive' if branch.inactive else 'active'} on the Fabric node, "
                               f"but its condition says otherwise; switch to the {mc_text(mc)}-fabric node first")
        if branch.inactive and any(not isinstance(item, int) for item in branch.items):
            raise ResolveError(f"{where}: nested condition inside a commented branch that is kept is not supported")
        kept.append((branch, None if taken == remaining else only(taken)))
        remaining -= taken
        if not remaining:
            break
    if not any_version and len(kept) == len(chain.branches) and all(
            (b.kind == "else") == (c is None) and (c is None or c == b.cond.strip()) for b, c in kept):
        # a pure loader chain that stays as it is
        stats.unchanged += 1
        out.extend(lines[chain.branches[0].line:chain.branches[0].line + 1])
        for index, (branch, _) in enumerate(kept):
            if index:
                out.append(lines[branch.line])
            render(branch.items, lines, mc, loaders, stats, name, out)
        out.append(lines[chain.end])
        return
    if not kept:
        stats.dropped += 1
        return
    if len(kept) == 1 and kept[0][1] is None:
        stats.unconditional += 1
        render(kept[0][0].items, lines, mc, loaders, stats, name, out)
        return
    stats.loader += 1
    indent = chain.indent
    for index, (branch, cond) in enumerate(kept):
        if index == 0:
            out.append(f"{indent}//? if {cond} {{")
        else:
            close = "*/" if kept[index - 1][0].inactive else ""
            out.append(f"{indent}{close}//?}} else {{" if cond is None else f"{indent}{close}//?}} else if {cond} {{")
        render(branch.items, lines, mc, loaders, stats, name, out)
    out.append(f"{indent}{'*/' if kept[-1][0].inactive else ''}//?}}")


def only(taken):
    (loader,) = taken
    return loader


def mc_text(mc):
    return ".".join(str(part) for part in mc)


def resolve_text(text, mc, loaders, name="<text>"):
    """Returns the resolved text and the statistics; raises ResolveError."""
    lines = text.split("\n")
    stats, out = Stats(), []
    render(parse_file(lines, name), lines, mc, loaders, stats, name, out)
    result = "\n".join(out)
    for number, line in enumerate(result.split("\n"), 1):
        match = MARKER.match(line)
        if match and re.search(r"(>=|<)\d", match.group("rest")):
            raise ResolveError(f"{name}:{number}: a Minecraft-version condition is left: {line.strip()}")
    return result, stats


def java_files(root):
    listed = subprocess.run(["git", "-C", str(root), "ls-files", "*.java"], capture_output=True, text=True, check=True)
    return [root / path for path in listed.stdout.splitlines() if not path.startswith("omlib/")]


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("root", type=Path)
    parser.add_argument("minecraft", help="the branch's Minecraft version, e.g. 1.21.1")
    parser.add_argument("loaders", help="the branch's loaders, e.g. fabric,neoforge")
    parser.add_argument("--check", action="store_true", help="report only, change nothing")
    args = parser.parse_args()
    mc = version(args.minecraft)
    loaders = tuple(args.loaders.split(","))
    if "fabric" not in loaders or any(loader not in LOADERS for loader in loaders):
        parser.error(f"loaders must include fabric and be among {', '.join(LOADERS)}")
    root = args.root.resolve()
    total, errors, changed = Stats(), [], 0
    for path in java_files(root):
        text = path.read_text()
        if "//?" not in text:
            continue
        name = str(path.relative_to(root))
        try:
            result, stats = resolve_text(text, mc, loaders, name)
        except ResolveError as error:
            errors.append(str(error))
            continue
        total.add(stats)
        if result != text:
            changed += 1
            print(f"{name}: {stats}")
            if not args.check:
                path.write_text(result)
    print(f"Total ({'check' if args.check else 'written'}): {changed} files, {total}")
    for error in errors:
        print(f"ERROR {error}", file=sys.stderr)
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
