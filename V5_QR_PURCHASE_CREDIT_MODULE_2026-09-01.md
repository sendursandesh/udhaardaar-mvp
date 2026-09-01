# Udhaardaar V5 — QR Purchase & Credit Conversion Module

## Purpose
Allow a user purchasing goods/services from a vendor to scan the vendor's QR, capture the transaction, and—only when both sides agree—convert the transaction into an Udhaardaar credit arrangement. The architecture must also support regulated financial institutions such as banks and NBFCs as future/connected credit providers.

## Core flow
`Scan Vendor QR -> identify vendor -> create purchase transaction -> buyer/seller review -> choose Pay Now OR Convert to Credit -> show eligible credit options/limits -> consent -> credit registration -> repayment schedule -> documents/OTP -> active credit`

## QR scan
- Add a dedicated "Scan QR / Pay or Credit" entry point from V5 Home.
- Scan standard vendor/payment QR data and identify the vendor/merchant where supported.
- Display merchant identity before transaction confirmation.
- Never silently create a credit from a scan.

## Transaction capture
Capture at minimum:
- Buyer/user
- Seller/vendor
- Transaction date/time
- Invoice/reference number when available
- Goods/service description
- Gross amount
- Taxes/discounts when available
- Payment/credit choice
- Source QR/payment reference
- Supporting invoice/document

## Mutual credit conversion
After scanning and recording the purchase:
- Buyer can request conversion of the purchase into credit.
- Seller can offer/accept credit terms.
- A credit becomes active only after required parties agree to the final terms and required OTP/document consent is completed.
- Show the buyer's consent-authorized Udhaardaar Score and eligible exposure/limit to the seller/credit provider.
- Show seller/vendor information and proposed terms to the buyer before acceptance.
- Support seller-financed trade credit and third-party financed credit.

## Financial institution integration architecture
Design the module around a provider abstraction so future integrations can include:
- Banks
- NBFCs
- Other permitted/regulated lending partners
- Merchant/vendor financing programs

Provider flow:
`Transaction -> consented data package -> eligibility/pre-screen -> lender offers -> user selects offer -> lender underwriting/decision -> lender disbursement/payment -> Udhaardaar records credit and repayment obligations`

Udhaardaar should not represent itself as a lender merely because the UI displays offers. Provider identity, role, status and source must be explicit.

## Score and eligibility
- Score must be consent-controlled.
- Buyer may authorize score/exposure data for seller or lender evaluation.
- Eligibility must consider configurable provider rules, existing outstanding obligations, repayment history and transaction amount.
- Display reason/status when credit is unavailable rather than silently failing.

## Documents and consent
For credit conversion, generate/store the transaction-backed credit document and supporting invoice/reference. Apply the same V5 consent state machine:
`draft -> document prepared -> borrower/buyer consent -> seller/merchant consent where required -> lender/provider consent where applicable -> active`.

## Repayment
The resulting credit must use the central V5 repayment engine, including the corrected EMI, principal+interest and Bullet modes, and must appear in Repayment Centre as PAYABLE or RECEIVABLE as applicable.

## Security and audit
- QR payload, transaction data, credit offer and consent events must be auditable.
- Do not expose score or financial information without explicit consent.
- Keep provider/lender decisions distinguishable from Udhaardaar's own score calculation.
- Preserve transaction and credit identifiers for reconciliation.

## Release acceptance
This is a core V5 module, not a future placeholder. V5 navigation must expose it and the data model/services must support at least a complete local/demo flow from QR transaction capture through mutually consented credit creation. Production bank/NBFC integrations may require partner APIs, agreements and regulatory/compliance work, but the V5 architecture must be provider-ready.
