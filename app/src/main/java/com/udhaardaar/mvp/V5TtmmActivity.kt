package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

/** TTMM (Take The Money, Mate): shared-bill memory and consent-based micro-credit. */
class V5TtmmActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val friends = mutableListOf<Pair<String, Double>>()
    private lateinit var list: LinearLayout
    private lateinit var payer: EditText
    private lateinit var bill: EditText

    private fun add(r: LinearLayout, v: View, h: Int = 52) { r.addView(v, LinearLayout.LayoutParams(-1, h).apply { setMargins(0, 4, 0, 4) }) }
    private fun field(h: String) = EditText(this).apply { hint = h; setSingleLine(true); textSize = 14f }
    private fun btn(t: String, c: Int = Color.rgb(25,111,220), a: () -> Unit) = Button(this).apply { text=t; isAllCaps=false; setTextColor(Color.WHITE); setBackgroundColor(c); setOnClickListener{a()} }

    override fun onCreate(b: Bundle?) { super.onCreate(b); show() }

    private fun show() {
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(28,24,28,24)}
        add(root,TextView(this).apply{text="TTMM";textSize=28f;setTextColor(Color.rgb(24,58,92))},58)
        add(root,TextView(this).apply{text="Take The Money, Mate — split a bill, turn each friend's verified share into a credit record, and remember it for the next get-together.";textSize=13f},76)
        payer=field("Who paid the bill? *");bill=field("Total bill ₹ *");add(root,payer);add(root,bill)
        add(root,btn("ADD FRIEND + SHARE",Color.rgb(0,145,135)){addFriend(root)})
        list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};add(root,list,LinearLayout.LayoutParams.MATCH_PARENT.height.coerceAtLeast(0))
        add(root,btn("CREATE TTMM + SEND VERIFICATION",Color.rgb(25,145,78)){createBill()})
        add(root,btn("TTMM MEMORY / RECOVERY",Color.rgb(210,135,15)){memory()})
        add(root,btn("HOME",Color.rgb(24,58,92)){finish()})
        setContentView(ScrollView(this).apply{addView(root)})
    }

    private fun addFriend(root: LinearLayout) {
        val n=field("Friend name / mobile *"),s=field("Their share ₹ *")
        val row=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};add(row,n);add(row,s,48)
        add(list,row,105)
        add(list,btn("ADD THIS SHARE",Color.rgb(100,100,100)){val x=s.text.toString().toDoubleOrNull();if(n.text.isBlank()||x==null||x<0){s.error="Enter a valid share";return@btn};friends.add(n.text.toString() to x);Toast.makeText(this,"Share added for ${n.text}",Toast.LENGTH_SHORT).show();n.isEnabled=false;s.isEnabled=false})
    }

    private fun createBill() {
        val total=bill.text.toString().toDoubleOrNull();if(payer.text.isBlank()||total==null||total<=0){bill.error="Enter a valid total bill";return}
        if(friends.isEmpty()){Toast.makeText(this,"Add at least one friend share",Toast.LENGTH_LONG).show();return}
        val sum=friends.sumOf{it.second};if(sum>total+0.01){Toast.makeText(this,"Friend shares exceed the bill",Toast.LENGTH_LONG).show();return}
        val id="TTMM-${System.currentTimeMillis()}";val arr=JSONArray();friends.forEach{(n,x)->arr.put(JSONObject().apply{put("friend",n);put("share",x);put("verification","PENDING")})}
        store.add("ttmm_bills",JSONObject().apply{put("id",id);put("payer",payer.text.toString());put("total",total);put("friends",arr.toString());put("createdAt",System.currentTimeMillis());put("status","VERIFICATION_PENDING")})
        friends.forEach{(n,x)->store.add("ttmm_credits",JSONObject().apply{put("id","TTMMC-${System.currentTimeMillis()}-${n.hashCode()}");put("ttmmId",id);put("counterparty",n);put("amount",x);put("direction","RECEIVABLE");put("type","FRIEND_BILL_SHARE");put("verification","PENDING");put("recoveryMemory","TTMM:$id")})}
        AlertDialog.Builder(this).setTitle("TTMM created").setMessage("Each friend's ₹ share is recorded as a pending, consent-controlled credit. Friends can verify or reject their share. Once verified, it remains searchable in TTMM Memory for future recovery/get-togethers.").setPositiveButton("OK",null).show()
    }

    private fun memory() {
        val a=store.all("ttmm_bills");val msg=if(a.isEmpty())"No TTMM memories yet." else a.takeLast(20).joinToString("\n\n"){ "${it.optString("id")} • ${it.optString("payer")} • ₹${it.optDouble("total")} • ${it.optString("status")}" }
        AlertDialog.Builder(this).setTitle("TTMM Memory").setMessage(msg).setPositiveButton("OK",null).show()
    }
}
