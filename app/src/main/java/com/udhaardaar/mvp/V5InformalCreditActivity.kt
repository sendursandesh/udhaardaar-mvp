package com.udhaardaar.mvp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class V5InformalCreditActivity : AppCompatActivity() {
    private val repo by lazy { V5WorkflowRepository(this) }
    private val consent by lazy { V5OtpConsentService(this) }
    private val scanner = registerForActivityResult(ScanContract()) { result ->
        if (!result.contents.isNullOrBlank()) applyQr(result.contents)
    }
    private lateinit var name: EditText
    private lateinit var vendor: EditText
    private lateinit var invoice: EditText
    private lateinit var date: EditText
    private lateinit var principal: EditText
    private lateinit var roi: EditText
    private lateinit var start: EditText
    private lateinit var end: EditText
    private lateinit var type: Spinner
    private lateinit var direction: Spinner
    private lateinit var method: Spinner

    private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true)}
    private fun put(r:LinearLayout,v:android.view.View){r.addView(v,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)})}
    override fun onCreate(b:Bundle?){super.onCreate(b);show()}

    private fun show(){
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,20,24,24)}
        r.addView(TextView(this).apply{text="Udhaardaar V5 • Informal Credit";textSize=23f})
        r.addView(TextView(this).apply{text="Identify party → scan/import → terms → consent → register";textSize=13f})
        put(r,Button(this).apply{text="SCAN QR — AUTO-FILL CREDIT";setOnClickListener{scanner.launch(ScanOptions().apply{setDesiredBarcodeFormats(ScanOptions.QR_CODE);setPrompt("Scan credit / invoice QR code");setBeepEnabled(true);setOrientationLocked(false)})}})
        name=e("Borrower / party name or profile ID *"); vendor=e("Vendor / seller (auto-filled from QR when available)"); invoice=e("Invoice / bill number (auto-filled from QR when available)"); date=e("Invoice / transaction date (auto-filled from QR when available)")
        type=Spinner(this).apply{adapter=ArrayAdapter(this@V5InformalCreditActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Personal Credit","Business Credit","Trade Credit","Advance","Other"))}
        direction=Spinner(this).apply{adapter=ArrayAdapter(this@V5InformalCreditActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Credit Given","Credit Received"))}
        principal=e("Principal / invoice amount *"); roi=e("Annual ROI %"); method=Spinner(this).apply{adapter=ArrayAdapter(this@V5InformalCreditActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("EMI","Principal + Interest","Bullet / Full payment"))}; start=e("Start date YYYY-MM-DD *"); end=e("End date YYYY-MM-DD *")
        put(r,name);put(r,vendor);put(r,invoice);put(r,date);put(r,type);put(r,direction);put(r,principal);put(r,roi);put(r,method);put(r,start);put(r,end)
        put(r,Button(this).apply{text="SEND OTP CONSENT + REGISTER";setOnClickListener{registerAfterConsent()}})
        put(r,Button(this).apply{text="BACK";setOnClickListener{finish()}})
        setContentView(ScrollView(this).apply{addView(r)})
    }

    private fun applyQr(raw:String){
        try {
            val j=org.json.JSONObject(raw)
            name.setText(first(j,"vendor","vendor_name","seller","supplier","merchant","party"))
            vendor.setText(first(j,"vendor","vendor_name","seller","supplier","merchant","party"))
            invoice.setText(first(j,"invoice","invoice_no","invoice_number","bill_no","bill_number"))
            date.setText(first(j,"date","invoice_date","transaction_date"))
            principal.setText(first(j,"amount","total","total_amount","invoice_amount","grand_total"))
        } catch (_:Exception) {
            raw.split('&','\n',';').mapNotNull{p->p.split('=',':',limit=2).takeIf{x->x.size==2}?.let{x->x[0].trim().lowercase() to x[1].trim()}}.toMap().let{m->
                vendor.setText(m["vendor"]?:m["seller"]?:m["supplier"]?:""); name.setText(vendor.text); invoice.setText(m["invoice"]?:m["invoice_no"]?:""); date.setText(m["date"]?:m["invoice_date"]?:""); principal.setText(m["amount"]?:m["total"]?:"")
            }
        }
        Toast.makeText(this,"QR data captured. Review all fields before consent.",Toast.LENGTH_LONG).show()
    }
    private fun first(j:org.json.JSONObject,vararg keys:String):String=keys.firstNotNullOfOrNull{if(j.has(it)&&!j.isNull(it))j.optString(it).takeIf{v->v.isNotBlank()} else null}?:""

    private fun registerAfterConsent(){
        val p=principal.text.toString().trim().toDoubleOrNull()
        if(name.text.trim().length<2||p==null||p<=0||start.text.isBlank()||end.text.isBlank()){Toast.makeText(this,"Complete required fields",Toast.LENGTH_LONG).show();return}
        val id="CR-${System.currentTimeMillis()}"; val cid=consent.issue(id,"INFORMAL_CREDIT_BORROWER_CONSENT",name.text.toString()); val otp=readOtp(cid)
        val input=e("Enter 6-digit OTP")
        AlertDialog.Builder(this).setTitle("Counterparty consent").setMessage("Demo OTP: $otp\nProduction SMS gateway is required for live OTP delivery.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create().also{d->d.setOnShowListener{d.getButton(-1).setOnClickListener{if(consent.verify(cid,input.text.toString())){repo.saveCredit(id,name.text.toString(),type.selectedItem.toString(),direction.selectedItem.toString(),p,roi.text.toString().toDoubleOrNull()?:0.0,method.selectedItem.toString(),start.text.toString(),end.text.toString(),vendor.text.toString(),invoice.text.toString(),date.text.toString(),cid);repo.appendAudit(id,"CREDIT_REGISTERED_AFTER_CONSENT",name.text.toString(),"vendor=${vendor.text}; invoice=${invoice.text}; transactionDate=${date.text}; QR metadata preserved");Toast.makeText(this@V5InformalCreditActivity,"Consent verified; credit registered",Toast.LENGTH_LONG).show();finish()}else input.error="Incorrect OTP"}};d.show()}
    }
    private fun readOtp(id:String):String { val c=V5LocalStore(this).find("consents",id); return c?.optString("otp","") ?: "" }
}
