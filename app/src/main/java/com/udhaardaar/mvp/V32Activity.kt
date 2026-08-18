package com.udhaardaar.mvp

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow
import kotlin.random.Random

class V32Activity : AppCompatActivity() {
    private lateinit var db: V32DatabaseHelper
    private val prefs by lazy { getSharedPreferences("udhaardaar_v32_session", MODE_PRIVATE) }
    private var userId = ""
    private var pendingOtp = ""
    private var selectedPhoto: Uri? = null
    private var photoTarget: ImageView? = null

    private fun text(value: String, size: Float = 16f) = TextView(this).apply { text = value; textSize = size; setPadding(8, 10, 8, 10) }
    private fun edit(hint: String, numeric: Boolean = false) = EditText(this).apply {
        this.hint = hint
        setPadding(18, 8, 18, 8)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
    }
    private fun phoneEdit(hint: String) = EditText(this).apply {
        this.hint = hint
        setPadding(18, 8, 18, 8)
        inputType = InputType.TYPE_CLASS_PHONE
        filters = arrayOf(InputFilter.LengthFilter(10))
        keyListener = android.text.method.DigitsKeyListener.getInstance("0123456789")
    }
    private fun button(label: String, action: () -> Unit) = Button(this).apply { text = label; setOnClickListener { action() }; isAllCaps = false }
    private fun base(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 22, 22, 30) }
    private fun show(root: View) { setContentView(ScrollView(this).apply { addView(root) }) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = V32DatabaseHelper(this)
        val c = db.user()
        val exists = c.moveToFirst()
        c.close()
        if (exists && prefs.getBoolean("logged_in", false)) dashboard() else loginOrRegister()
    }

    private fun loginOrRegister() {
        val r = base()
        r.addView(text("UDHAARDAAR", 30f).apply { gravity = Gravity.CENTER })
        r.addView(text("V3.2.1 • Secure profile-first credit management", 15f).apply { gravity = Gravity.CENTER })

        val mobile = phoneEdit("Mobile number (10 digits)")
        val name = edit("Full name")
        val address = edit("Address")
        val email = edit("Email")
        val otp = phoneEdit("6-digit OTP").apply { filters = arrayOf(InputFilter.LengthFilter(6)); visibility = View.GONE }
        val verify = button("Verify OTP & Save Profile") { verifyUserProfile(otp, name, mobile, address, email) }.apply { visibility = View.GONE }

        val c = db.user()
        val exists = c.moveToFirst()
        c.close()

        if (!exists) {
            r.addView(text("Create your Lender / Account Owner profile", 20f))
            listOf(name, mobile, address, email).forEach { r.addView(it) }
            val photo = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(-1, 180)
                setImageResource(android.R.drawable.ic_menu_camera)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
            r.addView(photo)
            r.addView(button("Add profile photo (optional)") { pickPhoto(photo) })
            r.addView(button("Create Profile + Send OTP") {
                val cleanMobile = mobile.text.toString()
                if (name.text.isNullOrBlank() || cleanMobile.length != 10) {
                    Toast.makeText(this, "Enter name and exactly 10-digit mobile number", Toast.LENGTH_SHORT).show()
                    return@button
                }
                pendingOtp = otpCode()
                otp.visibility = View.VISIBLE
                verify.visibility = View.VISIBLE
                Toast.makeText(this, "OTP sent. Demo OTP: $pendingOtp", Toast.LENGTH_LONG).show()
            })
            r.addView(otp)
            r.addView(verify)
        } else {
            r.addView(text("Login", 22f))
            r.addView(mobile)
            r.addView(button("Send OTP") {
                if (mobile.text.toString().length != 10) {
                    Toast.makeText(this, "Enter exactly 10 digits", Toast.LENGTH_SHORT).show()
                    return@button
                }
                pendingOtp = otpCode()
                otp.visibility = View.VISIBLE
                verify.visibility = View.VISIBLE
                Toast.makeText(this, "OTP sent. Demo OTP: $pendingOtp", Toast.LENGTH_LONG).show()
            })
            r.addView(otp)
            r.addView(verify)
        }
        show(r)
    }

    private fun verifyUserProfile(otp: EditText, name: EditText, mobile: EditText, address: EditText, email: EditText) {
        if (pendingOtp.isEmpty()) {
            Toast.makeText(this, "Please send OTP first", Toast.LENGTH_SHORT).show()
            return
        }
        if (otp.text.toString() != pendingOtp) {
            otp.error = "Incorrect OTP"
            return
        }
        try {
            userId = "USR-${System.currentTimeMillis()}"
            val row = db.saveUser(userId, name.text.toString(), mobile.text.toString(), address.text.toString(), email.text.toString(), selectedPhoto?.toString())
            if (row > 0) {
                prefs.edit().putBoolean("logged_in", true).apply()
                pendingOtp = ""
                Toast.makeText(this, "Profile saved. User ID: $userId", Toast.LENGTH_LONG).show()
                dashboard()
            } else {
                Toast.makeText(this, "Could not save profile", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Profile save failed: ${e.message ?: "unknown error"}", Toast.LENGTH_LONG).show()
        }
    }

    private fun dashboard() {
        val r = base()
        val c = db.user()
        var name = ""
        if (c.moveToFirst()) {
            userId = c.getString(c.getColumnIndexOrThrow("unique_id"))
            name = c.getString(c.getColumnIndexOrThrow("name"))
        }
        c.close()
        r.addView(text("Welcome, $name", 26f))
        r.addView(text("Lender ID: $userId", 14f))
        r.addView(text("Borrowers: ${db.borrowerCount()}   |   Guarantors: ${db.guarantorCount()}   |   Credits: ${db.creditCount()}", 15f))
        r.addView(button("My Lender Profile") { lenderProfile() })
        r.addView(button("Add / Search Borrower Profile") { profileForm("BORROWER") })
        r.addView(button("Add / Search Guarantor Profile") { profileForm("GUARANTOR") })
        r.addView(button("Register New Credit") { creditForm() })
        r.addView(button("Logout") { prefs.edit().putBoolean("logged_in", false).apply(); loginOrRegister() })
        show(r)
    }

    private fun lenderProfile() {
        val r = base()
        r.addView(text("Lender / Account Owner Profile", 24f))
        val c = db.user()
        if (c.moveToFirst()) {
            r.addView(text("Unique ID: ${c.getString(c.getColumnIndexOrThrow("unique_id"))}"))
            r.addView(text("Name: ${c.getString(c.getColumnIndexOrThrow("name"))}"))
            r.addView(text("Mobile: ${c.getString(c.getColumnIndexOrThrow("mobile"))}"))
            r.addView(text("Address: ${c.getString(c.getColumnIndexOrThrow("address"))}"))
            r.addView(text("Email: ${c.getString(c.getColumnIndexOrThrow("email"))}"))
        }
        c.close()
        r.addView(button("Back to Dashboard") { dashboard() })
        show(r)
    }

    private fun profileForm(role: String) {
        val r = base()
        r.addView(text("${if (role == "BORROWER") "Borrower" else "Guarantor"} Profile", 25f))
        val id = edit("Unique ID (leave blank to generate)")
        val name = edit("Full name *")
        val mobile = phoneEdit("Mobile number * (10 digits)")
        val alt = phoneEdit("Alternate mobile (10 digits)")
        val address = edit("Full address *")
        val city = edit("City")
        val state = edit("State")
        val pin = edit("PIN code", true)
        val email = edit("Email")
        val pan = edit("PAN")
        val aadhaar = edit("Aadhaar / other ID")
        val occupation = edit("Occupation / Business type")
        val business = edit("Business name (optional)")
        val gstin = edit("GSTIN (optional)")
        val photo = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 170)
            setImageResource(android.R.drawable.ic_menu_camera)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        listOf(id, name, mobile, alt, address, city, state, pin, email, pan, aadhaar, occupation, business, gstin).forEach { r.addView(it) }
        r.addView(photo)
        r.addView(button("Add photo (optional)") { pickPhoto(photo) })
        r.addView(button("SAVE ${if (role == "BORROWER") "BORROWER" else "GUARANTOR"} PROFILE") {
            if (name.text.isNullOrBlank() || mobile.text.length != 10 || address.text.isNullOrBlank()) {
                Toast.makeText(this, "Name, exactly 10-digit mobile and address are required", Toast.LENGTH_SHORT).show()
                return@button
            }
            val uid = if (id.text.isNullOrBlank()) "${if (role == "BORROWER") "BOR" else "GUA"}-${System.currentTimeMillis()}" else id.text.toString()
            val row = db.addProfile(role, uid, name.text.toString(), mobile.text.toString(), alt.text.toString(), address.text.toString(), city.text.toString(), state.text.toString(), pin.text.toString(), email.text.toString(), pan.text.toString(), aadhaar.text.toString(), occupation.text.toString(), business.text.toString(), gstin.text.toString(), selectedPhoto?.toString())
            if (row > 0) {
                db.log(userId, row, null, "PROFILE_CREATED", "$role profile $uid created")
                Toast.makeText(this, "Profile saved: $uid", Toast.LENGTH_LONG).show()
                dashboard()
            } else Toast.makeText(this, "Could not save profile", Toast.LENGTH_LONG).show()
        })
        r.addView(button("Back") { dashboard() })
        show(r)
    }

    private fun creditForm() {
        val r = base()
        r.addView(text("Register Credit", 25f))
        val borrowers = mutableListOf<Long>()
        val labels = mutableListOf<String>()
        val c = db.profiles("BORROWER")
        while (c.moveToNext()) { borrowers.add(c.getLong(0)); labels.add("${c.getString(2)} • ${c.getString(3)} • ${c.getString(1)}") }
        c.close()
        if (labels.isEmpty()) { Toast.makeText(this, "Create a borrower profile first", Toast.LENGTH_LONG).show(); profileForm("BORROWER"); return }
        val b = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, labels) }
        r.addView(text("Borrower")); r.addView(b)
        val type = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Personal Credit", "Business Credit", "Trade Credit", "Advance", "Rental / Lease", "Other")) }
        r.addView(text("Credit Type")); r.addView(type)
        val direction = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Credit Given", "Credit Received")) }
        r.addView(direction)
        val principal = edit("Principal amount", true)
        val roi = edit("Annual ROI %", true)
        val tenor = edit("Tenor in months", true)
        val method = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, arrayOf("EMI", "Principal + Interest")) }
        val result = text("Enter principal, ROI and tenor to calculate repayment", 15f)
        r.addView(principal); r.addView(roi); r.addView(tenor); r.addView(text("Repayment Method")); r.addView(method); r.addView(result)
        val start = edit("Start date (YYYY-MM-DD)")
        val end = edit("End date (auto-calculated)").apply { isFocusable = false }
        r.addView(start); r.addView(end)
        val guarantor = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Guarantor: NO", "Guarantor: YES")) }
        r.addView(text("Is guarantor available?")); r.addView(guarantor)
        val gIds = mutableListOf<Long>(); val gLabels = mutableListOf<String>()
        val gc = db.profiles("GUARANTOR")
        while (gc.moveToNext()) { gIds.add(gc.getLong(0)); gLabels.add("${gc.getString(2)} • ${gc.getString(3)} • ${gc.getString(1)}") }
        gc.close()
        val gSpinner = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, if (gLabels.isEmpty()) arrayOf("No guarantor profiles yet") else gLabels.toTypedArray()); visibility = View.GONE }
        r.addView(gSpinner)
        guarantor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p: AdapterView<*>?) {}
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, itemId: Long) { gSpinner.visibility = if (pos == 1) View.VISIBLE else View.GONE }
        }
        fun calc() {
            val p = principal.text.toString().toDoubleOrNull() ?: return
            val rate = roi.text.toString().toDoubleOrNull() ?: return
            val n = tenor.text.toString().toIntOrNull() ?: return
            if (n <= 0) return
            val monthly = rate / 1200.0
            val emi = if (monthly == 0.0) p / n else p * monthly * (1 + monthly).pow(n) / ((1 + monthly).pow(n) - 1)
            val interest = if (method.selectedItemPosition == 0) emi * n - p else p * rate / 100.0 * n / 12.0
            val payment = if (method.selectedItemPosition == 0) emi else p / n + interest / n
            result.text = "${method.selectedItem}: ${money(payment)} per month\nTotal interest: ${money(interest)}\nTotal payable: ${money(p + interest)}\nInstallments: $n"
            val d = Calendar.getInstance(); start.setText(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d.time)); d.add(Calendar.MONTH, n); end.setText(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d.time))
        }
        principal.setOnFocusChangeListener { _, has -> if (!has) calc() }; roi.setOnFocusChangeListener { _, has -> if (!has) calc() }; tenor.setOnFocusChangeListener { _, has -> if (!has) calc() }
        method.onItemSelectedListener = object : AdapterView.OnItemSelectedListener { override fun onNothingSelected(p: AdapterView<*>?) {}; override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { calc() } }
        r.addView(button("CALCULATE / REFRESH REPAYMENT") { calc() })
        r.addView(button("SEND CONSENT OTP") { pendingOtp = otpCode(); Toast.makeText(this, "Demo OTP: $pendingOtp", Toast.LENGTH_LONG).show() })
        val otp = phoneEdit("Enter 6-digit OTP").apply { filters = arrayOf(InputFilter.LengthFilter(6)) }
        r.addView(otp)
        r.addView(button("VERIFY OTP + REGISTER CREDIT") {
            if (pendingOtp.isEmpty()) { Toast.makeText(this, "Send OTP first", Toast.LENGTH_SHORT).show(); return@button }
            if (otp.text.toString() != pendingOtp) { otp.error = "Incorrect OTP"; return@button }
            val p = principal.text.toString().toDoubleOrNull() ?: 0.0; val rate = roi.text.toString().toDoubleOrNull() ?: 0.0; val n = tenor.text.toString().toIntOrNull() ?: 0
            if (p <= 0 || n <= 0) { Toast.makeText(this, "Enter valid principal and tenor", Toast.LENGTH_SHORT).show(); return@button }
            val m = rate / 1200.0; val emi = if (m == 0.0) p / n else p * m * (1 + m).pow(n) / ((1 + m).pow(n) - 1); val interest = if (method.selectedItemPosition == 0) emi * n - p else p * rate / 100 * n / 12; val payment = if (method.selectedItemPosition == 0) emi else p / n + interest / n
            val gid = if (guarantor.selectedItemPosition == 1 && gIds.isNotEmpty()) gIds[gSpinner.selectedItemPosition.coerceAtMost(gIds.lastIndex)] else null
            val credit = db.addCredit(borrowers[b.selectedItemPosition], gid, type.selectedItem.toString(), if (direction.selectedItemPosition == 0) "GIVEN" else "RECEIVED", p, rate, n, method.selectedItem.toString(), payment, interest, p + interest, start.text.toString(), end.text.toString())
            if (credit > 0) { db.log(userId, borrowers[b.selectedItemPosition], credit, "CREDIT_REGISTERED", "OTP verified; ${method.selectedItem}; repayment calculated automatically"); Toast.makeText(this, "Credit registered successfully. OTP verified.", Toast.LENGTH_LONG).show(); pendingOtp = ""; dashboard() } else Toast.makeText(this, "Unable to register credit", Toast.LENGTH_LONG).show()
        })
        r.addView(button("Back") { dashboard() }); show(r)
    }

    private fun otpCode() = Random.nextInt(100000, 1000000).toString()
    private fun money(v: Double) = "₹" + String.format(Locale.US, "%,.2f", v)
    private fun pickPhoto(target: ImageView) { photoTarget = target; startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE) }, 901) }
    @Deprecated("Compatibility") override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { super.onActivityResult(requestCode, resultCode, data); if (requestCode == 901 && resultCode == RESULT_OK) { selectedPhoto = data?.data; photoTarget?.setImageURI(selectedPhoto) } }
}
