package com.udhaardaar.mvp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class V5RepaymentActivity:AppCompatActivity(){
 private val service by lazy{V5RepaymentService(this)}
 private val store by lazy{V5LocalStore(this)}
 private val bg=Color.rgb(238,248,253);private val navy=Color.rgb(24,58,92);private val teal=Color.rgb(0,145,135);private val green=Color.rgb(25,145,78)
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 private fun box(c:Int=Color.WHITE)=android.graphics.drawable.GradientDrawable().apply{setColor(c);setStroke(dp(1),Color.rgb(205,218,228));cornerRadius=dp(14).toFloat()}
 private fun e(h:String)=EditText(this).apply{hint=h;textSize=15f;setSingleLine(true);minHeight=dp(50);setPadding(dp(12),dp(7),dp(12),dp(7));background=box();imeOptions=android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;setOnFocusChangeListener{v,has->if(has)v.post{v.requestRectangleOnScreen(android.graphics.Rect(0,0,v.width,v.height),true)}}}
 private fun add(r:LinearLayout,v:View){r.addView(v,LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT).apply{setMargins(0,dp(4),0,dp(4))})}
 private fun b(s:String,c:Int,fn:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=14f;setTextColor(Color.WHITE);background=box(c);minHeight=dp(50);setOnClickListener{fn()}}
 private val dateFmt=SimpleDateFormat("dd MMM yyyy",Locale.US)
 override fun onCreate(x:Bundle?){super.onCreate(x);window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
 private fun show(){
  val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(24));setBackgroundColor(bg)}
  add(r,TextView(this).apply{text="UDHAARDAAR V5";textSize=12f;setTextColor(teal)})
  add(r,TextView(this).apply{text="Repayment Centre";textSize=22f;setTextColor(navy)})
  add(r,TextView(this).apply{text="Choose the registered credit. Direction and party authorization are derived from the credit; informal repayments require counterparty consent.";textSize=12f;setTextColor(Color.DKGRAY)})
  val allCredits=store.all("credits")
  val credit=Spinner(this);credit.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,if(allCredits.isEmpty())listOf("No registered credits yet")else allCredits.map{j->"${j.optString("id")} • ${j.optString("borrower","Counterparty")} • ${directionOf(j)} • ₹${money(j.optDouble("amount",0.0))}"});add(r,TextView(this).apply{text="SELECT CREDIT";textSize=12f;setTextColor(teal)});add(r,credit)
  val details=TextView(this).apply{background=box();setPadding(dp(12),dp(10),dp(12),dp(10));textSize=13f;setTextColor(navy)};add(r,details)
  val schedule=TextView(this).apply{background=box();setPadding(dp(12),dp(10),dp(12),dp(10));textSize=13f;setTextColor(navy)};add(r,schedule)
  fun refresh(){val c=allCredits.getOrNull(credit.selectedItemPosition);if(c==null){details.text="No credit selected";schedule.text="No repayment schedule";return};val rs=store.all("repayments").filter{it.optString("creditId")==c.optString("id")};val paid=rs.sumOf{it.optDouble("amount",0.0)};details.text="Credit type: ${c.optString("creditType","-")}\nDirection: ${directionOf(c)}\nOriginal principal: ₹${money(c.optDouble("amount",0.0))}\nROI: ${c.optDouble("roi",0.0)}%\nRepayment mode: ${c.optString("repaymentMethod","-")}\nInstallment: ₹${money(c.optDouble("installmentAmount",0.0))}\nInstallments: ${c.optInt("installments",0)}\nStart: ${c.optString("start","-")}\nEnd: ${c.optString("end","-")}\nTotal payable: ₹${money(c.optDouble("totalPayable",0.0))}\nTotal paid: ₹${money(paid)}\nOutstanding: ₹${money(c.optDouble("outstanding",c.optDouble("totalPayable",0.0)))}\nStatus: ${c.optString("status","-")}";schedule.text=scheduleText(c)}
  credit.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long)=refresh();override fun onNothingSelected(p:AdapterView<*>?) {}};refresh()
  val kind=Spinner(this);kind.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("INFORMAL","FORMAL"));add(r,kind)
  val mode=Spinner(this);mode.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("EMI","PRINCIPAL_PLUS_INTEREST","BULLET_PRINCIPAL_ONLY","BULLET_INTEREST_MONTHLY","BULLET_PRINCIPAL_PLUS_INTEREST_AT_END"));add(r,mode)
  val amount=e("Repayment amount ₹ *");val date=e("Payment date *");val ref=e("Transaction reference");val evidence=e("Bank / account evidence reference");listOf(amount,date,ref,evidence).forEach{add(r,it)};add(r,b("PICK PAYMENT DATE",teal){pickDate(date)})
  add(r,b("RECORD REPAYMENT",green){
   val c=allCredits.getOrNull(credit.selectedItemPosition);val a=amount.text.toString().toDoubleOrNull()
   if(c==null){toast("Select a registered credit");return@b};if(a==null||a<=0){amount.error="Enter a positive amount";return@b}
   val out=c.optDouble("outstanding",c.optDouble("totalPayable",c.optDouble("amount",0.0)));if(a>out+0.01){amount.error="Cannot exceed outstanding ₹${money(out)}";return@b};if(date.text.isBlank()){date.error="Payment date required";return@b}
   if(kind.selectedItem.toString()=="FORMAL"&&evidence.text.isBlank()){evidence.error="Bank/account evidence required";return@b}
   if(kind.selectedItem.toString()=="INFORMAL"){
    try{val req=service.requestForCurrentUser(c.optString("id"),a,date.text.toString(),mode.selectedItem.toString(),ref.text.toString(),evidence.text.toString());val code=store.find("repayment_requests",req)?.optString("otp","")?:"";val input=e("Enter 6-digit OTP");val d=AlertDialog.Builder(this).setTitle("Counterparty consent required").setMessage("Repayment: ₹${money(a)}\nDemo OTP: $code").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();d.setOnShowListener{d.getButton(-1).setOnClickListener{if(service.confirm(req,input.text.toString(),code)){d.dismiss();toast("Repayment confirmed and ledger updated");show()}else input.error="Incorrect OTP"}};d.show()}catch(ex:Exception){toast(ex.message?:"Unable to create repayment request")}
   }else{
    val accounts=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE);val mobile=accounts.getString("current_mobile","").orEmpty();val borrowerMobile=c.optString("borrowerMobile");val lenderMobile=c.optString("lenderMobile");if(mobile!=borrowerMobile&&mobile!=lenderMobile){toast("This account is not a party to the selected credit");return@b}
    val counterparty=if(mobile==borrowerMobile)c.optString("lenderMobile") else c.optString("borrowerMobile")
    store.add("repayments",JSONObject().apply{put("id","LED-${System.currentTimeMillis()}");put("creditId",c.optString("id"));put("counterparty",counterparty);put("amount",a);put("date",date.text.toString());put("method",mode.selectedItem.toString());put("direction",directionOf(c));put("evidence",evidence.text.toString());put("status","CONFIRMED_FORMAL");put("consent","NOT_REQUIRED");put("createdAt",System.currentTimeMillis())});val newOut=(out-a).coerceAtLeast(0.0);c.put("outstanding",newOut);c.put("status",if(newOut<=0.01)"SETTLED" else "ACTIVE");store.replace("credits",c);toast("Formal repayment recorded and credit outstanding updated");show()
   }
  })
  add(r,TextView(this).apply{text="REPAYMENT VIEWS";textSize=12f;setTextColor(navy)})
  val filter=Spinner(this);filter.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("All","Payable","Receivable","Due","Overdue","Pending Consent","Settled"));add(r,filter)
  add(r,b("VIEW FILTERED HISTORY",navy){val f=filter.selectedItem.toString();val rows=(store.all("repayments")+store.all("repayment_requests")).filter{matchesFilter(it,f)}.sortedByDescending{it.optLong("createdAt",0)};val msg=rows.joinToString("\n\n"){j->historyLine(j)}.ifBlank{"No records match: $f"};AlertDialog.Builder(this).setTitle("$f repayment history").setMessage(msg).setPositiveButton("OK",null).show()})
  add(r,b("BACK",navy){finish()});setContentView(ScrollView(this).apply{isFillViewport=true;isSmoothScrollingEnabled=true;addView(r)})
 }
 private fun historyLine(j:JSONObject):String{val id=j.optString("creditId","-");val dir=j.optString("direction","-");val amt=money(j.optDouble("amount",0.0));val date=j.optString("date","-");val status=j.optString("status","-");val consent=j.optString("consent","-");return id+" • "+dir+"\n₹"+amt+" • "+date+"\nStatus: "+status+" • Consent: "+consent}
 private fun directionOf(c:JSONObject)=if(c.optString("direction")=="Credit Given")"RECEIVABLE" else "PAYABLE"
 private fun money(v:Double)=String.format(Locale.US,"%,.2f",v)
 private fun scheduleText(c:JSONObject):String{
  val rows=store.all("repayment_schedule").filter{it.optString("creditId")==c.optString("id")}.sortedBy{it.optInt("installmentNo",0)}
  if(rows.isEmpty())return "REPAYMENT SCHEDULE\nNo generated schedule found for this credit."
  val today=Calendar.getInstance().time
  val lines=ArrayList<String>()
  lines.add("REPAYMENT SCHEDULE")
  for(j in rows){
   val status=if(j.optString("status")=="PAID")"PAID" else try{if(dateFmt.parse(j.optString("dueDate"))?.before(today)==true)"OVERDUE" else "DUE/PENDING"}catch(_:Exception){j.optString("status","PENDING")}
   lines.add(j.optInt("installmentNo").toString()+". "+j.optString("dueDate")+" • ₹"+money(j.optDouble("dueAmount",0.0))+" • "+status)
  }
  return lines.joinToString("\n")
 }
 private fun isScheduleOverdue(creditId:String):Boolean{val today=Calendar.getInstance().time;return store.all("repayment_schedule").any{it.optString("creditId")==creditId&&it.optString("status")!="PAID"&&try{dateFmt.parse(it.optString("dueDate"))?.before(today)==true}catch(_:Exception){false}}}
 private fun isScheduleDue(creditId:String):Boolean{val today=Calendar.getInstance().time;return store.all("repayment_schedule").any{it.optString("creditId")==creditId&&it.optString("status")!="PAID"&&try{!dateFmt.parse(it.optString("dueDate"))!!.after(today)}catch(_:Exception){false}}}
 private fun matchesFilter(j:JSONObject,f:String):Boolean{
  val dir=j.optString("direction")
  val creditId=j.optString("creditId")
  return when(f){
   "Payable"->dir=="PAYABLE"
   "Receivable"->dir=="RECEIVABLE"
   "Pending Consent"->j.optString("status")=="COUNTERPARTY_OTP_PENDING"
   "Settled"->setOf("SETTLED","CONFIRMED","CONFIRMED_FORMAL").contains(j.optString("status"))
   "Due"->if(creditId.isBlank())false else isScheduleDue(creditId)&&!isScheduleOverdue(creditId)&&j.optString("status")!="SETTLED"
   "Overdue"->if(creditId.isBlank())false else isScheduleOverdue(creditId)&&j.optString("status")!="SETTLED"
   else->true
  }
 }
 private fun pickDate(t:EditText){val c=Calendar.getInstance();DatePickerDialog(this,{_,y,m,d->t.setText(String.format(Locale.US,"%02d %s %04d",d,SimpleDateFormat("MMM",Locale.US).format(Calendar.getInstance().apply{set(Calendar.MONTH,m)}.time),y))},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}
 private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
