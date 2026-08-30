# Udhaardaar — V4/V5 Infrastructure Rebuild Product Specification

## Purpose
Build Udhaardaar as a scalable, privacy-first personal credit, obligation, document, asset and succession/claim-management platform — not merely an udhaar ledger.

## Product promise
Know what you owe, what is owed to you, what you own, where the evidence is, and help authorised family members recover what belongs to you.

## Architecture principles
- One authenticated account and one clear home dashboard.
- Modular domain architecture: Identity, Credit, Repayment, Documents, Assets, Succession/Claims, Legal Assistance, Notifications, Analytics.
- Local-first capable data layer with clear migration path to secure cloud sync/API.
- Role/permission based access; private-by-default asset vault.
- Immutable audit events for consent, document versions, repayments, claim actions and critical changes.
- Version every legally/reputationally important document and term set.
- Never represent an internal score as an official credit-bureau score.
- Formal and informal credit rules are distinct; rental/lease is a separate domain.
- Data minimisation, masking, consent and export/delete controls.

## Authentication & identity
- Single clean login: mobile + PIN/password.
- Account creation with mobile OTP, profile photo and basic identity/contact details.
- Forgot PIN/recovery through OTP.
- Persistent session; logout always returns to login.
- Portrait-first, keyboard-safe responsive forms.
- Verified profile and counterparty identity states.

## Counterparty / borrower profile
- Person/business profile.
- Search by name, mobile, PAN, Aadhaar reference, GSTIN, unique profile ID.
- PAN/Aadhaar/GSTIN structural validation; never imply government verification unless actually performed.
- PIN -> city/state lookup with manual fallback.
- Photo.
- Address/contact information.
- Guarantor relationships.
- Credit history and exposure summary.
- Dispute/correction mechanism.

## Credit domains
### Informal credit
Personal, business, trade credit, advances and other informal obligations.
- Given and received.
- Cash, bank transfer, UPI, cheque and other lending method.
- Principal, ROI, charges, periodicity, tenure, dates.
- EMI, principal+interest, bullet/full repayment and other schedules.
- Trade invoice upload and structured extraction.
- Borrower/guarantor consent.
- OTP consent before final registration where required.

### Formal credit
Bank/NBFC/formal lender.
- Sanction details.
- ROI and all sanctioned charges.
- No counterparty OTP consent requirement merely to record lender-issued repayment/statement information.
- Sanction-letter upload.
- Account-statement upload.
- Loan Charge Audit: extract sanctioned charges/ROI from sanction letter; scan statement transactions; classify actual interest/fees/taxes/penalties/refunds; reconcile sanctioned vs actual; produce variance/evidence report.
- Findings: matches, discrepancy, cannot determine, reversed/refunded, review required.
- Never automatically assert illegality; provide evidence and review-oriented language.

### Rental / lease
Separate module, never reuse personal-credit UI.
- Property/premises.
- Landlord/tenant/counterparty.
- Rent, deposit, escalation, frequency, due date.
- Lease start/end.
- Notice period.
- Maintenance/utilities responsibility.
- Agreement/document vault.
- Rent schedule, arrears and reminders.

## Repayment engine
- Principal/interest accounting.
- EMI and principal+interest schedules.
- Due/upcoming/overdue.
- Advance reminders for all relevant obligation types, including formal and informal credits and rentals.
- Partial repayment.
- Repayment evidence and method.
- Informal repayment consent workflow: party permissions restrict updates to relevant loans; consent/audit record.
- Formal repayment updates may be recorded from lender/bank statements without counterparty consent.
- Full chronological repayment history.

## Digital documents, DPN and guarantee
At credit registration:
- Generate bank-style Demand Promissory Note from finalised terms.
- Generate separate guarantee document for each guarantor.
- Send/display to borrower and guarantor.
- Digital review + consent/OTP as applicable.
- Timestamp, identity, document hash/version and consent event.
- Store completed copies in immutable audit vault.
- Term changes create a new document version and fresh consent where required.
- Status lifecycle: draft, sent, viewed, pending, consented, completed, superseded, archived.
- Templates must be legally reviewed before production representation as enforceable instruments.

## Financial Asset Vault
- Bank accounts/deposits.
- Insurance policies.
- Shares/securities/mutual funds/bonds.
- Pension/retirement assets.
- Loans/receivables.
- Other financial investments.
- Institution, reference/account/policy identifiers with masking.
- Ownership, nominee/beneficiary, co-owner.
- Approximate value and valuation date.
- Maturity/renewal dates.
- Supporting documents.
- Private-by-default access.

## Non-financial Asset Vault
- Land/property/house.
- Vehicles.
- Jewellery/valuables.
- Business interests.
- Intellectual/property interests where appropriate.
- Other material assets.
- Ownership/co-owner, location, registration/reference, approximate value, documents, nominee/heir linkage.

## Trusted person / succession access
- Designated trusted persons.
- Nominee/beneficiary records.
- Permission levels: emergency discovery, view, document access, claim preparation; never automatic transfer of ownership.
- Emergency/succession workflow.
- Access audit log.

## Inheritance & claim assistance
- Mark owner as deceased / succession event with appropriate evidence.
- Death certificate and supporting document repository.
- Asset discovery checklist.
- Nominee and legal-heir information.
- Asset/institution-specific claim checklist.
- Claim application/document tracking.
- Status: identified, documents pending, application prepared, submitted, query received, approved, transferred/closed, disputed.
- Reminders and next actions.
- Evidence bundle generation.
- No claim of guaranteed recovery or legal outcome.

## Legal assistance
- Location-wise lawyer/contact assistance.
- Legal issue categories: credit default, recovery evidence, asset claim, succession/heir documentation, disputes.
- Document/evidence bundle preparation.
- Timeline and case/claim notes.
- Professional referral model.
- Clear distinction between software assistance and legal advice.

## Notifications
- Due dates, overdue, renewals, maturity, lease dates, insurance/policy deadlines, claim tasks and document expiry.
- User-configurable channels and quiet periods.

## Intelligence layer
- OCR/document extraction for invoices, sanction letters, statements, leases and asset documents.
- Human-review checkpoints for uncertain extraction.
- Explainable calculations.
- Credit behaviour analytics.
- Exposure and cash-flow summaries.
- Asset completeness checklist.
- Family/succession readiness score (non-legal, clearly labelled).

## Business model / economic viability
Freemium entry: basic personal credit records, reminders and limited profiles.
Premium: larger secure vault, document intelligence, formal-loan charge audit, advanced reports, family/trusted-person succession features.
Business/trade tier: trade-credit records, invoice workflows, multiple users, reporting.
Professional ecosystem: paid legal/financial-professional referrals or services where legally and ethically permitted.
Do not sell or expose users' sensitive financial data for advertising or lead generation without explicit lawful consent.

## Scalability roadmap
Phase 1: robust Android modular MVP with local encrypted data, document metadata, deterministic calculations and complete UX.
Phase 2: authenticated backend/API, encrypted cloud backup/sync, notification service and controlled multi-device access.
Phase 3: OCR/AI document intelligence, institution-specific parsers and reconciliation.
Phase 4: trusted-person/succession workflows, professional network and claim-service ecosystem.

## Acceptance / QA gates
- No crash on launch, minimise/resume, rotation policy, back navigation or logout.
- Login/create/recovery/logout end-to-end.
- Every mandatory field validates.
- Keyboard never permanently hides a focused field.
- Cursor advances logically.
- Photo persists and displays.
- PIN lookup works with fallback.
- Informal/formal/rental flows remain separated.
- OTP/consent is persisted and auditable.
- DPN/guarantee generated and versioned.
- Documents persist and remain linked to their domain record.
- Sanction-vs-statement reconciliation produces deterministic, reviewable output.
- Repayment schedules and partial payments reconcile correctly.
- Asset and succession access permissions tested.
- Backward-compatible database migration from existing V3/V4 data.
- Debug APK and release candidate APK built from the same tagged source commit.
