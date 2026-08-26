# Udhaardaar V3 Launch QA

This directory is reserved for release-gate Android UI/instrumentation tests.

## Required release scenarios

1. Clean launch/login/logout and navigation.
2. Keyboard focus test for every editable field; focused control and bottom actions must remain above IME and Android navigation insets.
3. Lender profile creation and validation.
4. Multiple borrower profiles and repeated borrower credits.
5. Guarantor creation, association and authorisation boundaries.
6. Personal, business, trade, advance, rental/lease and other credit registration.
7. OTP success, wrong OTP, expired/reused OTP and consent gating.
8. Regular, partial, delayed, missed and default repayments.
9. Outstanding balance, due/overdue state, history and score consistency.
10. Digital document generation, retrieval and transaction association.
11. Privacy/authorised-view checks: unrelated parties must not access or modify another party's protected records.
12. Database persistence after activity recreation and app restart.
13. Invalid input, duplicate records, empty states and error recovery.
14. Final APK install/upgrade and launch smoke test.

Tests must use synthetic data only. Real OTP values must never be logged or asserted in source control; real-device OTP entry remains a manual acceptance step.