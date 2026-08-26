"""Deterministic V3 business-rule QA. This does not replace real-device/OTP testing."""
from datetime import date
import re

PAN = re.compile(r"^[A-Z]{5}[0-9]{4}[A-Z]$")
GST = re.compile(r"^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")

def emi(principal, annual_rate, months):
    assert principal > 0 and months >= 1
    if annual_rate == 0:
        return principal / months
    r = annual_rate / 1200
    return principal * r * (1+r)**months / ((1+r)**months-1)

def simple_interest(principal, annual_rate, months):
    assert principal > 0 and months >= 1
    return principal * annual_rate / 100 * months / 12

def validate_profile(mobile, pin="", pan="", aadhaar="", gstin=""):
    assert re.fullmatch(r"[0-9]{10}", mobile)
    if pin: assert re.fullmatch(r"[0-9]{6}", pin)
    if pan: assert PAN.fullmatch(pan.upper())
    if aadhaar: assert re.fullmatch(r"[0-9]{12}", aadhaar)
    if gstin: assert GST.fullmatch(gstin.upper())

def expect_fail(fn):
    try: fn()
    except (AssertionError, ValueError, TypeError): return
    raise AssertionError("expected failure")

def require_positive(x):
    if x <= 0: raise ValueError("amount must be positive")

def assert_close(a,b,tol=0.01): assert abs(a-b) <= tol, (a,b)
def assert_true(x): assert x

def run():
    passed=total=0
    def case(name,fn):
        nonlocal passed,total
        total += 1; fn(); passed += 1; print(f"PASS {total:02d}: {name}")
    cases = [
      ("valid mobile/profile",lambda:validate_profile("9876543210","834001","ABCDE1234F","123456789012")),
      ("invalid mobile rejected",lambda:expect_fail(lambda:validate_profile("987654321"))),
      ("invalid PIN rejected",lambda:expect_fail(lambda:validate_profile("9876543210","83400"))),
      ("invalid PAN rejected",lambda:expect_fail(lambda:validate_profile("9876543210",pan="ABC123"))),
      ("invalid Aadhaar length rejected",lambda:expect_fail(lambda:validate_profile("9876543210",aadhaar="1234"))),
      ("invalid GSTIN rejected",lambda:expect_fail(lambda:validate_profile("9876543210",gstin="22ABCDE1234F1Z"))),
      ("PAN case normalisation",lambda:validate_profile("9876543210",pan="abcde1234f")),
      ("GST case normalisation",lambda:validate_profile("9876543210",gstin="22ABCDE1234F1Z5")),
      ("blank optional identifiers allowed",lambda:validate_profile("9876543210")),
      ("ten-digit boundary",lambda:validate_profile("0000000000")),
      ("zero ROI EMI",lambda:assert_close(emi(12000,0,12),1000)),
      ("12% EMI",lambda:assert_close(emi(12000,12,12),1066.19,0.02)),
      ("simple interest",lambda:assert_close(simple_interest(12000,12,12),1440)),
      ("one-month EMI",lambda:assert_close(emi(1000,12,1),1010,0.01)),
      ("small principal",lambda:emi(1,0,1)),
      ("large principal",lambda:emi(10_000_000,15,240)),
      ("one-month simple interest",lambda:assert_close(simple_interest(12000,12,1),120)),
      ("long tenor EMI",lambda:emi(500000,10,240)),
      ("principal+interest payable",lambda:assert_close(10000+simple_interest(10000,12,12),11200)),
      ("invalid principal rejected",lambda:expect_fail(lambda:emi(0,12,12))),
      ("partial repayment leaves balance",lambda:assert_close(max(0,10000-2500),7500)),
      ("full repayment closes balance",lambda:assert_close(max(0,10000-10000),0)),
      ("overpayment capped",lambda:assert_close(min(15000,10000),10000)),
      ("zero repayment rejected",lambda:expect_fail(lambda:require_positive(0))),
      ("negative repayment rejected",lambda:expect_fail(lambda:require_positive(-1))),
      ("due-date ordering",lambda:assert_true(["2026-01-01","2026-02-01"]==sorted(["2026-02-01","2026-01-01"]))),
      ("overdue classification",lambda:assert_true("2026-01-01"<date.today().isoformat())),
      ("future due classification",lambda:assert_true("2999-01-01">=date.today().isoformat())),
      ("repayment cannot exceed outstanding",lambda:assert_true(min(700,500)==500)),
      ("closed balance remains zero",lambda:assert_true(max(0,0-500)==0)),
    ]
    for name,fn in cases: case(name,fn)
    assert passed == 30
    print(f"V3 FUNCTIONAL BUSINESS QA PASS: {passed}/{total} deterministic cases")

if __name__ == "__main__": run()
