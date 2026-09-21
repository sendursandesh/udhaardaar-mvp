package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.random.Random

class V62RepaymentActivity:AppCompatActivity(){
    private val store by lazy{V5LocalStore(this)}
    private val d get()=resources.displayMetrics.density
    private lateinit var root:LinearLayout
    private fun add(v:android.view.View,g:Int=7)=ArthSaathiV62Design.add(root,v,g)
    override fun onCreate(b:Bundle?){super.onCreate(b);render()}
    override fun onResume(){super.onResume();if(!isFinishing)render()}
    private fun render(){
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding((16*d).toInt(),(8*d).toInt(),(16*d).toInt(),(28*d).toInt());setBackgroundColor(ArthSaathiV62Design.BG)}
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
        add(ArthSaathiV62Design.title(this,"Repayment Centre","Select schedule item • Part-pay • Consent"),2)
        add(ArthSaathiV62Design.text(this,"Open a registered credit to see its actual agreed repayment schedule. Select the instalment being paid; part payment is allowed.",11f,ArthSaathiV62Design.MUTED),7)
        val owner=V62Integration.currentUserId(this)
        val rows=store.all(V62Store.RELATIONSHIPS).filter{it.optString("ownerUserId")==owner&&it.optString("status")!="CLOSED"}
        if(rows.isEmpty())add(ArthSaathiV62Design.text(this,"No active credit relationships.",13f,ArthSaathiV62Design.NAVY,true),12)
        rows.forEach{relationship(it)}
    }
    private fun relationship(rel:JSONObject){
        val cp=store.find(V62Store.COUNTERPARTIES,rel.optString("counterpartyId"));val name=cp?.optString("name").orEmpty().ifBlank{"Borrower"}
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding((13*d).toInt(),(11*d).toInt(),(13*d).toInt(),(11*d).toInt());background=ArthSaathiV62Design.card()}
        box.addView(ArthSaathiV62Design.text(this,name,15f,ArthSaathiV62Design.NAVY,true));box.addView(ArthSaathiV62Design.text(this,"Outstanding ₹${money(rel.optDouble("outstanding",rel.optDouble("principal",0.0)))} • ${rel.optString("repaymentMode","")}",11f,ArthSaathiV62Design.MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=4})
        val schedule=runCatching{JSONArray(rel.optString("repaymentSchedule","[]"))}.getOrDefault(JSONArray())
        if(schedule.length()==0){box.addView(ArthSaathiV62Design.text(this,"No schedule found. Use the registered terms to create a repayment schedule.",11f,ArthSaathiV62Design.RED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=7});box.addView(ArthSaathiV62Design.button(this,"RECORD NON-EMI / BULLET PAYMENT",ArthSaathiV62Design.GREEN){recordBullet(rel)},LinearLayout.LayoutParams(-1,-2).apply{topMargin=7})}
        for(i in 0 until schedule.length()){val item=schedule.optJSONObject(i)?:continue;if(item.optString("status")=="PAID")continue;val n=item.optInt("installment");val due=item.optDouble("dueAmount");val paid=item.optDouble("paidAmount");box.addView(ArthSaathiV62Design.button(this,"EMI $n • due ${item.optString("dueDate")} • ₹${money((due-paid).coerceAtLeast(0.0))}",ArthSaathiV62Design.BLUE){recordSchedulePayment(rel,i)},LinearLayout.LayoutParams(-1,-2).apply{topMargin=5})}
        add(box,8)
    }
    private fun recordSchedulePayment(rel:JSONObject,index:Int){
        val schedule=runCatching{JSONArray(rel.optString("repaymentSchedule","[]"))}.getOrDefault(JSONArray());val item=schedule.optJSONObject(index)?:return;val due=(item.optDouble("dueAmount")-item.optDouble("paidAmount")).coerceAtLeast(0.0)
        val amount=ArthSaathiV62Design.input(this,"Amount being paid ₹ (part payment allowed)");val date=ArthSaathiV62Design.input(this,"Payment date — tap calendar");date.isFocusable=false;date.setOnClickListener{V62UserFlow.pickDate(this,date)}
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(amount);addView(date,LinearLayout.LayoutParams(-1,-2).apply{topMargin=7})}
        AlertDialog.Builder(this).setTitle("EMI ${item.optInt("installment")} payment").setMessage("Outstanding for this schedule item: ₹${money(due)}").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("REQUEST OTP"){_,_->
            val a=amount.text.toString().replace(",","").toDoubleOrNull()?:0.0;if(a<=0||a>due||V62UserFlow.parseDate(date.text.toString())==null){Toast.makeText(this,"Enter a valid payment amount and calendar date.",Toast.LENGTH_LONG).show()}else otpAndCommit(rel,index,a,date.text.toString())
        }.show()
    }
    private fun recordBullet(rel:JSONObject){
        val amount=ArthSaathiV62Design.input(this,"Payment amount ₹");val date=ArthSaathiV62Design.input(this,"Payment date — tap calendar");date.isFocusable=false;date.setOnClickListener{V62UserFlow.pickDate(this,date)}
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(amount);addView(date,LinearLayout.LayoutParams(-1,-2).apply{topMargin=7})}
        AlertDialog.Builder(this).setTitle("Scheduled payment").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("REQUEST OTP"){_,_->val a=amount.text.toString().replace(",","").toDoubleOrNull()?:0.0;if(a<=0||V62UserFlow.parseDate(date.text.toString())==null)Toast.makeText(this,"Enter amount and calendar date.",Toast.LENGTH_LONG).show()else otpAndCommitBullet(rel,a,date.text.toString())}.show()
    }
    private fun otpAndCommit(rel:JSONObject,index:Int,a:Double,date:String){
        val code=(100000+Random.nextInt(900000)).toString();val e=ArthSaathiV62Design.input(this,"Enter 6-digit OTP");val dlg=AlertDialog.Builder(this).setTitle("Repayment consent").setMessage("Demo OTP: $code").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();dlg.setOnShowListener{dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(e.text.toString()!=code){e.error="Incorrect OTP";return@setOnClickListener};dlg.dismiss();val schedule=JSONArray(rel.optString("repaymentSchedule","[]"));val item=schedule.optJSONObject(index)?:return@setOnClickListener;item.put("paidAmount",item.optDouble("paidAmount")+a);if(item.optDouble("paidAmount")>=item.optDouble("dueAmount")-0.01)item.put("status","PAID")else item.put("status","PART_PAID");item.put("lastPaymentDate",date);rel.put("repaymentSchedule",schedule.toString());recalculate(rel,a,date);save(rel)}};dlg.show()
    }
    private fun otpAndCommitBullet(rel:JSONObject,a:Double,date:String){
        val code=(100000+Random.nextInt(900000)).toString();val e=ArthSaathiV62Design.input(this,"Enter 6-digit OTP");val dlg=AlertDialog.Builder(this).setTitle("Repayment consent").setMessage("Demo OTP: $code").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();dlg.setOnShowListener{dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(e.text.toString()!=code){e.error="Incorrect OTP";return@setOnClickListener};dlg.dismiss();recalculate(rel,a,date);save(rel)}};dlg.show()
    }
    private fun recalculate(rel:JSONObject,a:Double,date:String){val before=rel.optDouble("outstanding",rel.optDouble("principal",0.0));rel.put("outstanding",(before-a).coerceAtLeast(0.0));rel.put("lastRepaymentAmount",a);rel.put("lastRepaymentDate",date);if(rel.optDouble("outstanding")<=0.01)rel.put("status","CLOSED")}
    private fun save(rel:JSONObject){val owner=V62Integration.currentUserId(this);store.replace(V62Store.RELATIONSHIPS,rel);val id=V62Store.id("PAY");store.add(V62Store.REPAYMENTS,JSONObject().apply{put("id",id);put("relationshipId",rel.optString("id"));put("counterpartyId",rel.optString("counterpartyId"));put("amount",rel.optDouble("lastRepaymentAmount"));put("principal",rel.optDouble("lastRepaymentAmount"));put("interest",0.0);put("date",rel.optString("lastRepaymentDate"));put("consentVerified",true);put("recordedBy",owner);put("ownerUserId",owner);put("createdAt",System.currentTimeMillis())});V62Integration.recordConsent(this,rel.optString("counterpartyId"),rel.optString("id"),"REPAYMENT_CONFIRMATION",true);V62EventBus.publish(V62Event(V62Events.REPAYMENT_CHANGED,id));V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED,rel.optString("id")));Toast.makeText(this,"Repayment recorded; schedule and outstanding updated.",Toast.LENGTH_LONG).show();render()}
    private fun money(v:Double)=String.format(Locale.US,"%.2f",v)
}