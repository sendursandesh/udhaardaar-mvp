# Udhaardaar V5 Implementation Status

V5 is a clean rebuild branch. The product contract is deliberately broader than the legacy V4 screen.

## Implemented foundation
- Central V5 feature registry
- Stable navigation routes
- Domain models for profiles, credit, rental, documents, assets, claims and charge comparison
- Strict PAN/Aadhaar/GSTIN/mobile/PIN format validation helpers
- Existing Android manifest already enforces portrait orientation and adjustResize
- Existing application lifecycle hooks provide keyboard-aware scrolling and mobile input limits

## Build gates
The V5 APK workflow must pass:
1. Gradle compilation
2. APK existence/non-zero size
3. APK ZIP integrity
4. Package-name verification
5. Instrumented/manual smoke checks before release

## Product acceptance gates
A V5 release is not considered complete until these are demonstrably usable:
- Login -> create account -> logout -> login is deterministic
- Profile photo persists and displays
- PIN resolves city/state
- Invalid identity numbers are rejected
- Keyboard never hides active field
- Cursor moves through forms
- App survives background/minimise and restores state
- Informal credit requires borrower/counterparty OTP consent
- Formal credit never asks bank for counterparty consent
- DPN and guarantor guarantee are generated, consented and audit-stored
- Rental has its own lease fields and workflow
- Sanction letter and account statement can be uploaded and compared
- Financial and non-financial assets can be vaulted
- Nominee/trusted person/inheritance/claim workflows exist
- Legal-assistance workflow exists
- Audit history is retained for protected documents/consents/claims

No APK should be presented as final until these gates are passed.
