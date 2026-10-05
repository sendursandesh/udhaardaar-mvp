package com.udhaardaar.mvp

object ArthSaathiConsolidatedArchitecture {
    const val VERSION = "6.2-consolidated"
    const val BRAND = "ArthSaathi"
    const val TAGLINE = "Your Money. Your Records. Your Rights."

    data class Module(
        val id: String,
        val title: String,
        val category: String,
        val entryPoint: String,
        val dataStore: String,
        val supportsSave: Boolean = true,
        val supportsView: Boolean = true
    )

    val modules = listOf(
        Module("INFORMAL_CREDIT", "Udhaardaar / Informal Credit", "CREDIT", "V62CreditRegistrationActivity", "v62_relationships"),
        Module("CREDIT_BUREAU", "Informal Credit Intelligence / Bureau", "CREDIT", "V62BureauActivity", "v62_relationships"),
        Module("QR_KHATA", "QR Khata", "CREDIT", "ArthSaathiModulesActivity:QR", "qr_khata"),
        Module("ASSET_VAULT", "Asset Vault", "PROTECT", "V62AssetVaultActivity", "v62_assets"),
        Module("INSURANCE_PROTECT", "Insurance & Benefits Protect", "PROTECT", "V62InsuranceActivity", "v62_insurance"),
        Module("CHARGECHECK", "ChargeCheck", "CHECK", "V62ChargeCheckActivity", "v62_charge_checks"),
        Module("GROUP_KHATA", "Group Khata / Shared Expenses", "SHARED", "V62TTMMActivity", "v62_ttmm"),
        Module("WILL_LEGACY", "Will & Legacy Planner", "LEGACY", "ArthSaathiModulesActivity:WILL", "wills"),
        Module("LEGAL_ASSISTANCE", "Legal Assistance", "LEGACY", "ArthSaathiModulesActivity:LEGAL", "lawyers"),
        Module("AI_ADVISOR", "ArthSaathi AI Advisor", "INTELLIGENCE", "ArthSaathiModulesActivity:AI", "arthsaathi_preferences"),
        Module("MIS", "Management Information System", "INTELLIGENCE", "V62MISActivity", "v62_mis"),
        Module("RENTAL_LEASE", "Rental & Lease", "CREDIT", "V62RentalLeaseActivity", "v62_relationships"),
        Module("REVENUE", "Revenue / Charges", "PLATFORM", "V62ChargesActivity", "v62_revenue")
    )

    val requiredCrossCuttingRules = setOf(
        "REGISTER_CREDIT_IS_NEW_CREDIT_ONLY",
        "LOANS_UDHAAR_IS_EXISTING_ACCOUNT_REPOSITORY",
        "COUNTERPARTY_HISTORY_REQUIRES_CONSENT",
        "INFORMAL_CREDIT_FINAL_REGISTRATION_REQUIRES_OTP",
        "DIGITAL_DOCUMENTS_PRECEDE_FINAL_INFORMAL_REGISTRATION",
        "PROTECTED_ACTIONS_CREATE_AUDIT_EVENT",
        "RESPONSIVE_KEYBOARD_SAFE_UI",
        "NO_LEGACY_V5_PRIMARY_NAVIGATION",
        "SUGGESTIONS_NEVER_EXECUTE_MONEY_MOVEMENT"
    )

    fun verifyRegistry(): List<String> {
        val errors = mutableListOf<String>()
        if (modules.map { it.id }.size != modules.map { it.id }.toSet().size)
            errors += "Duplicate module id"
        if (modules.size != 13) errors += "Expected 13 consolidated top-level modules"
        if ("INFORMAL_CREDIT" !in modules.map { it.id } || "CREDIT_BUREAU" !in modules.map { it.id })
            errors += "Credit and Bureau modules are not both present"
        if ("MIS" !in modules.map { it.id } || "REVENUE" !in modules.map { it.id })
            errors += "MIS and Revenue modules are mandatory"
        modules.filter { it.entryPoint.startsWith("V5") || it.entryPoint.startsWith("V4") || it.entryPoint.startsWith("V3") }
            .forEach { errors += "Legacy primary entry point: ${it.id} -> ${it.entryPoint}" }
        return errors
    }
}
