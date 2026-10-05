# ArthSaathi Independent Consolidated Architecture — 2026-10-05

## Status
This branch is the **independent consolidated implementation layer**. It is not a V3/V4/V5/V6/V6.2/V7 continuation.

Historical versions remain in the repository only as reference material. They are not part of the active application entry/navigation contract.

## Active dependency boundary

`ArthSaathiLoginActivity → ArthSaathiHomeActivity → ArthSaathiArchitectureRegistry / ArthSaathiNavigation → ArthSaathiWorkflowRepository → ArthSaathiDataStore`

Application entry:
- `ArthSaathiApp`

Active manifest:
- `ArthSaathiLoginActivity`
- `ArthSaathiHomeActivity`

## Canonical ownership

- Register Credit = creation of a new credit only.
- Loans & Udhaar = repository of previously registered active/closed accounts.
- Repayment Centre = single repayment engine.
- People & Relationships = identity, borrower/lender/guarantor relationships.
- Document Vault = canonical evidence/document owner.
- MIS = canonical cross-module management-information layer.
- Group Khata / Group Expenses = shared-expense/group-credit function.
- Security & Consent = authorization, OTP/consent and audit boundary.
- Revenue & Payments = user charges/payment boundary.
- Portfolio Switch Analysis = advisory only; never executes money movement.

## Independence rule

No canonical ArthSaathi class may import, instantiate, route to, or persist through a V3/V4/V5/V6/V6.2/V7 class, store, repository or activity.

A CI architecture guard enforces this boundary for the canonical implementation files and active Android manifest.

## Important scope

This step establishes the **clean architecture and dependency boundary only**. It does not claim that every business feature is already fully implemented or that an APK has passed build/emulator QA. Those are subsequent stages after the architecture is frozen and audited.
