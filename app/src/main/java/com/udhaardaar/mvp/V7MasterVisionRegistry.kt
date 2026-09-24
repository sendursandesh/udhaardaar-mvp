package com.udhaardaar.mvp

/**
 * ArthSaathi V7 Step 6 — Master Vision module ownership.
 *
 * This registry is the canonical V7 ownership map. A module marked NATIVE is
 * implemented by V7-owned code/data. LEGACY_ADAPTER means functionality still
 * resides in a V5/V6.2 engine and is reachable only through V7LegacyAdapter.
 *
 * Step 6 uses this registry to prevent accidental reintroduction of direct
 * legacy dependencies while the remaining modules are migrated.
 */
object V7MasterVisionRegistry {
    enum class Ownership { NATIVE, LEGACY_ADAPTER }

    enum class Module(
        val key: String,
        val displayName: String,
        val ownership: Ownership,
        val v7DataKeys: List<String>
    ) {
        RECORD("RECORD", "Record & Identity", Ownership.NATIVE,
            listOf(V7Core.Keys.PEOPLE, V7Core.Keys.BUSINESSES, V7Core.Keys.ADDRESSES, V7Core.Keys.DOCUMENTS)),
        CREDIT("CREDIT", "Credit & Money Relationships", Ownership.NATIVE,
            listOf(V7Core.Keys.RELATIONSHIPS, V7Core.Keys.REPAYMENTS, V7Core.Keys.PAYMENTS, V7Core.Keys.INVOICES)),
        ASSETS("ASSETS", "Asset Vault", Ownership.NATIVE,
            listOf(V7Core.Keys.ASSETS, V7Core.Keys.HOLDINGS, V7Core.Keys.NOMINEES)),
        LIABILITIES("LIABILITIES", "Liability Vault", Ownership.NATIVE,
            listOf(V7Core.Keys.LIABILITIES)),
        PROTECT("PROTECT", "Insurance & Protection", Ownership.NATIVE,
            listOf(V7Core.Keys.POLICIES, V7Core.Keys.CLAIMS, V7Core.Keys.NOMINEES, V7Core.Keys.DOCUMENTS)),
        GROW("GROW", "Portfolio & Financial Intelligence", Ownership.NATIVE,
            listOf(V7Core.Keys.PORTFOLIOS, V7Core.Keys.HOLDINGS, V7Core.Keys.MARKET, V7Core.Keys.SCENARIOS)),
        LEGAL("LEGAL", "Legal & Claims", Ownership.NATIVE,
            listOf(V7Core.Keys.LEGAL, V7Core.Keys.CLAIMS, V7Core.Keys.PROFESSIONALS)),
        BENEFITS("BENEFITS", "Government Benefits", Ownership.NATIVE,
            listOf(V7Core.Keys.SERVICES, V7Core.Keys.ALERTS)),
        TTMM("TTMM", "Together • Share & Settle", Ownership.NATIVE,
            listOf(V7Core.Keys.TTMM, V7Core.Keys.PAYMENTS)),
        QR_KHATA("QR_KHATA", "QR Udhaar Khata", Ownership.LEGACY_ADAPTER,
            listOf(V7Core.Keys.QR, V7Core.Keys.RELATIONSHIPS)),
        TRADE("TRADE", "Trade Credit", Ownership.NATIVE,
            listOf(V7Core.Keys.TRADE, V7Core.Keys.INVOICES, V7Core.Keys.RELATIONSHIPS)),
        FORMAL_CREDIT("FORMAL_CREDIT", "Formal Credit", Ownership.LEGACY_ADAPTER,
            listOf(V7Core.Keys.FORMAL, V7Core.Keys.RELATIONSHIPS, V7Core.Keys.DOCUMENTS)),
        REPAYMENT("REPAYMENT", "Repayment Centre", Ownership.NATIVE,
            listOf(V7Core.Keys.REPAYMENTS, V7Core.Keys.RELATIONSHIPS, V7Core.Keys.CONSENTS)),
        MIS("MIS", "MIS & Reports", Ownership.NATIVE,
            listOf(V7Core.Keys.AUDIT, V7Core.Keys.RELATIONSHIPS, V7Core.Keys.ASSETS, V7Core.Keys.LIABILITIES)),
        RENTAL("RENTAL", "Rental & Lease", Ownership.LEGACY_ADAPTER,
            listOf(V7Core.Keys.RELATIONSHIPS, V7Core.Keys.DOCUMENTS)),
        GUARANTOR("GUARANTOR", "Guarantor", Ownership.NATIVE,
            listOf(V7Core.Keys.PEOPLE, V7Core.Keys.RELATIONSHIPS, V7Core.Keys.DOCUMENTS)),
        CHARGECHECK("CHARGECHECK", "ChargeCheck", Ownership.LEGACY_ADAPTER,
            listOf(V7Core.Keys.DOCUMENTS, V7Core.Keys.LIABILITIES)),
        QR_SCANNER("QR_SCANNER", "QR Scanner", Ownership.LEGACY_ADAPTER,
            listOf(V7Core.Keys.QR))
    }

    fun all(): List<Module> = Module.entries

    fun native(): List<Module> = all().filter { it.ownership == Ownership.NATIVE }

    fun legacyBacked(): List<Module> = all().filter { it.ownership == Ownership.LEGACY_ADAPTER }

    fun find(key: String): Module? = all().firstOrNull { it.key == key }
}
