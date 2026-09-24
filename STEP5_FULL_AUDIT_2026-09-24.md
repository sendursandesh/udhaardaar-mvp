# Step 5 Full Architecture / Flow / Regression Audit — 2026-09-24

## Scope
Audit of the Step 5 baseline at commit `07a141328113552d36d16e8b83370a0c5daa2fd8`, covering V7 architecture, V5 integration boundary, navigation, inter-module refresh, consent/ownership controls, screens, branding and regression gates.

## Findings

### Critical / release-blocking
1. **V7 was not the authenticated landing route.** Login entered V5Home, leaving the V7 command centre outside the normal user journey.
2. **V7 module navigation bypassed the consent-safe V5 credit flow.** The V7 Credit tile opened the older RegisterCreditV3Activity, which does not implement the full V5 borrower → terms → documents → OTP-consent → registration sequence.
3. **V7 data ownership was not enforced on reads.** V7Core.all() returned all records from the local store, while records were stamped with ownerUserId. This created a cross-account visibility risk.
4. **V7 event publication had incomplete entity mapping.** Repayment records did not publish REPAYMENT_CHANGED.
5. **The V7-to-V6.2 bridge was not installed at application startup**, so the canonical event boundary could exist without being active.
6. **The approved ArthSaathi logo was not present in the Step 5 tree.** The drawable still contained the older Udhaardaar logo.

### High
7. V7ModuleActivity used generic fallback routes for several modules; these are now explicitly routed to the existing service centre rather than silently presenting an unrelated dashboard.
8. V7 screens are dynamically constructed, so visual regression needs emulator execution in addition to source inspection.
9. V7Core's event bus is in-process and non-persistent; it is suitable for the current local migration boundary but is not yet a durable cross-process/event-store architecture.
10. PIN/OTP in the current MVP is demo/local verification; production SMS/authentication infrastructure remains a release dependency.

### Medium
11. V7 location resolution validates PIN format but does not yet resolve authoritative postal data.
12. The existing legacy V3 dashboard/records screens use separate persistence from the V7 core, so full live cross-module metrics still require an explicit repository/migration adapter rather than assuming both stores are one database.

## Rectifications applied in this audit branch

- Routed successful login to V7HomeActivity.
- Routed V7 Credit to V5CreditRegistrationActivity, preserving the OTP/document/consent gate.
- Routed V7 Repayment to V5RepaymentActivity.
- Added V7 REPAYMENT_CHANGED event publication.
- Enforced current-user ownership filtering for V7 reads and ownership validation on V7 updates.
- Installed V7LegacyEventBridge during Application startup.
- Removed duplicate V7 module routing introduced during rectification.
- Restored the approved enclosed A-tree ArthSaathi logo.
- Changed application label from Udhaardaar to ArthSaathi.
- Kept the existing V5 service centre as the compatibility destination for V7 modules whose dedicated V7 UI is not yet complete.

## Regression gate

The previous CI architecture build compiled successfully and passed unit tests, but its emulator smoke gate failed with focus/lifecycle failures. Therefore **source compilation alone is not accepted as release evidence**.

Required next gate:
1. clean debug/release compile;
2. unit tests;
3. emulator smoke tests;
4. login → V7 home;
5. Credit → borrower → document → lender OTP → borrower OTP → registration;
6. Repayment → consent/evidence → outstanding refresh;
7. Asset/Document/Legal navigation;
8. background/foreground and keyboard-scroll tests;
9. logo/branding visual check;
10. fresh-install and upgrade-install checks;
11. final APK integrity and package/version validation.

## Release status

**Architecture audit:** rectifications applied in this branch.

**APK release status:** NOT YET CERTIFIED from this audit branch until the complete CI/emulator regression gate passes.
