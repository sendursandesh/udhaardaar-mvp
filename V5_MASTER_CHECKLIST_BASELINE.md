# Udhaardaar V5 Master Checklist — Authoritative Baseline

This checklist is the authoritative V5 baseline. Status must be treated as **RED / YELLOW / GREEN** based on actual implementation and verification, never by presence of a UI entry or acceptance-gate text alone. No item is GREEN until the implementation is functional, integrated, build-verified, and flow-consistent.

## RED / YELLOW items carried into the correction pass

- [ ] 🔴 OCR/document scanning: real extraction from supported documents, review/correction and confidence handling.
- [ ] 🔴 Formal Credit: complete end-to-end module, consent, document linkage and shared repayment/record integration.
- [ ] 🔴 Nominee / Beneficiary: complete relationship, consent and asset/claim linkage.
- [ ] 🔴 Trusted Person / controlled access: request, consent, scoped access and lifecycle.
- [ ] 🔴 Inheritance & Claims: complete succession/claim lifecycle, heir/nominee relationships, evidence and status.
- [ ] 🔴 Legal Assistance: complete case/workflow and evidence linkage.
- [ ] 🟡 Notifications / reminders / readiness: actionable derived alerts and readiness workflow.
- [ ] 🟡 Production-grade OTP/SMS architecture: provider abstraction and secure production path; demo OTP must not be mistaken for production delivery.
- [ ] 🟡 TTMM: complete participant/profile linkage and settlement integration.
- [ ] 🟡 V5 UI/UX: smooth guided flow, consistent navigation, responsive layouts, step progression and accessible CTAs.
- [ ] 🟡 Cloud/sync/multi-device architecture.
- [ ] 🟡 Security/privacy hardening including protected local data, sessions and granular access.

## Mandatory cross-module invariants

- [ ] Borrower selection returns and persists the selected profile ID into credit registration.
- [ ] Borrower history/exposure is consent-gated by the borrower and audit-recorded.
- [ ] Digital credit documents are generated/reviewed before final credit registration and linked to the credit ID.
- [ ] Borrower consent precedes credit registration.
- [ ] Guarantor consent and guarantee document are linked to the relevant credit ID.
- [ ] Informal, formal and other credit records use a coherent repayment/outstanding model.
- [ ] Repayment requires counterparty consent OTP; overpayment and closed-credit invariants are enforced.
- [ ] Rental/lease is a separate module with agreement document, consent and persistent rental record.
- [ ] TTMM participants are linked to party/profile identities where applicable and settlement updates the ledger.
- [ ] Assets, liabilities, nominees and claims maintain inter-module relationships.
- [ ] Audit trail records material consent, registration, repayment, access, document and claim events.
- [ ] Dates and repayment end dates calculate correctly from start date/tenure/periodicity.
- [ ] PIN entry resolves city/state reliably with validation/fallback.

## Release gate

- [ ] Official V5 workflow passes unit tests.
- [ ] Official V5 acceptance gates pass.
- [ ] APK assembles successfully.
- [ ] APK integrity/installability checks pass.
- [ ] Fresh APK artifact is produced from the corrected main branch.

## Baseline rule

This file is the authoritative checklist for subsequent Udhaardaar V5 work. Future changes must preserve all GREEN items and may not regress them. New requirements are additions to this baseline, not replacements for it.
