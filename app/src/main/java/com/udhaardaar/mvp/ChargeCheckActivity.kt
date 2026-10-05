package com.udhaardaar.mvp
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
class ChargeCheckActivity : AppCompatActivity() {
 private val store by lazy { V5LocalStore(this) }
 private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
 private fun field(h:String)=EditText(this).apply{hint=h;textSize=14f;setSingleLine(true);minHeight=dp(50)}
 private fun add(r:LinearLayout,v:android.view.View){r.addView(v,LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT).apply{setMargins(0,dp(4),0,dp(4))})}
 private fun btn(t:String,c:Int,a:()->Unit)=Button(this).apply{text=t;isAllCaps=false;setTextColor(Color.WHITE);setBackgroundColor(c);setOnClickListener{a()}}
 override fun onCreate(b:Bundle?){super.onCreate(b);render()}
 private fun render(){
  val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(16),dp(18),dp(24));setBackgroundColor(0xFFF7F2E8.toInt())}
  add(r,TextView(this).apply{text="ChargeCheck";textSize=24f;setTextColor(0xFF17324D.toInt())})
  add(r,TextView(this).apply{text="Compare sanctioned terms with actual debits, identify variance and preserve the evidence trail.";textSize=12f})
  val ref=field("Loan / account reference *"); val sanctionedRoi=field("Sanctioned ROI %"); val actualRoi=field("Actual ROI %")
  val sanctionedCharges=field("Sanctioned fees / charges ₹"); val actualCharges=field("Actual fees / debits ₹")
  val sanctionedInterest=field("Sanctioned interest ₹"); val actualInterest=field("Actual interest charged ₹")
  listOf(ref,sanctionedRoi,actualRoi,sanctionedCharges,actualCharges,sanctionedInterest,actualInterest).forEach{add(r,it)}
  add(r,btn("UPLOAD SANCTION LETTER / STATEMENT",0xFF008F87.toInt()){pickEvidence()})
  add(r,btn("RUN CHARGECHECK",0xFF2E7D5B.toInt()){
   val sc=sanctionedCharges.text.toString().toDoubleOrNull()?:0.0; val ac=actualCharges.text.toString().toDoubleOrNull()?:0.0
   val si=sanctionedInterest.text.toString().toDoubleOrNull()?:0.0; val ai=actualInterest.text.toString().toDoubleOrNull()?:0.0
   val variance=(ac-sc)+(ai-si)
   val roiVariance=(actualRoi.text.toString().toDoubleOrNull()?:0.0)-(sanctionedRoi.text.toString().toDoubleOrNull()?:0.0)
   val id=ref.text.toString().ifBlank{"CHARGECHECK-${System.currentTimeMillis()}"}
   store.replace("charge_checks",JSONObject().apply{put("id",id);put("sanctionedCharges",sc);put("actualCharges",ac);put("sanctionedInterest",si);put("actualInterest",ai);put("variance",variance);put("sanctionedRoi",sanctionedRoi.text.toString());put("actualRoi",actualRoi.text.toString());put("roiVariance",roiVariance);put("status",if(variance>0.01||roiVariance>0.001)"REVIEW_REQUIRED" else "RECONCILED");put("checkedAt",System.currentTimeMillis())})
   AlertDialog.Builder(this).setTitle("ChargeCheck Result").setMessage("Charge variance: ₹%.2f\nROI variance: %.2f percentage points\nStatus: %s".format(variance,roiVariance,if(variance>0.01||roiVariance>0.001)"REVIEW REQUIRED" else "RECONCILED")).setPositiveButton("OK",null).show()
  })
  add(r,btn("VIEW PREVIOUS CHECKS",0xFFC79A3B.toInt()){val a=store.all("charge_checks").takeLast(20);AlertDialog.Builder(this).setTitle("ChargeCheck History").setMessage(if(a.isEmpty())"No checks yet." else a.joinToString("\n\n"){"${it.optString("id")} • ${it.optString("status")}\nVariance ₹${it.optDouble("variance")} • ROI Δ ${it.optDouble("roiVariance")}"}).setPositiveButton("OK",null).show()})
  add(r,btn("HOME",0xFF17324D.toInt()){finish()})
  setContentView(ScrollView(this).apply{addView(r)})
 }
 private fun pickEvidence(){startActivityForResult(android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply{type="application/pdf";addCategory(android.content.Intent.CATEGORY_OPENABLE);addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)},7004)}
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:android.content.Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==7004&&resultCode==RESULT_OK&&data?.data!=null){val u=data.data!!;try{contentResolver.takePersistableUriPermission(u,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};store.add("documents",JSONObject().apply{put("id","DOC-${System.currentTimeMillis()}");put("type","CHARGECHECK_EVIDENCE");put("uri",u.toString());put("status","INDEXED");put("createdAt",System.currentTimeMillis())});Toast.makeText(this,"Evidence indexed",Toast.LENGTH_LONG).show()}}
}