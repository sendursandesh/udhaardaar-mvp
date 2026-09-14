package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class V62CreditRegistrationActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val draft by lazy { getSharedPreferences("v62_credit_draft", MODE_PRIVATE) }
    private val d get() = resources.displayMetrics.density
    private lateinit var root: LinearLayout
    private var selected: JSONObject? = null
    private var step = 1
    private var docUri = ""
    private fun input(h: String) = ArthSaathiV62Design.input(this, h)
    private fun add(v: android.view.View, gap: Int = 7) = ArthSaathiV62Design.add(root, v, gap)

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); render() }
    private fun render() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(ArthSaathiV62Design.dp(16,d), ArthSaathiV62Design.dp(10,d), ArthSaathiV62Design.dp(16,d), ArthSaathiV62Design.dp(28,d)); setBackgroundColor(ArthSaathiV62Design.BG) }
        add(ArthSaathiV62Design.title(this, "Register Financial Relationship", "Search • Verify • Agree • Record"), 2)
        add(ArthSaathiV62Design.text(this, "Counterparty history is disclosed only after consent + OTP. Supporting evidence is retained before final registration.", 10f, ArthSaathiV62Design.MUTED), 5)
        add(ArthSaathiV62Design.text(this, "STEP $step OF 4", 10f, ArthSaathiV62Design.TEAL, true), 8)
        when (step) { 1 -> profile(); 2 -> terms(); 3 -> evidence(); else -> consent() }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }
    private fun profile() {
        add(ArthSaathiV62Design.section(this, "1 • COUNTERPARTY"), 8)
        add(ArthSaathiV62Design.button(this, "SEARCH / CREATE COUNTERPARTY", ArthSaathiV62Design.TEAL) { chooseCounterparty() }, 10)
        selected?.let { cp ->
            add(ArthSaathiV62Design.text(this, "Selected: ${cp.optString("name")} • ${cp.optString("mobile")}", 14f, ArthSaathiV62Design.NAVY, true), 7)
            if (draft.getBoolean("historyConsentVerified", false)) add(ArthSaathiV62Design.text(this, historySummary(cp.optString("id")), 11f, ArthSaathiV62Design.NAVY), 6)
            else add(ArthSaathiV62Design.button(this, "REQUEST HISTORY-CONSENT OTP", ArthSaathiV62Design.TEAL) { historyConsent() }, 7)
        }
        add(ArthSaathiV62Design.button(this, "CONTINUE TO TERMS →", ArthSaathiV62Design.GREEN) {
            if (selected == null) Toast.makeText(this, "Select or create a counterparty first.", Toast.LENGTH_SHORT).show()
            else if (!draft.getBoolean("historyConsentVerified", false)) Toast.makeText(this, "History consent OTP is mandatory.", Toast.LENGTH_LONG).show()
            else { step = 2; render() }
        }, 12)
    }
    private fun chooseCounterparty() {
        val q = input("Search name / mobile / PAN / Aadhaar / GSTIN / profile ID")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(q); addView(list) }
        val dlg = AlertDialog.Builder(this).setTitle("Registered counterparty").setView(wrap).setNegativeButton("CANCEL", null).create()
        fun refresh() {
            list.removeAllViews(); val x = q.text.toString().trim().lowercase(Locale.getDefault())
            val rows = store.all("profiles").filter { p -> x.isEmpty() || listOf("id","name","mobile","pan","aadhaar","gstin").any { p.optString(it).lowercase(Locale.getDefault()).contains(x) } }
            if (rows.isEmpty()) list.addView(ArthSaathiV62Design.button(this, "CREATE NEW COUNTERPARTY", ArthSaathiV62Design.TEAL) { dlg.dismiss(); createCounterparty() })
            else rows.take(15).forEach { p -> list.addView(ArthSaathiV62Design.button(this, "${p.optString("name")} • ${p.optString("mobile")}", ArthSaathiV62Design.NAVY) { dlg.dismiss(); selected = p; draft.edit().putBoolean("historyConsentVerified", false).apply(); render() }) }
        }
        q.addTextChangedListener(object : android.text.TextWatcher { override fun beforeTextChanged(s: CharSequence?, a: Int, c: Int, x: Int) {} ; override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { refresh() }; override fun afterTextChanged(e: android.text.Editable?) {} })
        dlg.show(); refresh()
    }
    private fun createCounterparty() {
        val n = input("Name / business name *"); val m = input("Mobile *"); val pan = input("PAN"); val gst = input("GSTIN")
        val w = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(n); addView(m); addView(pan); addView(gst) }
        AlertDialog.Builder(this).setTitle("Create counterparty").setView(w).setNegativeButton("CANCEL", null).setPositiveButton("CREATE") { _, _ ->
            if (n.text.length >= 2 && V5Validation.mobile(m.text.toString())) {
                val cp = JSONObject().apply { put("id", ArthSaathiV62Core.id("CP")); put("name", n.text.toString().trim()); put("mobile", m.text.toString().trim()); put("pan", pan.text.toString().trim().uppercase(Locale.getDefault())); put("gstin", gst.text.toString().trim().uppercase(Locale.getDefault())) }
                store.add("profiles", cp); selected = cp; draft.edit().putBoolean("historyConsentVerified", false).apply(); render()
            } else Toast.makeText(this, "Enter valid name and mobile.", Toast.LENGTH_SHORT).show()
        }.show()
    }
    private fun historyConsent() {
        val cp = selected ?: return
        if (cp.optString("mobile").isBlank()) { Toast.makeText(this, "Valid counterparty mobile is required.", Toast.LENGTH_LONG).show(); return }
        val otp = (100000..999999).random(); val entry = input("Enter 6-digit OTP")
        val dlg = AlertDialog.Builder(this).setTitle("History-sharing consent").setMessage("Consent is required to disclose recorded history, outstanding behaviour and internal ArthSaathi score.\n\nDemo OTP: $otp").setView(entry).setNegativeButton("DECLINE", null).setPositiveButton("VERIFY", null).create()
        dlg.setOnShowListener { dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if (entry.text.toString() == otp.toString()) { dlg.dismiss(); draft.edit().putBoolean("historyConsentVerified", true).putString("historyConsentAt", System.currentTimeMillis().toString()).apply(); render() } else entry.error = "Incorrect OTP" } }; dlg.show()
    }
    private fun historySummary(id: String): String {
        val rels = store.all(ArthSaathiV62Core.RELATIONSHIPS).filter { it.optString("counterpartyId") == id }
        val repayments = store.all(ArthSaathiV62Core.REPAYMENTS).filter { it.optString("counterpartyId") == id || it.optString("relationshipId").isNotBlank() && rels.any { r -> r.optString("id") == it.optString("relationshipId") } }
        val exposure = rels.sumOf { it.optDouble("amount", 0.0) }; val repaid = repayments.sumOf { it.optDouble("amount", 0.0) }
        val score = (750 - rels.size * 8 - (kotlin.math.max(0.0, exposure - repaid) / 10000.0)).toInt().coerceIn(300, 900)
        return "CONSENTED HISTORY\nRelationships: ${rels.size}\nRecorded exposure: ₹${"%.2f".format(Locale.US, exposure)}\nRecorded repayments: ₹${"%.2f".format(Locale.US, repaid)}\nArthSaathi Score (internal): $score"
    }
    private fun terms() {
        add(ArthSaathiV62Design.section(this, "2 • TERMS & REPAYMENT"), 8)
        val type = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Personal / Hand Loan","Business Trade Credit — Supplier / Seller","Rental / Lease Credit","Other")) }
        val amount = input("Principal / invoice value ₹ *"); val roi = input("Interest rate % — 0 = interest-free")
        val method = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("EMI","Principal + Interest","Bullet / single payment")) }
        val period = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Monthly","Quarterly","Half-yearly","Yearly","One-time")) }
        val start = input("Start date DD/MM/YYYY"); val end = input("End / final payment date DD/MM/YYYY"); val emi = input("Auto-calculated EMI").apply { isFocusable = false }
        listOf(type, amount, roi, method, period, start, end, emi).forEach { add(it, 4) }
        fun calc() {
            if (method.selectedItemPosition != 0) { emi.setText(""); return }
            val p = amount.text.toString().replace(",", "").toDoubleOrNull(); val r = roi.text.toString().toDoubleOrNull() ?: 0.0; val sd = parse(start.text.toString()); val ed = parse(end.text.toString()); val freq = when(period.selectedItemPosition){0->12;1->4;2->2;3->1;else->0}
            if (p == null || p <= 0 || sd == null || ed == null || !ed.after(sd) || freq == 0) { emi.setText(""); return }
            val n = maxOf(1, Math.round(((ed.time-sd.time).toDouble()/(1000*60*60*24*30.4375))/(12.0/freq)).toInt()); val rate = r/100.0/freq; val pay = if(rate == 0.0) p/n else p*rate*(1+rate).pow(n)/((1+rate).pow(n)-1); emi.setText(String.format(Locale.US, "%.2f", pay))
        }
        val tw = object:android.text.TextWatcher { override fun beforeTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){}; override fun onTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){calc()}; override fun afterTextChanged(e:android.text.Editable?){} }
        amount.addTextChangedListener(tw); roi.addTextChangedListener(tw); start.addTextChangedListener(tw); end.addTextChangedListener(tw)
        add(ArthSaathiV62Design.button(this, "CONTINUE →", ArthSaathiV62Design.GREEN) { draft.edit().putString("amount",amount.text.toString()).putString("roi",roi.text.toString()).putString("method",method.selectedItem.toString()).putString("period",period.selectedItem.toString()).putString("start",start.text.toString()).putString("end",end.text.toString()).putString("emi",emi.text.toString()).putString("type",type.selectedItem.toString()).apply(); step=3; render() }, 12)
    }
    private fun evidence() {
        add(ArthSaathiV62Design.section(this, "3 • SUPPORTING DOCUMENT"), 8); add(ArthSaathiV62Design.text(this, "Attach the invoice or supporting evidence before final consent. The original document remains retained.",10f,ArthSaathiV62Design.MUTED),5)
        add(ArthSaathiV62Design.button(this,"SCAN / ATTACH INVOICE",ArthSaathiV62Design.TEAL){pickDoc()},8); add(ArthSaathiV62Design.button(this,"ATTACH OTHER DOCUMENT",ArthSaathiV62Design.BLUE){pickDoc()},5)
        if(docUri.isNotBlank()) add(ArthSaathiV62Design.text(this,"Original retained: $docUri",10f,ArthSaathiV62Design.GREEN,true),7)
        add(ArthSaathiV62Design.button(this,"REVIEW PROMISSORY NOTE →",ArthSaathiV62Design.GREEN){ if(docUri.isBlank()) Toast.makeText(this,"Attach supporting evidence first.",Toast.LENGTH_LONG).show() else { step=4; render() } },12)
    }
    private fun consent() {
        add(ArthSaathiV62Design.section(this,"4 • PROMISSORY NOTE & FINAL OTP"),8)
        val cp = selected ?: run { Toast.makeText(this,"Counterparty missing.",Toast.LENGTH_SHORT).show(); return }
        val note = "I promise to pay ${cp.optString("name")} an amount of ₹${draft.getString("amount","")} under ${draft.getString("method","")} terms.\n\nInterest: ${draft.getString("roi","0")}%\nPeriodicity: ${draft.getString("period","")}\nFinal date: ${draft.getString("end","")}\nCalculated EMI: ${draft.getString("emi","")}"
        add(ArthSaathiV62Design.text(this,"PROMISSORY NOTE\n\n$note",13f,ArthSaathiV62Design.NAVY),8)
        add(ArthSaathiV62Design.button(this,"REQUEST CONSENT OTP",ArthSaathiV62Design.TEAL){registerAfterOtp(note)},10)
    }
    private fun registerAfterOtp(note:String) {
        val cp = selected ?: return; val otp=(100000..999999).random(); val entry=input("Enter 6-digit OTP")
        val dlg=AlertDialog.Builder(this).setTitle("Registration consent OTP").setMessage("Review the promissory note before authorising registration.\n\nDemo OTP: $otp").setView(entry).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY & REGISTER",null).create()
        dlg.setOnShowListener { dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if(entry.text.toString()==otp.toString()){dlg.dismiss(); register(note,cp)} else entry.error="Invalid OTP" } }; dlg.show()
    }
    private fun register(note:String, cp:JSONObject) {
        val rel=JSONObject().apply{put("id",ArthSaathiV62Core.id("REL"));put("counterpartyId",cp.optString("id"));put("amount",draft.getString("amount","0").toDoubleOrNull()?:0.0);put("roi",draft.getString("roi","0").toDoubleOrNull()?:0.0);put("method",draft.getString("method",""));put("period",draft.getString("period",""));put("emi",draft.getString("emi","0").toDoubleOrNull()?:0.0);put("start",draft.getString("start",""));put("end",draft.getString("end",""));put("type",draft.getString("type",""));put("promissoryNote",note);put("supportingDocumentUri",docUri);put("status","ACTIVE");put("outstanding",draft.getString("amount","0").toDoubleOrNull()?:0.0);put("historyConsent","VERIFIED");put("registrationConsent","VERIFIED");put("createdAt",System.currentTimeMillis())}
        store.add(ArthSaathiV62Core.RELATIONSHIPS,rel); Toast.makeText(this,"Financial relationship registered successfully",Toast.LENGTH_LONG).show(); draft.edit().clear().apply(); finish()
    }
    private fun pickDoc(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/pdf";addCategory(Intent.CATEGORY_OPENABLE)},77)}
    override fun onActivityResult(r:Int,c:Int,data:Intent?){super.onActivityResult(r,c,data);if(r==77&&c==RESULT_OK&&data?.data!=null){docUri=ArthSaathiV62Core.saveDocument(this,data.data!!,"CREDIT").optString("uri");render()}}
    private fun parse(s:String):Date?=runCatching{SimpleDateFormat("dd/MM/yyyy",Locale.US).apply{isLenient=false}.parse(s)}.getOrNull()
}
