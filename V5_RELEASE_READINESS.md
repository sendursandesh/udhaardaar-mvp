# V5 release readiness snapshot

## Domain services committed
- Authentication/session architecture: V5 navigation contract
- Profiles/guarantors: persistence facade + domain models
- Informal credit: credit service + consent-gated score
- Informal repayment: bilateral request + OTP confirmation
- Formal credit: charge audit engine/service
- Rental/lease: separate persistence service
- Documents: private vault + hashing/versioning/audit contracts
- Financial/non-financial assets: persistence service
- Succession/claims: claim persistence + statuses
- Reminders: unified service
- Access control: privacy policy

## Not yet release-ready
The source still requires final screen wiring, complete Room/SQLite V5 migration implementation, real OTP provider integration, OCR/document extraction adapter, complete document rendering/delivery, authentication lifecycle integration, automated tests, real-device QA and successful APK build.

Do not label an APK as V5 final until every item above is passed.
