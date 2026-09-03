# Udhaardaar V5 Thorough Rectification Specification

## Release gate
The APK is not considered complete merely because Gradle compiles. Release requires functional/regression QA of every critical flow.

## 1. Professional information architecture
- Replace dense/clumsy module grid with a task-oriented home dashboard.
- Primary actions: Record Credit, Record Repayment, Scan QR Credit, Parties, Documents, Repayment Centre.
- Secondary/support: Reports, Profile, Settings/Help.
- Each flow uses clear progress, Back/Next, contextual guidance and validation.
- Density-safe layouts; no clipped/crushed/overlapping content on small and large Android screens.

## 2. Credit registration
Party identification/search -> existing/new party -> credit type -> terms -> repayment calculation -> guarantor -> document preview/generation -> required OTP consents -> final registration.

## 3. QR-based credit
- Provide a dedicated Scan QR action and Android camera permission handling.
- Decode standard QR payloads using a robust QR scanning library.
- Parse common structured invoice/payment payload fields when present: vendor/seller name, invoice/reference number, date, amount, tax/total, GSTIN and other available fields.
- Populate the credit form automatically, clearly mark scanned fields, and allow user correction before saving.
- Preserve the raw decoded payload and scan timestamp as evidence metadata.
- If a QR is not a supported structured credit/invoice payload, show decoded content for confirmation rather than silently inventing fields.

## 4. Timestamped evidence preservation
Every transaction/event/document action must retain:
- immutable event/record identifier
- device timestamp in UTC and displayed local time
- event type and actor/party identity
- source/origin (manual, QR scan, document generation, OTP consent, repayment, etc.)
- relevant transaction/document reference
- prior/current state where applicable
- raw QR payload where applicable
- document hash/version metadata where applicable

Corrections must create a new event/version rather than overwriting historical evidence.

## 5. Demand promissory note
- Generate the demand promissory note from the registered credit data.
- Imprint transaction/document timestamp and a unique document/evidence ID on the note.
- Preserve generation timestamp, document version and hash/evidence metadata alongside the record.
- Subsequent changes must not silently mutate the prior executed version.

## 6. Dates
- Native date picker for start/end dates.
- Start date plus repayment period automatically calculates end date.
- Allow explicit end-date override with clear indication.
- Provide calendar-oriented selection where platform capability supports it.

## 7. Repayment centre
- Separate payable and receivable views with complete transaction details.
- Repayment recording requires appropriate counterparty consent/OTP before it becomes finalized.
- Preserve repayment event timestamps and consent evidence.
- Enforce party-level permissions.

## 8. PIN autofill
- 6-digit PIN validation.
- Automatic city/state lookup with robust error handling and editable result.

## 9. QA gate
Test clean install, upgrade from previous V5, launch, navigation, profile create/search/update, credit creation, QR scan, date calculation, guarantor, document generation, OTP/consent sequencing, promissory note evidence, repayment, dashboard updates, persistence/reopen and screen-density safety. Do not label APK thoroughly rectified unless all critical paths pass.
