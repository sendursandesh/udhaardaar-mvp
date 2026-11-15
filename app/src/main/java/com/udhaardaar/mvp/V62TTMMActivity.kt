package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * TTMM stores registered counterparty IDs, never free-text member names.
 * Consent uses the app's explicitly accepted local demo-OTP workflow during development.
 */
class V62TTMMActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d get() = resources.displayMetrics.density
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding((16*d).toInt(), (8*d).toInt(), (16*d).toInt(), (28*d).toInt()); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private val owner get() = V62Integration.currentUserId(this)
    private val members = mutableListOf<String>() // counterparty IDs
    private var groupId = ""

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); loadGroup(); render() }
    override fun onResume() { super.onResume(); if (!isFinishing) { loadGroup(); render() } }

    private fun profile(id: String): JSONObject? = s.find(V62Store.COUNTERPARTIES, id)?.takeIf { it.optString("createdBy") == owner }
    private fun label(id: String): String {
        val p = profile(id) ?: return "Unverified profile"
        return "${p.optString("name")} • ${p.optString("mobile")}"
    }
    private fun loadGroup() {
        val g = s.all(V62Store.TTMM_GROUPS).filter { it.optString("ownerUserId", "") == owner }.lastOrNull()
        if (g != null) {
            groupId = g.optString("id")
            members.clear()
            g.optString("members").split("|").filter { it.isNotBlank() && profile(it) != null }.forEach { members.add(it) }
        }
    }
    private fun add(v: android.view.View, gap: Int = 7) = ArthSaathiV62Design.add(root, v, gap)

    private fun render() {
        root.removeAllViews()
        add(ArthSaathiV62Design.title(this, "Together • Share & Settle", "TTMM • Group money made simple"), 2)
        add(ArthSaathiV62Design.text(this, "Create a group with registered profiles. Record a shared expense, allocate each member's share, and track later partial/full contributions and dues. Every expense, contribution and settlement requires demo-OTP consent from the involved group members during development.", 10f, ArthSaathiV62Design.MUTED), 8)
        add(ArthSaathiV62Design.section(this, "REGISTERED GROUP MEMBERS"), 10)
        add(ArthSaathiV62Design.button(this, "ADD REGISTERED MEMBER", ArthSaathiV62Design.TEAL) { chooseMember() }, 4)
        members.forEachIndexed { i, id -> add(ArthSaathiV62Design.text(this, "${i+1}. ${label(id)}", 12f, ArthSaathiV62Design.NAVY, true), 2) }
        if (members.size < 2) add(ArthSaathiV62Design.text(this, "Add at least two registered profiles to enable shared expense calculation.", 10f, ArthSaathiV62Design.MUTED), 6)
        add(ArthSaathiV62Design.section(this, "SHARED EXPENSE"), 10)
        val desc = ArthSaathiV62Design.input(this, "Expense description")
        val amt = ArthSaathiV62Design.input(this, "Amount ₹")
        val payer = Spinner(this).apply { adapter = ArrayAdapter(this@V62TTMMActivity, android.R.layout.simple_spinner_dropdown_item, members.map { label(it) }) }
        val split = Spinner(this).apply { adapter = ArrayAdapter(this@V62TTMMActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Equal split", "Custom amounts", "Percentage split", "Shares / units")) }
        val allocation = ArthSaathiV62Design.input(this, "Custom / % / shares values in member order, comma separated")
        listOf(desc, amt, payer, split, allocation).forEach { add(it, 4) }
        add(ArthSaathiV62Design.button(this, "RECORD EXPENSE & CALCULATE BALANCES", ArthSaathiV62Design.BLUE) {
            val payerId = members.getOrNull(payer.selectedItemPosition).orEmpty()
            record(desc.text.toString(), amt.text.toString(), payerId, split.selectedItem.toString(), allocation.text.toString())
        }, 8)
        add(ArthSaathiV62Design.section(this, "CURRENT BALANCES"), 12)
        balances().forEach { (id, value) -> add(ArthSaathiV62Design.text(this, "${label(id)}: ${if (value >= 0) "gets back" else "owes"} ₹${"%.2f".format(Locale.US, kotlin.math.abs(value))}", 11f, ArthSaathiV62Design.NAVY, true), 3) }
        add(ArthSaathiV62Design.button(this, "RECORD MEMBER CONTRIBUTION", ArthSaathiV62Design.GREEN) { contributionDialog() }, 8)
        add(ArthSaathiV62Design.button(this, "SETTLE A GROUP DUE", ArthSaathiV62Design.TEAL) { settlementDialog() }, 5)
        add(ArthSaathiV62Design.section(this, "OPEN CONTRIBUTIONS / DUES"), 12)
        openDues().forEach { due -> add(ArthSaathiV62Design.text(this, "${label(due.optString("member"))} owes ${label(due.optString("to"))} ₹${"%.2f".format(Locale.US, due.optDouble("due"))}\n${due.optString("description")} • Expense ₹${"%.2f".format(Locale.US, due.optDouble("expenseAmount"))}", 10f, ArthSaathiV62Design.NAVY), 4) }
        if (openDues().isEmpty()) add(ArthSaathiV62Design.text(this, "No open member contributions for this group.", 10f, ArthSaathiV62Design.MUTED), 4)
        add(ArthSaathiV62Design.section(this, "RECENT EXPENSES"), 12)
        s.all(V62Store.TTMM_EXPENSES).filter { it.optString("ownerUserId", "") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) }.takeLast(15).reversed().forEach {
            add(ArthSaathiV62Design.text(this, "${it.optString("description")} • ₹${"%.2f".format(Locale.US, it.optDouble("amount"))}\nPayer: ${label(it.optString("payerId"))} • ${it.optString("splitMethod")}\n${it.optString("balanceSummary")}", 10f, ArthSaathiV62Design.NAVY), 4)
        }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun saveGroup() {
        if (groupId.isBlank()) groupId = V62Store.id("GRP")
        s.replace(V62Store.TTMM_GROUPS, JSONObject().apply { put("id", groupId); put("ownerUserId", owner); put("members", members.joinToString("|")); put("status", "ACTIVE"); put("updatedAt", System.currentTimeMillis()) })
    }

    private fun chooseMember() {
        if (groupId.isBlank()) groupId = V62Store.id("GRP")
        val query = ArthSaathiV62Design.input(this, "Search registered name / mobile")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(query); addView(list) }
        val dlg = AlertDialog.Builder(this).setTitle("Select registered profile").setView(wrap).setNegativeButton("CANCEL", null).create()
        fun refresh() {
            list.removeAllViews()
            val rows = V62Integration.findCounterparties(this, query.text.toString()).filter { it.optString("id") !in members }.take(12)
            if (rows.isEmpty()) list.addView(ArthSaathiV62Design.text(this, "No matching registered profile.", 11f, ArthSaathiV62Design.MUTED))
            rows.forEach { p ->
                list.addView(ArthSaathiV62Design.button(this, "${p.optString("name")} • ${p.optString("mobile")}", ArthSaathiV62Design.NAVY) {
                    members.add(p.optString("id")); saveGroup(); dlg.dismiss(); render()
                })
            }
            list.addView(ArthSaathiV62Design.button(this, "CREATE NEW PROFILE FOR THIS GROUP", ArthSaathiV62Design.TEAL) {
                dlg.dismiss(); createMemberProfile()
            })
        }
        query.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refresh() }
            override fun afterTextChanged(e: android.text.Editable?) {}
        })
        dlg.show(); refresh()
    }

    private fun createMemberProfile() {
        val name = ArthSaathiV62Design.input(this, "Member / business name *")
        val mobile = ArthSaathiV62Design.input(this, "Indian mobile number *")
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(name); addView(mobile) }
        AlertDialog.Builder(this).setTitle("Register group member").setMessage("This creates a counterparty profile linked to this group. The member must have a valid mobile number.").setView(box).setNegativeButton("CANCEL", null).setPositiveButton("CREATE") { _, _ ->
            val cp = V62Integration.createCounterparty(this, name.text.toString(), mobile.text.toString(), transactionContext = groupId)
            if (cp == null) Toast.makeText(this, "Enter a name and valid 10-digit Indian mobile number.", Toast.LENGTH_LONG).show()
            else if (cp.optString("id") in members) Toast.makeText(this, "This profile is already in the group.", Toast.LENGTH_SHORT).show()
            else { members.add(cp.optString("id")); saveGroup(); render() }
        }.show()
    }

    /** Each involved registered member verifies a separate local demo OTP before the ledger mutation. */
    private fun requestConsent(subjectIds: List<String>, eventType: String, index: Int = 0, onComplete: () -> Unit) {
        val ids = subjectIds.distinct()
        if (index >= ids.size) { onComplete(); return }
        val id = ids[index]
        val cp = profile(id)
        if (cp == null || cp.optString("mobile").matches(Regex("[6-9][0-9]{9}")).not()) {
            Toast.makeText(this, "A registered member with a valid mobile number is required.", Toast.LENGTH_LONG).show(); return
        }
        val otp = (100000..999999).random().toString()
        val entry = ArthSaathiV62Design.input(this, "Enter 6-digit consent OTP")
        val dlg = AlertDialog.Builder(this).setTitle("Member consent ${index+1} of ${ids.size}")
            .setMessage("Consent for ${cp.optString("name")} (${cp.optString("mobile")})\nAction: $eventType\n\nDemo OTP: $otp")
            .setView(entry).setNegativeButton("CANCEL", null).setPositiveButton("VERIFY", null).create()
        dlg.setOnShowListener {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (entry.text.toString() != otp) { entry.error = "Incorrect OTP"; return@setOnClickListener }
                dlg.dismiss()
                V62Integration.recordConsent(this, id, groupId, eventType, true)
                requestConsent(ids, eventType, index + 1, onComplete)
            }
        }
        dlg.show()
    }

    private fun record(desc: String, amount: String, payerId: String, method: String, raw: String) {
        val a = amount.replace(",", "").toDoubleOrNull() ?: 0.0
        if (desc.isBlank() || a <= 0 || members.size < 2 || payerId !in members || members.any { profile(it) == null }) {
            Toast.makeText(this, "Enter description, valid amount, at least 2 registered members and a registered payer.", Toast.LENGTH_LONG).show(); return
        }
        val allocations: List<Double> = when (method) {
            "Equal split" -> List(members.size) { a / members.size }
            "Custom amounts" -> { val values = parseValues(raw, members.size); if (values == null || kotlin.math.abs(values.sum() - a) > 0.01) { Toast.makeText(this, "Custom amounts must total the expense.", Toast.LENGTH_LONG).show(); return }; values }
            "Percentage split" -> { val values = parseValues(raw, members.size); if (values == null || kotlin.math.abs(values.sum() - 100.0) > 0.01) { Toast.makeText(this, "Percentages must total 100%.", Toast.LENGTH_LONG).show(); return }; values.map { a * it / 100.0 } }
            else -> { val shares = parseValues(raw, members.size); if (shares == null || shares.sum() <= 0) { Toast.makeText(this, "Enter positive share units for every member.", Toast.LENGTH_LONG).show(); return }; val total = shares.sum(); shares.map { a * it / total } }
        }
        saveGroup()
        requestConsent(members, "TTMM_EXPENSE_CONSENT") {
            val balance = members.mapIndexed { i, id -> id to (if (id == payerId) a else 0.0) - allocations[i] }.toMap()
            val summary = balance.entries.joinToString("; ") { "${label(it.key)}: ${if (it.value >= 0) "+" else "-"}₹${"%.2f".format(Locale.US, kotlin.math.abs(it.value))}" }
            val id = V62Store.id("EXP")
            s.add(V62Store.TTMM_EXPENSES, JSONObject().apply {
                put("id", id); put("ownerUserId", owner); put("groupId", groupId); put("description", desc.trim()); put("amount", a)
                put("payer", payerId); put("payerId", payerId); put("splitMethod", method); put("allocations", JSONArray(allocations))
                put("members", members.joinToString("|")); put("balanceSummary", summary); put("status", "OPEN"); put("consentStatus", "OTP_VERIFIED"); put("createdAt", System.currentTimeMillis())
            })
            V62EventBus.publish(V62Event(V62Events.TTMM_EXPENSE_CHANGED, id))
            Toast.makeText(this, "Expense recorded after member consent.", Toast.LENGTH_SHORT).show(); render()
        }
    }

    private fun parseValues(raw: String, n: Int): List<Double>? = runCatching { raw.split(",").map { it.trim().toDouble() }.takeIf { it.size == n && it.all { x -> x >= 0 } } }.getOrNull()

    private fun expenseDue(e: JSONObject, memberId: String): Double {
        val ms = e.optString("members").split("|")
        val idx = ms.indexOf(memberId)
        if (idx < 0) return 0.0
        val allocation = e.optJSONArray("allocations")?.optDouble(idx, 0.0) ?: 0.0
        if (memberId == e.optString("payerId", e.optString("payer"))) return 0.0
        val contributed = s.all(V62Store.TTMM_CONTRIBUTIONS).filter {
            it.optString("ownerUserId") == owner && it.optString("expenseId") == e.optString("id") && it.optString("from") == memberId && it.optString("status", "RECORDED") == "RECORDED"
        }.sumOf { it.optDouble("amount", 0.0) }
        val settled = s.all(V62Store.TTMM_SETTLEMENTS).filter {
            it.optString("ownerUserId") == owner && it.optString("expenseId") == e.optString("id") && it.optString("from") == memberId && it.optString("status", "SETTLED") == "SETTLED"
        }.sumOf { it.optDouble("amount", 0.0) }
        return (allocation - contributed - settled).coerceAtLeast(0.0)
    }

    private fun openDues(): List<JSONObject> {
        val out = mutableListOf<JSONObject>()
        s.all(V62Store.TTMM_EXPENSES).filter { it.optString("ownerUserId") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) && it.optString("status", "OPEN") != "CLOSED" }.forEach { e ->
            val payer = e.optString("payerId", e.optString("payer"))
            e.optString("members").split("|").filter { it.isNotBlank() && it != payer }.forEach { id ->
                val due = expenseDue(e, id)
                if (due > 0.01) out += JSONObject().apply { put("expenseId", e.optString("id")); put("description", e.optString("description")); put("expenseAmount", e.optDouble("amount")); put("member", id); put("to", payer); put("due", due) }
            }
        }
        return out
    }

    private fun balances(): Map<String, Double> {
        val out = members.associateWith { 0.0 }.toMutableMap()
        s.all(V62Store.TTMM_EXPENSES).filter { it.optString("ownerUserId", "") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) }.forEach { e ->
            val ms = e.optString("members").split("|"); val al = e.optJSONArray("allocations") ?: return@forEach
            ms.forEachIndexed { i, id -> out[id] = (out[id] ?: 0.0) - al.optDouble(i, 0.0) }
            val payer = e.optString("payerId", e.optString("payer")); out[payer] = (out[payer] ?: 0.0) + e.optDouble("amount", 0.0)
        }
        s.all(V62Store.TTMM_CONTRIBUTIONS).filter { it.optString("ownerUserId", "") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) && it.optString("status", "RECORDED") == "RECORDED" }.forEach { x ->
            val from = x.optString("from"); val to = x.optString("to"); out[from] = (out[from] ?: 0.0) + x.optDouble("amount", 0.0); out[to] = (out[to] ?: 0.0) - x.optDouble("amount", 0.0)
        }
        s.all(V62Store.TTMM_SETTLEMENTS).filter { it.optString("ownerUserId", "") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) && it.optString("status", "SETTLED") == "SETTLED" }.forEach { x ->
            val from = x.optString("from"); val to = x.optString("to"); out[from] = (out[from] ?: 0.0) + x.optDouble("amount", 0.0); out[to] = (out[to] ?: 0.0) - x.optDouble("amount", 0.0)
        }
        return out
    }

    private fun contributionDialog() {
        val dues = openDues()
        if (dues.isEmpty()) { Toast.makeText(this, "There are no open member contributions.", Toast.LENGTH_SHORT).show(); return }
        val labels = dues.map { "${label(it.optString("member"))} → ${label(it.optString("to"))} • ${it.optString("description")} • due ₹${"%.2f".format(Locale.US, it.optDouble("due"))}" }
        val selector = Spinner(this).apply { adapter = ArrayAdapter(this@V62TTMMActivity, android.R.layout.simple_spinner_dropdown_item, labels) }
        val amount = ArthSaathiV62Design.input(this, "Contribution amount ₹")
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(selector); addView(amount) }
        AlertDialog.Builder(this).setTitle("Member contribution").setView(box).setNegativeButton("CANCEL", null).setPositiveButton("REQUEST MEMBER CONSENT") { _, _ ->
            val due = dues.getOrNull(selector.selectedItemPosition) ?: return@setPositiveButton
            val a = amount.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val remaining = due.optDouble("due")
            if (a <= 0 || a > remaining + 0.01) { Toast.makeText(this, "Contribution must be positive and cannot exceed the remaining share.", Toast.LENGTH_LONG).show(); return@setPositiveButton }
            requestConsent(listOf(due.optString("member"), due.optString("to")), "TTMM_CONTRIBUTION_CONSENT") {
                saveGroup(); val id = V62Store.id("CON")
                s.add(V62Store.TTMM_CONTRIBUTIONS, JSONObject().apply {
                    put("id", id); put("ownerUserId", owner); put("groupId", groupId); put("expenseId", due.optString("expenseId")); put("description", due.optString("description"))
                    put("from", due.optString("member")); put("to", due.optString("to")); put("amount", a); put("status", "RECORDED"); put("consentStatus", "OTP_VERIFIED"); put("createdAt", System.currentTimeMillis())
                })
                V62EventBus.publish(V62Event(V62Events.TTMM_CONTRIBUTION_CHANGED, id)); render()
            }
        }.show()
    }

    private fun settlementDialog() {
        val dues = openDues()
        if (dues.isEmpty()) { Toast.makeText(this, "There are no open group dues to settle.", Toast.LENGTH_SHORT).show(); return }
        val labels = dues.map { "${label(it.optString("member"))} → ${label(it.optString("to"))} • ${it.optString("description")} • due ₹${"%.2f".format(Locale.US, it.optDouble("due"))}" }
        val selector = Spinner(this).apply { adapter = ArrayAdapter(this@V62TTMMActivity, android.R.layout.simple_spinner_dropdown_item, labels) }
        val amount = ArthSaathiV62Design.input(this, "Settlement amount ₹")
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(selector); addView(amount) }
        AlertDialog.Builder(this).setTitle("Settle a group due").setView(box).setNegativeButton("CANCEL", null).setPositiveButton("REQUEST CONSENT & SETTLE") { _, _ ->
            val due = dues.getOrNull(selector.selectedItemPosition) ?: return@setPositiveButton
            val a = amount.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val remaining = due.optDouble("due")
            if (a <= 0 || a > remaining + 0.01) { Toast.makeText(this, "Settlement must be positive and cannot exceed the remaining share.", Toast.LENGTH_LONG).show(); return@setPositiveButton }
            requestConsent(listOf(due.optString("member"), due.optString("to")), "TTMM_SETTLEMENT_CONSENT") {
                saveGroup(); val id = V62Store.id("SET")
                s.add(V62Store.TTMM_SETTLEMENTS, JSONObject().apply {
                    put("id", id); put("ownerUserId", owner); put("groupId", groupId); put("expenseId", due.optString("expenseId"))
                    put("description", due.optString("description")); put("from", due.optString("member")); put("to", due.optString("to"))
                    put("amount", a); put("status", "SETTLED"); put("consentStatus", "OTP_VERIFIED"); put("createdAt", System.currentTimeMillis())
                })
                V62EventBus.publish(V62Event(V62Events.TTMM_SETTLEMENT_CHANGED, id)); render()
            }
        }.show()
    }
}