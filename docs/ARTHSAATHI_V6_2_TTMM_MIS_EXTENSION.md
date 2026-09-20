# ArthSaathi V6.2 — TTMM Contribution & MIS Extension

## TTMM proposition
TTMM remains the canonical shared-expense engine, with a clearer user-facing label: **Together • Share & Settle**.

The module explicitly supports the common case where one member pays an expense first and the other members contribute later. Example: a local gully-cricket group incurs ₹5,000; the initial payer records the expense, the app calculates each member's share, and members can subsequently record partial or full contributions against that exact expense.

## Data model
- `v62_ttmm_groups` — group and member roster.
- `v62_ttmm_expenses` — originating expense, payer, split method and member allocations.
- `v62_ttmm_contributions` — later contribution linked to the originating expense, member, recipient and amount.
- `v62_ttmm_settlements` — general group settlement entries.

A contribution cannot exceed the member's remaining share for the linked expense. The contribution ledger is therefore separate from the original expense and remains auditable.

## Cross-module flow
`TTMM Expense → Member Share → Contribution(s) → Remaining Due → Settlement → MIS`

TTMM expense and contribution events are published through the V6.2 event bus so MIS can refresh without maintaining a second source of truth.

## MIS extension
MIS now shows:
- current portfolio/asset value and asset-allocation donut;
- portfolio financial-position donut covering assets, liabilities, informal credit exposure and open TTMM dues;
- actual completed benefit value;
- completed refunds recovered;
- completed claim/recovery value;
- total value generated from those successfully completed records;
- TTMM expense, contributed and outstanding values.

Pending/rejected records are excluded from the completed-value figures.

## Architecture rule
The extension is built on the locked V6.2 golden-fix branch and does not replace the approved Picture 2 visual reference. It extends the existing V6.2 source-of-truth/event architecture rather than creating an isolated parallel workflow.
