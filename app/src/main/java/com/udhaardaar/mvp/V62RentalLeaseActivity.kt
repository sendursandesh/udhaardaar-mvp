package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import org.json.JSONObject

class V62RentalLeaseActivity:androidx.appcompat.app.AppCompatActivity(){
 private val store by lazy{V5LocalStore(this)};private val d by lazy{resources.displayMetrics.density};private val root by lazy{LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(ArthSaathiV62Design.dp(16,d),ArthSaathiV62Design.dp(10,d),ArthSaathiV62Design.dp(16,d),ArthSaathiV62Design.dp(28,d));setBackgroundColor(ArthSaathiV62Design.BG)}}
 private var doc=JSONObject();private val fields=linkedMapOf("Lessor / Landlord" to "","Lessee / Tenant" to "","Property / premises description" to "","Monthly rent" to "","Security deposit" to "","Lease start date" to "","Lease end date" to "","Rent due date" to "","Escalation / revision" to "","Lock-in period" to "","Notice period" to "","Maintenance responsibility" to "","Utilities responsibility" to "","Late-payment terms" to "","Renewal terms" to "","Termination terms" to "","Special conditions" to "")
 private fun input(h:String)=ArthSaathiV62Design.input(this,h)
 override fun onCreate(b:Bundle?){super.onCreate(b);render()}
 private fun render(){root.removeAllViews();ArthSaathiV62Design.add(root,ArthSaathiV62Design.title(this,"Rental & Lease","Scan agreements • Track critical terms"),2);ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,"You may be the landlord/lessor OR tenant/lessee. The relationship is stored from your side, not as an arbitrary third-party profile.",10f,ArthSaathiV62Design.MUTED),8);ArthSaathiV62Design.add(root,ArthSaathiV62Design.button(this,"SCAN LEASE DEED / RENT AGREEMENT",ArthSaathiV62Design.TEAL){pick()},8);ArthSaathiV62Design.add(root,ArthSaathiV62Design.button(this,"MANUAL ENTRY — ALL CRITICAL TERMS",ArthSaathiV62Design.BLUE){showForm()},5);if(doc.length()>0)ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,"Original document retained • AI extraction requires your verification",10f,ArthSaathiV62Design.GREEN,true),10);setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})}
 private fun pick(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/pdf";putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);addCategory(Intent.CATEGORY_OPENABLE)},42)}
 override fun onActivityResult(r:Int,c:Int,data:Intent?){super.onActivityResult(r,c,data);if(r==42&&c==Activity.RESULT_OK&&data?.data!=null){doc=ArthSaathiV62Core.saveDocument(this,data.data!!,"LEASE_RENTAL");Toast.makeText(this,"Agreement retained. AI critical-term extraction queued for review.",Toast.LENGTH_LONG).show();showForm()}}
 private fun showForm(){root.removeAllViews();ArthSaathiV62Design.add(root,ArthSaathiV62Design.title(this,"Verify Lease Terms","AI proposal → your confirmation"),2);val edits=linkedMapOf<String,EditText>();fields.keys.forEach{key->val e=input(key);edits[key]=e;ArthSaathiV62Design.add(root,e,5)};ArthSaathiV62Design.add(root,ArthSaathiV62Design.button(this,"SAVE VERIFIED AGREEMENT",ArthSaathiV62Design.GREEN){val o=JSONObject().apply{put("id",ArthSaathiV62Core.id("LEASE"));put("side","USER");put("documentId",doc.optString("id"));put("verifiedAt",System.currentTimeMillis())};edits.forEach{(k,e)->o.put(k,e.text.toString().trim())};store.add("v62_rentals",o);Toast.makeText(this,"Lease/rental relationship saved.",Toast.LENGTH_SHORT).show();finish()});setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})}
}
