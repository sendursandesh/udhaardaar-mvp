package com.udhaardaar.mvp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject

class V5QrCreditActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val engine by lazy { V5PlatformEngine(this) }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun edit(hint: String) = EditText(this).apply {
        this.hint = hint
        setSingleLine(true)
        textSize = 16f
        minHeight = dp(52)
        setPadding(dp(12), dp(8), dp(12), dp(8))
    }

    private fun addRow(root: LinearLayout, view: View, heightDp: Int = 0) {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            if (heightDp > 0) dp(heightDp) else ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dp(6), 0, dp(6))
        root.addView(view, params)
        view.minimumHeight = dp(54)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(24))
        }

        addRow(root, TextView(this).apply {
            text = "UDHAARDAAR V5 • SCAN QR / PAY OR CREDIT"
            textSize = 20f
            setTextColor(Color.rgb(24, 58, 92))
            setPadding(0, 0, 0, dp(8))
        })
        addRow(root, TextView(this).apply {
            text = "No silent credit: transaction → consent → offer → agreement → payment/credit"
            textSize = 14f
        }, 48)

        val qr = edit("QR payload / merchant reference *")
        val merchant = edit("Merchant / seller *")
        val invoice = edit("Invoice / transaction reference")
        val goods = edit("Goods / service description")
        val gross = edit("Gross amount *")
        val tax = edit("Tax")
        val discount = edit("Discount")
        val buyer = edit("Buyer")
        listOf(qr, merchant, invoice, goods, gross, tax, discount, buyer).forEach { addRow(root, it) }

        addRow(root, Button(this).apply {
            text = "CAPTURE QR TRANSACTION"
            setOnClickListener {
                val amt = gross.text.toString().toDoubleOrNull()
                if (qr.text.isBlank() || merchant.text.isBlank() || amt == null || amt <= 0.0) {
                    Toast.makeText(this@V5QrCreditActivity, "Enter QR, merchant and valid amount", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val id = "QR-${System.currentTimeMillis()}"
                store.add("qr_transactions", JSONObject().apply {
                    put("id", id)
                    put("qrRef", qr.text.toString())
                    put("merchant", merchant.text.toString())
                    put("invoice", invoice.text.toString())
                    put("goods", goods.text.toString())
                    put("gross", amt)
                    put("tax", tax.text.toString())
                    put("discount", discount.text.toString())
                    put("buyer", buyer.text.toString())
                    put("timestamp", System.currentTimeMillis())
                    put("status", "CAPTURED")
                })
                engine.audit(id, "QR_TRANSACTION_CAPTURED", buyer.text.toString())
                Toast.makeText(this@V5QrCreditActivity, "Transaction captured", Toast.LENGTH_LONG).show()
            }
        })

        addRow(root, Button(this).apply {
            text = "PAY NOW"
            setOnClickListener {
                Toast.makeText(
                    this@V5QrCreditActivity,
                    "Payment hand-off recorded; live PSP/bank integration is required for actual settlement.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        addRow(root, Button(this).apply {
            text = "REQUEST / OFFER CREDIT"
            setOnClickListener {
                val id = "OFR-${System.currentTimeMillis()}"
                store.add("credit_offers", JSONObject().apply {
                    put("id", id)
                    put("merchant", merchant.text.toString())
                    put("amount", gross.text.toString().toDoubleOrNull() ?: 0.0)
                    put("status", "CONSENT_PENDING")
                    put("provider", "Provider abstraction")
                    put("createdAt", System.currentTimeMillis())
                })
                engine.audit(id, "CREDIT_OFFER_CREATED", buyer.text.toString())
                Toast.makeText(
                    this@V5QrCreditActivity,
                    "Credit offer created pending seller/provider consent and underwriting",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        addRow(root, Button(this).apply {
            text = "BACK"
            setOnClickListener { finish() }
        })

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            isSmoothScrollingEnabled = true
            addView(root, ViewGroup.LayoutParams(-1, -2))
        }
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, maxOf(imeBottom, dp(24)))
            insets
        }
        setContentView(scroll)
    }
}
