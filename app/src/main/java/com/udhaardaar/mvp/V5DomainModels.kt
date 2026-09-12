package com.udhaardaar.mvp

import java.time.Instant

data class V5Profile(val id:String,val type:String,val name:String,val mobile:String,val pan:String?=null,val aadhaar:String?=null,val gstin:String?=null,val photoUri:String?=null,val city:String?=null,val state:String?=null,val pin:String?=null)
data class V5Credit(val id:String,val profileId:String,val direction:String,val creditType:String,val principal:Double,val roiPercent:Double,val repaymentMethod:String,val startDate:String,val endDate:String,val consentState:String,val documentIds:List<String> = emptyList())
data class V5Rental(val id:String,val tenantProfileId:String,val property:String,val landlord:String,val monthlyRent:Double,val deposit:Double,val startDate:String,val endDate:String,val escalationPercent:Double=0.0,val noticeDays:Int=0,val documentId:String?=null)
data class V5Document(val id:String,val type:String,val uri:String,val sha256:String?=null,val createdAt:Instant=Instant.now(),val version:Int=1)

data class V5Asset(
    val id:String,
    val ownerProfileId:String,
    val category:String,
    val title:String,
    val description:String,
    val estimatedValue:Double?=null,
    val proofDocumentIds:List<String> = emptyList(),
    val nomineeProfileId:String?=null,
    val assetSubtype:String="",
    val institutionOrCounterparty:String="",
    val accountOrReference:String="",
    val acquisitionDate:String="",
    val maturityDate:String="",
    val location:String="",
    val ownership:String="Self",
    val encumbrance:String="None",
    val outstandingLiability:Double=0.0,
    val policyOrCertificate:String="",
    val notes:String=""
)
data class V5Claim(val id:String,val assetId:String,val claimantProfileId:String,val relationship:String,val status:String,val requiredDocumentIds:List<String> = emptyList(),val legalProfessionalId:String?=null)
data class V5ChargeComparison(val sanctionDocumentId:String,val statementDocumentId:String,val sanctionedRoi:Double?,val actualRoi:Double?,val sanctionedFees:Double,val actualFees:Double,val variance:Double,val findings:List<String>)

object V5Validation {
    private val panRegex=Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    private val aadhaarRegex=Regex("^[2-9][0-9]{11}$")
    private val gstinRegex=Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
    private val mobileRegex=Regex("^[6-9][0-9]{9}$")
    private val pinRegex=Regex("^[1-9][0-9]{5}$")

    fun pan(value:String):Boolean {
        val v=value.trim().uppercase()
        if(v.isEmpty()) return true
        if(!panRegex.matches(v)) return false
        // PAN positions 4 and 5 have defined semantic character classes.
        return v[3] in "PCHFATBLJG" && v[4].isLetter()
    }

    fun aadhaar(value:String):Boolean {
        val v=value.filterNot{it.isWhitespace()}
        if(v.isEmpty()) return true
        if(!aadhaarRegex.matches(v)) return false
        // Aadhaar uses the Verhoeff checksum; this rejects arbitrary 12-digit numbers.
        return verhoeffValid(v)
    }

    fun gstin(value:String):Boolean {
        val v=value.trim().uppercase()
        if(v.isEmpty()) return true
        if(!gstinRegex.matches(v)) return false
        val stateCode=v.substring(0,2).toIntOrNull() ?: return false
        // Valid GSTIN state/territory codes include 01-38; 97 is the special territory code.
        if(stateCode !in 1..38 && stateCode != 97) return false
        return gstChecksumValid(v)
    }

    fun mobile(value:String)=mobileRegex.matches(value.trim())
    fun pin(value:String)=pinRegex.matches(value.trim())

    private fun verhoeffValid(value:String):Boolean {
        val d=arrayOf(
            intArrayOf(0,1,2,3,4,5,6,7,8,9),
            intArrayOf(1,2,3,4,0,6,7,8,9,5),
            intArrayOf(2,3,4,0,1,7,8,9,5,6),
            intArrayOf(3,4,0,1,2,8,9,5,6,7),
            intArrayOf(4,0,1,2,3,9,5,6,7,8),
            intArrayOf(5,9,8,7,6,0,4,3,2,1),
            intArrayOf(6,5,9,8,7,1,0,4,3,2),
            intArrayOf(7,6,5,9,8,2,1,0,4,3),
            intArrayOf(8,7,6,5,9,3,2,1,0,4),
            intArrayOf(9,8,7,6,5,4,3,2,1,0)
        )
        val p=arrayOf(
            intArrayOf(0,1,2,3,4,5,6,7,8,9),
            intArrayOf(1,5,7,6,2,8,3,0,9,4),
            intArrayOf(5,8,0,3,7,9,6,1,4,2),
            intArrayOf(8,9,1,6,0,4,3,5,2,7),
            intArrayOf(9,4,5,3,1,2,6,8,7,0),
            intArrayOf(4,2,8,6,5,7,3,9,0,1),
            intArrayOf(2,7,9,3,8,0,6,4,1,5),
            intArrayOf(7,0,4,6,9,1,3,2,5,8)
        )
        val inv=intArrayOf(0,4,3,2,1,5,6,7,8,9)
        var c=0
        value.reversed().forEachIndexed{index,ch->
            val digit=ch-'0'
            c=d[c][p[index%8][digit]]
        }
        return inv[c]==0
    }

    private fun gstChecksumValid(value:String):Boolean {
        val chars="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var sum=0
        for(i in 0 until 14){
            val n=chars.indexOf(value[i])
            if(n<0) return false
            val product=n*if(i%2==0) 1 else 2
            sum += product/36 + product%36
        }
        val checkIndex=(36-(sum%36))%36
        return value[14]==chars[checkIndex]
    }
}
