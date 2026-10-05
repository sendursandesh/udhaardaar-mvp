package com.arthsaathi.master

import android.app.Activity
import android.os.Bundle
import android.widget.*
import org.json.JSONObject

class ArthSaathiModuleActivity:Activity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);val route=intent.getStringExtra("route").orEmpty()
  when(route){ArthSaathiNavigation.REGISTER_CREDIT->register();ArthSaathiNavigation.LOANS_UDHAAR->loans();else->generic(route)}
 }
 private fun base(title:String)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,28,20,20);setBackgroundColor(ArthSaathiDesign.CREAM);addView(ArthSaathiDesign.header(this@ArthSaathiModuleActivity,title))}
 private fun register(){
  val root=base("Register Credit")
  root.addView(ArthSaathiDesign.text(this,"Creates a NEW credit only. Existing accounts belong in Loans & Udhaar.",13f,false,ArthSaathiDesign.MUTED))
  val nature=Spinner(this);nature.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,ArthSaathiArchitectureRegistry.creditNatures);root.addView(nature)
  val person=EditText(this).apply{hint="Borrower / counterparty name"};root.addView(person)
  val amount=EditText(this).apply{hint="Principal / amount";inputType=2};root.addView(amount)
  val roi=EditText(this).apply{hint="ROI % (e.g. 12.50)";inputType=8194};root.addView(roi)
  val months=EditText(this).apply{hint="Repayment months";inputType=2};root.addView(months)
  root.addView(ArthSaathiDesign.button(this,"Calculate & Register"){try{
   val p=amount.text.toString().toDouble();val r=roi.text.toString().ifBlank{"0"}.toDouble();val m=months.text.toString().ifBlank{"1"}.toInt()
   val plan=ArthSaathiFinancialRules.calculate(p,r,m,"EMI")
   val account=ArthSaathiWorkflowRepository().registerNewCredit(JSONObject().apply{put("nature",nature.selectedItem.toString());put("person",person.text.toString());put("principal",p);put("roi",r);put("months",m);put("emi",plan.emi);put("totalPayable",plan.total)})
   Toast.makeText(this,"Registered "+account.optString("accountId"),Toast.LENGTH_LONG).show();finish()
  }catch(e:Exception){Toast.makeText(this,e.message?:"Check fields",Toast.LENGTH_LONG).show()}})
  setContentView(ScrollView(this).apply{addView(root)})
 }
 private fun loans(){
  val root=base("Loans & Udhaar")
  root.addView(ArthSaathiDesign.text(this,"Previously registered accounts — active and closed.",13f,false,ArthSaathiDesign.MUTED))
  val accounts=ArthSaathiWorkflowRepository().existingCredits()
  if(accounts.isEmpty())root.addView(ArthSaathiDesign.text(this,"No registered credit accounts yet.",15f))
  accounts.forEach{account->
   root.addView(ArthSaathiDesign.button(this,account.optString("person","Unknown")+" • ₹"+account.optString("principal")+" • "+account.optString("status")){details(account)})
  }
  setContentView(ScrollView(this).apply{addView(root)})
 }
 private fun details(a:JSONObject){val root=base("Credit Account Details");root.addView(ArthSaathiDesign.text(this,a.toString(2),13f));setContentView(ScrollView(this).apply{addView(root)})}
 private fun generic(route:String){val root=base(ArthSaathiArchitectureRegistry.byRoute(route)?.title?:"ArthSaathi");root.addView(ArthSaathiDesign.text(this,"This module is registered in the consolidated architecture and is routed through the independent ArthSaathi layer.",14f));setContentView(root)}
}
