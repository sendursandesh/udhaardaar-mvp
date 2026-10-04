package com.udhaardaar.mvp

/**
 * ArthSaathi V7 Option 2 — canonical product/navigation contract.
 *
 * This is the single user-facing taxonomy for the V7 master build.
 * Technical/internal names (MIS, TTMM, QR_KHATA, GROW, etc.) must not be
 * exposed as unexplained primary navigation labels.
 */
object ArthSaathiV7MasterVision {
    data class Tile(
        val title: String,
        val subtitle: String,
        val icon: String,
        val route: String
    )

    val dashboard: List<Tile> = listOf(
        Tile("Loans & Udhaar", "Give, receive & track credit", "₹", "CREDIT"),
        Tile("Collect / Pay Dues", "See dues & record payments", "↻", "REPAYMENT"),
        Tile("My Assets & Property", "Property, gold, deposits & more", "▣", "ASSETS"),
        Tile("Insurance & Protection", "Policies, renewals & claims", "◆", "PROTECT"),
        Tile("Investments & Returns", "Invest, compare & track growth", "▥", "GROW"),
        Tile("Group Expenses", "Share costs with friends & family", "👥", "TTMM"),
        Tile("Will, Inheritance & Claims", "Plan and claim family assets", "♜", "WILL"),
        Tile("Legal Help & Claims", "Find legal help and advocates", "⚖", "LEGAL"),
        Tile("More Financial Tools", "Khata, ChargeCheck & other tools", "•••", "MORE")
    )

    val creditTypes = listOf(
        "Personal Loan / Hand Loan",
        "Trade Credit / Udhaar",
        "Rental / Lease"
    )

    val moreSections: List<Pair<String, List<Tile>>> = listOf(
        "Money & Credit" to listOf(
            Tile("Shop Credit / QR Khata", "Scan and record merchant credit", "▦", "QR_KHATA"),
            Tile("My Loans & Dues", "Loans, liabilities & outstanding", "LI", "LIABILITIES"),
            Tile("Credit Reliability", "Consent-based reliability view", "◈", "SCORE")
        ),
        "Protect & Claim" to listOf(
            Tile("My Documents", "Important papers & evidence", "▣", "DOCUMENTS"),
            Tile("Government Benefits", "Benefits, refunds & money received", "BE", "BENEFITS")
        ),
        "Family" to listOf(
            Tile("My Profile & Family", "People, contacts & access", "●", "PEOPLE"),
            Tile("Nominees & Beneficiaries", "Who can receive your assets", "○", "NOMINEE"),
            Tile("Will & Inheritance", "Plan family assets & claims", "♜", "WILL")
        ),
        "Money Checks & Services" to listOf(
            Tile("Check Loan / Bank Charges", "Compare promised vs actual", "CC", "CHARGECHECK"),
            Tile("Payments & Charges", "Service charges & payments", "💳", "REVENUE")
        ),
        "Security" to listOf(
            Tile("Privacy & Consent", "Control who can access records", "✓", "SECURITY")
        )
    )

    fun creditFlow(type: String): List<String> = when {
        type == "Rental / Lease" -> listOf(
            "Choose person / business",
            "Enter rent, deposit and lease term",
            "Scan or upload lease deed / rent agreement",
            "Read document and review extracted terms",
            "Confirm dates, frequency and responsibilities",
            "Consent / sign and register",
            "Track rental dues and documents"
        )
        type == "Trade Credit / Udhaar" -> listOf(
            "Choose vendor / borrower",
            "Scan invoice or QR where available",
            "Review amount and transaction date",
            "Set repayment terms",
            "Add guarantor if required",
            "Consent / document",
            "Register and track dues"
        )
        else -> listOf(
            "Choose borrower / lender",
            "Enter credit amount and method",
            "Set interest and repayment terms",
            "Add guarantor if required",
            "Create digital credit document",
            "OTP consent / confirmation",
            "Register and track repayment"
        )
    }
}
