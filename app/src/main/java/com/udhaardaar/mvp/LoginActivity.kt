package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

/** V6.2 account gate styled to the approved ArthSaathi visual master. */
class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val d get() = resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()
    private fun input(h:String)=ArthSaathiV62Design.input(this,h).apply{
        inputType=InputType.TYPE_CLASS_PHONE
        filters=arrayOf(InputFilter.LengthFilter(10))
        imeOptions=android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
    }
    private fun button(s:String,fill:Int,click:()->Unit)=ArthSaathiV62Design.button(this,s,fill,click)

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        if(prefs.getBoolean("logged_in",false)){openHome();return}
        showLogin()
    }

    private fun shell()=LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL
        gravity=Gravity.CENTER_HORIZONTAL
        setPadding(dp(20),dp(14),dp(20),dp(26))
        background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xffffe8a6.toInt(),ArthSaathiV62Design.BG,0xfffffdf7.toInt()))
    }

    private fun logoBlock(r:LinearLayout){ r.addView(ArthSaathiV62Design.masthead(this,true)) }

    private fun tabs(r:LinearLayout){
        val t=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            background=ArthSaathiV62Design.card(0xfffff7df.toInt(),17,d)
            setPadding(dp(3),dp(3),dp(3),dp(3))
        }
        t.addView(button("Login",ArthSaathiV62Design.GOLD_DEEP){showLogin()},LinearLayout.LayoutParams(0,dp(46),1f).apply{rightMargin=dp(2)})
        t.addView(button("Sign Up",ArthSaathiV62Design.PALE_GOLD){showRegister()},LinearLayout.LayoutParams(0,dp(46),1f).apply{leftMargin=dp(2)})
        r.addView(t,LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(10)})
    }

    private fun showLogin(){
        val r=shell();logoBlock(r);tabs(r)
        val m=input("Enter registered mobile number")
        r.addView(m,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(13)})
        r.addView(button("SEND OTP",ArthSaathiV62Design.GOLD_DEEP){
            val x=m.text.toString()
            if(!validMobile(x)){Toast.makeText(this,"Enter a valid 10-digit Indian mobile number.",Toast.LENGTH_LONG).show();return@button}
            if(!prefs.contains("name_$x")){showNoAccount(x);return@button}
            otp("Secure login OTP",x){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",x).apply();openHome()}
        },LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(9)})
        r.addView(ArthSaathiV62Design.text(this,"or",10f,ArthSaathiV62Design.MUTED).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        r.addView(button("Continue with Google",ArthSaathiV62Design.WHITE){
            Toast.makeText(this,"Google sign-in will be connected in the production identity layer.",Toast.LENGTH_SHORT).show()
        },LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        r.addView(ArthSaathiV62Design.text(this,"By continuing you agree to our Terms & Privacy Policy.",9f,ArthSaathiV62Design.MUTED).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }

    private fun showNoAccount(mobile:String){
        AlertDialog.Builder(this).setTitle("Account not found")
            .setMessage("No ArthSaathi account is registered for $mobile. OTP login cannot create an account. Please create your account first.")
            .setPositiveButton("CREATE ACCOUNT"){_,_->showRegister(mobile)}
            .setNegativeButton("CANCEL",null).show()
    }

    private fun showRegister(prefill:String=""){
        val r=shell();logoBlock(r);tabs(r)
        r.addView(ArthSaathiV62Design.text(this,"Create your ArthSaathi account",18f,ArthSaathiV62Design.NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(12)})
        val n=ArthSaathiV62Design.input(this,"Full name")
        val m=input("Mobile number")
        if(prefill.isNotBlank())m.setText(prefill)
        r.addView(n,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        r.addView(m,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        r.addView(button("VERIFY MOBILE + CREATE ACCOUNT",ArthSaathiV62Design.GOLD_DEEP){
            val x=m.text.toString()
            if(n.text.trim().length<2||!validMobile(x)){Toast.makeText(this,"Enter name and valid 10-digit mobile.",Toast.LENGTH_LONG).show();return@button}
            if(prefs.contains("name_$x")){Toast.makeText(this,"An account already exists. Please log in.",Toast.LENGTH_LONG).show();return@button}
            otp("Verify mobile and create account",x){prefs.edit().putString("name_$x",n.text.toString().trim()).putBoolean("logged_in",true).putString("current_mobile",x).apply();openHome()}
        },LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(9)})
        r.addView(button("BACK TO LOGIN",ArthSaathiV62Design.NAVY){showLogin()},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }

    private fun validMobile(x:String)=x.matches(Regex("[6-9][0-9]{9}"))

    private fun otp(title:String,mobile:String,done:()->Unit){
        val code=(100000+Random.nextInt(900000)).toString()
        val e=ArthSaathiV62Design.input(this,"Enter 6-digit OTP").apply{inputType=InputType.TYPE_CLASS_NUMBER;filters=arrayOf(InputFilter.LengthFilter(6))}
        val dialog=AlertDialog.Builder(this).setTitle(title).setMessage("Demo OTP: $code").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create()
        dialog.setOnShowListener{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(e.text.toString()==code){dialog.dismiss();done()}else e.error="Incorrect OTP"}}
        dialog.show()
    }
    private fun openHome(){startActivity(Intent(this,V62HomeActivity::class.java));finish()}
}
