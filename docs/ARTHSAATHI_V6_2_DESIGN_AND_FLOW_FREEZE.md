# ArthSaathi V6.2 — Design & Flow Freeze

**Status: FROZEN for V6.2 implementation**  
**Brand:** ArthSaathi  
**Tagline:** Navigate Your Financial Journey  
**Pillars:** Plan • Protect • Grow • Nominate

## 1. Visual design freeze

The V6.2 UI follows the approved premium fintech direction:
- Light premium background with strong navy typography.
- Teal and blue primary actions.
- Gold used as a restrained journey/highlight accent.
- Rounded premium cards, grouped sections and generous spacing.
- Every normal V6.2 page uses the common ArthSaathi shell: canonical logo, page title, contextual subtitle, grouped content cards/inputs, primary action and Back/Home path.
- Keyboard-safe forms and scrollable content are mandatory.
- No generic banking/rupee/piggy-bank logo replacement.

### Approved logo — frozen resource

The canonical frozen resource is `@drawable/arthsaathi_logo`, located at `app/src/main/res/drawable/arthsaathi_logo.xml`.

Visual concept: **teal compass arc + blue route + navy A/gateway + gold journey road + gold north star + route point + blue horizon.**

The same resource is used by the application icon, round icon, login and V6.2 page headers. Any future logo change requires a separate design-version decision.

## 2. Frozen primary page journey

1. Login / Welcome — ArthSaathi branding, tagline and secure entry.
2. Home / Financial Dashboard — financial snapshot plus connected module tiles.
3. Financial Centre — Formal Credit, Funding/Lending, ChargeCheck, QR Udhaar Khata and supporting identity/data modules.
4. Register Financial Relationship — Search/Create Counterparty → History Consent → Credit Details → Repayment Terms → Evidence → Promissory Note → Registration Consent OTP → Relationship created.
5. Borrower/Counterparty Profile — identity, consent status and, only after valid consent, history/outstanding/behaviour/ArthSaathi Score.
6. Repayment Centre — consented relationship → due schedule → record repayment → updated outstanding → event/history.
7. QR Udhaar Khata — Scan/request → identify counterparty → explicit consent/OTP → relationship → ledger entry → outstanding → repayment → score/history.
8. Formal Credit — provider/product → sanction/statement evidence → smart document reader → structured charges/costs → review → save.
9. ChargeCheck — scan/attach sanction letter and account statement → OCR/read pages → extract candidates → compare → evidence-backed review record.
10. Asset Vault — scan/attach original evidence → structured asset record → confidence/source trace → review.
11. Insurance & Benefits Protect — scan/attach policy → terms/critical clauses → due/expiry information → review/confirmation.
12. Rental / Lease — parties → property → rent/deposit/terms → document evidence → review.
13. TTMM — group → members → shared expense → split method → balances.
14. MIS / Reports — connected financial data, allocation, returns, risk, idle funds, charges, interest, opportunity cost and savings.
15. Legacy / Legal / AI — will/nomination, claim assistance, legal assistance and AI financial guidance with appropriate disclaimers.

## 3. ChargeCheck smart-reader flow — frozen

Original evidence is retained. OCR/extraction remains reviewable. Extracted values carry source evidence and must not silently become verified facts. Review distinguishes sanctioned, debited, waived/concession, tax, penalty and other charge categories where identifiable.

## 4. Consent and privacy freeze

- QR scan alone never reveals private credit history.
- History/outstanding/behaviour/score requires explicit counterparty consent and OTP verification.
- Credit registration has a separate final registration-consent OTP after the promissory note is shown.
- QR Khata ledger mutation now also requires explicit OTP confirmation.
- Funding/lending disclosure requires explicit user consent and is limited to authorised providers/participants.
- Original documents remain linked to their source record.

## 5. Implementation rule

All normal V6.2 navigation targets remain within the V6.2 architecture. Legacy V5/V4/V3 activities are compatibility code only and are not normal V6.2 destinations.

This file is the design/flow freeze for V6.2. Future changes should be additive and explicitly versioned rather than silently changing the frozen visual language or primary journeys.
