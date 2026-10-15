# ArthSaathi V6.2 QA Audit — 20 Sep 2026

Branch: arthsaathi-v6.2-functional-rebuild-2026-09-20

This build cycle incorporates the saved ArthSaathi design draft: gold/navy branding, enclosed A-tree/star journey logo, compact three-column dashboard action tiles, five-item bottom navigation, and grouped More/Financial Centre sub-folders.

Runtime-critical corrections in this cycle:
- MIS keeps one ScrollView parent instead of re-parenting the same root during refresh.
- Insurance & Protection keeps one ScrollView parent across manual-entry/OCR refresh.
- Rental & Lease keeps one ScrollView parent across manual-entry/OCR refresh.
- Legacy • Legal • AI keeps one ScrollView parent across onResume refresh.
- Credit Intelligence keeps one ScrollView parent across onResume refresh.
- TTMM keeps one ScrollView parent across onResume refresh.
- Smoke audit expanded to Home, Credit, Repayment, Asset Vault, Insurance, Rental, TTMM, QR Khata, MIS, Credit Intelligence, ChargeCheck, Legacy/Legal/AI and More/Financial Centre, including resume/re-render paths.

Release gate remains: compile + unit tests + Android emulator smoke tests + APK artifact. CI pass alone is not treated as final.
