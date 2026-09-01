package com.udhaardaar.mvp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

/** TTMM: shared-bill memory and friend-verified micro-credit, integrated with V5 obligations. */
class V5TtmmActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val friends = mutableListOf<Triple<String, String, Double>>()
    private lateinit var list: LinearLayout
    private lateinit var payer: EditText
    private lateinit var bill: EditText
    private lateinit var billRef: EditText

    private fun add(r: LinearLayout, v: View, h: Int = 52) { r.addView(v, LinearLayout.LayoutParams(-1, h).apply { setMargins(0, 4, 0, 4) }) }
    private fun field(h: String) = EditText(this).apply { hint = h; setSingleLine(true); textSize = 14f }
    private fun btn(t: String, c: Int, a: () -> Unit) = Button(this).apply { text = t; isAllCaps = false; setTextColor(Color.WHITE); setBackgroundColor(c); setOnClickListener { a() } }
    override fun onCreate(b: Bundle?) { super.onCreate(b); show() }

    private fun show() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28, 24, 28, 24) }
        add(root, TextView(this).apply { text = "TTMM"; textSize = 28f; setTextColor(Color.rgb(24,58,92)) }, 58)
        add(root, TextView(this).apply { text = "Take The Money, Mate — one friend pays, everyone verifies their share. Confirmed shares become V5 receivable obligations and are remembered for recovery."; textSize = 13f }, 82)
        payer = field("Who paid the bill? *"); bill = field("Total bill ₹ *"); billRef = field("Restaurant / bill reference (optional)")
        add(root, payer); add(root, bill); add(root, billRef)
        add(root, btn("ADD FRIEND + SHARE", Color.rgb(0,145,135)) { addFriend() })
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; add(root, list, 420)
        add(root, btn("CREATE TTMM + REQUEST VERIFICATION", Color.rgb(25,145,78)) { createBill() })
        add(root, btn("FRIEND VERIFICATION / CONFIRM DEBT", Color.rgb(25,111,220)) { verificationCentre() })
        add(root, btn("TTMM MEMORY / RECOVERY", Color.rgb(210,135,15)) { memory() })
        add(root, btn("HOME", Color.rgb(24,58,92)) { finish() })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun addFriend() {
        val n = field("Friend name *"); val id = field("Friend mobile / identifier (optional)"); val s = field("Their share ₹ *")
        val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; add(row, n); add(row, id, 48); add(row, s, 48); add(list, row, 150)
        add(list, btn("ADD THIS SHARE", Color.rgb(100,100,100)) {
            val x = s.text.toString().toDoubleOrNull()
            if (n.text.isBlank() || x == null || x <= 0) { s.error = "Enter a valid share"; return@btn }
            friends.add(Triple(n.text.toString().trim(), id.text.toString().trim(), x)); n.isEnabled=false; id.isEnabled=false; s.isEnabled=false
            Toast.makeText(this, "Share added for ${n.text}", Toast.LENGTH_SHORT).show()
        })
    }

    private fun createBill() {
        val total = bill.text.toString().toDoubleOrNull()
        if (payer.text.isBlank() || total == null || total <= 0) { bill.error = "Enter a valid total bill"; return }
        if (friends.isEmpty()) { Toast.makeText(this, "Add at least one friend share", Toast.LENGTH_LONG).show(); return }
        val sum = friends.sumOf { it.third }
        if (sum > total + 0.01) { Toast.makeText(this, "Friend shares exceed the bill", Toast.LENGTH_LONG).show(); return }
        val id = "TTMM-${System.currentTimeMillis()}"; val now = System.currentTimeMillis(); val arr = JSONArray()
        friends.forEach { (name, identifier, share) -> arr.put(JSONObject().apply { put("friend",name); put("identifier",identifier); put("share",share); put("verification","PENDING"); put("status","PROPOSED") }) }
        store.add("ttmm_bills", JSONObject().apply { put("id",id); put("payer",payer.text.toString().trim()); put("total",total); put("billRef",billRef.text.toString().trim()); put("friends",arr.toString()); put("createdAt",now); put("status","VERIFICATION_PENDING"); put("audit","TTMM_CREATED:$now") })
        friends.forEachIndexed { index, (name, identifier, share) ->
            store.add("ttmm_credits", JSONObject().apply { put("id","TTMMC-$now-$index"); put("ttmmId",id); put("payer",payer.text.toString().trim()); put("counterparty",name); put("identifier",identifier); put("amount",share); put("direction","RECEIVABLE"); put("type","TTMM_FRIEND_BILL"); put("verification","PENDING"); put("status","PROPOSED"); put("recoveryMemory","TTMM:$id"); put("createdAt",now); put("audit","TTMM_CREDIT_PROPOSED:$now") })
        }
        AlertDialog.Builder(this).setTitle("TTMM created").setMessage("Each friend's share is only proposed. The friend must explicitly confirm it before it becomes a confirmed V5 debt and appears in the central obligation record.").setPositiveButton("VERIFY NOW") { _, _ -> verificationCentre() }.setNegativeButton("OK",null).show()
    }

    private fun verificationCentre() {
        val pending = store.all("ttmm_credits").filter { it.optString("verification") == "PENDING" }
        if (pending.isEmpty()) { AlertDialog.Builder(this).setTitle("Friend Verification").setMessage("No pending friend confirmations.").setPositiveButton("OK",null).show(); return }
        pending.forEach { credit ->
            AlertDialog.Builder(this).setTitle("Confirm debt — ${credit.optString("counterparty")}").setMessage("TTMM share: ₹${credit.optDouble("amount")}\n\nConfirm only if you acknowledge this amount as payable to ${credit.optString("payer")}. Rejected shares never become V5 obligations.").setPositiveButton("CONFIRM DEBT") { _, _ -> verify(credit,true) }.setNegativeButton("REJECT") { _, _ -> verify(credit,false) }.setNeutralButton("LATER",null).show()
        }
    }

    private fun verify(credit: JSONObject, confirmed: Boolean) {
        val now = System.currentTimeMillis(); val status = if (confirmed) "CONFIRMED" else "REJECTED"; val verification = if (confirmed) "VERIFIED" else "REJECTED"
        val updated = JSONObject(credit.toString()).apply { put("verification",verification); put("status",status); put("verifiedAt",now); put("audit","TTMM_DEBT_${if(confirmed)"CONFIRMED" else "REJECTED"}:$now") }
        store.replace("ttmm_credits", updated)
        store.add("ttmm_verification_audit", JSONObject().apply { put("creditId",credit.optString("id")); put("ttmmId",credit.optString("ttmmId")); put("action",if(confirmed)"DEBT_CONFIRMED" else "DEBT_REJECTED"); put("actor",credit.optString("counterparty")); put("timestamp",now) })
        if (confirmed) {
            val centralId = "TTMM-${credit.optString("id")}"; val central = JSONObject().apply {
                put("id",centralId); put("source","TTMM"); put("sourceId",credit.optString("id")); put("type","TTMM_FRIEND_BILL"); put("direction","RECEIVABLE"); put("payer",credit.optString("payer")); put("borrower",credit.optString("counterparty")); put("counterparty",credit.optString("counterparty")); put("amount",credit.optDouble("amount")); put("principal",credit.optDouble("amount")); put("interest",0.0); put("roi",0.0); put("repaymentMode","BULLET_PRINCIPAL_ONLY"); put("status","ACTIVE"); put("consent","CONFIRMED_BY_FRIEND"); put("consentAt",now); put("verification","VERIFIED"); put("recoveryMemory",credit.optString("recoveryMemory")); put("createdAt",credit.optLong("createdAt")); put("registeredAt",now); put("audit","TTMM_CENTRAL_CREDIT_CREATED:$now")
            }; store.replace("credits",central); store.add("ttmm_credits_confirmed",updated)
        }
        updateBillStatus(credit.optString("ttmmId"))
        Toast.makeText(this,if(confirmed)"Debt confirmed, added to V5 obligations and remembered" else "Share rejected and excluded from V5 obligations",Toast.LENGTH_LONG).show()
        verificationCentre()
    }

    private fun updateBillStatus(ttmmId:String) {
        val shares=store.all("ttmm_credits").filter{it.optString("ttmmId")==ttmmId}; if(shares.isEmpty())return
        val status=when { shares.any{it.optString("verification")=="PENDING"}->"VERIFICATION_PENDING"; shares.all{it.optString("verification")=="REJECTED"}->"REJECTED"; else->"PARTIALLY_OR_FULLY_CONFIRMED" }
        store.find("ttmm_bills",ttmmId)?.let{it.put("status",status);it.put("updatedAt",System.currentTimeMillis());store.replace("ttmm_bills",it)}
    }

    private fun memory() {
        val credits=store.all("ttmm_credits_confirmed")
        if(credits.isEmpty()){AlertDialog.Builder(this).setTitle("TTMM Memory / Recovery").setMessage("No confirmed TTMM debts yet.").setPositiveButton("OK",null).show();return}
        val lines=credits.takeLast(30).joinToString("\n\n"){ "${it.optString("counterparty")} • ₹${it.optDouble("amount")}\nCONFIRMED • ${it.optString("recoveryMemory")}\nConfirmed: ${it.optLong("verifiedAt")}" }
        AlertDialog.Builder(this).setTitle("TTMM Memory / Recovery").setMessage(lines).setPositiveButton("OK",null).show()
    }
}
