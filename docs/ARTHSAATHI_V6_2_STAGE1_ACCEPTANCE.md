# ArthSaathi V6.2 — Stage 1 Acceptance Baseline

## Scope
Stage 1 is the architecture/code/UI/storage integration gate before functional expansion.

### Architecture
- V6.2 master domain contract and module registry are canonical.
- Cross-module changes use the V62Event/V62EventBus contract.
- Navigation is centralized in V62ModuleRegistry.
- V5/V4/V3 activities remain compatibility code only and are not V6.2 navigation targets.

### Storage
- V6.2 domain keys are centralized in V62Store.
- Existing V5LocalStore remains the physical persistence boundary during migration.
- Financial records are encrypted at rest with Android Keystore AES-GCM by V5LocalStore.
- Documents retain the original URI and are indexed through V62Documents.

### Privacy / consent
- Counterparty creation is transaction-scoped.
- Protected history requires explicit OTP consent.
- Credit registration has a separate final consent gate.
- QR and funding flows do not bypass consent.

### UI / branding
- ArthSaathi is the canonical application label.
- ArthSaathi logo is the canonical application icon and V6.2 screen logo.
- Theme.ArthSaathi is canonical; Theme.Udhaardaar is retained only as a compatibility alias.
- Visual constants are centralized in ArthSaathiV62Design.

### Integration
- Credit, repayment, asset, insurance, rental, TTMM, document, charge-check, trade-credit, formal-credit, funding, legacy/legal/AI and MIS surfaces are represented in the V6.2 contract.
- MIS refreshes from V6.2 events and the same source-of-truth store.
- Savings ledger updates publish SAVINGS_CHANGED so connected analytics can refresh.
- Accounting adapters are vendor-neutral and include TALLY, SAP, generic REST, CSV and webhook boundaries.

## Hard gate
Stage 1 is considered build-complete only after the V6.2 GitHub Actions workflow compiles `app-debug.apk` and all architecture gates pass. Source rectification alone is not treated as a successful APK build.
