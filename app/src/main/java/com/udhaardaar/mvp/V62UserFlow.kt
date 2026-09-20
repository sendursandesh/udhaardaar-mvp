package com.udhaardaar.mvp

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object V62UserFlow {
    fun pickDate(context:Context,target:android.widget.EditText){
        val now=Calendar.getInstance()
        DatePickerDialog(context,{_,y,m,day->target.setText(String.format(Locale.US,"%02d/%02d/%04d",day,m+1,y))},now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH)).show()
    }
    fun parseDate(value:String)=runCatching{SimpleDateFormat("dd/MM/yyyy",Locale.US).apply{isLenient=false}.parse(value)}.getOrNull()
    fun validPan(value:String)=value.uppercase(Locale.US).matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))
            fun validAadhaar(value:String)=value.matches(Regex("[2-9][0-9]{11}")) && value.toSet().size>1
    fun validGstin(value:String)=value.uppercase(Locale.US).matches(Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]"))
    fun validMobile(value:String)=value.matches(Regex("[6-9][0-9]{9}"))
    fun monthsBetween(start:java.util.Date,end:java.util.Date):Int{
        val a=Calendar.getInstance().apply{time=start};val b=Calendar.getInstance().apply{time=end}
        var n=(b.get(Calendar.YEAR)-a.get(Calendar.YEAR))*12+b.get(Calendar.MONTH)-a.get(Calendar.MONTH)
        if(b.get(Calendar.DAY_OF_MONTH)<a.get(Calendar.DAY_OF_MONTH))n--
        return n.coerceAtLeast(1)
    }
}

object V62PromissoryNote {
    data class Files(val word:File,val pdf:File)
    fun create(context:Context,borrower:String,lender:String,principal:Double,roi:Double,months:Int,emi:Double,start:String,end:String,period:String):Files{
        val dir=File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"ArthSaathi");dir.mkdirs()
        val safe=System.currentTimeMillis().toString()
        val title="DEMAND PROMISSORY NOTE"
        val body="$title\n\nI, $borrower, promise to pay to $lender the principal sum of ₹${String.format(Locale.US,"%.2f",principal)}, together with interest at ${String.format(Locale.US,"%.2f",roi)}% per annum, according to the agreed repayment terms.\n\nTenure: $months months\nNumber of instalments: $months\nInstalment amount: ₹${String.format(Locale.US,"%.2f",emi)}\nRepayment frequency: $period\nStart date: $start\nFinal payment date: $end\n\nI acknowledge the above debt and promise to pay the lender the principal and applicable interest in accordance with the registered schedule.\n\nBorrower signature: ____________________\nLender acknowledgement: ____________________\nExecution date: ____________________\nOTP consent reference: ____________________"
        val word=File(dir,"PromissoryNote_$safe.doc").apply{writeText("<html><body><pre style=\"font-family:serif;font-size:14pt\">${body.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\n","<br/>")}</pre></body></html>")}
        val pdf=File(dir,"PromissoryNote_$safe.pdf")
        val doc=PdfDocument();val page=doc.startPage(PdfDocument.PageInfo.Builder(595,842,1).create());val canvas=page.canvas;val paint=android.graphics.Paint().apply{color=android.graphics.Color.BLACK;textSize=14f};var y=55f
        body.split("\n").forEach{line->if(y>800f){doc.finishPage(page)}else{canvas.drawText(line.take(85),40f,y,paint);y+=20f}}
        doc.finishPage(page);FileOutputStream(pdf).use{doc.writeTo(it)};doc.close();return Files(word,pdf)
    }
}