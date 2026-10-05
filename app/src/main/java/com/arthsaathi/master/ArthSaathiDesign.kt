package com.arthsaathi.master

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.TextView

object ArthSaathiDesign {
    val NAVY=Color.rgb(5,24,48); val GOLD=Color.rgb(214,148,8); val CREAM=Color.rgb(255,250,241); val WHITE=Color.WHITE; val MUTED=Color.rgb(101,115,130)
    fun text(c:Context,s:String,size:Float=14f,bold:Boolean=false,color:Int=NAVY)=TextView(c).apply{text=s;textSize=size;setTextColor(color);typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL)}
    fun card():GradientDrawable=GradientDrawable().apply{setColor(WHITE);cornerRadius=22f;setStroke(1,Color.rgb(225,215,190))}
    fun button(c:Context,label:String,action:()->Unit)=Button(c).apply{text=label;textSize=15f;isAllCaps=false;setTextColor(NAVY);typeface=Typeface.DEFAULT_BOLD;background=GradientDrawable().apply{setColor(GOLD);cornerRadius=18f};setOnClickListener{action()}}
    fun header(c:Context,title:String)=text(c,title,23f,true,NAVY).apply{setPadding(0,12,0,12)}
}
