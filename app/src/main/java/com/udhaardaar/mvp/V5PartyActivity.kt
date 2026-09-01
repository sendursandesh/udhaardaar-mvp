package com.udhaardaar.mvp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class V5PartyActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }

    private fun e(h: String) = EditText(this).apply {
        hint = h
        setSingleLine(true)
        textSize = 14f
    }

    private fun add(r: LinearLayout, v: View) {
        r.addView(v, LinearLayout.LayoutParams(-1, 52).apply { setMargins(0, 3, 0, 3) })
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        show()
    }

    private fun show() {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 12, 18, 18)
        }
        r.addView(TextView(this).apply {
            text = "UDHAARDAAR V5 • PARTY & IDENTITY"
            textSize = 21f
            setTextColor(Color.rgb(24, 58, 92))
        })
        r.addView(TextView(this).apply {
            text = "Search existing party or create a verified profile"
            textSize = 12f
        })

        val search = e("Search name / mobile / PAN / Aadhaar / GSTIN")
        add(r, search)
        add(r, Button(this).apply {
            text = "SEARCH PARTY"
            setOnClickListener {
                val q = search.text.toString().trim()
                val hits = store.all("profiles").filter { p ->
                    listOf("name", "mobile", "pan", "aadhaar", "gstin").any { key ->
                        p.optString(key).equals(q, true)
                    }
                }
                showHits(hits)
            }
        })

        val type = Spinner(this)
        type.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("BORROWER", "LENDER", "SELLER", "BUYER", "GUARANTOR", "NOMINEE", "LEGAL HEIR", "TRUSTED PERSON", "BUSINESS", "BANK/NBFC"))
        add(r, type)

        val name = e("Full name / legal entity *")
        val mobile = e("Mobile *")
        val pan = e("PAN")
        val aad = e("Aadhaar (12 digits)")
        val gst = e("GSTIN")
        val pin = e("PIN code")
        val city = e("City")
        val state = e("State")
        listOf(name, mobile, pan, aad, gst, pin, city, state).forEach { add(r, it) }

        add(r, Button(this).apply {
            text = "CREATE / UPDATE PROFILE"
            setOnClickListener {
                if (!V5Validation.mobile(mobile.text.toString())) {
                    Toast.makeText(this@V5PartyActivity, "Enter valid mobile", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (!V5Validation.pan(pan.text.toString())) {
                    Toast.makeText(this@V5PartyActivity, "Invalid PAN", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (!V5Validation.gstin(gst.text.toString())) {
                    Toast.makeText(this@V5PartyActivity, "Invalid GSTIN", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val id = "P-${System.currentTimeMillis()}"
                store.replace("profiles", JSONObject().apply {
                    put("id", id)
                    put("type", type.selectedItem.toString())
                    put("name", name.text.toString().trim())
                    put("mobile", mobile.text.toString().trim())
                    put("pan", pan.text.toString().trim().uppercase())
                    put("aadhaar", aad.text.toString().trim())
                    put("gstin", gst.text.toString().trim().uppercase())
                    put("pin", pin.text.toString().trim())
                    put("city", city.text.toString().trim())
                    put("state", state.text.toString().trim())
                    put("verified", false)
                    put("createdAt", System.currentTimeMillis())
                })
                Toast.makeText(this@V5PartyActivity, "Profile saved. Verification can be completed before execution.", Toast.LENGTH_LONG).show()
            }
        })
        add(r, Button(this).apply { text = "BACK"; setOnClickListener { finish() } })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun showHits(hits: List<JSONObject>) {
        val msg = if (hits.isEmpty()) {
            "No matching profile. Create a new profile below."
        } else {
            hits.joinToString("\n\n") { p ->
                val outstanding = store.all("credits")
                    .filter { it.optString("borrowerProfileId") == p.optString("id") }
                    .sumOf { it.optDouble("outstanding", 0.0) }
                "${p.optString("name")} • ${p.optString("type")}\nMobile: ${p.optString("mobile")}\nOutstanding: $outstanding"
            }
        }
        AlertDialog.Builder(this).setTitle("Party results").setMessage(msg)
            .setPositiveButton("OK", null).show()
    }
}
