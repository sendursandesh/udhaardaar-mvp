# ArthSaathi V6 Master Product Checklist

Brand: ArthSaathi
Tagline: Your Money. Your Records. Your Rights.

## 10 product modules

1. Udhaardaar / Informal Credit
- borrower search and history consent
- credit direction, terms, ROI, periodicity, repayment schedule
- guarantor
- DPN / digital documentation
- borrower/counterparty OTP consent
- audit trail
- repayment requests, evidence and counterparty confirmation
- overdue and credit-score foundation

2. Informal Credit Intelligence / Bureau
- consolidated informal credit history
- repayment behaviour
- overdue/default/settlement events
- explainable score
- consent and audit controls
- future bureau/reporting layer

3. QR Khata
- merchant and customer identity
- QR scan entry point
- daily credit purchases
- transaction ledger and open balance
- repayment linkage
- audit trail
- future merchant QR settlement/network layer

4. Asset Vault
- financial assets
- non-financial assets
- ownership metadata and evidence
- nominee/beneficiary
- trusted person
- succession workspace
- claim lifecycle

5. Insurance & Benefits Protect
- formal insurance
- debit/credit-card linked cover
- PMSBY/PMJJBY and other eligible schemes/benefits
- expiry/renewal alerts
- condition/eligibility rules
- claim-readiness evidence
- user acknowledgement and notifications

6. ChargeCheck
- sanction letter upload
- account statement upload
- OCR extraction with review/confidence
- sanctioned vs actual charges
- interest/fees/taxes/penalties/refunds
- variance/evidence linkage
- claim/complaint handoff

7. TTMM Shared Expenses
- group creation
- expense capture
- split/share calculation
- balances and settlement
- consent/audit safeguards
- no dead-end settlement state

8. Will & Legacy Planner
- asset-linked beneficiary selection
- Will draft generation
- executor/instruction fields
- versioned drafts
- legal-review handoff
- clear draft-not-legal-advice status

9. Legal Assistance
- legal domain selection
- lawyer directory
- location/locality
- expertise/contact/address
- verification status
- direct call/contact
- contextual handoff from claims, inheritance and ChargeCheck

10. ArthSaathi AI Advisor
- user risk/liquidity/horizon preferences
- explainable rule-based alerts in V6
- idle-cash review signal
- investment review signal framework
- insurance-condition alerts
- renewal alerts
- credit/overdue reminders
- no automatic money movement or investment switch
- explicit reason for every suggestion

## Cross-module requirements

- unified ArthSaathi branding and tree-style A logo
- responsive portrait UI
- keyboard-safe adjustResize behaviour
- scroll-safe long forms
- visible focus/cursor progression
- no dead-end navigation; every module has back/home path
- OTP before protected consent events
- digital documentation before final informal-credit registration
- audit event for protected actions
- user-controlled notification preferences
- clear distinction between records, suggestions and executed actions
- privacy/consent boundary for personal financial data

## Release gates

- source compiles
- APK assembles
- APK integrity/installability passes
- login/create/reset flows work
- all ten module entry points open
- all module save/view flows work without crashes
- existing V5 credit/repayment/asset/claim/ChargeCheck workflows remain reachable
- fresh APK produced from main
