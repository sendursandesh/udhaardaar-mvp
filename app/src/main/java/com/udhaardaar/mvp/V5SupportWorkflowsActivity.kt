package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import org.json.JSONObject

class V5SupportWorkflowsActivity : androidx.appcompat.app.AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val consent by lazy { V5OtpConsentService(this) }
    private fun edit(hint: String) = EditText(this).apply { this.hint = hint; setSingleLine(true); setPadding(14, 10, 14, 10) }
    private fun add(root: LinearLayout, view: View, height: Int = 56) { root.addView(view, LinearLayout.LayoutParams(-1, height).apply { setMargins(0, 5, 0, 5) }) }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 16, 20, 28) }
        add(root, TextView(this).apply { text = "UDHAARDAAR V5 • Support & Protection"; textSize = 23f }, 62)
        when (intent.getStringExtra("mode") ?: "ACCESS") {
            "LEGAL" -> legal(root)
            "NOTIFICATIONS" -> readiness(root)
            "NOMINEE" -> nominee(root)
            else -> access(root)
        }
        add(root, Button(this).apply { text = "BACK TO HOME"; setOnClickListener { finish() } }, 54)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun access(root: LinearLayout) {
        add(root, TextView(this).apply { text = "Controlled access: request → OTP consent → scoped grant → revoke"; textSize = 13f }, 55)
        val owner = edit("Owner profile ID *")
        val trusted = edit("Trusted person profile ID *")
        val scope = edit("Scope: ASSETS,LIABILITIES,CLAIMS,DOCUMENTS *")
        val expiry = edit("Expiry date YYYY-MM-DD (optional)")
        add(root, owner); add(root, trusted); add(root, scope); add(root, expiry)
        add(root, Button(this).apply {
            text = "REQUEST + CONSENT ACCESS"
            setOnClickListener {
                val ownerId = owner.text.toString().trim(); val trustedId = trusted.text.toString().trim(); val scopeText = scope.text.toString().trim().uppercase(); val expiryText = expiry.text.toString().trim()
                if (ownerId.isBlank() || trustedId.isBlank() || scopeText.isBlank()) { toast("Owner, trusted person and scope are required"); return@setOnClickListener }
                val id = "ACC-${System.currentTimeMillis()}"
                store.add("access_requests", JSONObject().apply { put("id", id); put("ownerProfileId", ownerId); put("trustedPersonProfileId", trustedId); put("scope", scopeText); put("expiry", expiryText); put("status", "CONSENT_PENDING"); put("createdAt", System.currentTimeMillis()) })
                val cid = consent.issue(id, "TRUSTED_ACCESS", trustedId)
                val code = store.find("consents", cid)?.optString("otp", "") ?: ""
                val input = edit("Enter OTP")
                val dialog = AlertDialog.Builder(this@V5SupportWorkflowsActivity).setTitle("Trusted access consent").setMessage("Demo OTP: $code").setView(input).setNegativeButton("CANCEL", null).setPositiveButton("CONFIRM", null).create()
                dialog.setOnShowListener {
                    dialog.getButton(-1).setOnClickListener {
                        if (consent.verify(cid, input.text.toString())) {
                            store.find("access_requests", id)?.let { request -> request.put("status", "ACTIVE"); request.put("consentId", cid); request.put("consentedAt", System.currentTimeMillis()); store.replace("access_requests", request) }
                            store.add("access_grants", JSONObject().apply { put("id", "GRANT-${System.currentTimeMillis()}"); put("accessRequestId", id); put("ownerProfileId", ownerId); put("trustedPersonProfileId", trustedId); put("scope", scopeText); put("expiry", expiryText); put("status", "ACTIVE"); put("createdAt", System.currentTimeMillis()) })
                            store.add("audit", JSONObject().apply { put("id", "AUD-${System.currentTimeMillis()}"); put("entityId", id); put("event", "CONTROLLED_ACCESS_GRANTED"); put("at", System.currentTimeMillis()) })
                            toast("Scoped controlled access activated"); dialog.dismiss()
                        } else input.error = "Incorrect OTP"
                    }
                }
                dialog.show()
            }
        }, 60)
        add(root, Button(this).apply {
            text = "REVOKE ACTIVE ACCESS"
            setOnClickListener {
                val grant = store.all("access_grants").lastOrNull { it.optString("status") == "ACTIVE" }
                if (grant == null) toast("No active grant") else {
                    grant.put("status", "REVOKED"); grant.put("revokedAt", System.currentTimeMillis()); store.replace("access_grants", grant)
                    store.find("access_requests", grant.optString("accessRequestId"))?.let { it.put("status", "REVOKED"); store.replace("access_requests", it) }
                    toast("Access revoked and audited")
                }
            }
        }, 60)
    }

    private fun nominee(root: LinearLayout) {
        add(root, TextView(this).apply { text = "Nominee / beneficiary: relationship, linkage and OTP consent"; textSize = 13f }, 55)
        val owner = edit("Owner profile ID *"); val beneficiary = edit("Nominee / beneficiary profile ID *"); val asset = edit("Asset / liability ID (optional)"); val relation = edit("Relationship / capacity"); val share = edit("Share % (optional)")
        add(root, owner); add(root, beneficiary); add(root, asset); add(root, relation); add(root, share)
        add(root, Button(this).apply {
            text = "SAVE NOMINEE + GET CONSENT"
            setOnClickListener {
                val ownerId = owner.text.toString().trim(); val beneficiaryId = beneficiary.text.toString().trim()
                if (ownerId.isBlank() || beneficiaryId.isBlank()) { toast("Owner and nominee are required"); return@setOnClickListener }
                val id = "NOM-${System.currentTimeMillis()}"; val cid = consent.issue(id, "NOMINEE_CONSENT", beneficiaryId); val code = store.find("consents", cid)?.optString("otp", "") ?: ""; val input = edit("Nominee OTP")
                val dialog = AlertDialog.Builder(this@V5SupportWorkflowsActivity).setTitle("Nominee consent").setMessage("Demo OTP: $code").setView(input).setNegativeButton("CANCEL", null).setPositiveButton("CONFIRM", null).create()
                dialog.setOnShowListener {
                    dialog.getButton(-1).setOnClickListener {
                        if (consent.verify(cid, input.text.toString())) {
                            store.add("nominees", JSONObject().apply { put("id", id); put("ownerProfileId", ownerId); put("nomineeProfileId", beneficiaryId); put("assetId", asset.text.toString().trim()); put("relationship", relation.text.toString().trim()); put("sharePercent", share.text.toString().trim()); put("consentId", cid); put("status", "ACTIVE"); put("createdAt", System.currentTimeMillis()) })
                            store.add("audit", JSONObject().apply { put("id", "AUD-${System.currentTimeMillis()}"); put("entityId", id); put("event", "NOMINEE_REGISTERED_WITH_CONSENT"); put("at", System.currentTimeMillis()) })
                            toast("Nominee saved with consent"); dialog.dismiss()
                        } else input.error = "Incorrect OTP"
                    }
                }
                dialog.show()
            }
        }, 60)
    }

    private fun legal(root: LinearLayout) {
        add(root, TextView(this).apply { text = "Legal support: case creation, evidence linkage and status"; textSize = 13f }, 50)
        val subject = edit("Case subject / profile ID *"); val caseType = edit("Case type"); val evidence = edit("Evidence/document IDs (comma separated)"); val notes = edit("Facts / assistance required *")
        add(root, subject); add(root, caseType); add(root, evidence); add(root, notes)
        add(root, Button(this).apply {
            text = "CREATE LEGAL CASE"
            setOnClickListener {
                val subjectId = subject.text.toString().trim(); val facts = notes.text.toString().trim()
                if (subjectId.isBlank() || facts.isBlank()) { toast("Subject and facts are required"); return@setOnClickListener }
                val id = "LEG-${System.currentTimeMillis()}"
                store.add("legal_cases", JSONObject().apply { put("id", id); put("subject", subjectId); put("caseType", caseType.text.toString().trim()); put("evidenceIds", evidence.text.toString().trim()); put("notes", facts); put("status", "OPEN"); put("createdAt", System.currentTimeMillis()); put("updatedAt", System.currentTimeMillis()) })
                store.add("audit", JSONObject().apply { put("id", "AUD-${System.currentTimeMillis()}"); put("entityId", id); put("event", "LEGAL_CASE_CREATED"); put("at", System.currentTimeMillis()) })
                toast("Legal support case created")
            }
        }, 60)
        add(root, Button(this).apply {
            text = "ADVANCE LAST CASE TO IN REVIEW"
            setOnClickListener {
                val item = store.all("legal_cases").lastOrNull()
                if (item == null) toast("No case") else { item.put("status", "IN_REVIEW"); item.put("updatedAt", System.currentTimeMillis()); store.replace("legal_cases", item); toast("Case moved to IN_REVIEW") }
            }
        }, 60)
    }

    private fun readiness(root: LinearLayout) {
        add(root, TextView(this).apply { text = "Readiness centre • actionable items from credits, assets, liabilities and claims"; textSize = 13f }, 55)
        val lines = mutableListOf<String>()
        store.all("credits").filter { it.optString("status") != "CLOSED" }.forEach { lines.add("CREDIT DUE: ${it.optString("id")} • outstanding ₹${it.optDouble("outstanding", 0.0)}") }
        store.all("assets").filter { it.optString("category") == "LIABILITY" }.forEach { lines.add("LIABILITY: ${it.optString("title")} • ₹${it.optDouble("outstandingLiability", it.optDouble("estimatedValue", 0.0))}") }
        store.all("death_claim_cases").filter { it.optString("status") != "CLOSED" }.forEach { lines.add("CLAIM FOLLOW-UP: ${it.optString("id")} • ${it.optString("status", "OPEN")}") }
        val alertText = if (lines.isEmpty()) "No actionable items currently." else lines.joinToString("\n\n")
        add(root, TextView(this).apply { text = alertText; textSize = 15f }, 220)
        add(root, Button(this).apply { text = "CREATE / REFRESH ALERT QUEUE"; setOnClickListener { store.add("notifications", JSONObject().apply { put("id", "NTF-${System.currentTimeMillis()}"); put("message", alertText); put("status", "PENDING"); put("createdAt", System.currentTimeMillis()) }); toast("Actionable readiness alert queue refreshed") } }, 60)
        add(root, Button(this).apply { text = "MARK LAST ALERT DONE"; setOnClickListener { store.all("notifications").lastOrNull()?.let { it.put("status", "DONE"); it.put("completedAt", System.currentTimeMillis()); store.replace("notifications", it); toast("Alert completed") } ?: toast("No alert") } }, 60)
    }
}
