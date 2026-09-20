# ArthSaathi V6.2 — Final Architecture Contract

## 1. Product principle
ArthSaathi is a connected financial-life system, not a collection of isolated screens. The common graph is:

**Identity → People/Businesses → Relationships → Transactions → Documents → Assets/Liabilities → Protection → Credit Intelligence → Funding → Legacy → MIS → AI → Alerts**

Every protected action is consented, OTP/audit controlled, and every module must have a reachable Home/Back path.

## 2. First-class modules
1. Home / Financial Dashboard
2. Profile & Identity
3. Family
4. Contacts / People & Businesses
5. Address & Geolocation
6. Informal Credit / Udhaar
7. QR Udhaar Khata
8. Informal Credit Intelligence / ArthSaathi Score
9. Trade Credit
10. Accounting / ERP Integration
11. Formal Credit
12. Funding & Lending Marketplace
13. Repayment Centre
14. Guarantor
15. ChargeCheck
16. Asset Vault
17. Liability Vault
18. Insurance & Benefits Protect
19. Rental & Lease
20. TTMM Shared Expenses
21. Document Vault & Document Intelligence
22. MIS / Financial Analytics
23. Reports & Statements
24. Government Schemes & Benefits
25. Inheritance & Claim Assistance
26. Will / Nomination / Legacy Planner
27. Legal Assistance
28. ArthSaathi AI Advisor
29. Alerts & Notifications
30. Consent / OTP / Digital Confirmation
31. Security & Account Management
32. Integration / API Platform

## 3. Relationship-first credit architecture
A new credit starts as a relationship draft. Counterparty search uses permitted identifiers; if absent, profile creation is allowed only inside that relationship draft. No arbitrary third-party profile creation.

For history access: **request consent → OTP → show repayment history/outstanding/behaviour/score → continue lending decision**.

For registration: **terms → guarantor/evidence → promissory note → separate registration consent/OTP → active relationship → repayment schedule → events**.

Supported modes: EMI, principal + interest, bullet. Trade credit is a relationship plus accounting/invoice evidence, not a free-form ledger entry.

## 4. QR Udhaar Khata
QR is an entry point into a real relationship. Scan/request → identify party → explicit consent where protected data is requested → create/open Khata relationship → transaction ledger → outstanding → repayment → score/history events. QR scanning alone never exposes private financial history.

## 5. Trade Credit + ERP integration
Use a vendor-neutral adapter/API contract. Never embed SAP/Tally-specific logic into UI.

Canonical import fields include customer/supplier master, GSTIN/PAN, invoice number/date, amount, credit period, due date, outstanding, overdue, credit limit, payment history, debit/credit notes, source system and source record ID.

Adapters can target SAP, Tally and future accounting/ERP systems. Imported records retain source provenance and user/company authorisation. Conflicts require review rather than silent overwrite.

## 6. Formal Credit + Funding Marketplace
Formal credit supports bank/NBFC/institutional loan products, eligibility/application, offer comparison and document/consent workflow.

Funding Marketplace allows a user to explicitly request funding using their ArthSaathi profile. Potential providers may include authorised institutions and other legally permitted participants. Consent is explicit, revocable where applicable, and scanning a QR does not itself authorise disclosure or funding.

Flow: **funding intent → consent → profile/score package → eligible providers → offers → user acceptance → formal/relationship creation → repayment**.

## 7. ChargeCheck
Ingest sanction letters and account statements; retain evidence; OCR/extract with confidence and review; compare sanctioned vs actual interest, fees, taxes, penalties, insurance and other charges; calculate variance; link evidence; provide complaint/claim handoff without presenting legal conclusions as facts.

## 8. Asset / liability / protection graph
Assets and liabilities have ownership, evidence, nominee/beneficiary and trusted-person metadata. Insurance and benefits connect to people, nominees, expiry/renewal and claim readiness. Government schemes use eligibility conditions and user acknowledgement. No automatic financial transaction is executed by an alert.

## 9. Document Intelligence
Original evidence is retained. OCR is page-aware. Extracted fields carry confidence, source page/text, critical flag and user-confirmed/user-edited status. Critical terms are highlighted before save. Semantic intelligence may be supplied by a replaceable service boundary; the app must never silently treat uncertain extraction as verified fact.

## 10. MIS
MIS consumes connected financial events and provides tables plus visual charts for asset allocation, returns received, risk, idle funds, average investment, period-wise investment, charges, interest received, opportunity cost versus a defined comparison/highest-yield portfolio, app savings and opportunity cost saved.

## 11. Legacy / Legal
Will drafts are versioned and asset-linked; nomination/beneficiary selections connect to Family and Asset/Insurance records. Claim assistance connects to documents and protection records. Legal Assistance supports locality/domain/expertise/contact and verification status; legal review is a handoff, not an AI substitute.

## 12. AI Advisor
V6.2 uses explainable rules/service boundaries. Inputs include liquidity, risk, horizon, assets, liabilities, credit, protection, idle funds, renewals, benefits and legacy gaps. Every suggestion states its reason. No automatic money movement, investment switch or legally binding action.

## 13. Address & Geolocation
Reusable AddressService supports location-assisted capture (when the user permits it), manual correction and PIN-code enrichment for city/state/district/post office. Location is an input aid, not truth; user confirmation is required and existing entered addresses are never silently replaced.

## 14. UI contract — frozen
Brand: **ArthSaathi**
Tagline: **Navigate Your Financial Journey**
Pillars: **Plan • Protect • Grow • Nominate**

Approved mark: compass/route + strong A gateway + gold journey road + destination star + blue horizon. Do not substitute a generic rupee, bank, shield, house or stock-chart logo.

Visual system: premium light background, navy typography, teal/blue primary actions, restrained gold journey accent, rounded cards, clear hierarchy, responsive portrait layouts. All long forms use ScrollView/keyboard-safe adjustResize. Focus progression and cursor visibility are required.

Every page follows the same shell: approved logo → ArthSaathi title → contextual subtitle → grouped cards/sections → primary action → secondary action → Back/Home. No V5 screen is a normal V6.2 navigation target.

## 15. Persistence/event contract
Modules communicate through shared domain entities and event bus. Core events include relationship changed, repayment changed, document added, asset changed, liability changed, policy changed, trade import changed, TTMM expense changed, consent changed, funding request changed, charge-check changed and alert generated.

## 16. Release gates
- compile succeeds
- APK assembles and is non-empty/integrity checked
- V6.2 version remains 64 / 6.2
- Login/create/reset and session persistence work
- all first-class module entry points are reachable
- module save/view flows do not crash
- no V5 navigation from V6.2 home/module routes
- logo and visual contract resources are present
- architecture/domain registry and event bus are present
- document scanner/intelligence and MIS chart components are present
- keyboard-safe and minimize/resume behaviour are tested before calling the APK release-ready
