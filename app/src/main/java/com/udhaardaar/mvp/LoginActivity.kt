package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val bg=Color.rgb(246,248,251); private val navy=Color.rgb(25,43,65); private val blue=Color.rgb(38,99,235); private val teal=Color.rgb(0,145,135); private val muted=Color.rgb(96,112,128); private val border=Color.rgb(218,225,233)
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun box(fill:Int=Color.WHITE,stroke:Int=border,r:Int=14)=GradientDrawable().apply{setColor(fill);setStroke(dp(1),stroke);cornerRadius=dp(r).toFloat()}
    private fun lp(h:Int=-2,top:Int=0)=LinearLayout.LayoutParams(-1,if(h<0)LinearLayout.LayoutParams.WRAP_CONTENT else dp(h)).apply{setMargins(0,dp(top),0,0)}
    private fun label(s:String,size:Float,color:Int=navy,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size;setTextColor(color);typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL);includeFontPadding=false}
    private fun input(h:String,digits:Boolean=false)=EditText(this).apply{hint=h;setSingleLine(true);textSize=16f;setTextColor(navy);setHintTextColor(muted);setPadding(dp(16),dp(12),dp(16),dp(12));minHeight=dp(54);background=box();imeOptions=android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;if(digits)inputType=InputType.TYPE_CLASS_NUMBER;filters=arrayOf(InputFilter.LengthFilter(if(digits)10 else 80))}
    private fun validMobile(x:String)=x.matches(Regex("[6-9][0-9]{9}"))
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);login()}
    private fun base():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(18),dp(20),dp(28));setBackgroundColor(bg)}
    private fun screen(content:LinearLayout){setContentView(ScrollView(this).apply{isFillViewport=true;clipToPadding=false;addView(content)})}
    private fun header(r:LinearLayout,title:String,subtitle:String){val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(20),dp(18),dp(20));background=box()};card.addView(label("UDHAARDAAR",12f,teal,true));card.addView(label(title,25f,navy,true),lp(-2,7));card.addView(label(subtitle,13f,muted),lp(-2,6));r.addView(card,lp(-2,0))}
    private fun primary(s:String,click:()->Unit)=TextView(this).apply{text=s;textSize=14f;setTextColor(Color.WHITE);typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);gravity=Gravity.CENTER;minHeight=dp(54);background=box(blue,blue,12);setOnClickListener{click()}}
    private fun secondary(s:String,click:()->Unit)=TextView(this).apply{text=s;textSize=14f;setTextColor(navy);typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);gravity=Gravity.CENTER;minHeight=dp(52);background=box();setOnClickListener{click()}}
    private fun login(){
        val r=base();header(r,"Welcome back","Securely manage credit, repayments, documents and assets")
        val section=label("SIGN IN",12f,muted,true);r.addView(section,lp(-2,18))
        val m=input("Registered mobile number",true);val p=input("4–6 digit PIN",true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;filters=arrayOf(InputFilter.LengthFilter(6))}
        r.addView(m,lp(-2,8));r.addView(p,lp(-2,10))
        r.addView(primary("LOGIN WITH PIN + OTP"){val x=m.text.toString();val pin=p.text.toString();if(!validMobile(x)||prefs.getString("pin_$x",null)!=pin){Toast.makeText(this,"Enter a valid registered mobile and PIN",Toast.LENGTH_LONG).show();return@primary};otpDialog("Secure login — OTP required",x){go(x)}},lp(-2,14))
        r.addView(secondary("CREATE PROFILE / ACCOUNT"){register()},lp(-2,10))
        r.addView(secondary("RESET PIN WITH OTP"){val x=m.text.toString();if(validMobile(x)&&prefs.contains("pin_$x"))otpDialog("Reset PIN — OTP required",x){resetPin(x)}else Toast.makeText(this,"Enter a registered mobile first",Toast.LENGTH_LONG).show()},lp(-2,10))
        r.addView(label("Your records stay linked to your verified profile. Review every consent and transaction before confirmation.",12f,muted),lp(-2,18));screen(r)
    }
    private fun otpDialog(title:String,mobile:String,onVerified:()->Unit){val otp=(100000+Random.nextInt(900000)).toString();val code=input("Enter 6-digit OTP",true).apply{filters=arrayOf(InputFilter.LengthFilter(6))};val d=AlertDialog.Builder(this).setTitle(title).setMessage("Demo OTP: $otp\nUse the OTP sent to the registered mobile in production.").setView(code).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();d.setOnShowListener{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(code.text.toString()==otp){d.dismiss();onVerified()}else code.error="Incorrect OTP"}};d.show()}
    private fun go(m:String){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",m).apply();startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish()}
    private fun resetPin(x:String){val np=input("New 4–6 digit PIN",true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;filters=arrayOf(InputFilter.LengthFilter(6))};AlertDialog.Builder(this).setTitle("Set new PIN").setView(np).setPositiveButton("SAVE"){_,_->if(np.text.length in 4..6){prefs.edit().putString("pin_$x",np.text.toString()).apply();Toast.makeText(this,"PIN updated",Toast.LENGTH_LONG).show()}else Toast.makeText(this,"PIN must be 4–6 digits",Toast.LENGTH_LONG).show()}.setNegativeButton("CANCEL",null).show()}
    private fun register(){
        val r=base();header(r,"Create your profile","One verified profile for your credit and transaction records")
        val n=input("Full name");val m=input("Mobile number",true);val p=input("Create 4–6 digit PIN",true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;filters=arrayOf(InputFilter.LengthFilter(6))}
        r.addView(label("PROFILE DETAILS",12f,muted,true),lp(-2,18));r.addView(n,lp(-2,8));r.addView(m,lp(-2,10));r.addView(p,lp(-2,10))
        r.addView(primary("VERIFY MOBILE + CREATE ACCOUNT"){val x=m.text.toString();if(!validMobile(x)||n.text.toString().trim().length<2||p.text.length !in 4..6){Toast.makeText(this,"Complete valid profile details",Toast.LENGTH_LONG).show();return@primary};otpDialog("Verify mobile — account creation",x){prefs.edit().putString("pin_$x",p.text.toString()).putString("name_$x",n.text.toString().trim()).apply();go(x)}},lp(-2,14));r.addView(secondary("BACK TO SIGN IN"){login()},lp(-2,10));screen(r)
    }
}
