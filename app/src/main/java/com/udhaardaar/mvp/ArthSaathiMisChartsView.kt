package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import java.util.Locale
import kotlin.math.max

/** Native MIS charts from the canonical ledger: balance, asset composition, and actual measures. */
class ArthSaathiMisChartsView(context: Context, private val metrics: ArthSaathiCoreEngine.Mis) : View(context) {
    private val density = resources.displayMetrics.density
    private val navy = android.graphics.Color.rgb(14, 38, 70)
    private val gold = android.graphics.Color.rgb(216, 148, 8)
    private val teal = android.graphics.Color.rgb(12, 155, 145)
    private val blue = android.graphics.Color.rgb(44, 103, 218)
    private val green = android.graphics.Color.rgb(24, 139, 94)
    private val orange = android.graphics.Color.rgb(214, 104, 45)
    private val muted = android.graphics.Color.rgb(92, 108, 124)
    private val track = android.graphics.Color.rgb(231, 226, 212)
    private val white = android.graphics.Color.WHITE
    private fun dp(v: Float) = v * density
    private fun money(v: Double) = String.format(Locale.US, "₹%,.2f", v)

    private val values = listOf(
        "Credits" to metrics.credits,
        "Repayments" to metrics.repayments,
        "Outstanding" to metrics.outstanding,
        "Total assets" to metrics.assets,
        "Financial assets" to metrics.financialAssets,
        "Non-financial" to metrics.nonFinancialAssets,
        "Portfolio value" to metrics.portfolioValue,
        "Liabilities" to metrics.liabilities,
        "Benefits / refunds" to metrics.benefits,
        "Group expenses" to metrics.groupExpenses,
        "Service revenue" to metrics.revenue,
        "Actual charges" to metrics.actualCharges,
        "Charge variance" to metrics.chargeVariance
    )
    private val palette = intArrayOf(navy, teal, orange, gold, teal, green, blue, navy, green, muted, gold, orange, blue)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = navy
        textSize = dp(11.5f)
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    private val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = navy
        textSize = dp(13.5f)
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
    }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val piePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = track; strokeWidth = dp(1f) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = dp(750f).toInt()
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec))
    }

    private fun drawPie(canvas: Canvas, centerX: Float, centerY: Float, radius: Float, values: List<Double>, colors: IntArray) {
        val total = values.sumOf { it.coerceAtLeast(0.0) }
        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        if (total <= 0.0) {
            piePaint.color = track
            canvas.drawCircle(centerX, centerY, radius, piePaint)
            return
        }
        var angle = -90f
        values.forEachIndexed { index, value ->
            if (value > 0.0) {
                val sweep = (value / total * 360.0).toFloat()
                piePaint.color = colors[index % colors.size]
                canvas.drawArc(rect, angle, sweep, true, piePaint)
                angle += sweep
            }
        }
    }

    private fun legend(canvas: Canvas, x: Float, y: Float, color: Int, label: String) {
        piePaint.color = color
        canvas.drawRoundRect(RectF(x, y - dp(8f), x + dp(9f), y + dp(1f)), dp(2f), dp(2f), piePaint)
        canvas.drawText(label, x + dp(14f), y, textPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val pad = dp(12f)
        canvas.drawRoundRect(RectF(0f, 0f, w, height.toFloat()), dp(16f), dp(16f),
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white; style = Paint.Style.FILL })
        canvas.drawRoundRect(RectF(0f, 0f, w, height.toFloat()), dp(16f), dp(16f),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(224, 213, 185)
                style = Paint.Style.STROKE
                strokeWidth = dp(1f)
            })

        canvas.drawText("Recorded balance snapshot", pad, dp(22f), boldPaint)
        drawPie(canvas, w * 0.27f, dp(70f), dp(32f),
            listOf(metrics.assets, metrics.liabilities), intArrayOf(gold, navy))
        legend(canvas, w * 0.50f, dp(62f), gold, "Assets  " + money(metrics.assets))
        legend(canvas, w * 0.50f, dp(84f), navy, "Liabilities  " + money(metrics.liabilities))

        canvas.drawLine(pad, dp(119f), w - pad, dp(119f), linePaint)
        canvas.drawText("Asset & portfolio composition", pad, dp(141f), boldPaint)
        val mixValues = listOf(metrics.financialAssets, metrics.nonFinancialAssets, metrics.portfolioValue)
        val mixColors = intArrayOf(teal, green, blue)
        drawPie(canvas, w * 0.27f, dp(194f), dp(34f), mixValues, mixColors)
        legend(canvas, w * 0.50f, dp(174f), teal, "Financial  " + money(metrics.financialAssets))
        legend(canvas, w * 0.50f, dp(196f), green, "Non-financial  " + money(metrics.nonFinancialAssets))
        legend(canvas, w * 0.50f, dp(218f), blue, "Portfolio  " + money(metrics.portfolioValue))

        val chartTitleY = dp(267f)
        canvas.drawLine(pad, chartTitleY - dp(12f), w - pad, chartTitleY - dp(12f), linePaint)
        canvas.drawText("All recorded measures", pad, chartTitleY, boldPaint)
        val maxValue = max(1.0, values.maxOf { it.second.coerceAtLeast(0.0) })
        val labelWidth = dp(100f)
        val valueWidth = dp(72f)
        val barLeft = pad + labelWidth
        val barRight = w - pad - valueWidth
        val barWidth = (barRight - barLeft).coerceAtLeast(dp(24f))
        values.forEachIndexed { index, item ->
            val y = chartTitleY + dp(21f) + index * dp(32f)
            canvas.drawText(item.first, pad, y + dp(3f), textPaint)
            val top = y - dp(7f)
            val bottom = top + dp(12f)
            barPaint.color = track
            canvas.drawRoundRect(RectF(barLeft, top, barLeft + barWidth, bottom), dp(5f), dp(5f), barPaint)
            val ratio = (item.second.coerceAtLeast(0.0) / maxValue).toFloat().coerceIn(0f, 1f)
            if (ratio > 0f) {
                barPaint.color = palette[index]
                canvas.drawRoundRect(RectF(barLeft, top, barLeft + barWidth * ratio, bottom), dp(5f), dp(5f), barPaint)
            }
            textPaint.textSize = dp(9f)
            canvas.drawText(compact(item.second), barRight + dp(3f), y + dp(3f), textPaint)
            textPaint.textSize = dp(11.5f)
        }
    }

    private fun compact(value: Double): String = when {
        value >= 10_000_000 -> String.format(Locale.US, "%.1fCr", value / 10_000_000)
        value >= 100_000 -> String.format(Locale.US, "%.1fL", value / 100_000)
        value >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000)
        else -> String.format(Locale.US, "%.0f", value)
    }
}
