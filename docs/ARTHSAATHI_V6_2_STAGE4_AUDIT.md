# ArthSaathi V6.2 — Stage 4 Integration & Design Audit

## Audit target
Branch: `arthsaathi-v6.2-final-architecture-copy`

Stage 4 audits Stage 1–3 together and closes integration defects before the next V6.2 APK is accepted.

## Stage 1 — Architecture / UI foundation
- Canonical ArthSaathi brand, tagline and pillars are in the V6.2 design layer.
- Canonical logo resource is `@drawable/arthsaathi_logo`.
- Premium light background, navy typography, teal/blue actions, restrained gold accents, rounded cards and keyboard-safe scrollable forms are the common visual system.
- V6.2 module registry and V62Event/V62EventBus are present.
- Legacy V5/V4/V3 activities are not normal V6.2 manifest navigation targets.

## Stage 2 — Core relationship / credit flows
- Counterparty search/create remains transaction-scoped.
- History is consent/OTP gated before exposure/score disclosure.
- Credit registration retains a separate final registration consent/OTP gate.
- Credit supports EMI, principal + interest and bullet repayment modes.
- Evidence/document and trade-credit intelligence boundaries are retained.

## Stage 3 — Repayment correctness
- Total repayment may include interest; only the principal component reduces principal outstanding.
- Principal cannot reduce outstanding below zero.
- Repayment history stores total, principal, interest, date and consent state.
- Closed status is applied when principal outstanding reaches zero.
- Repayment and relationship events are published through the V6.2 event bus.

## Stage 4 — Integration corrections completed
1. QR scanner activity is declared in the manifest.
2. QR scanner results return to the Financial Centre Khata form and populate party, amount and invoice/date context for review.
3. QR Khata ledger mutation requires explicit OTP confirmation before persistence.
4. QR repayment creates a repayment record/event and updates the linked relationship balance; QR credit increases relationship principal/outstanding.
5. A dedicated `QR_KHATA_CHANGED` event is in the V6.2 event contract.
6. Home financial snapshot is scoped to the active V6.2 account.
7. Logo freeze documentation now references the actual canonical `arthsaathi_logo.xml` resource.
8. V6.2 build gates explicitly check QR activity, QR event, canonical logo usage and QR consent action.

## Design/page audit
- Login uses the canonical ArthSaathi logo, brand, tagline and pillars.
- Home uses the canonical logo/title shell and concise command-centre layout.
- Primary V6.2 module pages use the shared `ArthSaathiV62Design` title/card/input/button system.
- Financial Centre, Credit Intelligence and ChargeCheck use the same premium visual language and Back paths.
- Approved logo concept is preserved: teal compass arc + blue route + navy A/gateway + gold journey road + gold north star + route point + blue horizon.
- No generic rupee/bank/piggy-bank replacement is used as the application logo.

## Acceptance boundary
Source audit is not by itself APK acceptance. Final Stage 4 acceptance requires the V6.2 GitHub Actions build to compile, pass the architecture gates and produce an intact APK from the final audited commit.

## Known production boundary
OTP shown in the current prototype is a demo/local verification flow. A production SMS/OTP provider must be connected before production release. This does not change the V6.2 architecture or consent-gate semantics.
