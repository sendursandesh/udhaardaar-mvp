package com.udhaardaar.mvp

import android.os.Bundle
import android.text.InputType
import android.widget.*
import org.json.JSONObject

class V62ChargesActivity : androidx.appcompat.app.AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val d get() = resources.displayMetrics.density
    private fun dp(v:Int) = (v*d).toInt()

    override fun onCreate(b:Bundle?){ super.onCreate(b); render() }

    private fun render(){
        val root=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(16),dp(14),dp(16),dp(24))
            setBackgroundColor(ArthSaathiV62Design.BG)
        }
        root.addView(ArthSaathiV62Design.title(this,"Charges","ArthSaathi service-charge register"))
        val desc=EditText(this).apply{hint="Service / transaction description";setSingleLine()}
        val amount=EditText(this).apply{
            hint="Amount (₹)"
            inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine()
        }
        root.addView(desc)
        root.addView(amount,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        root.addView(Button(this).apply{
            text="RECORD CHARGE"
            setOnClickListener{
                val a=amount.text.toString().toDoubleOrNull()
                if(a==null||a<=0){
                    Toast.makeText(this@V62ChargesActivity,"Enter a positive amount.",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val id="CHG-${System.currentTimeMillis()}"
                store.add("v62_revenue",JSONObject().apply{
                    put("id",id)
                    put("description",desc.text.toString().trim())
                    put("amount",a)
                    put("status","RECORDED")
                    put("createdAt",System.currentTimeMillis())
                })
                V5WorkflowRepository(this@V62ChargesActivity).appendAudit(id,"SERVICE_CHARGE_RECORDED","self","amount=$a")
                Toast.makeText(this@V62ChargesActivity,"Charge recorded: $id",Toast.LENGTH_LONG).show()
            }
        },LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(12)})
        root.addView(Button(this).apply{text="BACK";setOnClickListener{finish()}},LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(8)})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
    }
}
