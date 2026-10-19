# ArthSaathi — release-hardening branch

This branch is the source baseline for the APK delivered on 19 October 2026:

- Branch: `arthsaathi-release-hardening-2026-10-10`
- Delivered commit: `a20e4cf788b5f84d984dc769e39c030d4c7d3ed5`
- Android application ID: `com.arthsaathi.master`
- Gradle version identity at the audited source: versionCode `800`, versionName `8.0`

## Important release status

The existence of a successful build or test run is not by itself acceptance of the complete application. This branch must remain the working baseline while the consolidated architecture, module routes, field validation, calculations, data persistence, inter-module updates, UI, and crash behavior are audited. Do not replace it with a V3/V6.2/V7 branch or create a new release APK until the full audit and required tests pass.

The prior APK delivery reported a passing build/unit/emulator run (77 tests, no reported failures, errors, or skips). A later audit identified architecture, UI, and flow gaps, so that APK is not certified as final.

## Canonical module surface

The consolidated module activity routes the following journeys through the architecture registry:

- Credit registration, existing loans/udhaar, repayment, QR Khata, and group Khata (TTMM)
- MIS, portfolio, investment-switch analysis, Asset Vault, Liability Vault, benefits/refunds, and formal-charge review
- Claims, will/legacy records, legal help, advocate directory, and document vault
- AI advisor, revenue/service charges, consent/security, profile, family/nominees, address, credit intelligence, trade credit, guarantor, rental/lease, alerts, and integrations

Some modules currently use generic record forms. Their presence in navigation must not be interpreted as proof that all requested calculations, external connections, or end-to-end workflows are fully implemented.

## Privacy and safety

Protected credit/profile information must not be exposed merely because a profile can be searched. Consent and authorization checks must remain enforced before protected information is displayed or consent-gated repayment changes are accepted. External application synchronization may remain deferred until launch, but internal application flows must be implemented and tested.

## Release acceptance checklist

- [ ] Confirm the release build is from this branch and record its exact commit SHA.
- [ ] Reconcile every required module against the consolidated architecture and identify generic/incomplete screens.
- [ ] Verify unique navigation ownership; remove duplicate or dead routes.
- [ ] Test field constraints, validation, focus/cursor progression, keyboard scrolling, date selection, and document/QR journeys.
- [ ] Test calculations, persistence, consent boundaries, and inter-module/MIS updates with realistic scenarios.
- [ ] Run unit, instrumentation/emulator, regression, and crash checks; record results rather than assuming success.
- [ ] Confirm approved logo and visual design on launcher, login, home, and module screens.
- [ ] Build and publish the final APK only after all required acceptance checks pass.
