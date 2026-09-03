package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private fun page()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(40,40,40,40)}
    private fun input(h:String,digits:Boolean=false)=EditText(this).apply{hint=h;setSingleLine(true);setPadding(16,12,16,12);if(digits)inputType=InputType.TYPE_CLASS_NUMBER}
    private fun validMobile(x:String)=x.matches(Regex("[6-9][0-9]{9}"))
    private fun otpDialog(title:String,mobile:String,onVerified:()->Unit){
        val otp=(100000+Random.nextInt(900000)).toString()
        val code=input("Enter 6-digit OTP",true).apply{inputType=InputType.TYPE_CLASS_NUMBER;filters=arrayOf(android.text.InputFilter.LengthFilter(6))}
        val d=AlertDialog.Builder(this).setTitle(title).setMessage("Demo OTP: $otp\nUse the OTP sent to the registered mobile in production.").setView(code).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create()
        d.setOnShowListener{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(code.text.toString()==otp){d.dismiss();onVerified()}else code.error="Incorrect OTP"}}
        d.show()
    }
    private fun go(m:String){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",m).apply();startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish()}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);login()}
    private fun login(){
        val r=page();r.addView(TextView(this).apply{text="UDHAARDAAR";textSize=30f});r.addView(TextView(this).apply{text="Secure credit, repayment and asset records";textSize=15f})
        val m=input("Registered mobile number",true);val p=input("4-6 digit PIN",true);p.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;r.addView(m);r.addView(p)
        r.addView(Button(this).apply{text="LOGIN WITH PIN + OTP";setOnClickListener{val x=m.text.toString();val pin=p.text.toString();if(!validMobile(x)||prefs.getString("pin_$x",null)!=pin){Toast.makeText(this@LoginActivity,"Invalid mobile or PIN",Toast.LENGTH_LONG).show();return@setOnClickListener};otpDialog("Secure login — OTP required",x){go(x)}}})
        r.addView(Button(this).apply{text="CREATE PROFILE / ACCOUNT";setOnClickListener{register()}})
        r.addView(Button(this).apply{text="RESET PIN WITH OTP";setOnClickListener{val x=m.text.toString();if(validMobile(x)&&prefs.contains("pin_$x"))otpDialog("Reset PIN — OTP required",x){resetPin(x)}else Toast.makeText(this@LoginActivity,"Enter a registered mobile",Toast.LENGTH_LONG).show()}})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }
    private fun resetPin(x:String){val np=input("New 4-6 digit PIN",true);np.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;AlertDialog.Builder(this).setTitle("Set new PIN").setView(np).setPositiveButton("SAVE"){_,_->if(np.text.length in 4..6){prefs.edit().putString("pin_$x",np.text.toString()).apply();Toast.makeText(this,"PIN updated. Login with PIN + OTP.",Toast.LENGTH_LONG).show()}else Toast.makeText(this,"PIN must be 4-6 digits",Toast.LENGTH_LONG).show()}.setNegativeButton("CANCEL",null).show()}
    private fun register(){
        val r=page();r.addView(TextView(this).apply{text="CREATE PROFILE";textSize=24f});val n=input("Full name");val m=input("Mobile number",true);val p=input("Create 4-6 digit PIN",true);p.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD;r.addView(n);r.addView(m);r.addView(p)
        r.addView(Button(this).apply{text="SEND OTP + CREATE ACCOUNT";setOnClickListener{val x=m.text.toString();if(!validMobile(x)||n.text.toString().trim().length<2||p.text.length !in 4..6){Toast.makeText(this@LoginActivity,"Complete valid profile details",Toast.LENGTH_LONG).show();return@setOnClickListener};otpDialog("Verify mobile — account creation",x){prefs.edit().putString("pin_$x",p.text.toString()).putString("name_$x",n.text.toString().trim()).apply();go(x)}}})
        r.addView(Button(this).apply{text="BACK";setOnClickListener{login()}});setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }
}
