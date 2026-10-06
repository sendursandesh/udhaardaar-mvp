package com.udhaardaar.mvp

/** Canonical contracts: every feature must attach to one owner and one flow. */
object ArthSaathiArchitectureContracts {
    data class Contract(val module:String,val owns:Set<String>,val reads:Set<String>,val writes:Set<String>,val protectedActions:Set<String>,val outputs:Set<String>)
    val contracts = listOf(
        Contract("REGISTER_CREDIT",setOf("new credit registration"),setOf("people","documents","consent"),setOf("credit accounts"),setOf("register credit","create DPN"),setOf("new account")),
        Contract("LOANS_UDHAAR",setOf("credit account history"),setOf("credit accounts","people","documents"),setOf("account status"),setOf("view protected history"),setOf("account detail")),
        Contract("REPAYMENT",setOf("all repayments"),setOf("credit accounts","schedules","consent"),setOf("repayments","account balances","account status"),setOf("record repayment","close account"),setOf("updated balance","receipt")),
        Contract("QR_KHATA",setOf("trade-credit capture"),setOf("QR","invoice","people"),setOf("trade records","documents"),setOf("register trade credit"),setOf("trade account")),
        Contract("GROUP_KHATA",setOf("group expenses"),setOf("people","groups"),setOf("group records","member shares"),setOf("record group expense"),setOf("member obligations")),
        Contract("MIS",setOf("cross-module reporting"),setOf("all approved module data"),emptySet(),emptySet(),setOf("actual values","charts","portfolio","assets","benefits","refunds","liabilities")),
        Contract("SWITCH_ANALYSIS",setOf("portfolio comparison"),setOf("portfolio","market references","risk profile"),setOf("analysis records"),setOf("accept recommendation"),setOf("return comparison","opportunity cost","risk note")),
        Contract("ASSET_VAULT",setOf("assets"),setOf("documents","people"),setOf("assets","nominees"),setOf("change ownership","nomination"),setOf("asset position")),
        Contract("CLAIMS",setOf("claim assistance"),setOf("assets","nominees","documents","legal"),setOf("claims"),setOf("submit claim","change claimant"),setOf("claim status")),
        Contract("REVENUE",setOf("service charges and payments"),setOf("services","transactions"),setOf("charges","payments"),setOf("initiate payment","refund"),setOf("payment status","revenue report")),
        Contract("SECURITY_CONSENT",setOf("authorization and audit"),setOf("identity","consent requests"),setOf("consent records","audit events"),setOf("verify identity","approve protected action"),setOf("audit trail")),
        Contract("DOCUMENT_VAULT",setOf("evidence documents"),setOf("all module references"),setOf("documents"),setOf("share protected document"),setOf("document evidence"))
    )
    init { check(contracts.map{it.module}.distinct().size==contracts.size) }
    fun forModule(id:String)=contracts.firstOrNull{it.module==id}
}