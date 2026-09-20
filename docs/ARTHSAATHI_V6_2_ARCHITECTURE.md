# ArthSaathi V6.2 — Architecture First

## 1. Core principle
ArthSaathi is one connected financial-life system. A financial relationship, repayment, document, asset, policy or TTMM expense is a source record; Home, MIS, Alerts, AI, Legacy and Claims consume the same record rather than keeping parallel copies.

## 2. Identity and counterparty rule
The signed-in user is the owner of their financial workspace. A counterparty is not a free-standing contact directory entry. A counterparty may be created only while starting a real relationship such as credit, trade credit, rental/lease or guarantee. Search can find an existing counterparty; if absent, the current transaction flow opens counterparty creation and then returns to the same transaction.

## 3. Credit decision journey
`Select/Create counterparty → choose relationship/role → request history consent → OTP verification → show history/outstanding/repayment behaviour/score → enter terms → calculate repayment → guarantor → attach evidence → show promissory note/final terms → credit-registration OTP consent → register active relationship`.

History-sharing consent and credit-registration consent are separate events.

Supported relationship roles include lender, borrower, supplier, buyer, seller, landlord, tenant and guarantor. Credit supports EMI, principal+interest and bullet repayment. Bullet supports a separate final payment date and optional interest-servicing frequency.

## 4. Connected repayment engine
An active relationship creates a repayment schedule. A recorded repayment updates outstanding, due/overdue status, repayment history and derived score. The same change refreshes Home, MIS, alerts and AI insights.

## 5. Document intelligence
All document modules follow:

`Capture/Upload → retain original → OCR/text extraction → document classification → semantic term extraction → critical-term detection/filter → confidence + source page/text → user review/edit → confirmation → structured record`

No AI result silently becomes authoritative. Every extracted field has provenance, confidence and confirmation state.

### Insurance
Policy number, insurer, policy type, holder/insured, premium/frequency, sum assured, dates, next due date, nominee, exclusions, waiting periods, claim conditions, riders, surrender/loan terms and other critical terms.

### Asset Vault
Property papers and bank passbooks are supported. Extract ownership, property/account identifiers, description, address, registration/survey/area, encumbrance/mortgage and bank/branch/account/IFSC/balance information as applicable.

### Rental / Lease
Lease deed/rent agreement extraction covers parties, property, rent, deposit, dates, renewal, notice, escalation, maintenance, utilities, lock-in, termination and special conditions.

### Invoice
Invoice number/date, parties, GSTIN, items, taxable value, tax, total, due date, payment terms and purchase order are retained for future assistance.

## 6. Asset/insurance/legacy graph
Assets link to owner/co-owner, documents, liabilities/loans, insurance, nominee, valuation and legacy. Insurance links to insured, nominee, premium obligations, documents and claims. Will/legacy and claim assistance consume these existing records instead of asking the user to re-enter them.

## 7. TTMM
TTMM is a first-class module: group → members → expense → payer → equal/custom/percentage/share split → balances → settlement → history. Relevant expense/settlement changes emit events into the common event stream.

## 8. MIS
MIS is derived from recorded data and must include visual allocation charts and tables for asset allocation, returns/interest, risk, idle funds, average investment, investment period, charges, opportunity cost and savings created. Opportunity-cost calculations must show period and assumptions.

## 9. Event propagation
`RELATIONSHIP_CHANGED`, `REPAYMENT_CHANGED`, `DOCUMENT_ADDED`, `ASSET_CHANGED`, `POLICY_CHANGED`, `TTMM_EXPENSE_CHANGED`, `CONSENT_CHANGED`, `NOMINEE_CHANGED`, `CLAIM_CHANGED`, `WILL_CHANGED`.

The V6.2 repository is the write boundary and publishes these events. A future server sync can consume the same domain contract.

## 10. UX contract
One V6.2 navigation system. No normal V6.2 route may open V5/V4/V3 activities. Login is the first real screen when unauthenticated. Minimize/resume must retain state. Keyboard handling uses resize/scroll rather than hiding fields. Language is explicit, persisted and application-wide; there is no automatic language switching.

## 11. Security/consent
Sensitive records are stored behind the existing encrypted local persistence boundary during migration. OTP endpoints remain a backend integration boundary; local UI must never pretend an unverified OTP is verified. Original documents are retained and linked to extracted records.

## 12. Implementation order
1. Domain contracts and repository/event boundary.
2. Identity/counterparty + consent gates.
3. Credit and repayment connected flow.
4. Document intelligence abstraction + original-document retention.
5. Insurance, Asset Vault, Rental/Lease and Invoice implementations.
6. TTMM connected data model.
7. MIS charts/tables and derived metrics.
8. Legacy/Legal/Claims consuming the same vault.
9. Unified navigation, language and state-resume hardening.
10. Static build gates, then runtime acceptance testing. **No APK is considered ready before runtime acceptance.**
