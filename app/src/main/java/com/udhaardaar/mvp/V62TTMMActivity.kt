package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class V62TTMMActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d get() = resources.displayMetrics.density
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding((16*d).toInt(), (8*d).toInt(), (16*d).toInt(), (28*d).toInt()); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private val owner get() = V62Integration.currentUserId(this)
    private val members = mutableListOf<String>()
    private var groupId = ""

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); loadGroup(); render() }
    override fun onResume() { super.onResume(); if (!isFinishing) { loadGroup(); render() } }
    private fun loadGroup() { val g = s.all(V62Store.TTMM_GROUPS).filter { it.optString("ownerUserId", "") == owner }.lastOrNull(); if (g != null) { groupId = g.optString("id"); members.clear(); g.optString("members").split("|").filter { it.isNotBlank() }.forEach { members.add(it) } } }
    private fun add(v: android.view.View, gap: Int = 7) = ArthSaathiV62Design.add(root, v, gap)

    private fun render() {
        root.removeAllViews()
        add(ArthSaathiV62Design.title(this, "Together • Share & Settle", "TTMM • Group money made simple"), 2)
        add(ArthSaathiV62Design.text(this, "Record a group expense even when one person pays ₹5,000 first. Track each member's share, later partial/full contributions, remaining dues and final settlement. Every entry stays linked to the group ledger and MIS.", 10f, ArthSaathiV62Design.MUTED), 8)
        add(ArthSaathiV62Design.section(this, "GROUP MEMBERS"), 10)
        val name = ArthSaathiV62Design.input(this, "Member name / mobile")
        add(name, 4)
        add(ArthSaathiV62Design.button(this, "ADD MEMBER", ArthSaathiV62Design.TEAL) { if (name.text.isNotBlank()) { members.add(name.text.toString().trim()); saveGroup(); name.setText(""); render() } }, 4)
        members.forEachIndexed { i, m -> add(ArthSaathiV62Design.text(this, "${i+1}. $m", 12f, ArthSaathiV62Design.NAVY, true), 2) }
        if (members.size < 2) add(ArthSaathiV62Design.text(this, "Add at least two members to enable shared expense calculation.", 10f, ArthSaathiV62Design.MUTED), 6)
        add(ArthSaathiV62Design.section(this, "SHARED EXPENSE"), 10)
        val desc = ArthSaathiV62Design.input(this, "Expense description"); val amt = ArthSaathiV62Design.input(this, "Amount ₹"); val payer = ArthSaathiV62Design.input(this, "Paid by — exact member name")
        val split = Spinner(this).apply { adapter = ArrayAdapter(this@V62TTMMActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Equal split", "Custom amounts", "Percentage split", "Shares / units")) }
        val allocation = ArthSaathiV62Design.input(this, "Custom / % / shares values in member order, comma separated")
        listOf(desc, amt, payer, split, allocation).forEach { add(it, 4) }
        add(ArthSaathiV62Design.button(this, "RECORD EXPENSE & CALCULATE BALANCES", ArthSaathiV62Design.BLUE) { record(desc.text.toString(), amt.text.toString(), payer.text.toString(), split.selectedItem.toString(), allocation.text.toString()) }, 8)
        add(ArthSaathiV62Design.section(this, "CURRENT BALANCES"), 12)
        balances().forEach { (m, v) -> add(ArthSaathiV62Design.text(this, "$m: ${if (v >= 0) "gets back" else "owes"} ₹${"%.2f".format(Locale.US, kotlin.math.abs(v))}", 11f, ArthSaathiV62Design.NAVY, true), 3) }
        add(ArthSaathiV62Design.button(this, "RECORD MEMBER CONTRIBUTION", ArthSaathiV62Design.GREEN) { contributionDialog() }, 8)
add(ArthSaathiV62Design.button(this, "SETTLE A GROUP DUE", ArthSaathiV62Design.TEAL) { settlementDialog() }, 5)
add(ArthSaathiV62Design.section(this, "OPEN CONTRIBUTIONS / DUES"), 12)
openDues().forEach { d -> add(ArthSaathiV62Design.text(this, "${d.optString("member")} owes ${d.optString("to")} ₹${"%.2f".format(Locale.US, d.optDouble("due"))}\n${d.optString("description")} • Expense ₹${"%.2f".format(Locale.US, d.optDouble("expenseAmount"))}", 10f, ArthSaathiV62Design.NAVY), 4) }
if (openDues().isEmpty()) add(ArthSaathiV62Design.text(this, "No open member contributions for this group.", 10f, ArthSaathiV62Design.MUTED), 4)
        add(ArthSaathiV62Design.section(this, "RECENT EXPENSES"), 12)
        s.all(V62Store.TTMM_EXPENSES).filter { it.optString("ownerUserId", "") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) }.takeLast(15).reversed().forEach { add(ArthSaathiV62Design.text(this, "${it.optString("description")} • ₹${"%.2f".format(Locale.US, it.optDouble("amount"))}\nPayer: ${it.optString("payer")} • ${it.optString("splitMethod")}\n${it.optString("balanceSummary")}", 10f, ArthSaathiV62Design.NAVY), 4) }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun saveGroup() {
        if (groupId.isBlank()) groupId = V62Store.id("GRP")
        s.replace(V62Store.TTMM_GROUPS, JSONObject().apply { put("id", groupId); put("ownerUserId", owner); put("members", members.joinToString("|")); put("status", "ACTIVE"); put("updatedAt", System.currentTimeMillis()) })
    }

    private fun record(desc: String, amount: String, payer: String, method: String, raw: String) {
        val a = amount.replace(",", "").toDoubleOrNull() ?: 0.0
        if (desc.isBlank() || a <= 0 || members.size < 2 || payer.trim() !in members) { Toast.makeText(this, "Enter description, valid amount, at least 2 members and an exact payer name.", Toast.LENGTH_LONG).show(); return }
        saveGroup()
        val allocations: List<Double> = when (method) {
            "Equal split" -> List(members.size) { a / members.size }
            "Custom amounts" -> { val values = parseValues(raw, members.size); if (values == null || kotlin.math.abs(values.sum() - a) > 0.01) { Toast.makeText(this, "Custom amounts must total the expense.", Toast.LENGTH_LONG).show(); return }; values }
            "Percentage split" -> { val values = parseValues(raw, members.size); if (values == null || kotlin.math.abs(values.sum() - 100.0) > 0.01) { Toast.makeText(this, "Percentages must total 100%.", Toast.LENGTH_LONG).show(); return }; values.map { a * it / 100.0 } }
            else -> { val shares = parseValues(raw, members.size); if (shares == null || shares.sum() <= 0) { Toast.makeText(this, "Enter positive share units for every member.", Toast.LENGTH_LONG).show(); return }; val total = shares.sum(); shares.map { a * it / total } }
        }
        val balance = members.mapIndexed { i, m -> m to (if (m == payer.trim()) a else 0.0) - allocations[i] }.toMap()
        val summary = balance.entries.joinToString("; ") { "${it.key}: ${if (it.value >= 0) "+" else "-"}₹${"%.2f".format(Locale.US, kotlin.math.abs(it.value))}" }
        val id = V62Store.id("EXP")
        s.add(V62Store.TTMM_EXPENSES, JSONObject().apply { put("id", id); put("ownerUserId", owner); put("groupId", groupId); put("description", desc.trim()); put("amount", a); put("payer", payer.trim()); put("splitMethod", method); put("allocations", JSONArray(allocations)); put("members", members.joinToString("|")); put("balanceSummary", summary); put("status", "OPEN"); put("createdAt", System.currentTimeMillis()) })
        V62EventBus.publish(V62Event(V62Events.TTMM_EXPENSE_CHANGED, id)); Toast.makeText(this, "Expense recorded and balances calculated.", Toast.LENGTH_SHORT).show(); render()
    }

    private fun parseValues(raw: String, n: Int): List<Double>? = runCatching { raw.split(",").map { it.trim().toDouble() }.takeIf { it.size == n && it.all { x -> x >= 0 } } }.getOrNull()

    private fun expenseDue(e: JSONObject, member: String): Double {
        val ms = e.optString("members").split("|")
        val idx = ms.indexOf(member)
        if (idx < 0) return 0.0
        val allocation = e.optJSONArray("allocations")?.optDouble(idx, 0.0) ?: 0.0
        if (member == e.optString("payer")) return 0.0
        val contributed = s.all(V62Store.TTMM_CONTRIBUTIONS).filter {
            it.optString("ownerUserId") == owner && it.optString("expenseId") == e.optString("id") &&
                it.optString("from") == member && it.optString("status", "RECORDED") == "RECORDED"
        }.sumOf { it.optDouble("amount", 0.0) }
        val settled = s.all(V62Store.TTMM_SETTLEMENTS).filter {
            it.optString("ownerUserId") == owner && it.optString("expenseId") == e.optString("id") &&
                it.optString("from") == member && it.optString("status", "SETTLED") == "SETTLED"
        }.sumOf { it.optDouble("amount", 0.0) }
        return (allocation - contributed - settled).coerceAtLeast(0.0)
    }

    private fun openDues(): List<JSONObject> {
        val out = mutableListOf<JSONObject>()
        s.all(V62Store.TTMM_EXPENSES).filter { it.optString("ownerUserId") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) && it.optString("status", "OPEN") != "CLOSED" }.forEach { e ->
            val payer = e.optString("payer")
            e.optString("members").split("|").filter { it.isNotBlank() && it != payer }.forEach { m ->
                val due = expenseDue(e, m)
                if (due > 0.01) out += JSONObject().apply { put("expenseId", e.optString("id")); put("description", e.optString("description")); put("expenseAmount", e.optDouble("amount")); put("member", m); put("to", payer); put("due", due) }
            }
        }
        return out
    }

    private fun balances(): Map<String, Double> {
        val out = members.associateWith { 0.0 }.toMutableMap()
        s.all(V62Store.TTMM_EXPENSES).filter { it.optString("ownerUserId", "") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) }.forEach { e ->
            val ms = e.optString("members").split("|"); val al = e.optJSONArray("allocations") ?: return@forEach
            ms.forEachIndexed { i, m -> out[m] = (out[m] ?: 0.0) - al.optDouble(i, 0.0) }
            val payer = e.optString("payer"); out[payer] = (out[payer] ?: 0.0) + e.optDouble("amount", 0.0)
        }
        s.all(V62Store.TTMM_CONTRIBUTIONS).filter { it.optString("ownerUserId", "") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) && it.optString("status", "RECORDED") == "RECORDED" }.forEach { x -> out[x.optString("from")] = (out[x.optString("from")] ?: 0.0) + x.optDouble("amount", 0.0); out[x.optString("to")] = (out[x.optString("to")] ?: 0.0) - x.optDouble("amount", 0.0) }
        s.all(V62Store.TTMM_SETTLEMENTS).filter { it.optString("ownerUserId", "") == owner && (groupId.isBlank() || it.optString("groupId") == groupId) }.forEach { x -> out[x.optString("from")] = (out[x.optString("from")] ?: 0.0) + x.optDouble("amount", 0.0); out[x.optString("to")] = (out[x.optString("to")] ?: 0.0) - x.optDouble("amount", 0.0) }
        return out
    }

    private fun contributionDialog() {
        val dues = openDues()
        if (dues.isEmpty()) { Toast.makeText(this, "There are no open member contributions.", Toast.LENGTH_SHORT).show(); return }
        val labels = dues.map { "${it.optString("member")} → ${it.optString("to")} • ${it.optString("description")} • due ₹${"%.2f".format(Locale.US, it.optDouble("due"))}" }
        val selector = Spinner(this).apply { adapter = ArrayAdapter(this@V62TTMMActivity, android.R.layout.simple_spinner_dropdown_item, labels) }
        val amount = ArthSaathiV62Design.input(this, "Contribution amount ₹")
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(selector); addView(amount) }
        AlertDialog.Builder(this).setTitle("Member contribution").setView(box).setNegativeButton("CANCEL", null).setPositiveButton("RECORD") { _, _ ->
            val d = dues.getOrNull(selector.selectedItemPosition) ?: return@setPositiveButton
            val a = amount.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val due = d.optDouble("due")
            if (a <= 0 || a > due + 0.01) { Toast.makeText(this, "Contribution must be positive and cannot exceed the remaining share.", Toast.LENGTH_LONG).show(); return@setPositiveButton }
            saveGroup(); val id = V62Store.id("CON")
            s.add(V62Store.TTMM_CONTRIBUTIONS, JSONObject().apply { put("id", id); put("ownerUserId", owner); put("groupId", groupId); put("expenseId", d.optString("expenseId")); put("description", d.optString("description")); put("from", d.optString("member")); put("to", d.optString("to")); put("amount", a); put("status", "RECORDED"); put("createdAt", System.currentTimeMillis()) })
            V62EventBus.publish(V62Event(V62Events.TTMM_CONTRIBUTION_CHANGED, id)); render()
        }.show()
    }

    private fun settlementDialog() {
        val dues = openDues()
        if (dues.isEmpty()) { Toast.makeText(this, "There are no open group dues to settle.", Toast.LENGTH_SHORT).show(); return }
        val labels = dues.map { "${it.optString("member")} → ${it.optString("to")} • ${it.optString("description")} • due ₹${"%.2f".format(Locale.US, it.optDouble("due"))}" }
        val selector = Spinner(this).apply { adapter = ArrayAdapter(this@V62TTMMActivity, android.R.layout.simple_spinner_dropdown_item, labels) }
        val amount = ArthSaathiV62Design.input(this, "Settlement amount ₹")
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(selector); addView(amount) }
        AlertDialog.Builder(this).setTitle("Settle a group due").setView(box).setNegativeButton("CANCEL", null).setPositiveButton("SETTLE") { _, _ ->
            val due = dues.getOrNull(selector.selectedItemPosition) ?: return@setPositiveButton
            val a = amount.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val remaining = due.optDouble("due")
            if (a <= 0 || a > remaining + 0.01) {
                Toast.makeText(this, "Settlement must be positive and cannot exceed the remaining share.", Toast.LENGTH_LONG).show(); return@setPositiveButton
            }
            saveGroup()
            val id = V62Store.id("SET")
            s.add(V62Store.TTMM_SETTLEMENTS, JSONObject().apply {
                put("id", id); put("ownerUserId", owner); put("groupId", groupId); put("expenseId", due.optString("expenseId"))
                put("description", due.optString("description")); put("from", due.optString("member")); put("to", due.optString("to"))
                put("amount", a); put("status", "SETTLED"); put("createdAt", System.currentTimeMillis())
            })
            V62EventBus.publish(V62Event(V62Events.TTMM_EXPENSE_CHANGED, id)); render()
        }.show()
    }
}