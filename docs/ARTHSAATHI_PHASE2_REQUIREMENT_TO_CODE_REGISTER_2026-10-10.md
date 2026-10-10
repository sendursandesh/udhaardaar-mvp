# ArthSaathi Phase 2 — Requirement-to-Code Register (Initial Audit)
Date: 2026-10-10
Branch: `arthsaathi-release-hardening-2026-10-10`
Baseline commit: `cf9e5ebebfdad16a6551e0f99f53893f3e5f5697`
Register status: INITIAL INVENTORY + source-risk audit committed. Implementation coverage and runtime behaviour remain UNVERIFIED until tests prove them. Detailed findings: [Phase 2 Source Findings](ARTHSAATHI_PHASE2_SOURCE_FINDINGS_2026-10-10.md). Confirmed blockers include the failed Kotlin build, ownerless-record cross-account visibility in V7Core.all, unproven production OTP boundary, multiple navigation registries, and document-reference records that are not actual stored attachments.

## Status definitions
- **Located**: a plausible source/spec/test file exists; this does not prove feature completeness.
- **Partial**: some implementation evidence exists, but requirements or integration are incomplete/unknown.
- **Missing**: no implementation evidence located in the audited source inventory.
- **Blocked**: cannot be reliably assessed until build/dependency or specification blockers are resolved.
- **Verified**: only after a test against the current hardening commit passes and its evidence is recorded.

## Specification authority
1. `ARTHSAATHI_CONSOLIDATED_MASTER_ARCHITECTURE_2026-10-05.md` — canonical navigation/domain ownership rules.
2. `ArthSaathi_Master_Version_Requirements.txt` in Library — master vision, no-regression rule, ten gates, ten real-world test rounds.
3. `ArthSaathi_Final_Non_Regulatory_Build_Architecture.docx` in Library — intended initial non-regulatory release scope.
4. `ARTHSAATHI_V6_MASTER_CHECKLIST.md`, V6.2 architecture/logo freeze, V7 master blueprint and QA audit — historical requirements and migration evidence.
5. Conflicts between these artifacts must be documented and resolved explicitly; never silently overwrite the user's approved master vision.

## Requirement register

| ID | Requirement / acceptance outcome | Likely owner(s) in source tree | Current audit status | Required proof |
|---|---|---|---|---|
| AS-001 | Premium ArthSaathi branding, canonical logo, tagline and consistent shell | `app/src/main/res/drawable/arthsaathi_logo.xml`, `ArthSaathiV62Design.kt`, `ArthSaathiV7Design.kt` | Partial / visual comparison pending | Compare actual APK screenshots with frozen approved design; verify launcher/login/home/module headers |
| AS-002 | Authentication, mobile OTP, session, reset and logout | `ArthSaathiLoginActivity.kt`, `ArthSaathiSession.kt`, `V7SessionActivity.kt`, OTP services | Blocked / unverified | Valid/invalid/expired OTP, no production demo OTP, session expiry, logout and relaunch |
| AS-003 | Profile, identity, family, people and relationships | `ArthSaathiCoreEngine.kt`, V5/V62/V7 domain and profile-related flows | Partial / canonical owner unclear | Create/view/edit, relationship linking, field validation, data isolation and restart persistence |
| AS-004 | PIN/address/map workflow and editable multiple addresses | Address-related UI/services/resources to locate | Unverified | PIN validation, lookup success/failure, manual fallback, map permission denial, user confirmation and address update propagation |
| AS-005 | Informal credit registration with credit-type-specific fields | `ArthSaathiCoreEngine.kt`, `V5CreditService.kt`, `V62CreditRegistrationActivity.kt`, `ArthSaathiV7MasterVision.kt` | Blocked by build / duplicate generations | Full agreed flow from party search through terms, method, guarantor, documents, consent, OTP and account creation |
| AS-006 | Existing Loans & Udhaar account centre, details, history and closure | V5/V62/V7 credit screens and repositories | Partial / owner reconciliation required | Active/closed lists, account detail, correct balances, chronological transactions, closure and reopen prevention |
| AS-007 | Repayment schedules, EMI, principal+interest, rent/lease and dues | `V5RepaymentService.kt`, `V62RepaymentActivity.kt`, `ArthSaathiCoreEngine.kt` | Blocked by build / calculations unverified | Zero-rate and non-zero-rate schedules, rounding, early/partial/full payment, overpayment rejection and date edges |
| AS-008 | Consent, OTP confirmation, protected history/score, audit and revocation | `V5OtpConsentService.kt`, `V5AccessPolicy.kt`, core engine and security/session services | Partial / critical security review required | Negative tests proving no protected disclosure or repayment update without correct authenticated consent; revoke/expiry/audit |
| AS-009 | Guarantor, demand promissory note and digital evidence | `V5GuarantorAndDocuments.kt`, `V5GuarantorConsentActivity.kt`, document services | Partial / end-to-end unverified | Link to correct credit, versioned document, parties, timestamps, consent record and reopen/download path |
| AS-010 | QR Khata, trade credit, invoice capture and scanner/OCR | `V62QRKhataActivity.kt`, `V7ToolsActivity.kt`, `V5OcrService.kt`, scanner dependency | Blocked: scanner symbols unresolved in inspected build | Real scan/cancel/invalid QR, invoice extraction with user review, no duplicate transaction and ledger linkage |
| AS-011 | Financial and non-financial Asset Vault, ownership, nominee and evidence | `V5AssetVaultActivity.kt`, `V62AssetVaultActivity.kt`, `V62AssetLifecycle.kt` | Partial / canonical persistence unclear | Create/edit/delete policy, ownership evidence, value history, nominee, linked document and MIS propagation |
| AS-012 | Liability Vault and obligations | `V62Domain.kt`, core engine/module services | Unverified | Loan/guarantee/lease entries, outstanding values, edit, archive and net-worth/MIS updates |
| AS-013 | Portfolio holdings, returns, allocation, benchmark and switch/opportunity cost | `ArthSaathiCoreEngine.kt`, `V62MISActivity.kt`, V7 grow/advisor routes | Partial / calculation and market-data evidence missing | Test math, fees/tax assumptions, risk profile, stale/missing prices, uncertainty labels and no automatic trade execution |
| AS-014 | MIS actual numbers, charts, assets/portfolio and benefits/refunds value generated | `ArthSaathiCoreEngine.kt`, `V62MISActivity.kt` | Partial / figures require reconciliation | Same source records reconcile across cards/charts/reports after create/edit/delete/refund/restart |
| AS-015 | Insurance, protection, government schemes, renewal and claim readiness | `V62InsuranceActivity.kt`, V5 support workflows | Partial / eligibility evidence unverified | Policy lifecycle, dates, eligibility disclaimer, reminders, document linkage and claim handoff |
| AS-016 | ChargeCheck: sanctioned vs actual charges, OCR, variance and refund/complaint evidence | `V5ChargeAuditService.kt`, `V5ChargeComparisonActivity.kt`, `V62ChargeCheckActivity.kt` | Partial / extraction and math unverified | Compare principal/interest/fees/tax/penalty/refund with source evidence; manual correction and confidence |
| AS-017 | TTMM / Group Khata, contributions, split, balances and settlement | `V5TTMMActivity.kt`, `V62TTMMActivity.kt` | Partial / no-dead-end settlement unverified | Unequal splits, edits, removal, duplicate contribution, partial settlement, rounding and member balances |
| AS-018 | Document Vault, OCR, metadata, version, provenance and integrity | `V5DocumentService.kt`, `V62Documents.kt`, `V62DocumentIntelligence.kt` | Partial / secure storage and lifecycle unverified | Upload/view/download, unsupported/corrupt file, permission denial, linked entity, versioning and integrity |
| AS-019 | Benefits/refunds, completed status and actual value generated | Core engine/module registry and MIS | Partial / linkage unclear | Mark completed only with evidence/status/date/value; ensure value counted once in MIS |
| AS-020 | Nominee, Will, inheritance and claim lifecycle | `V5AssetClaimService.kt`, `V5DeathClaimLegalActivity.kt`, `V62LegacyLegalAIActivity.kt` | Partial / legal disclaimer and workflow unverified | Asset-to-claim trace, evidence checklist, draft-not-legal-advice notice and status transitions |
| AS-021 | Legal/advocate directory by domain and city, contact and verification status | `V5SupportWorkflowsActivity.kt`, `V62LegacyLegalAIActivity.kt` | Unverified / data source not confirmed | Search/filter, verified vs unverified label, contact action and unavailable-network fallback |
| AS-022 | ArthSaathi AI Advisor, explainable alerts and user-authorized data use | Core engine and V7 advisor/module routes | Partial / rules and data permissions unverified | Each recommendation shows reason, inputs, limitations and user action; no hidden financial execution |
| AS-023 | Revenue, pricing, invoices, payment gateway configuration and reconciliation | `ArthSaathiV7MasterVision.kt` routes and core engine | Partial / real payment integration not established | Test mode, success/failure/cancel/webhook/idempotency/receipt/refund/reconciliation; never store raw card credentials |
| AS-024 | Alerts/reminders, preferences, due dates and expiry | `V5ReminderService.kt`, `ReminderReceiver.kt` | Partial / scheduling and restart behaviour unverified | Timezone/date boundary, permission denial, duplicate suppression, reschedule after restart and preference opt-out |
| AS-025 | Reports/statements/export and family-readable financial summary | Module/report sources to locate | Unverified | Reconcile exported totals to source data; permission, empty state, redaction and file integrity |
| AS-026 | Integration/API and future scalable partner connections | V7 platform/architecture services | Architecture located; runtime integration unverified | Authenticated adapter contracts, timeout/retry, idempotency, schema versioning and audit |
| AS-027 | Unified canonical persistence, migrations, backup/recovery and no data loss | `ArthSaathiDataStore.kt`, `V5LocalStore.kt`, `V7LocalStore.kt`, repositories | Blocked / multiple generations present | Upgrade/migration tests from supported baseline, restart, rollback, corruption handling and export/recovery |
| AS-028 | One canonical navigation destination per module; no duplicate/dead-end routes | `ArthSaathiNavigation.kt`, `V5Navigation.kt`, V62/V7 module registries | Partial / competing contracts present | Enumerate every entry point; assert same canonical route, back/home path and no dead screens |
| AS-029 | Mobile UX: scroll, keyboard, cursor, validation, photo/camera, dates and accessibility | UI activities/design modules | Unverified | Instrumented small-screen runs; keyboard open/close, scroll to last field, focus progression, date picker, camera cancel |
| AS-030 | Data privacy, least privilege, encryption, consent, audit and deletion/export policy | Access policy, consent/security/session and storage services | Partial / security audit required | Negative authorization tests, sensitive-log scan, secure-storage review, data export/deletion and retention tests |
| AS-031 | Stable release: clean build, install, upgrade, signing, artifact provenance | `.github/workflows/*.yml`, Gradle config | Blocked: inspected consolidated build failed | Clean checkout build; unit/instrumentation tests; install/upgrade; package/version/signing/hash and exact commit provenance |

## Cross-module invariant tests
1. Create/update one entity once; every linked view uses the same canonical ID and current data.
2. Failed/cancelled saves leave no partial records and do not refresh totals as if successful.
3. Repeated submissions are idempotent or clearly rejected; no duplicate payments, refunds or benefits.
4. Unauthorized actors cannot read protected details or alter repayments, scores, documents or consent.
5. Recompute MIS after all relevant source changes and compare every displayed total/chart to a direct source-record calculation.
6. App restart and process recreation preserve committed records and discard unsaved drafts safely.
7. Any module failure has a retry/back path and does not block unrelated modules.
8. Every money value has defined currency, sign, precision, rounding, source and effective date.
9. External data is marked with source and freshness; unavailable data never masquerades as live/verified.
10. Every release candidate runs the ten master real-world rounds plus a full regression suite.

## Immediate Phase 2 tasks
- [ ] Resolve the canonical master-vision version and enumerate every requirement/sub-requirement from it without altering the source document.
- [x] Inspect representative source owners, persistence, events and current tests; detailed findings are recorded in the linked source findings report. Full tracing for all 31 areas remains open.
- [x] Inspect representative unit/instrumentation tests; architecture registry assertions do not by themselves prove end-to-end functionality. Full suite execution and test-by-test coverage mapping remain open.
- [ ] Add stable requirement IDs and link each ID to one or more automated tests.
- [ ] Mark no item Verified without a passing test on the current hardening commit.

This register is deliberately conservative: source files and navigation tiles are evidence of implementation attempts, not proof that the feature is complete or reliable.


## Rectification update — 2026-10-10
The following changes are committed on the hardening branch; their build/test evidence remains pending until the new branch-specific CI run finishes.

| Requirement ID | Rectification evidence | Verification status |
|---|---|---|
| AS-002 / AS-008 | Master record access now depends on the active session owner; login UI still accepts mobile number without OTP and remains a release-blocking authentication gap. | Partial / NOT VERIFIED |
| AS-003 | ArthSaathiDataStore tags newly appended rows with the active owner, filters reads to that owner, preserves other owners on replacement, and rejects writes without a session or for another owner. | Code committed / CI pending |
| AS-005 | Fixed ArthSaathiCoreEngine.saveModule JSON field receiver and canonical credit-type registry reference. | Code committed / CI pending |
| AS-010 | Corrected ZXing IntentIntegrator / IntentResult imports to the integration package. | Code committed / CI pending |
| AS-001 | Canonical arthsaathi_logo.xml exists; pixel/visual comparison against the approved design is not yet completed. | Partial / NOT VERIFIED |

Commits: 84e737151daa91f8aaf7a72c75cbb75c19e862ee, 9ee94f13e524006f954f6a9a354936b703d1a97d, 3f7df0c22649d4f415560cf8c980b6dbc7480ae7, 1dd62f26237698032192c7670d5a480ddc5e713f, 6e003717d210a3b541f6bcf59cd910df2b4c5ce5.

A dedicated branch CI workflow now runs clean :app:testDebugUnitTest :app:assembleDebug, verifies the APK archive, records SHA-256 and uploads the APK/test results. Do not mark any row VERIFIED until the run for the exact final commit passes. Authentication remains release-blocking until an actual OTP service/provider is configured and invalid/expired/replayed challenge tests pass. The source-only change is not proof of secure authentication.
