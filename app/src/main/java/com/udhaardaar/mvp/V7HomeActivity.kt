package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/** ArthSaathi V7 canonical home — matched to the approved gold/blue visual master. */
class V7HomeActivity : V7SessionActivity() {
    private val d get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing) render()
    }

    private fun render() {
        if (!V7AccountStore.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(7), dp(10), dp(10))
            setBackgroundColor(ArthSaathiV7Design.CREAM)
        }

        // Approved master header: compact brand identity followed by the dashboard.
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ArthSaathiV7Design.bg(this@V7HomeActivity)
            setPadding(dp(8), dp(7), dp(8), dp(9))
        }
        header.addView(ArthSaathiV7Design.masthead(this))
        root.addView(header)

        val name = V7AccountStore.currentName(this).ifBlank { "User" }
        val welcome = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(11), dp(8), dp(11), dp(8))
            background = ArthSaathiV7Design.card(this@V7HomeActivity, Color.WHITE, 15)
        }
        val mobile = V7AccountStore.currentMobile(this)
        val person = V7Core.all(this, V7Core.Keys.PEOPLE).firstOrNull { it.optString("mobile") == mobile }
        val avatar = ImageView(this).apply {
            contentDescription = "Profile picture"
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ArthSaathiV7Design.GOLD_PALE)
                setStroke(dp(1), ArthSaathiV7Design.GOLD)
            }
            val uri = person?.optString("photoUri").orEmpty()
            if (uri.isNotBlank()) runCatching {
                contentResolver.openInputStream(android.net.Uri.parse(uri)).use { input ->
                    if (input != null) setImageBitmap(android.graphics.BitmapFactory.decodeStream(input))
                }
            }
            if (drawable == null) setImageResource(R.drawable.arthsaathi_logo)
        }
        welcome.addView(avatar, LinearLayout.LayoutParams(dp(40), dp(40)))
        val wt = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(9), 0, 0, 0)
        }
        wt.addView(ArthSaathiV7Design.text(this, "Hello $name", 15f, ArthSaathiV7Design.NAVY, true))
        wt.addView(ArthSaathiV7Design.text(this, "Good to see you!", 9.5f, ArthSaathiV7Design.MUTED))
        welcome.addView(wt, LinearLayout.LayoutParams(0, -2, 1f))
        welcome.addView(
            ArthSaathiV7Design.text(this, "Plan • Protect • Grow • Nominate", 8.5f, ArthSaathiV7Design.GOLD, true).apply {
                gravity = Gravity.CENTER
            }
        )
        root.addView(welcome, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })

        val plan = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(13), dp(10), dp(13), dp(10))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(ArthSaathiV7Design.GOLD_PALE, Color.WHITE)).apply {
                cornerRadius = dp(15).toFloat()
                setStroke(dp(1), ArthSaathiV7Design.GOLD)
            }
        }
        val planWords = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        planWords.addView(ArthSaathiV7Design.text(this, "Plan Today", 15f, ArthSaathiV7Design.NAVY, true))
        planWords.addView(ArthSaathiV7Design.text(this, "For a Brighter Tomorrow", 10f, ArthSaathiV7Design.MUTED))
        plan.addView(planWords, LinearLayout.LayoutParams(0, -2, 1f))
        plan.addView(ArthSaathiV7Design.text(this, "›", 28f, ArthSaathiV7Design.GOLD, true))
        plan.setOnClickListener { openModule("GROW") }
        root.addView(plan, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })

        val m = V7Core.metrics(this)
        root.addView(
            ArthSaathiV7Design.section(this, "Your Financial Snapshot"),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) }
        )
        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(
            "₹ %.2f".format(m.optDouble("assets")) to "Total Assets",
            "₹ %.2f".format(m.optDouble("activeCredits")) to "Active Credits"
        ).forEachIndexed { i, pair ->
            val stat = ArthSaathiV7Design.stat(this, pair.first, pair.second,
                if (i == 1) ArthSaathiV7Design.GREEN else ArthSaathiV7Design.GOLD)
            stats.addView(stat, LinearLayout.LayoutParams(0, dp(70), 1f).apply {
                if (i > 0) leftMargin = dp(6)
            })
        }
        root.addView(stats)

        root.addView(
            ArthSaathiV7Design.section(this, "What do you want to do?"),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) }
        )

        addGrid(root, listOf(
            Triple("₹", "Loans & Udhaar", "Give, receive & track credit") to { open("CREDIT") },
            Triple("↻", "Collect / Pay Dues", "Repayments & outstanding") to { open("REPAYMENT") },
            Triple("▣", "My Assets & Property", "Property, gold, deposits & more") to { open("ASSET_VAULT") },
            Triple("◆", "Insurance & Protection", "Policies, renewals & claims") to { open("INSURANCE") },
            Triple("▥", "Investments & Returns", "Portfolio, MIS & growth") to { openModule("GROW") },
            Triple("♜", "Will & Inheritance", "Plan family assets & claims") to { openTool("WILL") },
            Triple("⚖", "Legal Help & Claims", "Legal help and advocates") to { openModule("LEGAL") },
            Triple("●", "Nominee & Family", "Choose who can receive assets") to { openTool("NOMINEE") },
            Triple("✦", "My Money Report", "Assets, dues, returns & benefits") to { openTool("MIS") }
        ))

        root.addView(
            ArthSaathiV7Design.bottomNav(this, "Home", mapOf(
                "Home" to {},
                "Credit" to { openModule("CREDIT") },
                "Repay" to { openModule("REPAYMENT") },
                "Vault" to { openModule("ASSETS") },
                "More" to { startActivity(Intent(this, V7ModuleActivity::class.java).putExtra("module", "MORE")) }
            )),
            LinearLayout.LayoutParams(-1, dp(62)).apply { topMargin = dp(9) }
        )

        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        })
    }

    private fun addGrid(
        root: LinearLayout,
        items: List<Pair<Triple<String, String, String>, () -> Unit>>
    ) {
        var i = 0
        while (i < items.size) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            for (j in 0..2) {
                val index = i + j
                if (index < items.size) {
                    val item = items[index]
                    row.addView(
                        ArthSaathiV7Design.tile(this, item.first.first, item.first.second, item.first.third, item.second),
                        LinearLayout.LayoutParams(0, dp(116), 1f).apply {
                            if (j > 0) leftMargin = dp(5)
                        }
                    )
                }
            }
            root.addView(row, LinearLayout.LayoutParams(-1, dp(116)).apply {
                if (i > 0) topMargin = dp(5)
            })
            i += 3
        }
    }

    private fun open(key: String) {
        val module = when (key) {
            "CREDIT" -> "CREDIT"
            "REPAYMENT" -> "REPAYMENT"
            "ASSET_VAULT" -> "ASSETS"
            "INSURANCE" -> "PROTECT"
            "LEGACY" -> "LEGAL"
            else -> "MORE"
        }
        startActivity(Intent(this, V7ModuleActivity::class.java).putExtra("module", module))
    }

    private fun openModule(key: String) {
        startActivity(Intent(this, V7ModuleActivity::class.java).putExtra("module", key))
    }

    private fun openTool(key: String) {
        startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", key))
    }
}
