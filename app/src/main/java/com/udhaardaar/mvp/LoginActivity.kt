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
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val db by lazy { V32DatabaseHelper(this) }
    private var otp = ""
    private var otpMobile = ""
    private var photoTarget: EditText? = null
    private val blue = Color.rgb(25,111,220)
    private val navy = Color.rgb(24,58,92)
    private val teal = Color.rgb(0,145,135)
    private val green = Color.rgb(25,145,78)
    private val pageBg = Color.rgb(238,248,253)

    private fun dp(v:Int) = (v * resources.displayMetrics.density).toInt()
    private fun box(fill:Int = Color.WHITE) = GradientDrawable().apply { setColor(fill); setStroke(dp(1), Color.rgb(190,210,225)); cornerRadius = dp(16).toFloat() }
    private fun label(s:String, size:Float = 16f, c:Int = navy) = TextView(this).apply { text=s; textSize=size; setTextColor(c); setPadding(dp(4),dp(4),dp(4),dp(4)) }
    private fun field(h:String, max:Int=0, num:Boolean=false) = EditText(this).apply { hint=h; textSize=16f; setSingleLine(true); setPadding(dp(14),dp(8),dp(14),dp(8)); background=box(); if(max>0) filters=arrayOf(InputFilter.LengthFilter(max)); if(num) inputType=InputType.TYPE_CLASS_NUMBER }
    private fun btn(s:String,c:Int,fn:()->Unit) = Button(this).apply { text=s; isAllCaps=false; setTextColor(Color.WHITE); background=box(c); setOnClickListener { fn() } }
    private fun add(r:LinearLayout,v:View,h:Int=54) { r.addView(v,LinearLayout.LayoutParams(-1,dp(h)).apply { setMargins(0,dp(4),0,dp(4)) }) }
    private fun root() = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER_HORIZONTAL; setPadding(dp(20),dp(22),dp(20),dp(30)); setBackgroundColor(pageBg) }
    private fun show(r:LinearLayout) { setContentView(ScrollView(this).apply { isFillViewport=true; addView(r) }); window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) }
    private fun toast(s:String) = Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun validMobile(s:String) = Regex("^[6-9][0-9]{9}\$").matches(s)

    override fun onCreate(state:Bundle?) { super.onCreate(state); loginPage() }
    override fun onNewIntent(intent:Intent?) { super.onNewIntent(intent); loginPage() }

    private fun loginPage() {
        val r=root()
        r.addView(label("UDHAARDAAR",30f,teal)); r.addView(label("Personal Credit • Assets • Succession",14f)); r.addView(label("WELCOME",23f)); r.addView(label("Choose an option to continue",14f,Color.DKGRAY))
        val m=field("Registered mobile number (10 digits)",10,true)
        val pin=field("4–6 digit PIN",6,true).apply { inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        add(r,m); add(r,pin)
        add(r,btn("LOGIN",blue) { val x=m.text.toString(); when { !validMobile(x)->toast("Enter a valid 10-digit mobile number"); prefs.getString("pin_$x",null)==null->toast("Account not found — tap CREATE PROFILE below"); prefs.getString("pin_$x","")!=pin.text.toString()->toast("Incorrect PIN"); else->enter(x) } })
        add(r,btn("CREATE PROFILE / ACCOUNT",navy){registrationPage()},58)
        add(r,btn("FORGOT PIN / LOGIN WITH OTP",teal){otpLogin(m.text.toString())},52)
        r.addView(label("For this release candidate, OTP is a secure local test OTP. Live SMS requires a configured SMS gateway/backend.",12f,Color.DKGRAY)); show(r)
    }

    private fun registrationPage() {
        val r=root(); r.addView(label("CREATE PROFILE",24f)); r.addView(label("Enter details, send OTP, verify and create your profile.",13f,Color.DKGRAY))
        val name=field("Full name *"); val m=field("Mobile number *",10,true); val address=field("Full address *"); val photo=field("Selected photo",500); photo.isFocusable=false; photo.visibility=View.GONE
        add(r,name); add(r,m); add(r,address)
        add(r,btn("ADD PROFILE PHOTO",teal){ photoTarget=photo; startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type="image/*"; addCategory(Intent.CATEGORY_OPENABLE); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) },501) },52); add(r,photo,42)
        val p=field("Create 4–6 digit PIN",6,true).apply { inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        val p2=field("Confirm PIN",6,true).apply { inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        add(r,p); add(r,p2)
        val ob=field("Enter 6-digit OTP",6,true); ob.visibility=View.GONE; add(r,ob); val status=label("OTP not sent",13f,Color.DKGRAY); add(r,status,42)
        add(r,btn("SEND OTP",blue){
            val x=m.text.toString(); val ok=validMobile(x) && name.text.trim().length>=2 && address.text.trim().length>=5 && p.text.length in 4..6 && p.text.toString()==p2.text.toString()
            if(!ok){toast("Complete name, valid mobile, address and matching 4–6 digit PIN");return@btn}
            if(prefs.contains("pin_$x")){toast("Mobile already registered");return@btn}
            otp=Random.nextInt(100000,1000000).toString(); otpMobile=x; ob.visibility=View.VISIBLE; status.text="OTP generated and ready for verification"; status.setTextColor(green)
            AlertDialog.Builder(this).setTitle("OTP SENT").setMessage("Demo OTP: $otp\n\nEnter this 6-digit OTP below to create the profile. Live SMS delivery requires an SMS backend.").setPositiveButton("OK",null).show()
        },58)
        add(r,btn("VERIFY OTP + CREATE PROFILE",green){
            if(otpMobile.isBlank() || ob.text.toString()!=otp){ob.error="Enter the latest OTP";toast("Incorrect or missing OTP");return@btn}
            val mobile=otpMobile; val nm=name.text.toString().trim(); val ad=address.text.toString().trim(); val photoUri=photo.text.toString().ifBlank{null}
            prefs.edit().putString("pin_$mobile",p.text.toString()).putString("name_$mobile",nm).putString("address_$mobile",ad).putString("photo_$mobile",photoUri ?: "").apply()
            try { db.upsertProfile(null,"BORROWER","BOR-${System.currentTimeMillis()}",nm,mobile,"",ad,"","","","","","",photoUri) } catch(_:Exception) { }
            toast("Profile created successfully"); enter(mobile)
        },58)
        add(r,btn("BACK TO LOGIN",Color.DKGRAY){loginPage()},50); show(r)
    }

    private fun otpLogin(m:String) {
        if(!validMobile(m) || !prefs.contains("pin_$m")){toast("Enter a registered 10-digit mobile");return}
        otp=Random.nextInt(100000,1000000).toString(); otpMobile=m; val input=field("Enter 6-digit OTP",6,true)
        val d=AlertDialog.Builder(this).setTitle("LOGIN OTP SENT").setMessage("Demo OTP: $otp\n\nEnter the OTP below.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create()
        d.setOnShowListener { d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if(input.text.toString()!=otp) input.error="Incorrect OTP" else { d.dismiss(); resetPin(m) } } }; d.show()
    }
    private fun resetPin(m:String) { val np=field("New 4–6 digit PIN",6,true); val d=AlertDialog.Builder(this).setTitle("Reset PIN").setView(np).setPositiveButton("SAVE",null).setNegativeButton("CANCEL",null).create(); d.setOnShowListener { d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if(np.text.length !in 4..6) np.error="4–6 digits required" else { prefs.edit().putString("pin_$m",np.text.toString()).apply(); d.dismiss(); enter(m) } } }; d.show() }
    private fun enter(m:String) { prefs.edit().putBoolean("logged_in",true).putString("current_mobile",m).apply(); startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)) }
    override fun onActivityResult(req:Int,result:Int,data:Intent?) { super.onActivityResult(req,result,data); if(req==501 && result==RESULT_OK && data?.data!=null){ val u=data.data!!; try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){}; photoTarget?.visibility=View.VISIBLE; photoTarget?.setText(u.toString()) } }
}
