package com.udhaardaar.mvp

/** Runtime guard for the non-negotiable consolidated architecture. */
object ArthSaathiArchitectureGuard {
    fun verify() {
        val r = ArthSaathiArchitectureRegistry
        check(r.canonicalModule("REGISTER_CREDIT")?.area == "RECORD")
        check(r.canonicalModule("LOANS_UDHAAR")?.area == "CREDIT")
        check(r.canonicalModule("REPAYMENT")?.area == "CREDIT")
        check(r.canonicalModule("MIS")?.area == "INTELLIGENCE")
        check(r.canonicalModule("GROUP_KHATA")?.title == "Group Khata / Group Expenses")
        check(r.modules.map { it.id }.distinct().size == r.modules.size)
        check(r.modules.map { it.route }.distinct().size == r.modules.size)
        check(ArthSaathiFlowRegistry.flows.any { it.id == "REGISTER_CREDIT" })
        check(ArthSaathiFlowRegistry.flows.any { it.id == "REPAYMENT" })
        check(ArthSaathiMisModel.metrics.any { it.id == "NET_POSITION" })
        check(ArthSaathiSecurityModel.policies.any { it.resource == "credit_history" && it.requiresAuthorization })
        check(ArthSaathiMasterModel.CreditNature.values().contains(ArthSaathiMasterModel.CreditNature.RENTAL_LEASE))
    }
}
