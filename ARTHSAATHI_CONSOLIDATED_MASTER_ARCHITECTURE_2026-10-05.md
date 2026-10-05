# ArthSaathi Consolidated Master Architecture — 2026-10-05

This document is the canonical product architecture produced by reconciling the five previously established ArthSaathi architecture layers.

## Product model

ArthSaathi is a user-controlled financial-life operating system:

**People → Relationships → Money/Credit → Repayments → Assets → Liabilities → Protection → Documents → Claims/Legacy → Intelligence/MIS → Actions**

## Top-level areas

1. Home / Command Centre
2. Record
3. Credit
4. Assets
5. Grow
6. Protect
7. Claim / Legacy
8. Legal
9. People & Relationships
10. Document / Intelligence
11. ArthSaathi AI Advisor
12. Revenue / Payments
13. Platform / Security

## Non-negotiable ownership rules

- **Register Credit** is the creation journey for a new credit.
- **Loans & Udhaar** is the account centre for previously registered credits, including active and closed accounts.
- Selecting an existing credit account opens its complete account detail/history.
- **Repayment Centre** is the single repayment engine.
- **MIS** is the single cross-module management-information layer.
- **Document Vault** is the canonical document/evidence owner.
- **People & Relationships** owns identity, family, borrower/lender and guarantor relationships.
- **Security & Consent** owns access, OTP/consent and audit boundaries.
- **Group Khata / Group Expenses** replaces the TTMM label while retaining its intended shared-expense functionality.
- Shortcuts may exist on Home, but they must route to one canonical destination and must not create duplicate modules.

## Credit registration

Nature-of-credit selection is mandatory. The flow is:

Identify party → nature of credit → credit-specific fields → method → repayment calculation → guarantor → digital document → consent/OTP → registration → account detail.

Credit-specific forms must not force irrelevant fields. For example, rental/lease must not incorrectly require principal/ROI fields where they do not apply.

## Existing account centre

Loans & Udhaar must show previously registered accounts with active/closed status. Opening an account must expose the full details, terms, schedule, transactions, outstanding, evidence, consent history and closure state.

## Intelligence

MIS must show actual recorded numbers first, with charts for assets/portfolio and value generated through benefits/refunds. Portfolio-switch analysis must compare return, cost, risk and opportunity cost before suggesting a switch.

## Security

Protected borrower/counterparty information and reliability/score information remain consent-gated. Production authentication must use a real configured OTP provider; the development OTP harness cannot be treated as production authentication.

## Implementation rule

The consolidated architecture is now the source of truth for subsequent coding, reconciliation, QA and release. No old V5/V6.2/V7 navigation contract may silently override it.
