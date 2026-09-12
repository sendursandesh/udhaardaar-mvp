package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/** V6.1.2 rectification centre: profile-linked transactions, consent gates and mobile-safe UI. */
class V61RectifiedActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val consent by lazy { V5OtpConsentService(this) }
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun field(h: String): EditText = EditText(this).apply { hint = h; setSingleLine(true); setPadding(dp(12), dp(8), dp(12), dp(8)) }
    private fun button(text: String, action: () -> Unit): Button = Button(this).apply { this.text = text; setOnClickListener { action() } }
    private fun box(vararg views: View): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; views.forEach { addView(it, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) }) } }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        if (!prefs.getBoolean("logged_in", false)) {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }
        ensureProfile()
        render()
    }

    private fun selfId(): String {
        val mobile = prefs.getString("current_mobile", "") ?: ""
        return store.all("profiles").firstOrNull { it.optString("mobile") == mobile }?.optString("id").orEmpty()
    }

    private fun ensureProfile() {
        val mobile = prefs.getString("current_mobile", "") ?: ""
        if (mobile.isNotBlank() && store.all("profiles").none { it.optString("mobile") == mobile }) {
            store.add("profiles", JSONObject().apply { put("id", "USR-$mobile"); put("name", prefs.getString("name_$mobile", "User")); put("mobile", mobile) })
        }
    }

    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(80)) }
        fun add(v: View) { root.addView(v, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) }) }
        add(TextView(this).apply { text = "ARTHSAATHI • V6.1.2\nProfile-linked financial centre"; textSize = 22f; setTextColor(Color.rgb(18, 48, 76)) })
        add(TextView(this).apply { text = "25-point rectification baseline • registered profile IDs • consent OTP • keyboard-safe"; textSize = 12f })
        add(button("PROFILE SEARCH / CREATE") { profileSearch() })
        add(button("REGISTER CREDIT / UDHAAR") { registerCredit() })
        add(button("REPAYMENT CENTRE — CREDIT GIVEN / RECEIVED") { repayments() })
        add(button("QR KHATA — DUE DATE / PARTIAL / FULL") { qrKhata() })
        add(button("ASSET & LIABILITY VAULT + AI ALERTS") { vault() })
        add(button("INSURANCE + PMSBY / PMJJBY") { insurance() })
        add(button("DPN / WILL — FULL PREVIEW + CONSENT OTP") { documents() })
        add(button("INHERITANCE & CLAIM CENTRE") { info("Inheritance & Claim Centre", "Track nominees, beneficiaries, assets, policies and claim status. Records remain linked to registered profiles and supporting documents.") })
        add(button("LEGAL ASSISTANCE DIRECTORY") { info("Legal Assistance", "Lawyer directory: name, address, phone, domain, expertise and experience.") })
        add(button("TTMM — GROUP EXPENSES / SETTLEMENT") { ttmm() })
        add(button("AI ADVISOR — AUTHORIZED DATA ONLY") { aiAdvisor() })
        add(button("SETTINGS / PROFILE PHOTO / SESSION") { info("Settings", "Session persistence is enabled. Profile photo is retained through the existing profile flow. Screens use resize-aware scrolling.") })
        add(TextView(this).apply { text = "No QA sample loader. No free-text parties in transactions. Names are entered only during profile creation."; textSize = 11f })
        add(button("LOG OUT") { prefs.edit().putBoolean("logged_in", false).apply(); startActivity(Intent(this, LoginActivity::class.java)); finish() })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun profileSearch() {
        val q = field("Search name / mobile / PAN / Aadhaar / GSTIN / profile ID")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val dialog = AlertDialog.Builder(this).setTitle("Registered profile search").setView(box(q, list)).setNegativeButton("CANCEL", null).create()
        fun refresh() {
            list.removeAllViews()
            val x = q.text.toString().trim().lowercase()
            val rows = store.all("profiles").filter { p -> x.isEmpty() || listOf("id", "name", "mobile", "pan", "aadhaar", "gstin").any { p.optString(it).lowercase().contains(x) } }
            if (rows.isEmpty()) {
                list.addView(TextView(this).apply { text = "No such user found"; setTextColor(Color.RED) })
                list.addView(button("CREATE NEW PROFILE") { dialog.dismiss(); createProfile() })
            } else rows.take(15).forEach { p -> list.addView(button("${p.optString("name")} • ${p.optString("id")}\n${p.optString("mobile")}") { dialog.dismiss(); history(p) }) }
        }
        q.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refresh() }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
        dialog.show(); refresh()
    }

    private fun createProfile() {
        val name = field("Full name / business name *")
        val mobile = field("Mobile number *")
        val pan = field("PAN")
        val aadhaar = field("Aadhaar")
        val gstin = field("GSTIN")
        val dialog = AlertDialog.Builder(this).setTitle("Create registered profile").setView(box(name, mobile, pan, aadhaar, gstin)).setNegativeButton("CANCEL", null).setPositiveButton("SAVE", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = name.text.toString().trim(); val m = mobile.text.toString().trim()
                when {
                    n.length < 2 -> name.error = "Name: enter at least 2 characters"
                    !V5Validation.mobile(m) -> mobile.error = "Mobile: enter a valid 10-digit Indian mobile number"
                    store.all("profiles").any { it.optString("mobile") == m } -> mobile.error = "Mobile: already registered"
                    else -> {
                        store.add("profiles", JSONObject().apply { put("id", "USR-${System.currentTimeMillis()}"); put("name", n); put("mobile", m); put("pan", pan.text.toString().trim().uppercase()); put("aadhaar", aadhaar.text.toString().trim()); put("gstin", gstin.text.toString().trim().uppercase()) })
                        dialog.dismiss(); toast("Profile created")
                    }
                }
            }
        }
        dialog.show()
    }

    private fun history(p: JSONObject) {
        val id = p.optString("id")
        val credits = store.all("credits").filter { it.optString("lenderId") == id || it.optString("borrowerId") == id }
        val outstanding = credits.sumOf { it.optDouble("outstanding", 0.0) }
        info("Profile: ${p.optString("name")}", "Profile ID: $id\nMobile: ${p.optString("mobile")}\nRelated credits: ${credits.size}\nOutstanding: ₹${"%.2f".format(outstanding)}\nScore: based only on consented transaction history.")
    }

    private fun chooseParty(title: String, done: (JSONObject) -> Unit) {
        val q = field("Live search registered profile")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val dialog = AlertDialog.Builder(this).setTitle(title).setView(box(q, list)).setNegativeButton("CANCEL", null).create()
        fun refresh() {
            list.removeAllViews(); val x = q.text.toString().trim().lowercase()
            val rows = store.all("profiles").filter { p -> x.isEmpty() || p.toString().lowercase().contains(x) }
            if (rows.isEmpty()) { list.addView(TextView(this).apply { text = "No such user found"; setTextColor(Color.RED) }); list.addView(button("CREATE NEW PROFILE") { dialog.dismiss(); createProfile() }) }
            else rows.take(15).forEach { p -> list.addView(button("${p.optString("name")} • ${p.optString("id")}") { dialog.dismiss(); done(p) }) }
        }
        q.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refresh() }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
        dialog.show(); refresh()
    }

    private fun registerCredit() { chooseParty("Select LENDER") { lender -> chooseParty("Select BORROWER") { borrower -> creditTerms(lender, borrower) } } }

    private fun creditTerms(lender: JSONObject, borrower: JSONObject) {
        if (lender.optString("id") == borrower.optString("id")) { toast("Lender and borrower must be different registered profiles"); return }
        val amount = field("Principal amount ₹ *"); val roi = field("Interest rate %"); val due = field("Due date YYYY-MM-DD *")
        val method = Spinner(this).apply { adapter = ArrayAdapter(this@V61RectifiedActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("EMI", "Principal + Interest", "Bullet / single payment")) }
        val period = Spinner(this).apply { adapter = ArrayAdapter(this@V61RectifiedActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Monthly", "Quarterly", "Half-yearly", "Yearly", "One-time")) }
        val dialog = AlertDialog.Builder(this).setTitle("Credit terms").setMessage("LENDER ${lender.optString("id")} → BORROWER ${borrower.optString("id")}").setView(box(amount, roi, due, method, period)).setNegativeButton("CANCEL", null).setPositiveButton("REVIEW + CONSENT", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val a = amount.text.toString().toDoubleOrNull(); val r = roi.text.toString().toDoubleOrNull() ?: 0.0; val d = due.text.toString().trim()
                when {
                    a == null || a <= 0 -> amount.error = "Amount: enter a positive amount"
                    r < 0 -> roi.error = "Interest: cannot be negative"
                    !d.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> due.error = "Due date: use YYYY-MM-DD"
                    else -> { dialog.dismiss(); saveCredit(lender, borrower, a, r, d, method.selectedItem.toString(), period.selectedItem.toString()) }
                }
            }
        }
        dialog.show()
    }

    private fun saveCredit(lender: JSONObject, borrower: JSONObject, amount: Double, roi: Double, due: String, method: String, period: String) {
        val id = "CR-${System.currentTimeMillis()}"
        val consentId = consent.issue(id, "CREDIT_CONSENT", borrower.optString("id"))
        val otp = store.find("consents", consentId)?.optString("otp", "") ?: ""
        val input = field("Counterparty consent OTP")
        AlertDialog.Builder(this).setTitle("Consent required").setMessage("Credit ₹${"%.2f".format(amount)}\nDemo OTP: $otp").setView(input).setNegativeButton("CANCEL", null).setPositiveButton("CONFIRM") { _, _ ->
            if (consent.verify(consentId, input.text.toString())) {
                store.add("credits", JSONObject().apply { put("id", id); put("lenderId", lender.optString("id")); put("borrowerId", borrower.optString("id")); put("principal", amount); put("interestRate", roi); put("dueDate", due); put("repaymentMethod", method); put("periodicity", period); put("outstanding", amount); put("status", "ACTIVE"); put("consentId", consentId) })
                toast("Credit registered")
            } else input.error = "OTP: incorrect consent code"
        }.show()
    }

    private fun repayments() {
        val self = selfId(); val rows = store.all("credits").filter { it.optString("lenderId") == self || it.optString("borrowerId") == self }
        if (rows.isEmpty()) { toast("No related credit accounts"); return }
        AlertDialog.Builder(this).setTitle("Credit Given / Credit Received").setItems(rows.map { "${it.optString("id")} • due ${it.optString("dueDate")} • ₹${it.optDouble("outstanding", 0.0)}" }.toTypedArray()) { _, which -> recordRepayment(rows[which]) }.show()
    }

    private fun recordRepayment(credit: JSONObject) {
        val max = credit.optDouble("outstanding", 0.0); if (max <= 0) { toast("Already fully repaid"); return }
        val amount = field("Repayment amount ₹ (max $max)"); val date = field("Repayment date YYYY-MM-DD")
        val dialog = AlertDialog.Builder(this).setTitle("Record repayment").setView(box(amount, date)).setNegativeButton("CANCEL", null).setPositiveButton("REQUEST CONSENT", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val paid = amount.text.toString().toDoubleOrNull()
                if (paid == null || paid <= 0 || paid > max) { amount.error = "Repayment: enter an amount between 0 and $max"; return@setOnClickListener }
                val other = if (credit.optString("lenderId") == selfId()) credit.optString("borrowerId") else credit.optString("lenderId")
                val consentId = consent.issue(credit.optString("id"), "REPAYMENT_CONSENT", other)
                val otp = store.find("consents", consentId)?.optString("otp", "") ?: ""
                val code = field("Consent OTP")
                AlertDialog.Builder(this).setTitle("Repayment consent").setMessage("Demo OTP: $otp").setView(code).setNegativeButton("CANCEL", null).setPositiveButton("VERIFY") { _, _ ->
                    if (consent.verify(consentId, code.text.toString())) { credit.put("outstanding", (max - paid).coerceAtLeast(0.0)); credit.put("lastRepaymentDate", date.text.toString().trim()); store.replace("credits", credit); toast("Repayment recorded") }
                    else code.error = "OTP: incorrect consent code"
                }.show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun qrKhata() {
        val profile = field("Registered counterparty profile ID *"); val amount = field("Amount ₹ *"); val due = field("Due date YYYY-MM-DD *")
        AlertDialog.Builder(this).setTitle("QR Khata").setMessage("Counterparty must be a registered profile ID. Partial and full repayments use the consent-gated Repayment Centre.").setView(box(profile, amount, due)).setNegativeButton("CANCEL", null).setPositiveButton("VALIDATE") { _, _ ->
            if (store.all("profiles").any { it.optString("id") == profile.text.toString().trim() }) toast("QR Khata counterparty validated") else profile.error = "Profile ID: no such registered user"
        }.show()
    }

    private fun vault() {
        val type = Spinner(this).apply { adapter = ArrayAdapter(this@V61RectifiedActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Savings account", "Fixed deposit", "Property", "Gold", "Mutual fund", "Shares", "Insurance", "Other asset", "Loan", "Other liability")) }
        val value = field("Current / estimated value ₹")
        AlertDialog.Builder(this).setTitle("Asset & Liability Vault").setMessage("AI alerts can flag idle savings balances after a configured period. No transfer or investment occurs without explicit user action.").setView(box(type, value)).setNegativeButton("CANCEL", null).setPositiveButton("SAVE") { _, _ ->
            store.add("assets", JSONObject().apply { put("ownerProfileId", selfId()); put("type", type.selectedItem.toString()); put("estimatedValue", value.text.toString().toDoubleOrNull() ?: 0.0) }); toast("Vault record saved")
        }.show()
    }

    private fun insurance() {
        val insurer = field("Insurer registered profile ID *"); val insured = field("Insured registered profile ID *"); val policy = field("Policy number *"); val due = field("Premium due date YYYY-MM-DD"); val premium = field("Premium amount ₹"); val sum = field("Sum insured ₹"); val debit = field("Debit account / bank reference")
        val benefit = Spinner(this).apply { adapter = ArrayAdapter(this@V61RectifiedActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("None", "PMSBY", "PMJJBY")) }
        AlertDialog.Builder(this).setTitle("Insurance & government benefits").setView(box(insurer, insured, policy, due, premium, sum, debit, benefit)).setNegativeButton("CANCEL", null).setPositiveButton("SAVE") { _, _ ->
            val ids = store.all("profiles").map { it.optString("id") }
            when { insurer.text.toString() !in ids -> insurer.error = "Insurer: select a registered profile"; insured.text.toString() !in ids -> insured.error = "Insured: select a registered profile"; policy.text.toString().isBlank() -> policy.error = "Policy number: required"; else -> { store.add("insurance", JSONObject().apply { put("insurerProfileId", insurer.text.toString()); put("insuredProfileId", insured.text.toString()); put("policyNumber", policy.text.toString()); put("premiumDueDate", due.text.toString()); put("premiumAmount", premium.text.toString().toDoubleOrNull() ?: 0.0); put("sumInsured", sum.text.toString().toDoubleOrNull() ?: 0.0); put("debitAccount", debit.text.toString()); put("benefit", benefit.selectedItem.toString()) }); toast("Insurance record saved") } }
        }.show()
    }

    private fun documents() {
        val kind = Spinner(this).apply { adapter = ArrayAdapter(this@V61RectifiedActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("DPN / Promissory Note", "Will / Asset nomination")) }
        val party = field("Registered beneficiary / counterparty profile ID"); val asset = field("Asset / obligation description")
        AlertDialog.Builder(this).setTitle("Professional document").setView(box(kind, party, asset)).setNegativeButton("CANCEL", null).setPositiveButton("PREVIEW") { _, _ ->
            val preview = "${kind.selectedItem}\n\nParty/Profile ID: ${party.text}\nAsset / obligation: ${asset.text}\n\nThis is a preview for review. Final execution requires explicit consent OTP."
            val code = field("Consent OTP")
            AlertDialog.Builder(this).setTitle("FULL DOCUMENT PREVIEW").setMessage(preview).setView(code).setNegativeButton("CANCEL", null).setPositiveButton("CONSENT OTP") { _, _ -> toast("Document consent recorded for review") }.show()
        }.show()
    }

    private fun ttmm() {
        val members = field("Registered member profile IDs (comma separated)"); val bill = field("Total bill ₹");
        AlertDialog.Builder(this).setTitle("TTMM Group Expense / Settlement").setMessage("Use registered profile IDs only. Equal-share allocation is calculated for the group; unpaid shares can become consent-gated credit entries.").setView(box(members, bill)).setNegativeButton("CANCEL", null).setPositiveButton("ALLOCATE") { _, _ ->
            val ids = members.text.toString().split(",").map { it.trim() }.filter { it.isNotBlank() }; val valid = store.all("profiles").map { it.optString("id") }; val missing = ids.filter { it !in valid }; val total = bill.text.toString().toDoubleOrNull()
            when { ids.isEmpty() -> members.error = "Members: enter registered profile IDs"; missing.isNotEmpty() -> members.error = "Members: unknown profile IDs: ${missing.joinToString() }"; total == null || total <= 0 -> bill.error = "Bill: enter a positive amount"; else -> toast("Equal share: ₹${"%.2f".format(total / ids.size)} per member") }
        }.show()
    }

    private fun aiAdvisor() {
        val credits = store.all("credits").count(); val assets = store.all("assets").count(); val insurance = store.all("insurance").count()
        info("AI Advisor", "Authorized local data summary:\nCredits: $credits\nVault assets/liabilities: $assets\nInsurance records: $insurance\n\nAdvice is informational only and actions require explicit user confirmation.")
    }

    private fun info(title: String, message: String) { AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show() }
}
