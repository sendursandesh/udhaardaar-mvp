package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val d get() = resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()

    override fun onCreate(b:Bundle?) {
        super.onCreate(b)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        if (prefs.getBoolean("logged_in",false)) { openHome(); return }
        showLogin()
    }

    private fun shell(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(16),dp(16),dp(16),dp(26))
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(ArthSaathiV7Design.NAVY, ArthSaathiV7Design.NAVY_2, ArthSaathiV7Design.CREAM))
    }

    private fun brandBlock(r:LinearLayout) {
        val box=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER_HORIZONTAL; setPadding(0,dp(14),0,dp(10)) }
        box.addView(ArthSaathiV7Design.logo(this,112))
        box.addView(ArthSaathiV7Design.brand(this,30f,true))
        box.addView(ArthSaathiV7Design.text(this,ArthSaathiV7Design.TAGLINE,12f,ArthSaathiV7Design.GOLD_2,true).apply { gravity=Gravity.CENTER })
        box.addView(ArthSaathiV7Design.text(this,ArthSaathiV7Design.PILLARS,10f,ArthSaathiV7Design.GOLD_PALE,true).apply { gravity=Gravity.CENTER })
        r.addView(box)
    }

    private fun showLogin() {
        val r=shell(); brandBlock(r)
        r.addView(ArthSaathiV7Design.text(this,"Welcome Back",21f,Color.WHITE,true).apply { gravity=Gravity.CENTER })
        r.addView(ArthSaathiV7Design.text(this,"Let’s build your financial clarity together.",10.5f,ArthSaathiV7Design.GOLD_PALE).apply { gravity=Gravity.CENTER },
            LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(3) })
        val card=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(14),dp(14),dp(14),dp(14)); background=ArthSaathiV7Design.card(this@LoginActivity,Color.WHITE,18) }
        val m=ArthSaathiV7Design.input(this,"Enter registered mobile number")
        card.addView(m)
        card.addView(ArthSaathiV7Design.goldButton(this,"SEND OTP") {
            val x=m.text.toString()
            if(!validMobile(x)){m.error="Enter a valid 10-digit mobile number";return@goldButton}
            if(!prefs.contains("name_$x")){showNoAccount(x);return@goldButton}
            otp("Secure login OTP",x){prefs.edit().putBoolean("logged_in",true).putString("current_mobile",x).apply();openHome()}
        },LinearLayout.LayoutParams(-1,dp(48)).apply { topMargin=dp(10) })
        card.addView(ArthSaathiV7Design.text(this,"or",10f,ArthSaathiV7Design.MUTED).apply { gravity=Gravity.CENTER },
            LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(9) })
        card.addView(ArthSaathiV7Design.outlineButton(this,"Create New Account"){showRegister()},
            LinearLayout.LayoutParams(-1,dp(46)).apply { topMargin=dp(7) })
        card.addView(ArthSaathiV7Design.text(this,"By continuing you agree to our Terms & Privacy Policy.",9f,ArthSaathiV7Design.MUTED).apply { gravity=Gravity.CENTER },
            LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(9) })
        r.addView(card,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(12) })
        setContentView(ScrollView(this).apply { isFillViewport=true; addView(r) })
    }

    private fun showNoAccount(mobile:String) {
        AlertDialog.Builder(this).setTitle("Account not found")
            .setMessage("No ArthSaathi account is registered for $mobile. Please create your account first.")
            .setPositiveButton("CREATE ACCOUNT"){_,_->showRegister(mobile)}
            .setNegativeButton("CANCEL",null).show()
    }

    private fun showRegister(prefill:String="") {
        val r=shell(); brandBlock(r)
        r.addView(ArthSaathiV7Design.text(this,"Create your ArthSaathi account",20f,Color.WHITE,true).apply { gravity=Gravity.CENTER })
        val card=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(14),dp(14),dp(14),dp(14)); background=ArthSaathiV7Design.card(this@LoginActivity,Color.WHITE,18) }
        val n=ArthSaathiV7Design.input(this,"Full name")
        val m=ArthSaathiV7Design.input(this,"Mobile number").apply { inputType=InputType.TYPE_CLASS_PHONE; filters=arrayOf(InputFilter.LengthFilter(10)) }
        if(prefill.isNotBlank())m.setText(prefill)
        card.addView(n)
        card.addView(m,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(7) })
        card.addView(ArthSaathiV7Design.goldButton(this,"VERIFY MOBILE + CREATE ACCOUNT"){
            val x=m.text.toString()
            if(n.text.trim().length<2||!validMobile(x)){Toast.makeText(this,"Enter name and valid 10-digit mobile.",Toast.LENGTH_LONG).show();return@goldButton}
            if(prefs.contains("name_$x")){Toast.makeText(this,"An account already exists. Please log in.",Toast.LENGTH_LONG).show();return@goldButton}
            otp("Verify mobile and create account",x){
                prefs.edit().putString("name_$x",n.text.toString().trim()).putBoolean("logged_in",true).putString("current_mobile",x).apply()
                openHome()
            }
        },LinearLayout.LayoutParams(-1,dp(48)).apply { topMargin=dp(10) })
        card.addView(ArthSaathiV7Design.outlineButton(this,"BACK TO LOGIN"){showLogin()},
            LinearLayout.LayoutParams(-1,dp(46)).apply { topMargin=dp(7) })
        r.addView(card,LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(10) })
        setContentView(ScrollView(this).apply { isFillViewport=true; addView(r) })
    }

    private fun validMobile(x:String)=x.matches(Regex("[6-9][0-9]{9}"))

    private fun otp(title:String,mobile:String,done:()->Unit) {
        val code=(100000+Random.nextInt(900000)).toString()
        val e=ArthSaathiV7Design.input(this,"Enter 6-digit OTP").apply { inputType=InputType.TYPE_CLASS_NUMBER; filters=arrayOf(InputFilter.LengthFilter(6)) }
        val dialog=AlertDialog.Builder(this).setTitle(title).setMessage("Demo OTP: $code").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create()
        dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if(e.text.toString()==code){dialog.dismiss();done()}else e.error="Incorrect OTP" } }
        dialog.show()
    }

    private fun openHome(){startActivity(Intent(this,V7HomeActivity::class.java));finish()}
}
