# ArthSaathi V7 Master Architecture Blueprint
## Branch: arthsaathi-v7-master-build-2026-09-23
## Source commit audited: 81b8030a46c52de12febdbc21626c25378811885

Status: Architecture reconciliation baseline

## 1. Architectural decision

V7 is the single application architecture and source of truth. V5/V6.2 code is retained only as implementation/reference material during controlled migration. New V7 screens must not create parallel domain truth.

Target flow:

UI -> V7 Presentation/Navigation -> V7 Application Services -> Canonical Domain Models -> Repository Interfaces -> Canonical Persistence
                                      |-> Consent Service -> OTP -> Audit
                                      |-> Canonical Event Bus -> dependent modules

Legacy V5/V6.2 adapters may sit behind repository/service interfaces temporarily, but V7 must own contracts and integration.

## 2. Master module map

| Module | V7 owner | Primary data | Key events | Consent |
|---|---|---|---|---|
| Home / Financial Command Centre | V7 Home | dashboard projections | DASHBOARD_REFRESHED | No |
| Profile & Identity | V7 Profile | person/account | PROFILE_CHANGED | Sensitive changes |
| People / Family / Relationships | V7 People | person/relationship | PERSON_CHANGED, RELATIONSHIP_CHANGED | Relationship/visibility as applicable |
| Address & Location | V7 Address | address/location | ADDRESS_CHANGED | No |
| Informal Credit | V7 Credit | credit/loan | CREDIT_CREATED/UPDATED | Required for protected borrower history/actions |
| Trade Credit | V7 Trade | invoice/trade credit | TRADE_CREDIT_CHANGED | Required for counterparty-protected actions |
| Formal Credit | V7 Formal | sanctioned facility | FORMAL_CREDIT_CHANGED | Required for protected financial data |
| Repayment Centre | V7 Repayment | repayment/schedule | REPAYMENT_CHANGED | Required for consent-controlled updates |
| Guarantor / Promissory | V7 Credit Documents | guarantor/document | DOCUMENT_CHANGED, CONSENT_GRANTED | Required |
| QR Khata | V7 QR | scan/credit linkage | QR_SCANNED | Required before protected disclosure |
| Score / Credit Intelligence | V7 Intelligence | score/projections | SCORE_REFRESHED | Score/history only after consent |
| Asset Vault | V7 Assets | assets/ownership | ASSET_CHANGED | Sensitive asset disclosure |
| Liability Vault | V7 Liabilities | liabilities | LIABILITY_CHANGED | Sensitive financial disclosure |
| Insurance / Protection | V7 Protect | policy/coverage | POLICY_CHANGED | Sensitive policy data |
| ChargeCheck | V7 ChargeCheck | sanctioned vs actual charges | CHARGECHECK_UPDATED | Required where another party's protected data is accessed |
| TTMM / Growth | V7 Grow | market/portfolio data | HOLDING_CHANGED, MARKET_REFRESHED | Account-level authorization |
| MIS / Analytics | V7 Analytics | projections/read models | ANALYTICS_REFRESHED | Account authorization |
| Documents / Intelligence | V7 Documents | document metadata/extraction | DOCUMENT_CHANGED | Document-specific |
| Nominee / Will / Legacy | V7 Legacy | nominee/will/claim | NOMINEE_CHANGED, LEGACY_CHANGED | Required for protected records |
| Claims / Legal | V7 Legal | claim/legal case | CLAIM_CHANGED | Required |
| Funding Marketplace | V7 Funding | funding request/application | FUNDING_CHANGED | Required |
| AI Financial Advisor | V7 Advisor | derived advice/context | ADVISORY_REFRESHED | Explicit account/data authorization |
| Alerts / Notifications | V7 Alerts | notification state | ALERT_CREATED/READ | Account authorization |
| Security / Account | V7 Security | session/device/security | SESSION_CHANGED | Required for security operations |
| Integration/API | V7 Platform | integration metadata | INTEGRATION_CHANGED | Explicit authorization |

## 3. Canonical flow rules

### Authentication
Mobile -> OTP provider -> verification -> authenticated session/token -> V7 Home.
Local/demo OTP is development-only and must not be release authentication.

### Protected borrower/counterparty data
Search identity -> obtain consent -> verify OTP where required -> retrieve minimum permitted data -> audit access -> expire/revoke consent.

### Credit registration
Borrower/counterparty -> credit type -> credit terms -> repayment method -> guarantor -> documents/promissory note -> consent/OTP -> persist transaction -> publish CREDIT_CREATED -> refresh repayment/dashboard/MIS projections.

### Repayment
Open authorized credit -> show chronological schedule -> record payment -> consent/OTP if required -> persist repayment -> publish REPAYMENT_CHANGED -> update outstanding/next due/dashboard/MIS.

### Asset / liability
Identify owner -> create/update record -> validate -> persist -> publish change event -> update dashboard/net-worth/analytics and related protection/claim views.

### Document intelligence
Capture/upload -> secure storage -> metadata -> optional OCR/extraction -> user confirmation -> canonical record update -> audit event.

## 4. Canonical contracts

V7 must define one canonical contract for:
- Person
- Account
- Relationship
- Address
- Credit
- CreditParty
- RepaymentSchedule
- Repayment
- Guarantor
- Document
- Consent
- AuditEvent
- Asset
- Liability
- InsurancePolicy
- Claim
- Nominee
- Portfolio/Holding
- FundingRequest
- Alert

Legacy DTOs must be mapped into these contracts rather than exposed directly to V7 UI.

## 5. Persistence rule

V7 Repository interfaces are the only persistence boundary visible to application services.

Migration sequence:
1. Define canonical contracts.
2. Add repository interfaces.
3. Wrap existing V5LocalStore/V6.2 storage behind adapters.
4. Add migration/read-reconciliation tests.
5. Move canonical writes to the new repository implementation.
6. Retire direct legacy storage calls.

Do not perform a destructive database rewrite before compatibility tests pass.

## 6. Event rule

V7 will have one canonical EventBus/Event contract.

Required canonical events include:
PROFILE_CHANGED
PERSON_CHANGED
RELATIONSHIP_CHANGED
ADDRESS_CHANGED
CREDIT_CREATED
CREDIT_CHANGED
REPAYMENT_CHANGED
DOCUMENT_CHANGED
CONSENT_GRANTED
CONSENT_REVOKED
ASSET_CHANGED
LIABILITY_CHANGED
POLICY_CHANGED
CLAIM_CHANGED
NOMINEE_CHANGED
HOLDING_CHANGED
FUNDING_CHANGED
ALERT_CREATED
SESSION_CHANGED

Legacy V6.2 events may be translated at the adapter boundary. No module should subscribe to multiple competing event buses.

## 7. Consent and security rule

One Consent Service owns:
request -> purpose -> scope -> subject -> actor -> OTP challenge -> verification -> timestamp -> expiry -> revocation -> audit.

No UI may directly decide that protected data is accessible.

Every protected read/write must pass through an authorization/consent policy.

## 8. Navigation rule

V7 owns navigation. V7 UI must not directly depend on legacy Activity names.

Temporary legacy launches are permitted only through a V7 adapter/router and must be tracked for migration.

## 9. Classification of existing architecture

KEEP:
- V7 visual foundation
- V7 Home / Financial Command Centre
- V7 module navigation concept
- V62ArchitectureSpec as functional reference
- proven V6.2 domain/service logic after adapterization
- encrypted V5LocalStore foundation while migration is controlled
- existing functional test assets where valid

MERGE:
- V7Core domain definitions + V6.2 domain contracts
- V7 and V6.2 event systems
- V7 and V6.2 consent systems
- V7 navigation + legacy module routing
- V7 dashboard + existing metrics engines
- V5/V6.2 persistence + canonical V7 repositories

REPLACE:
- local/demo OTP login for production authentication
- direct legacy Activity routing from V7
- duplicate consent decisions
- duplicate event buses
- direct UI access to legacy storage
- non-authoritative PIN resolution

RETIRE AFTER MIGRATION:
- obsolete V5 UI entry points
- duplicate V6.2/V7 navigation paths
- duplicate event definitions
- duplicate consent implementations
- direct legacy persistence calls
- development-only authentication paths

## 10. Dependency direction

Allowed:
Presentation -> Application -> Domain -> Repository interfaces
Infrastructure -> Repository implementations/adapters
Legacy adapters -> Domain/application contracts

Forbidden:
UI -> V5LocalStore
UI -> legacy database format
UI -> V6.2 event bus
UI -> direct OTP implementation
Legacy module -> V7 UI state

## 11. Definition of done for architecture

Architecture reconciliation is complete when:
- every production screen has one V7 owner;
- every persistent entity has one canonical model;
- every write has one canonical repository path;
- every protected action has one consent policy;
- every cross-module update uses the canonical event contract;
- V7 navigation can reach all production modules without direct legacy coupling;
- legacy components remaining in production are explicitly adapterized;
- authentication and location services have production implementations;
- integration tests prove cross-module updates.

## 12. Implementation order

Phase A: canonical contracts + event + consent interfaces
Phase B: repository boundary and legacy adapters
Phase C: V7 navigation ownership
Phase D: authentication/security and location services
Phase E: credit/repayment/people/document integration
Phase F: assets/liabilities/protection/legacy/legal/growth integration
Phase G: dashboard/MIS/AI/funding projections
Phase H: migration cleanup and legacy retirement
Phase I: full regression QA and release build

This blueprint is the architectural baseline for subsequent coding and QA on the V7 master branch.
