package com.udhaardaar.mvp

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** V6.2 smooth credit journey: search/create profile -> history -> terms -> guarantor -> consent -> register. */
class V62CreditRegistrationActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val consent by lazy { V5OtpConsentService(this) }
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val navy = Color.rgb(16, 45, 82)
    private val teal = Color.rgb(12, 171, 158)
    private val blue = Color.rgb(42, 103, 221)
    private val green = Color.rgb(18, 137, 91)
    private val gold = Color.rgb(211, 161, 37)
    private val red = Color.rgb(193, 67, 72)
    private val muted = Color.rgb(92, 108, 124)
    private val bg = Color.rgb(246, 249, 252)
    private val border = Color.rgb(220, 228, 236)

    private var lender: JSONObject? = null
    private var borrower: JSONObject? = null
    private var guarantor: JSONObject? = null
    private var step = 1
    private lateinit var root: LinearLayout

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun card(fill: Int = Color.WHITE) = android.graphics.drawable.GradientDrawable().apply {
        setColor(fill); setStroke(dp(1), border); cornerRadius = dp(16).toFloat()
    }
    private fun tv(s: String, size: Float, color: Int = navy, bold: Boolean = false) = TextView(this).apply {
        text = s; textSize = size; setTextColor(color)
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }
    private fun field(hint: String) = EditText(this).apply {
        this.hint = hint; setSingleLine(true); setTextSize(15f)
        setPadding(dp(12), dp(8), dp(12), dp(8)); background = card()
    }
    private fun primary(label: String, click: () -> Unit) = Button(this).apply {
        text = label; setTextColor(Color.WHITE); setBackgroundColor(blue); setOnClickListener { click() }
    }
    private fun add(v: View, top: Int = 8) { root.addView(v, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top) }) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        if (!prefs.getBoolean("logged_in", false)) { finish(); return }
        render()
    }

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(24)); setBackgroundColor(bg)
        }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
        add(tv("ARTHSAATHI", 24f, navy, true), 0)
        add(tv("Register Credit", 18f, teal, true), 2)
        add(tv("A guided, profile-linked journey — no free-text parties.", 11f, muted), 2)
        add(stepBar(), 12)
        when (step) {
            1 -> partiesStep()
            2 -> termsStep()
            3 -> guarantorStep()
            4 -> consentStep()
        }
    }

    private fun stepBar(): View {
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("1 Profile", "2 Terms", "3 Guarantor", "4 Consent").forEachIndexed { i, s ->
            val active = i + 1 <= step
            val v = tv(s, 10f, if (active) teal else muted, active)
            v.gravity = Gravity.CENTER; v.setPadding(dp(5), dp(8), dp(5), dp(8)); v.background = card(if (active) Color.rgb(235, 249, 247) else Color.WHITE)
            bar.addView(v, LinearLayout.LayoutParams(0, dp(40), 1f).apply { if (i > 0) leftMargin = dp(4) })
        }
        return bar
    }

    private fun partiesStep() {
        add(tv("WHO IS INVOLVED?", 11f, muted, true), 16)
        partyCard("LENDER", lender, blue) { chooseProfile("Select lender") { lender = it; render() } }
        partyCard("BORROWER", borrower, green) { chooseProfile("Select borrower") { borrower = it; render() } }
        if (lender != null && borrower != null) {
            if (lender!!.optString("id") == borrower!!.optString("id")) {
                add(tv("Lender and borrower must be different profiles.", 12f, red, true), 8)
            } else {
                add(primary("CONTINUE TO CREDIT TERMS  →") { step = 2; render() }, 16)
            }
        }
        add(tv("Tip: search by name, mobile, PAN, Aadhaar, GSTIN or profile ID. Selecting a profile shows its consent-based credit history and outstanding.", 11f, muted), 12)
    }

    private fun partyCard(role: String, p: JSONObject?, accent: Int, click: () -> Unit) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = card(); setOnClickListener { click() } }
        box.addView(tv(role, 10f, accent, true))
        if (p == null) {
            box.addView(tv("Tap to search or create a registered profile", 15f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
            box.addView(tv("Name • Mobile • PAN • Aadhaar • GSTIN", 10f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
        } else {
            box.addView(tv("${p.optString("name")}  •  ${p.optString("id")}", 15f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
            box.addView(tv("${p.optString("mobile")}  •  ${p.optString("pan").ifBlank { "PAN —" }}", 10f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
            val related = store.all("credits").filter { it.optString("lenderId") == p.optString("id") || it.optString("borrowerId") == p.optString("id") }
            val outstanding = related.filter { it.optString("status") == "ACTIVE" }.sumOf { it.optDouble("outstanding", 0.0) }
            val score = scoreFor(p.optString("id"), related)
            box.addView(tv("History ${related.size}  •  Outstanding ₹${"%.0f".format(outstanding)}  •  Score $score", 11f, teal, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })
            box.addView(tv("Tap to change profile", 9f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
        }
        add(box, 7)
    }

    private fun chooseProfile(title: String, done: (JSONObject) -> Unit) {
        val search = field("Search name / mobile / PAN / Aadhaar / GSTIN / profile ID")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(4), dp(4), dp(4), dp(4)) }
        container.addView(search, LinearLayout.LayoutParams(-1, -2))
        container.addView(list, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        val dialog = AlertDialog.Builder(this).setTitle(title).setView(container).setNegativeButton("CANCEL", null).create()
        fun refresh() {
            list.removeAllViews()
            val q = search.text.toString().trim().lowercase()
            val rows = store.all("profiles").filter { p ->
                q.isEmpty() || listOf("id", "name", "mobile", "pan", "aadhaar", "gstin").any { p.optString(it).lowercase().contains(q) }
            }
            if (rows.isEmpty()) {
                list.addView(tv("No registered profile found", 12f, red, true))
                val create = primary("CREATE NEW PROFILE") { dialog.dismiss(); createProfile { done(it) } }
                list.addView(create, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(8) })
            } else rows.take(12).forEach { p ->
                val b = Button(this).apply { text = "${p.optString("name")}  •  ${p.optString("id")}\n${p.optString("mobile")}"; gravity = Gravity.START; setOnClickListener { dialog.dismiss(); showHistory(p) { done(p) } } }
                list.addView(b, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) })
            }
        }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refresh() }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        dialog.show(); refresh()
    }

    private fun showHistory(p: JSONObject, done: () -> Unit) {
        val rows = store.all("credits").filter { it.optString("lenderId") == p.optString("id") || it.optString("borrowerId") == p.optString("id") }
        val outstanding = rows.filter { it.optString("status") == "ACTIVE" }.sumOf { it.optDouble("outstanding", 0.0) }
        val score = scoreFor(p.optString("id"), rows)
        AlertDialog.Builder(this)
            .setTitle("${p.optString("name")} • profile history")
            .setMessage("Profile ID: ${p.optString("id")}\nMobile: ${p.optString("mobile")}\nRelated credits: ${rows.size}\nOutstanding: ₹${"%.2f".format(outstanding)}\nConsent-based score: $score\n\nOnly consented transaction history is used for this score.")
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("SELECT", null)
            .create().also { d -> d.setOnShowListener { d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { d.dismiss(); done() } }; d.show() }
    }

    private fun createProfile(done: (JSONObject) -> Unit) {
        val name = field("Full name / business name *")
        val mobile = field("Mobile number *")
        val pan = field("PAN (optional)")
        val aadhaar = field("Aadhaar (optional)")
        val gst = field("GSTIN (optional)")
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(name); addView(mobile, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) }); addView(pan, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) }); addView(aadhaar, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) }); addView(gst, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) }) }
        val d = AlertDialog.Builder(this).setTitle("Create registered profile").setView(form).setNegativeButton("CANCEL", null).setPositiveButton("SAVE + CONTINUE", null).create()
        d.setOnShowListener {
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = name.text.toString().trim(); val m = mobile.text.toString().trim(); val panV = pan.text.toString().trim().uppercase(); val gstV = gst.text.toString().trim().uppercase()
                when {
                    n.length < 2 -> name.error = "Enter at least 2 characters"
                    !V5Validation.mobile(m) -> mobile.error = "Enter a valid 10-digit Indian mobile number"
                    store.all("profiles").any { it.optString("mobile") == m } -> mobile.error = "Mobile already registered"
                    panV.isNotBlank() && !Regex("[A-Z]{5}[0-9]{4}[A-Z]").matches(panV) -> pan.error = "Invalid PAN format"
                    gstV.isNotBlank() && !Regex("[0-9]{2}[A-Z0-9]{13}").matches(gstV) -> gst.error = "Invalid GSTIN format"
                    else -> {
                        val p = JSONObject().apply { put("id", "USR-${System.currentTimeMillis()}"); put("name", n); put("mobile", m); put("pan", panV); put("aadhaar", aadhaar.text.toString().trim()); put("gstin", gstV); put("createdAt", System.currentTimeMillis()) }
                        store.add("profiles", p); d.dismiss(); Toast.makeText(this, "Profile created — continuing credit registration", Toast.LENGTH_LONG).show(); done(p)
                    }
                }
            }
        }
        d.show()
    }

    private fun termsStep() {
        add(tv("CREDIT TERMS", 11f, muted, true), 16)
        val amount = field("Principal amount ₹ *")
        val roi = field("Interest / ROI % (annual, if applicable)")
        val start = field("Start date — tap to select")
        val end = field("End / due date — tap to select")
        val emi = field("EMI amount ₹ (only for EMI method)")
        val notes = field("Purpose / notes (optional)")
        val method = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("EMI", "Principal + Interest", "Bullet / single payment")) }
        val period = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Monthly", "Quarterly", "Half-yearly", "Yearly", "One-time")) }
        datePicker(start); datePicker(end)
        add(labelled("Principal", amount)); add(labelled("ROI", roi)); add(labelled("Start", start)); add(labelled("End", end))
        add(labelled("Repayment method", method)); add(labelled("Periodicity", period)); add(labelled("EMI", emi)); add(labelled("Notes", notes))
        add(tv("LENDER  ${lender!!.optString("name")}  →  BORROWER  ${borrower!!.optString("name")}", 11f, teal, true), 12)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(primary("← BACK") { step = 1; render() }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { rightMargin = dp(5) })
        row.addView(primary("CONTINUE →") {
            val a = amount.text.toString().toDoubleOrNull(); val r = roi.text.toString().toDoubleOrNull() ?: 0.0
            when { a == null || a <= 0 -> amount.error = "Enter a positive amount"; r < 0 -> roi.error = "ROI cannot be negative"; start.text.isNullOrBlank() -> start.error = "Select start date"; end.text.isNullOrBlank() -> end.error = "Select end date"; method.selectedItemPosition == 0 && (emi.text.toString().toDoubleOrNull() ?: 0.0) <= 0 -> emi.error = "Enter EMI amount"; else -> {
                getSharedPreferences("v62_credit_draft", MODE_PRIVATE).edit().putString("amount", a.toString()).putString("roi", r.toString()).putString("start", start.text.toString()).putString("end", end.text.toString()).putString("method", method.selectedItem.toString()).putString("period", period.selectedItem.toString()).putString("emi", emi.text.toString()).putString("notes", notes.text.toString()).apply(); step = 3; render()
            }
        }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { leftMargin = dp(5) })
        add(row, 16)
    }

    private fun labelled(title: String, view: View): View {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        box.addView(tv(title, 10f, muted, true)); box.addView(view, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) }); return box
    }

    private fun datePicker(target: EditText) { target.isFocusable = false; target.setOnClickListener { val c = Calendar.getInstance(); android.app.DatePickerDialog(this, { _, y, m, d -> c.set(y, m, d); target.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(c.time)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() } }

    private fun guarantorStep() {
        add(tv("GUARANTOR & DOCUMENT", 11f, muted, true), 16)
        add(tv("A guarantor is optional. If selected, the guarantor is also linked to the credit record and included in the consent review.", 11f, muted), 5)
        val g = guarantor
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)); background = card() }
        if (g == null) {
            box.addView(tv("No guarantor selected", 15f, navy, true)); box.addView(tv("You can continue without one.", 10f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
            val select = Button(this).apply { text = "ADD GUARANTOR"; setOnClickListener { chooseProfile("Select guarantor") { guarantor = it; render() } }
            }
            box.addView(select, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(10) })
        } else {
            box.addView(tv("${g.optString("name")} • ${g.optString("id")}", 15f, navy, true)); box.addView(tv("${g.optString("mobile")}", 10f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
            box.addView(Button(this).apply { text = "REMOVE GUARANTOR"; setOnClickListener { guarantor = null; render() } }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })
        }
        add(box, 8)
        val doc = field("Digital document note — e.g. DPN / invoice / agreement reference")
        add(labelled("Digital document", doc), 12)
        getSharedPreferences("v62_credit_draft", MODE_PRIVATE).edit().putString("document", doc.text.toString()).apply()
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(primary("← BACK") { step = 2; render() }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { rightMargin = dp(5) })
        row.addView(primary("REVIEW + CONSENT →") { getSharedPreferences("v62_credit_draft", MODE_PRIVATE).edit().putString("document", doc.text.toString()).apply(); step = 4; render() }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { leftMargin = dp(5) })
        add(row, 16)
    }

    private fun consentStep() {
        val d = getSharedPreferences("v62_credit_draft", MODE_PRIVATE)
        val amount = d.getString("amount", "0") ?: "0"
        val method = d.getString("method", "") ?: ""
        val period = d.getString("period", "") ?: ""
        val start = d.getString("start", "") ?: ""
        val end = d.getString("end", "") ?: ""
        val roi = d.getString("roi", "0") ?: "0"
        val doc = d.getString("document", "") ?: ""
        add(tv("FINAL REVIEW", 11f, muted, true), 16)
        val review = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)); background = card(Color.rgb(239, 249, 246)) }
        review.addView(tv("₹$amount  •  $method  •  $period", 18f, navy, true))
        review.addView(tv("ROI $roi%  •  $start → $end", 11f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) })
        review.addView(tv("Lender: ${lender!!.optString("name")}\nBorrower: ${borrower!!.optString("name")}${guarantor?.let { "\nGuarantor: ${it.optString("name")}" } ?: ""}", 11f, navy), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        review.addView(tv("Digital document: ${doc.ifBlank { "Not specified" }}", 10f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) })
        add(review, 8)
        add(tv("Consent OTP", 11f, muted, true), 12)
        add(tv("Both lender and borrower consent are required. In this offline build the generated demo OTP is shown after you request consent. A production SMS/OTP provider must replace this local demo boundary.", 10f, muted), 4)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(primary("← BACK") { step = 3; render() }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { rightMargin = dp(5) })
        row.addView(primary("REQUEST OTP") { requestConsent(amount, method, period, start, end, roi, doc) }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { leftMargin = dp(5) })
        add(row, 16)
    }

    private fun requestConsent(amount: String, method: String, period: String, start: String, end: String, roi: String, doc: String) {
        val creditId = "CR-${System.currentTimeMillis()}"
        val borrowerConsent = consent.issue(creditId, "CREDIT_CONSENT_BORROWER", borrower!!.optString("id"))
        val lenderConsent = consent.issue(creditId, "CREDIT_CONSENT_LENDER", lender!!.optString("id"))
        val bOtp = store.find("consents", borrowerConsent)?.optString("otp", "") ?: ""
        val lOtp = store.find("consents", lenderConsent)?.optString("otp", "") ?: ""
        val b = field("Borrower OTP")
        val l = field("Lender OTP")
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(tv("Borrower demo OTP: $bOtp", 11f, teal, true)); addView(b, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) }); addView(tv("Lender demo OTP: $lOtp", 11f, teal, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) }); addView(l, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5) }) }
        AlertDialog.Builder(this).setTitle("Consent verification").setMessage("Verify both parties before creating the credit record.").setView(form).setNegativeButton("CANCEL", null).setPositiveButton("VERIFY + REGISTER") { _, _ ->
            val okB = consent.verify(borrowerConsent, b.text.toString().trim()); val okL = consent.verify(lenderConsent, l.text.toString().trim())
            if (!okB || !okL) { Toast.makeText(this, "Consent not verified — credit was not registered", Toast.LENGTH_LONG).show(); return@setPositiveButton }
            val credit = JSONObject().apply {
                put("id", creditId); put("lenderId", lender!!.optString("id")); put("borrowerId", borrower!!.optString("id")); put("principal", amount.toDoubleOrNull() ?: 0.0); put("interestRate", roi.toDoubleOrNull() ?: 0.0); put("startDate", start); put("dueDate", end); put("repaymentMethod", method); put("periodicity", period); put("emiAmount", dble(getSharedPreferences("v62_credit_draft", MODE_PRIVATE).getString("emi", "0"))); put("documentRef", doc); put("guarantorId", guarantor?.optString("id") ?: ""); put("outstanding", amount.toDoubleOrNull() ?: 0.0); put("status", "ACTIVE"); put("consentIdBorrower", borrowerConsent); put("consentIdLender", lenderConsent); put("createdAt", System.currentTimeMillis())
            }
            store.add("credits", credit)
            store.add("audit", JSONObject().apply { put("id", "AUD-${System.currentTimeMillis()}"); put("entityId", creditId); put("event", "CREDIT_REGISTERED"); put("at", System.currentTimeMillis()) })
            getSharedPreferences("v62_credit_draft", MODE_PRIVATE).edit().clear().apply()
            AlertDialog.Builder(this).setTitle("Credit registered ✓").setMessage("Credit $creditId has been registered with both-party consent.\n\n₹$amount outstanding\nMethod: $method\nDue: $end\n\nThe record is now available to the repayment and profile history flows.").setPositiveButton("DONE") { _, _ -> finish() }.show()
        }.show()
    }

    private fun dble(s: String?) = s?.toDoubleOrNull() ?: 0.0
    private fun scoreFor(id: String, rows: List<JSONObject>): Int {
        if (rows.isEmpty()) return 700
        val paid = rows.sumOf { it.optDouble("principal", 0.0) - it.optDouble("outstanding", 0.0) }
        val principal = rows.sumOf { it.optDouble("principal", 0.0) }
        val ratio = if (principal > 0) (paid / principal).coerceIn(0.0, 1.0) else 0.0
        return (650 + ratio * 220).toInt().coerceIn(300, 900)
    }
}
