package com.udhaardaar.mvp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class VerificationDocumentsActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("verification_v3", MODE_PRIVATE) }
    private var pendingOtp = ""
    private var selectedDocumentUri: Uri? = null
    private lateinit var result: TextView
    private lateinit var uniqueId: EditText
    private lateinit var name: EditText
    private lateinit var mobile: EditText
    private lateinit var pan: EditText
    private lateinit var gstin: EditText
    private lateinit var otp: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_documents)

        uniqueId = findViewById(R.id.etUniqueId)
        name = findViewById(R.id.etPartyName)
        mobile = findViewById(R.id.etMobile)
        pan = findViewById(R.id.etPan)
        gstin = findViewById(R.id.etGstin)
        otp = findViewById(R.id.etOtp)
        result = findViewById(R.id.tvVerificationResult)

        findViewById<Button>(R.id.btnSearch).setOnClickListener { searchParty() }
        findViewById<Button>(R.id.btnCreateProfile).setOnClickListener { createProfile() }
        findViewById<Button>(R.id.btnAttachDocument).setOnClickListener { pickDocument() }
        findViewById<Button>(R.id.btnGenerateOtp).setOnClickListener { generateOtp() }
        findViewById<Button>(R.id.btnShareOtp).setOnClickListener { shareOtp() }
        findViewById<Button>(R.id.btnVerifyOtp).setOnClickListener { verifyOtp() }
    }

    private fun createProfile() {
        val person = name.text.toString().trim()
        if (person.isEmpty()) {
            name.error = "Enter name / business name"
            return
        }
        val next = prefs.getInt("party_count", 0) + 1
        val id = "UDH-${SimpleDateFormat("yyyy", Locale.US).format(Date())}-${String.format(Locale.US, "%06d", next)}"
        prefs.edit()
            .putInt("party_count", next)
            .putString("party_${next}_id", id)
            .putString("party_${next}_name", person)
            .putString("party_${next}_mobile", mobile.text.toString().trim())
            .putString("party_${next}_pan", pan.text.toString().trim())
            .putString("party_${next}_gstin", gstin.text.toString().trim())
            .apply()
        uniqueId.setText(id)
        result.text = "Profile created and uniquely identified.\nUnique ID: $id\nYou can now use this ID for verification/search."
        Toast.makeText(this, "Profile created", Toast.LENGTH_SHORT).show()
    }

    private fun searchParty() {
        val query = uniqueId.text.toString().trim().lowercase(Locale.getDefault())
        if (query.isEmpty()) {
            uniqueId.error = "Enter Unique ID"
            return
        }
        val count = prefs.getInt("party_count", 0)
        for (i in 1..count) {
            val id = prefs.getString("party_${i}_id", "") ?: ""
            val person = prefs.getString("party_${i}_name", "") ?: ""
            val m = prefs.getString("party_${i}_mobile", "") ?: ""
            val p = prefs.getString("party_${i}_pan", "") ?: ""
            val g = prefs.getString("party_${i}_gstin", "") ?: ""
            if (id.lowercase(Locale.getDefault()) == query) {
                name.setText(person); mobile.setText(m); pan.setText(p); gstin.setText(g)
                result.text = "VERIFIED LOCAL PROFILE\nUnique ID: $id\nName: $person\nMobile: $m\nPAN: ${mask(p)}\nGSTIN: ${mask(g)}"
                return
            }
        }
        result.text = "No profile found for this Unique ID."
    }

    private fun mask(value: String): String {
        if (value.length <= 4) return value
        return "•".repeat(value.length - 4) + value.takeLast(4)
    }

    private fun pickDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
        }
        startActivityForResult(intent, 701)
    }

    @Deprecated("Android callback compatibility for minSdk 23")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 701 && resultCode == RESULT_OK) {
            selectedDocumentUri = data?.data
            result.text = "Document selected:\n${selectedDocumentUri?.lastPathSegment ?: "document"}\n\nIt is ready to be attached to a future credit/consent record."
        }
    }

    private fun generateOtp() {
        pendingOtp = Random.nextInt(100000, 1000000).toString()
        otp.setText("")
        result.text = "OTP generated for consent confirmation. Use Share OTP to send it through your preferred channel.\n\nFor production deployment, connect this flow to a real SMS/OTP provider."
        Toast.makeText(this, "OTP generated", Toast.LENGTH_SHORT).show()
    }

    private fun shareOtp() {
        if (pendingOtp.isEmpty()) {
            generateOtp()
        }
        val text = "Udhaardaar verification OTP: $pendingOtp. Do not share this OTP with anyone except the intended consent participant."
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, "Share OTP"))
    }

    private fun verifyOtp() {
        if (pendingOtp.isEmpty()) {
            Toast.makeText(this, "Generate OTP first", Toast.LENGTH_SHORT).show()
            return
        }
        if (otp.text.toString().trim() == pendingOtp) {
            result.text = "OTP VERIFIED ✓\nDigital consent checkpoint completed locally.\nThe production version should persist a signed consent record and server-side verification timestamp."
            pendingOtp = ""
            otp.setText("")
        } else {
            otp.error = "Incorrect OTP"
        }
    }
}
