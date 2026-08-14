package com.udhaardaar.mvp

import android.app.*
import android.os.Bundle
import android.graphics.Color
import android.view.*
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    lateinit var content: LinearLayout
    val blue = Color.rgb(21,101,192)
    override fun onCreate(b: Bundle?) { super.onCreate(b); dashboard() }

    fun base(title:String): LinearLayout {
        val outer=LinearLayout(this); outer.orientation=LinearLayout.VERTICAL; outer.setPadding(28,22,28,18)
        val bar=LinearLayout(this); bar.orientation=LinearLayout.HORIZONTAL; bar.gravity=Gravity.CENTER_VERTICAL
        val logo=TextView(this); logo.text="Udhaardaar"; logo.textSize=25f; logo.setTextColor(blue); logo.setTypeface(null,1)
        bar.addView(logo, LinearLayout.LayoutParams(0,60,1f))
        outer.addView(bar)
        val t=TextView(this); t.text=title; t.textSize=22f; t.setTypeface(null,1); t.setPadding(0,18,0,18); outer.addView(t)
        content=outer; setContentView(outer); return outer
    }
    fun btn(s:String, action:()->Unit): Button { val b=Button(this); b.text=s; b.setOnClickListener{action()}; return b }
    fun dashboard() {
        val o=base("Credit & Trust Dashboard")
        val card=TextView(this); card.text="Trust Score\\n\\n742 / 900\\nGood standing\\n\\nActive credit: ₹25,000    Repaid: ₹15,000"
        card.textSize=18f; card.setPadding(22,22,22,22); card.setBackgroundColor(Color.rgb(235,243,252)); o.addView(card)
        o.addView(btn("＋  Record New Udhaar"){create()})
        o.addView(btn("Repayment / Settlement"){repay()})
        o.addView(btn("My Transactions"){history()})
        o.addView(btn("How Udhaardaar Score Works"){score()})
    }
    fun create() {
        val o=base("Record New Udhaar")
        val name=EditText(this); name.hint="Borrower name"; o.addView(name)
        val phone=EditText(this); phone.hint="Mobile number"; phone.inputType=2; o.addView(phone)
        val amt=EditText(this); amt.hint="Amount (₹)"; amt.inputType=2; o.addView(amt)
        val purpose=EditText(this); purpose.hint="Purpose / trade description"; o.addView(purpose)
        val due=EditText(this); due.hint="Due date (DD-MM-YYYY)"; o.addView(due)
        o.addView(btn("Create & Acknowledge"){ 
            Toast.makeText(this,"Transaction recorded as DEMO TXN-"+System.currentTimeMillis().toString().takeLast(6),Toast.LENGTH_LONG).show()
            dashboard()
        })
        o.addView(btn("Back"){dashboard()})
    }
    fun repay() {
        val o=base("Repayment / Settlement")
        val id=EditText(this); id.hint="Transaction ID"; o.addView(id)
        val amt=EditText(this); amt.hint="Repayment amount (₹)"; amt.inputType=2; o.addView(amt)
        o.addView(btn("Record Repayment"){Toast.makeText(this,"Repayment recorded in MVP demo",Toast.LENGTH_LONG).show(); dashboard()})
        o.addView(btn("Back"){dashboard()})
    }
    fun history() {
        val o=base("Transaction History")
        val t=TextView(this); t.text="TXN-482913   Raj Kumar\\n₹10,000   Due 30-09-2026   ACTIVE\\n\\nTXN-381204   Anita Traders\\n₹15,000   Repaid ₹15,000   SETTLED\\n\\nTXN-277190   S. Verma\\n₹5,000    Due 15-08-2026   ACTIVE"
        t.textSize=16f; t.setPadding(10,10,10,30); o.addView(t)
        o.addView(btn("Back"){dashboard()})
    }
    fun score() {
        val o=base("Udhaardaar Trust Score")
        val t=TextView(this); t.text="742 / 900\\n\\nDemo scoring factors:\\n• Repayment history\\n• Timeliness\\n• Number of completed transactions\\n• Verified acknowledgements\\n• Outstanding obligations\\n\\nThis MVP score is illustrative only. A production bureau would require identity, consent, data governance, dispute handling and regulatory review."
        t.textSize=17f; o.addView(t); o.addView(btn("Back"){dashboard()})
    }
}
