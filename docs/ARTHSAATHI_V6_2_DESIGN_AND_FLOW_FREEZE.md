# ArthSaathi V6.2 — Design & Flow Freeze

**Status: FROZEN for V6.2 implementation**  
**Brand:** ArthSaathi  
**Tagline:** Navigate Your Financial Journey  
**Pillars:** Plan • Protect • Grow • Nominate

## 1. Visual design freeze

The V6.2 UI is frozen to the approved premium fintech direction shown in the approved design presentation:

- Light premium background with strong navy typography.
- Teal and blue primary actions.
- Gold used as a restrained journey/highlight accent.
- Rounded premium cards, grouped sections and generous spacing.
- Every V6.2 page uses the common ArthSaathi visual shell: approved logo, page title, contextual subtitle, grouped content cards/inputs, primary action, secondary action and Back/Home navigation.
- Keyboard-safe forms and scrollable content are mandatory.
- No generic banking/rupee/piggy-bank logo replacement.

### Approved logo — frozen resource

The repository resource `@drawable/udhaardaar_logo` is the approved ArthSaathi logo resource and must be preserved. The visual concept is:

**teal compass arc + blue route + navy A/gateway + gold journey road + gold north star + route point + blue horizon.**

The app must not silently substitute a newly generated logo. Any future logo change requires a separate design decision outside this V6.2 freeze.

## 2. Frozen primary page journey

1. **Login / Welcome** — ArthSaathi branding, tagline and secure entry.
2. **Home / Financial Dashboard** — financial snapshot plus the connected module tiles.
3. **Financial Centre** — Formal Credit, Funding/Lending, ChargeCheck, QR Udhaar Khata and supporting identity/data modules.
4. **Register Financial Relationship** — Search/Create Counterparty → History Consent → Credit Details → Repayment Terms → Evidence → Promissory Note → Registration Consent OTP → Relationship created.
5. **Borrower/Counterparty Profile** — identity, consent status and, only after valid consent, history/outstanding/behaviour/ArthSaathi Score.
6. **Repayment Centre** — consented relationship → due schedule → record repayment → updated outstanding → event/history.
7. **QR Udhaar Khata** — Scan/request → identify counterparty → explicit consent → relationship → ledger entry → outstanding → repayment → score/history.
8. **Formal Credit** — provider/product → sanction/statement evidence → smart document reader → structured charges/costs → review → save.
9. **ChargeCheck** — scan sanction letter and account statement → OCR/read pages → extract sanctioned charges and actually debited charges → map/compare → variance/effective cost → evidence-backed review record.
10. **Asset Vault** — scan/attach original evidence → structured asset record → confidence/source trace → review.
11. **Insurance & Benefits Protect** — scan/attach policy → terms/critical clauses → due/expiry information → review/confirmation.
12. **Rental / Lease** — parties → property → rent/deposit/terms → document evidence → review.
13. **TTMM** — group → members → shared expense → split method → balances.
14. **MIS / Reports** — connected financial data, allocation, returns, risk, idle funds, charges, interest, opportunity cost and savings.
15. **Legacy / Legal / AI** — will/nomination, claim assistance, legal assistance and AI financial guidance with appropriate disclaimers.

## 3. ChargeCheck smart-reader flow — frozen

### Inputs

- Sanction letter / sanction advice / loan agreement.
- Account statement covering the relevant debit period.
- Optional loan/account identifier and user notes.

### Reader pipeline

1. User selects or scans the **sanction letter**.
2. Original document is retained; OCR reads every available page.
3. Smart extraction searches for sanctioned financial terms, including as applicable:
   - sanctioned principal / limit
   - interest rate and benchmark/spread
   - processing fee
   - documentation fee
   - legal/valuation/technical charges
   - insurance
   - GST/tax
   - prepayment/foreclosure charges
   - penal/default interest
   - bounce/return charges
   - annual/renewal charges
   - other stated fees/charges
   - fee waivers/concessions
4. User selects/scans the **account statement**.
5. Original statement is retained; OCR reads every available page.
6. Smart extraction searches transaction lines for actual debits, using charge-related descriptions and amounts. It must preserve page/line evidence where available.
7. The app presents a review table with **Sanctioned / Actually Debited / Variance / Evidence**.
8. No extracted amount is silently treated as verified. User can edit/confirm mappings.
9. Confirmed results are saved to ChargeCheck with document references and source text/page evidence.
10. The app may flag a review/complaint opportunity, but it must not assert a legal violation or refund entitlement automatically.

### Data integrity rules

- Keep original evidence.
- Keep extracted text/source trace.
- Keep extraction confidence/review state.
- Distinguish **SANCTIONED**, **DEBITED**, **WAIVED/CONCESSION**, **TAX**, **PENALTY**, and **OTHER** where identifiable.
- Never compare only one aggregate charge if item-level evidence is available.

## 4. Consent and privacy freeze

- QR scan alone never reveals private credit history.
- History/outstanding/behaviour/score requires explicit counterparty consent and OTP verification.
- Credit registration has a separate final registration-consent OTP after the promissory note is shown.
- Funding/lending disclosure requires explicit user consent and is limited to providers/participants authorised for the request.
- Original documents remain linked to their source record.

## 5. Implementation rule

All normal V6.2 navigation targets must remain within the V6.2 architecture. Legacy V5/V4/V3 activities are not normal V6.2 destinations.

This file is the design/flow freeze for V6.2. Future changes should be additive and explicitly versioned rather than silently changing the frozen visual language or primary journeys.
