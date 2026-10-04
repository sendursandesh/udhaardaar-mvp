package com.udhaardaar.mvp

/**
 * Canonical ArthSaathi V7 top-level navigation contract.
 *
 * Every top-level module has exactly one V7 owner and one destination path.
 * Sub-functions (MIS, reports, claims, TTMM actions, QR actions, etc.) are
 * tools inside their owning module and are never separate legacy routes.
 */
object V7MasterVisionRegistry {
    enum class Ownership { NATIVE, ROUTER }

    enum class Destination { NATIVE_MODULE, MODULE_ROUTER }

    enum class Module(
        val key: String,
        val displayName: String,
        val ownership: Ownership,
        val destination: Destination,
        val v7DataKeys: List<String>
    ) {
        RECORD("RECORD", "Record & Identity", Ownership.NATIVE, Destination.NATIVE_MODULE,
            listOf(V7Core.Keys.PEOPLE, V7Core.Keys.BUSINESSES, V7Core.Keys.ADDRESSES, V7Core.Keys.DOCUMENTS)),
        CREDIT("CREDIT", "Loans & Udhaar", Ownership.NATIVE, Destination.NATIVE_MODULE,
            listOf(V7Core.Keys.RELATIONSHIPS, V7Core.Keys.REPAYMENTS, V7Core.Keys.PAYMENTS, V7Core.Keys.INVOICES)),
        REPAYMENT("REPAYMENT", "Collect / Pay Dues", Ownership.NATIVE, Destination.NATIVE_MODULE,
            listOf(V7Core.Keys.REPAYMENTS, V7Core.Keys.RELATIONSHIPS, V7Core.Keys.CONSENTS)),
        ASSETS("ASSETS", "My Assets & Property", Ownership.NATIVE, Destination.NATIVE_MODULE,
            listOf(V7Core.Keys.ASSETS, V7Core.Keys.HOLDINGS, V7Core.Keys.NOMINEES)),
        LIABILITIES("LIABILITIES", "My Loans & Dues", Ownership.NATIVE, Destination.NATIVE_MODULE,
            listOf(V7Core.Keys.LIABILITIES)),
        PROTECT("PROTECT", "Insurance & Protection", Ownership.ROUTER, Destination.MODULE_ROUTER,
            listOf(V7Core.Keys.POLICIES, V7Core.Keys.DOCUMENTS, V7Core.Keys.NOMINEES, V7Core.Keys.ALERTS)),
        GROW("GROW", "Investments & Returns", Ownership.ROUTER, Destination.MODULE_ROUTER,
            listOf(V7Core.Keys.PORTFOLIOS, V7Core.Keys.HOLDINGS, V7Core.Keys.MARKET, V7Core.Keys.SCENARIOS)),
        TTMM("TTMM", "Group Expenses — TTMM", Ownership.ROUTER, Destination.MODULE_ROUTER,
            listOf(V7Core.Keys.TTMM, V7Core.Keys.PAYMENTS)),
        QR_KHATA("QR_KHATA", "QR Udhaar Khata", Ownership.ROUTER, Destination.MODULE_ROUTER,
            listOf(V7Core.Keys.QR, V7Core.Keys.RELATIONSHIPS, V7Core.Keys.CONSENTS)),
        LEGAL("LEGAL", "Legal Help & Claims", Ownership.ROUTER, Destination.MODULE_ROUTER,
            listOf(V7Core.Keys.LEGAL, V7Core.Keys.CLAIMS, V7Core.Keys.PROFESSIONALS)),
        MORE("MORE", "More Services", Ownership.ROUTER, Destination.MODULE_ROUTER,
            listOf(V7Core.Keys.SERVICES, V7Core.Keys.REVENUE, V7Core.Keys.AUDIT))
    }

    fun all(): List<Module> = Module.entries
    fun native(): List<Module> = all().filter { it.ownership == Ownership.NATIVE }
    fun legacyBacked(): List<Module> = emptyList()
    fun find(key: String): Module? = all().firstOrNull { it.key == key }
    fun destination(key: String): Destination? = find(key)?.destination
}
