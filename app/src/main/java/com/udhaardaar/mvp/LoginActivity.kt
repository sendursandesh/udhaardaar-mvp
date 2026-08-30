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
    private var otp = ""
    private var otpMobile = ""
    private var photoTarget: EditText? = null
    private val blue=Color.rgb(25,111,220); private val navy=Color.rgb(24,58,92); private val teal=Color.rgb(0,145,135); private val green=Color.rgb(25,145,78); private val pageBg=Color.rgb(238,248,253)
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun box(fill:Int=Color.WHITE,stroke:Int=Color.rgb(190,210,225))=GradientDrawable().apply{setColor(fill);setStroke(dp(1),stroke);cornerRadius=dp(16).toFloat()}
    private fun t(s:String,size:Float=16f,c:Int=navy)=TextView(this).apply{text=s;textSize=size;setTextColor(c);setPadding(dp(4),dp(4),dp(4),dp(4))}
    private fun f(h:String,max:Int=0,num:Boolean=false)=EditText(this).apply{hint=h;textSize=16f;setSingleLine(true);setPadding(dp(14),dp(8),dp(14),dp(8));background=box();if(max>0)filters=arrayOf(InputFilter.LengthFilter(max));if(num)inputType=InputType.TYPE_CLASS_NUMBER;imeOptions=EditorInfo.IME_ACTION_NEXT}
    private fun b(s:String,c:Int,click:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=14f;setTextColor(Color.WHITE);background=box(c,c);setOnClickListener{click()}}
    private fun base()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(20),dp(26),dp(20),dp(30));setBackgroundColor(pageBg)}
    private fun put(r:LinearLayout,v:View,h:Int=54){r.addView(v,LinearLayout.LayoutParams(-1,dp(h)).apply{setMargins(0,dp(4),0,dp(4))})}
    private fun show(r:LinearLayout){setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun logo(n:Int)=ImageView(this).apply{layoutParams=LinearLayout.LayoutParams(dp(n),dp(n));setImageResource(R.drawable.udhaardaar_logo);scaleType=ImageView.ScaleType.CENTER_INSIDE}
    override fun onCreate(state:Bundle?){super.onCreate(state);loginPage()}
    override fun onNewIntent(intent:Intent?){super.onNewIntent(intent);loginPage()}
    private fun loginPage(){val r=base();r.addView(logo(90));r.addView(t("Udhaardaar",30f).apply{gravity=Gravity.CENTER});r.addView(t("Your Credit. Your Trust. Our Record.",14f,teal).apply{gravity=Gravity.CENTER});r.addView(Space(this),LinearLayout.LayoutParams(1,dp(16)));r.addView(t("Login",23f));r.addView(t("Registered mobile number + PIN",13f,Color.DKGRAY));val m=f("Registered mobile number",10,true);val pin=f("4–6 digit PIN",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};put(r,m);put(r,pin);put(r,b("LOGIN",blue){val ms=m.text.toString().trim();if(ms.length!=10){toast("Enter a valid 10-digit mobile number");return@b};val saved=prefs.getString("pin_$ms",null);if(saved==null){toast("Account not found. Tap Create account.");return@b};if(saved!=pin.text.toString()){toast("Incorrect PIN");return@b};enter(ms)},52);val links=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER};links.addView(b("Forgot PIN / OTP",teal){otpLogin(m.text.toString().trim())},LinearLayout.LayoutParams(0,50,1f));links.addView(b("Create account",navy){registrationPage()},LinearLayout.LayoutParams(0,50,1f));r.addView(links,LinearLayout.LayoutParams(-1,dp(54)));show(r)}
    private fun registrationPage(){val r=base();r.addView(logo(70));r.addView(t("Create Account",23f));r.addView(t("Complete your account once. Then use mobile + PIN for login.",13f,Color.DKGRAY));val name=f("Full name *");val m=f("Mobile number * (10 digits)",10,true);val address=f("Full address *");val email=f("Email (optional)");val photo=f("Selected photo",400);photo.isFocusable=false;photo.visibility=View.GONE;put(r,name);put(r,m);put(r,address);put(r,email);put(r,b("ADD PROFILE PHOTO",teal){choosePhoto(photo)},52);put(r,photo,42);val p=f("Create 4–6 digit PIN *",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};val p2=f("Confirm PIN *",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};put(r,p);put(r,p2);val otpBox=f("Enter 6-digit OTP",6,true).apply{visibility=View.GONE};put(r,otpBox);put(r,b("SEND OTP",blue){val ms=m.text.toString().trim();if(name.text.trim().length<2||ms.length!=10||address.text.trim().length<5||p.text.length !in 4..6||p.text.toString()!=p2.text.toString()){toast("Complete name, address, mobile and matching PIN");return@b};if(prefs.contains("pin_$ms")){toast("This mobile is already registered. Please login.");return@b};otp=Random.nextInt(100000,1000000).toString();otpMobile=ms;otpBox.visibility=View.VISIBLE;toast("Demo OTP: $otp")},52);put(r,b("VERIFY OTP + CREATE ACCOUNT",green){if(otp.isBlank()||otpBox.text.toString()!=otp){toast("Enter the correct OTP");return@b};val ms=otpMobile;if(ms.isBlank()){toast("Send OTP first");return@b};prefs.edit().putString("pin_$ms",p.text.toString()).putString("name_$ms",name.text.toString().trim()).putString("address_$ms",address.text.toString().trim()).putString("email_$ms",email.text.toString().trim()).putString("photo_$ms",photo.text.toString()).apply();enter(ms)},54);put(r,b("BACK TO LOGIN",Color.rgb(90,110,125)){loginPage()},50);show(r)}
    private fun otpLogin(ms:String){if(ms.length!=10){toast("Enter your registered 10-digit mobile number first");return};if(!prefs.contains("pin_$ms")){toast("Mobile number is not registered");return};otp=Random.nextInt(100000,1000000).toString();otpMobile=ms;val input=f("Enter 6-digit OTP",6,true);val d=AlertDialog.Builder(this).setTitle("OTP verification").setMessage("Demo build OTP is displayed as a toast. Production will connect an SMS OTP provider.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();d.setOnShowListener{toast("Demo OTP: $otp");d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(input.text.toString()!=otp){input.error="Incorrect OTP"}else{d.dismiss();setNewPin(ms)}}};d.show()}
    private fun setNewPin(ms:String){val p=f("New 4–6 digit PIN",6,true);val p2=f("Confirm PIN",6,true);val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),0,dp(10),0);addView(p);addView(p2)};val d=AlertDialog.Builder(this).setTitle("Set new PIN").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("SAVE",null).create();d.setOnShowListener{d.getButton(-1).setOnClickListener{if(p.text.length !in 4..6||p.text.toString()!=p2.text.toString()){p2.error="PINs must match"}else{prefs.edit().putString("pin_$ms",p.text.toString()).apply();d.dismiss();enter(ms)}}};d.show()}
    private fun choosePhoto(target:EditText){photoTarget=target;startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="image/*";addCategory(Intent.CATEGORY_OPENABLE);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)},501)}
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==501&&resultCode==RESULT_OK&&data?.data!=null){val u=data.data!!;try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};photoTarget?.visibility=View.VISIBLE;photoTarget?.setText(u.toString())}}
    private fun enter(ms:String){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",ms).apply();startActivity(Intent(this,V4HomeActivity::class.java).putExtra("authenticated_mobile",ms))}
}
