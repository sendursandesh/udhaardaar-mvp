package com.udhaardaar.mvp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/** ArthSaathi V6.2 asset vault — compact premium replacement for the legacy vault screen. */
class V62AssetVaultActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val navy=Color.rgb(16,45,82); private val teal=Color.rgb(12,171,158); private val blue=Color.rgb(42,103,221); private val gold=Color.rgb(211,161,37); private val bg=Color.rgb(246,249,252); private val border=Color.rgb(220,228,236)
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun tv(s:String,size:Float,color:Int=navy,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size;setTextColor(color);typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL)}
    private fun box(fill:Int=Color.WHITE)=android.graphics.drawable.GradientDrawable().apply{setColor(fill);setStroke(dp(1),border);cornerRadius=dp(16).toFloat()}
    private fun btn(s:String,color:Int=blue,click:()->Unit)=TextView(this).apply{text=s;textSize=14f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD;minHeight=dp(52);background=box(color);setOnClickListener{click()}}
    private fun input(h:String)=EditText(this).apply{hint=h;textSize=15f;setSingleLine(true);setPadding(dp(12),dp(10),dp(12),dp(10));background=box()}
    private fun lp(t:Int)=LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(t)}
    override fun onCreate(b:Bundle?){super.onCreate(b);render()}
    private fun render(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(12),dp(16),dp(20));setBackgroundColor(bg)};val head=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(14));background=box()};head.addView(tv("ARTHSAATHI",12f,teal,true));head.addView(tv("Asset Vault",24f,navy,true),lp(4));head.addView(tv("Keep assets, ownership evidence and nomination links together",11f),lp(3));root.addView(head);val name=input("Asset name / account / property *");val type=input("Asset type: bank / property / vehicle / investment / other");val value=input("Approximate value ₹");val nominee=input("Nominee / intended beneficiary");val evidence=input("Evidence reference / document note");listOf(name,type,value,nominee,evidence).forEach{root.addView(it,lp(8))};root.addView(btn("SAVE ASSET + ENABLE SMART ALERTS",teal){val n=name.text.toString().trim();if(n.isBlank()){name.error="Enter asset name";return@btn};val id="AST-${System.currentTimeMillis()}";store.add("assets",JSONObject().apply{put("id",id);put("name",n);put("type",type.text.toString().trim());put("value",value.text.toString().toDoubleOrNull()?:0.0);put("nominee",nominee.text.toString().trim());put("evidence",evidence.text.toString().trim());put("createdAt",System.currentTimeMillis())});Toast.makeText(this,"Asset saved: $id",Toast.LENGTH_LONG).show();render()},lp(14));root.addView(btn("VIEW ASSET REGISTER",blue){showAssets()},lp(7));root.addView(tv("AI alerts can flag stale cash balances, missing nomination, renewal dates and other user-defined conditions. No money is moved automatically.",10f,Color.rgb(92,108,124)),lp(12));root.addView(btn("← BACK TO ARTHSAATHI HOME",gold){finish()},lp(14));setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})}
    private fun showAssets(){val rows=store.all("assets");val msg=if(rows.isEmpty())"No assets recorded yet." else rows.joinToString("\n\n"){j->"${j.optString("id")} • ${j.optString("name")}\n${j.optString("type")} • ₹${String.format("%.2f",j.optDouble("value",0.0))}\nNominee: ${j.optString("nominee")}\nEvidence: ${j.optString("evidence")}"};AlertDialog.Builder(this).setTitle("Asset Register").setMessage(msg).setPositiveButton("OK",null).show()}
}
