package com.udhaardaar.mvp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/** ArthSaathi V6.2 repayment centre — compact replacement for the legacy repayment screen. */
class V62RepaymentActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val navy=Color.rgb(16,45,82); private val teal=Color.rgb(12,171,158); private val blue=Color.rgb(42,103,221); private val green=Color.rgb(18,137,91); private val red=Color.rgb(193,67,72); private val bg=Color.rgb(246,249,252); private val border=Color.rgb(220,228,236)
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun tv(s:String,size:Float,color:Int=navy,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size;setTextColor(color);typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL)}
    private fun box(fill:Int=Color.WHITE)=android.graphics.drawable.GradientDrawable().apply{setColor(fill);setStroke(dp(1),border);cornerRadius=dp(16).toFloat()}
    private fun btn(s:String,color:Int=blue,click:()->Unit)=TextView(this).apply{text=s;textSize=14f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD;minHeight=dp(52);background=box(color);setOnClickListener{click()}}
    private fun lp(t:Int)=LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(t)}
    override fun onCreate(b:Bundle?){super.onCreate(b);render()}
    private fun render(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(12),dp(16),dp(20));setBackgroundColor(bg)};val head=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(14));background=box()};head.addView(tv("ARTHSAATHI",12f,teal,true));head.addView(tv("Repayment Centre",24f,navy,true),lp(4));head.addView(tv("Track dues • record payments • consent",11f),lp(3));root.addView(head);val rows=store.all("credits");val active=rows.filter{it.optString("status")=="ACTIVE"};root.addView(tv("${active.size} active credits",12f,blue,true),lp(14));if(active.isEmpty())root.addView(tv("No active credit records yet. Register a credit to start the repayment journey.",13f),lp(6)) else active.take(20).forEach{c->val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(14),dp(12),dp(14),dp(12));background=box()};card.addView(tv("${c.optString("id")} • ${c.optString("repaymentMethod")}",14f,navy,true));card.addView(tv("Principal ₹${String.format("%.2f",c.optDouble("principal",0.0))} • Outstanding ₹${String.format("%.2f",c.optDouble("outstanding",0.0))}",11f),lp(4));card.addView(tv("EMI ₹${String.format("%.2f",c.optDouble("emiAmount",0.0))} • ${c.optString("periodicity")} • Due ${c.optString("dueDate")}",11f,teal,true),lp(3));root.addView(card,lp(7));root.addView(btn("RECORD PAYMENT — ${c.optString("id")}",green){record(c)},lp(5))};root.addView(btn("← BACK TO ARTHSAATHI HOME",blue){finish()},lp(18));setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})}
    private fun record(c:JSONObject){val e=EditText(this).apply{hint="Payment amount ₹";textSize=16f;inputType=2 or 8192;setSingleLine(true)};AlertDialog.Builder(this).setTitle("Record repayment").setMessage("${c.optString("id")} • ${c.optString("repaymentMethod")}\nBoth-party consent should be obtained before finalising a repayment.").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("SAVE"){_,_->val p=e.text.toString().toDoubleOrNull();if(p==null||p<=0){Toast.makeText(this,"Enter a positive payment amount",Toast.LENGTH_LONG).show();return@setPositiveButton};val out=(c.optDouble("outstanding",0.0)-p).coerceAtLeast(0.0);c.put("outstanding",out);if(out<=0)c.put("status","CLOSED");store.replace("credits",c);Toast.makeText(this,"Payment recorded. Outstanding ₹${String.format("%.2f",out)}",Toast.LENGTH_LONG).show();render()}.show()}
}
