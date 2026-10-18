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
    private val canvas = ArthSaathiV7Design.CREAM
    private lateinit var body: LinearLayout
    private lateinit var nav: LinearLayout
    private var currentDestination = "Home"

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
            background = ArthSaathiV7Design.bg(this@ArthSaathiHomeActivity)
        }
        header.addView(ImageView(this).apply {
            setImageResource(R.drawable.arthsaathi_logo)
            contentDescription = "ArthSaathi approved journey logo"
            tag = "arthsaathi-canonical-logo"
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(58), dp(58)))
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), 0, 0, 0) }
        brand.addView(TextView(this).apply {
            val wordmark = android.text.SpannableString("ArthSaathi")
            wordmark.setSpan(android.text.style.ForegroundColorSpan(Color.WHITE), 0, 4,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            wordmark.setSpan(android.text.style.ForegroundColorSpan(ArthSaathiV7Design.GOLD_2), 4, wordmark.length,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            text = wordmark
            textSize = 25f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        })
        brand.addView(TextView(this).apply {
            text = "Navigate Your Financial Journey"
            textSize = 12f
            setTextColor(ArthSaathiV7Design.GOLD_PALE)
        })
        brand.addView(TextView(this).apply {
            text = "Plan • Protect • Grow • Nominate"
            textSize = 10f
            setTextColor(ArthSaathiV7Design.GOLD_2)
            setPadding(0, dp(3), 0, 0)
        })
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val accountPrefs = getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE)
        val activeMobile = accountPrefs.getString("current_mobile", ArthSaathiSession.mobile(this).orEmpty()).orEmpty()
        val displayName = accountPrefs.getString("name_$activeMobile", "User").orEmpty().ifBlank { "User" }
        val photoUri = listOf("profile_photo_uri", "user_photo_uri", "photo_uri")
            .firstNotNullOfOrNull { key -> accountPrefs.getString(key, null)?.takeIf { it.isNotBlank() } }
        val profileView: View = if (photoUri != null) {
            ImageView(this).apply {
                contentDescription = "$displayName profile photo"
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = rounded(Color.rgb(255, 244, 207), 28)
                runCatching { setImageURI(android.net.Uri.parse(photoUri)) }
                    .onFailure { setImageResource(R.drawable.arthsaathi_logo) }
            }
        } else {
            TextView(this).apply {
                text = displayName.trim().firstOrNull()?.uppercase() ?: "U"
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(navy)
                gravity = Gravity.CENTER
                background = rounded(Color.rgb(255, 244, 207), 28)
                contentDescription = "$displayName profile"
            }
        }
        header.addView(profileView, LinearLayout.LayoutParams(dp(42), dp(42)))
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

    override fun onResume() {
        super.onResume()
        if (::body.isInitialized && currentDestination.isNotBlank()) render(currentDestination)
    }

    private fun render(destination: String) {
        currentDestination = destination
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

        if (destination == "Home") {
            val plan = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), dp(13), dp(16), dp(13))
                background = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                    intArrayOf(ArthSaathiV7Design.GOLD_PALE, Color.WHITE)
                ).apply {
                    cornerRadius = dp(16).toFloat()
                    setStroke(dp(1), gold)
                }
                setOnClickListener { render("More") }
            }
            val planText = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            planText.addView(TextView(this).apply {
                text = "Plan Today"
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(navy)
            })
            planText.addView(TextView(this).apply {
                text = "For a Brighter Tomorrow"
                textSize = 12f
                setTextColor(muted)
            })
            plan.addView(planText, LinearLayout.LayoutParams(0, -2, 1f))
            plan.addView(TextView(this).apply { text = "›"; textSize = 30f; setTextColor(gold) })
            body.addView(plan, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(6) })

            addSectionTitle("YOUR FINANCIAL SNAPSHOT")
            val metrics = ArthSaathiCoreEngine.mis()
            val allRecords = ArthSaathiDataStore.records()
            val activeCredits = (0 until allRecords.length()).count { index ->
                val record = allRecords.optJSONObject(index)
                record != null && record.optString("type") == "CREDIT" && record.optString("status") == "ACTIVE"
            }
            fun addMetricRow(items: List<Triple<String, String, Int>>) {
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                items.forEachIndexed { index, item ->
                    val stat = ArthSaathiV7Design.stat(this, item.first, item.second, item.third)
                    row.addView(stat, LinearLayout.LayoutParams(0, dp(78), 1f).apply {
                        if (index > 0) leftMargin = dp(7)
                    })
                }
                body.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(7) })
            }
            fun money(value: Double): String = "₹" + String.format(java.util.Locale.US, "%,.2f", value)
            addMetricRow(listOf(
                Triple(money(metrics.assets), "Total Assets", gold),
                Triple(money(metrics.liabilities), "Total Liabilities", ArthSaathiV7Design.RED)
            ))
            addMetricRow(listOf(
                Triple(money(metrics.outstanding), "Credit Outstanding", ArthSaathiV7Design.TEAL),
                Triple(activeCredits.toString(), "Active Credits", ArthSaathiV7Design.GREEN)
            ))
            addMetricRow(listOf(
                Triple(money(metrics.benefits), "Benefits / Refunds", gold),
                Triple(money(metrics.revenue), "Service Revenue", navy)
            ))
            addSectionTitle("QUICK ACTIONS")
            val quickActions = listOf(
                Triple("₹", "Register Credit", "Record a new credit") to "REGISTER_CREDIT",
                Triple("↻", "Repayment", "Record a payment") to "REPAYMENT",
                Triple("▣", "Asset Vault", "What you own") to "ASSET_VAULT",
                Triple("✚", "Protect", "Insurance and cover") to "PROTECTION",
                Triple("▥", "Grow", "Portfolio and returns") to "PORTFOLIO",
                Triple("★", "Benefits", "Benefits and refunds") to "BENEFITS",
                Triple("⚖", "Legal Help", "Find assistance") to "LEGAL",
                Triple("⇄", "Group Khata", "Share and settle") to "GROUP_KHATA",
                Triple("▤", "MIS & Insights", "Your financial position") to "MIS"
            )
            var index = 0
            while (index < quickActions.size) {
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                for (column in 0..2) {
                    val itemIndex = index + column
                    if (itemIndex < quickActions.size) {
                        val item = quickActions[itemIndex]
                        row.addView(ArthSaathiV7Design.tile(
                            this, item.first.first, item.first.second, item.first.third
                        ) { openModule(item.second) }, LinearLayout.LayoutParams(0, dp(112), 1f).apply {
                            if (column > 0) leftMargin = dp(6)
                        })
                    }
                }
                body.addView(row, LinearLayout.LayoutParams(-1, dp(112)).apply {
                    if (index > 0) topMargin = dp(6)
                })
                index += 3
            }
            val allServices = Button(this).apply {
                text = "View all services  ›"
                isAllCaps = false
                setTextColor(Color.WHITE)
                background = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(ArthSaathiV7Design.NAVY_2, ArthSaathiV7Design.NAVY_3)
                ).apply { cornerRadius = dp(14).toFloat() }
                setOnClickListener { render("More") }
            }
            body.addView(allServices, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(10) })
        } else {
            val modules = when (destination) {
                "Credit" -> listOf("REGISTER_CREDIT", "LOANS_UDHAAR", "QR_KHATA", "PEOPLE")
                "Repay" -> listOf("REPAYMENT", "LOANS_UDHAAR")
                "Vault" -> listOf("ASSET_VAULT", "LIABILITY_VAULT", "DOCUMENT_VAULT")
                "More" -> listOf(
                    "PEOPLE", "MIS", "PORTFOLIO", "SWITCH_ANALYSIS", "PROTECTION", "BENEFITS",
                    "CHARGECHECK", "GROUP_KHATA", "CLAIMS", "WILL_LEGACY", "LEGAL",
                    "ADVOCATES", "AI_ADVISOR", "REVENUE", "SECURITY_CONSENT", "INTEGRATIONS"
                )
                else -> emptyList()
            }.distinct()
            val chosen = modules.mapNotNull { id -> ArthSaathiArchitectureRegistry.canonicalModule(id) }
            val grouped = chosen.groupBy { it.area }
            grouped.forEach { (area, items) ->
                body.addView(TextView(this).apply {
                    text = area.replace('_', ' ')
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(gold)
                    setPadding(dp(3), dp(17), 0, dp(6))
                })
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
                        setOnClickListener { openModule(module.id) }
                    }
                    val lp = LinearLayout.LayoutParams(-1, dp(54))
                    lp.bottomMargin = dp(7)
                    body.addView(card, lp)
                }
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

    private fun openModule(moduleId: String) {
        val module = ArthSaathiArchitectureRegistry.canonicalModule(moduleId) ?: return
        startActivity(Intent(this, ArthSaathiMasterModuleActivity::class.java).putExtra("module", module.id))
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
