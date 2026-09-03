# Udhaardaar V5 Master Checklist — Authoritative Baseline

Process/checkpoint status is independent of APK build status. GREEN means the process is implemented and integrated in source; release/build gates remain separate.

## Correction pass status

- [x] 🟢 26. V5 UI/UX: global V5 visual system now applies consistently across V5 screens — unified off-white surface, navy typography, blue action controls, outlined rounded inputs/spinners, minimum touch targets, accessibility labels, system-bar treatment and keyboard-safe scrolling/focus handling. V5 home retains the guided dashboard and bottom navigation.
- [ ] 🟡 27. Production SMS vendor deployment: provider boundary is implemented without embedding secrets, but live vendor credentials/backend deployment are still required.
- [x] 🟢 28. Inheritance & claims: heir/nominee relationships, evidence capture, vault linkage and lifecycle status transitions are implemented.
- [x] 🟢 29. OCR: real ML Kit extraction with confidence/review is implemented in the sanction-vs-statement document flow; remaining document-specific screens can reuse the same OCR boundary.

## Previously corrected checkpoints

- [x] 🟢 Borrower selection and profile-ID persistence.
- [x] 🟢 Borrower history/exposure consent gate and audit.
- [x] 🟢 Digital document before final credit registration.
- [x] 🟢 Borrower consent before registration.
- [x] 🟢 Guarantor document/consent linkage.
- [x] 🟢 Informal/formal shared repayment model.
- [x] 🟢 Counterparty OTP, overpayment and closed-credit safeguards.
- [x] 🟢 Separate rental persistent record and consent.
- [x] 🟢 TTMM settlement safeguards.
- [x] 🟢 Asset/liability/claim audit relationships.
- [x] 🟢 Audit trail, dates/end-date calculation and PIN resolution.

## Release gate — separate from process status

- [ ] Official V5 workflow passes unit tests.
- [ ] Official V5 acceptance gates pass.
- [ ] APK assembles successfully.
- [ ] APK integrity/installability checks pass.
- [ ] Fresh APK artifact is produced from corrected main.

## Baseline rule

Future changes must preserve all GREEN process checkpoints and may not regress them. New requirements are additions, not replacements.
