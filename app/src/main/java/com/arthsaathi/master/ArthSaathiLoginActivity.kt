package com.arthsaathi.master

import android.app.Activity
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout

class ArthSaathiLoginActivity:Activity(){
 override fun onCreate(b:Bundle?){super.onCreate(b)
  if(ArthSaathiSession.isLoggedIn(this)){home();return}
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,48,32,32);setBackgroundColor(ArthSaathiDesign.CREAM)}
  root.addView(ArthSaathiDesign.header(this,"ArthSaathi"))
  root.addView(ArthSaathiDesign.text(this,"Navigate Your Financial Journey",12f,false,ArthSaathiDesign.GOLD))
  val mobile=EditText(this).apply{hint="10-digit mobile number";inputType=InputType.TYPE_CLASS_PHONE;filters=arrayOf(InputFilter.LengthFilter(10))}
  root.addView(mobile)
  root.addView(ArthSaathiDesign.button(this,"Continue"){val v=mobile.text.toString();if(!v.matches(Regex("[6-9][0-9]{9}")))mobile.error="Enter valid 10-digit mobile number" else {ArthSaathiSession.login(this,v);home()}})
  setContentView(root)
 }
 private fun home(){startActivity(android.content.Intent(this,ArthSaathiHomeActivity::class.java));finish()}
}
