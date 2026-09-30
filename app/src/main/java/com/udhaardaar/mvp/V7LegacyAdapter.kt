package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent

/**
 * Single compatibility boundary for legacy V5/V6.2 capabilities.
 * V7 presentation code calls this adapter instead of naming legacy activities directly.
 */
object V7LegacyAdapter {
    enum class Route {
        PEOPLE, DOCUMENTS, CREDIT, QR_KHATA, TRADE, FORMAL_CREDIT, REPAYMENT,
        MIS, INSURANCE, ASSET_VAULT, LEGAL, TTMM, CHARGECHECK, LIABILITY,
        BENEFITS, RENTAL, GUARANTOR, QR_SCANNER
    }

    fun open(context: Context, route: Route) {
        val intent = when (route) {
            Route.PEOPLE -> Intent(context, V62ExtendedModulesActivity::class.java).putExtra("openSection", "PEOPLE")
            Route.DOCUMENTS -> Intent(context, V62ExtendedModulesActivity::class.java).putExtra("openSection", "DOCUMENTS")
            Route.CREDIT -> Intent(context, V62CreditRegistrationActivity::class.java)
            Route.QR_KHATA -> Intent(context, V62QRKhataActivity::class.java)
            Route.TRADE -> Intent(context, V62ExtendedModulesActivity::class.java).putExtra("openSection", "TRADE")
            Route.FORMAL_CREDIT -> Intent(context, V62CreditIntelligenceActivity::class.java)
            Route.REPAYMENT -> Intent(context, V62RepaymentActivity::class.java)
            Route.MIS -> Intent(context, V62MISActivity::class.java)
            Route.INSURANCE -> Intent(context, V62InsuranceActivity::class.java)
            Route.ASSET_VAULT -> Intent(context, V62AssetVaultActivity::class.java)
            Route.LEGAL -> Intent(context, V62LegacyLegalAIActivity::class.java)
            Route.TTMM -> Intent(context, V62TTMMActivity::class.java)
            Route.CHARGECHECK -> Intent(context, V62ExtendedModulesActivity::class.java).putExtra("openSection", "CHARGECHECK")
            Route.LIABILITY -> Intent(context, V62ExtendedModulesActivity::class.java).putExtra("openSection", "LIABILITY")
            Route.BENEFITS -> Intent(context, V62ExtendedModulesActivity::class.java).putExtra("openSection", "BENEFITS")
            Route.RENTAL -> Intent(context, V62ExtendedModulesActivity::class.java).putExtra("openSection", "RENTAL")
            Route.GUARANTOR -> Intent(context, V62ExtendedModulesActivity::class.java).putExtra("openSection", "GUARANTOR")
            Route.QR_SCANNER -> Intent(context, QrCreditScannerActivity::class.java)
        }
        context.startActivity(intent)
    }
}
