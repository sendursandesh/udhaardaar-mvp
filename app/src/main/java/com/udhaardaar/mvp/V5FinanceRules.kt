package com.udhaardaar.mvp

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow

object V5FinanceRules {
 data class CreditPlan(val principal: Double,val totalPayable: Double,val totalInterest: Double,val emi: Double)
 fun calculateCreditPlan(principal: Double,roiPercent: Double,periodicity: String,startDate: String,endDate: String,repaymentMethod: String): CreditPlan { require(principal>0.0){"Principal must be greater than zero"}; require(roiPercent>=0.0){"ROI cannot be negative"}; require(validDate(startDate)&&validDate(endDate)){"Dates must be in yyyy-MM-dd format"}; require(endDateOnOrAfterStart(startDate,endDate)){"End date cannot be before start date"}; val periods=monthsBetween(startDate,endDate).coerceAtLeast(1); val method=repaymentMethod.trim().lowercase(Locale.ROOT); val monthlyRate=roiPercent/100.0/12.0; val totalPayable=if(method.contains("principal")&&method.contains("interest")) principal+principal*(roiPercent/100.0)*(periods/12.0) else if(monthlyRate==0.0) principal else { val factor=(1+monthlyRate).pow(periods); principal*monthlyRate*factor/(factor-1)*periods }; return CreditPlan(principal,totalPayable,(totalPayable-principal).coerceAtLeast(0.0),totalPayable/periods) }
 fun validDate(value:String):Boolean { if(!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(value)) return false; val f=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply{isLenient=false}; val p=ParsePosition(0); return f.parse(value,p)!=null&&p.index==value.length }
 fun endDateOnOrAfterStart(startDate:String,endDate:String):Boolean { val s=parseDate(startDate)?:return false; val e=parseDate(endDate)?:return false; return !e.before(s) }
 fun monthsBetween(startDate:String,endDate:String):Int { val s=parseDate(startDate)?:return 0; val e=parseDate(endDate)?:return 0; var r=(e.get(Calendar.YEAR)-s.get(Calendar.YEAR))*12+e.get(Calendar.MONTH)-s.get(Calendar.MONTH); if(e.get(Calendar.DAY_OF_MONTH)<s.get(Calendar.DAY_OF_MONTH)) r--; return r.coerceAtLeast(0) }
 private fun parseDate(value:String):java.util.Date? { if(!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(value)) return null; val f=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply{isLenient=false}; val p=ParsePosition(0); return f.parse(value,p)?.takeIf{p.index==value.length} }
}
