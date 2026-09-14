package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.util.Locale

class V62LegacyLegalAIActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d by lazy { resources.displayMetrics.density }
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16.dp, 8.dp, 16.dp, 28.dp); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private val Int.dp: Int get() = (this * d).toInt()

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); render() }
    override fun onResume() { super.onResume(); if (!isFinishing) render() }
    private fun add(v: android.view.View, gap: Int = 8) = ArthSaathiV62Design.add(root, v, gap)

    private fun render() {
        root.removeAllViews(); add(ArthSaathiV62Design.title(this, "Legacy • Legal • AI", "Protect today • Prepare tomorrow"), 2)
        add(ArthSaathiV62Design.text(this, "These services consume the same V6.2 vault records. AI provides explainable assistance only; legal and investment decisions remain with you/professionals.", 10f, ArthSaathiV62Design.MUTED), 8)
        card("WILL & NOMINATION", "Versioned will drafts linked to selected assets and beneficiaries") { willDialog() }
        card("CLAIM ASSISTANCE", "Build a claim case from policies, assets and retained evidence") { claimDialog() }
        card("LEGAL ASSISTANCE", "Store/search local legal contacts by domain and expertise") { legalDialog() }
        card("AI FINANCIAL ADVISOR", "Explainable recommendations from liquidity, debt, protection and idle funds") { aiDialog() }
        card("ALERTS", "Review generated renewal, due-date and ChargeCheck alerts") { alertsDialog() }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun card(title: String, sub: String, click: () -> Unit) { val b = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(14.dp, 12.dp, 14.dp, 12.dp); background = ArthSaathiV62Design.card(); setOnClickListener { click() } }; b.addView(ArthSaathiV62Design.text(this, title, 13f, ArthSaathiV62Design.NAVY, true)); b.addView(ArthSaathiV62Design.text(this, sub, 10f, ArthSaathiV62Design.MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = 5.dp }); add(b) }

    private fun willDialog() {
        val assets = s.all(V62Store.ASSETS).filter { it.optBoolean("currentAsset", true) }
        val beneficiary = ArthSaathiV62Design.input(this, "Beneficiary / nominee name *"); val executor = ArthSaathiV62Design.input(this, "Executor / trusted person"); val instructions = ArthSaathiV62Design.input(this, "Distribution / legacy instructions *")
        val selected = mutableSetOf<String>(); val assetBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (assets.isEmpty()) assetBox.addView(ArthSaathiV62Design.text(this, "No current assets recorded yet. You may still create an instruction-only draft.", 10f, ArthSaathiV62Design.MUTED))
        assets.forEach { a -> val cb = CheckBox(this).apply { text = "${a.optString("Asset name / description", a.optString("type"))} • ₹${"%.2f".format(Locale.US, a.optDouble("value"))}"; setOnCheckedChangeListener { _, checked -> if (checked) selected.add(a.optString("id")) else selected.remove(a.optString("id")) } }; assetBox.addView(cb) }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16.dp, 0, 16.dp, 0); addView(beneficiary); addView(executor); addView(ArthSaathiV62Design.text(this@V62LegacyLegalAIActivity, "LINK ASSETS", 11f, ArthSaathiV62Design.TEAL, true)); addView(assetBox); addView(instructions) }
        AlertDialog.Builder(this).setTitle("Guided Will Draft").setMessage("Draft only — independent legal review and lawful execution are required.").setView(box).setNegativeButton("CANCEL", null).setPositiveButton("SAVE VERSIONED DRAFT") { _, _ ->
            if (beneficiary.text.isBlank() || instructions.text.isBlank()) { Toast.makeText(this, "Beneficiary and instructions are required.", Toast.LENGTH_LONG).show(); return@setPositiveButton }
            val versions = s.all(V62Store.WILLS).filter { it.optString("willGroupId") == beneficiary.text.toString().trim() }.size + 1; val id = V62Store.id("WILL"); s.add(V62Store.WILLS, JSONObject().apply { put("id", id); put("willGroupId", beneficiary.text.toString().trim()); put("version", versions); put("ownerUserId", V62Integration.currentUserId(this@V62LegacyLegalAIActivity)); put("beneficiary", beneficiary.text.toString().trim()); put("executor", executor.text.toString().trim()); put("assetIds", selected.joinToString("|")); put("instructions", instructions.text.toString().trim()); put("status", "DRAFT"); put("createdAt", System.currentTimeMillis()) }); val nomId=V62Store.id("NOM"); s.add(V62Store.NOMINEES,JSONObject().apply{put("id",nomId);put("ownerUserId",V62Integration.currentUserId(this@V62LegacyLegalAIActivity));put("name",beneficiary.text.toString().trim());put("assetIds",selected.joinToString("|"));put("source","WILL_DRAFT");put("createdAt",System.currentTimeMillis())}); V62EventBus.publish(V62Event(V62Events.WILL_CHANGED,id)); V62EventBus.publish(V62Event(V62Events.NOMINEE_CHANGED,nomId)); Toast.makeText(this, "Version $versions will draft saved with asset links.", Toast.LENGTH_LONG).show()
        }.show()
    }

    private fun claimDialog() {
        val p=s.all(V62Store.INSURANCE); val a=s.all(V62Store.ASSETS); val docs=s.all(V62Store.DOCUMENTS); val claims=s.all(V62Store.CLAIMS)
        val claimant=ArthSaathiV62Design.input(this,"Claimant name / relation *"); val event=ArthSaathiV62Design.input(this,"Claim event / date *")
        AlertDialog.Builder(this).setTitle("Claim Assistance").setMessage("Available evidence:\nPolicies: ${p.size}\nCurrent/historical assets: ${a.size}\nRetained documents: ${docs.size}\nExisting claim cases: ${claims.size}\n\nCreate a case first; submission remains a separate user/professional decision.").setView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(claimant);addView(event)}).setNegativeButton("CANCEL",null).setPositiveButton("CREATE CLAIM CASE"){_,_->if(claimant.text.isBlank()||event.text.isBlank())Toast.makeText(this,"Claimant and event are required.",Toast.LENGTH_LONG).show()else{val id=V62Store.id("CLAIM");s.add(V62Store.CLAIMS,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62LegacyLegalAIActivity));put("claimant",claimant.text.toString().trim());put("event",event.text.toString().trim());put("policyIds",p.joinToString("|"){it.optString("id")});put("assetIds",a.joinToString("|"){it.optString("id")});put("documentIds",docs.joinToString("|"){it.optString("id")});put("status","DRAFT");put("createdAt",System.currentTimeMillis())});V62EventBus.publish(V62Event(V62Events.CLAIM_CHANGED,id));Toast.makeText(this,"Claim case created from vault evidence.",Toast.LENGTH_LONG).show()}}.show()
    }

    private fun legalDialog() {
        val domain=ArthSaathiV62Design.input(this,"Legal domain / matter *"); val city=ArthSaathiV62Design.input(this,"City / locality *"); val expertise=ArthSaathiV62Design.input(this,"Expertise"); val name=ArthSaathiV62Design.input(this,"Lawyer / firm name *"); val phone=ArthSaathiV62Design.input(this,"Contact number *")
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(domain);addView(city);addView(expertise);addView(name);addView(phone)}
        AlertDialog.Builder(this).setTitle("Legal Assistance Directory").setMessage("Contacts entered here are user-provided unless marked VERIFIED by a connected directory. ArthSaathi does not imply legal endorsement.").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("SAVE CONTACT"){_,_->if(domain.text.isBlank()||city.text.isBlank()||name.text.isBlank()||phone.text.isBlank())Toast.makeText(this,"Domain, locality, name and contact are required.",Toast.LENGTH_LONG).show()else{s.add("v62_legal_contacts",JSONObject().apply{put("id",V62Store.id("LAW"));put("ownerUserId",V62Integration.currentUserId(this@V62LegacyLegalAIActivity));put("domain",domain.text.toString().trim());put("city",city.text.toString().trim());put("expertise",expertise.text.toString().trim());put("name",name.text.toString().trim());put("phone",phone.text.toString().trim());put("verificationStatus","USER_ADDED")});Toast.makeText(this,"Legal contact saved.",Toast.LENGTH_SHORT).show()}}.show()
    }

    private fun aiDialog() {
        val m=V62MisEngine.metrics(this); val lines=mutableListOf<String>(); val idle=m.optDouble("idleFunds",0.0); val liab=m.optDouble("liabilities",0.0); val exposure=m.optDouble("informalCreditExposure",0.0); val policies=s.all(V62Store.INSURANCE).size
        if(idle>0) lines += "• Idle funds ₹${"%.0f".format(Locale.US,idle)}: review whether they should remain idle; no automatic transfer is made." else lines += "• No idle-fund amount is recorded in the current asset vault."
        if(liab>0) lines += "• Liabilities ₹${"%.0f".format(Locale.US,liab)}: review repayment cost and due dates." else lines += "• No liability balance is recorded."
        lines += "• Informal credit outstanding: ₹${"%.0f".format(Locale.US,exposure)}."
        lines += if(policies==0) "• Protection gap signal: no insurance policy is recorded." else "• Protection records: $policies policy/policies recorded; review expiry and exclusions."
        AlertDialog.Builder(this).setTitle("AI Financial Advisor").setMessage("Explainable, record-based suggestions:\n\n${lines.joinToString("\n\n")}\n\nThese are informational suggestions based only on recorded data. No money movement, investment switch or legal action is performed automatically.").setPositiveButton("OK",null).show()
    }

    private fun alertsDialog() { val a=s.all(V62Store.ALERTS).takeLast(20).reversed(); AlertDialog.Builder(this).setTitle("ArthSaathi Alerts").setMessage(if(a.isEmpty())"No generated alerts yet." else a.joinToString("\n\n"){ "${it.optString("severity")}: ${it.optString("message")}" }).setPositiveButton("OK",null).show() }
}
