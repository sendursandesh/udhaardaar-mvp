package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.*
import org.json.JSONObject

class V62ChargeCheckActivity : androidx.appcompat.app.AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val d get()=resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()

    private fun field(h:String)=EditText(this).apply{
        hint=h;textSize=14f;setSingleLine();setTextColor(ArthSaathiV62Design.NAVY)
        setPadding(dp(12),dp(9),dp(12),dp(9))
    }

    override fun onCreate(b:Bundle?){super.onCreate(b);render()}

    private fun render(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(24));setBackgroundColor(ArthSaathiV62Design.BG)}
        root.addView(ArthSaathiV62Design.title(this,"ChargeCheck","Sanctioned vs actual charges"))
        root.addView(ArthSaathiV62Design.text(this,"Versioned comparison record; supporting sanction/statement documents remain reviewable.",11f,ArthSaathiV62Design.MUTED).apply{setPadding(0,dp(6),0,dp(10))})
        val sanctioned=field("Sanctioned total charges (₹)")
        val actual=field("Actual total charges (₹)")
        sanctioned.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        actual.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        root.addView(sanctioned);root.addView(actual,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        root.addView(Button(this).apply{
            text="COMPARE & SAVE EVIDENCE"
            setOnClickListener{
                val s=sanctioned.text.toString().toDoubleOrNull()
                val a=actual.text.toString().toDoubleOrNull()
                if(s==null||a==null||s<0||a<0){Toast.makeText(this@V62ChargeCheckActivity,"Enter valid non-negative amounts.",Toast.LENGTH_LONG).show();return@setOnClickListener}
                val variance=a-s
                val id="CC-${System.currentTimeMillis()}"
                store.add("v62_charge_checks",JSONObject().apply{put("id",id);put("sanctioned",s);put("actual",a);put("variance",variance);put("status","REVIEW");put("createdAt",System.currentTimeMillis())})
                V5WorkflowRepository(this@V62ChargeCheckActivity).appendAudit(id,"CHARGECHECK_RECORDED","self","sanctioned=$s; actual=$a; variance=$variance")
                Toast.makeText(this@V62ChargeCheckActivity,"ChargeCheck saved. Variance: ₹${"%.2f".format(variance)}",Toast.LENGTH_LONG).show()
            }
        },LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(12)})
        root.addView(Button(this).apply{text="VIEW SAVED CHECKS";setOnClickListener{showSaved()}},LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(8)})
        root.addView(Button(this).apply{text="BACK";setOnClickListener{finish()}},LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(8)})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
    }

    private fun showSaved(){
        val rows=store.all("v62_charge_checks")
        val msg=if(rows.isEmpty())"No ChargeCheck records." else rows.joinToString("\n\n"){j->"${j.optString("id")}\nSanctioned ₹${j.optDouble("sanctioned",0.0)}\nActual ₹${j.optDouble("actual",0.0)}\nVariance ₹${j.optDouble("variance",0.0)}"}
        AlertDialog.Builder(this).setTitle("ChargeCheck register").setMessage(msg).setPositiveButton("OK",null).show()
    }
}
