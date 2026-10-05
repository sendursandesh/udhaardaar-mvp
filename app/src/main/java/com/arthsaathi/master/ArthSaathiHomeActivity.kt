package com.arthsaathi.master

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView

class ArthSaathiHomeActivity:Activity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);if(!ArthSaathiSession.isLoggedIn(this)){login();return}
  val page=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,28,20,20);setBackgroundColor(ArthSaathiDesign.CREAM)}
  page.addView(ArthSaathiDesign.header(this,"ArthSaathi"))
  page.addView(ArthSaathiDesign.text(this,"Plan • Protect • Grow • Nominate",12f,true,ArthSaathiDesign.GOLD))
  page.addView(ArthSaathiDesign.text(this,"Your financial command centre",14f,false,ArthSaathiDesign.MUTED))
  ArthSaathiArchitectureRegistry.modules.forEach{m->
   val b=ArthSaathiDesign.button(this,m.title){open(m.route)}
   page.addView(b,LinearLayout.LayoutParams(-1,56).apply{topMargin=10})
  }
  val scroll=ScrollView(this);scroll.addView(page);setContentView(scroll)
 }
 private fun open(route:String){startActivity(android.content.Intent(this,ArthSaathiModuleActivity::class.java).putExtra("route",route))}
 private fun login(){startActivity(android.content.Intent(this,ArthSaathiLoginActivity::class.java));finish()}
}
