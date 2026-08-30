package com.udhaardaar.mvp

/** Orchestrates V5 domain transitions; screens should call these operations instead of bypassing rules. */
class V5IntegratedFlows(private val repo: V5WorkflowRepository) {
    data class CreditDraft(val id:String,val borrowerId:String,val direction:String,val type:String,val principal:Double,val roi:Double,val start:String,val end:String,val repaymentMethod:String,val guarantorIds:List<String> = emptyList())

    fun borrowerScoreConsent(creditId:String, consentId:String):Boolean {
        val ok=repo.markBorrowerConsent(creditId,consentId)
        if(ok) repo.appendAudit(creditId,"BORROWER_SCORE_CONSENT_OTP_VERIFIED",consentId,"Score may now be displayed")
        return ok
    }

    fun recordInformalRepaymentRequest(creditId:String, initiatedBy:String, amount:Double, date:String, method:String, reference:String, evidenceId:String?=null):String {
        val id=V5RepaymentRepository(repoContext(repo)).request(creditId,initiatedBy,amount,date,method,reference,evidenceId)
        repo.appendAudit(creditId,"REPAYMENT_REQUESTED",initiatedBy,"request=$id;amount=$amount")
        return id
    }

    /** Repository context is intentionally supplied by the application service in the real UI layer. */
    private fun repoContext(r:V5WorkflowRepository):android.content.Context {
        val f=r.javaClass.getDeclaredField("store"); f.isAccessible=true
        val store=f.get(r) as V5LocalStore
        val pf=store.javaClass.getDeclaredField("p"); pf.isAccessible=true
        val prefs=pf.get(store) as android.content.SharedPreferences
        return prefs.javaClass.getDeclaredField("this$0").let { throw IllegalStateException("Application service must inject Context") }
    }
}
