package com.udhaardaar.mvp

/** Canonical user journeys for the independent ArthSaathi architecture. */
object ArthSaathiFlowRegistry {
    data class Flow(val id:String,val title:String,val steps:List<String>,val protectedSteps:Set<Int>)
    val flows=listOf(
        Flow("REGISTER_CREDIT","Register new credit",listOf("identify party","select nature","capture credit-specific fields","select lending method","select repayment terms","capture guarantor","create digital document","request authorization","register account","open account in Loans & Udhaar"),setOf(7,8)),
        Flow("LOAN_DETAIL","Existing account detail",listOf("select account","verify authorization","show complete account","show documents","show repayment schedule","show repayment history","show outstanding and status"),setOf(1)),
        Flow("REPAYMENT","Repayment",listOf("select account","calculate payable","enter repayment","request authorization","record repayment","recalculate balance","update schedule","issue receipt","close account when settled"),setOf(3,4,8)),
        Flow("TRADE_QR","QR / trade credit",listOf("scan QR or invoice","extract invoice fields","confirm vendor date amount","identify counterparty","create trade record","capture evidence","authorize","register"),setOf(6,7)),
        Flow("GROUP_KHATA","Group Khata / Expenses",listOf("select group","record payer and total","add members","calculate shares","record contributions","show outstanding shares","settle contributions"),setOf(6)),
        Flow("ASSET_CLAIM","Asset to claim",listOf("select asset","verify ownership or nominee","collect evidence","identify claimant","start claim","legal assistance","track status","record outcome"),setOf(1,4,6)),
        Flow("SWITCH","Portfolio switch analysis",listOf("read portfolio","capture risk appetite","compare alternatives","compare return and cost","calculate opportunity cost","flag risk and eligibility","present explanation","user decision"),setOf(1,7)),
        Flow("CHARGECHECK","Sanctioned vs actual charges",listOf("capture sanction letter","extract sanctioned charges","capture bank statement","extract actual charges","normalize periods","compare","show variance","create support action"),setOf(1,3,7)),
        Flow("BENEFIT","Benefits and refunds",listOf("identify source","capture evidence","calculate value","track application","record approved value","record received value","show MIS impact"),setOf(1,4,5)),
        Flow("REVENUE","Service payment",listOf("select chargeable service","calculate charge","show terms","select payment gateway","receive payment result","record transaction","receipt","MIS revenue update"),setOf(2,4,5))
    )
    init{check(flows.map{it.id}.distinct().size==flows.size)}
}