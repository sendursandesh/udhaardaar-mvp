package com.udhaardaar.mvp

/** User-facing navigation projection of the consolidated master architecture. */
object ArthSaathiV7MasterVision {
    data class Tile(val title:String,val subtitle:String,val icon:String,val route:String)

    val dashboard = listOf(
        Tile("Register Credit","Create a new credit relationship","＋","REGISTER_CREDIT"),
        Tile("Loans & Udhaar","Existing active & closed accounts","₹","CREDIT"),
        Tile("Collect / Pay Dues","Schedules, payments & closure","↻","REPAYMENT"),
        Tile("My Assets & Property","Financial and non-financial assets","▣","ASSETS"),
        Tile("Investments & Returns","Portfolio, returns & switch analysis","▥","GROW"),
        Tile("Insurance & Protection","Policies, renewals & claims","◆","PROTECT"),
        Tile("Group Khata","Shared expenses & settlement","👥","GROUP_KHATA"),
        Tile("Will, Inheritance & Claims","Legacy and asset claims","♜","WILL"),
        Tile("Legal Help & Advocates","Legal support and advocate directory","⚖","LEGAL"),
        Tile("More Services","Documents, MIS, benefits, ChargeCheck & payments","•••","MORE")
    )

    val creditTypes = ArthSaathiArchitectureRegistry.creditNatureOptions

    val moreSections = listOf(
        "People & Records" to listOf(
            Tile("My Profile & Family","People, relationships & access","●","PEOPLE"),
            Tile("My Documents","Agreements, evidence & OCR","▣","DOCUMENTS"),
            Tile("QR / Trade Khata","Scan and record trade credit","▦","QR_KHATA"),
            Tile("Liability Vault","Loans and obligations","LI","LIABILITIES")
        ),
        "Money Intelligence" to listOf(
            Tile("My Money Report / MIS","Numbers, charts & value generated","▤","MIS"),
            Tile("Should I Switch?","Return, opportunity cost & risk","↔","OPPORTUNITY"),
            Tile("What-If Calculator","Future scenarios","◌","SCENARIO"),
            Tile("Market Values","Recorded market/reference values","⌁","MARKET")
        ),
        "Protection & Services" to listOf(
            Tile("Benefits & Refunds","Money received and value generated","BE","BENEFITS"),
            Tile("Check Loan / Bank Charges","Sanctioned vs actual","CC","CHARGECHECK"),
            Tile("Payments & Charges","Revenue, invoices and payment records","💳","REVENUE"),
            Tile("Privacy & Consent","Authorization, OTP and audit","✓","SECURITY")
        )
    )

    val creditCentre = listOf(
        "Loans & Udhaar","Register New Credit","Existing Credit Accounts",
        "Personal / Hand Loan","Trade Credit / Udhaar","Rental / Lease",
        "Formal Loan / Bank Credit","Active Accounts","Closed Accounts","Credit Account Details"
    )

    fun creditFlow(type:String):List<String> =
        if(type=="Rental / Lease") listOf(
            "Choose person / business","Enter rent, deposit and lease term",
            "Scan or upload lease deed / rent agreement","Review extracted terms",
            "Confirm dates, frequency and responsibilities","Consent / sign and register",
            "Track rental dues and documents"
        ) else if(type=="Trade Credit / Udhaar") listOf(
            "Choose vendor / borrower","Scan invoice or QR where available",
            "Review amount and transaction date","Set repayment terms",
            "Add guarantor if required","Consent / digital document",
            "Register and track dues"
        ) else listOf(
            "Choose borrower / lender","Enter credit amount and method",
            "Set interest and repayment terms","Add guarantor if required",
            "Create digital credit document","OTP consent / confirmation",
            "Register and track repayment"
        )
}
