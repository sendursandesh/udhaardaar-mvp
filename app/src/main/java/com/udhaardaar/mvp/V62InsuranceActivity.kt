package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import org.json.JSONObject

class V62InsuranceActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d by lazy { resources.displayMetrics.density }
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16.dp, 8.dp, 16.dp, 28.dp); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private val Int.dp: Int get() = (this * d).toInt()
    private var doc = JSONObject()
    private var extracted = emptyList<V62DocumentIntelligence.Field>()
    private val fields = listOf("Policy number", "Insurer", "Policy type", "Policyholder", "Insured person", "Premium / frequency", "Sum assured", "Start date", "End / maturity date", "Next premium due", "Nominee", "Status", "Exclusions / waiting period", "Riders / benefits", "Claim contact", "Loan / surrender value")

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); render() }
    private fun render() {
        root.removeAllViews()
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Insurance & Protection", "Read • Verify • Protect"), 2)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "Scan the complete policy. OCR flags critical obligations and limitations. AI output is review-first and every field remains editable.", 10f, ArthSaathiV62Design.MUTED), 8)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "SCAN / ATTACH POLICY", ArthSaathiV62Design.TEAL) { pick() }, 8)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "MANUAL ENTRY — ALL IMPORTANT FIELDS", ArthSaathiV62Design.BLUE) { showForm() }, 5)
        if (extracted.isNotEmpty()) {
            ArthSaathiV62Design.add(root, ArthSaathiV62Design.section(this, "CRITICAL TERMS FLAGGED"), 10)
            extracted.filter { it.critical }.forEach { ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "⚠ ${it.name}: ${it.value}\n${it.reason} • ${(it.confidence * 100).toInt()}% confidence", 11f, ArthSaathiV62Design.RED, true), 4) }
        }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun pick() { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); addCategory(Intent.CATEGORY_OPENABLE) }, 41) }
    override fun onActivityResult(r: Int, c: Int, data: Intent?) {
        super.onActivityResult(r, c, data)
        if (r == 41 && c == Activity.RESULT_OK && data?.data != null) {
            doc = V62Documents.retain(this, data.data!!, "INSURANCE_POLICY")
            V62DocumentScanner.scan(this, data.data!!, { text -> extracted = V62DocumentIntelligence.analyse("INSURANCE_POLICY", text); runOnUiThread { showForm() } }, { runOnUiThread { Toast.makeText(this, "Document reading failed; manual verification is available.", Toast.LENGTH_LONG).show(); showForm() } })
        }
    }

    private fun showForm() {
        root.removeAllViews()
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Verify Insurance Policy", "AI proposal → your confirmation"), 2)
        if (doc.length() > 0) ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "Original document retained • every extracted field must be reviewed", 10f, ArthSaathiV62Design.GREEN, true), 8)
        val map = linkedMapOf<String, EditText>()
        fields.forEach { k -> val e = ArthSaathiV62Design.input(this, k); extracted.firstOrNull { it.name == k }?.let { e.setText(it.value) }; map[k] = e; ArthSaathiV62Design.add(root, e, 4) }
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "SAVE VERIFIED POLICY", ArthSaathiV62Design.GREEN) {
            val id = if (doc.optString("id").isBlank()) V62Store.id("POL") else doc.optString("id")
            val o = JSONObject().apply { put("id", id); put("ownerUserId", V62Integration.currentUserId(this@V62InsuranceActivity)); put("documentId", doc.optString("id")); put("status", "VERIFIED"); put("verifiedAt", System.currentTimeMillis()) }
            map.forEach { (k, e) -> o.put(k, e.text.toString().trim()) }
            s.replace(V62Store.INSURANCE, o)
            V62EventBus.publish(V62Event(V62Events.POLICY_CHANGED, id))
            val due = map["Next premium due"]?.text.toString().trim()
            val maturity = map["End / maturity date"]?.text.toString().trim()
            if (due.isNotBlank()) V62Integration.addAlert(this, "INSURANCE_PREMIUM_DUE", "Insurance premium due: $due for policy ${map["Policy number"]?.text}", id, "ACTION")
            if (maturity.isNotBlank()) V62Integration.addAlert(this, "INSURANCE_EXPIRY", "Insurance policy maturity/expiry: $maturity for policy ${map["Policy number"]?.text}", id, "ACTION")
            Toast.makeText(this, "Policy saved to Protection Vault.", Toast.LENGTH_SHORT).show(); finish()
        }, 12)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }
}
