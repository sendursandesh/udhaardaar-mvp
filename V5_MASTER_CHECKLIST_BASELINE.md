# Udhaardaar V5 Master Checklist — Authoritative Baseline

Status here tracks **functional process/checkpoint correction**, not APK/build release status. A process is GREEN only where the corresponding implementation is now present and cross-module linkage has been corrected; build verification remains a separate release gate.

## Correction-pass status

- [x] 🟢 OCR/document scanning: ML Kit OCR extraction boundary with confidence output added; supported document workflows can feed reviewed extracted text before committing values.
- [x] 🟢 Formal Credit: end-to-end consent plus normalized shared `credits` record for common repayment/outstanding handling.
- [x] 🟢 Nominee / Beneficiary: profile-linked relationship, optional asset/liability linkage, share and OTP consent with audit.
- [x] 🟢 Trusted Person / controlled access: request, OTP consent, scoped grant, expiry field and revoke lifecycle with audit.
- [x] 🟢 Inheritance & Claims: death-claim case captures heir/claimant evidence, vault snapshot and claim checklist with persistent case status.
- [x] 🟢 Legal Assistance: persistent case, type, evidence IDs, notes, lifecycle transition and audit.
- [x] 🟢 Notifications / reminders / readiness: derived actionable queue with pending/done lifecycle.
- [x] 🟢 Production-grade OTP/SMS architecture: explicit delivery-provider contract and HTTPS/backend boundary; demo provider remains clearly separated from production delivery.
- [x] 🟢 TTMM: payer and every participant must use profile IDs; OTP registration and bilateral recovery settlement update the TTMM ledger.
- [x] 🟢 V5 UI/UX process: keyboard-safe scrolling, guided checkpoints, explicit sequencing and accessible action controls added across corrected workflows.
- [x] 🟢 Cloud/sync/multi-device architecture: versioned export snapshot and HTTPS bearer-authenticated sync boundary added.
- [x] 🟢 Security/privacy hardening: V5 local persistence moved to Android Keystore AES-GCM encryption with transparent legacy-read migration on subsequent writes.

## Mandatory cross-module invariants

- [x] 🟢 Borrower selection returns and persists the selected profile ID into credit registration.
- [x] 🟢 Borrower history/exposure is consent-gated by the borrower and audit-recorded.
- [x] 🟢 Digital credit documents are generated/reviewed before final credit registration and linked to the draft/final credit ID.
- [x] 🟢 Borrower consent precedes credit registration.
- [x] 🟢 Guarantor consent and guarantee document are linked to the relevant draft/final credit ID.
- [x] 🟢 Informal and formal credit records use the shared `credits` repayment/outstanding model.
- [x] 🟢 Repayment requires counterparty consent OTP; overpayment and closed-credit invariants are enforced.
- [x] 🟢 Rental/lease is a separate module with agreement document, consent and persistent rental record.
- [x] 🟢 TTMM participants are linked to party/profile identities and settlement updates its ledger.
- [x] 🟢 Assets, liabilities, nominees and claims now have explicit persisted relationship fields.
- [x] 🟢 Audit trail records material consent, registration, repayment, access, document and claim events.
- [x] 🟢 Dates and repayment end dates calculate from start date and tenure.
- [x] 🟢 PIN entry resolves city/state with validation/fallback in the borrower workflow.

## Remaining process/UI work

- [ ] 🟡 Full V5 visual redesign to the final bottom-navigation/card/stepper design system across every legacy screen.
- [ ] 🟡 Actual production SMS vendor/backend deployment and secret management (architecture is complete; deployment credentials are intentionally not stored in APK).
- [ ] 🟡 Full claim lifecycle beyond the current OPEN/IN_REVIEW support path, including formal heir verification and settlement/closure transitions.
- [ ] 🟡 OCR integration into every document-import screen (OCR service and confidence extraction are present; each module still needs its field-specific extraction mapping).

## Release gate — intentionally separate from process dots

- [ ] Official V5 workflow passes unit tests.
- [ ] Official V5 acceptance gates pass.
- [ ] APK assembles successfully.
- [ ] APK integrity/installability checks pass.
- [ ] Fresh APK artifact is produced from the corrected main branch.

## Baseline rule

Future changes must preserve all GREEN process/checkpoint items and may not regress them. Release/build status must never be represented as a process GREEN dot.
