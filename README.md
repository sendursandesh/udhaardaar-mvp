# ArthSaathi — consolidated master build

## Working baseline

- **Working branch:** `arthsaathi-step1-5-functional-audit-2026-10-18`
- **Android application ID:** `com.arthsaathi.master`
- **Gradle version identity:** versionCode `800`, versionName `8.0`
- **Canonical UI:** `ArthSaathiHomeActivity` and `ArthSaathiMasterModuleActivity`
- **Canonical module registry:** `ArthSaathiArchitectureRegistry`
- **Approved brand mark:** `app/src/main/res/drawable/arthsaathi_logo.xml`

Do not treat the earlier APK or a successful compile as final acceptance. The complete architecture and all module workflows must pass source review, build, instrumentation, regression, and device/emulator checks before a final APK is released.

## Consolidated architecture and data ownership

The persistent navigation is **Home | Credit | Repay | Vault | More**. Each feature should have one canonical route; legacy screens must not create duplicate entry points or competing record owners.

The canonical registry includes:

- **Credit and repayment:** Register Credit, Loans & Udhaar, Repayment Centre, QR Khata / Trade Credit, Formal Credit & Charge Review, Credit Intelligence & Score, Guarantor Records, Rental & Lease.
- **Assets and growth:** Asset Vault, Liability Vault, Portfolio & Investments, Portfolio Switch Analysis, Funding Marketplace.
- **Reporting and money operations:** MIS / Money Report, Benefits & Refunds, ChargeCheck, Revenue & Payments, Group Khata / TTMM.
- **Protection and claims:** Insurance & Protection, Claim Assistance, Will / Inheritance / Legacy, Legal Help, Advocate Directory, Document Vault.
- **People and platform:** People & Relationships, Profile & Identity, Family & Nominees, Address & Geolocation, Security & Consent, Alerts & Notifications, Accounting & ERP Adapters, Integrations, AI Advisor.

## Non-negotiable acceptance criteria

1. Correct branch and exact source commit must be recorded for every release APK.
2. All canonical modules must open, show the correct title and approved logo, and have functional save/edit/detail actions appropriate to that module.
3. Validate every field, keyboard/focus/cursor progression, scrolling, dates, numeric bounds, PAN/GSTIN/email/mobile/PIN formats, and error recovery.
4. Validate repayment schedules, outstanding balances, closure, benefit/refund totals, revenue received/refunded, charge variances, group contributions/settlements, portfolio values, and MIS charts/figures.
5. Every create/edit/repayment/settlement/claim/payment must propagate to all dependent records and MIS views without stale or duplicate totals.
6. Credit history remains hidden until provider-verified consent. Generic forms cannot create credit/person/repayment/audit records, change record identity/ownership, or rewrite protected credit terms.
7. Persist records across app restart; enforce account ownership and encrypted local storage. Never treat a typed status or a local/demo OTP as verified consent.
8. External application sync can remain deferred until launch, but internal workflows must be real and testable. A configuration screen alone does not count as a connected payment gateway, live market feed, Google Maps/Places lookup, SMS provider, or external integration.
9. Run build, unit tests, all-module instrumentation smoke tests, feature/integration/regression tests, and crash checks. Record the actual result and artifact checksum; do not infer a pass from the existence of a workflow.

## Audit findings and current work

The audit branch has been extended with:
- Guardrails against generic writes overriding record type, ID, owner, or timestamps.
- Guardrails against generic updates changing record identity or protected registered credit terms.
- Instrumentation cases for those protections.
- An instrumentation smoke test that opens every canonical registry module and checks its title and brand mark.

**These changes are not yet release-certified.** The connected GitHub tooling has not returned a CI run or status-check record for the latest audit commits, and this environment cannot access GitHub directly to run Gradle or an emulator. The new tests must execute in the project CI before their result can be claimed.

## Known functional gaps that must be closed before release

- Production OTP provider configuration is not present in this repository's app code; sign-in and consent must remain fail-closed until a trusted provider is connected.
- The repayment screen currently calls the repayment engine with a generic actor label and does not perform a borrower OTP confirmation step.
- Several modules still use generic record forms rather than dedicated domain-specific workflows.
- Portfolio-switch analysis is a calculation from supplied inputs; it is not yet a verified live-market comparison feed.
- Revenue settings store gateway/bank configuration data but do not by themselves execute real payments or verify provider callbacks.
- Address fields expose PIN/city/state and location-source fields, but a verified Google Maps/Places lookup must not be claimed unless implemented and tested.
- QR/OCR support must be verified against real invoice/payment payloads and the saved record linkage; decoded text alone is not proof of a completed trade-credit workflow.

Do not mark the app final while any required internal flow, inter-module update, or test remains incomplete.
