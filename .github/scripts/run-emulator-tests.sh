#!/usr/bin/env bash
set -Eeuo pipefail

mkdir -p app/build/qa-diagnostics
trap 'adb logcat -d -t 8000 > app/build/qa-diagnostics/emulator-logcat.txt 2>&1 || true; adb devices -l > app/build/qa-diagnostics/adb-devices.txt 2>&1 || true' EXIT

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
gradle clean :app:connectedDebugAndroidTest --no-daemon --no-parallel --max-workers=2 --stacktrace
gradle_exit=$?
set -e

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
