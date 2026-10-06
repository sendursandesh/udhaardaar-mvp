package com.udhaardaar.mvp

object ArthSaathiSecurityModel {
    enum class Access { OWNER, BORROWER, LENDER, GUARANTOR, NOMINEE, ADVOCATE, ADMIN, PUBLIC }
    enum class Sensitivity { PUBLIC, PRIVATE, PROTECTED, HIGHLY_PROTECTED }
    data class Policy(val resource:String,val sensitivity:Sensitivity,val allowed:Set<Access>,val requiresAuthorization:Boolean,val audit:Boolean)
    val policies=listOf(
        Policy("profile",Sensitivity.PRIVATE,setOf(Access.OWNER,Access.BORROWER),false,true),
        Policy("credit_history",Sensitivity.PROTECTED,setOf(Access.OWNER,Access.BORROWER,Access.LENDER),true,true),
        Policy("repayment",Sensitivity.PROTECTED,setOf(Access.OWNER,Access.BORROWER,Access.LENDER),true,true),
        Policy("guarantor",Sensitivity.PROTECTED,setOf(Access.OWNER,Access.BORROWER,Access.GUARANTOR,Access.LENDER),true,true),
        Policy("asset",Sensitivity.PROTECTED,setOf(Access.OWNER,Access.NOMINEE),true,true),
        Policy("claim",Sensitivity.HIGHLY_PROTECTED,setOf(Access.OWNER,Access.NOMINEE,Access.ADVOCATE),true,true),
        Policy("document",Sensitivity.PROTECTED,setOf(Access.OWNER,Access.BORROWER,Access.LENDER,Access.GUARANTOR,Access.NOMINEE,Access.ADVOCATE),true,true),
        Policy("mis",Sensitivity.PRIVATE,setOf(Access.OWNER),false,true)
    )
    init{check(policies.map{it.resource}.distinct().size==policies.size)}
}