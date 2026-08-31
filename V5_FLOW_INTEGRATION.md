# V5 integrated flow

## Informal credit registration
1. Create/select borrower profile.
2. Capture credit terms and direction (given/received).
3. Request borrower consent for score access.
4. Verify borrower OTP consent.
5. Calculate and display explainable Udhaardaar Score.
6. Create/link guarantor profile(s), when applicable.
7. Generate DPN and guarantee documents from final terms.
8. Send documents to borrower/guarantor.
9. Record viewed/OTP-consented status, hash, timestamp and version.
10. Register credit only after required document consent.
11. Activate repayment schedule and reminders.

## Informal repayment
1. Open schedule only to view details; no direct mark-paid action.
2. Either lender or borrower creates a repayment request.
3. Capture amount/date/method/reference/evidence.
4. Send request to counterparty.
5. Counterparty verifies OTP.
6. Confirm repayment and update outstanding/partial balance.
7. Append audit event.

## Formal credit
1. Create formal loan record.
2. Upload sanction letter.
3. Extract/normalize sanctioned ROI and charges.
4. Upload account statement.
5. Extract/normalize actual interest/fees/taxes/penalties/refunds.
6. Reconcile sanctioned vs actual.
7. Show evidence-linked findings and variance.
8. Do not request informal counterparty consent.

## Rental/lease
Separate creation, agreement, rent schedule, deposit, escalation, notice, arrears and reminders.

## Asset/succession
1. Add financial/non-financial asset.
2. Attach proof and ownership metadata.
3. Add nominee/beneficiary and trusted-person permissions.
4. On succession event, create inheritance workspace.
5. Add death certificate/heir documents.
6. Create institution-specific claim.
7. Track checklist, submission, queries, approval/transfer/dispute.
8. Generate evidence bundle and optionally route to legal assistance.
