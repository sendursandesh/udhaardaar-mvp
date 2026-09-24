package com.udhaardaar.mvp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/**
 * ArthSaathi V7 Step 6 — first native migration tranche.
 *
 * These screens use only V7Core/V7Records/V7LocalStore. They never launch
 * V5/V6.2 activities. Legacy-backed modules remain behind V7LegacyAdapter.
 */
class V7NativeModuleActivity : AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()
    private val key get() = intent.getStringExtra("module") ?: "RECORD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render(key)
    }

    private fun shell(title: String, subtitle: String, body: LinearLayout): ScrollView {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(7), dp(10), dp(20))
            setBackgroundColor(ArthSaathiV7Design.CREAM)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ArthSaathiV7Design.bg(this@V7NativeModuleActivity)
            setPadding(dp(9), dp(7), dp(9), dp(10))
        }
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        row.addView(ArthSaathiV7Design.text(this, "‹", 36f, Color.WHITE).apply {
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(dp(40), dp(44)))
        row.addView(ArthSaathiV7Design.brand(this, 23f, true), LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(row)
        header.addView(ArthSaathiV7Design.text(this, title, 21f, Color.WHITE, true))
        header.addView(ArthSaathiV7Design.text(this, subtitle, 10.5f, ArthSaathiV7Design.GOLD_PALE),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
        root.addView(header)
        root.addView(body, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })
        root.addView(ArthSaathiV7Design.goldButton(this, "Back to Financial Command Centre") { finish() },
            LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(14) })
        return ScrollView(this).apply { isFillViewport = true; addView(root) }
    }

    private fun field(hint: String, inputType: Int = android.text.InputType.TYPE_CLASS_TEXT): EditText =
        EditText(this).apply {
            this.hint = hint
            this.inputType = inputType
            setSingleLine(true)
            setPadding(dp(10), 0, dp(10), 0)
            setTextColor(ArthSaathiV7Design.NAVY)
            setHintTextColor(ArthSaathiV7Design.MUTED)
            background = ArthSaathiV7Design.card(this@V7NativeModuleActivity, Color.WHITE, 10)
        }

    private fun button(text: String, action: () -> Unit): Button =
        ArthSaathiV7Design.goldButton(this, text, action)

    private fun render(module: String) {
        when (module) {
            "RECORD" -> record()
            "CREDIT" -> credit()
            "REPAYMENT" -> repayment()
            "ASSETS" -> assets()
            "LIABILITIES" -> liabilities()
            else -> {
                Toast.makeText(this, "Native V7 module not registered: $module", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun record() {
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(ArthSaathiV7Design.section(this, "People & Identity", "V7-owned people records. No legacy storage."))
        val name = field("Full name")
        val mobile = field("10-digit mobile", android.text.InputType.TYPE_CLASS_PHONE)
        val pan = field("PAN (optional)")
        val aadhaar = field("Aadhaar (optional)", android.text.InputType.TYPE_CLASS_NUMBER)
        val gstin = field("GSTIN (optional)")
        listOf(name, mobile, pan, aadhaar, gstin).forEach {
            body.addView(it, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })
        }
        body.addView(button("Save Person in V7") {
            val n = name.text.toString().trim()
            val m = mobile.text.toString().trim()
            if (n.isBlank()) { name.error = "Name is required"; return@button }
            if (m.length != 10 || !m.all(Char::isDigit)) { mobile.error = "Enter exactly 10 digits"; return@button }
            V7Records.person(this, n, m, pan.text.toString(), aadhaar.text.toString(), gstin.text.toString())
            Toast.makeText(this, "Person saved in V7-owned Record.", Toast.LENGTH_SHORT).show()
            renderRecordList(body)
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })
        renderRecordList(body)
        setContentView(shell("Record & Identity", "People, identity and the canonical V7 record.", body))
    }

    private fun renderRecordList(body: LinearLayout) {
        body.findViewWithTag<View>("v7_record_list")?.let { body.removeView(it) }
        val box = LinearLayout(this).apply {
            tag = "v7_record_list"; orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(8), dp(2), 0)
        }
        box.addView(ArthSaathiV7Design.section(this, "Saved People", "Live from v7_people."))
        val people = V7Core.all(this, V7Core.Keys.PEOPLE)
        if (people.isEmpty()) box.addView(ArthSaathiV7Design.text(this, "No people recorded yet.", 10f, ArthSaathiV7Design.MUTED))
        people.takeLast(20).reversed().forEach { p ->
            box.addView(ArthSaathiV7Design.text(this,
                p.optString("name") + "  •  " + p.optString("mobile"), 12f, ArthSaathiV7Design.NAVY, true),
                LinearLayout.LayoutParams(-1, dp(38)))
        }
        body.addView(box)
    }

    private fun credit() {
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(ArthSaathiV7Design.section(this, "Register Credit", "V7-native relationship record. Repayment mutations remain consent-controlled."))
        val people = V7Core.all(this, V7Core.Keys.PEOPLE)
        val labels = if (people.isEmpty()) listOf("No person — create one in Record") else people.map { it.optString("name") + " • " + it.optString("mobile") }
        val spinner = Spinner(this).apply { adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item, labels) }
        body.addView(spinner, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })
        val amount = field("Amount", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val roi = field("ROI %", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val purpose = field("Purpose")
        val repayment = field("Repayment structure (EMI / Principal + Interest)")
        listOf(amount, roi, purpose, repayment).forEach { body.addView(it, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) }) }
        body.addView(button("Register Credit in V7") {
            if (people.isEmpty()) { Toast.makeText(this, "Create a person first in Record.", Toast.LENGTH_SHORT).show(); return@button }
            val a = amount.text.toString().toDoubleOrNull()
            if (a == null || a <= 0) { amount.error = "Enter a valid amount"; return@button }
            val r = roi.text.toString().toDoubleOrNull() ?: 0.0
            V7Records.relationship(this, people[spinner.selectedItemPosition].optString("id"),
                "INFORMAL_CREDIT", "RECEIVABLE", a, r,
                repayment.text.toString().ifBlank { "PRINCIPAL_PLUS_INTEREST" },
                purpose.text.toString())
            Toast.makeText(this, "Credit relationship saved in V7.", Toast.LENGTH_SHORT).show()
            renderRelationshipList(body)
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })
        renderRelationshipList(body)
        setContentView(shell("Credit & Money Relationships", "Native V7 credit registration with V7-owned data.", body))
    }

    private fun renderRelationshipList(body: LinearLayout) {
        body.findViewWithTag<View>("v7_rel_list")?.let { body.removeView(it) }
        val box = LinearLayout(this).apply { tag = "v7_rel_list"; orientation = LinearLayout.VERTICAL }
        box.addView(ArthSaathiV7Design.section(this, "Active Relationships", "Outstanding is derived from the canonical relationship record."))
        val rels = V7Core.all(this, V7Core.Keys.RELATIONSHIPS)
        if (rels.isEmpty()) box.addView(ArthSaathiV7Design.text(this, "No credit relationships recorded.", 10f, ArthSaathiV7Design.MUTED))
        rels.takeLast(20).reversed().forEach { r ->
            box.addView(ArthSaathiV7Design.text(this,
                "₹ %.2f  •  %s  •  Outstanding ₹ %.2f".format(r.optDouble("amount"), r.optString("type"), r.optDouble("outstanding")),
                11f, ArthSaathiV7Design.NAVY, true), LinearLayout.LayoutParams(-1, dp(40)))
        }
        body.addView(box)
    }

    private fun repayment() {
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(ArthSaathiV7Design.section(this, "Consent-Controlled Repayment", "V7 will reject mutation unless active consent exists for the relationship party."))
        val rels = V7Core.all(this, V7Core.Keys.RELATIONSHIPS).filter { it.optDouble("outstanding") > 0.005 }
        val labels = if (rels.isEmpty()) listOf("No outstanding V7 relationship") else rels.map { "₹ %.2f outstanding".format(it.optDouble("outstanding")) + " • " + it.optString("type") }
        val spinner = Spinner(this).apply { adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item, labels) }
        body.addView(spinner, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })
        val amount = field("Principal amount", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val method = field("Method (CASH / UPI / NEFT)")
        body.addView(amount, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })
        body.addView(method, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })
        body.addView(button("Record Repayment") {
            if (rels.isEmpty()) { Toast.makeText(this, "No outstanding V7 relationship.", Toast.LENGTH_SHORT).show(); return@button }
            val a = amount.text.toString().toDoubleOrNull()
            if (a == null || a <= 0) { amount.error = "Enter a valid amount"; return@button }
            val rel = rels[spinner.selectedItemPosition]
            val before = rel.optDouble("outstanding")
            V7Records.repayment(this, rel.optString("id"), a, a, 0.0, method.text.toString().ifBlank { "UPI" }, true)
            val after = V7Core.find(this, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))?.optDouble("outstanding") ?: before
            if (after == before) {
                Toast.makeText(this, "Not recorded: active OTP-verified consent is required.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Repayment recorded; outstanding updated.", Toast.LENGTH_SHORT).show()
                renderRepaymentList(body)
            }
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })
        body.addView(ArthSaathiV7Design.text(this,
            "Consent status is evaluated by the canonical V7 core. This screen does not bypass OTP consent.", 9.5f, ArthSaathiV7Design.MUTED),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })
        renderRepaymentList(body)
        setContentView(shell("Repayment Centre", "Chronological repayment records with consent enforcement.", body))
    }

    private fun renderRepaymentList(body: LinearLayout) {
        body.findViewWithTag<View>("v7_repay_list")?.let { body.removeView(it) }
        val box = LinearLayout(this).apply { tag = "v7_repay_list"; orientation = LinearLayout.VERTICAL }
        box.addView(ArthSaathiV7Design.section(this, "Recent Repayments", "Newest first."))
        val rows = V7Core.all(this, V7Core.Keys.REPAYMENTS).sortedByDescending { it.optLong("timestamp") }
        if (rows.isEmpty()) box.addView(ArthSaathiV7Design.text(this, "No repayment recorded.", 10f, ArthSaathiV7Design.MUTED))
        rows.take(20).forEach { r ->
            box.addView(ArthSaathiV7Design.text(this, "₹ %.2f  •  %s".format(r.optDouble("amount"), r.optString("method")), 11f, ArthSaathiV7Design.NAVY, true),
                LinearLayout.LayoutParams(-1, dp(36)))
        }
        body.addView(box)
    }

    private fun assets() {
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(ArthSaathiV7Design.section(this, "Asset Vault", "Financial and non-financial assets stored in V7."))
        val type = field("Asset type (property / vehicle / deposit / other)")
        val description = field("Description")
        val value = field("Current value", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        listOf(type, description, value).forEach { body.addView(it, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) }) }
        body.addView(button("Save Asset in V7") {
            val v = value.text.toString().toDoubleOrNull()
            if (type.text.toString().isBlank()) { type.error = "Asset type is required"; return@button }
            if (v == null || v < 0) { value.error = "Enter a valid value"; return@button }
            V7Records.asset(this, V7Core.user(this), type.text.toString(), description.text.toString(), v)
            Toast.makeText(this, "Asset saved in V7.", Toast.LENGTH_SHORT).show()
            renderAssetList(body)
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })
        renderAssetList(body)
        setContentView(shell("Asset Vault", "Native V7 asset ownership, value and evidence links.", body))
    }

    private fun renderAssetList(body: LinearLayout) {
        body.findViewWithTag<View>("v7_asset_list")?.let { body.removeView(it) }
        val box = LinearLayout(this).apply { tag = "v7_asset_list"; orientation = LinearLayout.VERTICAL }
        box.addView(ArthSaathiV7Design.section(this, "Saved Assets", "Live from v7_assets."))
        val assets = V7Core.all(this, V7Core.Keys.ASSETS)
        if (assets.isEmpty()) box.addView(ArthSaathiV7Design.text(this, "No assets recorded.", 10f, ArthSaathiV7Design.MUTED))
        assets.takeLast(20).reversed().forEach { a ->
            box.addView(ArthSaathiV7Design.text(this, "%s  •  ₹ %.2f".format(a.optString("type"), a.optDouble("currentValue")), 11f, ArthSaathiV7Design.NAVY, true),
                LinearLayout.LayoutParams(-1, dp(38)))
        }
        body.addView(box)
    }

    private fun liabilities() {
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(ArthSaathiV7Design.section(this, "Liability Vault", "Native V7 obligations and outstanding values."))
        val type = field("Liability type")
        val amount = field("Original amount", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val outstanding = field("Outstanding", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val rate = field("Rate %", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        listOf(type, amount, outstanding, rate).forEach { body.addView(it, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) }) }
        body.addView(button("Save Liability in V7") {
            val a = amount.text.toString().toDoubleOrNull()
            val o = outstanding.text.toString().toDoubleOrNull()
            if (type.text.toString().isBlank()) { type.error = "Type is required"; return@button }
            if (a == null || o == null || a < 0 || o < 0) { amount.error = "Enter valid amounts"; return@button }
            V7Records.liability(this, V7Core.user(this), type.text.toString(), a, o, rate.text.toString().toDoubleOrNull() ?: 0.0)
            Toast.makeText(this, "Liability saved in V7.", Toast.LENGTH_SHORT).show()
            renderLiabilityList(body)
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })
        renderLiabilityList(body)
        setContentView(shell("Liability Vault", "Native V7 liability records and outstanding tracking.", body))
    }

    private fun renderLiabilityList(body: LinearLayout) {
        body.findViewWithTag<View>("v7_liability_list")?.let { body.removeView(it) }
        val box = LinearLayout(this).apply { tag = "v7_liability_list"; orientation = LinearLayout.VERTICAL }
        box.addView(ArthSaathiV7Design.section(this, "Saved Liabilities", "Live from v7_liabilities."))
        val rows = V7Core.all(this, V7Core.Keys.LIABILITIES)
        if (rows.isEmpty()) box.addView(ArthSaathiV7Design.text(this, "No liabilities recorded.", 10f, ArthSaathiV7Design.MUTED))
        rows.takeLast(20).reversed().forEach { l ->
            box.addView(ArthSaathiV7Design.text(this, "%s  •  Outstanding ₹ %.2f".format(l.optString("type"), l.optDouble("outstanding")), 11f, ArthSaathiV7Design.NAVY, true),
                LinearLayout.LayoutParams(-1, dp(38)))
        }
        body.addView(box)
    }
}
