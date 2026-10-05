package com.udhaardaar.mvp
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
class GroupKhataActivity : AppCompatActivity() {
 private val store by lazy { V5LocalStore(this) }
 private val members=mutableListOf<Pair<String,Double>>()
 private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
 private fun field(h:String)=EditText(this).apply{hint=h;textSize=14f;setSingleLine(true);minHeight=dp(48)}
 private fun add(r:LinearLayout,v:android.view.View){r.addView(v,LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT).apply{setMargins(0,dp(4),0,dp(4))})}
 private fun btn(t:String,c:Int,a:()->Unit)=Button(this).apply{text=t;isAllCaps=false;setTextColor(Color.WHITE);setBackgroundColor(c);setOnClickListener{a()}}
 override fun onCreate(b:Bundle?){super.onCreate(b);render()}
 private fun render(){
  val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(16),dp(18),dp(24));setBackgroundColor(0xFFF7F2E8.toInt())}
  add(r,TextView(this).apply{text="Group Khata";textSize=24f;setTextColor(0xFF17324D.toInt())})
  add(r,TextView(this).apply{text="Record shared expenses, calculate each member's contribution, track who owes whom and settle.";textSize=12f})
  val group=field("Group name *"); val payer=field("Who paid? *"); val expense=field("Expense amount ₹ *"); val note=field("Expense description")
  listOf(group,payer,expense,note).forEach{add(r,it)}
  add(r,btn("ADD MEMBER + SHARE",0xFF008F87.toInt()){addMember(r)})
  add(r,btn("SAVE GROUP EXPENSE",0xFF2E7D5B.toInt()){
   val amount=expense.text.toString().toDoubleOrNull()
   if(group.text.isBlank()||payer.text.isBlank()||amount==null||amount<=0||members.isEmpty()){Toast.makeText(this,"Enter group, payer, amount and at least one member share",Toast.LENGTH_LONG).show();return@btn}
   val total=members.sumOf{it.second}
   if(kotlin.math.abs(total-amount)>0.01){Toast.makeText(this,"Member shares must total ₹%.2f".format(amount),Toast.LENGTH_LONG).show();return@btn}
   val id="GK-${System.currentTimeMillis()}";val arr=JSONArray();members.forEach{arr.put(JSONObject().apply{put("member",it.first);put("share",it.second);put("settled",false)})}
   store.add("group_khata_expenses",JSONObject().apply{put("id",id);put("group",group.text.toString().trim());put("payer",payer.text.toString().trim());put("amount",amount);put("note",note.text.toString());put("members",arr.toString());put("status","OPEN");put("createdAt",System.currentTimeMillis())})
   Toast.makeText(this,"Group expense saved",Toast.LENGTH_LONG).show()
  })
  add(r,btn("WHO OWES WHOM / SETTLEMENT",0xFFC79A3B.toInt()){settlement()})
  add(r,btn("GROUP KHATA HISTORY",0xFF196FDC.toInt()){history()})
  add(r,btn("HOME",0xFF17324D.toInt()){finish()})
  setContentView(ScrollView(this).apply{addView(r)})
 }
 private fun addMember(r:LinearLayout){
  val n=field("Member name *");val s=field("Their share ₹ *");add(r,n);add(r,s)
  add(r,btn("ADD THIS MEMBER",0xFF607D8B.toInt()){val x=s.text.toString().toDoubleOrNull();if(n.text.isBlank()||x==null||x<0){s.error="Enter valid share";return@btn};members.add(n.text.toString().trim() to x);Toast.makeText(this,"Added ${n.text}",Toast.LENGTH_SHORT).show();n.isEnabled=false;s.isEnabled=false})
 }
 private fun settlement(){
  val all=store.all("group_khata_expenses")
  if(all.isEmpty()){AlertDialog.Builder(this).setTitle("Settlement").setMessage("No group expenses yet.").setPositiveButton("OK",null).show();return}
  val lines=all.takeLast(20).joinToString("\n\n"){e->val a=JSONArray(e.optString("members","[]"));val shares=(0 until a.length()).joinToString(", "){i->val x=a.getJSONObject(i);"${x.optString("member")}: ₹${x.optDouble("share")}"};"${e.optString("group")} • Paid by ${e.optString("payer")} • ₹${e.optDouble("amount")}\n$shares\nStatus: ${e.optString("status")}"}
  AlertDialog.Builder(this).setTitle("Who Owes Whom").setMessage(lines).setPositiveButton("MARK LAST AS SETTLED"){_,_->val e=all.last();e.put("status","SETTLED");e.put("settledAt",System.currentTimeMillis());store.replace("group_khata_expenses",e);Toast.makeText(this,"Settlement recorded",Toast.LENGTH_LONG).show()}.setNegativeButton("CLOSE",null).show()
 }
 private fun history(){val a=store.all("group_khata_expenses");AlertDialog.Builder(this).setTitle("Group Khata History").setMessage(if(a.isEmpty())"No history yet." else a.takeLast(30).joinToString("\n\n"){"${it.optString("group")} • ₹${it.optDouble("amount")} • ${it.optString("status")}"}).setPositiveButton("OK",null).show()}
}