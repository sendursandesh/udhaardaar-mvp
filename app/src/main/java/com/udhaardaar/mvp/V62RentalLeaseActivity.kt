package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import org.json.JSONObject

class V62RentalLeaseActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d by lazy { resources.displayMetrics.density }
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16.dp, 8.dp, 16.dp, 28.dp); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private val Int.dp: Int get() = (this * d).toInt()
    private var doc = JSONObject()
    private var extracted = emptyList<V62DocumentIntelligence.Field>()
    private val fields = listOf("Lessor / Landlord", "Lessee / Tenant", "Property / premises description", "Monthly rent", "Security deposit", "Lease start date", "Lease end date", "Rent due date", "Escalation / revision", "Lock-in period", "Notice period", "Maintenance responsibility", "Utilities responsibility", "Late-payment terms", "Renewal terms", "Termination terms", "Special conditions")

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); render() }
    private fun render() {
        root.removeAllViews()
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Rental & Lease", "Read • Verify • Track"), 2)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "Landlord/lessor or tenant/lessee records are linked to a V6.2 relationship so rent obligations, documents and alerts can be consumed by MIS and AI.", 10f, ArthSaathiV62Design.MUTED), 8)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "SCAN LEASE DEED / RENT AGREEMENT", ArthSaathiV62Design.TEAL) { pick() }, 8)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "MANUAL ENTRY — ALL CRITICAL TERMS", ArthSaathiV62Design.BLUE) { showForm() }, 5)
        if (extracted.isNotEmpty()) { ArthSaathiV62Design.add(root, ArthSaathiV62Design.section(this, "CRITICAL TERMS FLAGGED"), 10); extracted.filter { it.critical }.forEach { ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "⚠ ${it.name}: ${it.value}\n${it.reason} • ${(it.confidence * 100).toInt()}% confidence", 11f, ArthSaathiV62Design.RED, true), 4) } }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }
    private fun pick() { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); addCategory(Intent.CATEGORY_OPENABLE) }, 42) }
    override fun onActivityResult(r: Int, c: Int, data: Intent?) { super.onActivityResult(r, c, data); if (r == 42 && c == Activity.RESULT_OK && data?.data != null) { doc = V62Documents.retain(this, data.data!!, "LEASE_DEED"); V62DocumentScanner.scan(this, data.data!!, { text -> extracted = V62DocumentIntelligence.analyse("LEASE_DEED", text); runOnUiThread { showForm() } }, { runOnUiThread { Toast.makeText(this, "Reading failed; manual verification available.", Toast.LENGTH_LONG).show(); showForm() } }) } }
    private fun showForm() {
        root.removeAllViews(); ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Verify Lease Terms", "Document proposal → your confirmation"), 2)
        val edits = linkedMapOf<String, EditText>(); fields.forEach { k -> val e = ArthSaathiV62Design.input(this, k); extracted.firstOrNull { it.name == k }?.let { e.setText(it.value) }; edits[k] = e; ArthSaathiV62Design.add(root, e, 4) }
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.button(this, "SAVE VERIFIED AGREEMENT", ArthSaathiV62Design.GREEN) {
            val id = V62Store.id("LEASE")
            val o = JSONObject().apply { put("id", id); put("ownerUserId", V62Integration.currentUserId(this@V62RentalLeaseActivity)); put("side", "USER"); put("documentId", doc.optString("id")); put("verifiedAt", System.currentTimeMillis()); put("status", "ACTIVE") }
            edits.forEach { (k, e) -> o.put(k, e.text.toString().trim()) }
            s.add(V62Store.RENTALS, o)
            val relId = V62Store.id("REL")
            s.add(V62Store.RELATIONSHIPS, JSONObject().apply { put("id", relId); put("ownerUserId", V62Integration.currentUserId(this@V62RentalLeaseActivity)); put("type", "RENTAL"); put("rentalId", id); put("status", "ACTIVE"); put("outstanding", 0.0); put("createdAt", System.currentTimeMillis()) })
            V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED, relId))
            val rentDue = edits["Rent due date"]?.text.toString().trim(); val end = edits["Lease end date"]?.text.toString().trim()
            if (rentDue.isNotBlank()) V62Integration.addAlert(this, "RENT_DUE", "Rent due date: $rentDue", id, "ACTION")
            if (end.isNotBlank()) V62Integration.addAlert(this, "LEASE_EXPIRY", "Lease end date: $end", id, "ACTION")
            Toast.makeText(this, "Lease/rental relationship saved.", Toast.LENGTH_SHORT).show(); finish()
        }, 10)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }
}
