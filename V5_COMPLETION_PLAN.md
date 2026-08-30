# V5 completion execution order

1. Replace legacy launcher/home routing with V5 shell.
2. Extend/migrate persistence for profile, guarantor, credit, consent, documents, repayment requests, assets, nominees, succession and claims.
3. Wire credit registration with borrower consent gate, score display, DPN and guarantor guarantee.
4. Wire bilateral repayment request/OTP confirmation; remove direct informal mark-paid actions.
5. Wire formal sanction/statement upload + OCR adapter + deterministic reconciliation.
6. Wire dedicated rental/lease workflow and schedules.
7. Wire financial/non-financial vault + trusted-person permissions.
8. Wire succession/deceased-owner and claim checklist/status/evidence bundle.
9. Wire legal assistance/referral and privacy/access controls.
10. Add migration, error handling, loading states, empty states and recovery paths.
11. Build debug APK and run static/build gates.
12. Run manual device smoke matrix and fix every failure.
13. Tag release candidate only after all mandatory acceptance checks pass.
