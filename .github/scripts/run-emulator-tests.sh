#!/usr/bin/env bash
set -Eeuo pipefail

mkdir -p app/build/qa-diagnostics
attempt_log_aggregate=/tmp/arthsaathi-instrumentation-attempts.log
: > "$attempt_log_aggregate"
trap 'mkdir -p app/build/qa-diagnostics; adb logcat -d -t 8000 > app/build/qa-diagnostics/emulator-logcat.txt 2>&1 || true; adb devices -l > app/build/qa-diagnostics/adb-devices.txt 2>&1 || true' EXIT

echo "=== Connected Android devices ==="
adb devices -l
timeout --signal=TERM --kill-after=10s 5m adb wait-for-device || { echo "FAIL: emulator did not become reachable within 5 minutes"; adb devices -l || true; exit 124; }

echo "=== Waiting up to 5 minutes for Android boot ==="
deadline=$((SECONDS + 300))
until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)" == "1" ]]; do
  if (( SECONDS >= deadline )); then
    echo "FAIL: emulator did not reach sys.boot_completed=1 within 5 minutes"
    adb devices -l || true
    adb logcat -d -t 3000 || true
    exit 124
  fi
  sleep 2
done

adb shell cmd package list packages >/dev/null
echo "=== Emulator ready; starting instrumentation tests ==="

set +e
gradle_exit=1
attempt=1
max_attempts=3
while (( attempt <= max_attempts )); do
  echo "=== Instrumentation attempt ${attempt}/${max_attempts} ==="
  attempt_log="/tmp/arthsaathi-instrumentation-attempt-${attempt}.log"
  # Bound each Gradle attempt and stream output so a stuck test is diagnosable in Actions logs.
  timeout --signal=TERM --kill-after=20s 8m gradle clean :app:connectedDebugAndroidTest --no-daemon --no-parallel --max-workers=2 --stacktrace --console=plain 2>&1 | tee "$attempt_log"
  gradle_exit=${PIPESTATUS[0]}
  printf '\n===== ATTEMPT %s LOG =====\n' "$attempt" >> "$attempt_log_aggregate"
  cat "$attempt_log" >> "$attempt_log_aggregate"

  executed_tests=$(python3 - <<'PY'
import glob
import xml.etree.ElementTree as ET

total = 0
for path in sorted(set(glob.glob("app/build/outputs/androidTest-results/**/*.xml", recursive=True))):
    try:
        root = ET.parse(path).getroot()
    except (ET.ParseError, OSError):
        continue
    suites = [root] if root.tag == "testsuite" else list(root.iter("testsuite"))
    for suite in suites:
        total += int(suite.attrib.get("tests", "0"))
print(total)
PY
)
  echo "Executed instrumentation tests on attempt ${attempt}: ${executed_tests}"

  if (( executed_tests > 0 )); then
    if (( gradle_exit != 0 )); then
      echo "FAIL: tests executed but Gradle reported failure; not retrying assertion/test failures."
    fi
    break
  fi

  if ! grep -Eqi "Instrumentation run failed due to Process crashed|Failed to start Emulator console|Instrumentation process crashed" "$attempt_log"; then
    echo "FAIL: zero tests executed, but the log does not match a known emulator/instrumentation crash; not retrying compile or configuration failures."
    break
  fi
  if (( attempt == max_attempts )); then
    echo "FAIL: instrumentation process crashed before tests executed after ${max_attempts} attempts."
    break
  fi

  echo "RETRY: confirmed instrumentation crash before tests executed; capturing diagnostics and repairing ADB before retry."
  mkdir -p app/build/qa-diagnostics
  adb devices -l > "app/build/qa-diagnostics/adb-before-retry-${attempt}.txt" 2>&1 || true
  adb logcat -d -t 4000 > "app/build/qa-diagnostics/logcat-before-retry-${attempt}.txt" 2>&1 || true
  adb kill-server || true
  sleep 2
  adb start-server || true
  timeout --signal=TERM --kill-after=10s 2m adb wait-for-device || {
    echo "FAIL: emulator did not reconnect after ADB restart."
    adb devices -l || true
    break
  }
  boot_deadline=$((SECONDS + 120))
  until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)" == "1" ]]; do
    if (( SECONDS >= boot_deadline )); then
      echo "FAIL: Android did not return to boot-completed state after ADB restart."
      adb devices -l || true
      break
    fi
    sleep 2
  done
  if [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)" != "1" ]]; then
    break
  fi
  adb shell cmd package list packages >/dev/null || {
    echo "FAIL: package manager did not respond after ADB restart."
    break
  }
  sleep 5
  attempt=$((attempt + 1))
done
set -e
mkdir -p app/build/qa-diagnostics
cp "$attempt_log_aggregate" app/build/qa-diagnostics/gradle-instrumentation-attempts.log

echo "=== Pulling canonical ArthSaathi visual evidence ==="
visual_exit=0
mkdir -p app/build/qa-diagnostics/visual-captures
# The instrumentation test exports screenshots to shared MediaStore Pictures storage.
# This survives Gradle's cleanup/uninstall of the target app after connected tests.
capture_dir=/sdcard/Pictures/arthsaathi_canonical
if adb shell "test -d '$capture_dir'"; then
  adb pull "$capture_dir/." app/build/qa-diagnostics/visual-captures/ || true
  screenshot_count=$(find app/build/qa-diagnostics/visual-captures -type f -name '*.png' | wc -l)
  echo "Canonical screenshots available: $screenshot_count"
  if (( screenshot_count < 1 )); then
    echo "FAIL: screenshot export directory exists but contains no PNG evidence."
    visual_exit=1
  fi
else
  echo "FAIL: no canonical screenshot export was produced by this instrumentation suite."
  visual_exit=1
fi

echo "=== Validating instrumentation reports ==="
set +e
python3 - <<'PY'
import glob
import sys
import xml.etree.ElementTree as ET

reports = sorted(set(glob.glob("app/build/outputs/androidTest-results/**/*.xml", recursive=True)))
reports = [p for p in reports if p.lower().endswith(".xml")]
if not reports:
    print("FAIL: no instrumentation XML reports were generated")
    sys.exit(2)

total_tests = total_failures = total_errors = 0
parsed_reports = 0
for path in reports:
    try:
        root = ET.parse(path).getroot()
    except (ET.ParseError, OSError) as exc:
        print(f"WARNING: could not parse {path}: {exc}")
        continue
    parsed_reports += 1
    suites = [root] if root.tag == "testsuite" else list(root.iter("testsuite"))
    for suite in suites:
        total_tests += int(suite.attrib.get("tests", "0"))
        total_failures += int(suite.attrib.get("failures", "0"))
        total_errors += int(suite.attrib.get("errors", "0"))

print(f"Instrumentation report totals: tests={total_tests}, failures={total_failures}, errors={total_errors}, reports={parsed_reports}")
if parsed_reports == 0 or total_tests < 1:
    print("FAIL: reports were absent, unreadable, or contained no executed tests")
    sys.exit(2)
if total_failures or total_errors:
    print("FAIL: instrumentation tests reported failures or errors")
    sys.exit(1)
PY
report_exit=$?
set -e

if (( gradle_exit != 0 )); then
  echo "FAIL: connectedDebugAndroidTest exited with status $gradle_exit"
  exit "$gradle_exit"
fi
if (( report_exit != 0 )); then
  exit "$report_exit"
fi
if (( visual_exit != 0 )); then
  echo "FAIL: canonical visual evidence is missing; inspect test output and QA diagnostics."
  exit 3
fi

echo "PASS: emulator instrumentation tests completed with zero reported failures and canonical screenshots verified."
