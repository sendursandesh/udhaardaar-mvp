package com.udhaardaar.mvp
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class V5QrCreditActivity:AppCompatActivity(){
 private val store by lazy{V5LocalStore(this)};private val engine by lazy{V5PlatformEngine(this)}
 private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true);textSize=14f}
 private fun add(r:LinearLayout,v:View){r.addView(v,LinearLayout.LayoutParams(-1,52).apply{setMargins(0,3,0,3)})}
 override fun onCreate(b:Bundle?){super.onCreate(b);val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(18,12,18,18)};r.addView(TextView(this).apply{text="UDHAARDAAR V5 • SCAN QR / PAY OR CREDIT";textSize=20f;setTextColor(Color.rgb(24,58,92))});r.addView(TextView(this).apply{text="No silent credit: transaction → consent → offer → agreement → payment/credit";textSize=12f})
  val qr=e("QR payload / merchant reference *");val merchant=e("Merchant / seller *");val invoice=e("Invoice / transaction reference");val goods=e("Goods / service description");val gross=e("Gross amount *");val tax=e("Tax");val discount=e("Discount");val buyer=e("Buyer");listOf(qr,merchant,invoice,goods,gross,tax,discount,buyer).forEach{add(r,it)}
  add(r,Button(this).apply{text="CAPTURE QR TRANSACTION";setOnClickListener{val amt=gross.text.toString().toDoubleOrNull();if(qr.text.isBlank()||merchant.text.isBlank()||amt==null||amt<=0){Toast.makeText(this@V5QrCreditActivity,"Enter QR, merchant and valid amount",Toast.LENGTH_LONG).show();return@setOnClickListener};val id="QR-${System.currentTimeMillis()}";store.add("qr_transactions",JSONObject().apply{put("id",id);put("qrRef",qr.text.toString());put("merchant",merchant.text.toString());put("invoice",invoice.text.toString());put("goods",goods.text.toString());put("gross",amt);put("tax",tax.text.toString());put("discount",discount.text.toString());put("buyer",buyer.text.toString());put("timestamp",System.currentTimeMillis());put("status","CAPTURED")});engine.audit(id,"QR_TRANSACTION_CAPTURED",buyer.text.toString());Toast.makeText(this@V5QrCreditActivity,"Transaction captured",Toast.LENGTH_LONG).show()}})
  add(r,Button(this).apply{text="PAY NOW";setOnClickListener{Toast.makeText(this@V5QrCreditActivity,"Payment hand-off recorded; live PSP/bank integration is required for actual settlement.",Toast.LENGTH_LONG).show()}});add(r,Button(this).apply{text="REQUEST / OFFER CREDIT";setOnClickListener{val id="OFR-${System.currentTimeMillis()}";store.add("credit_offers",JSONObject().apply{put("id",id);put("merchant",merchant.text.toString());put("amount",gross.text.toString().toDoubleOrNull()?:0.0);put("status","CONSENT_PENDING");put("provider","Provider abstraction");put("createdAt",System.currentTimeMillis())});engine.audit(id,"CREDIT_OFFER_CREATED",buyer.text.toString());Toast.makeText(this@V5QrCreditActivity,"Credit offer created pending seller/provider consent and underwriting",Toast.LENGTH_LONG).show()}});add(r,Button(this).apply{text="BACK";setOnClickListener{finish()}});setContentView(ScrollView(this).apply{addView(r)})}
}