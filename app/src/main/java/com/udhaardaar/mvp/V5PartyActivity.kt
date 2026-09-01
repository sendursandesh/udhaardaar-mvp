package com.udhaardaar.mvp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class V5PartyActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }

    private fun e(h: String) = EditText(this).apply {
        hint = h
        setSingleLine(true)
        textSize = 16f
        minHeight = dp(52)
        includeFontPadding = true
        setPadding(dp(12), dp(8), dp(12), dp(8))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun add(r: LinearLayout, v: View, minHeight: Int = 52) {
        r.addView(v, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, dp(5), 0, dp(5))
            this@apply // keep generated layout params explicit
        })
        v.minimumHeight = dp(minHeight)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 15f
        minHeight = dp(54)
        setPadding(dp(12), dp(8), dp(12), dp(8))
        setOnClickListener { action() }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        show()
    }

    private fun show() {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(24))
            setBackgroundColor(Color.rgb(238, 248, 253))
        }
        add(r, TextView(this).apply {
            text = "UDHAARDAAR V5\nPARTY & IDENTITY"
            textSize = 22f
            setTextColor(Color.rgb(24, 58, 92))
            includeFontPadding = true
        }, 72)
        add(r, TextView(this).apply {
            text = "Search an existing party or create a new profile. A saved profile remains available for later credit registration."
            textSize = 14f
            setPadding(0, 0, 0, dp(8))
        }, 60)

        val search = e("Search exact name / mobile / PAN / Aadhaar / GSTIN")
        add(r, search)
        add(r, button("SEARCH PARTY") {
            val q = search.text.toString().trim()
            if (q.isBlank()) {
                search.error = "Enter a name or identifier"
                return@button
            }
            val hits = store.all("profiles").filter { p ->
                listOf("name", "mobile", "pan", "aadhaar", "gstin").any { key ->
                    p.optString(key).equals(q, true)
                }
            }
            showHits(hits)
        })

        add(r, TextView(this).apply { text = "CREATE / UPDATE PROFILE"; textSize = 17f; setTextColor(Color.rgb(24, 58, 92)) }, 42)

        val type = Spinner(this).apply {
            minimumHeight = dp(52)
            adapter = ArrayAdapter(this@V5PartyActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("BORROWER", "LENDER", "SELLER", "BUYER", "GUARANTOR", "NOMINEE", "LEGAL HEIR", "TRUSTED PERSON", "BUSINESS", "BANK/NBFC"))
        }
        add(r, type)

        val name = e("Full name / legal entity *")
        val mobile = e("Mobile *")
        val pan = e("PAN (optional)")
        val aad = e("Aadhaar (12 digits, optional)")
        val gst = e("GSTIN (optional)")
        val pin = e("PIN code (optional)")
        val city = e("City")
        val state = e("State")
        listOf(name, mobile, pan, aad, gst, pin, city, state).forEach { add(r, it) }

        add(r, button("SAVE PROFILE") {
            val n = name.text.toString().trim()
            val m = mobile.text.toString().trim()
            val p = pan.text.toString().trim().uppercase()
            val a = aad.text.toString().trim()
            val g = gst.text.toString().trim().uppercase()
            val pc = pin.text.toString().trim()

            if (n.isBlank()) { name.error = "Name is required"; name.requestFocus(); return@button }
            if (!V5Validation.mobile(m)) { mobile.error = "Enter valid 10-digit mobile"; mobile.requestFocus(); return@button }
            if (!V5Validation.pan(p)) { pan.error = "Invalid PAN"; pan.requestFocus(); return@button }
            if (!V5Validation.aadhaar(a)) { aad.error = "Invalid Aadhaar"; aad.requestFocus(); return@button }
            if (!V5Validation.gstin(g)) { gst.error = "Invalid GSTIN"; gst.requestFocus(); return@button }
            if (pc.isNotBlank() && !V5Validation.pin(pc)) { pin.error = "Invalid 6-digit PIN"; pin.requestFocus(); return@button }

            val existing = store.all("profiles").firstOrNull {
                it.optString("mobile") == m || (p.isNotBlank() && it.optString("pan").equals(p, true))
            }
            val id = existing?.optString("id")?.takeIf { it.isNotBlank() } ?: "P-${System.currentTimeMillis()}"
            val profile = JSONObject().apply {
                put("id", id)
                put("type", type.selectedItem.toString())
                put("name", n)
                put("mobile", m)
                put("pan", p)
                put("aadhaar", a)
                put("gstin", g)
                put("pin", pc)
                put("city", city.text.toString().trim())
                put("state", state.text.toString().trim())
                put("verified", existing?.optBoolean("verified", false) ?: false)
                put("updatedAt", System.currentTimeMillis())
                if (existing == null) put("createdAt", System.currentTimeMillis())
            }
            store.replace("profiles", profile)
            Toast.makeText(this, if (existing == null) "Profile created successfully" else "Profile updated successfully", Toast.LENGTH_LONG).show()
            search.setText(m)
            showHits(listOf(profile))
        })

        add(r, button("CLEAR FORM") {
            listOf(name, mobile, pan, aad, gst, pin, city, state).forEach { it.setText(""); it.error = null }
        })
        add(r, button("BACK") { finish() })

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(r, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        })
    }

    private fun showHits(hits: List<JSONObject>) {
        val msg = if (hits.isEmpty()) {
            "No matching profile. Create a new profile below."
        } else {
            hits.joinToString("\n\n") { p ->
                val outstanding = store.all("credits")
                    .filter { it.optString("borrowerProfileId") == p.optString("id") }
                    .sumOf { it.optDouble("outstanding", 0.0) }
                "ID: ${p.optString("id")}\n${p.optString("name")} • ${p.optString("type")}\nMobile: ${p.optString("mobile")}\nOutstanding: ₹$outstanding"
            }
        }
        AlertDialog.Builder(this).setTitle("Party results").setMessage(msg)
            .setPositiveButton("OK", null).show()
    }
}
