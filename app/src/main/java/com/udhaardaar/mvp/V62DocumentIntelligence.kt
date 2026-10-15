package com.udhaardaar.mvp

import java.util.Locale

/** Shared review-first document intelligence contract for V6.2 modules. */
object V62DocumentIntelligence {
    data class Field(val name: String, val value: String, val critical: Boolean = false, val reason: String = "Review", val confidence: Double = 0.0)
    data class V62Extraction(val name: String, val value: String, val critical: Boolean = false, val reason: String = "Review", val confidence: Double = 0.0)
    data class Request(val documentType: String, val originalUri: String, val requestedFields: List<String>, val criticalTermCategories: List<String>)
    data class Result(val documentId: String, val extracted: List<V62Extraction>, val overallConfidence: Double, val reviewRequired: Boolean, val summary: String = "")

    val insuranceFields = listOf("Policy number","Insurer","Policy type","Policyholder","Insured person","Premium / frequency","Sum assured","Start date","End / maturity date","Next premium due","Nominee","Status","Exclusions / waiting period","Riders / benefits","Claim contact","Loan / surrender value")
    val assetFields = listOf("Asset name / description","Owner / account holder","Property address / description","Account number","Bank / branch","IFSC","Registration / deed number","Area","Current value ₹","Outstanding liability ₹","Nominee","Risk","Yield %","Idle / inactive","Evidence notes")
    val leaseFields = listOf("Lessor / Landlord","Lessee / Tenant","Property / premises description","Monthly rent","Security deposit","Lease start date","Lease end date","Rent due date","Escalation / revision","Lock-in period","Notice period","Maintenance responsibility","Utilities responsibility","Late-payment terms","Renewal terms","Termination terms","Special conditions")
    val invoiceFields = listOf("Invoice number","Invoice date","Supplier","Buyer","GSTIN","Items","Taxable value","Tax","Total","Due date","Payment terms","Purchase order")
    val criticalCategories = listOf("EXPIRY_OR_DUE_DATE","PAYMENT_OBLIGATION","INTEREST_OR_CHARGES","EXCLUSION","WAITING_PERIOD","CLAIM_CONDITION","NOTICE_OR_TERMINATION","LOCK_IN","ESCALATION","ENCUMBRANCE_OR_MORTGAGE","NOMINEE","OWNERSHIP","LIABILITY","PENALTY","RENEWAL")

    fun request(documentType: String, originalUri: String): Request = Request(documentType, originalUri, when (documentType) {
        "INSURANCE_POLICY" -> insuranceFields
        "PROPERTY_PAPER", "BANK_PASSBOOK" -> assetFields
        "LEASE_DEED", "RENT_AGREEMENT" -> leaseFields
        "INVOICE" -> invoiceFields
        else -> emptyList()
    }, criticalCategories)

    /** Deterministic local extraction proposal. It never silently writes data; screens must review fields. */
    fun analyse(documentType: String, text: String): List<Field> {
        if (text.isBlank()) return emptyList()
        val fields = when (documentType) {
            "INSURANCE_POLICY" -> insuranceFields
            "PROPERTY_PAPER", "BANK_PASSBOOK" -> assetFields
            "LEASE_DEED", "RENT_AGREEMENT" -> leaseFields
            "INVOICE", "CREDIT" -> invoiceFields
            else -> emptyList()
        }
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        return fields.mapNotNull { label ->
            val key = label.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), " ").trim()
            val match = lines.firstOrNull { line ->
                val l = line.lowercase(Locale.US)
                key.split(" ").filter { it.length > 2 }.count { l.contains(it) } >= minOf(2, key.split(" ").count { it.length > 2 })
            } ?: return@mapNotNull null
            Field(label, match.take(300), isCritical(label), "Extracted from document; verify", 0.55)
        }
    }

    private fun isCritical(label: String): Boolean {
        val x = label.lowercase(Locale.US)
        return listOf("due","expiry","end","exclusion","waiting","notice","lock-in","termination","penalty","liability","mortgage","encumbrance","nominee","ownership","escalation","interest","charge","renewal").any { x.contains(it) }
    }
}
