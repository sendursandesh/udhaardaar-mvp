package com.udhaardaar.mvp

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import org.json.JSONObject

class V62BureauActivity : androidx.appcompat.app.AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val d get() = resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()

    override fun onCreate(b:Bundle?){super.onCreate(b);render()}

    private fun render() {
        val root=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(16),dp(14),dp(16),dp(24))
            setBackgroundColor(ArthSaathiV62Design.BG)
        }
        root.addView(ArthSaathiV62Design.title(this,"Informal Credit Intelligence","Consent-first bureau foundation"))
        root.addView(ArthSaathiV62Design.text(this,
            "Consolidates recorded informal-credit behaviour. Counterparty history is not exposed without explicit consent.",
            11f, ArthSaathiV62Design.MUTED).apply{setPadding(0,dp(6),0,dp(12))})

        val credits=store.all(ArthSaathiV62Core.RELATIONSHIPS)
        val active=credits.count{it.optString("status")=="ACTIVE"}
        val overdue=credits.count{it.optString("status").contains("OVERDUE",true)}
        val settled=credits.count{it.optString("status").contains("CLOSED",true)||it.optString("status").contains("SETTLED",true)}
        val score=if(credits.isEmpty())"Not available — no consented history" else
            "Foundation score: ${(750 - overdue*60 + settled*10).coerceIn(300,900)} / 900"

        listOf(
            "Recorded relationships" to credits.size.toString(),
            "Active" to active.toString(),
            "Overdue / flagged" to overdue.toString(),
            "Settled / closed" to settled.toString(),
            "Score" to score
        ).forEach{(a,b)->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL
                setPadding(dp(12),dp(10),dp(12),dp(10))
                background=ArthSaathiV62Design.card(Color.WHITE,14,d)
            }
            row.addView(ArthSaathiV62Design.text(this,a,10f,ArthSaathiV62Design.MUTED,true))
            row.addView(ArthSaathiV62Design.text(this,b,14f,ArthSaathiV62Design.NAVY,true))
            root.addView(row,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        }

        val consent=CheckBox(this).apply{
            text="I have the counterparty's explicit consent to view/share this history."
            textSize=12f
            setTextColor(ArthSaathiV62Design.NAVY)
        }
        root.addView(consent,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(12)})
        root.addView(Button(this).apply{
            text="OPEN CONSENTED HISTORY"
            setOnClickListener{
                if(!consent.isChecked){
                    Toast.makeText(this@V62BureauActivity,"Consent is required before history is displayed.",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val payload=JSONObject().apply{put("records",credits.size);put("active",active);put("overdue",overdue);put("settled",settled)}
                V5WorkflowRepository(this@V62BureauActivity).appendAudit("BUREAU-${System.currentTimeMillis()}","BUREAU_HISTORY_VIEWED","self",payload.toString())
                Toast.makeText(this@V62BureauActivity,"Consented history access recorded.",Toast.LENGTH_LONG).show()
            }
        },LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(8)})
        root.addView(Button(this).apply{text="BACK";setOnClickListener{finish()}},LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(8)})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
    }
}
