package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import org.json.JSONObject

class V62AssetVaultActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d by lazy { resources.displayMetrics.density }
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16.dp, 8.dp, 16.dp, 28.dp); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private val Int.dp: Int get() = (this * d).toInt()
    private var doc = JSONObject()
    private var kind = "PROPERTY_PAPER"
    private var extracted = emptyList<V62DocumentIntelligence.Field>()
    private var savedAssetId = ""
    private val fields = listOf("Asset name / description", "Owner / account holder", "Property address / description", "Account number", "Bank / branch", "IFSC", "Registration / deed number", "Area", "Current value ₹", "Outstanding liability ₹", "Nominee", "Risk", "Yield %", "Idle / inactive", "Evidence notes")

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); render() }

    private fun render() {
        root.removeAllViews()
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Asset & Liability Vault", "Capture • Verify • Protect"), 2)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "Store the original document and turn it into structured, searchable financial data. AI extraction is review-first and never silently overwrites your records.", 10f, ArthSaathiV62Design.MUTED), 8)
        val type = Spinner(this).apply {
            adapter = ArrayAdapter(this@V62AssetVaultActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Property paper", "Bank passbook", "Other asset evidence"))
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) { kind = when (pos) { 0 -> "PROPERTY_PAPER"; 1 -> "BANK_PASSBOOK"; else -> "OTHER" } }
            }
        }
        ArthSaathiV62Design.add(root, type, 6)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "SCAN / ATTACH DOCUMENT", ArthSaathiV62Design.TEAL) { pick() }, 8)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "MANUAL ENTRY — ALL IMPORTANT FIELDS", ArthSaathiV62Design.BLUE) { showForm() }, 5)
        if (extracted.isNotEmpty()) {
            ArthSaathiV62Design.add(root, ArthSaathiV62Design.section(this, "CRITICAL DETAILS FOUND"), 10)
            extracted.filter { it.critical }.forEach { ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "⚠ ${it.name}: ${it.value}\n${it.reason} • ${(it.confidence * 100).toInt()}%", 11f, ArthSaathiV62Design.RED, true), 4) }
        }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun pick() { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); addCategory(Intent.CATEGORY_OPENABLE) }, 43) }

    override fun onActivityResult(r: Int, c: Int, data: Intent?) {
        super.onActivityResult(r, c, data)
        if (r == 43 && c == Activity.RESULT_OK && data?.data != null) {
            doc = V62Documents.retain(this, data.data!!, kind)
            V62DocumentScanner.scan(this, data.data!!, { text -> extracted = V62DocumentIntelligence.analyse(kind, text); runOnUiThread { showForm() } }, { runOnUiThread { Toast.makeText(this, "Reading failed; manual entry available.", Toast.LENGTH_LONG).show(); showForm() } })
        }
    }

    private fun showForm() {
        root.removeAllViews()
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Verify Vault Record", "Document proposal → your confirmation"), 2)
        val m = linkedMapOf<String, EditText>()
        fields.forEach { k -> val e = ArthSaathiV62Design.input(this, k); extracted.firstOrNull { it.name == k }?.let { e.setText(it.value) }; m[k] = e; ArthSaathiV62Design.add(root, e, 4) }
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.section(this, "ASSET LIFECYCLE"), 10)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "Assets are never deleted. Sell or release actions close the current asset position while preserving its complete historical trail.", 10f, ArthSaathiV62Design.MUTED), 5)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "SAVE VERIFIED RECORD", ArthSaathiV62Design.GREEN) {
            val id = V62Store.id("AST")
            val o = JSONObject().apply {
                put("id", id); put("type", kind); put("documentId", doc.optString("id")); put("value", m["Current value ₹"]?.text.toString().toDoubleOrNull() ?: 0.0); put("risk", m["Risk"]?.text.toString()); put("yieldPercent", m["Yield %"]?.text.toString().toDoubleOrNull() ?: 0.0); put("idle", m["Idle / inactive"]?.text.toString().equals("true", true)); put("nominee", m["Nominee"]?.text.toString()); put("createdAt", System.currentTimeMillis()); put("verified", true); put("lifecycleStatus", V62AssetLifecycle.ACTIVE); put("currentAsset", true)
            }
            m.forEach { (k, e) -> o.put(k, e.text.toString().trim()) }
            s.add(ArthSaathiV62Core.ASSETS, o); savedAssetId = id
            V62EventBus.publish(V62Event.ASSET_CHANGED)
            Toast.makeText(this, "Vault record saved.", Toast.LENGTH_SHORT).show()
            showLifecycleActions(id)
        }, 12)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun showLifecycleActions(assetId: String) {
        root.removeAllViews()
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Asset Lifecycle", "Manage • Release • Sell"), 2)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "Asset ID: $assetId\nStatus: ACTIVE\n\nChoose an action. Historical data remains preserved after the asset is closed.", 11f, ArthSaathiV62Design.MUTED), 10)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "SELL / TRANSFER OUT", ArthSaathiV62Design.BLUE) { showSellDialog(assetId) }, 8)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "RELEASE CHARGE / ENCUMBRANCE", ArthSaathiV62Design.TEAL) { showReleaseDialog(assetId) }, 8)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "DONE — KEEP AS ACTIVE", ArthSaathiV62Design.GREEN) { finish() }, 8)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun showSellDialog(assetId: String) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8.dp, 4.dp, 8.dp, 4.dp) }
        val date = ArthSaathiV62Design.input(this, "Sale date")
        val value = ArthSaathiV62Design.input(this, "Sale value ₹")
        val buyer = ArthSaathiV62Design.input(this, "Buyer name / entity")
        val payment = Spinner(this).apply { adapter = ArrayAdapter(this@V62AssetVaultActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Received", "Partially received", "Pending")) }
        box.addView(date); box.addView(value); box.addView(buyer); box.addView(payment)
        AlertDialog.Builder(this).setTitle("Sell Asset").setView(box).setPositiveButton("CONFIRM SALE") { _, _ ->
            val ok = V62AssetLifecycle.sell(this, assetId, date.text.toString().trim(), value.text.toString().toDoubleOrNull() ?: 0.0, buyer.text.toString(), payment.selectedItem.toString())
            Toast.makeText(this, if (ok) "Asset marked SOLD; history preserved." else "Sale could not be recorded.", Toast.LENGTH_LONG).show()
            if (ok) finish()
        }.setNegativeButton("CANCEL", null).show()
    }

    private fun showReleaseDialog(assetId: String) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8.dp, 4.dp, 8.dp, 4.dp) }
        val date = ArthSaathiV62Design.input(this, "Release date")
        val reason = ArthSaathiV62Design.input(this, "Reason for release")
        val from = ArthSaathiV62Design.input(this, "Released from whom / entity")
        val reference = ArthSaathiV62Design.input(this, "Loan / charge reference")
        box.addView(date); box.addView(reason); box.addView(from); box.addView(reference)
        AlertDialog.Builder(this).setTitle("Release Charge / Encumbrance").setView(box).setPositiveButton("CONFIRM RELEASE") { _, _ ->
            val ok = V62AssetLifecycle.release(this, assetId, date.text.toString().trim(), reason.text.toString(), from.text.toString(), reference.text.toString())
            Toast.makeText(this, if (ok) "Charge released; lifecycle history preserved." else "Release could not be recorded.", Toast.LENGTH_LONG).show()
            if (ok) finish()
        }.setNegativeButton("CANCEL", null).show()
    }
}
