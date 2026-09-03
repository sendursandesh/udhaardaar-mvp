package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/** TTMM = Tu Tera, Main Mera. Temporary shared-expense lending/settlement.
 * No loan documents, DPN or guarantor are required. Entries become registered only
 * after OTP confirmation by the counterparty. */
class V5TTMMActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val otp by lazy { V5OtpConsentService(this) }
    private lateinit var payer: EditText
    private lateinit var recipient: EditText
    private lateinit var amount: EditText
    private lateinit var purpose: EditText
    private lateinit var share: EditText
    private lateinit var status: TextView

    private fun e(hint:String)=EditText(this).apply{this.hint=hint;setSingleLine(true);setPadding(18,12,18,12)}
    private fun b(text:String, action:()->Unit)=Button(this).apply{this.text=text;setOnClickListener{action()}}

    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);render()}
    private fun render(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,18,24,28)}
        root.addView(TextView(this).apply{text="TU TERA, MAIN MERA";textSize=25f;setPadding(0,0,0,6)})
        root.addView(TextView(this).apply{text="Temporary shared-expense lending • OTP based • no documents";textSize=14f;setPadding(0,0,0,18)})
        payer=e("Person who paid the vendor"); recipient=e("Friend who will reimburse"); amount=e("Total amount paid (₹)"); purpose=e("Expense / vendor / reason"); share=e("Friend's pro-rata share (₹)")
        listOf(payer,recipient,amount,purpose,share).forEach{root.addView(it,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)})}
        root.addView(b("CALCULATE / REGISTER TEMPORARY SHARE"){issue()})
        status=TextView(this).apply{textSize=14f;setPadding(0,18,0,8)};root.addView(status)
        root.addView(b("VIEW TTMM ENTRIES"){showEntries()})
        root.addView(b("BACK"){finish()})
        setContentView(ScrollView(this).apply{addView(root)})
    }
    private fun issue(){
        val p=payer.text.toString().trim();val r=recipient.text.toString().trim();val a=amount.text.toString().toString().toDoubleOrNull();val s=share.text.toString().toString().toDoubleOrNull()
        if(p.isBlank()||r.isBlank()||a==null||a<=0||s==null||s<=0||s>a){status.text="Enter valid payer, friend, total and share amount.";return}
        val id="TTMM-${System.currentTimeMillis()}";val consent=otp.issue(id,"TTMM_SHARE","COUNTERPARTY")
        status.text="OTP sent to the counterparty. Entry is NOT registered until confirmation. Demo OTP is available in the confirmation dialog."
        val otpObj=store.find("consents",consent);val demo=otpObj?.optString("otp","")?:""
        val input=e("Enter counterparty OTP");AlertDialog.Builder(this).setTitle("Confirm temporary share").setMessage("₹${String.format("%.2f",s)} payable by ${r} to ${p}. This is a temporary shared-expense entry; no loan documents are required. Demo OTP: $demo").setView(input).setPositiveButton("CONFIRM"){_,_->
            if(otp.verify(consent,input.text.toString().trim())){
                store.add("ttmm_entries",JSONObject().apply{put("id",id);put("payer",p);put("recipient",r);put("totalAmount",a);put("shareAmount",s);put("purpose",purpose.text.toString().trim());put("consentId",consent);put("status","REGISTERED");put("createdAt",System.currentTimeMillis());put("updatedAt",System.currentTimeMillis())})
                V5WorkflowRepository(this).appendAudit(id,"TTMM_ENTRY_REGISTERED","COUNTERPARTY","OTP confirmed; temporary shared-expense reimbursement")
                status.text="Registered: ${r} owes ₹${String.format("%.2f",s)} to ${p}."
            }else status.text="Incorrect OTP. Nothing was registered."
        }.setNegativeButton("CANCEL",null).show()
    }
    private fun showEntries(){
        val rows=store.all("ttmm_entries");val text=if(rows.isEmpty())"No TTMM entries yet." else rows.joinToString("\n\n"){j->"${j.optString("id")}\n${j.optString("payer")} paid ₹${j.optDouble("totalAmount",0.0)}\n${j.optString("recipient")} owes ₹${j.optDouble("shareAmount",0.0)}\n${j.optString("purpose")} • ${j.optString("status")}"}
        AlertDialog.Builder(this).setTitle("TTMM Entries").setMessage(text).setPositiveButton("OK",null).show()
    }
}
