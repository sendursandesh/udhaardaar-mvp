package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class V62HomeActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private val root by lazy {
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16),dp(12),dp(16),dp(24))
            setBackgroundColor(ArthSaathiV62Design.BG)
        }
    }
    private fun dp(v:Int)=(v*d).toInt()

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        render()
    }
    override fun onResume(){super.onResume();if(!isFinishing)render()}

    private fun card(module: ArthSaathiConsolidatedArchitecture.Module): LinearLayout =
        LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(11),dp(9),dp(9),dp(9))
            background=ArthSaathiV62Design.card(Color.WHITE,16,d)
            setOnClickListener{open(module)}
            addView(ArthSaathiV62Design.text(this@V62HomeActivity,module.title,11.5f,ArthSaathiV62Design.NAVY,true))
            addView(ArthSaathiV62Design.text(this@V62HomeActivity,module.category,8.5f,ArthSaathiV62Design.MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3)})
        }

    private fun open(module: ArthSaathiConsolidatedArchitecture.Module){
        when {
            module.entryPoint=="V62CreditRegistrationActivity" -> startActivity(Intent(this,V62CreditRegistrationActivity::class.java))
            module.entryPoint=="V62BureauActivity" -> startActivity(Intent(this,V62BureauActivity::class.java))
            module.entryPoint=="V62AssetVaultActivity" -> startActivity(Intent(this,V62AssetVaultActivity::class.java))
            module.entryPoint=="V62InsuranceActivity" -> startActivity(Intent(this,V62InsuranceActivity::class.java))
            module.entryPoint=="V62ChargeCheckActivity" -> startActivity(Intent(this,V62ChargeCheckActivity::class.java))
            module.entryPoint=="V62TTMMActivity" -> startActivity(Intent(this,V62TTMMActivity::class.java))
            module.entryPoint=="V62MISActivity" -> startActivity(Intent(this,V62MISActivity::class.java))
            module.entryPoint=="V62RentalLeaseActivity" -> startActivity(Intent(this,V62RentalLeaseActivity::class.java))
            module.entryPoint=="V62ChargesActivity" -> startActivity(Intent(this,V62ChargesActivity::class.java))
            module.entryPoint.startsWith("ArthSaathiModulesActivity:") -> {
                val mode=module.entryPoint.substringAfter(":")
                startActivity(Intent(this,ArthSaathiModulesActivity::class.java).putExtra("mode",mode))
            }
        }
    }

    private fun render(){
        root.removeAllViews()
        val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE)
        if(!p.getBoolean("logged_in",false)){
            startActivity(Intent(this,LoginActivity::class.java));finish();return
        }

        ArthSaathiV62Design.add(root,ArthSaathiV62Design.title(this,"ArthSaathi","Your Money. Your Records. Your Rights."),2)
        ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,"CONSOLIDATED V6.2 ARCHITECTURE",10f,ArthSaathiV62Design.GOLD,true),5)
        ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,"One master navigation tree. Register new credit separately from existing Loans & Udhaar records.",11f,ArthSaathiV62Design.MUTED),3)

        val errors=ArthSaathiConsolidatedArchitecture.verifyRegistry()
        val status=if(errors.isEmpty())"ARCHITECTURE CHECK: PASS" else "ARCHITECTURE CHECK: REVIEW REQUIRED"
        val statusColor=if(errors.isEmpty())ArthSaathiV62Design.GREEN else ArthSaathiV62Design.RED
        ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,status,11f,statusColor,true),10)

        val modules=ArthSaathiConsolidatedArchitecture.modules
        var i=0
        while(i<modules.size){
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            row.addView(card(modules[i]),LinearLayout.LayoutParams(0,dp(82),1f).apply{rightMargin=dp(4)})
            if(i+1<modules.size) row.addView(card(modules[i+1]),LinearLayout.LayoutParams(0,dp(82),1f).apply{leftMargin=dp(4)})
            else row.addView(Space(this),LinearLayout.LayoutParams(0,dp(82),1f))
            root.addView(row,LinearLayout.LayoutParams(-1,dp(82)).apply{if(i>0)topMargin=dp(6)})
            i+=2
        }

        ArthSaathiV62Design.add(root,ArthSaathiV62Design.text(this,
            "Core flow: Register Credit → party/terms → repayment calculation → guarantor → digital document → OTP consent → final registration. Existing credit accounts remain under Loans & Udhaar / records.",
            10f,ArthSaathiV62Design.MUTED),12)

        root.addView(Button(this).apply{
            text="LOG OUT"
            setOnClickListener{
                p.edit().clear().apply()
                startActivity(Intent(this@V62HomeActivity,LoginActivity::class.java))
                finish()
            }
        },LinearLayout.LayoutParams(-1,dp(52)).apply{topMargin=dp(10)})

        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
    }
}
