# V5 Completion Execution Note

This branch is the controlled V5 completion branch created from the latest successful V5 build commit.

## Release contract

Complete and verify all gates in `V5_IMPLEMENTATION_STATUS.md` and `V5_RELEASE_READINESS.md` before calling the APK final.

## Required product rules

- Informal credit repayment changes require bilateral counterparty consent/OTP.
- Formal credit repayment changes do not require bank/counterparty consent.
- Due-date reminders apply to both formal and informal credit.
- Credit receiving is supported.
- Sanction letter conditions (ROI, charges and other terms) can be captured/extracted and compared with statement-of-account charges.
- Rental/lease is a distinct credit workflow.
- DPN and guarantor guarantee are consented and audit-stored.
- Private documents/assets use access control and audit history.
- Nominee/trusted-person/inheritance/claim workflows are retained.
- Legal-assistance workflow is retained.

## QA policy

A successful Gradle build is necessary but not sufficient. Release requires APK integrity/package verification, automated tests, and real-device smoke verification of the critical flows.
