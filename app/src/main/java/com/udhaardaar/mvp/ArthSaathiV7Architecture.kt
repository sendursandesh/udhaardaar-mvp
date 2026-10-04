package com.udhaardaar.mvp

/**
 * ArthSaathi V7 — MASTER INFORMATION ARCHITECTURE
 *
 * This file is the navigation/data relationship contract.
 * UI screens must follow this ownership model; modules must not duplicate
 * the same business journey in multiple places.
 *
 * Primary navigation:
 * Home | Credit | Repay | Vault | More
 *
 * Business journeys are owned by exactly one primary destination.
 */
object ArthSaathiV7Architecture {

    enum class Primary(val label: String) {
        HOME("Home"),
        CREDIT("Credit"),
        REPAY("Repay"),
        VAULT("Vault"),
        MORE("More")
    }

    data class Destination(
        val id: String,
        val label: String,
        val owner: Primary,
        val parent: String? = null
    )

    val primary = listOf(
        Destination("HOME", "Home", Primary.HOME),
        Destination("CREDIT", "Credit", Primary.CREDIT),
        Destination("REPAY", "Repay", Primary.REPAY),
        Destination("VAULT", "Vault", Primary.VAULT),
        Destination("MORE", "More", Primary.MORE)
    )

    // Exactly one owner for each major journey.
    val journeys = listOf(
        Destination("CREDIT_CENTRE", "Loans & Udhaar", Primary.CREDIT),
        Destination("REPAYMENT_CENTRE", "Collect / Pay Dues", Primary.REPAY),
        Destination("ASSET_VAULT", "My Assets & Property", Primary.VAULT),
        Destination("INSURANCE", "Insurance & Protection", Primary.HOME),
        Destination("INVESTMENTS", "Investments & Returns", Primary.HOME),
        Destination("GROUP_KHATA", "Group Khata", Primary.HOME),
        Destination("WILL_INHERITANCE", "Will, Inheritance & Claims", Primary.HOME),
        Destination("LEGAL_CLAIMS", "Legal Help & Claims", Primary.HOME),
        Destination("MORE_SERVICES", "More Financial Tools", Primary.MORE)
    )

    val groupKhataFlow = listOf(
        "Create / Open Group",
        "Add Members",
        "Add Shared Expense",
        "Equal Split or Custom Split",
        "Record Who Paid",
        "Calculate Who Owes Whom",
        "Settlement Plan",
        "Settlement Confirmation",
        "Group History"
    )

    val creditFlow = listOf(
        "Person / Business",
        "Credit Type",
        "Credit Terms",
        "Lending / Receiving Method",
        "Repayment Terms",
        "Guarantor",
        "Documents",
        "Consent",
        "OTP Confirmation",
        "Register Credit",
        "Credit Detail"
    )

    val recordRelationships = mapOf(
        "CREDIT" to listOf("PERSON", "GUARANTOR", "REPAYMENT", "DOCUMENT", "CONSENT", "LIABILITY"),
        "REPAYMENT" to listOf("CREDIT", "PERSON", "MIS", "AUDIT"),
        "ASSET" to listOf("PERSON", "NOMINEE", "DOCUMENT", "INSURANCE", "LIABILITY", "WILL", "CLAIM"),
        "INSURANCE" to listOf("PERSON", "NOMINEE", "DOCUMENT", "ASSET", "CLAIM"),
        "INVESTMENT" to listOf("PORTFOLIO", "MARKET_VALUE", "MIS", "SWITCH_ANALYSIS"),
        "GROUP_EXPENSE" to listOf("GROUP", "MEMBER", "CONTRIBUTION", "BALANCE", "SETTLEMENT"),
        "WILL" to listOf("PERSON", "NOMINEE", "BENEFICIARY", "ASSET", "CLAIM"),
        "CLAIM" to listOf("ASSET", "INSURANCE", "DOCUMENT", "LEGAL_HELP")
    )

    // My Money Report / MIS is an intelligence layer, not a duplicate data-entry module.
    val misSources = listOf(
        "CREDIT", "REPAYMENT", "ASSETS", "LIABILITIES",
        "INVESTMENTS", "INSURANCE", "GROUP_KHATA", "BENEFITS"
    )

    // User-facing names; technical route IDs remain internal.
    val userLabels = mapOf(
        "GROUP_KHATA" to "Group Khata",
        "MIS" to "My Money Report",
        "QR_KHATA" to "QR Udhaar Khata",
        "CHARGECHECK" to "Check Loan / Bank Charges",
        "ADVOCATE" to "Find an Advocate",
        "NOMINEE" to "Nominees & Beneficiaries"
    )
}
