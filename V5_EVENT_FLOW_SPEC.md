# V5 Sequential Event Integration

Profile created -> borrower selected -> credit terms saved -> borrower explicitly consents to score -> Udhaardaar Score displayed -> guarantor profile created (if applicable) -> DPN generated -> guarantee generated -> documents sent/viewed -> OTP consent -> credit registered.

Informal repayment: scheduled -> either party requests update -> counterparty OTP consent -> confirmed -> partial/fully reconciled. No direct mark-paid action.

Formal repayment: bank/account statement is authoritative input and does not require counterparty OTP consent.

Formal charge audit: sanction letter uploaded -> statement uploaded -> normalized/OCR data -> sanctioned vs actual reconciliation -> evidence-backed findings.

Rental: lease draft -> terms -> agreement -> active schedule -> due/arrears -> closure.

Assets: asset created -> proof -> nominee/trusted access -> maturity/renewal -> succession event -> claim file -> documents -> submitted -> query/approval/transfer/closure.

All critical consent/document/claim events are versioned and audit logged. UI must prevent skipping mandatory transitions.
