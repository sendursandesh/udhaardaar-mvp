# V5 next integration checkpoint

Implemented service foundations now cover:
- bilateral informal repayment request + counterparty OTP confirmation
- borrower-consent gate for Udhaardaar Score
- OTP consent event/audit persistence
- separate rental/lease persistence
- app-private document storage + SHA-256 metadata
- financial/non-financial asset persistence
- inheritance claim persistence
- unified due-date reminder records
- privacy/access policy

Remaining implementation gates before APK release:
- wire all services to real V5 screens and navigation
- migrate existing database records safely
- replace demo/local OTP with approved OTP provider adapter for production
- OCR adapter for invoices, sanction letters and statements
- full DPN/guarantee document rendering and delivery
- end-to-end authentication/session lifecycle
- real device QA, build and APK artifact verification
