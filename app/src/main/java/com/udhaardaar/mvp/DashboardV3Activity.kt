package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.ViewGroup
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

class DashboardV3Activity : AppCompatActivity() {
    private lateinit var databaseHelper: V3DatabaseHelper
    private lateinit var creditGiven: TextView
    private lateinit var creditReceived: TextView
    private lateinit var outstanding: TextView
    private lateinit var overdue: TextView
    private lateinit var upcomingRepayments: TextView
    private lateinit var recentActivity: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_dashboard_v3); databaseHelper=V3DatabaseHelper(this)
        creditGiven=findViewById(R.id.tvCreditGiven); creditReceived=findViewById(R.id.tvCreditReceived); outstanding=findViewById(R.id.tvOutstanding); overdue=findViewById(R.id.tvOverdue); upcomingRepayments=findViewById(R.id.tvUpcomingRepayments); recentActivity=findViewById(R.id.tvRecentActivity)
        findViewById<Button>(R.id.btnRegisterCredit).setOnClickListener { startActivity(Intent(this,RegisterCreditV3Activity::class.java)) }
        findViewById<Button>(R.id.btnRecordRepayment).setOnClickListener { }
        addV4Actions()
    }
    private fun addV4Actions() {
        val content=window.decorView.findViewById<ViewGroup>(android.R.id.content); val scroll=content.getChildAt(0) as? ViewGroup ?: return; val body=scroll.getChildAt(0) as? ViewGroup ?: return
        val formal=Button(this).apply { text="BANK / NBFC LOAN AUDIT"; textSize=15f; setTextColor(Color.WHITE); setOnClickListener { startActivity(Intent(this@DashboardV3Activity,FormalLoanAuditActivity::class.java)) } }
        val vault=Button(this).apply { text="PERSONAL ASSET & INHERITANCE VAULT"; textSize=15f; setTextColor(Color.WHITE); setOnClickListener { startActivity(Intent(this@DashboardV3Activity,AssetVaultActivity::class.java)) } }
        body.addView(formal,4); body.addView(vault,5)
    }
    override fun onResume(){super.onResume();updateDashboard()}
    private fun updateDashboard(){val db=databaseHelper.readableDatabase;var given=0.0;var received=0.0;var outstandingAmount=0.0;db.rawQuery("SELECT direction,SUM(principal_amount) FROM credits GROUP BY direction",null).use{c->while(c.moveToNext()){val d=c.getString(0);val a=c.getDouble(1);if(d=="GIVEN")given+=a;if(d=="RECEIVED")received+=a}};db.rawQuery("SELECT c.principal_amount,COALESCE(SUM(r.amount),0) FROM credits c LEFT JOIN repayments r ON c.id=r.credit_id WHERE c.status='ACTIVE' GROUP BY c.id",null).use{c->while(c.moveToNext())outstandingAmount+=(c.getDouble(0)-c.getDouble(1)).coerceAtLeast(0.0)};creditGiven.text=formatCurrency(given);creditReceived.text=formatCurrency(received);outstanding.text=formatCurrency(outstandingAmount);overdue.text=formatCurrency(0.0);upcomingRepayments.text="No upcoming repayments";recentActivity.text="No recent activity"}
    private fun formatCurrency(amount:Double)="₹"+String.format("%,.2f",amount)
}
