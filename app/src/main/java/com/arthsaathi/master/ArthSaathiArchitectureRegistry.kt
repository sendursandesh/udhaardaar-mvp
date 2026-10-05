package com.arthsaathi.master

object ArthSaathiArchitectureRegistry {
    data class Module(val id:String,val title:String,val section:String,val route:String)
    val modules=listOf(
        Module("REGISTER_CREDIT","Register Credit","Record",ArthSaathiNavigation.REGISTER_CREDIT),
        Module("LOANS_UDHAAR","Loans & Udhaar","Credit",ArthSaathiNavigation.LOANS_UDHAAR),
        Module("PEOPLE","People & Relationships","Record",ArthSaathiNavigation.PEOPLE),
        Module("REPAYMENT","Repayment Centre","Credit",ArthSaathiNavigation.REPAYMENT),
        Module("QR_KHATA","QR Khata / Trade Credit","Record",ArthSaathiNavigation.QR_KHATA),
        Module("GROUP_KHATA","Group Khata / Group Expenses","Record",ArthSaathiNavigation.GROUP_KHATA),
        Module("ASSET_VAULT","Asset Vault","Own",ArthSaathiNavigation.ASSET_VAULT),
        Module("LIABILITY_VAULT","Liability Vault","Own",ArthSaathiNavigation.LIABILITY_VAULT),
        Module("PORTFOLIO","Portfolio & Investments","Grow",ArthSaathiNavigation.PORTFOLIO),
        Module("MIS","MIS / Money Report","Grow",ArthSaathiNavigation.MIS),
        Module("SWITCH_ANALYSIS","Portfolio Switch Analysis","Grow",ArthSaathiNavigation.SWITCH_ANALYSIS),
        Module("PROTECTION","Insurance & Protection","Protect",ArthSaathiNavigation.PROTECTION),
        Module("BENEFITS","Benefits & Refunds","Protect",ArthSaathiNavigation.BENEFITS),
        Module("CHARGECHECK","ChargeCheck","Protect",ArthSaathiNavigation.CHARGECHECK),
        Module("CLAIMS","Claim Assistance","Claim",ArthSaathiNavigation.CLAIMS),
        Module("WILL_LEGACY","Will / Inheritance / Legacy","Claim",ArthSaathiNavigation.WILL_LEGACY),
        Module("LEGAL","Legal Help","Claim",ArthSaathiNavigation.LEGAL),
        Module("ADVOCATES","Advocate Directory","Claim",ArthSaathiNavigation.ADVOCATES),
        Module("DOCUMENT_VAULT","Document Vault","Claim",ArthSaathiNavigation.DOCUMENT_VAULT),
        Module("AI_ADVISOR","ArthSaathi AI Advisor","Intelligence",ArthSaathiNavigation.AI_ADVISOR),
        Module("REVENUE","Revenue & Payments","Platform",ArthSaathiNavigation.REVENUE),
        Module("SECURITY","Security, Consent & Audit","Platform",ArthSaathiNavigation.SECURITY),
        Module("INTEGRATIONS","Maps / Calendar / Payments / OCR","Platform",ArthSaathiNavigation.INTEGRATIONS)
    )
    val creditNatures=listOf("Personal / Hand Loan","Trade Credit / Udhaar","Rental / Lease","Formal Loan / Bank Credit","Other Receivable / Payable")
    fun byRoute(route:String)=modules.firstOrNull{it.route==route}
}
