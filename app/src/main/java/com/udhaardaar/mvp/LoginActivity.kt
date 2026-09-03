package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private var otp = ""
    private fun input(h:String, digits:Boolean=false)=EditText(this).apply { hint=h; setSingleLine(true); if(digits) inputType=InputType.TYPE_CLASS_NUMBER }
    private fun page()=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(40,40,40,40) }
    private fun go(m:String){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",m).apply();startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish()}
    override fun onCreate(b:Bundle?){super.onCreate(b);login()}
    private fun login(){
        val r=page();r.addView(TextView(this).apply{text="UDHAARDAAR";textSize=30f});r.addView(TextView(this).apply{text="Secure credit, repayment and asset records"})
        val m=input("Registered mobile number",true);val p=input("4-6 digit PIN",true);p.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;r.addView(m);r.addView(p)
        r.addView(Button(this).apply{text="LOGIN";setOnClickListener{val x=m.text.toString();val ok=x.matches(Regex("[6-9][0-9]{9}"))&&prefs.getString("pin_$x",null)==p.text.toString();if(ok)go(x)else Toast.makeText(this@LoginActivity,"Invalid mobile or PIN",Toast.LENGTH_LONG).show()}})
        r.addView(Button(this).apply{text="CREATE PROFILE / ACCOUNT";setOnClickListener{register()}})
        r.addView(Button(this).apply{text="LOGIN / RESET WITH OTP";setOnClickListener{val x=m.text.toString();if(x.matches(Regex("[6-9][0-9]{9}"))){otp=Random.nextInt(100000,1000000).toString();val code=input("Enter OTP",true);AlertDialog.Builder(this@LoginActivity).setTitle("Demo OTP").setMessage("OTP: $otp").setView(code).setPositiveButton("VERIFY"){_,_->go(x)}.setNegativeButton("CANCEL",null).show()}else Toast.makeText(this@LoginActivity,"Enter a valid mobile",Toast.LENGTH_LONG).show()}})
        setContentView(ScrollView(this).apply{addView(r)})
    }
    private fun register(){
        val r=page();r.addView(TextView(this).apply{text="CREATE PROFILE";textSize=24f});val n=input("Full name");val m=input("Mobile number",true);val p=input("Create 4-6 digit PIN",true);p.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;r.addView(n);r.addView(m);r.addView(p)
        r.addView(Button(this).apply{text="SEND OTP";setOnClickListener{val x=m.text.toString();if(!x.matches(Regex("[6-9][0-9]{9}"))||n.text.toString().trim().length<2||p.text.length !in 4..6){Toast.makeText(this@LoginActivity,"Complete valid profile details",Toast.LENGTH_LONG).show()}else{otp=Random.nextInt(100000,1000000).toString();AlertDialog.Builder(this@LoginActivity).setTitle("Demo OTP").setMessage("OTP: $otp").setPositiveButton("OK",null).show()}}})
        r.addView(Button(this).apply{text="CREATE + LOGIN";setOnClickListener{val x=m.text.toString();if(x.matches(Regex("[6-9][0-9]{9}"))&&n.text.toString().trim().length>=2&&p.text.length in 4..6){prefs.edit().putString("pin_$x",p.text.toString()).putString("name_$x",n.text.toString().trim()).apply();go(x)}else Toast.makeText(this@LoginActivity,"Complete the profile",Toast.LENGTH_LONG).show()}})
        r.addView(Button(this).apply{text="BACK";setOnClickListener{login()}});setContentView(ScrollView(this).apply{addView(r)})
    }
}
