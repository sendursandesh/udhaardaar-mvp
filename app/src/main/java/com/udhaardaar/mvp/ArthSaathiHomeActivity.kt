package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*

class ArthSaathiHomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ArthSaathiSession.isLoggedIn(this)) {
            startActivity(Intent(this, ArthSaathiLoginActivity::class.java)); finish(); return
        }
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(28,30,28,28)}
        root.addView(TextView(this).apply{text="ArthSaathi";textSize=30f})
        root.addView(TextView(this).apply{text="Financial-Life Command Centre • Master 8.0";textSize=15f;setPadding(0,8,0,18)})
        val scroll=ScrollView(this); val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}; scroll.addView(body); root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        ArthSaathiArchitectureRegistry.modules.filter{it.id!=ArthSaathiNavigation.HOME}.groupBy{it.area}.forEach{(area,mods)->
            body.addView(TextView(this).apply{text=area;textSize=18f;setPadding(0,18,0,6)})
            mods.forEach{m->body.addView(Button(this).apply{text=m.title;setOnClickListener{startActivity(Intent(this@ArthSaathiHomeActivity,ArthSaathiMasterModuleActivity::class.java).putExtra("module",m.id))}})}
        }
        body.addView(Button(this).apply{text="LOG OUT";setOnClickListener{ArthSaathiSession.logout(this@ArthSaathiHomeActivity);finish();startActivity(Intent(this@ArthSaathiHomeActivity,ArthSaathiLoginActivity::class.java))}})
        setContentView(root)
    }
}