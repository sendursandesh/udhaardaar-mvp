package com.udhaardaar.mvp

import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.util.Locale

/** Internal, explainable credit intelligence derived only from consented/local V6.2 records. */
class V62CreditIntelligenceActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d get() = resources.displayMetrics.density
    private val root by lazy { LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding((16*d).toInt(),(8*d).toInt(),(16*d).toInt(),(28*d).toInt()); setBackgroundColor(ArthSaathiV62Design.BG) } }
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(16);render()}
    override fun onResume(){super.onResume();if(!isFinishing)render()}
    private fun add(v:android.view.View,gap:Int=7)=ArthSaathiV62Design.add(root,v,gap)
    private fun render(){
        root.removeAllViews();add(ArthSaathiV62Design.title(this,"Credit Intelligence","History • Exposure • Repayment • Internal Score"),2);add(ArthSaathiV62Design.text(this,"This is an ArthSaathi internal behavioural indicator, not a credit-bureau report. Only records already present in your V6.2 account are used.",10f,ArthSaathiV62Design.MUTED),7)
        val cps=s.all(V62Store.COUNTERPARTIES);if(cps.isEmpty())add(ArthSaathiV62Design.text(this,"No counterparties recorded yet.",12f,ArthSaathiV62Design.NAVY,true),12)
        cps.forEach{cp->
            val id=cp.optString("id");val rel=s.all(V62Store.RELATIONSHIPS).filter{it.optString("counterpartyId")==id};val reps=s.all(V62Store.REPAYMENTS).filter{it.optString("counterpartyId")==id||rel.any{r->r.optString("id")==it.optString("relationshipId")}};val exposure=rel.filter{it.optString("status")!="CLOSED"}.sumOf{it.optDouble("outstanding",it.optDouble("amount",it.optDouble("principal",0.0)))};val repaid=reps.sumOf{it.optDouble("principal",it.optDouble("amount",0.0))};val score=V62Integration.score(this,id);val overdue=rel.sumOf{it.optDouble("overdueAmount",0.0)}
            val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding((13*d).toInt(),(11*d).toInt(),(13*d).toInt(),(11*d).toInt());background=ArthSaathiV62Design.card()};card.addView(ArthSaathiV62Design.text(this,"${cp.optString("name")} • ${cp.optString("mobile")}",14f,ArthSaathiV62Design.NAVY,true));card.addView(ArthSaathiV62Design.text(this,"Internal Score: $score / 900\nActive exposure: ₹${"%.2f".format(Locale.US,exposure)}\nRecorded repaid: ₹${"%.2f".format(Locale.US,repaid)}\nOverdue recorded: ₹${"%.2f".format(Locale.US,overdue)}\nRelationships: ${rel.size}",11f,ArthSaathiV62Design.MUTED),5);card.addView(ArthSaathiV62Design.text(this,explain(score,exposure,repaid,overdue),10f,ArthSaathiV62Design.TEAL));add(card,7)
        }
        add(ArthSaathiV62Design.button(this,"BACK",ArthSaathiV62Design.NAVY){finish()},10);setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
    }
    private fun explain(score:Int,exposure:Double,repaid:Double,overdue:Double):String=when{overdue>0->"Risk signal: recorded overdue amount exists. Review before extending additional credit.";exposure>0&&repaid<=0->"Limited repayment history is recorded against current exposure.";score>=800->"Positive signal: strong recorded repayment relative to exposure.";else->"Score reflects recorded exposure, repayment history and overdue/default signals only."}
}
