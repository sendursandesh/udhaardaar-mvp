# ArthSaathi Consolidated Master Architecture — Executable Model

## 1. Independence
This build is the exclusive consolidated architecture. Historical V3/V4/V5/V6/V6.2/V7 code is reference-only and is not a production dependency.

## 2. Canonical hierarchy
HOME → RECORD → CREDIT → ASSETS → GROW → PROTECT → CLAIM/LEGACY → LEGAL → PEOPLE → DOCUMENT/INTELLIGENCE → AI ADVISOR → REVENUE/PAYMENT → PLATFORM/SECURITY.

## 3. Canonical module ownership
HOME: Command Centre.
RECORD: Register Credit, QR Khata / Trade Credit, Group Khata / Group Expenses.
CREDIT: Loans & Udhaar, Repayment Centre, Repayment Schedule.
ASSETS: Asset Vault, Liability Vault.
GROW: Portfolio & Investments, Portfolio Switch Analysis.
PROTECT: Insurance & Protection, Benefits & Refunds, ChargeCheck.
CLAIM/LEGACY: Claim Assistance, Will / Inheritance / Legacy.
LEGAL: Legal Help, Advocate Directory.
PEOPLE: People & Relationships, Borrower / Counterparty Profile, PIN / Maps Address.
DOCUMENT/INTELLIGENCE: Document Vault, Document / Invoice Capture, MIS / Money Report.
AI: ArthSaathi AI Advisor.
REVENUE: Revenue & Payments.
PLATFORM: Security & Consent, authorization gateway, Integrations.

## 4. Core rules
- Register Credit is exclusively for NEW credit creation.
- Loans & Udhaar exclusively owns EXISTING and CLOSED account history.
- Every account has one canonical detail destination.
- Repayment Centre is the only repayment engine.
- MIS reads approved cross-module data and presents actual recorded values before derived analysis.
- Group Khata / Group Expenses is the canonical replacement for TTMM.
- Protected credit history, repayment, guarantor, documents and claims require authorization and audit.
- Digital DPN/document evidence belongs to the document layer and is linked to the owning transaction.
- Portfolio switching is advisory and explainable: return, cost, risk, eligibility and opportunity cost are compared before a user decision.
- Revenue and payment status are transaction records, not UI-only values.

## 5. Core data model
Person → Address → Relationship → CreditAccount → ScheduleItem → Repayment.
Person → Guarantor / Nominee.
CreditAccount → Documents / Consent / Audit.
Person → Assets / Liabilities / Portfolio / Insurance / Benefits.
Asset → Nominee → Claim → LegalCase → Advocate.
TradeCredit → QR/Invoice → Document.
Group → Members → Expense → Shares → Settlement.
Service → Charge → Payment → Revenue.
All approved records → MIS metrics → charts/insights → AI advisory.

## 6. Canonical flows
Register Credit:
party → nature → specific fields → method → repayment → guarantor → document/DPN → authorization → registration → Loans & Udhaar.

Existing Credit:
account list/search → authorization → complete detail → documents → schedule → repayment history → outstanding/status.

Repayment:
account → payable → repayment → authorization → record → balance → schedule → receipt → closure.

QR Trade Credit:
scan → extract → verify vendor/date/amount → counterparty → trade record → evidence → authorization → register.

Group Khata:
group → payer/total → members → shares → contributions → outstanding → settlement.

Asset Claim:
asset → ownership/nominee → evidence → claimant → claim → legal assistance → tracking → outcome.

ChargeCheck:
sanction letter → sanctioned charges → bank statement → actual charges → normalize → compare → variance → action.

Portfolio Switch:
portfolio → risk appetite → alternatives → return/cost → opportunity cost → risk/eligibility → explanation → user decision.

Benefits/Refunds:
source → evidence → value → application → approved → received → MIS.

Revenue:
service → charge → terms → payment gateway → result → transaction → receipt → MIS.

## 7. MIS
Required first-hand outputs include actual credit, repayment, outstanding, assets, portfolio, liabilities, benefits/refunds value generated, group dues, revenue and charge variance. Chart layers may present portfolio/assets/liabilities/net position after the underlying values are recorded.

## 8. Verification
The runtime architecture guard verifies uniqueness, critical module separation, canonical flows, security policy presence and MIS metric presence. Unit tests additionally verify module IDs/routes and required master areas.
