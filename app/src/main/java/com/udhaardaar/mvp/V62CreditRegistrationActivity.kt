package com.udhaardaar.mvp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** V6.2 guided credit registration: profile -> history -> terms -> guarantor -> consent -> record. */
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
    private val draft by lazy { getSharedPreferences("v62_credit_draft", MODE_PRIVATE) }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun text(s: String, size: Float, color: Int = navy, bold: Boolean = false) = TextView(this).apply {
        this.text = s; textSize = size; setTextColor(color)
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }
    private fun card(fill: Int = Color.WHITE) = android.graphics.drawable.GradientDrawable().apply {
        setColor(fill); setStroke(dp(1), border); cornerRadius = dp(16).toFloat()
    }
    private fun field(hint: String) = EditText(this).apply {
        this.hint = hint; setSingleLine(true); textSize = 15f
        setPadding(dp(12), dp(9), dp(12), dp(9)); background = card()
    }
    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label; setTextColor(Color.WHITE); setBackgroundColor(blue); setOnClickListener { action() }
    }
    private fun add(v: android.view.View, top: Int = 8) {
        root.addView(v, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        if (!prefs.getBoolean("logged_in", false)) { finish(); return }
        render()
    }

    private fun render() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(24)); setBackgroundColor(bg) }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
        add(text("ARTHSAATHI", 24f, navy, true), 0)
        add(text("Register Credit", 18f, teal, true), 2)
        add(text("A guided, profile-linked journey — no free-text parties.", 11f, muted), 2)
        add(stepBar(), 12)
        when (step) { 1 -> profileStep(); 2 -> termsStep(); 3 -> guarantorStep(); else -> consentStep() }
    }

    private fun stepBar(): LinearLayout {
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val labels = arrayOf("1 Profile", "2 Terms", "3 Guarantor", "4 Consent")
        labels.forEachIndexed { i, label ->
            val v = text(label, 10f, if (i + 1 <= step) teal else muted, i + 1 <= step)
            v.gravity = Gravity.CENTER; v.setPadding(dp(3), dp(8), dp(3), dp(8))
            v.background = card(if (i + 1 <= step) Color.rgb(235, 249, 247) else Color.WHITE)
            bar.addView(v, LinearLayout.LayoutParams(0, dp(40), 1f).apply { if (i > 0) leftMargin = dp(4) })
        }
        return bar
    }

    private fun profileStep() {
        add(text("WHO IS INVOLVED?", 11f, muted, true), 16)
        profileCard("LENDER", lender, blue) { chooseProfile("Select lender") { lender = it; render() } }
        profileCard("BORROWER", borrower, green) { chooseProfile("Select borrower") { borrower = it; render() } }
        if (lender != null && borrower != null && lender!!.optString("id") != borrower!!.optString("id")) add(button("CONTINUE TO CREDIT TERMS  →") { step = 2; render() }, 16)
        else if (lender != null && borrower != null) add(text("Lender and borrower must be different profiles.", 12f, red, true), 8)
        add(text("Search by name, mobile, PAN, Aadhaar, GSTIN or profile ID. Selecting a profile shows its consent-based history, outstanding and score.", 11f, muted), 12)
    }

    private fun profileCard(role: String, profile: JSONObject?, accent: Int, action: () -> Unit) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = card(); setOnClickListener { action() } }
        box.addView(text(role, 10f, accent, true))
        if (profile == null) {
            box.addView(text("Tap to search or create a registered profile", 15f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
            box.addView(text("Name • Mobile • PAN • Aadhaar • GSTIN", 10f, muted), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
        } else {
            box.addView(text("${profile.optString("name")}  •  ${profile.optString("id")}", 15f, navy, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
            val rows = relatedCredits(profile.optString("id")); val outstanding = rows.filter { it.optString("status") == "ACTIVE" }.sumOf { it.optDouble("outstanding", 0.0) }
            box.addView(text("${profile.optString("mobile")}  •  History ${rows.size}  •  Outstanding ₹${"%.0f".format(outstanding)}  •  Score ${score(rows)}", 10f, teal, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })
        }
        add(box, 7)
    }

    private fun chooseProfile(title: String, done: (JSONObject) -> Unit) {
        val search = field("Search name / mobile / PAN / Aadhaar / GSTIN / profile ID")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(4), dp(4), dp(4), dp(4)) }
        container.addView(search); container.addView(list, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })
        val dialog = AlertDialog.Builder(this).setTitle(title).setView(container).setNegativeButton("CANCEL", null).create()
        fun refresh() {
            list.removeAllViews(); val q = search.text.toString().trim().lowercase()
            val rows = store.all("profiles").filter { p -> q.isEmpty() || listOf("id", "name", "mobile", "pan", "aadhaar", "gstin").any { p.optString(it).lowercase().contains(q) } }
            if (rows.isEmpty()) {
                list.addView(text("No registered profile found", 12f, red, true))
                val create = button("CREATE NEW PROFILE") { dialog.dismiss(); createProfile(done) }
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
        val rows = relatedCredits(p.optString("id")); val outstanding = rows.filter { it.optString("status") == "ACTIVE" }.sumOf { it.optDouble("outstanding", 0.0) }
        val message = "Profile ID: ${p.optString("id")}\nMobile: ${p.optString("mobile")}\nRelated credits: ${rows.size}\nOutstanding: ₹${"%.2f".format(outstanding)}\nConsent-based score: ${score(rows)}\n\nOnly consented transaction history is used."
        val dialog = AlertDialog.Builder(this).setTitle("${p.optString("name")} • history").setMessage(message).setNegativeButton("CANCEL", null).setPositiveButton("SELECT", null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { dialog.dismiss(); done() } }; dialog.show()
    }

    private fun createProfile(done: (JSONObject) -> Unit) {
        val name = field("Full name / business name *"); val mobile = field("Mobile number *"); val pan = field("PAN (optional)"); val aadhaar = field("Aadhaar (optional)"); val gst = field("GSTIN (optional)")
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(name); addView(mobile, lp()); addView(pan, lp()); addView(aadhaar, lp()); addView(gst, lp()) }
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
        }; d.show()
    }

    private fun lp() = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) }

    private fun termsStep() {
        add(tv("CREDIT TERMS", 11f, muted, true), 16)
        val amount = field("Principal amount ₹ *"); val roi = field("Interest / ROI %"); val start = field("Start date — tap to select"); val end = field("End / due date — tap to select"); val emi = field("EMI amount ₹ (only for EMI method)"); val notes = field("Purpose / notes (optional)")
        val method = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("EMI", "Principal + Interest", "Bullet / single payment")) }
        val period = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Monthly", "Quarterly", "Half-yearly", "Yearly", "One-time")) }
        datePicker(start); datePicker(end)
        add(labelled("Principal", amount)); add(labelled("ROI", roi)); add(labelled("Start", start)); add(labelled("End", end)); add(labelled("Repayment method", method)); add(labelled("Periodicity", period)); add(labelled("EMI", emi)); add(labelled("Notes", notes))
        add(tv("LENDER  ${lender!!.optString("name")}  →  BORROWER  ${borrower!!.optString("name")}", 11f, teal, true), 12)
        val back = button("← BACK") { step = 1; render() }
        val next = button("CONTINUE →") {
            val a = amount.text.toString().toDoubleOrNull(); val r = roi.text.toString().toDoubleOrNull() ?: 0.0; val e = emi.text.toString().toDoubleOrNull() ?: 0.0
            when {
                a == null || a <= 0 -> amount.error = "Enter a positive amount"
                r < 0 -> roi.error = "ROI cannot be negative"
                start.text.isNullOrBlank() -> start.error = "Select start date"
                end.text.isNullOrBlank() -> end.error = "Select end date"
                method.selectedItemPosition == 0 && e <= 0 -> emi.error = "Enter EMI amount"
                else -> { draft.edit().putString("amount", a.toString()).putString("roi", r.toString()).putString("start", start.text.toString()).putString("end", end.text.toString()).putString("method", method.selectedItem.toString()).putString("period", period.selectedItem.toString()).putString("emi", e.toString()).putString("notes", notes.text.toString()).apply(); step = 3; render() }
            }
        }
        add(twoButtons(back, next), 16)
    }

    private fun labelled(title: String, view: android.view.View) = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(tv(title, 10f, muted, true)); addView(view, lp()) }
    private fun twoButtons(first: Button, second: Button) = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; addView(first, LinearLayout.LayoutParams(0, dp(52), 1f).apply { rightMargin = dp(5) }); addView(second, LinearLayout.LayoutParams(0, dp(52), 1f).apply { leftMargin = dp(5) }) }
    private fun datePicker(target: EditText) {
        target.isFocusable = false
        target.setOnClickListener { val c = Calendar.getInstance(); DatePickerDialog(this, { _, y, m, d -> c.set(y, m, d); target.setText(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(c.time)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show() }
    }

    private fun guarantorStep() {
        add(tv("GUARANTOR & DOCUMENT", 11f, muted, true), 16)
        add(tv("Optional guarantor. If selected, the profile is linked to the credit and included in consent review.", 11f, muted), 4)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)); background = card() }
        if (guarantor == null) {
            box.addView(tv("No guarantor selected", 15f, navy, true))
            val addG = Button(this).apply { text = "ADD GUARANTOR"; setOnClickListener { chooseProfile("Select guarantor") { guarantor = it; render() } }
            }
            box.addView(addG, lp())
        } else {
            box.addView(tv("${guarantor!!.optString("name")} • ${guarantor!!.optString("id")}", 15f, navy, true))
            box.addView(Button(this).apply { text = "REMOVE GUARANTOR"; setOnClickListener { guarantor = null; render() } }, lp())
        }
        add(box, 8)
        val doc = field("Digital document reference — DPN / invoice / agreement")
        add(labelled("Digital document", doc), 12)
        val back = button("← BACK") { step = 2; render() }
        val next = button("REVIEW + CONSENT →") { draft.edit().putString("document", doc.text.toString()).apply(); step = 4; render() }
        add(twoButtons(back, next), 16)
    }

    private fun consentStep() {
        val amount = draft.getString("amount", "0") ?: "0"; val roi = draft.getString("roi", "0") ?: "0"; val start = draft.getString("start", "") ?: ""; val end = draft.getString("end", "") ?: ""; val method = draft.getString("method", "") ?: ""; val period = draft.getString("period", "") ?: ""; val doc = draft.getString("document", "") ?: ""
        add(tv("FINAL REVIEW", 11f, muted, true), 16)
        val review = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)); background = card(Color.rgb(239, 249, 246)) }
        review.addView(tv("₹$amount  •  $method  •  $period", 17f, navy, true)); review.addView(tv("ROI $roi%  •  $start → $end", 11f, muted), lp()); review.addView(tv("Lender: ${lender!!.optString("name")}\nBorrower: ${borrower!!.optString("name")}${guarantor?.let { "\nGuarantor: ${it.optString("name")}" } ?: ""}", 11f, navy), lp()); review.addView(tv("Document: ${doc.ifBlank { "Not specified" }}", 10f, muted), lp())
        add(review, 8)
        add(tv("Both lender and borrower consent are required. This APK uses a local demo OTP boundary; production SMS delivery must be connected before real-world use.", 10f, muted), 10)
        add(button("REQUEST CONSENT OTP") { requestConsent(amount, roi, start, end, method, period, doc) }, 16)
        add(button("← BACK TO GUARANTOR") { step = 3; render() }, 6)
    }

    private fun requestConsent(amount: String, roi: String, start: String, end: String, method: String, period: String, doc: String) {
        val creditId = "CR-${System.currentTimeMillis()}"
        val borrowerConsent = consent.issue(creditId, "CREDIT_CONSENT_BORROWER", borrower!!.optString("id")); val lenderConsent = consent.issue(creditId, "CREDIT_CONSENT_LENDER", lender!!.optString("id"))
        val bOtp = store.find("consents", borrowerConsent)?.optString("otp", "") ?: ""; val lOtp = store.find("consents", lenderConsent)?.optString("otp", "") ?: ""
        val b = field("Borrower OTP"); val l = field("Lender OTP")
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(text("Borrower demo OTP: $bOtp", 11f, teal, true)); addView(b, lp()); addView(text("Lender demo OTP: $lOtp", 11f, teal, true), lp()); addView(l, lp()) }
        val dialog = AlertDialog.Builder(this).setTitle("Consent verification").setMessage("Verify both parties before creating the credit record.").setView(form).setNegativeButton("CANCEL", null).setPositiveButton("VERIFY + REGISTER", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val okB = consent.verify(borrowerConsent, b.text.toString().trim()); val okL = consent.verify(lenderConsent, l.text.toString().trim())
                if (!okB || !okL) { Toast.makeText(this, "Consent not verified — record not created", Toast.LENGTH_LONG).show(); return@setOnClickListener }
                val credit = JSONObject().apply {
                    put("id", creditId); put("lenderId", lender!!.optString("id")); put("borrowerId", borrower!!.optString("id")); put("principal", amount.toDoubleOrNull() ?: 0.0); put("interestRate", roi.toDoubleOrNull() ?: 0.0); put("startDate", start); put("dueDate", end); put("repaymentMethod", method); put("periodicity", period); put("emiAmount", draft.getString("emi", "0")?.toDoubleOrNull() ?: 0.0); put("notes", draft.getString("notes", "") ?: ""); put("documentRef", doc); put("guarantorId", guarantor?.optString("id") ?: ""); put("outstanding", amount.toDoubleOrNull() ?: 0.0); put("status", "ACTIVE"); put("consentIdBorrower", borrowerConsent); put("consentIdLender", lenderConsent); put("createdAt", System.currentTimeMillis())
                }
                store.add("credits", credit); store.add("audit", JSONObject().apply { put("id", "AUD-${System.currentTimeMillis()}"); put("entityId", creditId); put("event", "CREDIT_REGISTERED"); put("at", System.currentTimeMillis()) }); draft.edit().clear().apply(); dialog.dismiss()
                AlertDialog.Builder(this).setTitle("Credit registered ✓").setMessage("$creditId is registered with both-party consent.\n\n₹$amount outstanding\nDue: $end\nMethod: $method").setPositiveButton("DONE") { _, _ -> finish() }.show()
            }
        }
        dialog.show()
    }

    private fun relatedCredits(profileId: String) = store.all("credits").filter { it.optString("lenderId") == profileId || it.optString("borrowerId") == profileId }
    private fun score(rows: List<JSONObject>): Int {
        if (rows.isEmpty()) return 700
        val principal = rows.sumOf { it.optDouble("principal", 0.0) }; val paid = rows.sumOf { (it.optDouble("principal", 0.0) - it.optDouble("outstanding", 0.0)).coerceAtLeast(0.0) }; val ratio = if (principal > 0) paid / principal else 0.0
        return (650 + ratio.coerceIn(0.0, 1.0) * 220).toInt().coerceIn(300, 900)
    }
}
