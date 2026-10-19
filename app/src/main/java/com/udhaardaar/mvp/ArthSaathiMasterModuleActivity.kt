package com.udhaardaar.mvp

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.*
import androidx.core.content.FileProvider
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.io.ByteArrayInputStream
import java.util.*

/** Canonical executable surface for the consolidated ArthSaathi architecture. */
class ArthSaathiMasterModuleActivity : Activity() {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private var qrMerchantField: EditText? = null
    private var qrAmountField: EditText? = null
    private var qrReferenceField: EditText? = null
    private var qrNoteField: EditText? = null
    private var qrCapturedAtField: EditText? = null
    private var qrInvoiceDateField: EditText? = null
    private var qrRawPayload: String = ""
    private var documentTitleField: EditText? = null
    private var documentTypeField: EditText? = null
    private var documentRelatedField: EditText? = null
    private val requestPickDocument = 8124
    private val records get() = ArthSaathiDataStore.records()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        render(intent.getStringExtra("module") ?: ArthSaathiNavigation.HOME)
    }

    private fun render(id: String) {
        val m = ArthSaathiArchitectureRegistry.canonicalModule(id)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(ArthSaathiV7Design.CREAM)
            isFocusableInTouchMode = true
        }
        val masthead = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(8, 8, 8, 12)
            background = ArthSaathiV7Design.bg(this@ArthSaathiMasterModuleActivity)
        }
        masthead.addView(ArthSaathiV7Design.logo(this, 54).apply {
            tag = "arthsaathi-canonical-logo"
            contentDescription = "ArthSaathi logo"
        })
        val brandColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8, 0, 0, 0) }
        brandColumn.addView(ArthSaathiV7Design.brand(this, 23f, true))
        brandColumn.addView(ArthSaathiV7Design.text(this, ArthSaathiV7Design.TAGLINE, 11f, ArthSaathiV7Design.GOLD_PALE))
        brandColumn.addView(ArthSaathiV7Design.text(this, ArthSaathiV7Design.PILLARS, 10f, ArthSaathiV7Design.WHITE))
        masthead.addView(brandColumn, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(masthead)
        root.addView(TextView(this).apply {
            text = m?.title ?: "ArthSaathi"
            textSize = 24f
            setTextColor(ArthSaathiV7Design.NAVY)
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
            setPadding(4, 14, 4, 2)
            contentDescription = "Current page title"
            tag = "canonical-page-title"
        })
        root.addView(TextView(this).apply {
            text = "Step-by-step guidance • Complete each field and use Next to move forward. Your entries are validated before saving."
            textSize = 13f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(4, 2, 4, 10)
        })
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            descendantFocusability = android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
            clipToPadding = false
            setPadding(0, 0, 0, 8)
        }
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 4, 0, 8) }
        scroll.addView(body); root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        root.addView(ArthSaathiV7Design.goldButton(this, "← Home") { finish() })
        setContentView(root)
        when(id) {
            ArthSaathiNavigation.HOME -> home(body)
            ArthSaathiNavigation.REGISTER_CREDIT -> registerCredit(body)
            ArthSaathiNavigation.LOANS_UDHAAR -> loans(body)
            ArthSaathiNavigation.REPAYMENT -> repayment(body)
            ArthSaathiNavigation.QR_KHATA -> qrKhata(body)
            ArthSaathiNavigation.GROUP_KHATA -> groupKhata(body)
            ArthSaathiNavigation.MIS -> mis(body)
            ArthSaathiNavigation.SWITCH_ANALYSIS -> switchAnalysis(body)
            ArthSaathiNavigation.PEOPLE -> people(body)
            ArthSaathiNavigation.ASSET_VAULT -> assetVault(body)
            ArthSaathiNavigation.LIABILITY_VAULT -> liabilityVault(body)
            ArthSaathiNavigation.PORTFOLIO -> simple(body,"PORTFOLIO","Portfolio & Investments",listOf("Investment","Category","Invested value","Current value","Risk / horizon"))
            ArthSaathiNavigation.PROTECTION -> simple(body,"PROTECTION","Insurance & Protection",listOf("Policy / cover","Provider","Coverage","Expiry / renewal","Eligibility"))
            ArthSaathiNavigation.BENEFITS -> simple(body,"BENEFIT","Benefits & Refunds",listOf("Benefit / refund","Source","Value","Status","Evidence"))
            ArthSaathiNavigation.CHARGECHECK -> chargeCheck(body)
            ArthSaathiNavigation.CLAIMS -> claims(body)
            ArthSaathiNavigation.WILL_LEGACY -> willLegacy(body)
            ArthSaathiNavigation.LEGAL -> simple(body,"LEGAL_CASE","Legal Help",listOf("Domain","City / locality","Issue","Related module","Status"))
            ArthSaathiNavigation.ADVOCATES -> advocateDirectory(body)
            ArthSaathiNavigation.DOCUMENT_VAULT -> documentVault(body)
            ArthSaathiNavigation.AI_ADVISOR -> aiAdvisor(body)
            ArthSaathiNavigation.REVENUE -> revenue(body)
            ArthSaathiNavigation.SECURITY_CONSENT -> securityConsent(body)
            ArthSaathiNavigation.INTEGRATIONS -> simple(body,"INTEGRATION","Integrations",listOf("Provider","Capability","Environment","Status","Configuration reference"))
            ArthSaathiNavigation.PROFILE -> simple(body,"PROFILE","Profile & Identity",listOf("Full legal name","Mobile number (10 digits)","Email address","PAN","Aadhaar (last four digits only)","Preferred language"))
            ArthSaathiNavigation.FAMILY -> simple(body,"FAMILY","Family & Nominees",listOf("Family member / nominee name","Relationship","Mobile number (10 digits)","Nomination / relationship notes","Consent status"))
            ArthSaathiNavigation.ADDRESS -> simple(body,"ADDRESS","Address & Geolocation",listOf("Address label","PIN code (6 digits)","City","State","Landmark / address override","Location source / verification status"))
            ArthSaathiNavigation.CREDIT_INTELLIGENCE -> creditIntelligence(body)
            ArthSaathiNavigation.TRADE_CREDIT -> simple(body,"TRADE_CREDIT","Trade Credit",listOf("Supplier / customer","GSTIN","Invoice number","Invoice date","Invoice amount (₹)","Credit period in days","Due date","Source system / reference"))
            ArthSaathiNavigation.ACCOUNTING -> simple(body,"ACCOUNTING_ADAPTER","Accounting & ERP Adapters",listOf("Adapter (Tally / SAP / CSV / REST / webhook)","Source system","Environment (local / test / production)","Connection status","Configuration reference (no secrets)"))
            ArthSaathiNavigation.FORMAL_CREDIT -> simple(body,"FORMAL_CREDIT","Formal Credit & Charge Review",listOf("Lender / institution","Loan product","Sanctioned principal (₹)","Sanctioned interest rate (%)","Actual interest / charges (₹)","Evidence document reference","Review status"))
            ArthSaathiNavigation.FUNDING -> simple(body,"FUNDING_REQUEST","Funding Marketplace",listOf("Funding purpose","Requested amount (₹)","Business / personal category","Eligibility notes","Consent reference for profile sharing","Request status"))
            ArthSaathiNavigation.GUARANTOR -> simple(body,"GUARANTOR","Guarantor Records",listOf("Guarantor full name","Mobile number (10 digits)","Relationship to borrower","Consent / OTP reference","Related credit reference","Document reference"))
            ArthSaathiNavigation.RENTAL -> simple(body,"RENTAL_LEASE","Rental & Lease",listOf("Landlord / tenant","Agreement type (rental / lease)","Monthly rent (₹)","Security deposit (₹)","Start date","End / renewal date","Agreement document reference"))
            ArthSaathiNavigation.ALERTS -> simple(body,"ALERT","Alerts & Notifications",listOf("Alert title","Related module","Due date","Priority","Status","Notes"))
            else -> body.addView(TextView(this).apply { text = "Canonical module"; textSize = 17f })
        }
        styleActionButtons(body)
    }

    private fun styleActionButtons(parent: android.view.ViewGroup) {
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            if (child is Button) {
                child.isAllCaps = false
                child.textSize = 14f
                child.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                child.minHeight = ArthSaathiV7Design.dp(this, 46)
                child.setTextColor(android.graphics.Color.WHITE)
                child.background = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(ArthSaathiV7Design.TEAL, ArthSaathiV7Design.NAVY_3)
                ).apply {
                    cornerRadius = ArthSaathiV7Design.dp(this@ArthSaathiMasterModuleActivity, 13).toFloat()
                }
                child.elevation = ArthSaathiV7Design.dp(this, 2).toFloat()
            } else if (child is android.view.ViewGroup) {
                styleActionButtons(child)
            }
        }
    }

    private fun home(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "Your Money. Your Records. Your Rights.\n\nOne canonical destination per module. Register Credit creates new credits; Loans & Udhaar owns existing accounts; Repayment Centre is the single repayment engine; MIS is the single cross-module information layer."
            textSize = 16f
        })
        ArthSaathiArchitectureRegistry.modules.filter { it.id != "HOME" }.groupBy { it.area }.forEach { (area, list) ->
            body.addView(TextView(this).apply { text = area; textSize = 18f; setPadding(0,20,0,6) })
            list.forEach { mod ->
                body.addView(Button(this).apply {
                    text = mod.title
                    setOnClickListener { startActivity(android.content.Intent(this@ArthSaathiMasterModuleActivity, ArthSaathiMasterModuleActivity::class.java).putExtra("module",mod.id)) }
                })
            }
        }
    }

    private fun registerCredit(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "NEW CREDIT ONLY\nIdentify party → nature → credit-specific fields → method → repayment → guarantor → document → consent / OTP → registration."
            textSize = 15f
        })
        body.addView(TextView(this).apply {
            text = "Search existing people by name, mobile, PAN or Aadhaar. Select a match to link the credit to that profile. Protected credit history remains hidden until consent."
            textSize = 12f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 4, 0, 8)
        })
        val party = AutoCompleteTextView(this).apply {
            hint = "Borrower / counterparty name"
            setSingleLine(true)
            textSize = 16f
            setTextColor(ArthSaathiV7Design.NAVY)
            setHintTextColor(ArthSaathiV7Design.MUTED)
            setPadding(16, 12, 16, 12)
            minHeight = ArthSaathiV7Design.dp(this@ArthSaathiMasterModuleActivity, 52)
            background = ArthSaathiV7Design.card(this@ArthSaathiMasterModuleActivity, android.graphics.Color.WHITE, 12)
            threshold = 1
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
            setOnFocusChangeListener { view, focused -> if (focused) view.post { parentScrollIntoView(view) } }
        }
        body.addView(party, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = ArthSaathiV7Design.dp(this@ArthSaathiMasterModuleActivity, 10)
        })
        val mobile = field(body, "Counterparty mobile (10 digits)", false, 10)
        val existingPeople = (0 until records.length()).mapNotNull { records.optJSONObject(it) }
            .filter { it.optString("type") == "PERSON" }
        fun personLabel(person: JSONObject): String {
            val pan = person.optString("pan").ifBlank { "PAN —" }
            val aadhaar = person.optString("aadhaar").takeLast(4).let { if (it.isBlank()) "Aadhaar —" else "Aadhaar ••••" + it }
            return person.optString("name") + " • " + person.optString("mobile") + " • " + pan + " • " + aadhaar
        }
        val suggestionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        party.setAdapter(suggestionAdapter)
        var selectedPartyId: String? = null
        var suppressPartyReset = false
        party.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!suppressPartyReset) selectedPartyId = null
                val query = s?.toString()?.trim().orEmpty()
                suggestionAdapter.clear()
                if (query.length >= 1) {
                    existingPeople.filter { person ->
                        listOf("name", "mobile", "pan", "aadhaar", "id").any { key ->
                            person.optString(key).contains(query, ignoreCase = true)
                        }
                    }.take(12).forEach { suggestionAdapter.add(personLabel(it)) }
                }
                suggestionAdapter.notifyDataSetChanged()
                if (query.length >= 2 && suggestionAdapter.count > 0 && party.hasFocus()) {
                    party.post { if (party.hasFocus() && suggestionAdapter.count > 0) party.showDropDown() }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
        party.setOnItemClickListener { parent, _, position, _ ->
            val selectedLabel = parent.getItemAtPosition(position)?.toString().orEmpty()
            val selected = existingPeople.firstOrNull { personLabel(it) == selectedLabel }
            if (selected != null) {
                suppressPartyReset = true
                party.setText(selected.optString("name"), false)
                mobile.setText(selected.optString("mobile"))
                selectedPartyId = selected.optString("id")
                suppressPartyReset = false
            }
        }
        val nature = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ArthSaathiArchitectureRegistry.creditNatureOptions)
            body.addView(it)
        }
        val amount = field(body, "Principal / amount", true)
        val roi = field(body, "ROI %", true)
        val method = field(body, "Method: Cash / UPI / NEFT / Other")
        val terms = field(body, "Frequency (Monthly / Quarterly / Annual)").apply { setText("Monthly") }
        val repaymentModes = listOf("EMI (reducing balance)", "Principal + Interest (simple interest)", "Bullet / Full at End", "Rental / Lease")
        body.addView(TextView(this).apply {
            text = "REPAYMENT CALCULATION METHOD"
            textSize = 12f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(4, 10, 0, 2)
        })
        val repaymentModeSpinner = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, repaymentModes)
            body.addView(it)
        }
        val tenure = field(body, "Tenure (months)", true)
        val guarantor = field(body, "Guarantor (optional)")
        val due = field(body, "Due date").apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { pickDate(this) }
        }
        val doc = field(body, "Document title / ID (optional)")
        nature.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedNature = parent?.getItemAtPosition(position)?.toString().orEmpty()
                val leaseSelected = selectedNature.contains("Rental", true) || selectedNature.contains("Lease", true)
                if (leaseSelected) {
                    amount.hint = "Rent amount per period"
                    roi.visibility = android.view.View.GONE
                    terms.hint = "Rental frequency (Monthly / Quarterly / Annual)"
                    tenure.hint = "Lease duration (months)"
                    doc.hint = "Lease agreement title / ID"
                    val rentalIndex = repaymentModes.indexOfFirst { it.contains("Rental", true) || it.contains("Lease", true) }
                    if (rentalIndex >= 0 && repaymentModeSpinner.selectedItemPosition != rentalIndex) repaymentModeSpinner.setSelection(rentalIndex)
                } else {
                    amount.hint = "Principal / amount"
                    roi.visibility = android.view.View.VISIBLE
                    terms.hint = "Frequency (Monthly / Quarterly / Annual)"
                    tenure.hint = "Tenure (months)"
                    doc.hint = "Document title / ID (optional)"
                    if (repaymentModeSpinner.selectedItemPosition == repaymentModes.lastIndex) repaymentModeSpinner.setSelection(0)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        body.addView(Button(this).apply {
            text = "REGISTER CREDIT"
            setOnClickListener {
                val searchInput = party.text.toString().trim()
                if (searchInput.isEmpty()) { party.error = "Enter or select a borrower / counterparty"; return@setOnClickListener }
                var matchedPerson = selectedPartyId?.let { id -> existingPeople.firstOrNull { it.optString("id") == id } }
                    ?: ArthSaathiCoreEngine.findPersonByIdentity(searchInput)
                var partyName = matchedPerson?.optString("name")?.trim().orEmpty().ifBlank { searchInput }
                var partyMobile = mobile.text.toString().trim().ifBlank { matchedPerson?.optString("mobile").orEmpty() }
                if (matchedPerson != null && partyMobile.isNotBlank() &&
                    matchedPerson.optString("mobile").isNotBlank() && partyMobile != matchedPerson.optString("mobile")) {
                    if (selectedPartyId != null) {
                        mobile.error = "Mobile does not match the selected person profile"; return@setOnClickListener
                    }
                    matchedPerson = null
                }
                if (matchedPerson == null && (
                    ArthSaathiCoreEngine.validateMobile(searchInput) ||
                    ArthSaathiCoreEngine.validatePan(searchInput.uppercase(Locale.US)) ||
                    ArthSaathiCoreEngine.validateAadhaar(searchInput)
                )) {
                    party.error = "Select a matching saved person, or enter their full name"
                    return@setOnClickListener
                }
                if (partyMobile.isEmpty() || !ArthSaathiCoreEngine.validateMobile(partyMobile)) {
                    mobile.error = "A valid 10-digit mobile number is required for borrower consent"
                    return@setOnClickListener
                }
                val principal = amount.text.toString().trim().toDoubleOrNull()
                if (principal == null || !principal.isFinite() || principal <= 0.0) {
                    amount.error = "Enter an amount greater than zero"; return@setOnClickListener
                }
                val selectedNature = nature.selectedItem.toString()
                val lease = selectedNature.contains("Rental", true) || selectedNature.contains("Lease", true)
                val rate = if (lease) 0.0 else roi.text.toString().trim().toDoubleOrNull()
                if (rate == null || !rate.isFinite() || rate < 0.0 || rate > 100.0) {
                    roi.error = "ROI must be between 0 and 100%"; return@setOnClickListener
                }
                if (method.text.toString().trim().isEmpty()) {
                    method.error = "Enter how the credit was given"; return@setOnClickListener
                }
                val repaymentTerms = terms.text.toString().trim()
                val allowedFrequencies = listOf("Monthly", "Quarterly", "Half-yearly", "Annual")
                val normalizedFrequency = allowedFrequencies.firstOrNull { it.equals(repaymentTerms, true) }
                if (normalizedFrequency == null) {
                    terms.error = "Choose Monthly, Quarterly, Half-yearly or Annual"; return@setOnClickListener
                }
                val repaymentMode = repaymentModeSpinner.selectedItem?.toString().orEmpty()
                if (!lease && repaymentMode.contains("Rental", true)) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Choose Rental / Lease as the credit nature to use rental calculations.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (repaymentMode.contains("EMI", true) && !normalizedFrequency.equals("Monthly", true)) {
                    terms.error = "EMI uses a monthly schedule; select Monthly frequency"; return@setOnClickListener
                }
                val tenureMonths = tenure.text.toString().trim().toIntOrNull() ?: 0
                if (tenureMonths <= 0 || tenureMonths > 600) {
                    tenure.error = if (lease) "Enter lease duration from 1 to 600 months" else "Enter tenure from 1 to 600 months"
                    return@setOnClickListener
                }
                var documentId = ""
                val documentQuery = doc.text.toString().trim()
                val matchingDocument = if (documentQuery.isBlank()) null else
                    (0 until records.length()).mapNotNull { records.optJSONObject(it) }.firstOrNull { record ->
                        record.optString("type") == "DOCUMENT" &&
                            (record.optString("id") == documentQuery || record.optString("f0").equals(documentQuery, true))
                    }
                if (documentQuery.isNotBlank() && matchingDocument == null) {
                    doc.error = "No saved Document Vault item matches this title / ID"
                    return@setOnClickListener
                }
                if (lease && matchingDocument == null) {
                    doc.error = "Store the lease agreement in Document Vault and enter its title / ID"
                    return@setOnClickListener
                }
                documentId = matchingDocument?.optString("id").orEmpty()
                try {
                    val partyId = matchedPerson?.optString("id")?.takeIf { it.isNotBlank() }
                        ?: ArthSaathiCoreEngine.createPerson(partyName, partyMobile, "Borrower / counterparty").id
                    val guarantorName = guarantor.text.toString().trim()
                    val guarantorPerson = if (guarantorName.isBlank()) null else ArthSaathiCoreEngine.findPersonByIdentity(guarantorName)
                    val guarantorId = if (guarantorName.isBlank()) null else
                        guarantorPerson?.optString("id")?.takeIf { it.isNotBlank() }
                            ?: ArthSaathiCoreEngine.createPerson(guarantorName, "", "Guarantor").id
                    val result = ArthSaathiCoreEngine.createCredit(
                        partyId, partyName, selectedNature, principal, rate,
                        method.text.toString().trim(), normalizedFrequency,
                        due.text.toString().trim(), guarantorId, documentId.ifBlank { null },
                        partyMobile, guarantorName, repaymentMode, tenureMonths
                    )
                    if (!result.ok) {
                        Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        "Credit saved as pending consent. It becomes active only after verified counterparty consent.",
                        Toast.LENGTH_LONG).show()
                    render(ArthSaathiNavigation.LOANS_UDHAAR)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        e.message ?: "Please review the credit details.", Toast.LENGTH_LONG).show()
                } catch (e: IllegalStateException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        e.message ?: "Sign in again before saving.", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun loans(body: LinearLayout) {
        body.addView(TextView(this).apply { text="ACCOUNT CENTRE — active and closed credits. Tap an account for full details."; textSize=16f })
        var found=false
        for(i in 0 until records.length()){
            val o=records.getJSONObject(i); if(o.optString("type")!="CREDIT") continue
            found=true
            val outstanding=(o.optDouble("totalPayable",o.optDouble("amount"))-o.optDouble("paid")).coerceAtLeast(0.0)
            val state = when (o.optString("status")) {
                "PENDING_CONSENT" -> "Draft • Awaiting consent"
                "CLOSED" -> "Closed"
                else -> "Active"
            }
            val balanceLine = if (o.optString("status") == "PENDING_CONSENT") "Not registered yet" else "Outstanding ₹" + money(outstanding)
            body.addView(Button(this).apply { text=state+"\n"+o.optString("party")+" • "+o.optString("nature")+"\n"+balanceLine; setOnClickListener{detail(o)} })
        }
        if (!found) {
            body.addView(ArthSaathiV7Design.text(this,
                "Your credit account centre will show registered credit, consent state, total payable and outstanding balance here.",
                13f, ArthSaathiV7Design.MUTED).apply { setPadding(0, 12, 0, 12) })
            body.addView(ArthSaathiV7Design.goldButton(this, "REGISTER YOUR FIRST CREDIT") {
                render(ArthSaathiNavigation.REGISTER_CREDIT)
            })
        }
    }

    private fun detail(o:JSONObject){
        val out=(o.optDouble("totalPayable",o.optDouble("amount"))-o.optDouble("paid")).coerceAtLeast(0.0)
        AlertDialog.Builder(this).setTitle("Account "+o.optString("id")).setMessage(
            "Party: "+o.optString("party")+"\nNature: "+o.optString("nature")+"\nAmount: ₹"+money(o.optDouble("amount"))+
            "\nContractual payable: ₹"+money(o.optDouble("totalPayable",o.optDouble("amount")))+"\nROI: "+o.optDouble("roi")+"%\nRepayment mode: "+o.optString("repaymentMode",o.optString("terms"))+"\nMethod: "+o.optString("method")+"\nTerms: "+o.optString("terms", o.optString("repaymentTerms"))+
            "\nGuarantor: "+o.optString("guarantorName", o.optString("guarantor"))+"\nDue: "+o.optString("dueDate")+"\nDocument: "+o.optString("documentId", o.optString("document"))+
            "\nPaid: ₹"+money(o.optDouble("paid"))+"\nOutstanding: ₹"+money(out)+"\nConsent: "+o.optString("consentStatus")+"\nStatus: "+o.optString("status")
        ).setPositiveButton("OK",null).show()
    }

    private fun repayment(body:LinearLayout){
        body.addView(TextView(this).apply {
            text = "SINGLE REPAYMENT ENGINE\nProduction repayment changes require verified consent and a valid amount/method."
            textSize = 16f
        })
        var eligibleAccounts = 0
        for(i in 0 until records.length()){
            val o=records.getJSONObject(i)
            if(o.optString("type")!="CREDIT" || o.optString("status")!="ACTIVE") continue
            eligibleAccounts++
            body.addView(Button(this).apply {
                text="Record repayment • "+o.optString("party")
                isAllCaps = false
                setOnClickListener {
                    val amountField = EditText(this@ArthSaathiMasterModuleActivity).apply {
                        hint = "Repayment amount"
                        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    }
                    val methodField = EditText(this@ArthSaathiMasterModuleActivity).apply {
                        hint = "Method: Cash / UPI / NEFT / Other"
                    }
                    val form = LinearLayout(this@ArthSaathiMasterModuleActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(32, 8, 32, 0)
                        addView(amountField)
                        addView(methodField)
                    }
                    AlertDialog.Builder(this@ArthSaathiMasterModuleActivity)
                        .setTitle("Consent-gated repayment")
                        .setView(form)
                        .setNegativeButton("CANCEL", null)
                        .setPositiveButton("CONFIRM") { _, _ ->
                            val consent = o.optString("consentStatus")
                            if (consent != "CONSENTED") {
                                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                                    "Consent is required before repayment. Use Security & Consent first.",
                                    Toast.LENGTH_LONG).show()
                                return@setPositiveButton
                            }
                            val amount = amountField.text.toString().trim().toDoubleOrNull()
                            if (amount == null || !amount.isFinite() || amount <= 0.0) {
                                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                                    "Enter a repayment amount greater than zero.", Toast.LENGTH_LONG).show()
                                return@setPositiveButton
                            }
                            // Repayment validation must use contractual payable (principal + applicable interest),
                            // matching the canonical repayment engine and the account-centre balance.
                            val outstanding = (o.optDouble("totalPayable", o.optDouble("amount")) -
                                o.optDouble("paid")).coerceAtLeast(0.0)
                            if (amount > outstanding + 0.005) {
                                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                                    "Repayment exceeds outstanding ₹" + money(outstanding) + ".", Toast.LENGTH_LONG).show()
                                return@setPositiveButton
                            }
                            val method = methodField.text.toString().trim()
                            if (method.isEmpty()) {
                                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                                    "Enter the repayment method.", Toast.LENGTH_LONG).show()
                                return@setPositiveButton
                            }
                            val challenge = ArthSaathiCoreEngine.requestRepaymentOtp(
                                o.optString("id"), "lender"
                            )
                            if (!challenge.ok || challenge.id.isNullOrBlank()) {
                                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                                    challenge.message, Toast.LENGTH_LONG).show()
                                return@setPositiveButton
                            }
                            val otpField = EditText(this@ArthSaathiMasterModuleActivity).apply {
                                hint = "OTP sent to borrower mobile"
                                inputType = InputType.TYPE_CLASS_NUMBER
                                filters = arrayOf(InputFilter.LengthFilter(8))
                            }
                            AlertDialog.Builder(this@ArthSaathiMasterModuleActivity)
                                .setTitle("Verify borrower repayment OTP")
                                .setMessage("Enter the code sent by the configured provider to the borrower's registered mobile. No repayment is saved until verification succeeds.")
                                .setView(otpField)
                                .setNegativeButton("CANCEL", null)
                                .setPositiveButton("VERIFY & RECORD") { _, _ ->
                                    try {
                                        val result = ArthSaathiCoreEngine.repayment(
                                            o.optString("id"), amount, method, "lender",
                                            challenge.id, otpField.text.toString().trim()
                                        )
                                        Toast.makeText(this@ArthSaathiMasterModuleActivity,
                                            result.message, Toast.LENGTH_LONG).show()
                                        if (result.ok) render(ArthSaathiNavigation.REPAYMENT)
                                    } catch (e: IllegalArgumentException) {
                                        Toast.makeText(this@ArthSaathiMasterModuleActivity,
                                            e.message ?: "Repayment was rejected; no change was made.",
                                            Toast.LENGTH_LONG).show()
                                    } catch (e: IllegalStateException) {
                                        Toast.makeText(this@ArthSaathiMasterModuleActivity,
                                            e.message ?: "Sign in again before recording repayment.",
                                            Toast.LENGTH_LONG).show()
                                    }
                                }.show()
                        }.show()
                }
            })
        }
        if (eligibleAccounts == 0) {
            body.addView(ArthSaathiV7Design.text(this,
                "No active credit accounts are eligible for repayment yet. Register a credit first, then complete the required consent flow. Repayment actions will appear here when an account is active.",
                13f, ArthSaathiV7Design.MUTED).apply { setPadding(0, 12, 0, 12) })
            body.addView(ArthSaathiV7Design.goldButton(this, "GO TO CREDIT REGISTRATION") {
                render(ArthSaathiNavigation.REGISTER_CREDIT)
            })
            body.addView(ArthSaathiV7Design.outlineButton(this, "OPEN CONSENT CENTRE") {
                render(ArthSaathiNavigation.SECURITY_CONSENT)
            })
        }
    }

    private fun chargeCheck(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "CHARGECHECK — record sanctioned and actual charges. Variance is calculated as actual minus sanctioned; do not enter it manually."
            textSize = 16f
        })
        val account = field(body, "Account / facility")
        val sanctioned = field(body, "Sanctioned charge", true)
        val actual = field(body, "Actual charge", true)
        val evidence = field(body, "Evidence / document reference")
        body.addView(Button(this).apply {
            text = "CALCULATE & SAVE VARIANCE"
            isAllCaps = false
            setOnClickListener {
                if (account.text.toString().trim().isEmpty()) { account.error = "Account / facility is required"; return@setOnClickListener }
                val expected = sanctioned.text.toString().trim().toDoubleOrNull()
                if (expected == null || !expected.isFinite() || expected < 0.0) { sanctioned.error = "Enter a valid non-negative amount"; return@setOnClickListener }
                val charged = actual.text.toString().trim().toDoubleOrNull()
                if (charged == null || !charged.isFinite() || charged < 0.0) { actual.error = "Enter a valid non-negative amount"; return@setOnClickListener }
                val variance = kotlin.math.round((charged - expected) * 100.0) / 100.0
                try {
                    ArthSaathiCoreEngine.saveModule("CHARGECHECK", mapOf(
                        "f0" to account.text.toString().trim(), "f1" to expected, "f2" to charged,
                        "f3" to variance, "f4" to evidence.text.toString().trim()
                    ))
                    val explanation = if (variance > 0.0) "Excess charge recorded" else if (variance < 0.0) "Actual charge is below sanctioned amount" else "No variance"
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "$explanation: ₹" + money(kotlin.math.abs(variance)), Toast.LENGTH_LONG).show()
                    render(ArthSaathiNavigation.CHARGECHECK)
                } catch (e: IllegalStateException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Sign in again before saving.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "Saved comparisons"; textSize = 18f; setPadding(0, 18, 0, 6) })
        for (i in 0 until records.length()) {
            val r = records.optJSONObject(i) ?: continue
            if (r.optString("type") != "CHARGECHECK") continue
            val expected = r.optDouble("f1")
            val charged = r.optDouble("f2")
            val variance = r.optDouble("f3", charged - expected)
            body.addView(TextView(this).apply {
                text = r.optString("f0") + "\nSanctioned ₹" + money(expected) + " • Actual ₹" + money(charged) +
                    " • Variance ₹" + money(variance) + "\n" + r.optString("f4")
                setPadding(0, 8, 0, 8)
            })
        }
    }

    private fun revenue(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "REVENUE & PAYMENTS • Local ledger and pre-live setup. Actual gateway settlement/synchronization remains disabled until go-live."
            textSize = 16f
        })
        body.addView(TextView(this).apply { text = "PAYMENT SETUP (saved locally; no secret keys stored)"; textSize = 18f; setPadding(0, 18, 0, 6) })
        val provider = field(body, "Gateway provider / payment partner")
        val merchantId = field(body, "Merchant / account ID (non-secret)")
        val environment = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("Pre-live / not connected", "Sandbox configured", "Manual collection only"))
            body.addView(it)
        }
        val bankName = field(body, "Bank name")
        val accountHolder = field(body, "Bank account holder")
        val accountLast4 = field(body, "Bank account last 4 digits", false, 4)
        val ifsc = field(body, "IFSC (11 characters)")
        val settlementRef = field(body, "Settlement account / UPI reference")
        body.addView(Button(this).apply {
            text = "SAVE PAYMENT SETUP"
            isAllCaps = false
            setOnClickListener {
                val p = provider.text.toString().trim()
                val mid = merchantId.text.toString().trim()
                val bank = bankName.text.toString().trim()
                val holder = accountHolder.text.toString().trim()
                val last4 = accountLast4.text.toString().trim()
                val code = ifsc.text.toString().trim().uppercase(Locale.US)
                val settlement = settlementRef.text.toString().trim()
                if (p.isBlank() && bank.isBlank() && settlement.isBlank()) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Enter a gateway provider, bank name or settlement reference.", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (bank.isNotBlank() && (holder.isBlank() || !last4.matches(Regex("[0-9]{4}")) || !code.matches(Regex("[A-Z]{4}0[A-Z0-9]{6}")))) {
                    if (holder.isBlank()) accountHolder.error = "Required when adding a bank account"
                    if (!last4.matches(Regex("[0-9]{4}"))) accountLast4.error = "Enter only the final 4 digits"
                    if (!code.matches(Regex("[A-Z]{4}0[A-Z0-9]{6}"))) ifsc.error = "Enter a valid 11-character IFSC"
                    return@setOnClickListener
                }
                try {
                    ArthSaathiCoreEngine.saveModule("REVENUE_CONFIG", mapOf(
                        "gatewayProvider" to p, "merchantId" to mid,
                        "environment" to environment.selectedItem.toString(),
                        "bankName" to bank, "accountHolder" to holder, "accountLast4" to last4,
                        "ifsc" to code, "settlementReference" to settlement,
                        "gatewayConnected" to false
                    ))
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Payment setup saved locally. Live gateway connection is deferred until go-live.", Toast.LENGTH_LONG).show()
                    render(ArthSaathiNavigation.REVENUE)
                } catch (e: IllegalStateException) { Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Sign in before saving.", Toast.LENGTH_LONG).show() }
            }
        })
        val all = records
        val config = (0 until all.length()).mapNotNull { all.optJSONObject(it) }.lastOrNull { it.optString("type") == "REVENUE_CONFIG" }
        if (config != null) body.addView(TextView(this).apply {
            text = "Saved setup: " + config.optString("gatewayProvider", "Manual") +
                " • " + config.optString("environment") +
                (if (config.optString("bankName").isNotBlank()) "\nBank: " + config.optString("bankName") + " • ****" + config.optString("accountLast4") + " • " + config.optString("ifsc") else "") +
                "\nGateway status: Not connected (go-live step pending)"
            setPadding(8, 8, 8, 12)
        })
        body.addView(TextView(this).apply { text = "RECORD A PAYMENT / CHARGE"; textSize = 18f; setPadding(0, 18, 0, 6) })
        val service = field(body, "Service / transaction")
        val charge = field(body, "Charge amount", true)
        val status = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("PENDING", "PAID", "FAILED", "REFUNDED"))
            body.addView(it)
        }
        val reference = field(body, "Receipt / gateway reference")
        val notes = field(body, "Notes")
        body.addView(Button(this).apply {
            text = "SAVE PAYMENT RECORD"
            isAllCaps = false
            setOnClickListener {
                if (service.text.toString().trim().isEmpty()) { service.error = "Service / transaction is required"; return@setOnClickListener }
                val amount = charge.text.toString().trim().toDoubleOrNull()
                if (amount == null || !amount.isFinite() || amount <= 0.0) { charge.error = "Charge must be greater than zero"; return@setOnClickListener }
                val paymentStatus = status.selectedItem.toString()
                if (paymentStatus == "PAID" && reference.text.toString().trim().isEmpty()) {
                    reference.error = "Enter a receipt or payment reference for a paid record"; return@setOnClickListener
                }
                try {
                    ArthSaathiCoreEngine.saveModule("REVENUE", mapOf(
                        "f0" to service.text.toString().trim(), "f1" to amount, "f2" to paymentStatus,
                        "f3" to reference.text.toString().trim(), "f4" to notes.text.toString().trim()
                    ))
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Payment record saved as $paymentStatus", Toast.LENGTH_SHORT).show()
                    render(ArthSaathiNavigation.REVENUE)
                } catch (e: IllegalStateException) { Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Sign in before saving.", Toast.LENGTH_LONG).show() }
            }
        })
        body.addView(TextView(this).apply { text = "Recorded service charges"; textSize = 18f; setPadding(0, 18, 0, 6) })
        for (i in 0 until records.length()) {
            val r = records.optJSONObject(i) ?: continue
            if (r.optString("type") != "REVENUE") continue
            body.addView(TextView(this).apply {
                text = r.optString("f0") + " • ₹" + money(r.optDouble("f1")) + " • " + r.optString("f2") + "\n" + r.optString("f3")
                setPadding(0, 8, 0, 8)
            })
        }
    }

    /** Consent is a real provider-backed workflow, not a generic editable status field. */
    private fun securityConsent(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "SECURITY & CONSENT\nRequest a trusted-provider OTP for the borrower and verify it here. A typed status alone never grants consent."
            textSize = 16f
        })
        val credits = (0 until records.length()).mapNotNull { records.optJSONObject(it) }
            .filter { it.optString("type") == "CREDIT" }
        if (credits.isEmpty()) {
            body.addView(ArthSaathiV7Design.text(this,
                "Consent records are created when you register a credit. Borrower consent is required before protected credit history is disclosed or consent-gated repayment actions are accepted.",
                13f, ArthSaathiV7Design.MUTED).apply { setPadding(0, 12, 0, 12) })
            body.addView(ArthSaathiV7Design.goldButton(this, "REGISTER CREDIT") {
                render(ArthSaathiNavigation.REGISTER_CREDIT)
            })
            return
        }
        credits.forEach { record ->
            val party = record.optString("party", "Borrower")
            val status = record.optString("consentStatus", "PENDING")
            body.addView(TextView(this).apply {
                text = "\n" + party + " • " + record.optString("nature") +
                    "\nMobile: " + record.optString("partyMobile", "Not provided") +
                    "\nConsent status: " + status
                textSize = 14f
            })
            if (status != "CONSENTED") {
                if (status == "REQUESTED") {
                    val code = field(body, "OTP for " + party, false, 8)
                    body.addView(Button(this).apply {
                        text = "VERIFY CONSENT • " + party
                        isAllCaps = false
                        setOnClickListener {
                            val result = ArthSaathiCoreEngine.confirmConsent(
                                record.optString("id"), code.text.toString().trim(), "borrower"
                            )
                            Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_LONG).show()
                            if (result.ok) render(ArthSaathiNavigation.SECURITY_CONSENT)
                        }
                    })
                }
                body.addView(Button(this).apply {
                    text = if (status == "REQUESTED") "RESEND CONSENT OTP • " + party else "REQUEST CONSENT OTP • " + party
                    isAllCaps = false
                    setOnClickListener {
                        val result = ArthSaathiCoreEngine.requestConsent(record.optString("id"), "lender")
                        Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_LONG).show()
                        if (result.ok) render(ArthSaathiNavigation.SECURITY_CONSENT)
                    }
                })
            }
        }
    }

    /**
     * Privacy-first credit intelligence. A typed consent reference or status label is
     * never sufficient: the locally registered credit must carry a completed consent
     * state from the dedicated consent workflow before any financial history is shown.
     */
    private fun creditIntelligence(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "Credit history and score details are private. They are shown only for a registered credit whose borrower consent has been verified through Security & Consent."
            textSize = 14f
            setPadding(0, 4, 0, 12)
        })
        val credits = (0 until records.length()).mapNotNull { records.optJSONObject(it) }
            .filter { it.optString("type") == "CREDIT" }
        if (credits.isEmpty()) {
            body.addView(TextView(this).apply {
                text = "No registered credit records are available. Register a credit first; no history is inferred from a name or typed reference."
                textSize = 14f
                setPadding(0, 8, 0, 12)
            })
            body.addView(Button(this).apply {
                text = "REGISTER CREDIT"
                setOnClickListener { render(ArthSaathiNavigation.REGISTER_CREDIT) }
            })
            return
        }
        credits.forEach { credit ->
            val consented = credit.optString("consentStatus") == "CONSENTED"
            body.addView(TextView(this).apply {
                text = credit.optString("party", "Counterparty") + " • " +
                    credit.optString("nature", "Credit") + "\nConsent: " +
                    if (consented) "Verified" else "Not verified"
                textSize = 15f
                setPadding(0, 12, 0, 6)
            })
            if (!consented) {
                body.addView(TextView(this).apply {
                    text = "Protected details hidden until borrower consent is verified. A manually entered consent reference cannot unlock this record."
                    textSize = 13f
                    setPadding(8, 4, 8, 8)
                })
                body.addView(Button(this).apply {
                    text = "OPEN SECURITY & CONSENT"
                    setOnClickListener { render(ArthSaathiNavigation.SECURITY_CONSENT) }
                })
            } else {
                val payable = credit.optDouble("totalPayable", credit.optDouble("amount"))
                val paid = credit.optDouble("paid")
                val outstanding = (payable - paid).coerceAtLeast(0.0)
                body.addView(TextView(this).apply {
                    text = "Registered amount: ₹" + money(credit.optDouble("amount")) +
                        "\nContractual payable: ₹" + money(payable) +
                        "\nRepaid: ₹" + money(paid) +
                        "\nOutstanding: ₹" + money(outstanding) +
                        "\nAccount status: " + credit.optString("status", "UNKNOWN")
                    textSize = 14f
                    setPadding(8, 4, 8, 8)
                })
                val payments = (0 until records.length()).mapNotNull { records.optJSONObject(it) }
                    .filter { it.optString("type") == "REPAYMENT" &&
                        it.optString("recordId") == credit.optString("id") }
                body.addView(TextView(this).apply {
                    text = "Verified repayment transactions: " + payments.size
                    textSize = 13f
                    setPadding(8, 0, 8, 8)
                })
            }
        }
    }

    private fun mis(body:LinearLayout){
        val metrics = ArthSaathiCoreEngine.mis()
        body.addView(TextView(this).apply {
            text = "MIS / MONEY REPORT • ACTUAL RECORDED VALUES"
            textSize = 18f
        })
        val rows = listOf(
            "Credits registered" to metrics.credits,
            "Repayments recorded" to metrics.repayments,
            "Credit outstanding" to metrics.outstanding,
            "Group expenses" to metrics.groupExpenses,
            "Group contributions collected" to metrics.groupCollected,
            "Outstanding group shares" to metrics.groupOutstandingShares,
            "Assets / portfolio (current value)" to metrics.assets,
            "Financial assets" to metrics.financialAssets,
            "Non-financial assets" to metrics.nonFinancialAssets,
            "Portfolio value" to metrics.portfolioValue,
            "Liabilities recorded" to metrics.liabilities,
            "Benefits / refunds value generated" to metrics.benefits,
            "ArthSaathi service revenue (net of refunds)" to metrics.revenue,
            "Actual charges checked" to metrics.actualCharges,
            "Net charge variance (actual − sanctioned)" to metrics.chargeVariance
        )
        rows.forEach { (label, value) ->
            body.addView(TextView(this).apply {
                text = label + ": ₹" + money(value)
                textSize = 15f
                setPadding(0, 8, 0, 8)
                contentDescription = label + " " + money(value)
            })
        }
        // Keep the aggregate reconciliation and the underlying recorded positions visible together.
        // This lets a user trace the MIS total to each actual asset/portfolio entry.
        val records = ArthSaathiDataStore.records()
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            val kind = record.optString("type")
            if (kind != "ASSET" && kind != "PORTFOLIO") continue
            val name = record.optString("f0").ifBlank {
                record.optString("name").ifBlank { record.optString("assetName").ifBlank { kind } }
            }
            val value = if (kind == "PORTFOLIO") {
                record.optDouble("currentValue", record.optDouble("f3", record.optDouble("value", record.optDouble("f2", 0.0))))
            } else {
                record.optDouble("value", record.optDouble("f2", record.optDouble("currentValue", 0.0)))
            }
            body.addView(TextView(this).apply {
                text = "$kind detail — $name: ₹" + money(value)
                textSize = 13f
                setPadding(0, 3, 0, 3)
                contentDescription = "$kind $name " + money(value)
            })
        }
        body.addView(TextView(this).apply {
            text = "Charts below are derived from these same account-scoped ledger values."
            textSize = 12f
        })
        body.addView(ArthSaathiMisChartsView(this, metrics))
    }

    private fun switchAnalysis(body:LinearLayout){
        body.addView(TextView(this).apply{text="PORTFOLIO SWITCH ANALYSIS\nCompare return, cost, risk and opportunity cost before suggesting a switch. No automatic money movement.";textSize=16f})
        val current=field(body,"Current annual return %",true)
        val alternative=field(body,"Alternative annual return %",true)
        val value=field(body,"Amount considered",true)
        val currentCost=field(body,"Current annual fees / costs (₹)",true)
        val alternativeCost=field(body,"Alternative annual fees / costs (₹)",true)
        body.addView(Button(this).apply{text="CALCULATE OPPORTUNITY COST";isAllCaps=false;setOnClickListener{
            val c=current.text.toString().trim().toDoubleOrNull()
            if(c==null||!c.isFinite()||c !in 0.0..100.0){current.error="Enter a return from 0 to 100%";return@setOnClickListener}
            val a=alternative.text.toString().trim().toDoubleOrNull()
            if(a==null||!a.isFinite()||a !in 0.0..100.0){alternative.error="Enter a return from 0 to 100%";return@setOnClickListener}
            val v=value.text.toString().trim().toDoubleOrNull()
            if(v==null||!v.isFinite()||v<=0.0){value.error="Enter an amount greater than zero";return@setOnClickListener}
            val cc=currentCost.text.toString().trim().toDoubleOrNull()
            if(cc==null||!cc.isFinite()||cc<0.0){currentCost.error="Enter a non-negative annual cost";return@setOnClickListener}
            val ac=alternativeCost.text.toString().trim().toDoubleOrNull()
            if(ac==null||!ac.isFinite()||ac<0.0){alternativeCost.error="Enter a non-negative annual cost";return@setOnClickListener}
            val analysis=ArthSaathiCoreEngine.switchAnalysis(v,c,a,cc,ac)
            AlertDialog.Builder(this@ArthSaathiMasterModuleActivity).setTitle("Explainable comparison").setMessage(
                "Current annual return: "+String.format(Locale.US,"%.2f%%",c)+
                "\nAlternative annual return: "+String.format(Locale.US,"%.2f%%",a)+
                "\nReturn difference: "+String.format(Locale.US,"%.2f",analysis.optDouble("returnDifferencePct"))+" percentage points"+
                "\nGross annual opportunity difference: ₹"+money(analysis.optDouble("annualOpportunityDifference"))+
                "\nAlternative minus current annual cost: ₹"+money(analysis.optDouble("costDifference"))+
                "\nIndicative annual difference after entered costs: ₹"+money(analysis.optDouble("netIndicativeDifference"))+
                "\nRisk, tax, liquidity, exit fees and eligibility still require review. This is not a guaranteed return or an instruction to switch."
            ).setPositiveButton("OK",null).show()
        }})
    }

    private fun people(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "Create a person record. Mobile numbers are validated before they are stored."
            textSize = 15f
        })
        val name = field(body, "Full name")
        val mobile = field(body, "10-digit mobile", false, 10)
        val role = field(body, "Role / relationship")
        val address = field(body, "Address / locality")
        val pin = field(body, "6-digit PIN code (optional)", false, 6).apply { inputType = InputType.TYPE_CLASS_NUMBER }
        val district = field(body, "District / city (PIN lookup)")
        val state = field(body, "State (PIN lookup)")
        var lastPinLookupRequested = ""
        fun lookupPin(force: Boolean = false) {
            val pinValue = pin.text.toString().trim()
            if (!pinValue.matches(Regex("[1-9][0-9]{5}"))) {
                pin.error = "Enter a valid 6-digit Indian PIN code"
                return
            }
            if (!force && lastPinLookupRequested == pinValue) return
            lastPinLookupRequested = pinValue
            Toast.makeText(this@ArthSaathiMasterModuleActivity, "Looking up PIN code…", Toast.LENGTH_SHORT).show()
            Thread {
                var districtValue = ""
                var stateValue = ""
                var localityValue = ""
                var failure = ""
                try {
                    val connection = java.net.URL("https://api.postalpincode.in/pincode/$pinValue")
                        .openConnection() as javax.net.ssl.HttpsURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 6000
                    connection.readTimeout = 6000
                    connection.setRequestProperty("Accept", "application/json")
                    try {
                        val code = connection.responseCode
                        if (code !in 200..299) throw IllegalStateException("PIN lookup service returned HTTP $code")
                        val payload = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        val results = org.json.JSONArray(payload)
                        val first = results.optJSONObject(0)
                        val offices = first?.optJSONArray("PostOffice")
                        val office = if (first?.optString("Status") == "Success" && offices != null && offices.length() > 0) offices.optJSONObject(0) else null
                        if (office == null) throw IllegalStateException("No post office found for this PIN code")
                        districtValue = office.optString("District")
                        stateValue = office.optString("State")
                        localityValue = listOf(office.optString("Name"), office.optString("Block"))
                            .filter { it.isNotBlank() && it != "NA" }.distinct().joinToString(", ")
                    } finally {
                        connection.disconnect()
                    }
                } catch (e: Exception) {
                    failure = e.message ?: "PIN lookup unavailable; enter the address manually"
                }
                runOnUiThread {
                    if (pin.text.toString().trim() != pinValue) return@runOnUiThread
                    if (failure.isNotBlank()) {
                        Toast.makeText(this@ArthSaathiMasterModuleActivity, failure, Toast.LENGTH_LONG).show()
                    } else {
                        district.setText(districtValue)
                        state.setText(stateValue)
                        if (address.text.toString().isBlank() && localityValue.isNotBlank()) address.setText(localityValue)
                        Toast.makeText(this@ArthSaathiMasterModuleActivity, "District, state and locality filled from PIN code", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
        pin.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val value = s?.toString().orEmpty()
                if (value.length != 6) lastPinLookupRequested = ""
                if (value.matches(Regex("[1-9][0-9]{5}")) && value != lastPinLookupRequested) lookupPin()
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
        body.addView(Button(this).apply {
            text = "LOOK UP PIN / RETRY"
            setOnClickListener { lookupPin(force = true) }
        })
        val email = field(body, "Email (optional)")
        val pan = field(body, "PAN (optional)")
        val aadhaar = field(body, "Aadhaar (12 digits, optional)", true, 12).apply { inputType = InputType.TYPE_CLASS_NUMBER }
        val gstin = field(body, "GSTIN (optional)")
        body.addView(Button(this).apply {
            text = "SAVE PERSON"
            setOnClickListener {
                if (name.text.toString().trim().isEmpty()) {
                    name.error = "Name is required"; return@setOnClickListener
                }
                val phone = mobile.text.toString().trim()
                if (phone.isNotEmpty() && !ArthSaathiCoreEngine.validateMobile(phone)) {
                    mobile.error = "Enter a valid 10-digit mobile number"; return@setOnClickListener
                }
                val pinValue = pin.text.toString().trim()
                if (pinValue.isNotBlank() && !pinValue.matches(Regex("[1-9][0-9]{5}"))) {
                    pin.error = "Enter a valid 6-digit Indian PIN code"; return@setOnClickListener
                }
                if (pinValue.isNotBlank() && address.text.toString().isBlank() &&
                    (district.text.toString().isBlank() || state.text.toString().isBlank())) {
                    pin.error = "Wait for PIN lookup or enter the address manually"; return@setOnClickListener
                }
                val emailValue = email.text.toString().trim()
                if (!ArthSaathiCoreEngine.validateEmail(emailValue)) {
                    email.error = "Enter a valid email address"; return@setOnClickListener
                }
                val panValue = pan.text.toString().trim().uppercase(Locale.US)
                if (!ArthSaathiCoreEngine.validatePan(panValue)) {
                    pan.error = "Enter a valid PAN (e.g. ABCDE1234F)"; return@setOnClickListener
                }
                val aadhaarValue = aadhaar.text.toString().trim()
                if (!ArthSaathiCoreEngine.validateAadhaar(aadhaarValue)) {
                    aadhaar.error = "Enter 12 digits; Aadhaar cannot start with 0 or 1"; return@setOnClickListener
                }
                val gstinValue = gstin.text.toString().trim().uppercase(Locale.US)
                if (!ArthSaathiCoreEngine.validateGstin(gstinValue)) {
                    gstin.error = "Enter a valid 15-character GSTIN"; return@setOnClickListener
                }
                try {
                    val result = ArthSaathiCoreEngine.createPerson(
                        name.text.toString().trim(), phone, role.text.toString().trim(),
                        address.text.toString().trim() +
                            listOf(district.text.toString().trim(), state.text.toString().trim()).filter { it.isNotBlank() }.joinToString(", ", prefix = if (address.text.toString().isBlank()) "" else ", "),
                        panValue, gstinValue, pinValue, emailValue, aadhaarValue
                    )
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_SHORT).show()
                    render(ArthSaathiNavigation.PEOPLE)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        e.message ?: "Review the person details.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "Saved people"; textSize = 18f; setPadding(0, 20, 0, 6) })
        val saved = records
        var count = 0
        for (i in 0 until saved.length()) {
            val record = saved.optJSONObject(i) ?: continue
            if (record.optString("type") != "PERSON") continue
            count++
            body.addView(TextView(this).apply {
                text = record.optString("name") + " • " + record.optString("mobile") +
                    if (record.optString("role").isNotBlank()) " • " + record.optString("role") else ""
                setPadding(0, 8, 0, 8)
            })
        }
        if (count == 0) body.addView(TextView(this).apply { text = "No people saved yet." })
    }

    private fun groupKhata(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "GROUP KHATA • shared expenses, individual contributions and settlements. All records stay in this signed-in account."
            textSize = 16f
        })
        val group = field(body, "Group / activity name")
        val totalField = field(body, "Total expense (₹)", true)
        val membersField = field(body, "Number of members", true).apply { inputType = InputType.TYPE_CLASS_NUMBER }
        val memberNamesField = field(body, "Member names (comma-separated)")
        val purpose = field(body, "Purpose / notes")
        val share = TextView(this).apply { text = "Equal share per member: —"; textSize = 18f; setPadding(0, 16, 0, 16) }
        body.addView(share)
        body.addView(Button(this).apply {
            text = "CALCULATE EQUAL SHARE"
            setOnClickListener {
                val total = totalField.text.toString().trim().toDoubleOrNull()
                val count = membersField.text.toString().trim().toIntOrNull()
                if (group.text.toString().trim().isEmpty()) { group.error = "Enter a group or activity"; return@setOnClickListener }
                if (total == null || !total.isFinite() || total <= 0.0) { totalField.error = "Enter an expense greater than zero"; return@setOnClickListener }
                if (count == null || count <= 0 || count > 100000) { membersField.error = "Enter a valid positive member count"; return@setOnClickListener }
                val names = memberNamesField.text.toString().split(",").map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase(Locale.US) }
                if (names.size != count) { memberNamesField.error = "Enter exactly $count distinct member names, separated by commas"; return@setOnClickListener }
                share.text = "Total: ₹" + money(total) + "\nMembers: " + count + "\nEqual share per member: ₹" + money(total / count)
            }
        })
        body.addView(Button(this).apply {
            text = "SAVE GROUP EXPENSE"
            setOnClickListener {
                val groupName = group.text.toString().trim()
                val total = totalField.text.toString().trim().toDoubleOrNull()
                val count = membersField.text.toString().trim().toIntOrNull()
                if (groupName.isEmpty()) { group.error = "Enter a group or activity"; return@setOnClickListener }
                if (total == null || !total.isFinite() || total <= 0.0) { totalField.error = "Enter an expense greater than zero"; return@setOnClickListener }
                if (count == null || count <= 0 || count > 100000) { membersField.error = "Enter a valid positive member count"; return@setOnClickListener }
                val names = memberNamesField.text.toString().split(",").map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase(Locale.US) }
                if (names.size != count) { memberNamesField.error = "Enter exactly $count distinct member names, separated by commas"; return@setOnClickListener }
                try {
                    ArthSaathiCoreEngine.saveModule("GROUP_EXPENSE", mapOf(
                        "f0" to groupName, "f1" to total, "f2" to count,
                        "f3" to purpose.text.toString().trim(), "f4" to memberNamesField.text.toString().trim(), "total" to total, "members" to count
                    ))
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Group expense saved. Add contributions below.", Toast.LENGTH_LONG).show()
                    render(ArthSaathiNavigation.GROUP_KHATA)
                } catch (e: IllegalStateException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Sign in before saving.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "\nSAVED GROUPS • CONTRIBUTIONS • SETTLEMENTS"; textSize = 19f; setPadding(0, 24, 0, 8) })
        for (i in 0 until records.length()) {
            val expense = records.optJSONObject(i) ?: continue
            if (expense.optString("type") != "GROUP_EXPENSE") continue
            val groupId = expense.optString("id")
            val total = expense.optDouble("total", expense.optDouble("f1", 0.0))
            val members = expense.optInt("members", expense.optInt("f2", 0))
            val equalShare = if (members > 0) total / members else 0.0
            val name = expense.optString("f0", expense.optString("group", "Group expense"))
            body.addView(TextView(this).apply {
                text = name + "\nTotal: ₹" + money(total) + " • Members: " + members +
                    "\nEqual share per member: ₹" + money(equalShare) +
                    if (expense.optString("f3").isNotBlank()) "\n" + expense.optString("f3") else ""
                textSize = 15f; setPadding(8, 12, 8, 6)
            })
            val related = ArthSaathiCoreEngine.linked(groupId)
            val contributions = related.filter { it.optString("type") == "GROUP_CONTRIBUTION" }
            val settlements = related.filter { it.optString("type") == "GROUP_SETTLEMENT" }
            val memberNames = expense.optString("f4").split(",").map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase(Locale.US) }
            val balances = calculateGroupBalances(total, memberNames, contributions, settlements)
            val balanceText = if (balances.isEmpty()) "No individual contributions recorded yet." else balances.entries.joinToString("\n") {
                it.key + ": " + if (it.value >= 0.0) "is owed ₹" + money(it.value) else "owes ₹" + money(-it.value)
            }
            body.addView(TextView(this).apply { text = balanceText; setPadding(12, 4, 8, 8) })
            body.addView(Button(this).apply {
                text = "ADD CONTRIBUTION • " + name
                isAllCaps = false
                setOnClickListener { showContributionDialog(groupId, name) }
            })
            body.addView(Button(this).apply {
                text = "RECORD SETTLEMENT • " + name
                isAllCaps = false
                isEnabled = balances.values.any { it > 0.005 } && balances.values.any { it < -0.005 }
                setOnClickListener { showSettlementDialog(groupId, name, total, memberNames, contributions, settlements) }
            })
        }
    }

    private fun showContributionDialog(groupId: String, groupName: String) {
        val person = EditText(this).apply { hint = "Member name" }
        val amount = EditText(this).apply { hint = "Contribution amount"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 8, 32, 0); addView(person); addView(amount) }
        AlertDialog.Builder(this).setTitle("Add contribution • " + groupName).setView(layout)
            .setNegativeButton("CANCEL", null).setPositiveButton("SAVE") { _, _ ->
                val typedName = person.text.toString().trim()
                val value = amount.text.toString().trim().toDoubleOrNull()
                val member = groupMemberNames(groupId).firstOrNull { it.equals(typedName, true) }
                if (member == null || value == null || !value.isFinite() || value <= 0.0) {
                    Toast.makeText(this, "Choose a listed group member and enter a positive amount.", Toast.LENGTH_LONG).show(); return@setPositiveButton
                }
                try {
                    ArthSaathiCoreEngine.saveModule("GROUP_CONTRIBUTION",
                        mapOf("f0" to member, "f1" to value, "groupName" to groupName),
                        mapOf("parentId" to groupId))
                    render(ArthSaathiNavigation.GROUP_KHATA)
                } catch (e: IllegalStateException) { Toast.makeText(this, e.message ?: "Sign in before saving.", Toast.LENGTH_LONG).show() }
            }.show()
    }

    private fun showSettlementDialog(
        groupId: String, groupName: String, total: Double, memberNames: List<String>,
        contributions: List<JSONObject>, settlements: List<JSONObject>
    ) {
        val debtor = EditText(this).apply { hint = "Member who pays (owes money)" }
        val creditor = EditText(this).apply { hint = "Member who receives (is owed money)" }
        val amount = EditText(this).apply { hint = "Settlement amount"; inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 8, 32, 0); addView(debtor); addView(creditor); addView(amount) }
        AlertDialog.Builder(this).setTitle("Settle • " + groupName).setView(layout)
            .setNegativeButton("CANCEL", null).setPositiveButton("RECORD") { _, _ ->
                val typedDebtor = debtor.text.toString().trim()
                val typedCreditor = creditor.text.toString().trim()
                val value = amount.text.toString().trim().toDoubleOrNull()
                val from = memberNames.firstOrNull { it.equals(typedDebtor, true) }
                val to = memberNames.firstOrNull { it.equals(typedCreditor, true) }
                if (from == null || to == null || from.equals(to, true) || value == null || !value.isFinite() || value <= 0.0) {
                    Toast.makeText(this, "Choose two different listed members and a positive amount.", Toast.LENGTH_LONG).show(); return@setPositiveButton
                }
                val balances = calculateGroupBalances(total, memberNames, contributions, settlements)
                val debt = balances[from] ?: 0.0
                val credit = balances[to] ?: 0.0
                if (debt >= -0.005 || credit <= 0.005 || value > -debt + 0.005 || value > credit + 0.005) {
                    Toast.makeText(this, "Settlement exceeds the debtor's or creditor's recorded net balance.", Toast.LENGTH_LONG).show(); return@setPositiveButton
                }
                try {
                    ArthSaathiCoreEngine.saveModule("GROUP_SETTLEMENT",
                        mapOf("f0" to from, "f1" to to, "f2" to value, "groupName" to groupName),
                        mapOf("parentId" to groupId))
                    render(ArthSaathiNavigation.GROUP_KHATA)
                } catch (e: IllegalStateException) { Toast.makeText(this, e.message ?: "Sign in before saving.", Toast.LENGTH_LONG).show() }
            }.show()
    }

    private fun groupMemberNames(groupId: String): List<String> {
        val all = records
        val group = (0 until all.length()).mapNotNull { all.optJSONObject(it) }.firstOrNull { it.optString("id") == groupId }
        return group?.optString("f4").orEmpty().split(",").map { it.trim() }.filter { it.isNotBlank() }.distinctBy { it.lowercase(Locale.US) }
    }

    private fun calculateGroupBalances(
        total: Double, memberNames: List<String>,
        contributions: List<JSONObject>, settlements: List<JSONObject>
    ): LinkedHashMap<String, Double> {
        val share = if (memberNames.isEmpty()) 0.0 else total / memberNames.size
        val balances = linkedMapOf<String, Double>()
        memberNames.forEach { balances[it] = -share }
        contributions.forEach { record ->
            val member = memberNames.firstOrNull { it.equals(record.optString("f0").trim(), true) }
            if (member != null) balances[member] = (balances[member] ?: -share) + record.optDouble("f1")
        }
        settlements.forEach { record ->
            val debtor = memberNames.firstOrNull { it.equals(record.optString("f0"), true) }
            val creditor = memberNames.firstOrNull { it.equals(record.optString("f1"), true) }
            val value = record.optDouble("f2")
            if (debtor != null) balances[debtor] = (balances[debtor] ?: -share) + value
            if (creditor != null) balances[creditor] = (balances[creditor] ?: -share) - value
        }
        return balances
    }

    private fun qrKhata(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "QR KHATA • Scan locally, review extracted values and save the invoice record. External account sync is deferred until go-live."
            textSize = 16f
        })
        body.addView(Button(this).apply {
            text = "SCAN QR CODE"
            isAllCaps = false
            setOnClickListener {
                IntentIntegrator(this@ArthSaathiMasterModuleActivity)
                    .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    .setPrompt("Scan invoice, merchant or UPI QR")
                    .setBeepEnabled(false)
                    .setOrientationLocked(false)
                    .initiateScan()
            }
        })
        qrMerchantField = field(body, "Merchant / customer")
        qrAmountField = field(body, "Amount (₹)", true)
        qrReferenceField = field(body, "QR / invoice reference")
        qrNoteField = field(body, "Note / invoice description")
        qrInvoiceDateField = field(body, "Invoice date (from QR)")
        qrCapturedAtField = field(body, "Captured at (device time)")
        qrCapturedAtField?.setText(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
        body.addView(Button(this).apply {
            text = "SAVE QR / INVOICE RECORD"
            isAllCaps = false
            setOnClickListener {
                val merchant = qrMerchantField?.text?.toString()?.trim().orEmpty()
                val amount = qrAmountField?.text?.toString()?.trim()?.toDoubleOrNull()
                if (merchant.isBlank()) { qrMerchantField?.error = "Merchant / customer is required"; return@setOnClickListener }
                if (amount == null || !amount.isFinite() || amount <= 0.0) { qrAmountField?.error = "Enter a positive amount"; return@setOnClickListener }
                val reference = qrReferenceField?.text?.toString()?.trim().orEmpty()
                if (reference.isBlank() && qrRawPayload.isBlank()) { qrReferenceField?.error = "Enter an invoice reference or scan a QR"; return@setOnClickListener }
                try {
                    ArthSaathiCoreEngine.saveModule("QR_KHATA", mapOf(
                        "f0" to merchant, "f1" to amount, "f2" to reference,
                        "f3" to qrNoteField?.text?.toString()?.trim().orEmpty(),
                        "f4" to qrCapturedAtField?.text?.toString()?.trim().orEmpty(),
                        "f5" to qrRawPayload, "f6" to qrInvoiceDateField?.text?.toString()?.trim().orEmpty()
                    ))
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "QR / invoice record saved", Toast.LENGTH_LONG).show()
                    qrRawPayload = ""
                    render(ArthSaathiNavigation.QR_KHATA)
                } catch (e: IllegalStateException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Sign in before saving.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "\nSaved QR / invoice records"; textSize = 18f; setPadding(0, 18, 0, 6) })
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            if (record.optString("type") != "QR_KHATA") continue
            body.addView(TextView(this).apply {
                text = record.optString("f0") + " • ₹" + money(record.optDouble("f1")) +
                    "\nReference: " + record.optString("f2") + "\nInvoice date: " + record.optString("f6") + "\nCaptured: " + record.optString("f4")
                setPadding(8, 8, 8, 8)
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        if (requestCode == requestPickDocument) {
            if (resultCode == RESULT_OK) handleDocumentPickerResult(data)
            return
        }
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val payload = result.contents
            if (payload.isNullOrBlank()) {
                Toast.makeText(this, "QR scan cancelled; no record saved.", Toast.LENGTH_SHORT).show()
            } else {
                qrRawPayload = payload
                val parsed = ArthSaathiCoreEngine.parseQrPayload(payload)
                qrMerchantField?.setText(parsed.optString("merchant"))
                if (parsed.optString("amount").isNotBlank()) qrAmountField?.setText(parsed.optString("amount"))
                qrReferenceField?.setText(parsed.optString("reference"))
                qrNoteField?.setText(parsed.optString("note"))
                qrInvoiceDateField?.setText(parsed.optString("date"))
                qrCapturedAtField?.setText(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
                Toast.makeText(this, "QR read. Review all extracted values before saving.", Toast.LENGTH_LONG).show()
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun documentVault(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "DOCUMENT VAULT • Store evidence privately on this device. Files are encrypted with an Android Keystore AES-GCM key; the original SHA-256 is retained for integrity checks."
            textSize = 14f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 4, 0, 12)
        })
        documentTitleField = field(body, "Document title")
        documentTypeField = field(body, "Document type (agreement / invoice / policy / identity / other)")
        documentRelatedField = field(body, "Related account / module (optional)")
        body.addView(Button(this).apply {
            text = "CHOOSE & STORE DOCUMENT"
            setOnClickListener {
                if (documentTitleField?.text?.toString()?.trim().isNullOrEmpty()) {
                    documentTitleField?.error = "Enter a document title first"
                    return@setOnClickListener
                }
                val picker = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                startActivityForResult(picker, requestPickDocument)
            }
        })
        body.addView(TextView(this).apply {
            text = "\nSAVED DOCUMENTS"
            textSize = 18f
            setTextColor(ArthSaathiV7Design.NAVY)
            setPadding(0, 18, 0, 6)
        })
        var count = 0
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            if (record.optString("type") != "DOCUMENT") continue
            count++
            body.addView(TextView(this).apply {
                text = record.optString("f0", "Document") + "\n" +
                    record.optString("f1", "Unclassified") + " • " +
                    record.optLong("sizeBytes").let { String.format(java.util.Locale.US, "%,d bytes", it) } +
                    "\nSHA-256: " + record.optString("f4").take(16) + "…"
                textSize = 13f
                setPadding(8, 10, 8, 4)
            })
            body.addView(Button(this).apply {
                text = "OPEN • " + record.optString("f0", "DOCUMENT")
                isAllCaps = false
                setOnClickListener { openStoredDocument(record) }
            })
        }
        if (count == 0) body.addView(TextView(this).apply {
            text = "No documents stored yet. Select a file above to add the first encrypted record."
            textSize = 13f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 8, 0, 8)
        })
    }

    private fun handleDocumentPickerResult(data: android.content.Intent?) {
        val uri = data?.data ?: return
        try {
            val originalName = contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                } ?: "document.bin"
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val input = contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("The selected document could not be opened.")
            val stored = ArthSaathiDocumentStorage.store(this, input, originalName)
            val title = documentTitleField?.text?.toString()?.trim().orEmpty().ifBlank {
                originalName.substringBeforeLast('.', originalName)
            }
            val documentType = documentTypeField?.text?.toString()?.trim().orEmpty().ifBlank { "Other" }
            val related = documentRelatedField?.text?.toString()?.trim().orEmpty()
            ArthSaathiCoreEngine.saveModule(
                "DOCUMENT",
                mapOf(
                    "f0" to title,
                    "f1" to documentType,
                    "f2" to related,
                    "f3" to stored.fileName,
                    "f4" to stored.sha256,
                    "sizeBytes" to stored.sizeBytes,
                    "extension" to stored.extension,
                    "mimeType" to mimeType,
                    "encrypted" to true,
                    "storage" to "ANDROID_KEYSTORE_AES_GCM"
                )
            )
            Toast.makeText(this, "Document encrypted and saved. SHA-256 integrity reference recorded.", Toast.LENGTH_LONG).show()
            render(ArthSaathiNavigation.DOCUMENT_VAULT)
        } catch (e: Exception) {
            Toast.makeText(this, e.message ?: "Could not store the selected document.", Toast.LENGTH_LONG).show()
        }
    }

    private fun openStoredDocument(record: JSONObject) {
        try {
            val preview = ArthSaathiDocumentStorage.decryptToCache(
                this, record.optString("f3"), record.optString("extension", "bin")
            )
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", preview)
            val viewer = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, record.optString("mimeType", "application/octet-stream"))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(viewer)
        } catch (e: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "No installed app can open this document type.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message ?: "Document could not be opened.", Toast.LENGTH_LONG).show()
        }
    }

    private fun advocateDirectory(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "ADVOCATE DIRECTORY • Search contacts you have saved by name, legal domain and city. User-entered contacts are not independently verified unless explicitly marked."
            textSize = 14f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 4, 0, 12)
        })
        val search = field(body, "Search name / legal domain / city")
        val name = field(body, "Advocate / firm name")
        val domain = field(body, "Legal domain / expertise")
        val city = field(body, "City / locality")
        val verification = field(body, "Verification status (default: Unverified)")
        val contact = field(body, "Phone / email contact")
        val address = field(body, "Office address (optional)")
        body.addView(Button(this).apply {
            text = "SAVE ADVOCATE CONTACT"
            setOnClickListener {
                if (name.text.toString().trim().isEmpty()) { name.error = "Enter advocate or firm name"; return@setOnClickListener }
                if (domain.text.toString().trim().isEmpty()) { domain.error = "Enter legal domain / expertise"; return@setOnClickListener }
                if (city.text.toString().trim().isEmpty()) { city.error = "Enter city / locality"; return@setOnClickListener }
                if (contact.text.toString().trim().isEmpty()) { contact.error = "Enter a contact detail"; return@setOnClickListener }
                try {
                    ArthSaathiCoreEngine.saveModule("ADVOCATE", mapOf(
                        "f0" to name.text.toString().trim(),
                        "f1" to domain.text.toString().trim(),
                        "f2" to city.text.toString().trim(),
                        "f3" to verification.text.toString().trim().ifBlank { "Unverified — user-entered" },
                        "f4" to contact.text.toString().trim(),
                        "f5" to address.text.toString().trim()
                    ))
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Advocate contact saved to your private directory.", Toast.LENGTH_LONG).show()
                    render(ArthSaathiNavigation.ADVOCATES)
                } catch (e: Exception) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Could not save contact.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "\nDIRECTORY RESULTS"; textSize = 18f; setTextColor(ArthSaathiV7Design.NAVY); setPadding(0, 18, 0, 6) })
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(results)
        fun refreshResults() {
            results.removeAllViews()
            val query = search.text.toString().trim().lowercase(Locale.US)
            var count = 0
            for (i in 0 until records.length()) {
                val record = records.optJSONObject(i) ?: continue
                if (record.optString("type") != "ADVOCATE") continue
                val values = listOf("f0", "f1", "f2", "f3", "f4", "f5").map { record.optString(it) }
                if (query.isNotBlank() && values.none { it.lowercase(Locale.US).contains(query) }) continue
                count++
                results.addView(TextView(this).apply {
                    text = record.optString("f0") + "\n" + record.optString("f1") + " • " + record.optString("f2") +
                        "\n" + record.optString("f3", "Unverified") + "\n" + record.optString("f4") +
                        if (record.optString("f5").isBlank()) "" else "\n" + record.optString("f5")
                    textSize = 14f
                    setPadding(10, 12, 10, 6)
                })
                val phone = record.optString("f4").filter { it.isDigit() || it == '+' }
                results.addView(Button(this).apply {
                    text = "CONTACT"
                    isAllCaps = false
                    setOnClickListener {
                        if (phone.isBlank()) {
                            Toast.makeText(this@ArthSaathiMasterModuleActivity, record.optString("f4"), Toast.LENGTH_LONG).show()
                        } else {
                            runCatching {
                                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)))
                            }.onFailure {
                                Toast.makeText(this@ArthSaathiMasterModuleActivity, record.optString("f4"), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                })
            }
            if (count == 0) results.addView(TextView(this).apply {
                text = if (query.isBlank()) "No contacts saved yet. Add a contact above to build your searchable directory."
                    else "No saved contacts match this name, domain or city."
                setTextColor(ArthSaathiV7Design.MUTED)
                setPadding(0, 8, 0, 8)
            })
        }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = refreshResults()
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
        refreshResults()
    }

    private fun aiAdvisor(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "ARTHSAATHI ADVISOR • Explainable reminders from your own recorded data. Suggestions only: no money movement, investment switch or external disclosure is performed."
            textSize = 14f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 4, 0, 12)
        })
        val metrics = ArthSaathiCoreEngine.mis()
        val all = ArthSaathiDataStore.records()
        fun insight(title: String, reason: String, actionLabel: String, action: (() -> Unit)? = null) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 12, 14, 12)
                background = ArthSaathiV7Design.card(this@ArthSaathiMasterModuleActivity, android.graphics.Color.WHITE, 14)
            }
            card.addView(ArthSaathiV7Design.text(this, title, 15f, ArthSaathiV7Design.NAVY, true))
            card.addView(ArthSaathiV7Design.text(this, reason, 12f, ArthSaathiV7Design.MUTED).apply { setPadding(0, 6, 0, 6) })
            if (action != null) card.addView(Button(this).apply {
                text = actionLabel
                isAllCaps = false
                setOnClickListener { action() }
            })
            body.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
        }
        insight("Recorded financial position",
            "Assets Rs " + money(metrics.assets) + " • Liabilities Rs " + money(metrics.liabilities) +
                " • Credit outstanding Rs " + money(metrics.outstanding),
            "OPEN MIS") { startActivity(Intent(this, ArthSaathiMasterModuleActivity::class.java).putExtra("module", ArthSaathiNavigation.MIS)) }
        var actionable = 0
        for (i in 0 until all.length()) {
            val record = all.optJSONObject(i) ?: continue
            if (record.optString("type") == "CREDIT" && record.optString("status") == "ACTIVE") {
                val due = parseAdvisorDate(record.optString("dueDate"))
                if (due != null) {
                    val days = ((due.time - System.currentTimeMillis()) / 86_400_000L).toInt()
                    if (days < 0) {
                        actionable++
                        val balance = (record.optDouble("totalPayable", record.optDouble("amount")) - record.optDouble("paid")).coerceAtLeast(0.0)
                        insight("Overdue credit reminder",
                            record.optString("party") + " has a recorded due date that passed " + (-days) +
                                " day(s) ago. Outstanding: Rs " + money(balance) + ".",
                            "OPEN REPAYMENT") { startActivity(Intent(this, ArthSaathiMasterModuleActivity::class.java).putExtra("module", ArthSaathiNavigation.REPAYMENT)) }
                    } else if (days <= 7) {
                        actionable++
                        insight("Upcoming due date",
                            record.optString("party") + " has a recorded due date in " + days +
                                " day(s). Check the account terms before recording a payment.",
                            "OPEN ACCOUNT CENTRE") { startActivity(Intent(this, ArthSaathiMasterModuleActivity::class.java).putExtra("module", ArthSaathiNavigation.LOANS_UDHAAR)) }
                    }
                }
            }
            if (record.optString("type") == "CHARGECHECK" && record.optDouble("f3") > 0.0) {
                actionable++
                insight("Charge variance to review",
                    record.optString("f0") + " has a recorded positive variance of Rs " + money(record.optDouble("f3")) +
                        ". Review supporting evidence before taking action.",
                    "OPEN CHARGECHECK") { startActivity(Intent(this, ArthSaathiMasterModuleActivity::class.java).putExtra("module", ArthSaathiNavigation.CHARGECHECK)) }
            }
            if (record.optString("type") == "PROTECTION") {
                val expiry = parseAdvisorDate(record.optString("f3"))
                if (expiry != null) {
                    val days = ((expiry.time - System.currentTimeMillis()) / 86_400_000L).toInt()
                    if (days in 0..30) {
                        actionable++
                        insight("Protection renewal check",
                            record.optString("f0") + " has a recorded renewal / expiry date in " + days +
                                " day(s). Check policy terms and eligibility.",
                            "OPEN PROTECTION") { startActivity(Intent(this, ArthSaathiMasterModuleActivity::class.java).putExtra("module", ArthSaathiNavigation.PROTECTION)) }
                    }
                }
            }
        }
        if (actionable == 0) insight("No rule-based alerts found",
            "There are no overdue/upcoming due dates, positive ChargeCheck variances or near-term protection renewals in the records currently saved. This is not a guarantee that no obligations exist.",
            "OPEN PEOPLE & RECORDS") { startActivity(Intent(this, ArthSaathiMasterModuleActivity::class.java).putExtra("module", ArthSaathiNavigation.PEOPLE)) }
    }

    private fun parseAdvisorDate(value: String): Date? {
        if (value.isBlank()) return null
        for (pattern in listOf("dd-MM-yyyy", "yyyy-MM-dd")) {
            try {
                return SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(value)
            } catch (_: Exception) { }
        }
        return null
    }

    private fun claims(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "CLAIM ASSISTANCE • Track an asset, benefit or recovery through clear stages. Keep evidence references with the claim; approval and legal entitlement must be confirmed by the relevant institution or professional."
            textSize = 14f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 4, 0, 12)
        })
        val claimName = field(body, "Claim / asset / benefit")
        val claimant = field(body, "Claimant / nominee / authorised person")
        val institution = field(body, "Institution / source")
        val evidence = field(body, "Evidence / document references")
        val stages = listOf("IDENTIFIED", "DOCUMENTS_PENDING", "READY_TO_SUBMIT", "SUBMITTED", "FOLLOW_UP", "APPROVED", "REJECTED", "CLOSED")
        val status = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stages)
            body.addView(it)
        }
        body.addView(Button(this).apply {
            text = "SAVE CLAIM / UPDATE RECORD"
            setOnClickListener {
                if (claimName.text.toString().trim().isEmpty()) { claimName.error = "Enter the claim or asset"; return@setOnClickListener }
                if (claimant.text.toString().trim().isEmpty()) { claimant.error = "Enter claimant / authorised person"; return@setOnClickListener }
                try {
                    ArthSaathiCoreEngine.saveModule("CLAIM", mapOf(
                        "f0" to claimName.text.toString().trim(),
                        "f1" to claimant.text.toString().trim(),
                        "f2" to institution.text.toString().trim(),
                        "f3" to status.selectedItem.toString(),
                        "f4" to evidence.text.toString().trim()
                    ))
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Claim stage saved.", Toast.LENGTH_SHORT).show()
                    render(ArthSaathiNavigation.CLAIMS)
                } catch (e: Exception) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Could not save claim.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "\nCLAIM TRACKER"; textSize = 18f; setPadding(0, 18, 0, 6) })
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            if (record.optString("type") != "CLAIM") continue
            body.addView(TextView(this).apply {
                text = record.optString("f0") + "\nClaimant: " + record.optString("f1") +
                    "\nInstitution: " + record.optString("f2") + "\nStage: " + record.optString("f3") +
                    "\nEvidence: " + record.optString("f4")
                textSize = 14f
                setPadding(8, 12, 8, 4)
            })
            val current = record.optString("f3")
            val next = when (current) {
                "IDENTIFIED" -> "DOCUMENTS_PENDING"
                "DOCUMENTS_PENDING" -> "READY_TO_SUBMIT"
                "READY_TO_SUBMIT" -> "SUBMITTED"
                "SUBMITTED" -> "FOLLOW_UP"
                "FOLLOW_UP" -> "APPROVED"
                "APPROVED" -> "CLOSED"
                else -> ""
            }
            if (next.isNotBlank()) body.addView(Button(this).apply {
                text = "ADVANCE TO " + next.replace('_', ' ')
                isAllCaps = false
                setOnClickListener {
                    val result = ArthSaathiCoreEngine.update(record.optString("id"), mapOf("f3" to next), "user")
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_SHORT).show()
                    if (result.ok) render(ArthSaathiNavigation.CLAIMS)
                }
            })
        }
    }

    private fun willLegacy(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "WILL / INHERITANCE / LEGACY • This creates a structured planning record and a draft summary only—not a legally executed Will or legal advice. Obtain independent legal review and follow applicable signing / witness requirements."
            textSize = 14f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 4, 0, 12)
        })
        val asset = field(body, "Asset / property / account")
        val beneficiary = field(body, "Intended beneficiary / nominee")
        val executor = field(body, "Proposed executor / trusted person")
        val instruction = field(body, "Instructions / wishes (optional)")
        val version = field(body, "Draft version (e.g. 1)").apply { setText("1") }
        body.addView(Button(this).apply {
            text = "SAVE DRAFT & CREATE SUMMARY DOCUMENT"
            setOnClickListener {
                if (asset.text.toString().trim().isEmpty()) { asset.error = "Enter an asset or property"; return@setOnClickListener }
                if (beneficiary.text.toString().trim().isEmpty()) { beneficiary.error = "Enter intended beneficiary"; return@setOnClickListener }
                if (executor.text.toString().trim().isEmpty()) { executor.error = "Enter proposed executor / trusted person"; return@setOnClickListener }
                try {
                    val assetName = asset.text.toString().trim()
                    val beneficiaryName = beneficiary.text.toString().trim()
                    val executorName = executor.text.toString().trim()
                    val wishes = instruction.text.toString().trim()
                    val draftVersion = version.text.toString().trim().ifBlank { "1" }
                    val legacy = ArthSaathiCoreEngine.saveModule("LEGACY", mapOf(
                        "f0" to assetName,
                        "f1" to beneficiaryName,
                        "f2" to executorName,
                        "f3" to wishes,
                        "f4" to draftVersion,
                        "draftStatus" to "DRAFT_REQUIRES_LEGAL_REVIEW"
                    ))
                    val draftText = "ARTHSAATHI — LEGACY PLANNING DRAFT SUMMARY\n\n" +
                        "Status: DRAFT ONLY — NOT A LEGALLY EXECUTED WILL\n" +
                        "Draft version: " + draftVersion + "\nAsset / property / account: " + assetName +
                        "\nIntended beneficiary / nominee: " + beneficiaryName +
                        "\nProposed executor / trusted person: " + executorName +
                        "\nInstructions / wishes: " + wishes +
                        "\n\nIMPORTANT: This summary records the user's stated planning intentions only. It is not legal advice, does not establish ownership or inheritance rights, and does not replace a legally valid Will, nomination, trust or other instrument. Obtain independent legal review and comply with applicable execution and witness requirements."
                    val stored = ArthSaathiDocumentStorage.store(
                        this@ArthSaathiMasterModuleActivity,
                        ByteArrayInputStream(draftText.toByteArray(Charsets.UTF_8)),
                        "legacy-draft.txt"
                    )
                    ArthSaathiCoreEngine.saveModule(
                        "DOCUMENT",
                        mapOf(
                            "f0" to "Legacy draft summary — " + assetName,
                            "f1" to "Will / Legacy draft summary",
                            "f2" to "DRAFT_REQUIRES_LEGAL_REVIEW",
                            "f3" to stored.fileName,
                            "f4" to stored.sha256,
                            "sizeBytes" to stored.sizeBytes,
                            "extension" to stored.extension,
                            "mimeType" to "text/plain",
                            "encrypted" to true,
                            "storage" to "ANDROID_KEYSTORE_AES_GCM"
                        ),
                        mapOf("relatedId" to (legacy.id ?: ""))
                    )
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Draft summary encrypted and stored. Legal review is required.", Toast.LENGTH_LONG).show()
                    render(ArthSaathiNavigation.WILL_LEGACY)
                } catch (e: Exception) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Could not create legacy draft.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "\nSAVED LEGACY PLANS"; textSize = 18f; setPadding(0, 18, 0, 6) })
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            if (record.optString("type") != "LEGACY") continue
            body.addView(TextView(this).apply {
                text = record.optString("f0") + "\nBeneficiary: " + record.optString("f1") +
                    "\nExecutor: " + record.optString("f2") + "\nDraft version: " + record.optString("f4") +
                    "\nStatus: " + record.optString("draftStatus", "DRAFT_REQUIRES_LEGAL_REVIEW")
                textSize = 14f
                setPadding(8, 12, 8, 8)
            })
        }
    }

    private fun assetVault(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "ASSET VAULT • Record financial and non-financial assets, with owner and nominee links to People & Relationships. Evidence should be stored in Document Vault."
            textSize = 14f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 4, 0, 12)
        })
        val assetName = field(body, "Asset / account / property name")
        val categoryOptions = listOf("Financial Asset", "Non-financial Asset")
        val category = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)
            body.addView(TextView(this).apply { text = "ASSET CATEGORY"; textSize = 12f; setTextColor(ArthSaathiV7Design.MUTED) })
            body.addView(it)
        }
        val value = field(body, "Current value (₹)", true)
        val people = (0 until records.length()).mapNotNull { records.optJSONObject(it) }
            .filter { it.optString("type") == "PERSON" }
        fun personLabel(person: JSONObject): String =
            person.optString("name") + if (person.optString("mobile").isBlank()) "" else " • " + person.optString("mobile")
        val ownerLabels = listOf("Primary user / not linked") + people.map { personLabel(it) }
        val owner = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ownerLabels)
            body.addView(TextView(this).apply { text = "OWNER / RESPONSIBLE PERSON"; textSize = 12f; setTextColor(ArthSaathiV7Design.MUTED) })
            body.addView(it)
        }
        val nomineeLabels = listOf("No nominee recorded") + people.map { personLabel(it) }
        val nominee = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nomineeLabels)
            body.addView(TextView(this).apply { text = "NOMINEE / BENEFICIARY"; textSize = 12f; setTextColor(ArthSaathiV7Design.MUTED) })
            body.addView(it)
        }
        val evidence = field(body, "Document Vault title / ID (optional)")
        body.addView(Button(this).apply {
            text = "SAVE ASSET"
            setOnClickListener {
                val name = assetName.text.toString().trim()
                val amount = value.text.toString().trim().toDoubleOrNull()
                if (name.isBlank()) { assetName.error = "Enter an asset name"; return@setOnClickListener }
                if (amount == null || !amount.isFinite() || amount < 0.0) { value.error = "Enter a valid non-negative value"; return@setOnClickListener }
                val ownerPerson = people.getOrNull(owner.selectedItemPosition - 1)
                val nomineePerson = people.getOrNull(nominee.selectedItemPosition - 1)
                val links = linkedMapOf<String, String>()
                ownerPerson?.optString("id")?.takeIf { it.isNotBlank() }?.let { links["ownerPersonId"] = it }
                nomineePerson?.optString("id")?.takeIf { it.isNotBlank() }?.let { links["nomineePersonId"] = it }
                try {
                    ArthSaathiCoreEngine.saveModule("ASSET", mapOf(
                        "f0" to name, "f1" to category.selectedItem.toString(),
                        "f2" to amount, "value" to amount,
                        "f3" to (ownerPerson?.optString("name") ?: "Primary user"),
                        "f4" to evidence.text.toString().trim(),
                        "ownerPersonId" to (ownerPerson?.optString("id") ?: ""),
                        "nomineePersonId" to (nomineePerson?.optString("id") ?: ""),
                        "nomineeName" to (nomineePerson?.optString("name") ?: "")
                    ), links)
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Asset saved and linked to selected people.", Toast.LENGTH_SHORT).show()
                    render(ArthSaathiNavigation.ASSET_VAULT)
                } catch (e: Exception) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Could not save asset.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "\nSAVED ASSETS"; textSize = 18f; setPadding(0, 18, 0, 6) })
        var count = 0
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            if (record.optString("type") != "ASSET") continue
            count++
            body.addView(TextView(this).apply {
                text = record.optString("f0") + " • " + record.optString("f1") +
                    "\nCurrent value: ₹" + money(record.optDouble("value", record.optDouble("f2"))) +
                    "\nOwner: " + record.optString("f3", "Primary user") +
                    (if (record.optString("nomineeName").isBlank()) "" else "\nNominee: " + record.optString("nomineeName")) +
                    (if (record.optString("f4").isBlank()) "" else "\nEvidence: " + record.optString("f4"))
                textSize = 14f
                setPadding(8, 12, 8, 8)
            })
        }
        if (count == 0) body.addView(TextView(this).apply { text = "No assets recorded yet." })
    }

    private fun liabilityVault(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "LIABILITY VAULT • Record balances and due dates, and link the responsible person from People & Relationships."
            textSize = 14f
            setTextColor(ArthSaathiV7Design.MUTED)
            setPadding(0, 4, 0, 12)
        })
        val liabilityName = field(body, "Liability / loan / obligation")
        val lender = field(body, "Lender / institution")
        val outstanding = field(body, "Outstanding balance (₹)", true)
        val due = field(body, "Due date (optional)").apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { pickDate(this) }
        }
        val people = (0 until records.length()).mapNotNull { records.optJSONObject(it) }
            .filter { it.optString("type") == "PERSON" }
        val personLabels = listOf("Primary user / not linked") + people.map {
            it.optString("name") + if (it.optString("mobile").isBlank()) "" else " • " + it.optString("mobile")
        }
        val responsible = Spinner(this).also {
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, personLabels)
            body.addView(TextView(this).apply { text = "RESPONSIBLE PERSON"; textSize = 12f; setTextColor(ArthSaathiV7Design.MUTED) })
            body.addView(it)
        }
        val evidence = field(body, "Document Vault title / ID (optional)")
        body.addView(Button(this).apply {
            text = "SAVE LIABILITY"
            setOnClickListener {
                val name = liabilityName.text.toString().trim()
                val amount = outstanding.text.toString().trim().toDoubleOrNull()
                if (name.isBlank()) { liabilityName.error = "Enter the liability"; return@setOnClickListener }
                if (amount == null || !amount.isFinite() || amount < 0.0) { outstanding.error = "Enter a valid non-negative balance"; return@setOnClickListener }
                val person = people.getOrNull(responsible.selectedItemPosition - 1)
                val links = linkedMapOf<String, String>()
                person?.optString("id")?.takeIf { it.isNotBlank() }?.let { links["responsiblePersonId"] = it }
                try {
                    ArthSaathiCoreEngine.saveModule("LIABILITY", mapOf(
                        "f0" to name, "f1" to lender.text.toString().trim(),
                        "f2" to amount, "outstanding" to amount, "f3" to due.text.toString().trim(),
                        "f4" to evidence.text.toString().trim(),
                        "responsiblePersonId" to (person?.optString("id") ?: ""),
                        "responsiblePersonName" to (person?.optString("name") ?: "Primary user")
                    ), links)
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, "Liability saved and linked.", Toast.LENGTH_SHORT).show()
                    render(ArthSaathiNavigation.LIABILITY_VAULT)
                } catch (e: Exception) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, e.message ?: "Could not save liability.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "\nSAVED LIABILITIES"; textSize = 18f; setPadding(0, 18, 0, 6) })
        var count = 0
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            if (record.optString("type") != "LIABILITY") continue
            count++
            body.addView(TextView(this).apply {
                text = record.optString("f0") + "\nLender: " + record.optString("f1") +
                    "\nOutstanding: ₹" + money(record.optDouble("outstanding", record.optDouble("f2"))) +
                    "\nDue: " + record.optString("f3") +
                    "\nResponsible: " + record.optString("responsiblePersonName", "Primary user")
                textSize = 14f
                setPadding(8, 12, 8, 8)
            })
        }
        if (count == 0) body.addView(TextView(this).apply { text = "No liabilities recorded yet." })
    }

    private fun simple(body:LinearLayout,type:String,title:String,hints:List<String>){
        body.addView(TextView(this).apply{text=title+"\n";textSize=17f})
        val fields=hints.map { hint ->
            val numeric = isNumericHint(hint) || hint.contains("mobile number", true) || hint.contains("PIN code", true) || hint.contains("Aadhaar (last four", true)
            val maxLength = when {
                hint.contains("mobile number", true) -> 10
                hint.contains("PIN code", true) -> 6
                hint.contains("Aadhaar (last four", true) -> 4
                hint.contains("PAN", true) -> 10
                hint.contains("GSTIN", true) -> 15
                else -> null
            }
            val edit = field(body, hint, numeric, maxLength)
            if (!numeric && (hint.contains("date", true) || hint.contains("expiry", true) ||
                    hint.contains("renewal", true) || hint.contains("due", true))) {
                edit.isFocusable = false
                edit.isClickable = true
                edit.setOnClickListener { pickDate(edit) }
            }
            edit
        }
        var editingRecordId: String? = null
        lateinit var saveButton: Button
        saveButton = Button(this).apply{text="SAVE";setOnClickListener{
            if (fields.isNotEmpty() && fields[0].text.toString().trim().isEmpty()) {
                fields[0].error = "Required"; return@setOnClickListener
            }
            val values = linkedMapOf<String, Any?>()
            fields.forEachIndexed { idx, edit ->
                val hint = hints[idx]
                val rawValue = edit.text.toString().trim()
                if (hint.contains("mobile number", true) && !rawValue.matches(Regex("[6-9][0-9]{9}"))) {
                    edit.error = "Enter a valid 10-digit Indian mobile number"; return@setOnClickListener
                }
                if (hint.contains("email", true) && !ArthSaathiCoreEngine.validateEmail(rawValue)) {
                    edit.error = "Enter a valid email address"; return@setOnClickListener
                }
                if (hint.contains("Aadhaar (last four", true) && rawValue.isNotBlank() &&
                    !rawValue.matches(Regex("[0-9]{4}"))) {
                    edit.error = "Enter only the last four Aadhaar digits"; return@setOnClickListener
                }
                if (hint.contains("PIN code", true) && !rawValue.matches(Regex("[1-9][0-9]{5}"))) {
                    edit.error = "Enter a valid 6-digit PIN code"; return@setOnClickListener
                }
                if (hint.contains("PAN", true) && rawValue.isNotBlank() && !rawValue.uppercase(Locale.US).matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))) {
                    edit.error = "Enter a valid PAN"; return@setOnClickListener
                }
                if (hint.contains("GSTIN", true) && rawValue.isNotBlank() && !rawValue.uppercase(Locale.US).matches(Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]"))) {
                    edit.error = "Enter a valid GSTIN"; return@setOnClickListener
                }
                val value = if (hint.contains("PAN", true) || hint.contains("GSTIN", true)) rawValue.uppercase(Locale.US) else rawValue
                if (isNumericHint(hint)) {
                    val number = value.toDoubleOrNull()
                    if (number == null || !number.isFinite()) {
                        edit.error = "Enter a valid number"; return@setOnClickListener
                    }
                    if (number < 0.0 && !hints[idx].contains("variance", true)) {
                        edit.error = "Value cannot be negative"; return@setOnClickListener
                    }
                    if ((hints[idx].contains("rate", true) || hints[idx].contains("ROI", true)) &&
                        number !in 0.0..100.0) {
                        edit.error = "Rate must be between 0 and 100%"; return@setOnClickListener
                    }
                    values["f$idx"] = number
                } else {
                    values["f$idx"] = value
                }
            }
            try {
                val currentId = editingRecordId
                if (currentId == null) {
                    ArthSaathiCoreEngine.saveModule(type, values)
                } else {
                    val result = ArthSaathiCoreEngine.update(currentId, values)
                    if (!result.ok) {
                        Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }
                editingRecordId = null
                saveButton.text = "SAVE"
                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                    if (currentId == null) title + " record saved" else title + " record updated",
                    Toast.LENGTH_SHORT).show()
                render(intent.getStringExtra("module") ?: ArthSaathiNavigation.HOME)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                    e.message ?: "Please review the details.", Toast.LENGTH_LONG).show()
            } catch (e: IllegalStateException) {
                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                    e.message ?: "Sign in again before saving.", Toast.LENGTH_LONG).show()
            }
        }}
        body.addView(saveButton)
        body.addView(TextView(this).apply {
            text = "\nSAVED RECORDS"
            textSize = 19f
            setPadding(0, 24, 0, 8)
        })
        var savedCount = 0
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            if (record.optString("type") != type) continue
            savedCount++
            val lines = hints.mapIndexedNotNull { idx, hint ->
                if (!record.has("f$idx") || record.isNull("f$idx")) null
                else {
                    val value = record.opt("f$idx")
                    val display = if (value is Number) money(value.toDouble()) else value.toString()
                    hint + ": " + display
                }
            }
            val heading = record.optString("f0", record.optString("name", title + " record"))
            body.addView(TextView(this).apply {
                text = heading + (if (lines.isEmpty()) "" else "\n" + lines.drop(1).joinToString("\n"))
                textSize = 15f
                setPadding(8, 12, 8, 12)
            })
            body.addView(Button(this).apply {
                text = "EDIT • " + heading
                isAllCaps = false
                setOnClickListener {
                    editingRecordId = record.optString("id")
                    fields.forEachIndexed { idx, edit ->
                        val value = record.opt("f$idx")
                        edit.setText(if (value == null || value == JSONObject.NULL) "" else value.toString())
                    }
                    saveButton.text = "UPDATE RECORD"
                    fields.firstOrNull()?.requestFocus()
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        "Editing saved record. Review the fields and choose UPDATE RECORD.",
                        Toast.LENGTH_LONG).show()
                }
            })
        }
        if (savedCount == 0) body.addView(TextView(this).apply {
            text = "No saved records yet."
            setPadding(0, 8, 0, 8)
        })
    }

    private fun isNumericHint(hint: String): Boolean =
        listOf("value", "amount", "principal", "expense", "coverage", "invested",
            "charge", "outstanding", "original", "rate", "roi", "variance", "total")
            .any { hint.contains(it, true) }

    private fun field(parent:LinearLayout,hint:String,number:Boolean=false,max:Int?=null):EditText{
        val edit = EditText(this).apply {
            this.hint = hint
            setSingleLine(true)
            textSize = 16f
            setTextColor(ArthSaathiV7Design.NAVY)
            setHintTextColor(ArthSaathiV7Design.MUTED)
            setPadding(16, 12, 16, 12)
            minHeight = ArthSaathiV7Design.dp(this@ArthSaathiMasterModuleActivity, 52)
            background = ArthSaathiV7Design.card(this@ArthSaathiMasterModuleActivity, android.graphics.Color.WHITE, 12)
            if(number) inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            if(max!=null) filters=arrayOf(InputFilter.LengthFilter(max))
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
            setOnEditorActionListener { view, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    actionId == android.view.inputmethod.EditorInfo.IME_NULL) {
                    val next = view.focusSearch(android.view.View.FOCUS_FORWARD)
                    if (next != null) {
                        next.requestFocus()
                        if (next is EditText) {
                            next.post { parentScrollIntoView(next) }
                        }
                    }
                    true
                } else false
            }
            setOnFocusChangeListener { view, focused ->
                if (focused) view.post { parentScrollIntoView(view) }
            }
        }
        parent.addView(edit, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = ArthSaathiV7Design.dp(this@ArthSaathiMasterModuleActivity, 10) })
        return edit
    }

    private fun parentScrollIntoView(view: android.view.View) {
        var current: android.view.ViewParent? = view.parent
        while (current != null && current !is ScrollView) current = current.parent
        (current as? ScrollView)?.let { scroll ->
            val rect = android.graphics.Rect()
            view.getDrawingRect(rect)
            scroll.offsetDescendantRectToMyCoords(view, rect)
            scroll.requestChildRectangleOnScreen(view, rect, true)
        }
    }
    private fun pickDate(target:EditText){val c=Calendar.getInstance();DatePickerDialog(this,{_,y,m,d->target.setText(dateFormat.format(GregorianCalendar(y,m,d).time))},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}
    private fun money(v:Double)=String.format(Locale.US,"%.2f",v)
}
