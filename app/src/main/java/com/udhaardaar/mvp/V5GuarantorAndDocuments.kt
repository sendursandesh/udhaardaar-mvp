package com.udhaardaar.mvp

/** Explicit guarantor + digital documentation workflow contract. */
object V5GuarantorAndDocuments {
    data class GuarantorProfile(val id:String,val name:String,val mobile:String,val address:String,val pan:String?=null,val aadhaar:String?=null,val photoUri:String?=null,val relationship:String?=null)
    data class CreditDocumentPacket(val creditId:String,val promissoryNote:V5DomainModels?=null,val guarantees:List<GuaranteeRecord> = emptyList(),val supportingDocuments:List<String> = emptyList(),val borrowerConsent:V5ConsentAndScore.ConsentRecord,val guarantorConsents:List<V5ConsentAndScore.ConsentRecord>)
    data class V5DomainModels(val generated:Boolean=true)
    data class GuaranteeRecord(val guarantorId:String,val documentId:String,val state:V5ConsentAndScore.ConsentState)

    fun generateDpnTemplate(creditId:String, borrower:String, lender:String, principal:Double, roi:Double, start:String, end:String):String = generateDpnTemplate(creditId,borrower,lender,principal,roi,"EMI",0.0,0,start,end,0.0,0.0)
    fun generateDpnTemplate(creditId:String, borrower:String, lender:String, principal:Double, roi:Double, method:String, installment:Double, installments:Int, start:String, end:String, interest:Double, totalPayable:Double):String = """
        DEMAND PROMISSORY NOTE — DIGITAL DRAFT
        Credit Reference: $creditId
        Borrower: $borrower
        Lender: $lender
        Principal: ₹${"%.2f".format(principal)}
        Interest/ROI: ${"%.2f".format(roi)}% p.a.
        Interest amount: ₹${"%.2f".format(interest)}
        Repayment method: $method
        Installment / maturity amount: ₹${"%.2f".format(installment)}
        Number of installments: $installments
        Period: $start to $end
        Total repayment: ₹${"%.2f".format(totalPayable)}
        This is a digital draft for review and consent; legal enforceability depends on applicable law and final legal review.
    """.trimIndent()

    fun generateGuaranteeTemplate(creditId:String, guarantor:GuarantorProfile, borrower:String, principal:Double):String = """
        GUARANTEE — DIGITAL DRAFT
        Credit Reference: $creditId
        Borrower: $borrower
        Guarantor: ${guarantor.name}
        Guarantor mobile: ${guarantor.mobile}
        Principal: ₹${"%.2f".format(principal)}
        The guarantor must review the underlying credit terms and digitally consent before the completed guarantee is archived.
        This is a digital draft for review; legal enforceability depends on applicable law and final legal review.
    """.trimIndent()
}
