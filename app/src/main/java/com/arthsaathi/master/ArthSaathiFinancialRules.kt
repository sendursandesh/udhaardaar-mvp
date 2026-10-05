package com.arthsaathi.master

import kotlin.math.pow

object ArthSaathiFinancialRules {
    data class Plan(val emi:Double,val total:Double,val interest:Double)
    fun calculate(principal:Double,annualRate:Double,months:Int,method:String):Plan {
        require(principal>0 && months>0 && annualRate>=0)
        val m=annualRate/1200.0
        return if(method=="EMI" && m>0) {
            val f=(1+m).pow(months); val emi=principal*m*f/(f-1); Plan(emi,emi*months,emi*months-principal)
        } else if(method=="EMI") Plan(principal/months.toDouble(),principal,0.0)
        else { val total=principal + principal*annualRate*months/1200.0; Plan(total/months,total,total-principal) }
    }
}
