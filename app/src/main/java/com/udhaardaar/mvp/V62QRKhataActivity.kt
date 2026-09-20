package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

/** Dedicated V6.2 QR Udhaar Khata. It is no longer routed through the legacy Financial Centre page. */
class V62QRKhataActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy{V5LocalStore(this)}
    private val d get()=resources.displayMetrics.density
    private lateinit var root:LinearLayout
    private lateinit var party:EditText
    private lateinit var amount:EditText
    private lateinit var note:EditText
    private lateinit var direction:Spinner
    private fun add(v:android.view.View,g:Int=7)=ArthSaathiV62Design.add(root,v,g)
    private fun input(h:String)=ArthSaathiV62Design.input(this,h)
    override fun onCreate(b:Bundle?){super.onCreate(b);render()}
    private fun render(){
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding((16*d).toInt(),(8*d).toInt(),(16*d).toInt(),(28*d).toInt());setBackgroundColor(ArthSaathiV62Design.BG)}
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
        add(ArthSaathiV62Design.title(this,"QR Udhaar Khata","Scan • Identify • Consent • Record"),2)
        add(ArthSaathiV62Design.text(this,"A direct V6.2 ledger. QR data is only a draft; private history and ledger mutation require identified-party consent.",11f,ArthSaathiV62Design.MUTED),8)
        party=input("Counterparty name / mobile / profile ID *")
        amount=input("Amount ₹ *")
        direction=Spinner(this).apply{adapter=ArrayAdapter(this@V62QRKhataActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("CREDIT / UDHAAR GIVEN","REPAYMENT RECEIVED"))}
        note=input("Invoice / items / note")
        add(party,5);add(amount,5);add(direction,5);add(note,5)
        add(ArthSaathiV62Design.button(this,"SCAN QR / IDENTIFY PARTY",ArthSaathiV62Design.TEAL){startActivityForResult(Intent(this,QrCreditScannerActivity::class.java),700)},8)
        add(ArthSaathiV62Design.button(this,"RECORD ENTRY — CONSENT + OTP",ArthSaathiV62Design.GREEN){record()},8)
        add(ArthSaathiV62Design.button(this,"OPEN QR KHATA HISTORY",ArthSaathiV62Design.BLUE){history()},5)
        add(ArthSaathiV62Design.button(this,"BACK",ArthSaathiV62Design.NAVY){finish()},5)
    }
    private fun record(){
        val a=amount.text.toString().replace(",","").toDoubleOrNull()
        if(party.text.isBlank()||a==null||a<=0){Toast.makeText(this,"Identify the party and enter a valid amount.",Toast.LENGTH_LONG).show();return}
        val cp=V62Integration.findCounterparties(this,party.text.toString()).firstOrNull()
        if(cp==null){Toast.makeText(this,"Party must be an existing V6.2 counterparty. Create it through Register Credit first.",Toast.LENGTH_LONG).show();return}
        val owner=V62Integration.currentUserId(this);val isCredit=direction.selectedItemPosition==0
        val rel= s.all(V62Store.RELATIONSHIPS).firstOrNull{it.optString("ownerUserId")==owner&&it.optString("counterpartyId")==cp.optString("id")&&it.optString("status")!="CLOSED"}
        if(!isCredit&&rel==null){Toast.makeText(this,"No active credit relationship exists for this repayment.",Toast.LENGTH_LONG).show();return}
        val code=(100000+Random.nextInt(900000)).toString();val e=input("Enter 6-digit OTP")
        val dlg=AlertDialog.Builder(this).setTitle("QR Khata consent").setMessage("Demo OTP: $code\nConfirm this ledger entry and balance update.").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create()
        dlg.setOnShowListener{dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(e.text.toString()!=code){e.error="Incorrect OTP";return@setOnClickListener};dlg.dismiss();commit(cp,a,isCredit,rel,owner)}}
        dlg.show()
    }
    private fun commit(cp:JSONObject,a:Double,isCredit:Boolean,rel0:JSONObject?,owner:String){
        val rel=rel0?:JSONObject().apply{put("id",V62Store.id("REL"));put("ownerUserId",owner);put("counterpartyId",cp.optString("id"));put("counterpartyMobile",cp.optString("mobile"));put("type","PERSONAL_CREDIT");put("principal",0.0);put("amount",0.0);put("outstanding",0.0);put("status","ACTIVE");put("createdAt",System.currentTimeMillis())}
        val old=rel.optDouble("outstanding",rel.optDouble("amount",0.0));val after=if(isCredit)old+a else (old-a).coerceAtLeast(0.0)
        if(isCredit){rel.put("amount",rel.optDouble("amount",0.0)+a);rel.put("principal",rel.optDouble("principal",0.0)+a)}
        rel.put("outstanding",after);rel.put("lastQrKhataAt",System.currentTimeMillis());if(!isCredit&&after<=0)rel.put("status","CLOSED")
        if(rel0==null)s.add(V62Store.RELATIONSHIPS,rel)else s.replace(V62Store.RELATIONSHIPS,rel)
        val id=V62Store.id("QK");s.add(V62Store.QR_KHATA,JSONObject().apply{put("id",id);put("relationshipId",rel.optString("id"));put("counterpartyId",cp.optString("id"));put("party",cp.optString("name"));put("amount",a);put("direction",if(isCredit)"CREDIT" else "REPAYMENT");put("note",note.text.toString().trim());put("consentVerified",true);put("recordedBy",owner);put("createdAt",System.currentTimeMillis())})
        if(!isCredit)s.add(V62Store.REPAYMENTS,JSONObject().apply{put("id",V62Store.id("PAY"));put("relationshipId",rel.optString("id"));put("counterpartyId",cp.optString("id"));put("amount",a);put("principal",a);put("interest",0.0);put("date",SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date()));put("consentVerified",true);put("recordedBy",owner);put("ownerUserId",owner)})
        V62EventBus.publish(V62Event(V62Events.QR_KHATA_CHANGED,id));V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED,rel.optString("id")));Toast.makeText(this,"QR Khata entry saved and connected to the ledger.",Toast.LENGTH_LONG).show();render()
    }
    private fun history(){
        root.removeAllViews();add(ArthSaathiV62Design.title(this,"QR Udhaar Khata","Ledger history"),2)
        val owner=V62Integration.currentUserId(this);val rows=s.all(V62Store.QR_KHATA).filter{it.optString("recordedBy")==owner}.sortedByDescending{it.optLong("createdAt")}
        if(rows.isEmpty())add(ArthSaathiV62Design.text(this,"No QR Khata entries yet.",12f,ArthSaathiV62Design.MUTED),10)
        rows.forEach{add(ArthSaathiV62Design.text(this,"${it.optString("party")} • ${it.optString("direction")}\n₹${it.optDouble("amount")} • ${it.optString("note")}",12f,ArthSaathiV62Design.NAVY,true),6)}
        add(ArthSaathiV62Design.button(this,"BACK TO QR KHATA",ArthSaathiV62Design.NAVY){render()},10)
    }
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==700&&resultCode==RESULT_OK&&data!=null&&::party.isInitialized){party.setText(data.getStringExtra("vendor").orEmpty());amount.setText(data.getStringExtra("amount").orEmpty());note.setText(listOf(data.getStringExtra("invoice").orEmpty(),data.getStringExtra("date").orEmpty()).filter{it.isNotBlank()}.joinToString(" • "))}}
}