# ArthSaathi Phase 2 — Source-Level Findings & Test Adequacy
Date: 2026-10-10
Branch: `arthsaathi-release-hardening-2026-10-10`
Audited base: `cf9e5ebebfdad16a6551e0f99f53893f3e5f5697`
Purpose: Record verified source-level risks before any behavioural code changes. This report does not claim runtime reproduction.

## Evidence inspected
- Latest visible Actions run `37652261744` on the audited source commit; job `112898331664`.
- `app/build.gradle`
- `ArthSaathiCoreEngine.kt`, `ArthSaathiDataStore.kt`, `ArthSaathiWorkflowRepository.kt`
- `V7CoreArchitecture.kt`, `V7LocalStore.kt`, `V7MasterVisionRegistry.kt`, `ArthSaathiArchitectureRegistry.kt`, `ArthSaathiV7MasterVision.kt`, `V7ToolsActivity.kt`
- `V7IntegrationContractTest.kt`, `V62FunctionalContractTest.kt`, `V7FinalAcceptanceInstrumentedTest.kt`
- Consolidated architecture, master requirements in Library, non-regulatory build architecture and logo-freeze specification.

## Finding F-001 — Build is blocked on the inspected source commit
Severity: BLOCKER
Evidence: Actions run `37652261744` failed at `:app:compileDebugKotlin`. Logged errors include unresolved `put` calls in `ArthSaathiCoreEngine.kt`, unresolved `ArthSaathiMasterArchitecture` in `ArthSaathiV7MasterVision.kt`, and unresolved `IntentIntegrator`, `IntentResult` and scanner method calls in `V7ToolsActivity.kt`.
Required action: Fix each root cause without deleting required features, then run clean compile and all tests. Recheck errors against the latest hardening branch before changing code.
Evidence link: https://github.com/sendursandesh/udhaardaar-mvp/actions/runs/37652261744

## Finding F-002 — Legacy global data store exists beside V7 account-scoped encrypted storage
Severity: HIGH
Evidence: `ArthSaathiDataStore.kt` stores every record in one SharedPreferences JSON array called `arthsaathi_master_data`, with no account-owner partitioning or encryption in that class. `ArthSaathiCoreEngine.find/linked/mis` read that global array. `ArthSaathiCoreEngine.confirmConsent` accepts a syntactically shaped 4–8 digit string and marks consent granted; this code path itself does not validate a provider-issued challenge.
Risk: If this path is reachable from the active app, account isolation and production consent guarantees are not established.
Required action: Prove whether this engine is reachable in the release navigation. If reachable, route it through canonical account-scoped repositories and the real consent service or remove its route only after proving no required workflow is lost. Add negative cross-account and forged-OTP tests.

## Finding F-003 — Ownerless records are included in V7 account-scoped reads
Severity: HIGH
Evidence: `V7Core.all` filters records with `owner.isBlank() || owner == user(c)`. Thus any ownerless record in a V7 collection is visible to every logged-in account that reads that collection.
Risk: Cross-account disclosure if ownerless records can be created through migration, older code, direct storage or malformed data.
Required action: Default-deny ownerless records in normal user queries; design explicit, authenticated migration/recovery path for legacy records; add a test that inserts an ownerless record and proves no user can read it without an approved ownership-recovery step.

## Finding F-004 — V7 storage uses encrypted JSON arrays in SharedPreferences; concurrency, migrations and recovery need proof
Severity: HIGH
Evidence: `V7LocalStore` encrypts serialized arrays using Android Keystore AES-GCM, but each add/replace is read-modify-write over a whole collection and calls asynchronous `apply()`. There is no visible transaction boundary, schema version, backup/recovery contract or migration test in this storage class.
Risk: Concurrent writes may overwrite one another; app/device/key restoration and corrupted ciphertext need controlled recovery behaviour. Encryption at rest alone does not prove end-to-end data safety.
Required action: Test concurrent writes, process death, interrupted writes, corrupt payload, key invalidation, export/restore and migration. Consider transactional database storage behind repository interfaces if tests demonstrate SharedPreferences is insufficient. Do not migrate destructively without compatibility tests.

## Finding F-005 — Consent contract and repayment transaction boundary need adversarial tests
Severity: HIGH
Evidence: The inspected V7 final-acceptance test grants consent through `LocalConsentService` using `grant(con.id, true)`, which supplies a boolean rather than demonstrating a provider-issued OTP. `V7CoreArchitecture.kt` also has a separate local consent record helper. `V7Records.repayment` must be audited end-to-end for validation ordering, actor authorization, amount/principal/interest consistency, relationship ownership and atomic persistence of payment plus outstanding.
Risk: Tests may prove local happy-path behaviour without proving production authentication, authorized actors or atomic money-state changes.
Required action: Separate development harness from release configuration; require authenticated actor, active scoped consent where mandated, strict amount/method checks and atomic repayment + balance update. Add invalid/expired/revoked/foreign-user consent tests and crash-between-write tests.

## Finding F-006 — Current acceptance tests include architecture assertions, but are not enough to certify all workflows
Severity: HIGH
Evidence: `V7IntegrationContractTest.kt` tests contract enum presence, event subscription/unsubscribe, consent expiry semantics and duplicate enum names. `V62FunctionalContractTest.kt` checks identity regexes and a date helper. `V7FinalAcceptanceInstrumentedTest.kt` contains useful scenarios, but its assertion `V7MasterVisionRegistry.legacyBacked().isEmpty()` only tests the registry method returning an empty list; it does not prove that every router-backed feature is implemented natively or that all routes work. The final-acceptance test file must be read in full and executed on a device/emulator before coverage is claimed.
Required action: Add behaviour-level tests against repository operations and actual UI flows; measure requirements coverage; assert route reachability and successful create/view/update/reopen flows for every module.

## Finding F-007 — Multiple navigation/architecture registries can drift
Severity: HIGH
Evidence: The source tree has `ArthSaathiArchitectureRegistry`, `ArthSaathiV7MasterVision`, `V7MasterVisionRegistry`, plus V5/V6.2 navigation/module registries. Consolidated architecture says there must be one canonical owner per module and shortcut routes must resolve to it.
Risk: A module may appear in a dashboard but route to a different implementation or duplicate data owner.
Required action: Select the canonical V7 route registry as the contract; add cross-registry parity tests; remove/adapter-wrap old route entry points only after route coverage proves all accepted functions remain reachable.

## Finding F-008 — QR scanner dependency exists in Gradle but scanner API remains unresolved in the logged build
Severity: BLOCKER
Evidence: `app/build.gradle` includes `com.journeyapps:zxing-android-embedded:4.3.0`; the inspected compile nevertheless cannot resolve `IntentIntegrator` / `IntentResult` in `V7ToolsActivity.kt`.
Required action: Verify dependency resolution and imported artifact/API version in a clean build. Add scan success/cancel/invalid-payload instrumentation tests. Never treat scanned payload as verified transaction data without user review.

## Finding F-009 — Product screens sometimes record a reference rather than an actual document
Severity: HIGH
Evidence: `V7ToolsActivity.documents()` saves a name, type and reference string into `V7Core.Keys.DOCUMENTS`; it does not itself upload or securely store document bytes. This can be a valid reference-register feature but is not equivalent to the master requirement for a document vault with evidence files, versioning, provenance and integrity.
Required action: Label reference-only records accurately; implement secure attachment storage and metadata/version linkage before claiming upload/evidence-vault completeness.

## Finding F-010 — Portfolio, MIS and revenue workflows require deeper reconciliation tests
Severity: HIGH
Evidence: Existing test file includes one portfolio/net-worth scenario and one test-gateway revenue flow. This does not prove every source mutation updates every chart/report, nor does a test gateway prove production payment integration.
Required action: Reconcile MIS from canonical source records after each create/edit/close/delete/refund/restore action; test rounding and no-double-counting; test payment failure, cancellation, webhook replay, idempotency, refund and settlement. Production gateway remains an external integration until credentials/configuration and provider callbacks are verified.

## Finding F-011 — Latest build evidence does not establish a release APK
Severity: BLOCKER
Evidence: The inspected latest consolidated master run failed before APK upload; no verified APK artifact/hash was established in Phase 1.
Required action: Produce release artifact only after clean build + mandatory unit/instrumentation/regression checks; verify package, signing, hash, install/upgrade and exact source commit.

## Test coverage status
- Source compilation: FAILED in the inspected run.
- Unit tests: NOT VERIFIED on current hardening branch.
- Instrumentation/emulator: NOT VERIFIED.
- Full regression: NOT VERIFIED.
- Security/privacy adversarial testing: NOT VERIFIED.
- Actual APK install/upgrade: NOT VERIFIED.
- Production OTP and production payment gateway: NOT VERIFIED.

## Phase 2 gate
The requirement register is an initial 31-area mapping, not yet exhaustive to every sub-requirement in the full master vision. Continue by tracing actual entry points and data paths for each row and linking stable requirement IDs to behaviour tests. Do not mark requirements VERIFIED from file existence, navigation tiles, or a passing architecture-only assertion.


## Hardening follow-up — 2026-10-10 (not yet CI-verified)
After the initial audit, the release-hardening branch was changed to default-deny records whose `ownerUserId` is blank or does not match the active account. The predicate is isolated as `V7Core.ownerVisibleTo` in `app/src/main/java/com/udhaardaar/mvp/V7CoreArchitecture.kt`. A unit test was added for same-owner, foreign-owner, blank-owner and blank-current-user cases in `V7IntegrationContractTest.kt`.

Source commit: `3c1a362d5c26311d6ec32f5bd7faa58b5f623fd7`
Test commit: `bc4229737e4244ad82a58632ae39249567d3e210`

**Status:** Source-level mitigation committed; the new test has not yet been executed by a confirmed CI run. This does not close F-003 until the current branch builds and the test passes. Legacy ownerless records may now be hidden from ordinary reads; any recovery must be implemented as a separately authenticated migration and must not restore global visibility.


## Build-blocker rectifications committed — awaiting a fresh build
Three source fixes have now been committed to this isolated hardening branch:
- `ArthSaathiCoreEngine.saveModule`: JSON field writes now call `o.put(k, v)` on the intended record instead of calling an unresolved receiver method inside map iteration. Commit `84e737151daa91f8aaf7a72c75cbb75c19e862ee`.
- `ArthSaathiV7MasterVision`: credit-type options now reference the existing `ArthSaathiArchitectureRegistry.creditNatureOptions`, avoiding the unresolved `ArthSaathiMasterArchitecture` reference. Commit `9ee94f13e524006f954f6a9a354936b703d1a97d`.
- `V7ToolsActivity`: ZXing integration imports now use `com.google.zxing.integration.android.IntentIntegrator` and `IntentResult`. Commit `3f7df0c22649d4f415560cf8c980b6dbc7480ae7`.

These edits address the exact three categories of errors in the 2026-10-07 Actions log, but **they are not yet proven fixed** until a clean build is run on the latest hardening commit. The ownership filter/test commits also remain unverified by CI. No Phase 2 item is being promoted to VERIFIED based on source edits alone.
