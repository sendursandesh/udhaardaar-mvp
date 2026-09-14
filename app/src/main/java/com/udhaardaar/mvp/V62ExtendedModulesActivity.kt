package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.util.Locale
import kotlin.random.Random

/** Connected V6.2 secondary modules: QR, formal credit, funding, people, address, liability and benefits. */
class V62ExtendedModulesActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private lateinit var root: LinearLayout
    private val store by lazy { V5LocalStore(this) }
    private fun dp(v: Int) = (v * d).toInt()
    private fun input(h: String) = ArthSaathiV62Design.input(this, h)
    private fun btn(s: String, c: Int = ArthSaathiV62Design.BLUE, go: () -> Unit) = ArthSaathiV62Design.button(this, s, c, go)
    private fun add(v: android.view.View, t: Int = 8) = ArthSaathiV62Design.add(root, v, t)

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); menu() }
    private fun shell(title: String, sub: String) { root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(90)); setBackgroundColor(ArthSaathiV62Design.BG) }; setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) }); add(ArthSaathiV62Design.title(this, title, sub), 0) }

    private fun menu() {
        shell("Financial Centre", "Connected V6.2 services")
        add(ArthSaathiV62Design.section(this, "CREDIT & FUNDING"), 10)
        add(btn("FORMAL CREDIT & LOAN OFFERS") { formal() }); add(btn("FUNDING / LENDING REQUEST", ArthSaathiV62Design.TEAL) { funding() }); add(btn("CHARGECHECK", ArthSaathiV62Design.GOLD) { startActivity(Intent(this, V62ChargeCheckActivity::class.java)) }); add(btn("QR UDHAR KHATA", ArthSaathiV62Design.GREEN) { khata() })
        add(ArthSaathiV62Design.section(this, "PEOPLE, DATA & PROTECTION"), 12)
        add(btn("PROFILE • FAMILY • CONTACTS") { people() }); add(btn("ADDRESS & LOCATION", ArthSaathiV62Design.TEAL) { address() }); add(btn("LIABILITY VAULT", ArthSaathiV62Design.RED) { liability() }); add(btn("GOVERNMENT SCHEMES & BENEFITS", ArthSaathiV62Design.GREEN) { benefits() }); add(btn("REPORTS & STATEMENTS", ArthSaathiV62Design.NAVY) { reports() }); add(btn("BACK TO HOME", ArthSaathiV62Design.NAVY) { finish() })
    }

    private fun formal() {
        shell("Formal Credit", "Record offers and hand off to ChargeCheck")
        val p = input("Bank / NBFC / institution"); val n = input("Loan product"); val a = input("Principal amount ₹"); val r = input("Annual interest %"); val t = input("Tenure months"); val pf = input("Processing + other charges ₹")
        listOf(p, n, a, r, t, pf).forEach { add(it) }
        add(btn("SAVE FORMAL CREDIT OFFER", ArthSaathiV62Design.BLUE) {
            val principal = a.text.toString().replace(",", "").toDoubleOrNull(); if (p.text.isBlank() || n.text.isBlank() || principal == null || principal <= 0) { Toast.makeText(this, "Enter institution, product and valid principal.", Toast.LENGTH_LONG).show(); return@btn }
            val id = V62Store.id("FC"); store.add(V62Store.FORMAL_CREDIT, JSONObject().apply { put("id", id); put("ownerUserId", V62Integration.currentUserId(this@V62ExtendedModulesActivity)); put("provider", p.text.toString().trim()); put("product", n.text.toString().trim()); put("principal", principal); put("rate", r.text.toString()); put("tenure", t.text.toString()); put("charges", pf.text.toString()); put("status", "RECORDED"); put("createdAt", System.currentTimeMillis()) }); V62EventBus.publish(V62Event(V62Events.FORMAL_LOAN_CHANGED, id)); Toast.makeText(this, "Formal credit offer saved", Toast.LENGTH_SHORT).show()
        }); add(btn("OPEN CHARGECHECK", ArthSaathiV62Design.GOLD) { startActivity(Intent(this, V62ChargeCheckActivity::class.java)) }); add(btn("BACK", ArthSaathiV62Design.NAVY) { menu() })
    }

    private fun funding() {
        shell("Funding / Lending", "User-initiated, consented profile sharing")
        val purpose = input("Purpose of funding *"); val amount = input("Amount required ₹ *"); val tenure = input("Preferred tenure months"); val source = input("Preferred source")
        listOf(purpose, amount, tenure, source).forEach { add(it) }
        add(btn("REQUEST FUNDING — CONSENT + OTP", ArthSaathiV62Design.TEAL) {
            if (purpose.text.isBlank() || amount.text.toString().replace(",", "").toDoubleOrNull()?.let { it > 0 } != true) { Toast.makeText(this, "Purpose and a valid amount are required.", Toast.LENGTH_LONG).show(); return@btn }
            val code = (100000 + Random.nextInt(900000)).toString(); val e = input("Enter 6-digit OTP")
            AlertDialog.Builder(this).setTitle("Funding profile-sharing consent").setMessage("Demo OTP: $code\nOnly the profile data required for this request will be shared with selected providers.").setView(e).setNegativeButton("CANCEL", null).setPositiveButton("VERIFY", null).create().also { dlg -> dlg.setOnShowListener { dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if (e.text.toString() != code) { e.error = "Incorrect OTP"; return@setOnClickListener }; dlg.dismiss(); val id = V62Store.id("FUND"); store.add(V62Store.FUNDING_REQUESTS, JSONObject().apply { put("id", id); put("requesterId", V62Integration.currentUserId(this@V62ExtendedModulesActivity)); put("purpose", purpose.text.toString().trim()); put("amount", amount.text.toString().toDoubleOrNull() ?: 0.0); put("tenure", tenure.text.toString()); put("source", source.text.toString()); put("consent", "VERIFIED"); put("status", "REQUESTED"); put("createdAt", System.currentTimeMillis()) }); V62Integration.recordConsent(this, V62Integration.currentUserId(this), id, "FUNDING_PROFILE_SHARING", true); V62EventBus.publish(V62Event(V62Events.FUNDING_REQUEST_CHANGED, id)); Toast.makeText(this, "Funding request created", Toast.LENGTH_SHORT).show() } }; dlg.show() }
        }); add(btn("BACK", ArthSaathiV62Design.NAVY) { menu() })
    }

    private fun khata() {
        shell("QR Udhaar Khata", "Relationship entry point • ledger • balance")
        val party = input("Counterparty name / mobile / profile ID *"); val amount = input("Amount ₹ *"); val direction = Spinner(this).apply { adapter = ArrayAdapter(this@V62ExtendedModulesActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("CREDIT", "REPAYMENT")) }; val note = input("Invoice / items / note")
        listOf(party, amount, direction, note).forEach { add(it, 4) }
        add(btn("SCAN QR / IDENTIFY PARTY", ArthSaathiV62Design.TEAL) { startActivity(Intent(this, QrCreditScannerActivity::class.java)) }, 8)
        add(btn("RECORD KHATA ENTRY", ArthSaathiV62Design.GREEN) {
            val a = amount.text.toString().replace(",", "").toDoubleOrNull(); if (party.text.isBlank() || a == null || a <= 0) { Toast.makeText(this, "Party and valid amount are required.", Toast.LENGTH_LONG).show(); return@btn }
            val cp = V62Integration.findCounterparties(this, party.text.toString()).firstOrNull(); if (cp == null) { Toast.makeText(this, "Identify an existing counterparty first; QR never creates an unrestricted history key.", Toast.LENGTH_LONG).show(); return@btn }
            var rel = store.all(V62Store.RELATIONSHIPS).firstOrNull { it.optString("counterpartyId") == cp.optString("id") && it.optString("type") in listOf("PERSONAL_CREDIT", "TRADE_CREDIT") && it.optString("status") != "CLOSED" }
            if (rel == null) { rel = JSONObject().apply { put("id", V62Store.id("REL")); put("ownerUserId", V62Integration.currentUserId(this@V62ExtendedModulesActivity)); put("counterpartyId", cp.optString("id")); put("counterpartyMobile", cp.optString("mobile")); put("type", "PERSONAL_CREDIT"); put("principal", 0.0); put("amount", 0.0); put("outstanding", 0.0); put("status", "ACTIVE"); put("createdAt", System.currentTimeMillis()) }; store.add(V62Store.RELATIONSHIPS, rel); V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED, rel.optString("id"))) }
            val id = V62Store.id("QK"); val isCredit = direction.selectedItem.toString() == "CREDIT"; store.add(V62Store.TTMM_EXPENSES.replace("ttmm", "qrkhata"), JSONObject().apply { put("id", id); put("relationshipId", rel.optString("id")); put("counterpartyId", cp.optString("id")); put("party", cp.optString("name")); put("amount", a); put("direction", direction.selectedItem.toString()); put("note", note.text.toString().trim()); put("createdAt", System.currentTimeMillis()) })
            val old = rel.optDouble("outstanding", 0.0); rel.put("outstanding", (old + if (isCredit) a else -a).coerceAtLeast(0.0)); store.replace(V62Store.RELATIONSHIPS, rel); V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED, rel.optString("id"))); Toast.makeText(this, "QR Khata entry saved and relationship balance updated.", Toast.LENGTH_LONG).show()
        }); add(btn("BACK", ArthSaathiV62Design.NAVY) { menu() })
    }

    private fun people() {
        shell("People & Family", "Shared identity layer")
        val n = input("Full name *"); val m = input("Mobile"); val p = input("PAN"); val rel = input("Family / contact relationship"); val role = input("Role"); listOf(n,m,p,rel,role).forEach { add(it) }
        add(btn("SAVE PERSON / CONTACT") { if (n.text.isBlank()) Toast.makeText(this,"Name required",Toast.LENGTH_SHORT).show() else { val id=V62Store.id("PERSON"); store.add(V62Store.PEOPLE,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("name",n.text.toString().trim());put("mobile",m.text.toString().trim());put("pan",p.text.toString().trim().uppercase(Locale.getDefault()));put("relation",rel.text.toString().trim());put("role",role.text.toString().trim())}); V62EventBus.publish(V62Event(V62Events.PROFILE_CHANGED,id)); Toast.makeText(this,"Person saved",Toast.LENGTH_SHORT).show() } }); add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }

    private fun address() {
        shell("Address & Location", "Location-assisted capture with user confirmation")
        val pin=input("PIN code"); val line=input("Address line"); val city=input("City"); val district=input("District"); val state=input("State"); listOf(pin,line,city,district,state).forEach{add(it)}
        add(btn("SAVE CONFIRMED ADDRESS") { if(pin.text.length !in 6..6 || city.text.isBlank() || state.text.isBlank()) Toast.makeText(this,"Enter a valid 6-digit PIN, city and state.",Toast.LENGTH_LONG).show() else { val id=V62Store.id("ADDR"); store.add(V62Store.ADDRESSES,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("pin",pin.text.toString());put("line",line.text.toString());put("city",city.text.toString());put("district",district.text.toString());put("state",state.text.toString());put("source","USER_CONFIRMED")});V62EventBus.publish(V62Event(V62Events.ADDRESS_CHANGED,id));Toast.makeText(this,"Address saved",Toast.LENGTH_SHORT).show()} }); add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }

    private fun liability() {
        shell("Liability Vault", "Obligations included in financial snapshot")
        val n=input("Liability / loan name *"); val l=input("Lender"); val o=input("Outstanding ₹"); val r=input("Interest %"); val due=input("Next due date"); listOf(n,l,o,r,due).forEach{add(it)}
        add(btn("SAVE LIABILITY",ArthSaathiV62Design.RED){val out=o.text.toString().replace(",","").toDoubleOrNull();if(n.text.isBlank()||out==null||out<0)Toast.makeText(this,"Name and valid outstanding are required",Toast.LENGTH_LONG).show()else{val id=V62Store.id("LIAB");store.add(V62Store.LIABILITIES,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("name",n.text.toString().trim());put("lender",l.text.toString().trim());put("outstanding",out);put("rate",r.text.toString());put("due",due.text.toString())});V62EventBus.publish(V62Event(V62Events.LIABILITY_CHANGED,id));if(due.text.isNotBlank())V62Integration.addAlert(this,"LIABILITY_DUE","Liability due date: ${due.text}",id,"ACTION");Toast.makeText(this,"Liability saved",Toast.LENGTH_SHORT).show()}});add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }

    private fun benefits() {
        shell("Government Schemes & Benefits", "Eligibility records and reminders")
        val n=input("Scheme / benefit *"); val p=input("Provider"); val c=input("Eligibility / condition"); listOf(n,p,c).forEach{add(it)}
        add(btn("SAVE BENEFIT & CONDITIONS",ArthSaathiV62Design.GREEN){if(n.text.isBlank())Toast.makeText(this,"Scheme name required",Toast.LENGTH_SHORT).show()else{val id=V62Store.id("BEN");store.add(V62Store.BENEFITS,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("name",n.text.toString().trim());put("provider",p.text.toString().trim());put("condition",c.text.toString().trim());put("acknowledged",true)});V62EventBus.publish(V62Event(V62Events.PROFILE_CHANGED,id));Toast.makeText(this,"Benefit saved",Toast.LENGTH_SHORT).show()}});add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }

    private fun reports() {
        shell("Reports & Statements", "Connected V6.2 records")
        val counts=listOf("Relationships" to V62Store.RELATIONSHIPS,"Repayments" to V62Store.REPAYMENTS,"Assets" to V62Store.ASSETS,"Insurance" to V62Store.INSURANCE,"Rental / Lease" to V62Store.RENTALS,"TTMM expenses" to V62Store.TTMM_EXPENSES,"Liabilities" to V62Store.LIABILITIES,"ChargeCheck" to V62Store.CHARGECHECK,"Funding requests" to V62Store.FUNDING_REQUESTS,"Documents" to V62Store.DOCUMENTS)
        add(ArthSaathiV62Design.text(this,counts.joinToString("\n"){(n,k)->"$n: ${store.all(k).size}"},15f,ArthSaathiV62Design.NAVY,true),10);add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }
    override fun onBackPressed(){finish()}
}
