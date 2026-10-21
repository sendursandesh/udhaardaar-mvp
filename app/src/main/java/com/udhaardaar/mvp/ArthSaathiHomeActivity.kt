package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Canonical ArthSaathi command centre. The five persistent destinations are
 * Home | Credit | Repay | Vault | More; feature screens remain owned by the
 * single canonical module registry.
 */
class ArthSaathiHomeActivity : Activity() {
    // Use the frozen brand palette everywhere; do not fork colours in the launcher shell.
    private val navy = ArthSaathiV7Design.NAVY
    private val gold = ArthSaathiV7Design.GOLD
    private val ink = ArthSaathiV7Design.NAVY
    private val muted = ArthSaathiV7Design.MUTED
    private val canvas = ArthSaathiV7Design.CREAM
    private lateinit var body: LinearLayout
    private lateinit var nav: LinearLayout
    private lateinit var navScroll: HorizontalScrollView
    private var currentDestination = "Home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15+ edge-to-edge: explicitly reserve system-bar insets so the
        // persistent five-tab navigation stays above the gesture/3-button bar.
        WindowCompat.setDecorFitsSystemWindows(window, false)
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
        // Read identity only from the authenticated ArthSaathi account store.
        // Do not read the legacy Udhaardaar shared-preferences namespace here.
        val activeMobile = ArthSaathiSession.mobile(this).orEmpty()
        val profileRecords = ArthSaathiDataStore.records()
        var displayName = "User"
        var photoUri: String? = null
        var fallbackProfileName = ""
        var fallbackPhotoUri: String? = null
        for (index in profileRecords.length() - 1 downTo 0) {
            val record = profileRecords.optJSONObject(index) ?: continue
            if (record.optString("type") != "PROFILE") continue
            val candidateName = record.optString("f0").trim()
            val candidateMobile = record.optString("f1").trim()
            val candidatePhoto = record.optString("profilePhotoUri").takeIf { it.isNotBlank() }
            if (fallbackProfileName.isBlank() && candidateName.isNotBlank()) {
                fallbackProfileName = candidateName
                fallbackPhotoUri = candidatePhoto
            }
            if (candidateMobile.isBlank() || candidateMobile == activeMobile) {
                if (candidateName.isNotBlank()) displayName = candidateName
                photoUri = candidatePhoto
                break
            }
        }
        if (displayName == "User" && fallbackProfileName.isNotBlank()) displayName = fallbackProfileName
        if (photoUri == null) photoUri = fallbackPhotoUri
        val resolvedPhotoUri = photoUri
        val profileView: View = if (resolvedPhotoUri != null) {
            ImageView(this).apply {
                contentDescription = "$displayName profile photo"
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = rounded(Color.rgb(255, 244, 207), 28)
                runCatching { setImageURI(android.net.Uri.parse(resolvedPhotoUri)) }
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
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(4), dp(6), dp(4), dp(6))
        }
        navScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = false
            setBackgroundColor(Color.WHITE)
            elevation = dp(8).toFloat()
            addView(nav, ViewGroup.LayoutParams(-2, -1))
        }
        root.addView(navScroll, LinearLayout.LayoutParams(-1, dp(62)))
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            windowInsets
        }
        setContentView(root)
        ViewCompat.requestApplyInsets(root)
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
                "Repay" -> "Repayment, QR Khata and shared settlements"
                "Vault" -> "Assets, liabilities, investments and documents"
                else -> "Reports, protection, people and services"
            }
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(navy)
        })
        intro.addView(TextView(this).apply {
            text = when (destination) {
                "Home" -> "Track your financial position from the account-scoped ledger."
                "Credit" -> "Register and review credit records; protected history requires verified consent."
                "Repay" -> "Record verified repayments, review QR invoices and manage group contributions."
                "Vault" -> "Maintain assets, liabilities, investments, protection and supporting documents."
                else -> "Open the remaining reports, people, legal and platform services."
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
            body.addView(TextView(this).apply {
                text = "Use the five navigation tabs to open each module from one canonical location. Snapshot figures refresh from your saved ArthSaathi records."
                textSize = 12f
                setTextColor(muted)
                setPadding(dp(2), dp(8), dp(2), dp(8))
            })
        } else {
            val modules = when (destination) {
                "Credit" -> listOf(
                    "REGISTER_CREDIT", "LOANS_UDHAAR", "CREDIT_INTELLIGENCE",
                    "TRADE_CREDIT", "FORMAL_CREDIT", "GUARANTOR", "RENTAL"
                )
                "Repay" -> listOf("REPAYMENT", "QR_KHATA", "GROUP_KHATA", "CHARGECHECK")
                "Vault" -> listOf(
                    "ASSET_VAULT", "LIABILITY_VAULT", "PORTFOLIO", "SWITCH_ANALYSIS",
                    "DOCUMENT_VAULT", "PROTECTION", "BENEFITS", "CLAIMS", "WILL_LEGACY"
                )
                "More" -> listOf(
                    "PEOPLE", "MIS", "LEGAL", "ADVOCATES", "AI_ADVISOR", "REVENUE",
                    "SECURITY_CONSENT", "INTEGRATIONS", "PROFILE", "FAMILY", "ADDRESS",
                    "ACCOUNTING", "FUNDING", "ALERTS"
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
                setPadding(dp(10), dp(8), dp(10), dp(8))
                setOnClickListener {
                    render(tab)
                    navScroll.post {
                        val child = nav.getChildAt(tabs.indexOf(tab))
                        if (child != null) navScroll.smoothScrollTo(child.left - dp(8), 0)
                    }
                }
            }, LinearLayout.LayoutParams(dp(76), -1))
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
