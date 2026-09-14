package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class V62ChargeCheckActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private val store by lazy { V5LocalStore(this) }
    private lateinit var root: LinearLayout
    private var sanctionText = ""
    private var statementText = ""
    private var sanctionUri = ""
    private var statementUri = ""

    private fun add(v: View, top: Int = 8) = ArthSaathiV62Design.add(root, v, top)
    private fun btn(s: String, c: Int, go: () -> Unit) = ArthSaathiV62Design.button(this, s, c, go)

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); render() }

    private fun shell() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding((16 * d).toInt(), (12 * d).toInt(), (16 * d).toInt(), (90 * d).toInt()); setBackgroundColor(ArthSaathiV62Design.BG) }
        add(ArthSaathiV62Design.title(this, "ChargeCheck", "Scan • Smart Read • Compare • Verify"), 0)
        add(ArthSaathiV62Design.text(this, "Compare sanctioned terms with actual debits. Originals are retained and every extracted amount remains a reviewable candidate.", 10f, ArthSaathiV62Design.MUTED), 5)
    }

    private fun render() {
        shell()
        add(ArthSaathiV62Design.section(this, "1 • SANCTION EVIDENCE"), 10)
        add(btn("SCAN / ATTACH SANCTION LETTER", ArthSaathiV62Design.TEAL) { pick(101) })
        add(ArthSaathiV62Design.text(this, if (sanctionUri.isBlank()) "Not attached" else "Retained: $sanctionUri", 10f, ArthSaathiV62Design.GREEN, true), 5)
        add(ArthSaathiV62Design.section(this, "2 • ACCOUNT STATEMENT"), 12)
        add(btn("SCAN / ATTACH ACCOUNT STATEMENT", ArthSaathiV62Design.BLUE) { pick(102) })
        add(ArthSaathiV62Design.text(this, if (statementUri.isBlank()) "Not attached" else "Retained: $statementUri", 10f, ArthSaathiV62Design.GREEN, true), 5)
        add(ArthSaathiV62Design.section(this, "3 • SMART READER"), 12)
        add(ArthSaathiV62Design.text(this, "The reader searches OCR text for interest, fee, tax, penalty, insurance and other charge candidates, retaining source evidence for review.", 10f, ArthSaathiV62Design.MUTED), 4)
        add(btn("READ BOTH DOCUMENTS", ArthSaathiV62Design.GOLD) { if (sanctionText.isBlank() || statementText.isBlank()) Toast.makeText(this, "Attach both documents first.", Toast.LENGTH_LONG).show() else reviewResults() })
        add(btn("BACK TO FINANCIAL CENTRE", ArthSaathiV62Design.NAVY) { finish() }, 12)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun pick(request: Int) { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); addCategory(Intent.CATEGORY_OPENABLE) }, request) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return
        val uri = data.data!!
        val category = if (requestCode == 101) "CHARGECHECK_SANCTION" else "CHARGECHECK_STATEMENT"
        val retained = runCatching { V62Documents.retain(this, uri, category) }.getOrNull()
        if (requestCode == 101) sanctionUri = retained?.optString("uri").orEmpty().ifBlank { uri.toString() } else statementUri = retained?.optString("uri").orEmpty().ifBlank { uri.toString() }
        Toast.makeText(this, "Original document retained. Reading OCR…", Toast.LENGTH_SHORT).show()
        V62DocumentScanner.scan(this, uri, { text -> if (requestCode == 101) sanctionText = text else statementText = text; runOnUiThread { render() } }, { e -> runOnUiThread { Toast.makeText(this, "Could not read document: ${e.message}", Toast.LENGTH_LONG).show(); render() } })
    }

    private fun reviewResults() {
        val sanctioned = V62SmartChargeCheckReader.readSanction(sanctionText)
        val debited = V62SmartChargeCheckReader.readStatement(statementText)
        val categories = (sanctioned.findings.map { it.category } + debited.findings.map { it.category }).distinct()
        val rows = JSONArray()
        val text = StringBuilder("CHARGECHECK REVIEW\n\n")
        text.append("Sanction candidates: ${sanctioned.findings.size}\nActual debit candidates: ${debited.findings.size}\n\n")
        if (categories.isEmpty()) text.append("No candidates were confidently identified. Review originals manually.\n")
        categories.forEach { category ->
            val sv = sanctioned.findings.filter { it.category == category }.sumOf { it.amount }
            val av = debited.findings.filter { it.category == category }.sumOf { it.amount }
            val variance = av - sv
            text.append("${V62SmartChargeCheckReader.categoryLabel(category)}\n  Sanctioned: ₹${money(sv)}\n  Actually debited: ₹${money(av)}\n  Variance: ₹${money(variance)}\n\n")
            rows.put(JSONObject().apply { put("category", category); put("sanctioned", sv); put("debited", av); put("variance", variance); put("sanctionEvidence", JSONArray(sanctioned.findings.filter { it.category == category }.map { it.sourceText })); put("debitEvidence", JSONArray(debited.findings.filter { it.category == category }.map { it.sourceText })) })
        }
        text.append("\nAll values are extraction candidates until you confirm them.")
        AlertDialog.Builder(this).setTitle("Smart ChargeCheck Review").setMessage(text.toString()).setNegativeButton("CANCEL", null).setPositiveButton("CONFIRM & SAVE") { _, _ -> saveResult(rows) }.show()
    }

    private fun saveResult(rows: JSONArray) {
        val id = V62Store.id("CC")
        val record = JSONObject().apply { put("id", id); put("ownerUserId", V62Integration.currentUserId(this@V62ChargeCheckActivity)); put("sanctionDocument", sanctionUri); put("statementDocument", statementUri); put("sanctionText", sanctionText.take(20000)); put("statementText", statementText.take(20000)); put("lineItems", rows); put("status", "USER_CONFIRMED"); put("createdAt", System.currentTimeMillis()) }
        V62Store.add(this@V62ChargeCheckActivity, V62Store.CHARGECHECK, record)
        V62EventBus.publish(V62Event(V62Events.CHARGECHECK_CHANGED, id))
        val variance = (0 until rows.length()).sumOf { rows.optJSONObject(it)?.optDouble("variance", 0.0) ?: 0.0 }
        if (variance > 0) V62Integration.addAlert(this@V62ChargeCheckActivity, "CHARGECHECK_VARIANCE", "ChargeCheck found confirmed excess/variance of ₹${money(variance)} for review.", id, "ACTION")
        Toast.makeText(this, "ChargeCheck saved with evidence and extracted line items.", Toast.LENGTH_LONG).show()
    }

    private fun money(v: Double) = String.format(Locale.US, "%.2f", v)
}
