package com.udhaardaar.mvp

/** Privacy-by-default permission policy for V5 vault and counterparty records. */
object V5AccessPolicy {
 enum class Permission { OWNER, COUNTERPARTY, GUARANTOR, TRUSTED_PERSON, CLAIMANT, LEGAL_PROFESSIONAL }
 fun canViewPrivateAsset(actor:Permission)=actor==Permission.OWNER||actor==Permission.TRUSTED_PERSON
 fun canPrepareClaim(actor:Permission)=actor==Permission.OWNER||actor==Permission.TRUSTED_PERSON||actor==Permission.CLAIMANT||actor==Permission.LEGAL_PROFESSIONAL
 fun canUpdateInformalRepayment(actor:Permission)=actor==Permission.OWNER||actor==Permission.COUNTERPARTY
 fun canUpdateFormalStatement(actor:Permission)=actor==Permission.OWNER
}
