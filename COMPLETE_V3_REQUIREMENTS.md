# Udhaardaar V3 — Complete correction/launch scope

This is the implementation acceptance contract for the complete correction pass.

## UX / UI
- Professional, simple, consistent UI and dashboard
- Handset-bounded responsive screens; no uncontrolled giant pages
- Keyboard/IME must never hide the focused field; automatic scroll-to-focus and safe bottom inset
- Clear sections, validation, errors, loading, empty and success states
- Profile photo visible on home/profile where appropriate
- Automatic cursor progression where appropriate

## Identity / profiles
- Borrower identification/search by name, mobile, PAN and Aadhaar
- Existing borrower history shown before new credit
- New borrower profile when not found
- Personal/business identity fields
- PIN entry autofills city/state where available
- PAN, Aadhaar, mobile, PIN and GSTIN validation
- GSTIN/PAN formats and lengths enforced; no unlimited input

## Credit types
- Personal credit
- Business/trade credit
- Rental/other agreed credit categories
- Credit-specific fields and documentation
- Trade credit invoice capture/upload foundation and structured invoice details
- Guarantor details

## Repayment
- EMI
- Principal + interest
- Fixed repayment / bullet-maturity where applicable
- ROI always presented/validated as percentage
- Periodicity
- Start/end dates
- Automatic repayment calculations
- Chronological repayment schedules and repayment centre
- Repayment history and transaction details
- Party-restricted repayment updates: only authorised lender/borrower for that loan may update it

## Consent / privacy / security
- OTP verification in relevant flows, especially credit creation
- Digital consent before registration
- Digital documentation foundation
- No third-party data disclosure without consent/authorisation
- Authorised-view-only access controls
- Audit trail/foundation for sensitive changes
- Secure handling of identity and financial data

## Dashboard / records / product foundation
- Dashboard totals and useful summaries
- Full records and transaction detail
- Profile/search/history navigation works
- Reminders foundation
- Borrower scoring foundation
- Legal-assistance/default workflow hooks for future expansion
- Scalable data model and modular business rules
- Robust error handling and recovery

## Acceptance
- All flows must be tested, not merely compiled.
- No known blocker, crash, inaccessible field, broken navigation or silent validation failure may remain before APK is called final.
- Baseline is V3; do not substitute V2.
