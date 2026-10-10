package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Canonical ArthSaathi command centre. The five persistent destinations are
 * Home | Credit | Repay | Vault | More; feature screens remain owned by the
 * single canonical module registry.
 */
class ArthSaathiHomeActivity : Activity() {
    private val navy = Color.rgb(18, 58, 106)
    private val gold = Color.rgb(201, 138, 10)
    private val ink = Color.rgb(34, 48, 67)
    private val muted = Color.rgb(100, 116, 139)
    private val canvas = Color.rgb(245, 248, 252)
    private lateinit var body: LinearLayout
    private lateinit var nav: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ArthSaathiSession.isLoggedIn(this)) {
            startActivity(Intent(this, ArthSaathiLoginActivity::class.java))
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(canvas)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(14))
            setBackgroundColor(Color.WHITE)
        }
        header.addView(ImageView(this).apply {
            setImageResource(R.drawable.arthsaathi_logo)
            contentDescription = "ArthSaathi approved journey logo"
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(58), dp(58)))
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), 0, 0, 0) }
        brand.addView(TextView(this).apply {
            text = "ArthSaathi"
            textSize = 25f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(navy)
        })
        brand.addView(TextView(this).apply {
            text = "Your Financial-Life Command Centre"
            textSize = 12f
            setTextColor(muted)
        })
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(this).apply {
            text = "PROFILE"
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
            gravity = Gravity.CENTER
            setPadding(dp(9), dp(9), dp(9), dp(9))
            background = rounded(navy, 28)
        })
        root.addView(header)

        val accent = View(this).apply { setBackgroundColor(gold) }
        root.addView(accent, LinearLayout.LayoutParams(-1, dp(3)))

        body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(18))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(body) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(dp(4), dp(6), dp(4), dp(6))
            elevation = dp(8).toFloat()
        }
        root.addView(nav, LinearLayout.LayoutParams(-1, dp(62)))
        setContentView(root)
        render("Home")
    }

    private fun render(destination: String) {
        body.removeAllViews()
        nav.removeAllViews()

        val intro = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(Color.WHITE, 18)
        }
        intro.addView(TextView(this).apply {
            text = when (destination) {
                "Home" -> "One view. Your whole financial life."
                "Credit" -> "Credit & account centre"
                "Repay" -> "Repayments & dues"
                "Vault" -> "Assets & liabilities"
                else -> "Services & tools"
            }
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
        })
        intro.addView(TextView(this).apply {
            text = when (destination) {
                "Home" -> "Track credit, assets, investments, protection and claims in one place."
                "Credit" -> "Register and review credit records. Consent and repayment controls remain enforced."
                "Repay" -> "Open repayment workflows and review related records."
                "Vault" -> "Keep asset and liability records together."
                else -> "Access reports, documents, benefits, legal support and app services."
            }
            textSize = 13f
            setTextColor(muted)
            setPadding(0, dp(7), 0, 0)
        })
        body.addView(intro)
        addSectionTitle(if (destination == "Home") "YOUR WORKSPACE" else destination.uppercase())

        val modules = when (destination) {
            "Credit" -> listOf("REGISTER_CREDIT", "LOANS_UDHAAR", "QR_KHATA", "PEOPLE")
            "Repay" -> listOf("REPAYMENT", "LOANS_UDHAAR")
            "Vault" -> listOf("ASSET_VAULT", "LIABILITY_VAULT", "DOCUMENT_VAULT")
            "More" -> listOf(
                "MIS", "PORTFOLIO", "SWITCH_ANALYSIS", "PROTECTION", "BENEFITS",
                "CHARGECHECK", "GROUP_KHATA", "CLAIMS", "WILL_LEGACY", "LEGAL",
                "ADVOCATES", "AI_ADVISOR", "REVENUE", "SECURITY_CONSENT", "INTEGRATIONS"
            )
            else -> ArthSaathiArchitectureRegistry.modules
                .filter { it.id != "HOME" }
                .map { it.id }
        }.distinct()

        val chosen = modules.mapNotNull { id -> ArthSaathiArchitectureRegistry.canonicalModule(id) }
        val grouped = chosen.groupBy { it.area }
        grouped.forEach { (area, items) ->
            if (destination == "Home" || items.size > 1 || area != destination.uppercase()) {
                body.addView(TextView(this).apply {
                    text = area.replace('_', ' ')
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(gold)
                    setPadding(dp(3), dp(17), 0, dp(6))
                })
            }
            items.forEach { module ->
                val card = Button(this).apply {
                    text = module.title + "   ›"
                    textSize = 14f
                    isAllCaps = false
                    gravity = Gravity.CENTER_VERTICAL
                    setTextColor(navy)
                    background = rounded(Color.WHITE, 12)
                    elevation = dp(1).toFloat()
                    setPadding(dp(14), dp(8), dp(14), dp(8))
                    setOnClickListener {
                        startActivity(
                            Intent(this@ArthSaathiHomeActivity, ArthSaathiMasterModuleActivity::class.java)
                                .putExtra("module", module.id)
                        )
                    }
                }
                val lp = LinearLayout.LayoutParams(-1, dp(54))
                lp.bottomMargin = dp(7)
                body.addView(card, lp)
            }
        }

        val tabs = listOf("Home", "Credit", "Repay", "Vault", "More")
        tabs.forEach { tab ->
            nav.addView(TextView(this).apply {
                text = tab
                textSize = 11f
                typeface = if (tab == destination) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (tab == destination) navy else muted)
                gravity = Gravity.CENTER
                background = if (tab == destination) rounded(Color.rgb(232, 239, 248), 12) else null
                setPadding(dp(4), dp(8), dp(4), dp(8))
                setOnClickListener { render(tab) }
            }, LinearLayout.LayoutParams(0, -1, 1f))
        }

        if (destination == "More") {
            val logout = Button(this).apply {
                text = "Log out"
                isAllCaps = false
                setTextColor(Color.WHITE)
                setBackgroundColor(navy)
                setOnClickListener {
                    ArthSaathiSession.logout(this@ArthSaathiHomeActivity)
                    startActivity(Intent(this@ArthSaathiHomeActivity, ArthSaathiLoginActivity::class.java))
                    finish()
                }
            }
            body.addView(logout, LinearLayout.LayoutParams(-1, dp(48)))
        }
    }

    private fun addSectionTitle(value: String) {
        body.addView(TextView(this).apply {
            text = value
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(muted)
            setPadding(dp(3), dp(18), 0, dp(7))
        })
    }

    private fun rounded(color: Int, radiusDp: Int): android.graphics.drawable.GradientDrawable =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp).toFloat()
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
