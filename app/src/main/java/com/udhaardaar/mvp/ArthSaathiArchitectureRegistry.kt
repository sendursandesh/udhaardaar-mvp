package com.udhaardaar.mvp

object ArthSaathiArchitectureRegistry {
    data class Module(val id: String, val title: String, val area: String, val route: String, val canonical: Boolean = true)

    val modules = listOf(
        Module("HOME","Home / Command Centre","HOME",ArthSaathiNavigation.HOME),
        Module("REGISTER_CREDIT","Register Credit","RECORD",ArthSaathiNavigation.REGISTER_CREDIT),
        Module("LOANS_UDHAAR","Loans & Udhaar","CREDIT",ArthSaathiNavigation.LOANS_UDHAAR),
        Module("QR_KHATA","QR Khata / Trade Credit","RECORD",ArthSaathiNavigation.QR_KHATA),
        Module("GROUP_KHATA","Group Khata / Group Expenses","RECORD",ArthSaathiNavigation.GROUP_KHATA),
        Module("REPAYMENT","Repayment Centre","CREDIT",ArthSaathiNavigation.REPAYMENT),
        Module("PEOPLE","People & Relationships","PEOPLE",ArthSaathiNavigation.PEOPLE),
        Module("ASSET_VAULT","Asset Vault","ASSETS",ArthSaathiNavigation.ASSET_VAULT),
        Module("LIABILITY_VAULT","Liability Vault","ASSETS",ArthSaathiNavigation.LIABILITY_VAULT),
        Module("PORTFOLIO","Portfolio & Investments","GROW",ArthSaathiNavigation.PORTFOLIO),
        Module("MIS","MIS / Money Report","INTELLIGENCE",ArthSaathiNavigation.MIS),
        Module("SWITCH_ANALYSIS","Portfolio Switch Analysis","GROW",ArthSaathiNavigation.SWITCH_ANALYSIS),
        Module("PROTECTION","Insurance & Protection","PROTECT",ArthSaathiNavigation.PROTECTION),
        Module("BENEFITS","Benefits & Refunds","PROTECT",ArthSaathiNavigation.BENEFITS),
        Module("CHARGECHECK","ChargeCheck","PROTECT",ArthSaathiNavigation.CHARGECHECK),
        Module("CLAIMS","Claim Assistance","CLAIM_LEGACY",ArthSaathiNavigation.CLAIMS),
        Module("WILL_LEGACY","Will / Inheritance / Legacy","CLAIM_LEGACY",ArthSaathiNavigation.WILL_LEGACY),
        Module("LEGAL","Legal Help","LEGAL",ArthSaathiNavigation.LEGAL),
        Module("ADVOCATES","Advocate Directory","LEGAL",ArthSaathiNavigation.ADVOCATES),
        Module("DOCUMENT_VAULT","Document Vault","INTELLIGENCE",ArthSaathiNavigation.DOCUMENT_VAULT),
        Module("AI_ADVISOR","ArthSaathi AI Advisor","AI",ArthSaathiNavigation.AI_ADVISOR),
        Module("REVENUE","Revenue & Payments","REVENUE",ArthSaathiNavigation.REVENUE),
        Module("SECURITY_CONSENT","Security & Consent","PLATFORM",ArthSaathiNavigation.SECURITY_CONSENT),
        Module("INTEGRATIONS","Integrations","PLATFORM",ArthSaathiNavigation.INTEGRATIONS)
    )

    val creditNatureOptions = listOf(
        "Personal Loan / Hand Loan", "Trade Credit / Udhaar", "Rental / Lease",
        "Formal Loan / Bank Credit", "Other Receivable / Payable"
    )

    fun canonicalModule(id: String): Module? = modules.firstOrNull { it.id == id && it.canonical }
}
