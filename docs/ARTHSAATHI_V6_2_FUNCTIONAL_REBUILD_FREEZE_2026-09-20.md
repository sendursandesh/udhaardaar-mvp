# ArthSaathi V6.2 Functional Rebuild — 2026-09-20

This branch is a corrective rebuild from the canonical `arthsaathi-v6.2` baseline. It must not replace or delete previously frozen requirements.

## Non-negotiable retained requirements
- Account creation is separate from login. OTP login cannot create a new account.
- Borrower/counterparty search accepts name, mobile, PAN, Aadhaar and GSTIN.
- PAN format validation and strict Aadhaar format validation are required.
- Borrower history is disclosed only after explicit OTP consent.
- History includes on-time payments, delayed payments, defaults and default amount, plus marks obtained vs total marks and internal score.
- Start/payment dates use a calendar picker.
- User enters number of instalments; the app calculates end date and instalment amount.
- Repayment schedules are generated when credit is registered and retained with the relationship.
- EMI repayment opens the actual schedule, permits selecting an instalment and part-paying it.
- Non-EMI/bullet repayment also uses a stored schedule/term rather than an ad-hoc free-form repayment.
- Evidence/supporting document is optional for hand loans.
- Promissory note direction is borrower -> lender.
- Promissory note contains principal, interest, tenure, instalments, instalment amount, frequency, dates and consent/execution information.
- Executed promissory note is retained as a Word-compatible document and PDF in Documents.
- Every module screen has module identity plus current user identity at the top.
- Asset Vault provides a broad asset taxonomy including loans given, mutual funds, insurance, deposits, securities, property, gold, vehicles, business interests, pension and other assets.
- Asset scanning is optional and failure must fall back to manual entry rather than crash the module.
- MIS is a first-class module, directly reachable from Home, and reflects connected records automatically.
- MIS contains actual numeric portfolio/asset values, allocation chart, financial-position chart, completed benefit/refund/recovery values and total value generated.
- Home navigation must not repeat the same primary actions at multiple locations.
- QR Udhaar Khata must route directly to the dedicated V6.2 screen, not a legacy/old page.
- TTMM includes group/contributory expenses, member shares, contributions and settlement.
- Module data changes publish through the V6.2 event/source-of-truth layer so connected views refresh.

## Release rule
A CI build pass is necessary but not sufficient for final acceptance. Device smoke/UAT and module-by-module regression remain required before the canonical branch is promoted.
