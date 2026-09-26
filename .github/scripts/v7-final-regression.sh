#!/usr/bin/env bash
set -euo pipefail
adb wait-for-device
test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = "1"
adb shell pm path android
run_test() {
  local name="$1"; local class="$2"
  echo "===== V7 REGRESSION: $name ====="
  timeout 6m gradle :app:connectedDebugAndroidTest --no-daemon --stacktrace "-Pandroid.testInstrumentationRunnerArguments.class=$class"
  echo "===== PASS: $name ====="
}
run_test "legacy/V6.2 lifecycle smoke" "com.udhaardaar.mvp.ArthSaathiV62SmokeTest"
run_test "V7 broad functional/UI regression" "com.udhaardaar.mvp.V7BroadFunctionalUiRegressionInstrumentedTest"
run_test "V7 master integration matrix" "com.udhaardaar.mvp.V7MasterIntegrationScenarioInstrumentedTest"
run_test "V7 persistence boundary" "com.udhaardaar.mvp.V7PersistenceBoundaryInstrumentedTest"
run_test "V7 secure account/session" "com.udhaardaar.mvp.V7SecureAccountInstrumentedTest"
run_test "V7 Step 1-5 integration" "com.udhaardaar.mvp.V7Step1To5IntegrationInstrumentedTest"
run_test "V7 Step 6 native migration" "com.udhaardaar.mvp.V7Step6NativeMigrationInstrumentedTest"
echo "===== ALL V7 EMULATOR REGRESSION SUITES PASSED ====="
