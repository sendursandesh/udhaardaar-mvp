# Udhaardaar V3 — Master Product Acceptance Requirements

This is the consolidated acceptance baseline for the launch candidate. It combines the original V3 direction, the morning correction set, subsequent corrections, privacy/consent requirements, guarantor and rental/lease requirements, and the three-year informal-credit infrastructure vision.

## Identity and onboarding
- Mobile-number-first authentication; name is never an authenticator.
- OTP verification for login and protected actions.
- New-user profile creation is separate from login.
- Strict validation for mobile, alternate mobile, email, PAN, GSTIN, Aadhaar and PIN fields.
- Profile photo support.
- City/state/address assistance from PIN/location, with user confirmation.

## Privacy, consent and trust
- No third-party financial/profile data is disclosed without appropriate authorisation.
- Search results expose only minimum information needed for identification.
- Detailed credit history requires consent/authorisation.
- Repayment updates are restricted to parties authorised for that credit.
- Consent, OTP verification and material changes are auditable.
- Data access and document access must be logged.
- Privacy is enforced in the data-access layer, not only by hiding UI controls.

## Credit products
- Personal credit.
- Business credit.
- Trade credit with invoice/document support.
- Rental/lease obligations.
- Advance and extensible future credit types.
- Credit direction: given/received.
- Principal, ROI, periodicity, tenor, dates and repayment method.
- EMI and principal+interest calculations.
- Total payable, outstanding, due and overdue values.

## Counterparties and guarantor
- Borrower/lender search by authorised identifiers.
- Existing borrower profile or creation of a new profile.
- Borrower history available subject to authorisation.
- Guarantor profile, relationship, identity, coverage/obligation and documentation.
- Guarantor consent/OTP where applicable.
- Guarantor information is protected like other sensitive financial information.
- Co-borrower support and consent where required.

## Digital documentation
- Human-readable digital credit document before consent.
- OTP-linked digital consent.
- Supporting invoices/documents linked to the transaction.
- Document status and version history.
- Structured rental/lease agreement generation from entered terms.
- Generated documents remain linked to parties, transaction, version and consent/audit data.
- Legal wording must be configurable and reviewed before production legal reliance.

## Repayment
- Chronological repayment-centre schedule.
- Individual-credit due/overdue status.
- Record actual repayment events without destroying prior history.
- Consent-controlled repayment updates.
- Early payment and repayment behaviour history.
- Outstanding balance recalculation after each accepted payment.
- Reports must reconcile with transaction and repayment records.

## Reports and scoring
- Borrower credit history.
- Active/closed/overdue obligations.
- Repayment history.
- Credit score/reputation indicators derived from verified records.
- Authorised score view only.
- No opaque public negative listing.

## UX and quality
- Professional, trustworthy visual language.
- Light sky-blue direction with clear touchpoints and readable forms.
- Keyboard-safe scrolling on every long form.
- No locked scroll regions.
- Clear mandatory/optional labels.
- Automatic cursor/focus where helpful.
- Strong empty, error, loading and success states.
- No dead buttons or inaccessible screens.

## Scale and future readiness
- Stable unique IDs and relational transaction structure.
- Append-only/auditable material financial events.
- Separation of identity, consent, transaction, repayment and document data.
- Backend-ready architecture; local storage must not become the long-term system of record.
- Offline/poor-connectivity tolerant workflows where practical.
- Designed for large-scale onboarding and future API/integration capability.
- Regulatory/legal configuration points must be kept separate from UI logic so requirements can evolve by jurisdiction and product.

## QA acceptance
For each applicable workflow: run valid, invalid, boundary, duplicate, cancellation, back-navigation, update, consent, OTP, report-reconciliation and privacy-authorisation scenarios. Repeat critical transaction/report/update scenarios 10 times. Do not declare release readiness solely from a successful Gradle build.
