package com.udhaardaar.mvp

/** Canonical domain model for the independent ArthSaathi architecture. */
object ArthSaathiMasterModel {
    enum class CreditNature { PERSONAL_HAND_LOAN, TRADE_CREDIT, RENTAL_LEASE, FORMAL_LOAN, OTHER_RECEIVABLE_PAYABLE }
    enum class LendingMethod { CASH, UPI, NEFT, BANK_TRANSFER, OTHER }
    enum class RepaymentMethod { BULLET, PRINCIPAL_INTEREST, EMI, CUSTOM_SCHEDULE, RENTAL_PERIODIC, NONE }
    enum class AccountStatus { DRAFT, PENDING_CONSENT, ACTIVE, OVERDUE, CLOSED, DISPUTED }
    enum class ConsentStatus { NOT_REQUIRED, PENDING, VERIFIED, REJECTED, EXPIRED }
    enum class DocumentType { DPN, LOAN_AGREEMENT, LEASE_AGREEMENT, INVOICE, BANK_STATEMENT, SANCTION_LETTER, RECEIPT, IDENTITY, OTHER }

    data class Person(val id:String,val name:String,val mobile:String?,val email:String?,val addressId:String?,val role:String?)
    data class Address(val id:String,val pin:String?,val address:String?,val city:String?,val state:String?,val latitude:Double?,val longitude:Double?,val mapsRef:String?)
    data class Guarantor(val personId:String,val relationship:String?,val consent:ConsentStatus)
    data class CreditAccount(val id:String,val personId:String,val nature:CreditNature,val amount:Double,val roiPercent:Double?,val method:LendingMethod,val repayment:RepaymentMethod,val status:AccountStatus,val dueDate:String?,val guarantor:Guarantor?,val consent:ConsentStatus)
    data class Repayment(val id:String,val accountId:String,val amount:Double,val principal:Double,val interest:Double,val date:String,val consent:ConsentStatus)
    data class ScheduleItem(val id:String,val accountId:String,val sequence:Int,val dueDate:String,val emi:Double,val principal:Double,val interest:Double,val status:String)
    data class GroupExpense(val id:String,val groupId:String,val payerId:String,val total:Double,val purpose:String,val memberIds:List<String>)
    data class Asset(val id:String,val category:String,val description:String,val currentValue:Double,val ownerId:String?,val nomineeId:String?,val evidenceDocumentId:String?)
    data class Liability(val id:String,val category:String,val description:String,val outstanding:Double,val dueDate:String?,val lenderId:String?)
    data class Investment(val id:String,val category:String,val invested:Double,val currentValue:Double,val returnPercent:Double?,val risk:String?,val horizon:String?)
    data class Benefit(val id:String,val source:String,val description:String,val value:Double,val status:String,val evidenceDocumentId:String?)
    data class InsurancePolicy(val id:String,val provider:String,val cover:Double,val renewalDate:String?,val status:String)
    data class ChargeComparison(val id:String,val accountId:String,val sanctioned:Double,val actual:Double,val variance:Double)
    data class Claim(val id:String,val assetId:String?,val claimantId:String,val nomineeId:String?,val status:String,val action:String?)
    data class LegalCase(val id:String,val domain:String,val city:String?,val issue:String,val advocateId:String?,val status:String)
    data class Advocate(val id:String,val name:String,val domain:String,val city:String,val verified:Boolean,val contact:String?)
    data class Document(val id:String,val type:DocumentType,val title:String,val relatedId:String?,val uri:String?,val hash:String?,val createdAt:Long)
    data class RevenueTransaction(val id:String,val service:String,val amount:Double,val status:String,val gatewayRef:String?)
    data class ConsentRecord(val id:String,val action:String,val subjectId:String,val mobile:String?,val status:ConsentStatus,val timestamp:Long,val auditRef:String)
}