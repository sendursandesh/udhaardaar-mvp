package com.udhaardaar.mvp

import android.app.Activity

/**
 * Canonical V6.2 module registry. It keeps navigation and ownership in one place
 * so a module cannot silently become an isolated screen.
 */
object V62ModuleRegistry {
    data class Route(val key: String, val title: String, val activity: Class<out Activity>)

    val primaryRoutes = listOf(
        Route("CREDIT", "Credit & Udhaar", V62CreditRegistrationActivity::class.java),
        Route("CREDIT_INTELLIGENCE", "Credit Intelligence", V62CreditIntelligenceActivity::class.java),
        Route("REPAYMENT", "Repayment Centre", V62RepaymentActivity::class.java),
        Route("ASSET_VAULT", "Asset Vault", V62AssetVaultActivity::class.java),
        Route("INSURANCE", "Insurance & Protection", V62InsuranceActivity::class.java),
        Route("RENTAL", "Rental & Lease", V62RentalLeaseActivity::class.java),
        Route("TTMM", "Together • Share & Settle", V62TTMMActivity::class.java),
        Route("MIS", "MIS & Analytics", V62MISActivity::class.java),
        Route("LEGACY", "Legacy, Legal & AI", V62LegacyLegalAIActivity::class.java),
        Route("FINANCIAL_CENTRE", "Financial Centre", V62ExtendedModulesActivity::class.java)
    )

    fun route(key: String): Route? = primaryRoutes.firstOrNull { it.key == key }
    fun activity(key: String): Class<out Activity>? = route(key)?.activity
}