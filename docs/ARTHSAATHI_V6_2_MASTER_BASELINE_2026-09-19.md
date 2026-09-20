# ArthSaathi V6.2 — Master Baseline Freeze (19 Sep 2026)

This document is the source-of-truth checklist for the next APK build on branch `arthsaathi-v6.2-architecture-contributions-mis`. It consolidates decisions and corrections recorded during the last month and must be treated as a release gate.

## 1. Identity and approved visual system
- Brand: **ArthSaathi**.
- Primary journey line: **Navigate Your Financial Journey**.
- Retained ownership message: **Your Asset. Your Record. Your Right.**
- Retained ownership tags: **OWN • RECORD • PROTECT • CLAIM**.
- Approved concept: compass / A-gateway / journey-road / star.
- Approved wordmark treatment: blue/gold ArthSaathi wordmark beneath the logo.
- Approved premium palette: navy blue, blue, teal and gold.
- Rounded premium cards, compact bottom navigation and compact information hierarchy.
- User profile photo must be visible in the approved home/profile presentation.
- No legacy Udhaardaar logo, mixed legacy fonts, legacy palette or V5/V4/V3 home/navigation is permitted.

## 2. Core V6.2 architecture
The app uses one shared source-of-truth store plus the V6.2 event bus. Modules are connected through domain records and events rather than isolated duplicate ledgers.

Core modules retained:
Home & Financial Dashboard; Profile & Identity; Family; Contacts & People; Address & Geolocation; Informal Credit/Udhaar; QR Udhaar Khata; Credit Intelligence/Score; Trade Credit; Accounting/ERP Integration; Formal Credit; Funding & Lending Marketplace; Repayment Centre; Guarantor; ChargeCheck; Asset Vault; Liability Vault; Insurance & Protection; Government Schemes & Benefits; Rental & Lease; TTMM Shared Expenses; Group Contributions / Share & Settle; Document Vault & Intelligence; MIS & Financial Analytics; Reports & Statements; Inheritance & Claim Assistance; Will/Nomination/Legacy; Legal Assistance; AI Financial Advisor; Alerts & Notifications; Consent/OTP/Digital Confirmation; Security & Account Management; Integration/API Platform.

## 3. Identity, privacy and consent
- Account/user isolation applies to every financial record.
- Mobile + OTP is the authenticated login pattern.
- Mobile numbers are limited to 10 digits; Aadhaar/PAN/GSTIN/PIN/OTP/email have field validation.
- Protected borrower/counterparty history is not exposed merely by search.
- History sharing, credit registration, document confirmation, repayment confirmation, funding profile sharing, ERP data sharing, claim/legal handoff and QR-Khata confirmation use explicit consent as applicable.
- Credit registration shows final terms before the separate OTP consent.
- Repayment updates respect the relevant consent/authorization rules.
- User profile editing is restricted to the authorized user.
- Location/PIN assistance is editable/overridable by the user.

## 4. Credit, trade credit and repayment
- Credit registration supports personal/business credit and credit-type-specific flows.
- Search/autocomplete supports name, mobile, PAN, Aadhaar and GSTIN.
- Guarantor structure is retained.
- Repayment modes: EMI, principal + interest, bullet; bullet interest period options are retained.
- Repayment schedules and outstanding values must update correctly and chronologically.
- Trade credit includes invoice details, credit period/due date, outstanding/overdue, debit/credit notes and source traceability.
- Invoice upload/OCR and QR-based capture remain in scope.
- Formal credit retains sanctioned-vs-actual ChargeCheck, including fees, insurance, taxes, penalties, refunds, variance and evidence.
- Digital demand-promissory-note/document consent and timestamping remain in scope.

## 5. Asset, protection and legacy
- Asset Vault supports financial and non-financial assets with lifecycle state and current/historical values.
- Liability Vault, Insurance & Protection and Rental/Lease remain connected modules.
- Nominee, Will/Legacy, Inheritance/Claim Assistance and Legal Assistance remain in scope.
- Document Vault retains originals and traceable AI extraction proposals; critical terms remain source/page traceable.
- Due-date/reminder capability remains part of the connected architecture.

## 6. TTMM — Together • Share & Settle
TTMM explicitly handles the common group-expense case where one person pays first and others contribute later.

Required flow:
**Group → Members → Expense → Allocation → Member Share → Contribution(s) → Remaining Due → Settlement → MIS**

Required behavior:
- Equal split, custom amounts, percentage split and shares/units.
- Example: local gully-cricket group expense of ₹5,000 paid initially by one member; the app calculates each member's share and records later partial/full contributions.
- Every contribution is linked to the originating expense.
- Contribution cannot exceed the member's remaining share.
- Settlement is also linked to the originating expense and cannot exceed the remaining due.
- Expense, contribution and settlement events feed MIS through the common event bus.
- TTMM must show expense total, contributed value and outstanding value.

## 7. MIS — connected financial intelligence
MIS must show both actual numbers and visual allocation/position views.

Required:
- Current portfolio/asset value.
- Asset-allocation donut chart.
- Portfolio/financial-position donut covering assets, liabilities, informal credit exposure and open TTMM dues.
- Assets and liabilities as actual values.
- Informal credit exposure and TTMM open dues as actual values.
- Completed government/other benefits with actual value generated.
- Completed refunds recovered.
- Completed claims/recoveries.
- Total value generated = successfully completed benefit + refund + recovery value.
- Pending/rejected records excluded from completed-value totals.
- TTMM expense, contributed and outstanding values.
- Returns/interest, charges, savings, idle funds, risk tags, opportunity-cost indicators and historical asset value.
- Closed/sold assets remain historical but are excluded from current asset value.

## 8. Cross-module events
The canonical event contract includes profile, family, address, relationship, repayment, document, asset, liability, policy, TTMM expense, TTMM contribution, TTMM settlement, savings, consent, nominee, claim, will, trade-credit import, formal-loan, ChargeCheck, funding and alert changes.

MIS consumes the same source-of-truth records/events; it must never become a second financial ledger.

## 9. Release/QA gates
Before a new APK is called releasable:
1. Compile and unit tests pass.
2. Architecture audit passes.
3. All primary V6.2 routes resolve; no V5/V4/V3 route is reachable.
4. Home/login/module presentation uses the approved logo, wordmark, font and palette.
5. No menu crash, launch crash or minimize/reopen regression.
6. Keyboard/scroll behavior allows every field to be reached.
7. Required field length/format validations pass.
8. Consent/OTP gates are enforced at protected operations.
9. User/account isolation is verified.
10. TTMM expense/contribution/settlement accounting is verified for partial and full payments.
11. MIS values reconcile to source records and charts.
12. APK artifact is produced from this exact branch and exact verified commit; no older golden/c480/QA APK may be substituted silently.

## 10. Branch rule
The active build branch is `arthsaathi-v6.2-architecture-contributions-mis`. Changes must be made here unless a newer branch is first verified to contain all of this baseline. The previously rejected/obsolete branches are not valid substitutes.
