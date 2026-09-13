package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import org.json.JSONObject

class V62InsuranceActivity:androidx.appcompat.app.AppCompatActivity(){
 private val store by lazy{V5LocalStore(this)}; private val d by lazy{resources.displayMetrics.density}; private val root by lazy{LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(ArthSaathiV62Design.dp(16,d),ArthSaathiV62Design.dp(10,d),ArthSaathiV62Design.dp(16,d),ArthSaathiV62Design.dp(28,d));setBackgroundColor(ArthSaathiV62Design.BG)}}
 private lateinit var policy:JSONObject
 private fun input(h:String)=ArthSaathiV62Design.input(this,h)
 override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);render()}
 private fun render(){root.removeAllViews();ArthSaathiV62Design.add(root,ArthSaathiV62Design.title(this,"Insurance & Protection","Scan • Verify • Protect"),2);ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,"Your original policy is retained. AI extraction is always review-first: nothing becomes a confirmed financial fact until you verify it.",10f,ArthSaathiV62Design.MUTED),8)
  ArthSaathiV62Design.add(root,ArthSaathiV62Design.button(this,"SCAN / ATTACH POLICY DOCUMENT",ArthSaathiV62Design.TEAL){pick()},10)
  ArthSaathiV62Design.add(root,ArthSaathiV62Design.button(this,"ENTER MANUALLY",ArthSaathiV62Design.BLUE){form(null)},5)
  if(::policy.isInitialized){val status=if(policy.optBoolean("originalRetained"))"Original retained • AI review required" else "Manual entry";ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,"DOCUMENT STATUS: $status",10f,ArthSaathiV62Design.GREEN,true),10);ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,"Critical fields",15f,ArthSaathiV62Design.NAVY,true),7);val f=JSONObject();listOf("Policy number","Insurer","Policy type","Policyholder","Insured person","Premium / frequency","Sum assured","Start date","End / maturity date","Next premium due","Nominee","Status","Key exclusions / waiting periods","Riders / benefits","Claim contact","Loan / surrender value (if applicable)").forEach{f.put(it,"")};f.keys().forEach{val e=input(it);ArthSaathiV62Design.add(root,e,4)};ArthSaathiV62Design.add(root,ArthSaathiV62Design.button(this,"SAVE VERIFIED POLICY",ArthSaathiV62Design.GREEN){save()},12)}
  setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
 }
 private fun pick(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/pdf";putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);addCategory(Intent.CATEGORY_OPENABLE)},41)}
 override fun onActivityResult(r:Int,c:Int,data:Intent?){super.onActivityResult(r,c,data);if(r==41&&c==Activity.RESULT_OK&&data?.data!=null){policy=ArthSaathiV62Core.saveDocument(this,data.data!!,"INSURANCE");Toast.makeText(this,"Original policy retained. AI extraction queued for verification.",Toast.LENGTH_LONG).show();form(policy)}}
 private fun form(doc:JSONObject?){policy=doc?:JSONObject().apply{put("id",ArthSaathiV62Core.id("POL"));put("originalRetained",false)};render();}
 private fun save(){policy.put("status","VERIFIED");policy.put("verifiedAt",System.currentTimeMillis());store.replace(ArthSaathiV62Core.INSURANCE,policy);Toast.makeText(this,"Policy saved to Protection Vault.",Toast.LENGTH_SHORT).show();finish()}
}
