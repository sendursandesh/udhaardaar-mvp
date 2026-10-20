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
- Removed the Home screen's dependency on the legacy `udhaardaar_accounts` preference namespace; the profile header now reads account-scoped ArthSaathi profile records.
- Restored the agreed five-tab navigation `Home | Credit | Repay | Vault | More`; the 34 non-home modules are assigned exactly once across the four service tabs, and duplicate home quick-action tiles were removed.
- Repayment requires a separate provider-issued OTP bound to the specific credit and purpose; the legacy direct call fails closed. Balance, repayment ledger and audit entry are committed in one encrypted-store write.
- Added email and Aadhaar-last-four validation to generic module forms.
- Added instrumentation cases for generic-write/update protections, repayment challenge binding and wrong-OTP rejection, MIS propagation, legacy-profile isolation, field validation, canonical module title/brand mark, generic record updates, and group-MIS collection/outstanding-share reconciliation. Runtime tests must clear their account-scoped records between cases to prevent order-dependent results.
- Added `.github/workflows/arthsaathi-audit.yml` to run build/unit tests and Android 15 emulator instrumentation tests on this branch.

**These changes are not yet release-certified.** No status-check or workflow-run result has been returned for the latest audit commits. This environment cannot resolve GitHub from its local shell and has no Android SDK; the new CI workflow must execute and its build and emulator artifacts must be reviewed before any test pass or final APK can be claimed.

## Known functional gaps that must be closed before release

- Production OTP provider configuration is not present in this repository's app code. Provider-verified sign-in and consent remain fail-closed until a trusted provider is connected. A clearly labelled device-only offline workspace is available for internal module testing and local record management; it is not verified identity, and it cannot activate credit or authorise repayments without provider-verified consent.
- The repayment OTP path is implemented fail-closed, but it still depends on a trusted provider being installed and configured; it must be verified by the new instrumentation tests.
- Fifteen modules still use generic save/list/edit forms rather than dedicated domain workflows; safe editing is available for those records, but delete controls and complete domain relationships are not yet implemented.
- Rental & Lease currently writes a separate generic `RENTAL_LEASE` record instead of using the validated lease/contract calculation path in Register Credit.
- Trade Credit is not automatically linked to QR Khata invoice records or the credit/repayment engine; Claims and Legal records use free-text references rather than enforced links to assets, benefits and documents.
- Group Khata now feeds MIS with total contributions collected and outstanding equal-share balances after contributions and settlements; an Android instrumentation scenario covers the calculation. This test is authored but remains unverified until CI passes.
- Portfolio-switch analysis is a calculation from supplied inputs; it is not yet a verified live-market comparison feed.
- Revenue settings store gateway/bank configuration data but do not by themselves execute real payments or verify provider callbacks.
- Address fields expose PIN/city/state and location-source fields, but a verified Google Maps/Places lookup must not be claimed unless implemented and tested.
- QR/OCR support must be verified against real invoice/payment payloads and the saved record linkage; decoded text alone is not proof of a completed trade-credit workflow.

Do not mark the app final while any required internal flow, inter-module update, or test remains incomplete.
