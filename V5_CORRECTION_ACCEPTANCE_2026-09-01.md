# Udhaardaar V5 — Correction Acceptance Requirements

This branch is based only on `v5-completion-2026-08-31`. Legacy V2/V3/V4 screens must not be used as the functional baseline.

## 1. Party / borrower onboarding
- Credit registration starts with searchable existing profiles using name, mobile, PAN and Aadhaar-supported identifiers.
- If no profile exists, user can create a new borrower/counterparty profile in-flow and continue without losing entered credit data.
- Selected party's existing credit history and current outstanding exposure are shown before credit terms are finalized.

## 2. Informal credit execution order
Required state machine:
`Party selected/created -> credit terms -> repayment schedule -> guarantor (optional/required when selected) -> document generation -> document preview -> borrower consent OTP -> guarantor consent OTP (when guarantor exists) -> final registration -> score update`.
- No executed document may be marked executed before the relevant consent OTP succeeds.
- DPN/credit agreement and guarantor guarantee must be generated from the final terms and stored with immutable audit metadata.

## 3. Guarantor
- Add/remove guarantor in credit registration.
- Select existing guarantor profile or create one.
- Generate guarantor guarantee document from final credit terms.
- Require guarantor OTP consent before the credit can become ACTIVE when a guarantor is included.

## 4. Repayment consent and permissions
- Informal repayment entries require counterparty consent OTP before becoming confirmed ledger entries.
- A user can initiate a repayment request only for credits involving that user.
- Formal-credit repayments do not require bank/counterparty OTP consent; they are recorded from supporting bank/account evidence and user-authorized updates.
- Pending, consented, rejected and confirmed repayment states must be distinct and auditable.

## 5. Repayment Centre
Every credit row must clearly show:
- Credit type
- Counterparty
- Direction: PAYABLE / RECEIVABLE
- Original principal/amount
- Interest terms
- Repayment mode
- Next due date
- Next due amount
- Total paid
- Outstanding amount
- Status
- Consent state for pending repayment updates

Receivables must be included in the same centre and not hidden behind a separate legacy flow. Filters: All / Payable / Receivable / Due / Overdue / Pending Consent / Settled.

## 6. Bullet repayment calculation
Bullet is not EMI.

Modes:
- `BULLET_PRINCIPAL_ONLY`: principal is due in full on the final maturity date.
- `BULLET_INTEREST_MONTHLY`: monthly interest is separately due on each selected monthly due date; principal is due in full on maturity.
- `BULLET_PRINCIPAL_PLUS_INTEREST_AT_END`: no EMI-style interim due; cumulative principal + applicable interest is shown as the single maturity due amount.

The UI must dynamically show only the fields applicable to the selected mode and the repayment centre must display the corresponding due schedule/amount.

## 7. Rent / Lease
- Dedicated rental/lease workflow, not a generic credit form.
- Start and end dates selected using a calendar date picker.
- Stored/displayed lease dates use `ddMMyyyy`.
- Rent due day uses a scrollable numeric day selector (1–31), with validation against the applicable month.
- Save must return one specific validation error at a time, identifying the exact field/problem; no generic multi-error crash/toast.
- Rental save must persist and reload correctly.

## 8. Formal credit charge audit
- Upload sanction letter and account statement.
- Extract sanctioned ROI, fees, charges, taxes, penalties and other applicable charge terms from the sanction document.
- Extract actual debits/charges from statement data.
- Produce a comparative table for every charge category: Sanctioned / Actual / Difference / Excess or Shortfall / Evidence reference.
- Excess debited charges must be prominently identified.
- Generate a printable/shareable comparison report and provide Save/Share actions.
- Keep source documents and comparison report linked to the credit record.

## 9. Documents & consent vault
- Each user has a Documents & Consent area.
- All documents for that user's credits are indexed by credit, document type, party and execution/consent status.
- Documents can be previewed, downloaded/saved and shared through Android sharing.
- Consent audit stores who consented, what document/version was consented to, when, and consent status.

## 10. Financial/non-financial asset vault and claims
Expose functional modules for:
- Financial assets
- Non-financial assets
- Ownership details
- Nominee / trusted person
- Supporting documents
- Inheritance/succession information
- Claim initiation, evidence and status tracking
- Legal consultation request/workflow

These modules must be reachable from V5 home and persist data; placeholder screens are not acceptance.

## 11. Udhaardaar Score
- Calculate and display a score for eligible user profiles based on recorded credit/repayment history and the defined scoring model.
- Score must show component factors and calculation date/version.
- Score visibility must be consent-controlled.
- Add `Request Score Verification/Consent` flow for sharing/verifying score with another party.
- During credit creation, show the counterparty's consent-authorized score (or clearly state that it is unavailable/not authorized) before final registration.
- Score changes must be auditable after confirmed repayment/credit events.

## 12. Keyboard / form UX
- All form screens must use keyboard-aware scrolling/resizing so the active field remains visible.
- Focus transitions must not place the active field behind the IME.
- Test every long form on small and large Android screens.

## 13. Release gate
Do not label the APK final until all requirements above are implemented, wired to V5 navigation/data storage, and verified by build + automated tests + manual smoke/flow QA. A successful Gradle build alone is not sufficient.
