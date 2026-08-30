package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private var otp = ""; private var otpMobile = ""; private var photoTarget: EditText? = null
    private val blue=Color.rgb(25,111,220); private val navy=Color.rgb(24,58,92); private val teal=Color.rgb(0,145,135); private val green=Color.rgb(25,145,78); private val pageBg=Color.rgb(238,248,253)
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun box(fill:Int=Color.WHITE)=GradientDrawable().apply{setColor(fill);setStroke(dp(1),Color.rgb(190,210,225));cornerRadius=dp(16).toFloat()}
    private fun t(s:String,size:Float=16f,c:Int=navy)=TextView(this).apply{text=s;textSize=size;setTextColor(c);setPadding(dp(4),dp(4),dp(4),dp(4))}
    private fun f(h:String,max:Int=0,num:Boolean=false)=EditText(this).apply{hint=h;textSize=16f;setSingleLine(true);setPadding(dp(14),dp(8),dp(14),dp(8));background=box();if(max>0)filters=arrayOf(InputFilter.LengthFilter(max));if(num)inputType=InputType.TYPE_CLASS_NUMBER;imeOptions=EditorInfo.IME_ACTION_NEXT}
    private fun b(s:String,c:Int,fn:()->Unit)=Button(this).apply{text=s;isAllCaps=false;setTextColor(Color.WHITE);background=box(c);setOnClickListener{fn()}}
    private fun base()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(20),dp(26),dp(20),dp(30));setBackgroundColor(pageBg)}
    private fun put(r:LinearLayout,v:View,h:Int=54){r.addView(v,LinearLayout.LayoutParams(-1,dp(h)).apply{setMargins(0,dp(4),0,dp(4))})}
    private fun show(r:LinearLayout){setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)});window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    override fun onCreate(b:Bundle?){super.onCreate(b);loginPage()}
    override fun onNewIntent(i:Intent?){super.onNewIntent(i);loginPage()}
    private fun loginPage(){val r=base();r.addView(t("UDHAARDAAR",30f,teal));r.addView(t("Personal Credit • Assets • Succession",14f));r.addView(t("LOGIN",23f));val m=f("Registered mobile number (10 digits)",10,true);val pin=f("4–6 digit PIN",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};put(r,m);put(r,pin);put(r,b("LOGIN",blue){val x=m.text.toString();when{!Regex("^[6-9][0-9]{9}$").matches(x)->toast("Enter a valid 10-digit mobile number");prefs.getString("pin_$x",null)==null->toast("Account not found — use Create Account");prefs.getString("pin_$x","")!=pin.text.toString()->toast("Incorrect PIN");else->enter(x)}},54);val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};row.addView(b("Forgot PIN / OTP",teal){otpLogin(m.text.toString())},LinearLayout.LayoutParams(0,54,1f));row.addView(b("Create Account",navy){registrationPage()},LinearLayout.LayoutParams(0,54,1f));r.addView(row);show(r)}
    private fun registrationPage(){val r=base();r.addView(t("CREATE ACCOUNT",23f));val name=f("Full name *");val m=f("Mobile number *",10,true);val address=f("Full address *");val photo=f("Selected photo",500);photo.isFocusable=false;photo.visibility=View.GONE;put(r,name);put(r,m);put(r,address);put(r,b("ADD PROFILE PHOTO",teal){photoTarget=photo;startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="image/*";addCategory(Intent.CATEGORY_OPENABLE);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)},501)},52);put(r,photo,42);val p=f("Create 4–6 digit PIN",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};val p2=f("Confirm PIN",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};put(r,p);put(r,p2);val ob=f("6-digit OTP",6,true).apply{visibility=View.GONE};put(r,ob);put(r,b("SEND OTP",blue){val x=m.text.toString();if(!Regex("^[6-9][0-9]{9}$").matches(x)||name.text.trim().length<2||address.text.trim().length<5||p.text.length !in 4..6||p.text.toString()!=p2.text.toString()){toast("Complete required fields correctly");return@b};if(prefs.contains("pin_$x")){toast("Mobile already registered");return@b};otp=Random.nextInt(100000,1000000).toString();otpMobile=x;ob.visibility=View.VISIBLE;toast("Demo OTP: $otp")},52);put(r,b("VERIFY OTP + CREATE",green){if(ob.text.toString()!=otp||otpMobile.isBlank()){toast("Incorrect or missing OTP");return@b};prefs.edit().putString("pin_$otpMobile",p.text.toString()).putString("name_$otpMobile",name.text.toString()).putString("address_$otpMobile",address.text.toString()).putString("photo_$otpMobile",photo.text.toString()).apply();enter(otpMobile)},54);put(r,b("BACK TO LOGIN",Color.DKGRAY){loginPage()},50);show(r)}
    private fun otpLogin(m:String){if(!Regex("^[6-9][0-9]{9}$").matches(m)||!prefs.contains("pin_$m")){toast("Enter a registered 10-digit mobile");return};otp=Random.nextInt(100000,1000000).toString();otpMobile=m;val i=f("6-digit OTP",6,true);val d=AlertDialog.Builder(this).setTitle("OTP verification").setMessage("Demo OTP is shown in a toast. Production SMS provider is a backend dependency.").setView(i).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();d.setOnShowListener{toast("Demo OTP: $otp");d.getButton(-1).setOnClickListener{if(i.text.toString()!=otp)i.error="Incorrect OTP"else{d.dismiss();val np=f("New 4–6 digit PIN",6,true);val d2=AlertDialog.Builder(this).setTitle("Reset PIN").setView(np).setPositiveButton("SAVE",null).setNegativeButton("CANCEL",null).create();d2.setOnShowListener{d2.getButton(-1).setOnClickListener{if(np.text.length !in 4..6)np.error="4–6 digits required"else{prefs.edit().putString("pin_$m",np.text.toString()).apply();d2.dismiss();enter(m)}}};d2.show()}}};d.show()}
    private fun enter(m:String){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",m).apply();startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))}
    override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(r==501&&c==RESULT_OK&&d?.data!=null){val u=d.data!!;try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};photoTarget?.visibility=View.VISIBLE;photoTarget?.setText(u.toString())}}
}
