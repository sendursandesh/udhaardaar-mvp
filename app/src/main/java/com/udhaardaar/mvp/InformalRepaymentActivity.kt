package com.udhaardaar.mvp

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InformalRepaymentActivity : AppCompatActivity() {
    private lateinit var db: V3DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContentView(R.layout.activity_informal_repayment); db=V3DatabaseHelper(this)
        val spinner=findViewById<Spinner>(R.id.spinnerInformalCredit); val ids=mutableListOf<Long>(); val names=mutableListOf<String>(); db.readableDatabase.rawQuery("SELECT id,credit_type,direction,principal_amount FROM credits WHERE status='ACTIVE' ORDER BY id DESC",null).use{c->while(c.moveToNext()){ids.add(c.getLong(0));names.add("#${c.getLong(0)} • ${c.getString(1)} • ${c.getString(2)} • ₹${String.format("%,.2f",c.getDouble(3))}")}}
        if(names.isEmpty())names.add("No active informal credit");spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,names)
        val date=findViewById<EditText>(R.id.etInformalDate);val cal=Calendar.getInstance();date.setText(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.time));date.setOnClickListener{DatePickerDialog(this,{_,y,m,d->cal.set(y,m,d);date.setText(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(cal.time))},cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()}
        findViewById<Spinner>(R.id.spinnerInformalMethod).adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("UPI","NEFT","RTGS","IMPS","Bank Transfer","Cheque","Cash","Other"))
        findViewById<Button>(R.id.btnSaveInformalRepayment).setOnClickListener{if(ids.isEmpty())return@setOnClickListener toast("Add an informal credit first");if(!findViewById<CheckBox>(R.id.cbRepaymentConsent).isChecked)return@setOnClickListener toast("Required consent/authorisation is mandatory for informal repayment changes");val amount=findViewById<EditText>(R.id.etInformalAmount).text.toString().toDoubleOrNull();if(amount==null||amount<=0)return@setOnClickListener toast("Enter a valid amount");val creditId=ids[spinner.selectedItemPosition];val actor=db.readableDatabase.rawQuery("SELECT party_id FROM credit_parties WHERE credit_id=? ORDER BY CASE role WHEN 'BORROWER' THEN 0 ELSE 1 END LIMIT 1",arrayOf(creditId.toString())).use{if(it.moveToFirst())it.getLong(0) else -1L};if(actor<=0)return@setOnClickListener toast("No authorised party is linked to this credit");val saved=db.addRepaymentAuthorized(creditId,actor,date.text.toString(),amount,amount,0.0,findViewById<Spinner>(R.id.spinnerInformalMethod).selectedItem.toString(),findViewById<EditText>(R.id.etInformalReference).text.toString().trim(),findViewById<EditText>(R.id.etInformalNotes).text.toString().trim(),SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(cal.time),true);if(saved<=0)return@setOnClickListener toast("Repayment was not saved — authorisation failed");toast("Informal repayment recorded");finish()}
    }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
