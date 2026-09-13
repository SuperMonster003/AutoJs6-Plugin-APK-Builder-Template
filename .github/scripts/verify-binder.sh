#!/usr/bin/env bash
set -euo pipefail
expected_pages="${1:?Expected page size is required}"
actual_pages="$(adb shell getconf PAGESIZE | tr -d '\r')"
test "$actual_pages" = "$expected_pages"
test -n "${UNIVERSAL_RUNTIME_KIT_DIR:?A verified universal Runtime Kit is required}"
./gradlew --no-daemon --max-workers=2 :app:connectedDebugAndroidTest \
  "-Pautojs.apkBuilder.templatePlugin.runtimeKitDir=$UNIVERSAL_RUNTIME_KIT_DIR" \
  -Pandroid.testInstrumentationRunnerArguments.class=org.autojs.plugin.apkbuilder.template.impl.PluginDiscoveryAndroidTest \
  --stacktrace
