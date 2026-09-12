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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val navy=Color.rgb(18,48,76); private val teal=Color.rgb(0,145,135); private val blue=Color.rgb(38,99,235); private val bg=Color.rgb(247,249,252); private val muted=Color.rgb(92,108,124); private val border=Color.rgb(218,226,235)
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun box(fill:Int=Color.WHITE)=GradientDrawable().apply{setColor(fill);setStroke(dp(1),border);cornerRadius=dp(14).toFloat()}
    private fun input(h:String,digits:Boolean=false)=EditText(this).apply{hint=h;textSize=16f;setPadding(dp(14),dp(11),dp(14),dp(11));minHeight=dp(54);background=box();setSingleLine(true);imeOptions=android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;if(digits)inputType=InputType.TYPE_CLASS_NUMBER;filters=arrayOf(InputFilter.LengthFilter(if(digits)10 else 80))}
    private fun button(s:String,fill:Int=blue,click:()->Unit)=TextView(this).apply{text=s;textSize=14f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD;minHeight=dp(54);background=box(fill);setOnClickListener{click()}}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);login()}
    private fun shell():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(18),dp(20),dp(28));setBackgroundColor(bg)}
    private fun screen(r:LinearLayout){setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})}
    private fun header(r:LinearLayout,title:String,sub:String){val h=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(14),dp(14),dp(14),dp(14));background=box()};val logo=ImageView(this).apply{setImageResource(R.drawable.udhaardaar_logo);contentDescription="ArthSaathi logo"};h.addView(logo,LinearLayout.LayoutParams(dp(64),dp(64)));val b=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};b.addView(TextView(this@LoginActivity).apply{text="ARTHSAATHI";textSize=24f;setTextColor(navy);typeface=Typeface.DEFAULT_BOLD});b.addView(TextView(this@LoginActivity).apply{text="Your Money. Your Records. Your Rights.";textSize=12f;setTextColor(teal);typeface=Typeface.DEFAULT_BOLD});b.addView(TextView(this@LoginActivity).apply{text="$title\n$sub";textSize=12f;setTextColor(muted)});h.addView(b,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(12)});r.addView(h)}
    private fun add(r:LinearLayout,v:android.view.View,top:Int=10){r.addView(v,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,dp(top),0,0)})}
    private fun validMobile(x:String)=x.matches(Regex("[6-9][0-9]{9}"))
    private fun login(){val r=shell();header(r,"Secure sign in","Credit • assets • insurance • records • legacy");val m=input("Registered mobile number",true);val p=input("4–6 digit PIN",true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;filters=arrayOf(InputFilter.LengthFilter(6))};add(r,m,18);add(r,p);add(r,button("LOGIN WITH PIN + OTP"){val x=m.text.toString();if(!validMobile(x)||prefs.getString("pin_$x",null)!=p.text.toString()){Toast.makeText(this,"Enter a valid registered mobile and PIN",Toast.LENGTH_LONG).show();return@button};otpDialog("Secure login OTP",x){go(x)}});add(r,button("CREATE PROFILE / ACCOUNT",Color.rgb(0,145,135)){register()},10);add(r,button("RESET PIN WITH OTP",Color.rgb(90,105,120)){val x=m.text.toString();if(validMobile(x)&&prefs.contains("pin_$x"))otpDialog("Reset PIN OTP",x){resetPin(x)}else Toast.makeText(this,"Enter a registered mobile first",Toast.LENGTH_LONG).show()},10);screen(r)}
    private fun otpDialog(title:String,mobile:String,onVerified:()->Unit){val otp=(100000+Random.nextInt(900000)).toString();val code=input("Enter 6-digit OTP",true).apply{filters=arrayOf(InputFilter.LengthFilter(6))};val d=AlertDialog.Builder(this).setTitle(title).setMessage("Demo OTP: $otp\nProduction SMS gateway is required for live delivery.").setView(code).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();d.setOnShowListener{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(code.text.toString()==otp){d.dismiss();onVerified()}else code.error="Incorrect OTP"}};d.show()}
    private fun go(m:String){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",m).apply();startActivity(Intent(this,ArthSaathiHomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish()}
    private fun resetPin(x:String){val np=input("New 4–6 digit PIN",true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;filters=arrayOf(InputFilter.LengthFilter(6))};AlertDialog.Builder(this).setTitle("Set new PIN").setView(np).setPositiveButton("SAVE"){_,_->if(np.text.length in 4..6){prefs.edit().putString("pin_$x",np.text.toString()).apply();Toast.makeText(this,"PIN updated",Toast.LENGTH_LONG).show()}else Toast.makeText(this,"PIN must be 4–6 digits",Toast.LENGTH_LONG).show()}}.setNegativeButton("CANCEL",null).show()}
    private fun register(){val r=shell();header(r,"Create your profile","One verified profile for your ArthSaathi records");val n=input("Full name");val m=input("Mobile number",true);val p=input("Create 4–6 digit PIN",true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;filters=arrayOf(InputFilter.LengthFilter(6))};add(r,n,18);add(r,m);add(r,p);add(r,button("VERIFY MOBILE + CREATE ACCOUNT"){val x=m.text.toString();if(!validMobile(x)||n.text.toString().trim().length<2||p.text.length !in 4..6){Toast.makeText(this,"Complete valid profile details",Toast.LENGTH_LONG).show();return@button};otpDialog("Verify mobile",x){prefs.edit().putString("pin_$x",p.text.toString()).putString("name_$x",n.text.toString().trim()).apply();go(x)}});add(r,button("BACK TO SIGN IN",Color.rgb(90,105,120)){login()},10);screen(r)}
}
