package com.udhaardaar.mvp

/** Service boundary for the formal-loan sanction-vs-statement audit. */
class V5ChargeAuditService {
    fun audit(sanction: V5ChargeAudit.SanctionTerms, statement: V5ChargeAudit.StatementActuals): V5ChargeAudit.Report = V5ChargeAudit.reconcile(sanction, statement)
    fun summary(report: V5ChargeAudit.Report): String = buildString {
        append("Sanctioned fees: ₹${"%.2f".format(report.totalSanctionedFees)}\n")
        append("Actual net fees/charges: ₹${"%.2f".format(report.totalActualFees)}\n")
        append("Variance: ₹${"%.2f".format(report.netVariance)}\n\n")
        report.findings.forEach { append("${it.item}: ${it.status}; variance=${it.variance ?: "N/A"}\n") }
        append("\nFindings require document review and are not by themselves a legal determination.")
    }
}
