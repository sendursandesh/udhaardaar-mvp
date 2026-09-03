package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/** TTMM = Tu Tera, Main Mera. Temporary shared-expense lending/settlement.
 * No loan documents, DPN or guarantor are required. Registration and settlement are OTP based. */
class V5TTMMActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val otp by lazy { V5OtpConsentService(this) }
    private lateinit var payer: EditText
    private lateinit var recipients: EditText
    private lateinit var amount: EditText
    private lateinit var purpose: EditText
    private lateinit var status: TextView

    private fun e(hint:String, multi:Boolean=false)=EditText(this).apply{this.hint=hint;setSingleLine(!multi);if(multi){minLines=3;gravity=android.view.Gravity.TOP}setPadding(18,12,18,12)}
    private fun b(text:String, action:()->Unit)=Button(this).apply{this.text=text;setOnClickListener{action()}}

    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);render()}
    private fun render(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,18,24,28)}
        root.addView(TextView(this).apply{text="TU TERA, MAIN MERA";textSize=25f;setPadding(0,0,0,6)})
        root.addView(TextView(this).apply{text="Temporary shared-expense lending • pro-rata • OTP based • no documents";textSize=14f;setPadding(0,0,0,18)})
        payer=e("Person who paid the vendor")
        recipients=e("Friends and their shares — one per line: Amit=250, Riya=300",true)
        amount=e("Total amount paid to vendor (₹)")
        purpose=e("Expense / vendor / reason")
        listOf(payer,recipients,amount,purpose).forEach{root.addView(it,LinearLayout.LayoutParams(-1,if(it==recipients)150 else 58).apply{setMargins(0,5,0,5)})}
        root.addView(TextView(this).apply{text="Only the specified shares are registered. The payer's own share is the balance after friends' shares. No document/DPN/guarantor is created.";textSize=12f;setPadding(4,8,4,12)})
        root.addView(b("CALCULATE / REQUEST OTP"){issue()})
        status=TextView(this).apply{textSize=14f;setPadding(0,18,0,8)};root.addView(status)
        root.addView(b("VIEW TTMM ENTRIES"){showEntries()})
        root.addView(b("SETTLE / CONFIRM RECOVERY"){settleDialog()})
        root.addView(b("BACK"){finish()})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
    }
    private fun parseShares():List<Pair<String,Double>>=recipients.text.toString().lines().mapNotNull{line->val x=line.split("=",limit=2);if(x.size!=2)null else x[0].trim().takeIf{it.isNotBlank()}?.let{n->x[1].trim().toDoubleOrNull()?.let{v->n to v}}}.filter{it.second>0}
    private fun issue(){
        val p=payer.text.toString().trim();val a=amount.text.toString().trim().toDoubleOrNull();val rows=parseShares();val totalShares=rows.sumOf{it.second}
        if(p.isBlank()||a==null||a<=0||rows.isEmpty()||totalShares<=0||totalShares>a){status.text="Enter a valid payer, total amount and friend shares. Shares cannot exceed the vendor payment.";return}
        val id="TTMM-${System.currentTimeMillis()}";val consent=otp.issue(id,"TTMM_SHARE","COUNTERPARTIES")
        val demo=store.find("consents",consent)?.optString("otp","")?:""
        val summary=rows.joinToString("\n"){it.first+" → ₹"+String.format("%.2f",it.second)}
        val input=e("Enter counterparty OTP")
        AlertDialog.Builder(this).setTitle("Confirm TTMM sharing").setMessage("Paid by $p: ₹${String.format("%.2f",a)}\n\nFriends' shares:\n$summary\n\nPayer's own share/balance: ₹${String.format("%.2f",a-totalShares)}\n\nTemporary entry only. No loan documents are required. Demo OTP: $demo").setView(input).setPositiveButton("CONFIRM"){_,_->
            if(otp.verify(consent,input.text.toString().trim())){
                store.add("ttmm_entries",JSONObject().apply{put("id",id);put("payer",p);put("totalAmount",a);put("friendShares",rows.joinToString("|"){it.first+"="+it.second});put("friendShareTotal",totalShares);put("payerShare",a-totalShares);put("purpose",purpose.text.toString().trim());put("consentId",consent);put("status","REGISTERED");put("settledAmount",0.0);put("createdAt",System.currentTimeMillis());put("updatedAt",System.currentTimeMillis())})
                V5WorkflowRepository(this).appendAudit(id,"TTMM_ENTRY_REGISTERED","COUNTERPARTIES","OTP confirmed; temporary shared-expense reimbursement")
                status.text="Registered. Friends owe ₹${String.format("%.2f",totalShares)} in total to $p."
            }else status.text="Incorrect OTP. Nothing was registered."
        }.setNegativeButton("CANCEL",null).show()
    }
    private fun settleDialog(){
        val rows=store.all("ttmm_entries").filter{it.optString("status")=="REGISTERED"||it.optString("status")=="PARTIALLY_SETTLED"}
        if(rows.isEmpty()){status.text="No outstanding TTMM entries.";return}
        val labels=rows.mapIndexed{i,j->"$i: ${j.optString("id")} • ${j.optString("payer")} • receivable ₹${String.format("%.2f",j.optDouble("friendShareTotal",0.0)-j.optDouble("settledAmount",0.0))}"}
        val input=e("Enter entry number")
        AlertDialog.Builder(this).setTitle("TTMM recovery").setMessage(labels.joinToString("\n")).setView(input).setPositiveButton("NEXT"){_,_->val idx=input.text.toString().toIntOrNull();if(idx==null||idx !in rows.indices){status.text="Invalid entry number."}else confirmSettlement(rows[idx])}.setNegativeButton("CANCEL",null).show()
    }
    private fun confirmSettlement(j:JSONObject){
        val outstanding=j.optDouble("friendShareTotal",0.0)-j.optDouble("settledAmount",0.0);val amountInput=e("Amount recovered now (₹)");
        AlertDialog.Builder(this).setTitle("Confirm recovery").setMessage("Outstanding for this TTMM entry: ₹${String.format("%.2f",outstanding)}\nCounterparty must OTP-confirm this recovery.").setView(amountInput).setPositiveButton("REQUEST OTP"){_,_->val pay=amountInput.text.toString().toDoubleOrNull();if(pay==null||pay<=0||pay>outstanding){status.text="Invalid recovery amount."}else{val cid=otp.issue(j.optString("id"),"TTMM_RECOVERY","COUNTERPARTY");val demo=store.find("consents",cid)?.optString("otp","")?:"";val otpInput=e("Enter counterparty OTP");AlertDialog.Builder(this).setTitle("OTP confirmation").setMessage("Recovery ₹${String.format("%.2f",pay)}. Demo OTP: $demo").setView(otpInput).setPositiveButton("CONFIRM"){_,_->if(otp.verify(cid,otpInput.text.toString().trim())){val settled=j.optDouble("settledAmount",0.0)+pay;j.put("settledAmount",settled);j.put("status",if(settled+0.005>=j.optDouble("friendShareTotal",0.0))"SETTLED" else "PARTIALLY_SETTLED");j.put("updatedAt",System.currentTimeMillis());store.replace("ttmm_entries",j);V5WorkflowRepository(this).appendAudit(j.optString("id"),"TTMM_RECOVERY_CONFIRMED","COUNTERPARTY","OTP confirmed recovery ₹$pay");status.text="Recovery confirmed. Remaining ₹${String.format("%.2f",j.optDouble("friendShareTotal",0.0)-settled)}."}else status.text="Incorrect OTP. Recovery not recorded."}.setNegativeButton("CANCEL",null).show()}}.setNegativeButton("CANCEL",null).show()
    }
    private fun showEntries(){
        val rows=store.all("ttmm_entries");val text=if(rows.isEmpty())"No TTMM entries yet." else rows.joinToString("\n\n"){j->"${j.optString("id")}\n${j.optString("payer")} paid ₹${j.optDouble("totalAmount",0.0)}\nFriends' shares: ${j.optString("friendShares")}\nSettled: ₹${j.optDouble("settledAmount",0.0)}\nStatus: ${j.optString("status")}\n${j.optString("purpose")}"}
        AlertDialog.Builder(this).setTitle("TTMM Entries").setMessage(text).setPositiveButton("OK",null).show()
    }
}
