package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlin.math.min

/** Lightweight dependency-free donut chart used by the V6.2 MIS asset allocation view. */
class V62DonutChart(context: Context, private val values: List<Float>) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors = intArrayOf(
        0xff0b3954.toInt(), 0xff087e8b.toInt(), 0xffd4a017.toInt(),
        0xff4f6d7a.toInt(), 0xff6b4f8a.toInt(), 0xff7b8f8a.toInt()
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val total = values.sum().takeIf { it > 0f } ?: return
        val size = min(width, height).toFloat()
        val stroke = size * 0.16f
        val box = RectF(
            (width - size) / 2f + stroke, (height - size) / 2f + stroke,
            (width + size) / 2f - stroke, (height + size) / 2f - stroke
        )
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        var start = -90f
        values.forEachIndexed { i, value ->
            val sweep = value.coerceAtLeast(0f) / total * 360f
            paint.color = colors[i % colors.size]
            canvas.drawArc(box, start, sweep, false, paint)
            start += sweep
        }
        paint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY)
            MeasureSpec.getSize(heightMeasureSpec) else (width * 0.55f).toInt()
        setMeasuredDimension(width, height.coerceAtLeast(180))
    }
}
