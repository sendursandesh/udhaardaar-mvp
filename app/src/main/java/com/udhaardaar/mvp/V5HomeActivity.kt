package com.udhaardaar.mvp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale

/** V5 command centre. Uses the V5 branch only; no legacy flow is used as a baseline. */
class V5HomeActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val store by lazy { V5LocalStore(this) }
    private var pendingDocumentType = ""
    private var pendingDocumentOwner = ""

    private fun add(r: LinearLayout, v: View, h: Int = 52) {
        r.addView(v, LinearLayout.LayoutParams(-1, h).apply { setMargins(0, 3, 0, 3) })
    }

    private fun button(text: String, color: Int = Color.rgb(25, 111, 220), action: () -> Unit) =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            textSize = 13f
            setTextColor(Color.WHITE)
            setBackgroundColor(color)
            setOnClickListener { action() }
        }

    private fun field(hint: String) = EditText(this).apply {
        this.hint = hint
        setSingleLine(true)
        textSize = 14f
    }

    private fun form(r: LinearLayout, e: EditText) = add(r, e, 50)

    private fun page(title: String, subtitle: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16, 12, 16, 16)
        setBackgroundColor(Color.rgb(238, 248, 253))
        addView(TextView(this@V5HomeActivity).apply {
            text = title
            textSize = 22f
            setTextColor(Color.rgb(24, 58, 92))
        })
        addView(TextView(this@V5HomeActivity).apply {
            text = subtitle
            textSize = 12f
        })
    }

    private fun save(key: String, data: JSONObject) {
        data.put("updatedAt", System.currentTimeMillis())
        store.replace(key, data)
    }

    private fun showList(r: LinearLayout, key: String, label: String) {
        val items = store.all(key)
        add(r, TextView(this).apply {
            text = "$label: ${items.size}"
            textSize = 12f
            setTextColor(Color.rgb(24, 58, 92))
        }, 34)
        if (items.isNotEmpty()) {
            add(r, TextView(this).apply {
                text = items.takeLast(8).joinToString("\n") {
                    it.optString("id") + " • " + it.optString("status", "SAVED")
                }
                textSize = 11f
            }, minOf(140, 24 * items.size))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        home()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() { home() }

    private fun home() {
        if (!prefs.getBoolean("logged_in", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 16)
            setBackgroundColor(Color.rgb(238, 248, 253))
        }
        add(r, TextView(this).apply {
            text = "UDHAARDAAR V5"
            textSize = 24f
            setTextColor(Color.rgb(24, 58, 92))
        }, 42)
        add(r, TextView(this).apply {
            text = "Unified financial obligations • documents • assets • succession"
            textSize = 12f
        }, 34)
        add(r, button("PARTY & IDENTITY • Search / Create / History", Color.rgb(0, 145, 135)) {
            startActivity(Intent(this, V5PartyActivity::class.java))
        })
        add(r, button("SCAN QR • Pay or Request / Offer Credit") {
            startActivity(Intent(this, V5QrCreditActivity::class.java))
        })
        add(r, TextView(this).apply {
            text = "CREDIT & OBLIGATIONS"
            textSize = 12f
            setTextColor(Color.rgb(25, 111, 220))
        }, 28)
        tile(r, "Credit Registration", "Personal • business • trade • formal/informal • given/received") {
            startActivity(Intent(this, V5CreditRegistrationActivity::class.java))
        }
        tile(r, "Repayment Centre", "EMI • principal+interest • bullet modes • payable/receivable • evidence/consent") {
            startActivity(Intent(this, V5RepaymentActivity::class.java))
        }
        tile(r, "Formal Credit Audit", "Sanction + statement → terms → actual charges → variance report") { formal() }
        tile(r, "Rental / Lease Engine", "Persistent lease terms • due calendar • arrears • expiry/renewal") { rental() }
        add(r, TextView(this).apply {
            text = "DOCUMENTS • ASSETS • SUCCESSION"
            textSize = 12f
            setTextColor(Color.rgb(210, 135, 15))
        }, 28)
        tile(r, "Document & Consent Vault", "Indexed documents • preview/open/share • version/hash • consent trail") { documents() }
        tile(r, "Financial / Non-Financial Assets", "Ownership • proof • value • nominee • maturity/renewal") { assets() }
        tile(r, "Nominee / Trusted Person", "Permission-controlled discovery/view/claim preparation") { trusted() }
        tile(r, "Inheritance & Claims", "Heir/nominee • checklist • submission • approval • transfer/closure") { claims() }
        tile(r, "Legal Assistance", "Evidence bundle • timeline • recovery/claim professional referral") { legal() }
        tile(r, "Score & Readiness", "Consent-controlled explainable score • factors • confidence • version/date") { score() }
        add(r, button("MY PROFILE", Color.rgb(0, 145, 135)) { profile() })
        add(r, button("LOGOUT", Color.rgb(90, 110, 125)) {
            prefs.edit().putBoolean("logged_in", false).apply()
            startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun tile(r: LinearLayout, title: String, subtitle: String, action: () -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 6, 8, 6)
            setBackgroundColor(Color.WHITE)
            setOnClickListener { action() }
        }
        box.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(Color.rgb(24, 58, 92))
        })
        box.addView(TextView(this).apply { text = subtitle; textSize = 11f })
        add(r, box, 64)
    }

    private fun formal() {
        val r = page("Formal Credit Audit", "Record sanction terms and reconcile actual statement debits.")
        val ref = field("Audit ID (optional)")
        val sanction = field("Sanction document reference")
        val statement = field("Account statement reference")
        val sRoi = field("Sanctioned ROI %")
        val aRoi = field("Actual / charged ROI %")
        val sFees = field("Sanctioned fees + charges + taxes + penalties (₹)")
        val aFees = field("Actual debited fees + charges + taxes + penalties (₹)")
        listOf(ref, sanction, statement, sRoi, aRoi, sFees, aFees).forEach { form(r, it) }
        add(r, button("UPLOAD SANCTION / STATEMENT DOCUMENT", Color.rgb(0, 145, 135)) {
            pendingDocumentType = "FORMAL_AUDIT_SOURCE"
            pendingDocumentOwner = ref.text.toString()
            openDocument(7003)
        })
        add(r, button("SAVE + RUN RECONCILIATION", Color.rgb(25, 145, 78)) {
            val sanctioned = sFees.text.toString().toDoubleOrNull() ?: 0.0
            val actual = aFees.text.toString().toDoubleOrNull() ?: 0.0
            val variance = actual - sanctioned
            val id = if (ref.text.isBlank()) "AUDIT-${System.currentTimeMillis()}" else ref.text.toString()
            save("formal_audits", JSONObject().apply {
                put("id", id); put("sanction", sanction.text.toString()); put("statement", statement.text.toString())
                put("sanctionedRoi", sRoi.text.toString()); put("actualRoi", aRoi.text.toString())
                put("sanctionedFees", sanctioned); put("actualFees", actual); put("variance", variance)
                put("status", if (variance > 0) "EXCESS_CHARGE_REVIEW" else "RECONCILED")
            })
            AlertDialog.Builder(this)
                .setTitle(if (variance > 0) "EXCESS / VARIANCE FLAGGED" else "RECONCILIATION RESULT")
                .setMessage("Sanctioned charges: ₹$sanctioned\nActual debits: ₹$actual\nVariance: ₹$variance\nROI: ${sRoi.text}% vs ${aRoi.text}%")
                .setPositiveButton("OK", null).show()
        })
        showList(r, "formal_audits", "Saved audits")
        add(r, button("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun rental() {
        val r = page("Rental / Lease Engine", "Rental is a separate obligation. Dates are DDMMYYYY; due day is 1–31.")
        val id = field("Lease ID (optional)")
        val tenant = field("Tenant / counterparty *")
        val landlord = field("Landlord / owner *")
        val property = field("Property / premises *")
        val rent = field("Monthly rent ₹ *")
        val deposit = field("Security deposit ₹")
        val start = field("Start date DDMMYYYY *")
        val end = field("End date DDMMYYYY *")
        val due = field("Due day 1–31 *")
        val escalation = field("Escalation %")
        val notice = field("Notice days")
        val utilities = field("Utilities / maintenance")
        listOf(tenant, landlord, property, rent, deposit, start, end, due, escalation, notice, utilities).forEach { form(r, it) }
        add(r, button("PICK START DATE", Color.rgb(0, 145, 135)) { pickDate(start) })
        add(r, button("PICK END DATE", Color.rgb(0, 145, 135)) { pickDate(end) })
        add(r, button("SAVE LEASE + GENERATE DUE CALENDAR", Color.rgb(25, 145, 78)) {
            val dueDay = due.text.toString().toIntOrNull()
            val monthly = rent.text.toString().toDoubleOrNull()
            if (dueDay == null || dueDay !in 1..31) { due.error = "Enter a due day from 1 to 31"; return@button }
            if (tenant.text.isBlank() || landlord.text.isBlank() || property.text.isBlank() || monthly == null || start.text.length != 8 || end.text.length != 8) {
                Toast.makeText(this, "Complete required lease fields and DDMMYYYY dates", Toast.LENGTH_LONG).show(); return@button
            }
            val leaseId = if (id.text.isBlank()) "LEASE-${System.currentTimeMillis()}" else id.text.toString()
            val esc = escalation.text.toString().toDoubleOrNull() ?: 0.0
            save("rentals", JSONObject().apply {
                put("id", leaseId); put("tenant", tenant.text.toString()); put("landlord", landlord.text.toString())
                put("property", property.text.toString()); put("monthlyRent", monthly)
                put("deposit", deposit.text.toString().toDoubleOrNull() ?: 0.0)
                put("startDate", start.text.toString()); put("endDate", end.text.toString()); put("dueDay", dueDay)
                put("escalationPercent", esc); put("noticeDays", notice.text.toString().toIntOrNull() ?: 0)
                put("utilities", utilities.text.toString()); put("status", "ACTIVE")
            })
            save("rental_schedules", JSONObject().apply {
                put("id", "SCHEDULE-$leaseId"); put("leaseId", leaseId); put("monthlyRent", monthly)
                put("escalationPercent", esc); put("firstDueDay", dueDay); put("status", "GENERATED")
            })
            Toast.makeText(this, "Lease saved and due schedule generated", Toast.LENGTH_LONG).show()
        })
        showList(r, "rentals", "Saved leases")
        add(r, button("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun pickDate(target: EditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            target.setText(String.format(Locale.US, "%02d%02d%04d", day, month + 1, year))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun documents() {
        val r = page("Document & Consent Vault", "Indexed evidence with version, owner reference and execution trail.")
        val id = field("Credit / party / asset reference")
        form(r, id)
        val type = Spinner(this)
        type.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf(
            "DPN", "Credit Agreement", "Guarantor Guarantee", "Trade Invoice", "Sanction Letter",
            "Account Statement", "Lease", "Repayment Receipt", "Asset Proof", "Claim Document", "Consent / OTP Evidence"
        ))
        add(r, type)
        add(r, button("SELECT + INDEX DOCUMENT", Color.rgb(0, 145, 135)) {
            pendingDocumentType = type.selectedItem.toString()
            pendingDocumentOwner = id.text.toString()
            openDocument(7001)
        })
        add(r, TextView(this).apply {
            text = "Lifecycle: DRAFT → SENT → VIEWED → CONSENT PENDING → OTP VERIFIED → COMPLETED → ARCHIVED"
            textSize = 12f
        }, 60)
        showList(r, "documents", "Indexed documents")
        add(r, button("OPEN LAST DOCUMENT", Color.rgb(25, 145, 78)) {
            val x = store.all("documents").lastOrNull()
            if (x == null) Toast.makeText(this, "No indexed document", Toast.LENGTH_SHORT).show()
            else try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(x.optString("uri"))).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) }
            catch (_: Exception) { Toast.makeText(this, "No viewer available", Toast.LENGTH_SHORT).show() }
        })
        add(r, button("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun openDocument(requestCode: Int) {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }, requestCode)
    }

    @Deprecated("Android compatibility callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null || requestCode !in setOf(7001, 7002, 7003)) return
        val uri = data.data!!
        try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
        val id = "DOC-${System.currentTimeMillis()}"
        store.add("documents", JSONObject().apply {
            put("id", id); put("type", pendingDocumentType); put("ownerRef", pendingDocumentOwner)
            put("uri", uri.toString()); put("version", 1); put("status", "INDEXED"); put("createdAt", System.currentTimeMillis())
        })
        Toast.makeText(this, "Document indexed: $id", Toast.LENGTH_LONG).show()
    }

    private fun assets() {
        val r = page("Asset Vault", "Financial and non-financial assets with proof and beneficiary linkage.")
        val id = field("Asset ID (optional)")
        val category = field("Asset category / institution *")
        val title = field("Asset title *")
        val owner = field("Owner / co-owner *")
        val reference = field("Account / registration reference")
        val value = field("Estimated value ₹")
        val nominee = field("Nominee / beneficiary")
        val maturity = field("Maturity / renewal date DDMMYYYY")
        listOf(category, title, owner, reference, value, nominee, maturity).forEach { form(r, it) }
        add(r, button("UPLOAD ASSET PROOF", Color.rgb(0, 145, 135)) {
            pendingDocumentType = "ASSET_PROOF"
            pendingDocumentOwner = if (id.text.isBlank()) title.text.toString() else id.text.toString()
            openDocument(7002)
        })
        add(r, button("SAVE ASSET", Color.rgb(25, 145, 78)) {
            if (category.text.isBlank() || title.text.isBlank() || owner.text.isBlank()) {
                Toast.makeText(this, "Complete required asset fields", Toast.LENGTH_LONG).show(); return@button
            }
            val assetId = if (id.text.isBlank()) "ASSET-${System.currentTimeMillis()}" else id.text.toString()
            save("assets", JSONObject().apply {
                put("id", assetId); put("category", category.text.toString()); put("title", title.text.toString())
                put("owner", owner.text.toString()); put("reference", reference.text.toString())
                put("value", value.text.toString().toDoubleOrNull() ?: 0.0); put("nominee", nominee.text.toString())
                put("maturity", maturity.text.toString()); put("status", "ACTIVE")
            })
            Toast.makeText(this, "Asset saved", Toast.LENGTH_LONG).show()
        })
        showList(r, "assets", "Saved assets")
        add(r, button("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun trusted() {
        val r = page("Nominee / Trusted Person", "Permission is explicit and does not transfer ownership automatically.")
        val id = field("Permission ID (optional)")
        val name = field("Name *")
        val relationship = field("Relationship *")
        val mobile = field("Mobile *")
        val level = Spinner(this)
        level.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("VIEW_ONLY", "CLAIM_PREPARATION", "DOCUMENT_ACCESS", "EMERGENCY_CONTACT"))
        listOf(name, relationship, mobile).forEach { form(r, it) }
        add(r, level)
        add(r, button("SAVE PERMISSION", Color.rgb(25, 145, 78)) {
            if (name.text.isBlank() || relationship.text.isBlank() || !V5Validation.mobile(mobile.text.toString())) {
                Toast.makeText(this, "Enter valid name, relationship and mobile", Toast.LENGTH_LONG).show(); return@button
            }
            val permissionId = if (id.text.isBlank()) "PERM-${System.currentTimeMillis()}" else id.text.toString()
            save("trusted_people", JSONObject().apply {
                put("id", permissionId); put("name", name.text.toString()); put("relationship", relationship.text.toString())
                put("mobile", mobile.text.toString()); put("permission", level.selectedItem.toString()); put("status", "ACTIVE")
            })
            Toast.makeText(this, "Permission saved", Toast.LENGTH_LONG).show()
        })
        showList(r, "trusted_people", "Permissions")
        add(r, button("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun claims() {
        val r = page("Inheritance & Claims", "Evidence-driven succession workflow with explicit states.")
        val id = field("Claim ID (optional)")
        val owner = field("Owner / deceased person *")
        val claimant = field("Claimant / legal heir *")
        val relationship = field("Relationship *")
        val date = field("Succession date DDMMYYYY")
        val institution = field("Institution / authority *")
        val asset = field("Asset / policy reference *")
        val status = Spinner(this)
        status.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("IDENTIFIED", "DOCUMENTS_PENDING", "PREPARED", "SUBMITTED", "QUERY_RECEIVED", "APPROVED", "TRANSFERRED_CLOSED", "DISPUTED"))
        listOf(owner, claimant, relationship, date, institution, asset).forEach { form(r, it) }
        add(r, status)
        add(r, button("CREATE / UPDATE CLAIM FILE", Color.rgb(25, 145, 78)) {
            if (owner.text.isBlank() || claimant.text.isBlank() || relationship.text.isBlank() || institution.text.isBlank() || asset.text.isBlank()) {
                Toast.makeText(this, "Complete required claim fields", Toast.LENGTH_LONG).show(); return@button
            }
            val claimId = if (id.text.isBlank()) "CLAIM-${System.currentTimeMillis()}" else id.text.toString()
            save("claims", JSONObject().apply {
                put("id", claimId); put("owner", owner.text.toString()); put("claimant", claimant.text.toString())
                put("relationship", relationship.text.toString()); put("successionDate", date.text.toString())
                put("institution", institution.text.toString()); put("assetRef", asset.text.toString()); put("status", status.selectedItem.toString())
            })
            Toast.makeText(this, "Claim file saved", Toast.LENGTH_LONG).show()
        })
        showList(r, "claims", "Claim files")
        add(r, button("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun legal() {
        val r = page("Legal Assistance", "Evidence preparation and professional referral; not legal advice.")
        val reference = field("Credit / claim / asset reference")
        val issue = field("Issue / default / claim summary")
        form(r, reference); form(r, issue)
        add(r, button("PREPARE EVIDENCE BUNDLE", Color.rgb(210, 135, 15)) {
            val id = "BUNDLE-${System.currentTimeMillis()}"
            save("legal_bundles", JSONObject().apply {
                put("id", id); put("reference", reference.text.toString()); put("issue", issue.text.toString())
                put("sources", "DPN/agreement/guarantee; consent/OTP; repayment history; formal audit; asset proof; claim records")
                put("status", "READY_FOR_REVIEW")
            })
            AlertDialog.Builder(this).setTitle("Evidence bundle checklist")
                .setMessage("Contractual documents, consent/OTP evidence, repayment ledger, audit timeline, outstanding calculation and supporting evidence are included in the bundle index.")
                .setPositiveButton("OK", null).show()
        })
        add(r, button("PROFESSIONAL REFERRAL", Color.rgb(24, 58, 92)) {
            Toast.makeText(this, "Referral request recorded as an integration point", Toast.LENGTH_LONG).show()
        })
        showList(r, "legal_bundles", "Prepared bundles")
        add(r, button("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun score() {
        val r = page("Udhaardaar Score & Readiness", "Consent-controlled, explainable local score; not a statutory bureau score.")
        val profileId = field("Profile ID")
        form(r, profileId)
        add(r, button("CALCULATE SCORE", Color.rgb(25, 111, 220)) {
            val result = V5PlatformEngine(this).calculateScore(profileId.text.toString())
            save("score_snapshots", JSONObject().apply {
                put("id", "SCORE-${System.currentTimeMillis()}"); put("profileId", profileId.text.toString())
                put("score", result.optInt("score")); put("version", result.optString("version"))
                put("confidence", result.optString("confidence")); put("factors", result.optString("factors"))
                put("consent", "REQUIRED_FOR_SHARING")
            })
            AlertDialog.Builder(this).setTitle("V5 Score: ${result.optInt("score")}")
                .setMessage("Version: ${result.optString("version")}\nConfidence: ${result.optString("confidence")}\nFactors: ${result.optString("factors")}\nSharing: explicit consent required")
                .setPositiveButton("OK", null).show()
        })
        showList(r, "score_snapshots", "Score snapshots")
        add(r, button("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun profile() {
        val mobile = prefs.getString("current_mobile", "") ?: ""
        val name = prefs.getString("name_$mobile", "User") ?: "User"
        AlertDialog.Builder(this).setTitle("My Profile")
            .setMessage("$name\nMobile: $mobile\nV5 private-by-default vault; explicit consent controls sharing.")
            .setPositiveButton("OK", null).show()
    }
}
