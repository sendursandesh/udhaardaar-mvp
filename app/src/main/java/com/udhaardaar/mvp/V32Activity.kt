package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
    private val prefs by lazy { getSharedPreferences("udhaardaar_v32", MODE_PRIVATE) }
    private var otp = ""
    private var selectedPhoto: Uri? = null
    private var selectedInvoice: Uri? = null

    private val sky = Color.rgb(225, 244, 255)
    private val blue = Color.rgb(25, 111, 220)
    private val teal = Color.rgb(0, 145, 135)
    private val green = Color.rgb(25, 145, 78)
    private val navy = Color.rgb(24, 58, 92)
    private val red = Color.rgb(190, 55, 55)
    private val amber = Color.rgb(225, 145, 20)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        db = V32DatabaseHelper(this)
        if (db.hasUser() && prefs.getBoolean("logged_in", false)) dashboard() else registerOwner()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun root() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(28))
        setBackgroundColor(sky)
    }
    private fun label(s: String, size: Float = 16f, color: Int = navy) = TextView(this).apply {
        text = s; textSize = size; setTextColor(color); setPadding(dp(4), dp(5), dp(4), dp(5))
    }
    private fun field(hint: String, numeric: Boolean = false, max: Int = 0) = EditText(this).apply {
        this.hint = hint; textSize = 16f; setPadding(dp(12), dp(8), dp(12), dp(8))
        background = box(Color.WHITE, Color.rgb(190, 210, 225), 14)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        if (max > 0) filters = arrayOf(InputFilter.LengthFilter(max))
    }
    private fun button(s: String, color: Int = blue, action: () -> Unit) = Button(this).apply {
        text = s; isAllCaps = false; textSize = 14f; setTextColor(Color.WHITE)
        background = box(color, color, 18); setOnClickListener { action() }
    }
    private fun box(fill: Int, stroke: Int, radius: Int) = GradientDrawable().apply {
        setColor(fill); setStroke(dp(1), stroke); cornerRadius = dp(radius).toFloat()
    }
    private fun show(content: View) { setContentView(ScrollView(this).apply { isFillViewport = true; addView(content) }) }
    private fun gap(n: Int) = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(n)) }
    private fun money(v: Double) = "₹" + String.format(Locale.US, "%,.2f", v)
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun plusMonths(s: String, n: Int): String {
        val c = Calendar.getInstance(); c.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(s) ?: Date(); c.add(Calendar.MONTH, n)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
    }
    private fun page(title: String, subtitle: String): LinearLayout {
        val r = root(); val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val titles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        titles.addView(label(title, 25f)); titles.addView(label(subtitle, 12f, Color.DKGRAY))
        head.addView(titles); head.addView(button("HOME", blue) { dashboard() }, LinearLayout.LayoutParams(dp(78), dp(44)))
        r.addView(head); r.addView(gap(8)); return r
    }

    private fun registerOwner() {
        val r = root()
        val brand = label("🤝  UDHAARDAAR", 29f, Color.WHITE).apply { gravity = Gravity.CENTER; background = box(teal, teal, 24); setPadding(dp(10), dp(18), dp(10), dp(18)) }
        r.addView(brand); r.addView(label("Your Credit. Your Trust. Our Record.", 14f).apply { gravity = Gravity.CENTER }); r.addView(gap(10))
        val name = field("Lender / Account Owner name *")
        val mobile = field("Mobile number * (10 digits)", true, 10)
        val address = field("Full address *")
        val email = field("Email (optional)")
        val otpBox = field("Enter 6-digit OTP", true, 6); otpBox.visibility = View.GONE
        listOf(name, mobile, address, email).forEach { r.addView(it, LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(0, dp(3), 0, dp(3)) }) }
        r.addView(button("ADD PROFILE PHOTO (OPTIONAL)", teal) { choosePhoto() }); r.addView(otpBox)
        r.addView(button("CREATE PROFILE + SEND OTP", blue) {
            if (name.text.toString().trim().length < 2 || mobile.text.toString().length != 10 || address.text.toString().trim().length < 5) {
                toast("Name, address and exactly 10-digit mobile are required"); return@button
            }
            otp = Random.nextInt(100000, 1000000).toString(); otpBox.visibility = View.VISIBLE
            toast("Demo OTP: $otp")
        })
        r.addView(button("VERIFY OTP + SAVE PROFILE", green) {
            if (otp.isEmpty() || otpBox.text.toString() != otp) { otpBox.error = "Incorrect OTP"; return@button }
            db.saveUser("USR-${System.currentTimeMillis()}", name.text.toString().trim(), mobile.text.toString(), address.text.toString().trim(), email.text.toString().trim(), selectedPhoto?.toString())
            prefs.edit().putBoolean("logged_in", true).apply(); dashboard()
        })
        show(r)
    }

    private fun dashboard() {
        val r = page("Udhaardaar Dashboard", "Digital informal-credit record & repayment manager")
        val u = db.userData(); r.addView(label("Welcome, ${u?.name ?: "Lender"}", 21f)); r.addView(label("Unique ID: ${u?.id ?: "—"}", 12f, Color.DKGRAY)); r.addView(gap(5))
        val stats = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        stats.addView(label("Credit extended: ${money(db.totalCredit("Credit Given"))}", 16f, blue)); stats.addView(label("Credit received: ${money(db.totalCredit("Credit Received"))}", 16f, teal)); stats.addView(label("Due: ${db.dueCount(false)}    Overdue: ${db.dueCount(true)}", 16f, amber)); r.addView(stats); r.addView(gap(8))
        action(r, "＋", "REGISTER CREDIT", "Borrower → terms → guarantor → documents → OTP", blue) { registerCredit(null) }
        action(r, "⌕", "SEARCH BORROWER / GUARANTOR", "Name, mobile, PAN, Aadhaar, GSTIN or unique ID", teal) { searchProfiles("BORROWER") }
        action(r, "▣", "CREDIT HISTORY", "View registered transactions and borrower history", navy) { history(null) }
        action(r, "₹", "REPAYMENT CENTRE", "Record payments and review due/overdue schedules", green) { repayments(false) }
        action(r, "▤", "DIGITAL DOCUMENTS & CONSENT", "Review acknowledgement and consent record", amber) { documents() }
        action(r, "◎", "MY PROFILE", "Identity, photo and optional bank/NACH details", teal) { ownerProfile() }
        r.addView(button("LOGOUT", Color.DKGRAY) { prefs.edit().putBoolean("logged_in", false).apply(); registerOwner() }); show(r)
    }
    private fun action(r: LinearLayout, icon: String, title: String, sub: String, color: Int, click: () -> Unit) {
        val b = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(10), dp(8), dp(10)); background = box(Color.WHITE, Color.rgb(190, 210, 225), 18); setOnClickListener { click() } }
        b.addView(label(icon, 22f, color), LinearLayout.LayoutParams(dp(48), dp(52))); val t = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        t.addView(label(title, 16f)); t.addView(label(sub, 11f, Color.DKGRAY)); b.addView(t); b.addView(label("›", 28f, color)); r.addView(b); r.addView(gap(7))
    }

    private fun choosePhoto() { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE) }, 100) }
    private fun chooseInvoice() { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "application/pdf"; addCategory(Intent.CATEGORY_OPENABLE } , 101) }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { super.onActivityResult(requestCode, resultCode, data); if (resultCode == RESULT_OK) { if (requestCode == 100) selectedPhoto = data?.data else if (requestCode == 101) selectedInvoice = data?.data } }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()

    private fun searchProfiles(role: String) {
        val r = page(if (role == "BORROWER") "Search Borrower" else "Search Guarantor", "Name • mobile • PAN • Aadhaar • GSTIN • unique ID")
        val q = field("Search"); val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fun refresh() { list.removeAllViews(); val rows = db.searchProfiles(role, q.text.toString()); if (rows.isEmpty()) list.addView(label("No profile found.")); rows.forEach { p -> action(list, "●", p.name, "${p.mobile} • ${p.id}", blue) { if (role == "BORROWER") borrowerSummary(p.rowId) else profileForm(role, p.rowId) } } }
        r.addView(q, LinearLayout.LayoutParams(-1, dp(58))); r.addView(button("SEARCH", blue) { refresh() }); r.addView(button("CREATE NEW PROFILE", green) { profileForm(role, null) }); r.addView(gap(5)); r.addView(list); refresh(); r.addView(button("BACK", Color.DKGRAY) { dashboard() }); show(r)
    }
    private fun profileForm(role: String, rowId: Long?) {
        val p = rowId?.let { db.profileData(it) }; val r = page(if (role == "BORROWER") "Borrower Profile" else "Guarantor Profile", "Photo optional • mobile limited to 10 digits")
        val name = field("Full name *").apply { setText(p?.name ?: "") }; val mobile = field("Mobile number * (10 digits)", true, 10).apply { setText(p?.mobile ?: "") }
        val alternate = field("Alternate mobile (optional)", true, 10).apply { setText(p?.alternate ?: "") }; val address = field("Full address *").apply { setText(p?.address ?: "") }
        val city = field("City").apply { setText(p?.city ?: "") }; val state = field("State").apply { setText(p?.state ?: "") }; val pin = field("PIN code", true, 6).apply { setText(p?.pin ?: "") }
        val pan = field("PAN").apply { setText(p?.pan ?: "") }; val aadhaar = field("Aadhaar", true, 12).apply { setText(p?.aadhaar ?: "") }; val gst = field("GSTIN (optional)").apply { setText(p?.gstin ?: "") }
        listOf(name, mobile, alternate, address, city, state, pin, pan, aadhaar, gst).forEach { r.addView(it, LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(0, dp(2), 0, dp(2)) }) }
        r.addView(button("ADD / CHANGE PHOTO (OPTIONAL)", teal) { choosePhoto() })
        r.addView(button(if (p == null) "SAVE PROFILE" else "UPDATE PROFILE", green) {
            if (name.text.toString().trim().length < 2 || mobile.text.toString().length != 10 || address.text.toString().trim().length < 5) { toast("Name, address and 10-digit mobile are required"); return@button }
            val id = p?.id ?: "${if (role == "BORROWER") "BOR" else "GUA"}-${System.currentTimeMillis()}"
            db.upsertProfile(p?.rowId, role, id, name.text.toString().trim(), mobile.text.toString(), alternate.text.toString(), address.text.toString().trim(), city.text.toString(), state.text.toString(), pin.text.toString(), "", pan.text.toString().uppercase(Locale.US), aadhaar.text.toString(), gst.text.toString().uppercase(Locale.US), selectedPhoto?.toString() ?: p?.photo)
            toast("Profile saved: $id"); searchProfiles(role)
        }); r.addView(button("BACK", Color.DKGRAY) { searchProfiles(role) }); show(r)
    }
    private fun borrowerSummary(id: Long) {
        val p = db.profileData(id) ?: return; val s = db.borrowerSummary(id); val r = page("Borrower Summary", "Review history before registering a new credit")
        r.addView(label(p.name, 24f)); r.addView(label("Unique ID: ${p.id}")); r.addView(label("Mobile: ${p.mobile} • PAN: ${p.pan.ifBlank { "—" }}")); r.addView(label("Total: ${money(s.total)} • Outstanding: ${money(s.outstanding)}", 16f)); r.addView(label("Active: ${s.active} • Overdue: ${s.overdue}", 14f, if (s.overdue > 0) red else green))
        db.creditsForBorrower(id).forEach { c -> action(r, "₹", c.creditId, "${c.type} • ${money(c.amount)} • ${c.status}", if (c.status == "OVERDUE") red else blue) { creditDetail(c.id) } }
        r.addView(button("REGISTER NEW CREDIT FOR THIS BORROWER", blue) { registerCredit(id) }); r.addView(button("BACK", Color.DKGRAY) { searchProfiles("BORROWER") }); show(r)
    }

    private fun registerCredit(preselected: Long?) {
        val borrowers = db.searchProfiles("BORROWER", ""); if (borrowers.isEmpty()) { toast("Create a borrower profile first"); profileForm("BORROWER", null); return }
        val r = page("Register Credit", "Borrower → credit → repayment → guarantor → consent → OTP")
        val borrower = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, borrowers.map { "${it.name} • ${it.mobile}" }); preselected?.let { id -> setSelection(borrowers.indexOfFirst { it.rowId == id }.coerceAtLeast(0)) } }
        val type = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Personal Credit", "Business Credit", "Trade Credit", "Advance", "Rental / Lease", "Other")) }
        val direction = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Credit Given", "Credit Received")) }
        val amount = field("Principal / amount *", true); val roi = field("Annual ROI %", true); val tenor = field("Tenor in months", true); tenor.setText("1")
        val method = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, arrayOf("EMI", "Principal + Interest", "Bullet / Full payment", "Custom")) }
        r.addView(label("Borrower *")); r.addView(borrower); r.addView(label("Nature of credit *")); r.addView(type); r.addView(label("Direction")); r.addView(direction)
        listOf(amount, roi, tenor).forEach { r.addView(it, LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(0, dp(3), 0, dp(3)) }) }; r.addView(label("Repayment method")); r.addView(method)
        val guarantor = Spinner(this).apply { adapter = ArrayAdapter(this@V32Activity, android.R.layout.simple_spinner_dropdown_item, listOf("No guarantor") + db.searchProfiles("GUARANTOR", "").map { "${it.name} • ${it.mobile}" }) }
        r.addView(label("Guarantor")); r.addView(guarantor); r.addView(button("ADD GUARANTOR PROFILE", teal) { profileForm("GUARANTOR", null) })
        val gst = field("GSTIN / invoice reference (trade credit optional)"); r.addView(gst); r.addView(button("UPLOAD INVOICE (OPTIONAL)", teal) { chooseInvoice() })
        val consent = CheckBox(this).apply { text = "I have reviewed the digital credit terms and consent to electronic record creation."; setTextColor(navy) }; r.addView(consent)
        r.addView(button("REVIEW DOCUMENT", amber) { documents() }); val otpBox = field("Enter OTP", true, 6); otpBox.visibility = View.GONE; r.addView(otpBox)
        r.addView(button("SEND OTP FOR FINAL CONSENT", blue) { if (!consent.isChecked || amount.text.toString().toDoubleOrNull() == null) { toast("Enter amount and accept the terms"); return@button }; otp = Random.nextInt(100000, 1000000).toString(); otpBox.visibility = View.VISIBLE; toast("Demo OTP: $otp") })
        r.addView(button("VERIFY OTP + REGISTER CREDIT", green) {
            val principal = amount.text.toString().toDoubleOrNull() ?: 0.0; if (!consent.isChecked || principal <= 0.0 || otpBox.text.toString() != otp) { toast("Complete consent and correct OTP"); return@button }
            val months = tenor.text.toString().toIntOrNull()?.coerceIn(1, 240) ?: 1; val rate = (roi.text.toString().toDoubleOrNull() ?: 0.0) / 100.0; val interest = principal * rate * months / 12.0; val payable = principal + interest
            val installment = if (method.selectedItem.toString() == "EMI" && months > 0) { val m = rate / 12.0; if (m == 0.0) principal / months else principal * m * (1 + m).pow(months) / ((1 + m).pow(months) - 1) } else payable / months
            val start = today(); val end = plusMonths(start, months); val g = if (guarantor.selectedItemPosition == 0) null else db.searchProfiles("GUARANTOR", "").getOrNull(guarantor.selectedItemPosition - 1)?.rowId
            val id = db.addCredit(borrowers[borrower.selectedItemPosition].rowId, g, type.selectedItem.toString(), direction.selectedItem.toString(), principal, roi.text.toString().toDoubleOrNull() ?: 0.0, months, method.selectedItem.toString(), installment, interest, payable, start, end, gst.text.toString(), selectedInvoice?.toString(), true)
            db.createSchedule(id, installment, months, end); toast("Credit registered successfully"); dashboard()
        }); r.addView(button("BACK", Color.DKGRAY) { dashboard() }); show(r)
    }

    private fun history(direction: String?) {
        val r = page("Credit History", "Complete registered transaction record"); db.credits(direction).forEach { c -> action(r, "₹", c.creditId, "${c.borrowerName} • ${c.type} • ${money(c.amount)} • ${c.status}", blue) { creditDetail(c.id) } }; if (r.childCount == 2) r.addView(label("No credit records yet.")); show(r)
    }
    private fun creditDetail(id: Long) { val c = db.creditDetail(id) ?: return; val r = page("Credit ${c.creditId}", "Digital record"); r.addView(label("Borrower: ${c.borrowerName}")); r.addView(label("Type: ${c.type}")); r.addView(label("Amount: ${money(c.amount)}")); r.addView(label("ROI: ${c.roi}% • Method: ${c.method}")); r.addView(label("Payable: ${money(c.payable)}")); r.addView(label("Period: ${c.start} to ${c.end}")); r.addView(label("Status: ${c.status}")); r.addView(button("REPAYMENT SCHEDULE", green) { repayments(false) }); r.addView(button("BACK", Color.DKGRAY) { history(null) }); show(r) }
    private fun repayments(overdue: Boolean) { val r = page("Repayment Centre", if (overdue) "Overdue payments" else "Due and upcoming payments"); db.schedules(null, overdue).forEach { s -> action(r, "₹", s.creditId, "Due ${s.dueDate} • ${money(s.amount)} • ${s.status}", if (s.status == "OVERDUE") red else green) { val v = field("Payment amount", true); AlertDialog.Builder(this).setTitle("Record repayment").setView(v).setPositiveButton("SAVE") { _, _ -> db.recordPayment(s.id, s.creditDbId, v.text.toString().toDoubleOrNull() ?: 0.0); repayments(overdue) }.setNegativeButton("CANCEL", null).show() } }; if (r.childCount == 2) r.addView(label("No matching payments.")); show(r) }
    private fun documents() { val r = page("Digital Documents", "Read before giving electronic consent"); r.addView(label("UDHAARDAAR DIGITAL CREDIT ACKNOWLEDGEMENT", 19f)); r.addView(label("The parties confirm that the credit amount, repayment terms, identity information and supporting documents entered in the application should be reviewed before electronic consent. The record is intended to preserve the agreed transaction details and repayment history.")); r.addView(label("Borrower verification • credit terms • repayment schedule • guarantor information • uploaded invoice (where applicable) • consent and OTP verification", 14f, navy)); r.addView(button("I HAVE READ THE DOCUMENT", green) { toast("Document acknowledged. Final consent still requires OTP.") }); r.addView(button("BACK", Color.DKGRAY) { dashboard() }); show(r) }
    private fun ownerProfile() { val u = db.userData() ?: return; val r = page("Lender Profile", "Identity and optional bank/NACH details"); r.addView(label("Unique ID: ${u.id}")); r.addView(label("Name: ${u.name}", 18f)); r.addView(label("Mobile: ${u.mobile}")); r.addView(label("Address: ${u.address}")); r.addView(label("Email: ${u.email}")); r.addView(button("BACK", Color.DKGRAY) { dashboard() }); show(r) }
}
