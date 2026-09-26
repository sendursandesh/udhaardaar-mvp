#!/usr/bin/env bash
set -euo pipefail
adb wait-for-device
test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = "1"
adb shell pm path android
echo "===== V7 FINAL REGRESSION: unified emulator suite ====="
timeout 30m gradle :app:connectedDebugAndroidTest --no-daemon --stacktrace "-Pandroid.testInstrumentationRunnerArguments.class=com.udhaardaar.mvp.V7FinalRegressionSuite"
echo "===== ALL V7 EMULATOR REGRESSION TESTS PASSED ====="
