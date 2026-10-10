package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import java.util.Locale
import kotlin.math.max

/** Lightweight native MIS charts, driven only by the canonical ledger metrics. */
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
        "Assets / portfolio" to metrics.assets,
        "Liabilities" to metrics.liabilities,
        "Benefits / refunds" to metrics.benefits,
        "Group expenses" to metrics.groupExpenses,
        "Service revenue (net)" to metrics.revenue,
        "Actual charges" to metrics.actualCharges,
        "Charge variance" to metrics.chargeVariance
    )
    private val palette = intArrayOf(navy, teal, orange, gold, blue, green, muted, navy)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = navy
        textSize = dp(12f)
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
    }
    private val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = navy
        textSize = dp(14f)
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
    }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val piePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = track; strokeWidth = dp(1f) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = dp(500f).toInt()
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec))
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
        val pieTop = dp(34f)
        val radius = dp(42f)
        val centerX = w / 2f
        val centerY = pieTop + radius
        val assets = metrics.assets.coerceAtLeast(0.0)
        val liabilities = metrics.liabilities.coerceAtLeast(0.0)
        val total = assets + liabilities
        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        if (total <= 0.0) {
            piePaint.color = track
            canvas.drawCircle(centerX, centerY, radius, piePaint)
        } else {
            var angle = -90f
            val assetSweep = (assets / total * 360.0).toFloat()
            if (assets > 0.0) {
                piePaint.color = gold
                canvas.drawArc(rect, angle, assetSweep, true, piePaint)
                angle += assetSweep
            }
            if (liabilities > 0.0) {
                piePaint.color = navy
                canvas.drawArc(rect, angle, (liabilities / total * 360.0).toFloat(), true, piePaint)
            }
        }
        val legendY = centerY - dp(12f)
        piePaint.color = gold
        canvas.drawRoundRect(RectF(pad, legendY, pad + dp(10f), legendY + dp(10f)), dp(2f), dp(2f), piePaint)
        canvas.drawText("Assets / portfolio  " + money(assets), pad + dp(16f), legendY + dp(9f), textPaint)
        piePaint.color = navy
        canvas.drawRoundRect(RectF(pad, legendY + dp(20f), pad + dp(10f), legendY + dp(30f)), dp(2f), dp(2f), piePaint)
        canvas.drawText("Liabilities  " + money(liabilities), pad + dp(16f), legendY + dp(29f), textPaint)

        val chartTitleY = dp(151f)
        canvas.drawLine(pad, chartTitleY - dp(12f), w - pad, chartTitleY - dp(12f), linePaint)
        canvas.drawText("All recorded measures", pad, chartTitleY, boldPaint)
        val maxValue = max(1.0, values.maxOf { it.second.coerceAtLeast(0.0) })
        val labelWidth = dp(104f)
        val valueWidth = dp(82f)
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
            textPaint.textSize = dp(9.5f)
            canvas.drawText(compact(item.second), barRight + dp(4f), y + dp(3f), textPaint)
            textPaint.textSize = dp(12f)
        }
    }

    private fun compact(value: Double): String = when {
        value >= 10_000_000 -> String.format(Locale.US, "%.1fCr", value / 10_000_000)
        value >= 100_000 -> String.format(Locale.US, "%.1fL", value / 100_000)
        value >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000)
        else -> String.format(Locale.US, "%.0f", value)
    }
}
