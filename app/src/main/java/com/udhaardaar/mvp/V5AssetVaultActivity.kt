package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class V5AssetVaultActivity : androidx.appcompat.app.AppCompatActivity() {
    private val repo by lazy { V5WorkflowRepository(this) }
    private val store by lazy { V5LocalStore(this) }
    private lateinit var category: Spinner
    private lateinit var subtype: Spinner
    private lateinit var owner: EditText
    private lateinit var title: EditText
    private lateinit var value: EditText
    private lateinit var institution: EditText
    private lateinit var reference: EditText
    private lateinit var acquisition: EditText
    private lateinit var maturity: EditText
    private lateinit var location: EditText
    private lateinit var ownership: Spinner
    private lateinit var encumbrance: Spinner
    private lateinit var liability: EditText
    private lateinit var policy: EditText
    private lateinit var nominee: EditText
    private lateinit var docs: EditText
    private lateinit var notes: EditText

    private fun e(h: String) = EditText(this).apply { hint = h; setSingleLine(true); setPadding(14, 9, 14, 9) }
    private fun add(r: LinearLayout, v: android.view.View, h: Int = 55) { r.addView(v, LinearLayout.LayoutParams(-1, h).apply { setMargins(0, 4, 0, 4) }) }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        show()
    }

    private fun show() {
        val r = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 16, 20, 28) }
        add(r, TextView(this).apply { text = "UDHAARDAAR V5 • Asset & Liability Vault"; textSize = 23f }, 62)
        add(r, TextView(this).apply { text = "Maintain financial assets, non-financial assets and liabilities in one structured vault."; textSize = 13f }, 58)

        owner = e("Owner / obligor profile ID *")
        title = e("Asset / liability title *")
        value = e("Current / estimated value ₹ *")
        institution = e("Bank / lender / insurer / broker / counterparty")
        reference = e("Account / loan / folio / policy / registration reference")
        acquisition = e("Acquisition / opening / sanction date")
        maturity = e("Maturity / expiry / closure date")
        location = e("Asset location / branch / property address")
        ownership = Spinner(this).apply { adapter = ArrayAdapter(this@V5AssetVaultActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Self", "Joint", "Family / HUF", "Business", "Other")) }
        encumbrance = Spinner(this).apply { adapter = ArrayAdapter(this@V5AssetVaultActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("None", "Loan / mortgage", "Lien / pledge", "Lease / hypothecation", "Disputed", "Other")) }
        liability = e("Outstanding loan / liability ₹ (if any)")
        policy = e("Policy / certificate / registration number")
        nominee = e("Nominee / beneficiary profile ID")
        docs = e("Proof document IDs (comma separated)")
        notes = e("Notes / special terms / maturity instructions")

        category = Spinner(this).apply { adapter = ArrayAdapter(this@V5AssetVaultActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("FINANCIAL ASSET", "NON-FINANCIAL ASSET", "LIABILITY")) }
        add(r, category)
        subtype = Spinner(this)
        add(r, subtype)
        category.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                val choices = when (pos) {
                    0 -> arrayOf("Bank Account", "Fixed Deposit", "Recurring Deposit", "PPF", "EPF", "NPS", "Mutual Fund", "Shares / Stocks", "Bonds / Debentures", "Insurance / Policy", "Pension / Annuity", "Gold / Financial Gold", "Receivable / Loan Given", "Other Financial Asset")
                    1 -> arrayOf("Land", "Residential Property", "Commercial Property", "Agricultural Property", "Vehicle", "Jewellery / Precious Items", "Business Interest", "Machinery / Equipment", "Furniture / Household Valuable", "Digital / Intellectual Property", "Other Non-Financial Asset")
                    else -> arrayOf("Home Loan / Mortgage", "Personal Loan", "Vehicle Loan", "Business Loan", "Credit Card", "Education Loan", "Tax / Government Dues", "Rent / Lease Payable", "Supplier / Trade Payable", "Guarantee / Contingent Liability", "Other Liability")
                }
                subtype.adapter = ArrayAdapter(this@V5AssetVaultActivity, android.R.layout.simple_spinner_dropdown_item, choices)
                val isLiability = pos == 2
                value.hint = if (isLiability) "Current outstanding balance ₹ *" else "Current / estimated value ₹ *"
                liability.hint = if (isLiability) "Original / secured amount ₹ (if applicable)" else "Outstanding loan / liability ₹ (if any)"
                encumbrance.isEnabled = !isLiability
            }
        }

        listOf(owner, title, value, institution, reference, acquisition, maturity, location, ownership, encumbrance, liability, policy, nominee, docs, notes).forEach { add(r, it) }
        acquisition.setText(today())
        add(r, Button(this).apply { text = "SAVE RECORD"; setOnClickListener { save() } }, 58)
        add(r, Button(this).apply { text = "VIEW SAVED ASSETS & LIABILITIES"; setOnClickListener { list() } }, 58)
        add(r, Button(this).apply { text = "BACK"; setOnClickListener { finish() } }, 50)
        setContentView(ScrollView(this).apply { isFillViewport = true; isSmoothScrollingEnabled = true; addView(r) })
        if (intent.getStringExtra("openCategory") == "LIABILITY") category.setSelection(2)
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun save() {
        val o = owner.text.toString().trim(); val t = title.text.toString().trim(); val v = value.text.toString().trim().toDoubleOrNull()
        if (o.isEmpty() || t.isEmpty() || v == null || v < 0) { Toast.makeText(this, "Owner/obligor, title and valid current value/balance are required", Toast.LENGTH_LONG).show(); return }
        val isLiability = category.selectedItemPosition == 2
        val id = "${if (isLiability) "LIA" else "AST"}-${System.currentTimeMillis()}"
        val proof = docs.text.toString().split(',').map { it.trim() }.filter { it.isNotEmpty() }
        val outstanding = if (isLiability) v else (liability.text.toString().toDoubleOrNull() ?: 0.0)
        repo.saveAsset(V5Asset(id, o, category.selectedItem.toString(), t, if (isLiability) "Liability record" else "Asset record", v, proof, nominee.text.toString().ifBlank { null }, subtype.selectedItem?.toString() ?: "", institution.text.toString(), reference.text.toString(), acquisition.text.toString(), maturity.text.toString(), location.text.toString(), ownership.selectedItem.toString(), if (isLiability) "LIABILITY" else encumbrance.selectedItem.toString(), outstanding, policy.text.toString(), notes.text.toString()))
        repo.appendAudit(id, if (isLiability) "LIABILITY_CREATED" else "ASSET_CREATED", o, "category=${category.selectedItem}; subtype=${subtype.selectedItem}; amount=$v; timestamp=${System.currentTimeMillis()}")
        Toast.makeText(this, if (isLiability) "Liability saved with audit timestamp" else "Asset saved with audit timestamp", Toast.LENGTH_LONG).show()
    }

    private fun list() {
        val a = store.all("assets")
        val msg = if (a.isEmpty()) "No assets or liabilities recorded yet." else a.joinToString("\n\n") { x ->
            val cat = x.optString("category"); val amount = if (cat == "LIABILITY") x.optDouble("outstandingLiability", x.optDouble("estimatedValue", 0.0)) else x.optDouble("estimatedValue", 0.0)
            "${x.optString("title")}\n$cat • ${x.optString("assetSubtype")}\n${if (cat == "LIABILITY") "Outstanding: ₹$amount" else "Value: ₹$amount"}\nOwner/Obligor: ${x.optString("ownerProfileId")}\nCounterparty: ${x.optString("institutionOrCounterparty")}\nReference: ${x.optString("accountOrReference")}\nOwnership: ${x.optString("ownership")} • Status: ${x.optString("encumbrance")}\nDocuments: ${x.optString("documents")}"
        }
        AlertDialog.Builder(this).setTitle("Asset & liability vault").setMessage(msg).setPositiveButton("OK", null).show()
    }
}
