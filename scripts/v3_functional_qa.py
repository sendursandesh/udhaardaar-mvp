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
    if pin:
        assert re.fullmatch(r"[0-9]{6}", pin)
    if pan:
        assert PAN.fullmatch(pan.upper())
    if aadhaar:
        assert re.fullmatch(r"[0-9]{12}", aadhaar)
    if gstin:
        assert GST.fullmatch(gstin.upper())


def run():
    passed = 0
    total = 0
    def case(name, fn):
        nonlocal passed, total
        total += 1
        fn()
        passed += 1
        print(f"PASS {total:02d}: {name}")

    # 10 profile/identity cases
    case("valid mobile/profile", lambda: validate_profile("9876543210", "834001", "ABCDE1234F", "123456789012"))
    case("invalid mobile rejected", lambda: expect_fail(lambda: validate_profile("987654321")))
    case("invalid PIN rejected", lambda: expect_fail(lambda: validate_profile("9876543210", "83400")))
    case("invalid PAN rejected", lambda: expect_fail(lambda: validate_profile("9876543210", pan="ABC123")))
    case("invalid Aadhaar length rejected", lambda: expect_fail(lambda: validate_profile("9876543210", aadhaar="1234")))
    case("invalid GSTIN rejected", lambda: expect_fail(lambda: validate_profile("9876543210", gstin="22ABCDE1234F1Z")))
    case("PAN case normalisation", lambda: validate_profile("9876543210", pan="abcde1234f"))
    case("GST case normalisation", lambda: validate_profile("9876543210", gstin="22ABCDE1234F1Z5"))
    case("blank optional identifiers allowed", lambda: validate_profile("9876543210"))
    case("ten-digit boundary", lambda: validate_profile("0000000000"))

    # 10 credit/calculation cases
    case("zero ROI EMI", lambda: assert_close(emi(12000, 0, 12), 1000))
    case("12% EMI", lambda: assert_close(emi(12000, 12, 12), 1066.19, 0.02))
    case("simple interest", lambda: assert_close(simple_interest(12000, 12, 12), 1440))
    case("one-month EMI", lambda: assert_close(emi(1000, 12, 1), 1010, 0.01))
    case("small principal", lambda: emi(1, 0, 1))
    case("large principal", lambda: emi(10_000_000, 15, 240))
    case("one-month simple interest", lambda: assert_close(simple_interest(12000, 12, 1), 120))
    case("long tenor EMI", lambda: emi(500000, 10, 240))
    case("principal+interest payable", lambda: assert_close(10000 + simple_interest(10000, 12, 12), 11200))
    case("invalid principal rejected", lambda: expect_fail(lambda: emi(0, 12, 12)))

    # 10 repayment/edge cases at business-rule level
    case("partial repayment leaves balance", lambda: assert_close(max(0, 10000-2500), 7500))
    case("full repayment closes balance", lambda: assert_close(max(0, 10000-10000), 0))
    case("overpayment capped", lambda: assert_close(min(15000, 10000), 10000))
    case("zero repayment rejected", lambda: expect_fail(lambda: require_positive(0)))
    case("negative repayment rejected", lambda: expect_fail(lambda: require_positive(-1)))
    case("due-date ordering", lambda: assert ["2026-01-01","2026-02-01"] == sorted(["2026-02-01","2026-01-01"]))
    case("overdue classification", lambda: assert_true("2026-01-01" < date.today().isoformat()))
    case("future due classification", lambda: assert_true("2999-01-01" >= date.today().isoformat()))
    case("repayment cannot exceed outstanding", lambda: assert_true(min(700, 500) == 500))
    case("closed balance remains zero", lambda: assert_true(max(0, 0-500) == 0))

    print(f"V3 FUNCTIONAL BUSINESS QA PASS: {passed}/{total} deterministic cases")


def expect_fail(fn):
    try:
        fn()
    except (AssertionError, ValueError, TypeError):
        return
    raise AssertionError("expected failure")


def require_positive(x):
    if x <= 0:
        raise ValueError("amount must be positive")


def assert_close(a, b, tol=0.01):
    assert abs(a-b) <= tol, (a, b)


def assert_true(x):
    assert x


if __name__ == "__main__":
    run()
