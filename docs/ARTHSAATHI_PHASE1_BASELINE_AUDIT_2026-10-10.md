# ArthSaathi Phase 1 — Baseline Protection & Source Audit
Audit date: 2026-10-10
Status: IN PROGRESS — baseline branch created; release baseline not yet certified.

## 1. Protected source snapshot
- Repository: https://github.com/sendursandesh/udhaardaar-mvp
- Source branch: `arthsaathi-consolidated-implementation-2026-10-06`
- Exact source commit: `cf9e5ebebfdad16a6551e0f99f53893f3e5f5697`
- Isolated hardening branch: `arthsaathi-release-hardening-2026-10-10`
- Hardening branch initially points to the exact source commit above.
- No application source code was changed as part of this Phase 1 baseline record.
- Do not force-push or rewrite this hardening branch. Make future work as reviewable commits.

## 2. Existing authoritative specification artifacts found
- `ARTHSAATHI_CONSOLIDATED_MASTER_ARCHITECTURE_2026-10-05.md`
- `ARTHSAATHI_INDEPENDENT_ARCHITECTURE_2026-10-05.md`
- `ARTHSAATHI_V6_MASTER_CHECKLIST.md`
- `docs/ARTHSAATHI_V6_2_FINAL_ARCHITECTURE.md`
- `docs/ARTHSAATHI_V6_2_MASTER_BASELINE_2026-09-19.md`
- `docs/ARTHSAATHI_V6_2_NAVIGATION_ARCHITECTURE_2026-09-20.md`
- `docs/ARTHSAATHI_V6_2_QA_AUDIT_2026-09-20.md`
- `docs/ARTHSAATHI_V6_2_LOGO_FREEZE.md`
- `docs/ARTHSAATHI_V7_MASTER_BLUEPRINT.md`

The consolidated architecture states that it reconciles the five earlier architecture layers and is the source of truth for subsequent coding and QA. The V6.2 logo freeze identifies `app/src/main/res/drawable/arthsaathi_logo.xml` as the canonical logo resource and prohibits silent redesign.

The user's Library contains `ArthSaathi_Master_Version_Requirements.txt` / `ArthSaathi_Master_Version_Requirements(1).txt` and `ArthSaathi_Final_Non_Regulatory_Build_Architecture.docx`. The requirements file describes the master vision, modules, architecture principles, ten development gates, ten-round real-world testing and the no-regression rule. The non-regulatory build architecture further describes the intended first-release scope. These are retained Library artifacts and were not modified. The exact 53-section artifact mentioned in conversation has not yet been conclusively matched to a repository file; do not recreate or overwrite it from memory. Phase 2 must map the repository architecture against both the repository specifications and these Library baselines, documenting any unresolved conflict instead of silently choosing one.

## 3. Known release blockers from the inspected CI run
The GitHub Actions run associated with the source commit failed at Kotlin compilation on 2026-10-07. The available log reports:
- `ArthSaathiCoreEngine.kt`: unresolved `put` references.
- `ArthSaathiV7MasterVision.kt`: unresolved `ArthSaathiMasterArchitecture`.
- `V7ToolsActivity.kt`: unresolved `IntentIntegrator` / `IntentResult` and related scanner calls.

These are confirmed blockers in that run; the exact current state must be rechecked against the hardening branch before any fix is made.

## 4. Baseline safety observations
- The source branch protection endpoint reported protection disabled for `arthsaathi-consolidated-implementation-2026-10-06`.
- The source tree contains multiple historical architecture generations and test suites; these require a controlled audit, not wholesale deletion.
- `app/build.gradle` sets applicationId `com.arthsaathi.master`, versionCode 800, versionName 8.0, compileSdk 35, minSdk 23, targetSdk 35 and Java/Kotlin 17.
- Build success is not equivalent to functional acceptance. No claim is made here that the APK passes install, runtime, end-to-end, security or regression tests.
- No verified release APK/artifact hash was established during this Phase 1 inspection.

## 5. Phase 1 exit criteria
- [x] Exact source commit recorded.
- [x] Isolated hardening branch created from that exact commit.
- [x] Canonical architecture and logo-freeze documents identified.
- [x] Known compilation blockers recorded.
- [x] Locate the master version requirements and final non-regulatory architecture in the Library; exact match to the referenced 53-section artifact remains unconfirmed.
- [ ] Identify the last known-good APK and its exact source commit/hash, if available.
- [ ] Confirm canonical approved visual assets and compare them to the source resources.
- [x] Inventory top-level specification files, canonical logo resource, architecture registries, build workflows and existing unit/instrumentation test files from the source tree. Full resource-by-resource visual comparison remains pending.
- [ ] Establish branch protection and required checks where permissions/settings allow.

Phase 1 is PARTIALLY COMPLETE: the baseline is recorded and isolated, but release certification is blocked until a known-good APK/commit is found or explicitly recorded as unavailable, canonical assets are compared visually, and branch-protection settings are applied by a repository administrator. The latest 20 visible Actions runs inspected for the recent master-build/consolidated branches were failures; this is not proof that no older successful artifact exists. Phase 2 can start using this protected baseline while the remaining Phase 1 release-evidence items stay open.


## Update after controlled rectification began — 2026-10-10
- Added a dedicated CI workflow, `.github/workflows/arthsaathi-release-hardening.yml`, that runs a clean Gradle build, unit tests, APK archive validation and SHA-256 capture on this hardening branch.
- First run: https://github.com/sendursandesh/udhaardaar-mvp/actions/runs/38061441666 (queued/in progress at time of this update; its result must be checked before claiming a successful build).
- The repository integration cannot read branch-protection settings (the protection endpoint returns 403), and it does not expose a write operation for applying repository rules. Therefore branch protection is **not claimed as enabled**; an administrator must apply it in GitHub settings. The hardening branch remains separate and all changes are additive commits.
- Further audit identified that the active Master 8.0 login screen accepts a mobile number without any OTP challenge, while the master data store originally used one unscoped record array. The master data store has now been changed to filter and write by the signed-in session's mobile owner, preserve other owners' records during replacement, and deny ownerless legacy records by default. Unit-level ownership predicate coverage has been added.
- Important residual security finding: owner-scoped local storage does not itself solve authentication because the current login UI still establishes a session by mobile number alone. A real OTP provider/backend and verified challenge-response integration are required before the app can be certified for sensitive personal/financial data. Do not represent phone-number entry as OTP authentication.
- Approved logo resource was found at `app/src/main/res/drawable/arthsaathi_logo.xml`; source presence is confirmed, but visual comparison against the previously approved reference and rendered APK screenshot is still pending.
