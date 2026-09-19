package com.udhaardaar.mvp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale

/** Connected MIS surface: refreshes from the same V6.2 store and event stream as all modules. */
class V62MISActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d by lazy { resources.displayMetrics.density }
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16.dp, 8.dp, 16.dp, 28.dp); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private val Int.dp: Int get() = (this * d).toInt()
    private fun add(v: View, top: Int = 7) = ArthSaathiV62Design.add(root, v, top)
    private fun money(x: Double) = "₹${String.format(Locale.US, "%,.0f", x)}"
    private val eventListener: (V62Event) -> Unit = { runOnUiThread { if (!isFinishing) render() } }

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); render() }
    override fun onStart() { super.onStart(); V62EventBus.subscribe(eventListener) }
    override fun onStop() { V62EventBus.unsubscribe(eventListener); super.onStop() }
    override fun onResume() { super.onResume(); if (!isFinishing) render() }

    private fun render() {
        root.removeAllViews()
        add(ArthSaathiV62Design.title(this, "MIS & Financial Intelligence", "See the complete connected financial picture"), 2)
        add(ArthSaathiV62Design.text(this, "Live from the V6.2 source-of-truth records. Changes in assets, repayments, insurance, charges, savings and credit automatically refresh this view.", 10f, ArthSaathiV62Design.MUTED), 5)
        val m = V62MisEngine.metrics(this)
        val owner = V62Integration.currentUserId(this)
        val a = s.all(V62Store.ASSETS).filter { it.optString("ownerUserId", "") == owner }
        val p = s.all(V62Store.INSURANCE).filter { it.optString("ownerUserId", "") == owner }
        val total = m.optDouble("assetValue"); val ret = m.optDouble("interestReceived"); val charges = m.optDouble("charges")
        val saved = m.optDouble("appSavings"); val idle = m.optDouble("idleFunds"); val liabilities = m.optDouble("liabilities"); val exposure = m.optDouble("informalCreditExposure")
        val benefitValue = m.optDouble("completedBenefitValue"); val refundValue = m.optDouble("completedRefundValue"); val recoveryValue = m.optDouble("completedRecoveryValue"); val valueGenerated = m.optDouble("valueGenerated"); val ttmmOutstanding = m.optDouble("ttmmOutstanding")
        listOf("CURRENT ASSETS" to money(total), "INTEREST RECEIVED" to money(ret), "CHARGES" to money(charges), "APP SAVINGS" to money(saved), "IDLE FUNDS" to money(idle), "LIABILITIES" to money(liabilities), "INFORMAL CREDIT OUTSTANDING" to money(exposure), "TTMM OPEN DUES" to money(ttmmOutstanding)).forEach { metric(it.first, it.second) }
        add(ArthSaathiV62Design.section(this, "ASSET ALLOCATION"), 10)
        val current = a.filter { it.optBoolean("currentAsset", true) && it.optString("lifecycleStatus", "ACTIVE") !in setOf("SOLD", "TRANSFERRED", "GIFTED", "DISPOSED") }
        val g = current.groupBy { it.optString("type", "Other") }
        if (g.isNotEmpty()) { add(V62DonutChart(this, g.values.map { it.sumOf { q -> q.optDouble("value", 0.0) }.toFloat() }), 4); g.entries.forEachIndexed { i, e -> row(e.key, money(e.value.sumOf { q -> q.optDouble("value", 0.0) }), "${e.value.size} current item(s)", i % 2 == 0) } }
        else add(ArthSaathiV62Design.text(this, "No current assets recorded.", 11f, ArthSaathiV62Design.MUTED), 4)
                add(ArthSaathiV62Design.section(this, "PORTFOLIO & FINANCIAL POSITION"), 14)
        val mix = listOf(total.toFloat(), liabilities.toFloat(), exposure.toFloat(), ttmmOutstanding.toFloat()).map { kotlin.math.max(0f, it) }
        if (mix.any { it > 0f }) {
            add(V62DonutChart(this, mix), 4)
            row("Assets", money(total), "Current portfolio", true)
            row("Liabilities", money(liabilities), "Outstanding obligations", false)
            row("Informal credit", money(exposure), "Open credit exposure", true)
            row("Group dues", money(ttmmOutstanding), "TTMM open contributions", false)
        } else add(ArthSaathiV62Design.text(this, "No portfolio values recorded yet.", 11f, ArthSaathiV62Design.MUTED), 4)
        add(ArthSaathiV62Design.section(this, "COMPLETED BENEFITS • REFUNDS • RECOVERIES"), 14)
        head("Value stream", "Completed", "Actual value generated")
        row("Government / other benefits", money(benefitValue), "Successful records only", true)
        row("Refunds recovered", money(refundValue), "Completed charge claims", false)
        row("Claims / recoveries", money(recoveryValue), "Completed claim records", true)
        row("TOTAL VALUE GENERATED", money(valueGenerated), "Benefits + refunds + recoveries", false)
        add(ArthSaathiV62Design.section(this, "PERFORMANCE • RISK • OPPORTUNITY"), 14); head("Metric", "Recorded", "Basis")
        row("Average current investment", if (current.isEmpty()) "₹0" else money(total / current.size), "Current assets", true)
        row("Risk tags", "${current.count { it.optString("risk").isNotBlank() }} / ${current.size}", "Asset records", false)
        row("Insurance policies", p.size.toString(), "Protection vault", true)
        val highestYield = current.mapNotNull { it.optDouble("yieldPercent", Double.NaN).takeUnless { x -> x.isNaN() } }.maxOrNull() ?: 0.0
        row("Estimated idle opportunity", money(idle * highestYield / 100), "Idle × highest recorded yield (${String.format(Locale.US, "%.2f", highestYield)}%)", false)
        add(ArthSaathiV62Design.section(this, "RETURNS • CHARGES • SAVINGS"), 14); head("Measure", "Amount", "Source")
        row("Interest / returns", money(ret), "Repayments", true); row("Charges", money(charges), "Connected records", false); row("App savings", money(saved), "Savings ledger", true); row("Historical asset value", money(m.optDouble("historicalAssetValue")), "Includes closed assets", false)
        add(ArthSaathiV62Design.text(this, "Closed/sold assets remain historical but are excluded from current net-worth asset value. Opportunity-cost estimates use only a yield explicitly recorded on an asset.", 10f, ArthSaathiV62Design.MUTED), 14)
        add(ArthSaathiV62Design.text(this, "Value generated shows only successfully completed records; pending or rejected benefits, refunds and claims are excluded.", 10f, ArthSaathiV62Design.MUTED), 14)
        add(ArthSaathiV62Design.button(this, "REFRESH INTELLIGENCE", ArthSaathiV62Design.TEAL) { render() }, 10)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }
    private fun metric(k: String, v: String) { val b=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(12.dp,9.dp,12.dp,9.dp);background=ArthSaathiV62Design.card(Color.WHITE,14,d)};b.addView(ArthSaathiV62Design.text(this,k,9f,ArthSaathiV62Design.MUTED,true));b.addView(ArthSaathiV62Design.text(this,v,18f,ArthSaathiV62Design.NAVY,true));add(b,6) }
    private fun head(a:String,b:String,c:String){val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(8.dp,7.dp,8.dp,7.dp);background=ArthSaathiV62Design.card(0xffeef3f8.toInt(),10,d)};listOf(a,b,c).forEachIndexed{i,x->r.addView(ArthSaathiV62Design.text(this,x,9f,ArthSaathiV62Design.MUTED,true),LinearLayout.LayoutParams(0,-2,if(i==0)1.3f else 1f))};add(r,4)}
    private fun row(a:String,b:String,c:String,even:Boolean){val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(8.dp,8.dp,8.dp,8.dp);background=ArthSaathiV62Design.card(if(even)Color.WHITE else 0xfff8fafc.toInt(),10,d)};listOf(a,b,c).forEachIndexed{i,x->r.addView(ArthSaathiV62Design.text(this,x,10f,ArthSaathiV62Design.NAVY,i==0),LinearLayout.LayoutParams(0,-2,if(i==0)1.3f else 1f))};add(r,2)}
}
