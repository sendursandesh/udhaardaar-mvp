package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val store by lazy { V5LocalStore(this) }
    private var otp = ""
    private var otpMobile = ""
    private var photoTarget: EditText? = null
    private val blue = Color.rgb(25,111,220)
    private val navy = Color.rgb(24,58,92)
    private val teal = Color.rgb(0,145,135)
    private val green = Color.rgb(25,145,78)
    private val pageBg = Color.rgb(238,248,253)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun box(fill: Int = Color.WHITE) = GradientDrawable().apply { setColor(fill); setStroke(dp(1), Color.rgb(190,210,225)); cornerRadius = dp(14).toFloat() }
    private fun label(s: String, size: Float = 15f, c: Int = navy) = TextView(this).apply { text=s; textSize=size; setTextColor(c); setPadding(dp(4),dp(3),dp(4),dp(3)) }
    private fun field(h: String, max: Int = 0, number: Boolean = false) = EditText(this).apply { hint=h; textSize=14f; minHeight=dp(50); setSingleLine(true); setPadding(dp(12),dp(6),dp(12),dp(6)); background=box(); if(max>0) filters=arrayOf(InputFilter.LengthFilter(max)); if(number) inputType=InputType.TYPE_CLASS_NUMBER; setOnFocusChangeListener{v,has->if(has)v.post{v.requestRectangleOnScreen(Rect(0,0,v.width,v.height),true)}} }
    private fun button(s:String,c:Int,fn:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=14f;minHeight=dp(48);setTextColor(Color.WHITE);background=box(c);setOnClickListener{fn()}}
    private fun add(root:LinearLayout,v:View,h:Int=50){root.addView(v,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(h)).apply{setMargins(0,dp(3),0,dp(3))})}
    private fun root()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(16),dp(16),dp(16),dp(24));setBackgroundColor(pageBg)}
    private fun showPage(root:LinearLayout){setContentView(ScrollView(this).apply{isFillViewport=true;isSmoothScrollingEnabled=true;addView(root)});window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)}
    private fun validMobile(s:String)=Regex("^[6-9][0-9]{9}$").matches(s.trim())
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    override fun onCreate(state:Bundle?){super.onCreate(state);loginPage()}
    private fun brand(r:LinearLayout){val id=resources.getIdentifier("udhaardaar_logo","drawable",packageName);if(id!=0)add(r,ImageView(this).apply{setImageResource(id);adjustViewBounds=true;scaleType=ImageView.ScaleType.CENTER_INSIDE},90);add(r,label("UDHAARDAAR",27f,teal),48);add(r,label("Personal Credit • Assets • Succession",13f,navy),34)}
    private fun loginPage(){val r=root();brand(r);add(r,label("WELCOME",21f),38);add(r,label("Secure access to your financial and identity vault",13f,Color.DKGRAY),38);val mobile=field("Registered mobile number (10 digits)",10,true);val pin=field("4–6 digit PIN",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};add(r,mobile);add(r,pin);add(r,button("LOGIN",blue){val m=mobile.text.toString().trim();when{!validMobile(m)->toast("Enter a valid 10-digit mobile number");!prefs.contains("pin_$m")->toast("Account not found — tap CREATE PROFILE below");prefs.getString("pin_$m","")!=pin.text.toString()->toast("Incorrect PIN");else->enter(m)}});add(r,button("CREATE PROFILE / ACCOUNT",navy){registrationPage()});add(r,button("FORGOT PIN / LOGIN WITH OTP",teal){otpLogin(mobile.text.toString().trim())});add(r,label("Demo OTP is displayed locally. Production requires an SMS backend.",11f,Color.DKGRAY),40);showPage(r)}
    private fun registrationPage(){val r=root();add(r,label("CREATE PROFILE",22f),42);add(r,label("Enter details, verify OTP and create your account.",12f,Color.DKGRAY),36);val name=field("Full name *");val mobile=field("Mobile number *",10,true);val address=field("Full address *");val photo=field("Selected photo",500);photo.isFocusable=false;photo.visibility=View.GONE;add(r,name);add(r,mobile);add(r,address);add(r,button("ADD PROFILE PHOTO",teal){photoTarget=photo;startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="image/*";addCategory(Intent.CATEGORY_OPENABLE);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)},501)});add(r,photo,42);val p1=field("Create 4–6 digit PIN",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};val p2=field("Confirm PIN",6,true).apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD};add(r,p1);add(r,p2);val otpField=field("Enter 6-digit OTP",6,true);otpField.visibility=View.GONE;add(r,otpField);val status=label("OTP not sent",12f,Color.DKGRAY);add(r,status,38);add(r,button("SEND OTP",blue){val m=mobile.text.toString().trim();val valid=validMobile(m)&&name.text.toString().trim().length>=2&&address.text.toString().trim().length>=5&&p1.text.length in 4..6&&p1.text.toString()==p2.text.toString();if(!valid){toast("Complete name, valid mobile, address and matching 4–6 digit PIN");return@button};if(prefs.contains("pin_$m")){toast("Mobile already registered");return@button};otp=Random.nextInt(100000,1000000).toString();otpMobile=m;otpField.visibility=View.VISIBLE;status.text="OTP generated and ready for verification";status.setTextColor(green);AlertDialog.Builder(this).setTitle("OTP SENT").setMessage("Demo OTP: $otp\n\nEnter the OTP below.").setPositiveButton("OK",null).show()});add(r,button("VERIFY OTP + CREATE PROFILE",green){if(otpMobile.isBlank()||otpField.text.toString()!=otp){otpField.error="Enter the latest OTP";toast("Incorrect or missing OTP");return@button};val m=otpMobile;val nm=name.text.toString().trim();val ad=address.text.toString().trim();val photoUri=photo.text.toString().trim();prefs.edit().putString("pin_$m",p1.text.toString()).putString("name_$m",nm).putString("address_$m",ad).putString("photo_$m",photoUri).apply();store.replace("profiles",JSONObject().apply{put("id","BOR-${System.currentTimeMillis()}");put("type","BORROWER");put("name",nm);put("mobile",m);put("address",ad);put("photoUri",photoUri);put("createdAt",System.currentTimeMillis())});toast("Profile created successfully");enter(m)});add(r,button("BACK TO LOGIN",Color.DKGRAY){loginPage()});showPage(r)}
    private fun otpLogin(m:String){if(!validMobile(m)||!prefs.contains("pin_$m")){toast("Enter a registered 10-digit mobile");return};otp=Random.nextInt(100000,1000000).toString();otpMobile=m;val input=field("Enter 6-digit OTP",6,true);val dialog=AlertDialog.Builder(this).setTitle("LOGIN OTP SENT").setMessage("Demo OTP: $otp\n\nEnter the OTP below.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();dialog.setOnShowListener{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(input.text.toString()!=otp)input.error="Incorrect OTP"else{dialog.dismiss();resetPin(m)}}};dialog.show()}
    private fun resetPin(m:String){val np=field("New 4–6 digit PIN",6,true);val dialog=AlertDialog.Builder(this).setTitle("Reset PIN").setView(np).setNegativeButton("CANCEL",null).setPositiveButton("SAVE",null).create();dialog.setOnShowListener{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(np.text.length !in 4..6)np.error="4–6 digits required"else{prefs.edit().putString("pin_$m",np.text.toString()).apply();dialog.dismiss();enter(m)}}};dialog.show()}
    private fun enter(m:String){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",m).apply();startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))}
    override fun onActivityResult(req:Int,result:Int,data:Intent?){super.onActivityResult(req,result,data);if(req==501&&result==RESULT_OK&&data?.data!=null){val u:Uri=data.data!!;try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};photoTarget?.visibility=View.VISIBLE;photoTarget?.setText(u.toString())}}
}
