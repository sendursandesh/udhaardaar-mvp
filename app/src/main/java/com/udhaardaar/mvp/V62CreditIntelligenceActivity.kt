package com.udhaardaar.mvp

import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.util.Locale

/** Internal, explainable credit intelligence derived only from the current user's consented V6.2 records. */
class V62CreditIntelligenceActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d get() = resources.displayMetrics.density
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding((16*d).toInt(), (8*d).toInt(), (16*d).toInt(), (28*d).toInt()); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private lateinit var scroll: ScrollView
    private val ownerId by lazy { V62Integration.currentUserId(this) }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.setSoftInputMode(16)
        scroll = ScrollView(this).apply { isFillViewport = true; addView(root) }
        setContentView(scroll)
        render()
    }
    override fun onResume() { super.onResume(); if (!isFinishing) render() }

    private fun add(v: android.view.View, gap: Int = 7) = ArthSaathiV62Design.add(root, v, gap)

    private fun render() {
        root.removeAllViews()
        add(ArthSaathiV62Design.title(this, "Credit Intelligence", "History • Exposure • Repayment • Internal Score"), 2)
        add(ArthSaathiV62Design.text(this, "ArthSaathi's internal behavioural indicator is not a credit-bureau report. Relationship data is restricted to your V6.2 account, and history/score is shown only where history-sharing consent has been OTP-verified.", 10f, ArthSaathiV62Design.MUTED), 7)

        val allRels = s.all(V62Store.RELATIONSHIPS).filter { it.optString("ownerUserId").ifBlank { ownerId } == ownerId }
        val cpIds = allRels.map { it.optString("counterpartyId") }.filter { it.isNotBlank() }.toSet()
        val cps = s.all(V62Store.COUNTERPARTIES).filter { it.optString("id") in cpIds || it.optString("createdBy") == ownerId }

        if (cps.isEmpty()) add(ArthSaathiV62Design.text(this, "No V6.2 counterparties recorded yet.", 12f, ArthSaathiV62Design.NAVY, true), 12)

        cps.forEach { cp ->
            val id = cp.optString("id")
            val rel = allRels.filter { it.optString("counterpartyId") == id }
            if (rel.isEmpty() && cp.optString("createdBy") != ownerId) return@forEach

            val consented = s.all(V62Store.CONSENTS).any {
                it.optString("counterpartyId") == id &&
                it.optString("eventType") == "HISTORY_SHARING" &&
                it.optBoolean("otpVerified") &&
                it.optBoolean("granted")
            }

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding((13*d).toInt(), (11*d).toInt(), (13*d).toInt(), (11*d).toInt())
                background = ArthSaathiV62Design.card()
            }
            card.addView(ArthSaathiV62Design.text(this, "${cp.optString("name")} • ${cp.optString("mobile")}", 14f, ArthSaathiV62Design.NAVY, true))

            if (!consented) {
                card.addView(ArthSaathiV62Design.text(this, "History / internal score withheld until OTP-verified history-sharing consent is recorded.", 11f, ArthSaathiV62Design.MUTED), 5)
            } else {
                val reps = s.all(V62Store.REPAYMENTS).filter { it.optString("counterpartyId") == id || rel.any { r -> r.optString("id") == it.optString("relationshipId") } }
                val exposure = rel.filter { it.optString("status") != "CLOSED" }.sumOf { it.optDouble("outstanding", it.optDouble("amount", it.optDouble("principal", 0.0))) }
                val repaid = reps.sumOf { it.optDouble("principal", it.optDouble("amount", 0.0)) }
                val score = V62Integration.score(this, id)
                val overdue = rel.sumOf { it.optDouble("overdueAmount", 0.0) }
                card.addView(ArthSaathiV62Design.text(this, "Internal Score: $score / 900\nActive exposure: ₹${"%.2f".format(Locale.US, exposure)}\nRecorded repaid: ₹${"%.2f".format(Locale.US, repaid)}\nOverdue recorded: ₹${"%.2f".format(Locale.US, overdue)}\nRelationships: ${rel.size}", 11f, ArthSaathiV62Design.MUTED), 5)
                card.addView(ArthSaathiV62Design.text(this, explain(score, exposure, repaid, overdue), 10f, ArthSaathiV62Design.TEAL), 5)
            }
            add(card, 7)
        }
        add(ArthSaathiV62Design.button(this, "BACK", ArthSaathiV62Design.NAVY) { finish() }, 10)
        scroll.post { scroll.scrollTo(0,0) }
    }

    private fun explain(score: Int, exposure: Double, repaid: Double, overdue: Double): String = when {
        overdue > 0 -> "Risk signal: recorded overdue amount exists. Review before extending additional credit."
        exposure > 0 && repaid <= 0 -> "Limited repayment history is recorded against current exposure."
        score >= 800 -> "Positive signal: strong recorded repayment relative to exposure."
        else -> "Score reflects recorded exposure, repayment history and overdue/default signals only."
    }
}
