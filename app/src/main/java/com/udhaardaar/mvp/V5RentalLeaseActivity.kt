package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/** Separate rental/lease workflow. Agreement is captured before the rental record is completed. */
class V5RentalLeaseActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val repo by lazy { V5WorkflowRepository(this) }
    private var agreementUri: Uri? = null
    private lateinit var tenant: EditText
    private lateinit var landlord: EditText
    private lateinit var rent: EditText
    private lateinit var start: EditText
    private lateinit var period: EditText
    private lateinit var status: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(22,18,22,30)}
        fun add(v:android.view.View,h:Int=56){r.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)})}
        add(TextView(this).apply{text="UDHAARDAAR V5 • Rental / Lease";textSize=23f},62)
        add(TextView(this).apply{text="Separate rental module • agreement scan/import → review → tenant/landlord consent → rental record";textSize=13f},70)
        tenant=field("Tenant / lessee name or profile ID *");landlord=field("Landlord / lessor name or profile ID *");rent=field("Monthly rent / lease amount *");start=field("Lease start date *");period=field("Lease period in months *").apply{setText("12")}
        add(tenant);add(landlord);add(rent);add(start);add(period)
        status=TextView(this).apply{text="No rent agreement captured yet.";textSize=13f};add(status,70)
        add(Button(this).apply{text="SCAN / IMPORT RENT AGREEMENT";setOnClickListener{pickAgreement()}})
        add(Button(this).apply{text="REVIEW AGREEMENT + REGISTER RENTAL";setOnClickListener{register()}})
        add(Button(this).apply{text="BACK";setOnClickListener{finish()}})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }

    private fun field(h:String)=EditText(this).apply{hint=h;setSingleLine(true);setPadding(14,10,14,10)}

    private fun pickAgreement(){
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/pdf";putExtra(Intent.EXTRA_MIME_TYPES,arrayOf("application/pdf","image/*"))},7001)
    }

    @Deprecated("Activity result compatibility")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==7001&&resultCode==RESULT_OK){agreementUri=data?.data;status.text="Agreement captured: ${agreementUri?.lastPathSegment ?: "document"}. Review required before registration."}}

    private fun register(){
        val t=tenant.text.toString().trim();val l=landlord.text.toString().trim();val amount=rent.text.toString().trim().toDoubleOrNull();val months=period.text.toString().trim().toIntOrNull()?:0
        if(t.length<2||l.length<2||amount==null||amount<=0||months !in 1..240){Toast.makeText(this,"Complete valid tenant, landlord, rent and lease period",Toast.LENGTH_LONG).show();return}
        if(agreementUri==null){Toast.makeText(this,"Rent agreement must be scanned/imported before registration",Toast.LENGTH_LONG).show();return}
        val id="RENT-${System.currentTimeMillis()}";val now=System.currentTimeMillis()
        store.add("documents",JSONObject().apply{put("id","RENT-DOC-$now");put("type","RENT_AGREEMENT");put("status","REVIEW_PENDING");put("version",1);put("createdAt",now);put("uri",agreementUri.toString());put("tenant",t);put("landlord",l)})
        AlertDialog.Builder(this).setTitle("Rent agreement review checkpoint").setMessage("Agreement captured and timestamped. In production, OCR/extraction and legal document review must be completed here before consent.\n\nTenant: $t\nLandlord: $l\nRent: ₹$amount\nPeriod: $months months\n\nProceed to create a consent request?").setNegativeButton("CANCEL",null).setPositiveButton("REQUEST CONSENT"){_,_->requestConsent(id,t,l,amount,months)}.show()
    }

    private fun requestConsent(docId:String,t:String,l:String,amount:Double,months:Int){
        val consent=V5OtpConsentService(this);val cid=consent.issue(docId,"RENTAL_LEASE_CONSENT","$t / $l");val otp=store.find("consents",cid)?.optString("otp","")?:"";val input=field("Enter 6-digit rental consent OTP")
        AlertDialog.Builder(this).setTitle("Rental / lease consent").setMessage("Demo OTP: $otp\nProduction SMS gateway is required for live OTP delivery.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create().also{d->d.setOnShowListener{d.getButton(-1).setOnClickListener{if(consent.verify(cid,input.text.toString())){store.find("documents",docId)?.let{x->x.put("status","COMPLETED");x.put("consentedAt",System.currentTimeMillis());store.replace("documents",x)};repo.appendAudit(docId,"RENTAL_AGREEMENT_CONSENTED",t,"landlord=$l; rent=$amount; months=$months; timestamp=${System.currentTimeMillis()}");Toast.makeText(this,"Rental agreement consented and recorded",Toast.LENGTH_LONG).show();d.dismiss()}else input.error="Incorrect OTP"}};d.show()}
    }
}
