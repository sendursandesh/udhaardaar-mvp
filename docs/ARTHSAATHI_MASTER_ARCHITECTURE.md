# ArthSaathi — Consolidated Master Architecture

Status: canonical implementation baseline, 2026-10-06

## Rules
1. This architecture is independent of V3–V7 historical application flows.
2. There is one canonical destination for each module.
3. Register Credit creates a new credit; Loans & Udhaar is the account/history centre for existing and closed accounts.
4. Repayment Centre is the single repayment engine.
5. MIS is the cross-module intelligence/reporting layer and presents recorded values before derived insights.
6. Group Khata / Group Expenses replaces the earlier TTMM naming.
7. Protected actions use the common consent/audit boundary; production OTP/provider integration is a separate integration layer.

## Canonical layers
HOME → RECORD → CREDIT → ASSETS → GROW → PROTECT → CLAIM/LEGACY → LEGAL → PEOPLE → DOCUMENT/INTELLIGENCE → AI ADVISOR → REVENUE/PAYMENT → PLATFORM/SECURITY

## Canonical modules
- Home / Command Centre
- Register Credit
- Loans & Udhaar
- QR Khata / Trade Credit
- Group Khata / Group Expenses
- Repayment Centre
- People & Relationships
- Asset Vault
- Liability Vault
- Portfolio & Investments
- MIS / Money Report
- Portfolio Switch Analysis
- Insurance & Protection
- Benefits & Refunds
- ChargeCheck
- Claim Assistance
- Will / Inheritance / Legacy
- Legal Help
- Advocate Directory
- Document Vault
- ArthSaathi AI Advisor
- Revenue & Payments
- Security & Consent
- Integrations

## Core relationships
Common data/core → relationship engine → module services → event bus → consent/audit → MIS/AI/reporting.

## Credit registration flow
Party identification → nature of credit → credit-specific fields → lending/payment method → repayment terms → guarantor → digital documentation → consent/OTP boundary → registration → account appears in Loans & Udhaar → repayment handled only by Repayment Centre.
