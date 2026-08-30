package com.udhaardaar.mvp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FormalLoanRepaymentActivity : AppCompatActivity() {
    private lateinit var db: FormalLoanAuditDb
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContentView(R.layout.activity_formal_repayment); db=FormalLoanAuditDb(this)
        val spinner=findViewById<Spinner>(R.id.spinnerFormalLoan); val names=mutableListOf<String>(); val ids=mutableListOf<Long>(); db.loans().use{c->while(c.moveToNext()){ids.add(c.getLong(0));names.add("${c.getString(1)} • ${c.getString(2)?:("Loan #"+c.getLong(0))}")}}
        if(names.isEmpty()) names.add("No formal loans recorded"); spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,names)
        val date=findViewById<EditText>(R.id.etFormalDate); val cal=Calendar.getInstance(); date.setText(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.time)); date.setOnClickListener{DatePickerDialog(this,{_,y,m,d->cal.set(y,m,d);date.setText(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.time))},cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()}
        findViewById<Spinner>(R.id.spinnerFormalMethod).adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("UPI","NEFT","RTGS","IMPS","Bank Transfer","Cheque","Cash","Other"))
        findViewById<Button>(R.id.btnSaveFormalRepayment).setOnClickListener{if(ids.isEmpty())return@setOnClickListener toast("Add a formal loan first");val amount=findViewById<EditText>(R.id.etFormalAmount).text.toString().toDoubleOrNull();if(amount==null||amount<=0)return@setOnClickListener toast("Enter a valid payment amount");val v=android.content.ContentValues().apply{put("loan_id",ids[spinner.selectedItemPosition]);put("repayment_date",date.text.toString());put("amount",amount);put("payment_mode",findViewById<Spinner>(R.id.spinnerFormalMethod).selectedItem.toString());put("reference_number",findViewById<EditText>(R.id.etFormalReference).text.toString().trim());put("notes",findViewById<EditText>(R.id.etFormalNotes).text.toString().trim());put("created_at",FormalLoanAuditDb.now())};db.addRepayment(v);toast("Formal repayment saved — no counterparty consent required");finish()}
    }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
