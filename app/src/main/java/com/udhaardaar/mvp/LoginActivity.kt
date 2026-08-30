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
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private var otp = ""
    private var otpMobile = ""
    private var photoTarget: EditText? = null
    private val blue = Color.rgb(25,111,220)
    private val navy = Color.rgb(24,58,92)
    private val teal = Color.rgb(0,145,135)
    private val green = Color.rgb(25,145,78)
    private val pageBg = Color.rgb(238,248,253)

    private fun dp(v:Int) = (v * resources.displayMetrics.density).toInt()
    private fun box(fill:Int = Color.WHITE) = GradientDrawable().apply { setColor(fill); setStroke(dp(1),Color.rgb(190,210,225)); cornerRadius=dp(16).toFloat() }
    private fun text(s:String,size:Float=16f,c:Int=navy) = TextView(this).apply { text=s; textSize=size; setTextColor(c); setPadding(dp(4),dp(4),dp(4),dp(4)) }
    private fun field(h:String,max:Int=0,num:Boolean=false) = EditText(this).apply { hint=h; textSize=16f; setSingleLine(true); setPadding(dp(14),dp(8),dp(14),dp(8)); background=box(); if(max>0) filters=arrayOf(InputFilter.LengthFilter(max)); if(num) inputType=InputType.TYPE_CLASS_NUMBER; imeOptions=EditorInfo.IME_ACTION_NEXT }
    private fun button(s:String,c:Int,fn:()->Unit) = Button(this).apply { text=s; isAllCaps=false; setTextColor(Color.WHITE); background=box(c); setOnClickListener { fn() } }
    private fun base() = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER_HORIZONTAL; setPadding(dp(20),dp(26),dp(20),dp(30)); setBackgroundColor(pageBg) }
    private fun add(r:LinearLayout,v:View,h:Int=54) { r.addView(v,LinearLayout.LayoutParams(-1,dp(h)).apply { setMargins(0,dp(4),0,dp(4)) }) }
    private fun show(r:LinearLayout) { setContentView(ScrollView(this).apply { isFillViewport=true; addView(r) }); window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) }
    private fun toast(s:String) = Toast.makeText(this,s,Toast.LENGTH_LONG).show()

    override fun onCreate(b:Bundle?) { super.onCreate(b); loginPage() }
    override fun onNewIntent(i:Intent?) { super.onNewIntent(i); loginPage() }

    private fun loginPage() {
        val r=base()
        r.addView(text("UDHAARDAAR",30f,teal)); r.addView(text("Personal Credit • Assets • Succession",14f)); r.addView(text("LOGIN",23f))
        val m=field("Registered mobile number (10 digits)",10,true)
        val pin=field("4–6 digit PIN",6,true).apply { inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        add(r,m); add(r,pin)
        add(r,button("LOGIN",blue) {
            val x=m.text.toString()
            when {
                !Regex("^[6-9][0-9]{9}$").matches(x) -> toast("Enter a valid 10-digit mobile number")
                prefs.getString("pin_$x",null)==null -> toast("Account not found — use Create Account")
                prefs.getString("pin_$x","") != pin.text.toString() -> toast("Incorrect PIN")
                else -> enter(x)
            }
        })
        val row=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        row.addView(button("Forgot PIN / OTP",teal){ otpLogin(m.text.toString()) },LinearLayout.LayoutParams(0,54,1f))
        row.addView(button("Create Account",navy){ registrationPage() },LinearLayout.LayoutParams(0,54,1f))
        r.addView(row); show(r)
    }

    private fun registrationPage() {
        val r=base(); r.addView(text("CREATE ACCOUNT",23f))
        val name=field("Full name *"); val m=field("Mobile number *",10,true); val address=field("Full address *")
        val photo=field("Selected photo",500); photo.isFocusable=false; photo.visibility=View.GONE
        add(r,name); add(r,m); add(r,address)
        add(r,button("ADD PROFILE PHOTO",teal) {
            photoTarget=photo
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type="image/*"; addCategory(Intent.CATEGORY_OPENABLE); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) },501)
        },52)
        add(r,photo,42)
        val p=field("Create 4–6 digit PIN",6,true).apply { inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        val p2=field("Confirm PIN",6,true).apply { inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        add(r,p); add(r,p2)
        val ob=field("6-digit OTP",6,true).apply { visibility=View.GONE }; add(r,ob)
        add(r,button("SEND OTP",blue) {
            val x=m.text.toString()
            val valid=Regex("^[6-9][0-9]{9}$").matches(x) && name.text.trim().length>=2 && address.text.trim().length>=5 && p.text.length in 4..6 && p.text.toString()==p2.text.toString()
            if(!valid){ toast("Complete required fields correctly"); return@button }
            if(prefs.contains("pin_$x")){ toast("Mobile already registered"); return@button }
            otp=Random.nextInt(100000,1000000).toString(); otpMobile=x; ob.visibility=View.VISIBLE; toast("Demo OTP: $otp")
        },52)
        add(r,button("VERIFY OTP + CREATE",green) {
            if(ob.text.toString()!=otp || otpMobile.isBlank()){ toast("Incorrect or missing OTP"); return@button }
            prefs.edit().putString("pin_$otpMobile",p.text.toString()).putString("name_$otpMobile",name.text.toString()).putString("address_$otpMobile",address.text.toString()).putString("photo_$otpMobile",photo.text.toString()).apply()
            enter(otpMobile)
        })
        add(r,button("BACK TO LOGIN",Color.DKGRAY){loginPage()},50); show(r)
    }

    private fun otpLogin(m:String) {
        if(!Regex("^[6-9][0-9]{9}$").matches(m) || !prefs.contains("pin_$m")){ toast("Enter a registered 10-digit mobile"); return }
        otp=Random.nextInt(100000,1000000).toString(); otpMobile=m
        val i=field("6-digit OTP",6,true)
        val d=AlertDialog.Builder(this).setTitle("OTP verification").setMessage("Demo OTP is shown in a toast. Production SMS provider is a backend dependency.").setView(i).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create()
        d.setOnShowListener {
            toast("Demo OTP: $otp")
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if(i.text.toString()!=otp){ i.error="Incorrect OTP"; return@setOnClickListener }
                d.dismiss(); resetPin(m)
            }
        }; d.show()
    }

    private fun resetPin(m:String) {
        val np=field("New 4–6 digit PIN",6,true)
        val d=AlertDialog.Builder(this).setTitle("Reset PIN").setView(np).setPositiveButton("SAVE",null).setNegativeButton("CANCEL",null).create()
        d.setOnShowListener { d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if(np.text.length !in 4..6) np.error="4–6 digits required" else { prefs.edit().putString("pin_$m",np.text.toString()).apply(); d.dismiss(); enter(m) } } }; d.show()
    }

    private fun enter(m:String) { prefs.edit().putBoolean("logged_in",true).putString("current_mobile",m).apply(); startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)) }

    override fun onActivityResult(r:Int,c:Int,d:Intent?) { super.onActivityResult(r,c,d); if(r==501 && c==RESULT_OK && d?.data!=null){ val u=d.data!!; try{contentResolver.takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){}; photoTarget?.visibility=View.VISIBLE; photoTarget?.setText(u.toString()) } }
}
