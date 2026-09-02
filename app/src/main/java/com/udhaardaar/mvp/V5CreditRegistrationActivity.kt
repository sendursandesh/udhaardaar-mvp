package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject

/** End-to-end V5 credit registration: parties -> terms -> documents -> consent -> registration. */
class V5CreditRegistrationActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val otpService by lazy { V5OtpConsentService(this) }
    private val consents = linkedMapOf<String, String>()
    private var dpnCreated = false
    private var guaranteeCreated = false
    private lateinit var status: TextView
    private lateinit var direction: Spinner
    private lateinit var lender: EditText
    private lateinit var borrower: EditText
    private lateinit var guarantor: EditText
    private lateinit var guarantorMobile: EditText
    private lateinit var amount: EditText
    private lateinit var roi: EditText
    private lateinit var start: EditText
    private lateinit var end: EditText

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun e(hint: String, max: Int = 0) = EditText(this).apply {
        this.hint = hint
        setSingleLine(true)
        setTextSize(16f)
        minHeight = dp(52)
        setPadding(dp(12), dp(8), dp(12), dp(8))
        if (max > 0) filters = arrayOf(android.text.InputFilter.LengthFilter(max))
        setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) view.post {
                view.requestRectangleOnScreen(Rect(0, 0, view.width, view.height), true)
            }
        }
    }

    private fun addRow(root: LinearLayout, view: View, heightDp: Int = 0) {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            if (heightDp > 0) dp(heightDp) else ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dp(6), 0, dp(6))
        root.addView(view, params)
        view.minimumHeight = dp(54)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextSize(15f)
        minHeight = dp(54)
        setPadding(dp(12), dp(8), dp(12), dp(8))
        setOnClickListener { action() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        showForm()
    }

    private fun showForm() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(24))
        }

        addRow(root, TextView(this).apply {
            text = "UDHAARDAAR V5 • CREDIT REGISTRATION"
            textSize = 20f
            setTextColor(Color.rgb(24, 58, 92))
            includeFontPadding = true
        }, 70)
        addRow(root, TextView(this).apply {
            text = "Parties → terms → documents → consent → confirmation"
            textSize = 14f
        }, 48)

        direction = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@V5CreditRegistrationActivity,
                android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Credit Given", "Credit Received")
            )
        }
        addRow(root, direction)

        lender = e("Lender / creditor full name or profile ID *")
        borrower = e("Borrower / debtor full name or profile ID *")
        guarantor = e("Guarantor name (optional)")
        guarantorMobile = e("Guarantor mobile (required if guarantor added)", 10)
        amount = e("Principal amount *")
        roi = e("Annual ROI %")
        start = e("Start date YYYY-MM-DD *")
        end = e("End date YYYY-MM-DD *")
        listOf(lender, borrower, guarantor, guarantorMobile, amount, roi, start, end)
            .forEach { addRow(root, it) }

        addRow(root, button("CREATE / REFRESH DIGITAL DOCUMENT PACKET") { prepareDocuments() })
        addRow(root, button("CONSENT: LENDER") { requestConsent("LENDER", lender.text.toString()) })
        addRow(root, button("CONSENT: BORROWER") { requestConsent("BORROWER", borrower.text.toString()) })
        addRow(root, button("CONSENT: GUARANTOR") {
            if (guarantor.text.toString().trim().isEmpty()) {
                toast("No guarantor added")
            } else {
                requestConsent("GUARANTOR", guarantor.text.toString())
            }
        })

        status = TextView(this).apply {
            text = "Required: DPN + required guarantee + lender/borrower consent + guarantor consent when applicable."
            textSize = 14f
            setPadding(0, dp(8), 0, dp(8))
        }
        addRow(root, status, 80)
        addRow(root, button("CONFIRM & REGISTER CREDIT") { register() })
        addRow(root, button("HOME") { goHome() })
        addRow(root, button("BACK") { finish() })

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isSmoothScrollingEnabled = true
            addView(root, ViewGroup.LayoutParams(-1, -2))
        }
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, maxOf(imeBottom, dp(24)))
            insets
        }
        setContentView(scroll)
        scroll.post { scroll.scrollTo(0, 0) }
    }

    private fun prepareDocuments() {
        val creditId = "CR-${System.currentTimeMillis()}"
        val lenderName = lender.text.toString().trim()
        val borrowerName = borrower.text.toString().trim()
        val principal = amount.text.toString().toDoubleOrNull() ?: 0.0
        val rate = roi.text.toString().toDoubleOrNull() ?: 0.0
        val startDate = start.text.toString().trim()
        val endDate = end.text.toString().trim()

        if (lenderName.length < 2 || borrowerName.length < 2 || principal <= 0.0 ||
            startDate.isBlank() || endDate.isBlank()
        ) {
            toast("Complete lender, borrower, amount and dates first")
            return
        }

        store.replace("documents", JSONObject().apply {
            put("id", "DPN-$creditId")
            put("type", "Demand Promissory Note")
            put("creditId", creditId)
            put("content", V5GuarantorAndDocuments.generateDpnTemplate(
                creditId, borrowerName, lenderName, principal, rate, startDate, endDate
            ))
            put("status", "DRAFT")
            put("createdAt", System.currentTimeMillis())
        })
        dpnCreated = true
        guaranteeCreated = false

        val guarantorName = guarantor.text.toString().trim()
        val guarantorPhone = guarantorMobile.text.toString().trim()
        if (guarantorName.isNotEmpty()) {
            if (!Regex("^[6-9][0-9]{9}$").matches(guarantorPhone)) {
                toast("Enter valid guarantor mobile")
                dpnCreated = false
                return
            }
            val profile = V5GuarantorAndDocuments.GuarantorProfile(
                "G-${System.currentTimeMillis()}", guarantorName, guarantorPhone, ""
            )
            store.replace("documents", JSONObject().apply {
                put("id", "GUA-$creditId")
                put("type", "Guarantor Guarantee")
                put("creditId", creditId)
                put("content", V5GuarantorAndDocuments.generateGuaranteeTemplate(
                    creditId, profile, borrowerName, principal
                ))
                put("status", "DRAFT")
                put("createdAt", System.currentTimeMillis())
            })
            guaranteeCreated = true
        }

        status.text = if (guaranteeCreated) {
            "Digital packet CREATED: DPN + Guarantor Guarantee. Obtain required OTP consents."
        } else {
            "Digital packet CREATED: DPN. Obtain required OTP consents."
        }
        toast("Digital document packet created before registration")
    }

    private fun requestConsent(party: String, recipient: String) {
        if (recipient.trim().length < 2) {
            toast("Enter $party details first")
            return
        }
        if (party == "GUARANTOR" && !Regex("^[6-9][0-9]{9}$").matches(guarantorMobile.text.toString().trim())) {
            toast("Enter valid guarantor mobile")
            return
        }

        val consentId = otpService.issue("CR-PENDING", "CREDIT_${party}_CONSENT", recipient)
        val otp = store.find("consents", consentId)?.optString("otp", "") ?: ""
        val input = e("Enter 6-digit OTP", 6)
        val dialog = AlertDialog.Builder(this)
            .setTitle("$party CONSENT OTP")
            .setMessage("Demo OTP: $otp\nLive SMS requires configured provider.")
            .setView(input)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("VERIFY", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(-1).setOnClickListener {
                if (otpService.verify(consentId, input.text.toString())) {
                    consents[party] = consentId
                    status.text = "$party consent VERIFIED"
                    dialog.dismiss()
                } else {
                    input.error = "Incorrect OTP"
                }
            }
        }
        dialog.show()
    }

    private fun register() {
        val lenderName = lender.text.toString().trim()
        val borrowerName = borrower.text.toString().trim()
        val principal = amount.text.toString().toDoubleOrNull() ?: 0.0
        val guarantorName = guarantor.text.toString().trim()

        if (lenderName.length < 2 || borrowerName.length < 2 || principal <= 0.0) {
            toast("Complete lender, borrower and amount")
            return
        }
        if (!dpnCreated) {
            toast("Create digital DPN before registering")
            return
        }
        if (guarantorName.isNotEmpty() && !guaranteeCreated) {
            toast("Create guarantor guarantee before registering")
            return
        }
        if (!consents.containsKey("LENDER") || !consents.containsKey("BORROWER")) {
            toast("Lender and borrower OTP consent are required")
            return
        }
        if (guarantorName.isNotEmpty() && !consents.containsKey("GUARANTOR")) {
            toast("Guarantor OTP consent is required")
            return
        }

        val creditId = "CR-${System.currentTimeMillis()}"
        store.replace("credits", JSONObject().apply {
            put("id", creditId)
            put("direction", direction.selectedItem.toString())
            put("lender", lenderName)
            put("borrower", borrowerName)
            put("guarantor", guarantorName)
            put("amount", principal)
            put("roi", roi.text.toString())
            put("start", start.text.toString())
            put("end", end.text.toString())
            put("dpn", "DPN-$creditId")
            put("guarantee", if (guaranteeCreated) "GUA-$creditId" else "")
            put("lenderConsent", consents["LENDER"])
            put("borrowerConsent", consents["BORROWER"])
            put("guarantorConsent", consents["GUARANTOR"] ?: "")
            put("status", "REGISTERED")
            put("registeredAt", System.currentTimeMillis())
        })
        toast("Credit registered after required documents and consents")
        goHome()
    }

    private fun goHome() {
        startActivity(Intent(this, V5HomeActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        ))
        finish()
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
