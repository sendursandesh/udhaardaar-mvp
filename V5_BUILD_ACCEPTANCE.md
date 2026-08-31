# V5 Build Acceptance — mandatory before APK delivery

## Credit registration
- [ ] Borrower can be searched/created and full profile persisted.
- [ ] Guarantor can be separately created, linked and documented.
- [ ] Credit type determines the correct workflow; rental is never shown as personal credit.
- [ ] Informal credit captures direction, method, principal, ROI, charges, dates and repayment method.
- [ ] Borrower consent is explicitly obtained before displaying Udhaardaar Score.
- [ ] Score is explainable, bounded and labelled as internal/non-statutory.
- [ ] DPN generated from final terms.
- [ ] Guarantee generated for each guarantor.
- [ ] Borrower and guarantor receive/review and OTP-consent documents.
- [ ] Consent/document version/hash/timestamp are audit retained.

## Repayment
- [ ] An instalment row is not a direct "mark paid" action for informal credit.
- [ ] Either lender or borrower can initiate a repayment update request.
- [ ] Counterparty OTP consent is mandatory before an informal repayment becomes confirmed.
- [ ] Partial payments are supported and outstanding balance is recalculated.
- [ ] Payment method/reference/evidence are retained.
- [ ] Formal-bank repayment can be reconciled from statements without counterparty consent.

## Formal loan audit
- [ ] Sanction letter can be uploaded and normalized.
- [ ] Account statement can be uploaded and normalized/OCR-ready.
- [ ] Sanctioned ROI/fees and actual statement charges are reconciled.
- [ ] Variances are shown with evidence and cautious review language.

## Vault / succession
- [ ] Financial asset categories and documents.
- [ ] Non-financial asset categories and documents.
- [ ] Nominee/beneficiary and trusted-person permissions.
- [ ] Succession event + death certificate.
- [ ] Claim checklist/status/evidence bundle.
- [ ] Legal assistance/referral workflow.

## App quality
- [ ] Login/create/recovery/logout.
- [ ] Minimise/resume without unintended logout/crash.
- [ ] Portrait + keyboard-safe forms.
- [ ] Cursor progression.
- [ ] Photo persistence.
- [ ] PIN lookup + manual fallback.
- [ ] PAN/Aadhaar/GSTIN/mobile validation.
- [ ] Database migration from prior V3/V4 data.
- [ ] APK compile/integrity/package verification.
- [ ] Manual smoke test on a real Android device before release.
