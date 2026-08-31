package com.udhaardaar.mvp

import kotlin.math.abs

/** Deterministic sanction-vs-statement reconciliation. OCR adapters can feed these normalized entries later. */
object V5ChargeAudit {
    data class SanctionTerms(val roi: Double?, val processingFee: Double, val documentationFee: Double, val insurance: Double, val otherCharges: Double, val penalRate: Double?)
    data class StatementActuals(val interest: Double, val processingFee: Double, val documentationFee: Double, val insurance: Double, val otherCharges: Double, val penalCharges: Double, val refunds: Double)
    data class Finding(val item: String, val sanctioned: Double?, val actual: Double?, val variance: Double?, val status: String, val evidence: String? = null)
    data class Report(val findings: List<Finding>, val totalSanctionedFees: Double, val totalActualFees: Double, val netVariance: Double)

    fun reconcile(s: SanctionTerms, a: StatementActuals): Report {
        val rows = listOf(
            Finding("Processing fee", s.processingFee, a.processingFee, a.processingFee-s.processingFee, status(a.processingFee,s.processingFee)),
            Finding("Documentation fee", s.documentationFee, a.documentationFee, a.documentationFee-s.documentationFee, status(a.documentationFee,s.documentationFee)),
            Finding("Insurance", s.insurance, a.insurance, a.insurance-s.insurance, status(a.insurance,s.insurance)),
            Finding("Other charges", s.otherCharges, a.otherCharges, a.otherCharges-s.otherCharges, status(a.otherCharges,s.otherCharges)),
            Finding("Penal charges", null, a.penalCharges, null, "REVIEW REQUIRED"),
            Finding("Refunds / reversals", null, a.refunds, null, "REFUND IDENTIFIED")
        )
        val sanctioned = s.processingFee+s.documentationFee+s.insurance+s.otherCharges
        val actual = a.processingFee+a.documentationFee+a.insurance+a.otherCharges+a.penalCharges-a.refunds
        return Report(rows, sanctioned, actual, actual-sanctioned)
    }
    private fun status(actual:Double,sanctioned:Double)=when{abs(actual-sanctioned)<0.01->"MATCH";actual>sanctioned->"HIGHER THAN SANCTIONED";else->"LOWER / DIFFERENT"}
}
