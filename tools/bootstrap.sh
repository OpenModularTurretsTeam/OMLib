#!/bin/sh
# Builds the library for every node into build/maven (what the mods bundle and compile against), unless it was already
# built from the same sources. Called from the pluginManagement block of a mod's settings.gradle.kts, before anything
# else is configured, because Loom resolves the library's jar while the mod's projects are configured.
# JAVA_HOME must point at the JDK that runs Gradle (the settings script sets it).
set -e
cd "$(dirname "$0")/.."
stamp=build/maven/.stamp
# The library's jar bundles omlib-core: from the local OpenModularCore/ that settings.gradle.kts includes (the first one
# in a folder above this one), unless OMM_NO_LOCAL_CORE=1 (the mods' settings set it for -Pomm.noLocalCore), else from
# JitPack. So the stamp also covers which core it is and, for the local one, the sources the library's jar takes from it.
core=jitpack
if [ "${OMM_NO_LOCAL_CORE:-0}" = 0 ]; then
    dir=$(pwd)
    while [ "$dir" != / ]; do
        if [ -f "$dir/OpenModularCore/settings.gradle.kts" ]; then
            core="$dir/OpenModularCore"
            break
        fi
        dir=$(dirname "$dir")
    done
fi
inputs() {
    find src build-logic/src -type f ! -path '*/build/*' | sort
    ls ./*.kts gradle.properties build-logic/*.kts versions/*/gradle.properties
    if [ "$core" != jitpack ]; then
        # without build output and Gradle's and Kotlin's state folders (.gradle, .kotlin)
        find "$core/omlib-core" "$core/build-logic" \( -name build -o -name '.*' \) -prune -o -type f -print | sort
        ls "$core"/*.kts "$core/gradle.properties"
    fi
}
current=$( { echo "core: $core"; inputs | xargs sha256sum; } | sha256sum | cut -d' ' -f1 )
if [ -f "$stamp" ] && [ "$(cat "$stamp")" = "$current" ]; then
    exit 0
fi
echo "OMLib: building the library for all nodes into omlib/build/maven (core: $core)"
./gradlew --console=plain -q chiseledPublishLocal
mkdir -p build/maven
echo "$current" > "$stamp"
