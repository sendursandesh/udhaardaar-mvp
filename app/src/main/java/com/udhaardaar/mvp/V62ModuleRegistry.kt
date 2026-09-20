package com.udhaardaar.mvp

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Canonical V6.2 navigation ownership.
 *
 * A module has exactly one menu surface:
 * HOME = major journeys only
 * MORE = supporting modules/sub-modules
 * FLOW_ONLY = reached from a related workflow, never repeated as a menu tile
 *
 * Profile and Alerts are utility controls in the bottom bar and are deliberately
 * not repeated inside Home/More.
 */
object V62ModuleRegistry {
    enum class Surface { HOME, MORE, FLOW_ONLY }

    data class Route(
        val key: String,
        val title: String,
        val activity: Class<out Activity>,
        val surface: Surface,
        val section: String? = null
    )

    val routes = listOf(
        // Home: major journeys. Each appears here and nowhere else in the menu.
        Route("CREDIT", "Register Credit", V62CreditRegistrationActivity::class.java, Surface.HOME),
        Route("REPAYMENT", "Repayment Centre", V62RepaymentActivity::class.java, Surface.HOME),
        Route("ASSET_VAULT", "Asset Vault", V62AssetVaultActivity::class.java, Surface.HOME),
        Route("INSURANCE", "Protect", V62InsuranceActivity::class.java, Surface.HOME),
        Route("MIS", "MIS & Analytics", V62MISActivity::class.java, Surface.HOME),
        Route("LEGACY", "Legacy & Claims", V62LegacyLegalAIActivity::class.java, Surface.HOME),

        // More: supporting modules. They are not repeated on Home.
        Route("FORMAL_CREDIT", "Formal Credit", V62ExtendedModulesActivity::class.java, Surface.MORE, "FORMAL"),
        Route("FUNDING", "Funding / Lending", V62ExtendedModulesActivity::class.java, Surface.MORE, "FUNDING"),
        Route("CHARGECHECK", "ChargeCheck", V62ExtendedModulesActivity::class.java, Surface.MORE, "CHARGECHECK"),
        Route("QR_KHATA", "QR Udhaar Khata", V62ExtendedModulesActivity::class.java, Surface.MORE, "QR_KHATA"),
        Route("TTMM", "Together • Share & Settle", V62ExtendedModulesActivity::class.java, Surface.MORE, "TTMM"),
        Route("CREDIT_INTELLIGENCE", "Credit Intelligence", V62ExtendedModulesActivity::class.java, Surface.MORE, "CREDIT_INTELLIGENCE"),
        Route("LIABILITY_VAULT", "Liability Vault", V62ExtendedModulesActivity::class.java, Surface.MORE, "LIABILITY"),
        Route("PEOPLE", "Profile • Family • Contacts", V62ExtendedModulesActivity::class.java, Surface.MORE, "PEOPLE"),
        Route("ADDRESS", "Address & Location", V62ExtendedModulesActivity::class.java, Surface.MORE, "ADDRESS"),
        Route("BENEFITS", "Schemes & Benefits", V62ExtendedModulesActivity::class.java, Surface.MORE, "BENEFITS"),
        Route("DOCUMENTS", "Documents & Notes", V62ExtendedModulesActivity::class.java, Surface.MORE, "DOCUMENTS"),
        Route("REPORTS", "Reports & Statements", V62ExtendedModulesActivity::class.java, Surface.MORE, "REPORTS"),
        Route("RENTAL", "Rental & Lease", V62RentalLeaseActivity::class.java, Surface.MORE, "RENTAL"),

        // Flow-only: intentionally reachable from related screens, not menu tiles.
        Route("LEGAL", "Legal Assistance", V62LegacyLegalAIActivity::class.java, Surface.FLOW_ONLY, "LEGAL"),
        Route("AI", "AI Financial Advisor", V62LegacyLegalAIActivity::class.java, Surface.FLOW_ONLY, "AI"),
        Route("GUARANTOR", "Guarantor", V62CreditRegistrationActivity::class.java, Surface.FLOW_ONLY, "GUARANTOR")
    )

    val primaryRoutes = routes

    val homeRoutes get() = routes.filter { it.surface == Surface.HOME }
    val moreRoutes get() = routes.filter { it.surface == Surface.MORE }

    fun route(key: String): Route? = routes.firstOrNull { it.key == key }

    fun intent(context: Context, key: String): Intent {
        val r = requireNotNull(route(key)) { "Unknown V6.2 route: $key" }
        return Intent(context, r.activity).apply {
            r.section?.let { putExtra("openSection", it) }
        }
    }

    fun keysBySurface(surface: Surface): List<String> =
        routes.filter { it.surface == surface }.map { it.key }
}
