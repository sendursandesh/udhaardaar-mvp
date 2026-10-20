# ArthSaathi — consolidated master build

## Working baseline

- **Working branch:** `arthsaathi-step1-5-functional-audit-2026-10-18`
- **Android application ID:** `com.arthsaathi.master`
- **Gradle version identity:** versionCode `800`, versionName `8.0`
- **Canonical UI:** `ArthSaathiHomeActivity` and `ArthSaathiMasterModuleActivity`
- **Canonical module registry:** `ArthSaathiArchitectureRegistry`
- **Approved brand mark:** `app/src/main/res/drawable/arthsaathi_logo.xml`

Do not treat the earlier APK or a successful compile as final acceptance. The complete architecture and all module workflows must pass source review, build, instrumentation, regression, and device/emulator checks before a final APK is released.

## Branch and APK identity controls

- The release-candidate source branch is `arthsaathi-step1-5-functional-audit-2026-10-18`; the release workflow checks out the triggering commit SHA, not a moving branch tip.
- The canonical Android identity is application ID `com.arthsaathi.master`, versionCode `800`, versionName `8.0`. The release workflow fails closed if the branch or any of these Gradle identity values differ.
- Every successful build job records repository, branch, exact commit SHA, application ID, version code/name, workflow run/attempt and UTC build time in `release-provenance.txt`; it also records the APK SHA-256 in `apk-sha256.txt` and uploads both alongside the APK.
- Build provenance proves which source produced an artifact; it does not certify functional correctness. The emulator acceptance job must also pass before release approval.
- GitHub reports this branch as unprotected with required status checks disabled. Repository branch protection must be enabled in GitHub settings by an administrator; until that is done, the workflow's identity guard is an enforcement check, not a server-side prohibition on direct pushes.

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
- Rental & Lease now routes through Register Credit's validated lease calculation and Document Vault agreement-reference workflow; the new route still requires emulator acceptance verification.
- Trade Credit can now be created from a QR Khata invoice and retains a source-invoice relationship in the canonical credit/repayment ledger; a dedicated instrumentation test covers this linkage and must pass in CI. Claims now store explicit asset, benefit/refund and document IDs; Legal Help now stores an explicit relationship to a selected saved record. These new relationships require instrumentation verification. The Advocate Directory remains a local/manual record surface until a trusted external directory is integrated.
- Group Khata now feeds MIS with total contributions collected and outstanding equal-share balances after contributions and settlements; an Android instrumentation scenario covers the calculation. This test is authored but remains unverified until CI passes.
- Portfolio-switch analysis is a calculation from supplied inputs; it is not yet a verified live-market comparison feed.
- Revenue settings store gateway/bank configuration data but do not by themselves execute real payments or verify provider callbacks.
- Address fields expose PIN/city/state and location-source fields, but a verified Google Maps/Places lookup must not be claimed unless implemented and tested.
- QR/OCR support must be verified against real invoice/payment payloads and the saved record linkage; decoded text alone is not proof of a completed trade-credit workflow.

Do not mark the app final while any required internal flow, inter-module update, or test remains incomplete.


## Release identity lock (2026-10-20)

This section is the release identity contract for the current audit candidate. Do not infer a release version from a branch name alone.

- Canonical candidate branch: `arthsaathi-step1-5-functional-audit-2026-10-18`
- Canonical Android application ID: `com.arthsaathi.master`
- Canonical Android version: `versionCode 800`, `versionName 8.0`
- Source of truth: `app/build.gradle` on the exact commit being built.
- Artifact acceptance: the APK, `release-provenance.txt`, and `apk-sha256.txt` must come from the same successful workflow run and exact commit SHA.
- Test acceptance: both the build/unit-test job and emulator instrumentation job must succeed for that commit. Empty/missing CI results are **not** a pass.
- Branch protection: repository settings must require the audit workflow's build/unit and emulator checks before merging. This cannot be enabled by a source-file change; a repository administrator must apply and verify the GitHub branch rules.
- Release status remains **NOT CERTIFIED** until the checks and artifact provenance above are verified. Do not rename the APK or manually relabel its version to imply certification.

Do not build a release from a V6.2/V7 branch or cherry-pick a prior-version APK into this release. Any intentional version change must update the Gradle identity, workflow guard, provenance generation, tests, and release documentation together in one reviewed change.
