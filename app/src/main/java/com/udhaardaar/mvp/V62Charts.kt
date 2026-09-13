package com.udhaardaar.mvp

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class V62DonutChart(private val values:List<Float>):View(null){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG);private val colors=listOf(0xff0c9b91.toInt(),0xff2c67da.toInt(),0xffd0a02a.toInt(),0xff188b5e.toInt(),0xffbe444c.toInt(),0xff5c6c7c.toInt())
    override fun onDraw(c:Canvas){super.onDraw(c);if(values.isEmpty()||values.sum()<=0)return;val total=values.sum();val r=minOf(width,height)*.36f;val cx=width*.32f;val cy=height/2f;val rect=RectF(cx-r,cy-r,cx+r,cy+r);var start=-90f;p.style=Paint.Style.STROKE;p.strokeWidth=r*.34f;values.forEachIndexed{i,v->p.color=colors[i%colors.size];val sweep=360f*v/total;c.drawArc(rect,start,sweep,false,p);start+=sweep};p.style=Paint.Style.FILL;p.color=0xff0e2646.toInt();p.textAlign=Paint.Align.CENTER;p.textSize=r*.32f;c.drawText("${total.toInt()}",cx,cy+10,p);p.textSize=r*.12f;c.drawText("VALUE",cx,cy+r*.28f,p)}
}
