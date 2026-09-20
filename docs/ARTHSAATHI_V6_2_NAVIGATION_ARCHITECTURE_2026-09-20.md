# ArthSaathi V6.2 — Navigation & Module Ownership Architecture

## Objective
Every user-facing module has one menu owner. A related workflow may open another module when required, but the target is not repeated as a second menu tile.

## Home — major journeys only
1. Register Credit
2. Repayment
3. Asset Vault
4. Protect
5. MIS & Analytics
6. Legacy & Claims

Home intentionally does not contain separate Legal or AI buttons. Legal Assistance and AI Financial Advisor are sub-services inside the single Legacy & Claims hub.

The Home financial hero is informational; it is not a second MIS button.

## More — supporting services
### Record • Finance • Check
- Formal Credit
- Funding / Lending
- ChargeCheck
- QR Udhaar Khata
- Together • Share & Settle
- Credit Intelligence

### People • Assets • Protection
- Liability Vault
- Family • Contacts
- Address & Location
- Schemes & Benefits
- Rental & Lease
- Documents & Notes

### Report • Support • Return
- Reports & Statements

Asset Vault, Protect, MIS, Legacy & Claims, Register Credit and Repayment are deliberately absent from More because they already belong to Home.

## Bottom navigation — utility only
- Home
- Profile
- Alerts
- More

Credit, Repayment, Asset Vault, MIS and other financial modules are not repeated in the bottom bar.

## Legacy & Claims hub
Single owner for:
- Will & Nomination
- Claim Assistance
- Legal Assistance / advocate support
- AI Financial Advisor

Alerts are no longer a Legacy service card because Alerts has its own bottom-bar location.

## Routing rule
V62ModuleRegistry is the source of truth for route ownership. More submodules use an explicit openSection route into the Financial Centre. This prevents the same screen from being independently wired in several menu locations.

## QA rules
- No duplicate route key.
- Home and More sets must be disjoint.
- Legal, AI and Guarantor are flow-only routes, not menu duplicates.
- Bottom navigation contains utility destinations only.
- Every More tile must resolve to an implemented section/activity.
- Menu entry must open the intended screen, survive back navigation, and not recreate the same parent incorrectly.
- Module-to-module links are permitted only when they are part of a documented business flow (for example Formal Credit → ChargeCheck); they are not counted as duplicate menu placement.
