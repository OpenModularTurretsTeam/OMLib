#!/bin/sh
# Builds the library for every node into build/maven (what the mods bundle and compile against), unless it was already
# built from the same sources. Called from the pluginManagement block of a mod's settings.gradle.kts, before anything
# else is configured, because Loom resolves the library's jar while the mod's projects are configured.
# JAVA_HOME must point at the JDK that runs Gradle (the settings script sets it).
set -e
cd "$(dirname "$0")/.."
stamp=build/maven/.stamp
current=$( (find src build-logic/src -type f ! -path '*/build/*' | sort; ls ./*.kts gradle.properties build-logic/*.kts versions/*/gradle.properties) \
    | xargs sha256sum | sha256sum | cut -d' ' -f1 )
if [ -f "$stamp" ] && [ "$(cat "$stamp")" = "$current" ]; then
    exit 0
fi
echo "OMLib: building the library for all nodes into omlib/build/maven"
./gradlew --console=plain -q chiseledPublishLocal
mkdir -p build/maven
echo "$current" > "$stamp"
