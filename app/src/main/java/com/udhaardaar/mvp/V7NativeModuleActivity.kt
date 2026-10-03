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
    private var selectedPersonId: String? = null
    private var pendingPhotoUri: String = ""
    private lateinit var recordName: EditText
    private lateinit var recordMobile: EditText
    private lateinit var recordPan: EditText
    private lateinit var recordAadhaar: EditText
    private lateinit var recordGstin: EditText
    private lateinit var recordEmail: EditText
    private lateinit var recordAddress: EditText
    private lateinit var recordPin: EditText
    private lateinit var recordCity: EditText
    private lateinit var recordDistrict: EditText
    private lateinit var recordState: EditText

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 700 && resultCode == RESULT_OK) {
            pendingPhotoUri = data?.data?.toString().orEmpty()
            Toast.makeText(this, if (pendingPhotoUri.isNotBlank()) "Profile picture selected." else "No picture selected.", Toast.LENGTH_SHORT).show()
        }
    }

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
        body.addView(ArthSaathiV7Design.section(this, "Profile & Identity", "Create or edit one complete profile. Address and picture belong to the profile, not a separate module."))

        recordName = field("Full name")
        recordMobile = field("10-digit mobile", android.text.InputType.TYPE_CLASS_PHONE)
        recordPan = field("PAN (optional)")
        recordAadhaar = field("Aadhaar (optional)", android.text.InputType.TYPE_CLASS_NUMBER)
        recordGstin = field("GSTIN (optional)")
        recordEmail = field("Email (optional)", android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        recordAddress = field("Address")
        recordPin = field("6-digit PIN", android.text.InputType.TYPE_CLASS_NUMBER)
        recordCity = field("City / Town")
        recordDistrict = field("District")
        recordState = field("State")

        listOf(recordName,recordMobile,recordPan,recordAadhaar,recordGstin,recordEmail,recordAddress,recordPin,recordCity,recordDistrict,recordState).forEach {
            body.addView(it, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })
        }

        body.addView(button("Add / Change Profile Picture") {
            val pick = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(android.content.Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(pick, 700)
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(7) })

        body.addView(button(if (selectedPersonId == null) "Create Profile" else "Save Profile Changes") {
            val n = recordName.text.toString().trim()
            val m = recordMobile.text.toString().trim()
            if (n.isBlank()) { recordName.error = "Name is required"; return@button }
            if (!m.matches(Regex("[6-9][0-9]{9}"))) { recordMobile.error = "Enter a valid 10-digit mobile number"; return@button }
            val panValue = recordPan.text.toString().trim().uppercase()
            if (panValue.isNotBlank() && !panValue.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))) { recordPan.error = "Enter a valid PAN"; return@button }
            val aadhaarValue = recordAadhaar.text.toString().trim()
            if (aadhaarValue.isNotBlank() && !aadhaarValue.matches(Regex("[0-9]{12}"))) { recordAadhaar.error = "Enter a valid 12-digit Aadhaar"; return@button }
            val gstinValue = recordGstin.text.toString().trim().uppercase()
            if (gstinValue.isNotBlank() && !gstinValue.matches(Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]"))) { recordGstin.error = "Enter a valid GSTIN"; return@button }
            val pin = recordPin.text.toString().trim()
            if (pin.isNotBlank() && !pin.matches(Regex("[1-9][0-9]{5}"))) { recordPin.error = "Enter a valid 6-digit PIN"; return@button }

            if (selectedPersonId == null) {
                V7Records.person(this,n,m,panValue,aadhaarValue,gstinValue,recordEmail.text.toString(),recordAddress.text.toString(),pin,recordCity.text.toString(),recordDistrict.text.toString(),recordState.text.toString(),pendingPhotoUri)
                Toast.makeText(this, "Profile created in V7.", Toast.LENGTH_SHORT).show()
            } else {
                val p = V7Core.find(this,V7Core.Keys.PEOPLE,selectedPersonId!!) ?: return@button
                p.put("name",n);p.put("mobile",m);p.put("pan",panValue);p.put("aadhaar",aadhaarValue);p.put("gstin",gstinValue)
                p.put("email",recordEmail.text.toString().trim());p.put("address",recordAddress.text.toString().trim());p.put("pin",pin)
                p.put("city",recordCity.text.toString().trim());p.put("district",recordDistrict.text.toString().trim());p.put("state",recordState.text.toString().trim())
                if(pendingPhotoUri.isNotBlank()) p.put("photoUri",pendingPhotoUri)
                V7Core.replace(this,V7Core.Keys.PEOPLE,p)
                Toast.makeText(this, "Profile changes saved.", Toast.LENGTH_SHORT).show()
            }
            selectedPersonId = null
            pendingPhotoUri = ""
            clearRecordFields()
            renderRecordList(body)
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(7) })

        renderRecordList(body)
        setContentView(shell("Record & Identity", "One editable profile containing identity, contact, address and picture.", body))
    }

    private fun clearRecordFields() {
        listOf(recordName,recordMobile,recordPan,recordAadhaar,recordGstin,recordEmail,recordAddress,recordPin,recordCity,recordDistrict,recordState).forEach { it.text.clear() }
    }

    private fun loadPerson(p: JSONObject) {
        selectedPersonId = p.optString("id")
        recordName.setText(p.optString("name"))
        recordMobile.setText(p.optString("mobile"))
        recordPan.setText(p.optString("pan"))
        recordAadhaar.setText(p.optString("aadhaar"))
        recordGstin.setText(p.optString("gstin"))
        recordEmail.setText(p.optString("email"))
        recordAddress.setText(p.optString("address"))
        recordPin.setText(p.optString("pin"))
        recordCity.setText(p.optString("city"))
        recordDistrict.setText(p.optString("district"))
        recordState.setText(p.optString("state"))
        pendingPhotoUri = p.optString("photoUri")
    }

    private fun renderRecordList(body: LinearLayout) {
        body.findViewWithTag<View>("v7_record_list")?.let { body.removeView(it) }
        val box = LinearLayout(this).apply {
            tag = "v7_record_list"; orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(8), dp(2), 0)
        }
        box.addView(ArthSaathiV7Design.section(this, "Saved Profiles", "Tap Edit to update the complete profile."))
        val people = V7Core.all(this, V7Core.Keys.PEOPLE)
        if (people.isEmpty()) box.addView(ArthSaathiV7Design.text(this, "No profiles recorded yet.", 10f, ArthSaathiV7Design.MUTED))
        people.takeLast(20).reversed().forEach { p ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = ArthSaathiV7Design.card(this@V7NativeModuleActivity, Color.WHITE, 10)
                setPadding(dp(9), dp(5), dp(5), dp(5))
            }
            val info = ArthSaathiV7Design.text(this, p.optString("name") + "  •  " + p.optString("mobile"), 11.5f, ArthSaathiV7Design.NAVY, true)
            row.addView(info, LinearLayout.LayoutParams(0, dp(48), 1f))
            row.addView(button("Edit") { loadPerson(p) }, LinearLayout.LayoutParams(dp(82), dp(44)))
            box.addView(row, LinearLayout.LayoutParams(-1, dp(52)).apply { topMargin = dp(5) })
        }
        body.addView(box)
    }

    private fun credit() {
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(ArthSaathiV7Design.section(this, "Register Credit", "Selection-driven terms with automatic repayment calculation and schedule."))

        val people = V7Core.all(this, V7Core.Keys.PEOPLE)
        val labels = if (people.isEmpty()) listOf("No person — create one in Record") else people.map { it.optString("name") + " • " + it.optString("mobile") }
        val personSpinner = Spinner(this).apply { adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item, labels) }
        body.addView(personSpinner, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })

        val guarantorLabels = listOf("No guarantor") + people.map { it.optString("name") + " • " + it.optString("mobile") }
        val guarantorSpinner = Spinner(this).apply { adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item, guarantorLabels) }
        body.addView(labelledSpinner("Guarantor (optional)", guarantorSpinner), LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })

        val amount = field("Credit amount (₹)", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        body.addView(amount, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })

        val roiSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("0%","6%","8%","10%","12%","15%","18%","24%","36%","Custom"))
        }
        body.addView(labelledSpinner("ROI", roiSpinner), LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })

        val customRoi = field("Custom ROI %", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        customRoi.visibility = View.GONE
        body.addView(customRoi, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(2) })
        roiSpinner.onItemSelectedListener = object: android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                customRoi.visibility = if (position == 9) View.VISIBLE else View.GONE
            }
        }

        val methodSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("EMI","Principal + Interest","Bullet"))
        }
        body.addView(labelledSpinner("Repayment method", methodSpinner), LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })

        val tenureSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("3 months","6 months","9 months","12 months","18 months","24 months","36 months","48 months","60 months"))
        }
        body.addView(labelledSpinner("Repayment tenure", tenureSpinner), LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })

        val frequencySpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("Monthly","Quarterly","Half-yearly","Yearly"))
        }
        body.addView(labelledSpinner("Repayment frequency", frequencySpinner), LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })

        val startDate = TextView(this).apply {
            text = "Start date: Select"
            textSize = 14f; setTextColor(ArthSaathiV7Design.NAVY); gravity = Gravity.CENTER_VERTICAL
            background = ArthSaathiV7Design.card(this@V7NativeModuleActivity, Color.WHITE, 10)
            setPadding(dp(12),0,dp(12),0)
            setOnClickListener {
                val cal = java.util.Calendar.getInstance()
                android.app.DatePickerDialog(this@V7NativeModuleActivity,{_,y,m,d0->
                    text = "Start date: %04d-%02d-%02d".format(y,m+1,d0)
                },cal.get(java.util.Calendar.YEAR),cal.get(java.util.Calendar.MONTH),cal.get(java.util.Calendar.DAY_OF_MONTH)).show()
            }
        }
        body.addView(startDate, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })

        val preview = ArthSaathiV7Design.text(this,"Repayment schedule will be calculated automatically.",10.5f,ArthSaathiV7Design.NAVY)
        body.addView(preview, LinearLayout.LayoutParams(-1,-2).apply { topMargin = dp(8) })

        body.addView(button("Calculate Repayment Schedule") {
            val a = amount.text.toString().toDoubleOrNull()
            if (a == null || a <= 0) { amount.error = "Enter a valid amount"; return@button }
            val roi = if (roiSpinner.selectedItemPosition == 9) customRoi.text.toString().toDoubleOrNull() ?: -1.0 else roiSpinner.selectedItemPosition.let { listOf(0.0,6.0,8.0,10.0,12.0,15.0,18.0,24.0,36.0)[it] }
            if (roi < 0.0 || roi > 100.0) { customRoi.error = "ROI must be 0–100%"; return@button }
            val months = listOf(3,6,9,12,18,24,36,48,60)[tenureSpinner.selectedItemPosition]
            val method = methodSpinner.selectedItem.toString()
            val monthlyRate = roi / 1200.0
            val emi = if (method == "EMI") {
                if (monthlyRate == 0.0) a / months else a * monthlyRate * Math.pow(1+monthlyRate,months.toDouble()) / (Math.pow(1+monthlyRate,months.toDouble())-1)
            } else 0.0
            val interest = a * roi / 100.0 * months / 12.0
            val periodic = when(frequencySpinner.selectedItemPosition){0->1;1->3;2->6;else->12}
            val installments = kotlin.math.ceil(months.toDouble()/periodic).toInt()
            preview.text = when(method){
                "EMI" -> "EMI: ₹%.2f\nTotal payable: ₹%.2f\nInstallments: %d\nFirst due: calculated from selected start date".format(emi,emi*months,months)
                "Principal + Interest" -> "Principal per period: ₹%.2f\nEstimated total interest: ₹%.2f\nInstallments: %d".format(a/installments,interest,installments)
                else -> "Bullet repayment\nPrincipal: ₹%.2f\nEstimated interest: ₹%.2f\nDue after: %d months".format(a,interest,months)
            }
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })

        body.addView(button("Register Credit in V7") {
            if (people.isEmpty()) { Toast.makeText(this, "Create a person first in Record.", Toast.LENGTH_SHORT).show(); return@button }
            val a = amount.text.toString().toDoubleOrNull()
            if (a == null || a <= 0) { amount.error = "Enter a valid amount"; return@button }
            val roi = if (roiSpinner.selectedItemPosition == 9) customRoi.text.toString().toDoubleOrNull() ?: -1.0 else listOf(0.0,6.0,8.0,10.0,12.0,15.0,18.0,24.0,36.0)[roiSpinner.selectedItemPosition]
            if (roi < 0.0 || roi > 100.0) { customRoi.error = "ROI must be 0–100%"; return@button }
            val months = listOf(3,6,9,12,18,24,36,48,60)[tenureSpinner.selectedItemPosition]
            val method = methodSpinner.selectedItem.toString()
            val periodic = when(frequencySpinner.selectedItemPosition){0->1;1->3;2->6;else->12}
            val monthlyRate = roi / 1200.0
            val emi = if (method=="EMI") {
                if(monthlyRate==0.0) a/months else a*monthlyRate*Math.pow(1+monthlyRate,months.toDouble())/(Math.pow(1+monthlyRate,months.toDouble())-1)
            } else 0.0
            val interest = a*roi/100.0*months/12.0
            val rel = V7Records.relationship(this,people[personSpinner.selectedItemPosition].optString("id"),"INFORMAL_CREDIT","RECEIVABLE",a,roi,method.uppercase().replace(" ","_"),"")
            rel.put("tenureMonths",months);rel.put("frequency",frequencySpinner.selectedItem.toString());rel.put("periodMonths",periodic)
            rel.put("guarantorId",if(guarantorSpinner.selectedItemPosition>0) people[guarantorSpinner.selectedItemPosition-1].optString("id") else "")
            rel.put("emiAmount",emi);rel.put("estimatedInterest",interest);rel.put("startDate",startDate.text.toString().removePrefix("Start date: ").trim())
            rel.put("firstDueDate",startDate.text.toString().removePrefix("Start date: ").trim());rel.put("scheduleStatus","CALCULATED")
            V7Core.replace(this,V7Core.Keys.RELATIONSHIPS,rel)
            Toast.makeText(this, "Credit registered with calculated terms.", Toast.LENGTH_SHORT).show()
            renderRelationshipList(body)
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(7) })

        renderRelationshipList(body)
        setContentView(shell("Credit & Money Relationships", "Selection-driven credit terms and automatic repayment calculation.", body))
    }

    private fun labelledSpinner(label:String, spinner:Spinner):LinearLayout {
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        box.addView(ArthSaathiV7Design.text(this,label,10f,ArthSaathiV7Design.MUTED,true))
        box.addView(spinner,LinearLayout.LayoutParams(-1,dp(48)))
        return box
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
        body.addView(ArthSaathiV7Design.section(this, "Repayment Centre", "Select the repayment type and payment method; balances are updated only after required consent."))

        val rels = V7Core.all(this, V7Core.Keys.RELATIONSHIPS).filter { it.optDouble("outstanding") > 0.005 }
        val labels = if (rels.isEmpty()) listOf("No outstanding V7 relationship") else rels.map { "₹ %.2f outstanding • %s".format(it.optDouble("outstanding"),it.optString("type")) }
        val relationshipSpinner = Spinner(this).apply { adapter = ArrayAdapter(this@V7NativeModuleActivity,android.R.layout.simple_spinner_dropdown_item,labels) }
        body.addView(relationshipSpinner,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(6)})

        val repaymentType=Spinner(this).apply{
            adapter=ArrayAdapter(this@V7NativeModuleActivity,android.R.layout.simple_spinner_dropdown_item,
                listOf("Principal","Interest","EMI","Bullet repayment"))
        }
        body.addView(labelledSpinner("Repayment type",repaymentType),LinearLayout.LayoutParams(-1,dp(70)).apply{topMargin=dp(6)})

        val amount=field("Amount paid (₹)",android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        body.addView(amount,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(6)})

        val method=Spinner(this).apply{
            adapter=ArrayAdapter(this@V7NativeModuleActivity,android.R.layout.simple_spinner_dropdown_item,
                listOf("UPI","NEFT","Bank Transfer","Cash","NACH","Cheque","Other"))
        }
        body.addView(labelledSpinner("Payment method",method),LinearLayout.LayoutParams(-1,dp(70)).apply{topMargin=dp(6)})

        val note=field("Reference / note (optional)")
        body.addView(note,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(6)})

        val status=ArthSaathiV7Design.text(this,"No repayment recorded yet.",10.5f,ArthSaathiV7Design.NAVY)
        body.addView(status,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})

        body.addView(button("Record Repayment") {
            if(rels.isEmpty()){status.text="No outstanding relationship.";return@button}
            val rel=rels[relationshipSpinner.selectedItemPosition]
            val paid=amount.text.toString().toDoubleOrNull()
            if(paid==null||paid<=0){amount.error="Enter a valid payment amount";return@button}
            val outstanding=rel.optDouble("outstanding")
            if(paid>outstanding+0.005){amount.error="Payment cannot exceed outstanding amount";return@button}
            val type=repaymentType.selectedItem.toString()
            val principal=when(type){
                "Interest"->0.0
                "Principal"->paid
                else->{
                    val roi=rel.optDouble("roiPercent",0.0)
                    val monthlyInterest=outstanding*roi/1200.0
                    (paid-monthlyInterest).coerceIn(0.0,paid)
                }
            }
            val interest=(paid-principal).coerceAtLeast(0.0)
            V7Records.repayment(this,rel.optString("id"),paid,principal,interest,method.selectedItem.toString().uppercase().replace(" ","_"),true)
            val after=V7Core.find(this,V7Core.Keys.RELATIONSHIPS,rel.optString("id"))?.optDouble("outstanding")?:outstanding
            if(after>=outstanding-0.0001){
                status.text="Not recorded: active OTP-verified consent for this relationship is required."
            }else{
                status.text="Repayment recorded. Outstanding: ₹%.2f".format(after)
                renderRepaymentList(body)
            }
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})

        body.addView(ArthSaathiV7Design.text(this,
            "Every repayment is audited. No consent means no balance mutation.",9.5f,ArthSaathiV7Design.MUTED),
            LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        renderRepaymentList(body)
        setContentView(shell("Repayment Centre", "Structured repayment types, payment methods and consent enforcement.", body))
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
        body.addView(ArthSaathiV7Design.section(this, "Asset Vault", "Choose the asset nature first; the relevant fields appear automatically."))

        val nature = Spinner(this).apply {
            adapter = ArrayAdapter(this@V7NativeModuleActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("Property","Vehicle","Bank Deposit / FD","Investment","Insurance Policy","Business Interest","Gold / Jewellery","Other"))
        }
        body.addView(labelledSpinner("Nature of asset", nature), LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })

        val dynamic = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(dynamic, LinearLayout.LayoutParams(-1,-2).apply { topMargin = dp(2) })

        val description = field("Asset description")
        val value = field("Current value (₹)", android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL)
        body.addView(description, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })
        body.addView(value, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(6) })

        val people = V7Core.all(this,V7Core.Keys.PEOPLE)
        val nomineeLabels = listOf("No nominee selected") + people.map{it.optString("name")+" • "+it.optString("mobile")}
        val nominee = Spinner(this).apply {
            adapter = ArrayAdapter(this@V7NativeModuleActivity,android.R.layout.simple_spinner_dropdown_item,nomineeLabels)
        }
        body.addView(labelledSpinner("Asset-wise nominee",nominee), LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(6) })

        fun rebuild() {
            dynamic.removeAllViews()
            when(nature.selectedItemPosition) {
                0 -> {
                    val address=field("Property address")
                    val title=field("Title / deed document reference")
                    dynamic.addView(address,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.addView(title,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.tag = arrayOf(address,title)
                }
                1 -> {
                    val reg=field("Vehicle registration number")
                    val model=field("Make / model")
                    dynamic.addView(reg,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.addView(model,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.tag = arrayOf(reg,model)
                }
                2 -> {
                    val bank=field("Bank / institution")
                    val maturity=field("Maturity date / reference")
                    dynamic.addView(bank,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.addView(maturity,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.tag = arrayOf(bank,maturity)
                }
                3 -> {
                    val instrument=field("Instrument / folio / account reference")
                    val platform=field("Platform / institution")
                    dynamic.addView(instrument,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.addView(platform,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.tag = arrayOf(instrument,platform)
                }
                4 -> {
                    val policy=field("Policy number / insurer")
                    val renewal=field("Renewal / maturity date")
                    dynamic.addView(policy,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.addView(renewal,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.tag = arrayOf(policy,renewal)
                }
                5 -> {
                    val entity=field("Business / entity name")
                    val ownership=field("Ownership percentage")
                    dynamic.addView(entity,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.addView(ownership,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.tag = arrayOf(entity,ownership)
                }
                else -> {
                    val detail=field("Asset-specific detail")
                    dynamic.addView(detail,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(5)})
                    dynamic.tag = arrayOf(detail)
                }
            }
        }
        nature.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent:android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent:android.widget.AdapterView<*>?,view:View?,position:Int,id:Long){rebuild()}
        }
        rebuild()

        body.addView(button("Save Asset in V7") {
            val v=value.text.toString().toDoubleOrNull()
            if(v==null||v<0){value.error="Enter a valid value";return@button}
            val extra=(dynamic.tag as? Array<*>)?.mapNotNull{it as? EditText}?.joinToString(" | "){it.text.toString().trim()}.orEmpty()
            val nomineeId=if(nominee.selectedItemPosition>0) people[nominee.selectedItemPosition-1].optString("id") else ""
            val asset=V7Records.asset(this,V7Core.user(this),nature.selectedItem.toString(),description.text.toString()+" | "+extra,v,"",nomineeId)
            asset.put("assetNature",nature.selectedItem.toString())
            asset.put("dynamicDetails",extra)
            V7Core.replace(this,V7Core.Keys.ASSETS,asset)
            Toast.makeText(this,"Asset saved with type-specific details and nominee link.",Toast.LENGTH_SHORT).show()
            renderAssetList(body)
        }, LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        renderAssetList(body)
        setContentView(shell("Asset Vault", "Dynamic asset forms with evidence and asset-wise nominee.", body))
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
            if (a == null || o == null || a < 0 || o < 0 || o > a) { amount.error = "Enter valid amounts; outstanding cannot exceed original amount"; return@button }
            val rateValue = rate.text.toString().toDoubleOrNull() ?: 0.0
            if (rateValue < 0.0 || rateValue > 100.0) { rate.error = "Rate must be between 0 and 100%"; return@button }
            V7Records.liability(this, V7Core.user(this), type.text.toString(), a, o, rateValue)
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
