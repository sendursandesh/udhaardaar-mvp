# ArthSaathi Steps 1–5 Functional Audit
Date: 2026-10-18
Working branch: `arthsaathi-step1-5-functional-audit-2026-10-18`
Starting source revision: `1492794e88de6c2769efdd3d0caf044ef52aa29a`
Purpose: finish code, data-flow, module and pre-emulator QA before the final emulator acceptance run. This report is evidence-oriented; a file or route existing does not mean its requirement is complete.

## Step 1 — Source and architecture audit

### Confirmed architecture contract
The source-of-truth file is `ARTHSAATHI_CONSOLIDATED_MASTER_ARCHITECTURE_2026-10-05.md`. It requires one canonical owner for Register Credit, Loans & Udhaar, Repayment Centre, MIS, Document Vault, People & Relationships, Security & Consent, and Group Khata. Shortcut tiles must route to one destination. Credit registration must use the agreed party → credit type → type-specific fields → method → repayment → guarantor → document → consent/OTP → registration journey.

### Confirmed implementation risks
1. **Competing app shells and registries:** the manifest launcher is `ArthSaathiLoginActivity`, which opens `ArthSaathiHomeActivity`; the home shell uses `ArthSaathiMasterModuleActivity` and its 35-route registry. Separate V7 home, V7 router, V7 native module activity, V5 and V6.2 navigation/registries also exist. The V7 UI tests do not by themselves prove the launcher journey uses the same implementations.
2. **Generic forms are not full workflows:** the canonical master activity routes several areas (including profile, family, address, credit intelligence, trade credit, accounting, formal credit, funding, guarantor, rental and alerts) through a generic `simple(...)` form. This is a known functional gap, not accepted as feature completion.
3. **Login is safely fail-closed but operationally blocked without provider configuration:** the login screen refuses sign-in if a trusted OTP provider is not configured. A mobile number alone is not treated as authentication. Production OTP remains an external deployment dependency.
4. **Existing tests have useful scenarios but have coverage limits:** unit tests assert registries/contracts and some math. Instrumentation tests cover selected flows, but full requirement-to-test traceability and real-world ten-round acceptance are not yet complete.
5. **Evidence vault, payment gateway and external integrations:** source findings distinguish document-reference entries from stored attachment bytes and a test gateway from a production gateway. These must not be labelled production-ready until end-to-end tests/configuration prove them.

## Step 2 — Corrections applied on this audit branch

### Fix A — owner-safe write boundary
In `V7CoreArchitecture.kt`:
- `V7Core.add` now rejects writes when no account is signed in and rejects records explicitly owned by a different account.
- `V7Core.replace` now rejects blank-session writes, missing IDs, mismatched incoming owner IDs and attempts to overwrite a stored row owned by another account.
- Successful writes continue to stamp owner and update time, audit the change and publish the canonical event.
- This protects the V7 persistence boundary; it does **not** replace the separate production OTP/provider requirement.

### Fix B — idempotent revenue reconciliation
In `V7RevenueEngine.reconcile`:
- only a `SUCCESS` payment can be newly reconciled;
- blank payment IDs or settlement references are rejected;
- a repeat callback with the same settlement reference returns success without adding a second revenue record;
- a conflicting settlement reference is rejected;
- failed/cancelled/other non-success payments cannot be reconciled.
This prevents duplicate callback delivery from inflating revenue/MIS.

## Step 3 — Test cases added

In `V7Step1To5IntegrationInstrumentedTest.kt`:
- `crossAccountUpdateCannotOverwriteAnotherOwnersRecord`: switches account context and asserts a foreign account cannot modify another owner's record.
- `revenueReconciliationIsIdempotentAndRejectsFailedPayments`: repeats a settlement callback, checks exactly one revenue entry, rejects a different settlement reference and rejects reconciliation of a failed payment.

These tests are committed as code, but they are **not marked passed until a test runner executes them**.

## Step 4 — Module and cross-module test coverage review

Existing coverage includes:
- canonical route uniqueness and required module IDs;
- owner isolation and consent expiry/revocation contract checks;
- credit/repayment/consent and outstanding-balance update scenarios;
- asset/liability/portfolio/MIS projection checks;
- revenue/service/invoice/payment/reconciliation/MIS scenario;
- persistence namespace and UI screen smoke scenarios;
- QR/document/market/address and module-entry-point scenarios in the broader instrumentation suite.

Still open:
- a requirement-ID-to-test mapping for every sub-requirement;
- end-to-end CRUD/reopen/restart tests for all 35 canonical routes;
- real attachment bytes, version/integrity and document-to-entity linkage;
- complete portfolio-switch source freshness, fee/tax and risk tests;
- benefit/refund status and MIS no-double-counting tests;
- address/PIN/map permission and manual fallback flows;
- UI route parity between launcher shell and V7 implementation;
- OTP provider and production payment-provider callbacks;
- full crash, concurrency, interrupted-write, restore/migration and accessibility matrix.

## Step 5 — Pre-emulator static/unit/build gate

A separate workflow, `.github/workflows/arthsaathi-step1-5-preflight.yml`, has been added for this audit branch. It runs static guard checks, a clean unit-test suite, debug compilation/APK assembly, APK archive verification and SHA-256 capture. It deliberately does **not** run the final emulator acceptance job.

### Current gate state
- Source edits committed: yes.
- New cross-account and revenue-idempotency tests added: yes.
- Preflight workflow configured: yes.
- Preflight run result: pending until GitHub Actions reports it.
- Emulator/instrumentation result for the new tests: pending; not claimed passed.
- Release readiness: **not certified** while the preflight/instrumentation gates and the listed functional gaps remain open.

## Required next decision gate
Do not run the final emulator acceptance suite until the preflight is green, any failures are fixed, and the known active-shell/registry and generic-workflow gaps have been resolved or explicitly accepted as deferred scope. External OTP/payment/map integrations may remain configuration-blocked for a non-regulatory build, but the UI must clearly state that and internal workflows must remain testable without pretending an external integration succeeded.
