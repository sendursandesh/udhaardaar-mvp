package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.random.Random

class V323Activity : AppCompatActivity() {
    private lateinit var db: V32DatabaseHelper
    private val prefs by lazy { getSharedPreferences("udhaardaar_v32", MODE_PRIVATE) }
    private val history = ArrayDeque<View>()
    private var photoUri: Uri? = null
    private var invoiceUri: Uri? = null
    private var otp = ""

    private val sky = Color.rgb(225, 244, 255)
    private val blue = Color.rgb(25, 111, 220)
    private val teal = Color.rgb(0, 145, 135)
    private val green = Color.rgb(25, 145, 78)
    private val navy = Color.rgb(24, 58, 92)
    private val amber = Color.rgb(225, 145, 20)
    private val red = Color.rgb(190, 55, 55)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        db = V32DatabaseHelper(this)
        if (db.hasUser() && prefs.getBoolean("logged_in", false)) dashboard() else ownerRegistration()
    }

    override fun onBackPressed() {
        if (history.isNotEmpty()) setContentView(history.removeLast()) else super.onBackPressed()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun money(v: Double) = "₹" + String.format(Locale.US, "%,.2f", v)
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    private fun bg(fill: Int, stroke: Int = fill, radius: Int = 16) = GradientDrawable().apply { setColor(fill); setStroke(dp(1), stroke); cornerRadius = dp(radius).toFloat() }
    private fun root() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(14), dp(16), dp(28)); setBackgroundColor(sky) }
    private fun label(s: String, size: Float = 16f, color: Int = navy) = TextView(this).apply { text = s; textSize = size; setTextColor(color); setPadding(dp(4), dp(5), dp(4), dp(5)) }
    private fun field(hint: String, max: Int = 0) = EditText(this).apply { this.hint = hint; textSize = 16f; setSingleLine(true); setPadding(dp(12), dp(8), dp(12), dp(8)); background = bg(Color.WHITE, Color.rgb(190,210,225), 14); if (max > 0) filters = arrayOf(android.text.InputFilter.LengthFilter(max)) }
    private fun button(s: String, color: Int = blue, action: () -> Unit) = Button(this).apply { text = s; isAllCaps = false; textSize = 14f; setTextColor(Color.WHITE); background = bg(color, color, 18); setOnClickListener { action() } }
    private fun gap(h: Int) = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(h)) }

    private fun show(view: View, push: Boolean = true) {
        if (push) findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)?.let { history.addLast(it) }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(view) })
    }

    private fun page(title: String, subtitle: String): LinearLayout {
        val r = root()
        val h = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val t = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        t.addView(label("🤝  $title", 24f)); t.addView(label(subtitle, 12f, Color.DKGRAY)); h.addView(t)
        h.addView(button("HOME", blue) { history.clear(); dashboard() }, LinearLayout.LayoutParams(dp(78), dp(44)))
        r.addView(h); r.addView(gap(8)); return r
    }

    private fun photoView(uri: String?, size: Int = 92) = ImageView(this).apply {
        layoutParams = LinearLayout.LayoutParams(dp(size), dp(size)).apply { gravity = Gravity.CENTER; setMargins(0, dp(6), 0, dp(6)) }
        scaleType = ImageView.ScaleType.CENTER_CROP; background = bg(Color.WHITE, Color.rgb(180,205,220), 48)
        if (!uri.isNullOrBlank()) try { setImageURI(Uri.parse(uri)) } catch (_: Exception) { }
    }

    private fun choosePhoto() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) }, 100)
    }
    private fun chooseInvoice() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "application/pdf"; addCategory(Intent.CATEGORY_OPENABLE); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) }, 101)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data); if (resultCode != RESULT_OK || data?.data == null) return
        val u = data.data!!; try { contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
        if (requestCode == 100) photoUri = u else if (requestCode == 101) invoiceUri = u
    }

    private fun ownerRegistration() {
        history.clear(); val r = root()
        r.addView(label("🤝  UDHAARDAAR", 29f, Color.WHITE).apply { gravity = Gravity.CENTER; background = bg(teal, teal, 24); setPadding(dp(10), dp(18), dp(10), dp(18)) })
        r.addView(label("Your Credit. Your Trust. Our Record.", 14f).apply { gravity = Gravity.CENTER })
        val name = field("Lender / Account Owner name *"); val mobile = field("Mobile number * (10 digits)", 10); val address = field("Full address *"); val email = field("Email (optional)"); val otpBox = field("Enter 6-digit OTP", 6).apply { visibility = View.GONE }
        listOf(name,mobile,address,email).forEach { r.addView(it, LinearLayout.LayoutParams(-1,dp(56)).apply { setMargins(0,dp(3),0,dp(3)) }) }
        r.addView(button("ADD PROFILE PHOTO (OPTIONAL)", teal) { choosePhoto() }); r.addView(otpBox)
        r.addView(button("CREATE PROFILE + SEND OTP", blue) {
            if (name.text.toString().trim().length < 2 || mobile.text.toString().length != 10 || address.text.toString().trim().length < 5) { toast("Name, address and exactly 10-digit mobile are required"); return@button }
            otp = Random.nextInt(100000,1000000).toString(); otpBox.visibility = View.VISIBLE; toast("Demo OTP: $otp")
        })
        r.addView(button("VERIFY OTP + SAVE PROFILE", green) {
            if (otpBox.text.toString() != otp || otp.isBlank()) { toast("Incorrect OTP"); return@button }
            db.saveUser("USR-${System.currentTimeMillis()}", name.text.toString().trim(), mobile.text.toString(), address.text.toString().trim(), email.text.toString().trim(), photoUri?.toString())
            prefs.edit().putBoolean("logged_in", true).apply(); history.clear(); dashboard()
        })
        show(r, false)
    }

    private fun dashboard() {
        val r = page("Udhaardaar Dashboard", "Digital informal-credit record & repayment manager")
        val u = db.userData(); r.addView(photoView(u?.photo, 86)); r.addView(label("Welcome, ${u?.name ?: "Lender"}",21f)); r.addView(label("Unique ID: ${u?.id ?: "—"}",12f,Color.DKGRAY)); r.addView(gap(4))
        metric(r,"Credit extended",money(db.totalCredit("Credit Given")),blue){ history("Credit Given") }
        metric(r,"Credit received",money(db.totalCredit("Credit Received")),teal){ history("Credit Received") }
        metric(r,"Due / Overdue","${db.dueCount(false)} / ${db.dueCount(true)}",amber){ repayments(false) }
        action(r,"＋","REGISTER CREDIT","Borrower → terms → guarantor → documents → OTP",blue){ registerCredit(null) }
        action(r,"⌕","SEARCH BORROWER / GUARANTOR","Name, mobile, PAN, Aadhaar, GSTIN or unique ID",teal){ searchProfiles("BORROWER") }
        action(r,"▣","CREDIT HISTORY","Open registered transactions and borrower history",navy){ history(null) }
        action(r,"₹","REPAYMENT CENTRE","Record payments and review due / overdue schedules",green){ repayments(false) }
        action(r,"▤","DIGITAL DOCUMENTS & CONSENT","Read acknowledgement before consent",amber){ documents() }
        action(r,"◎","MY PROFILE","Identity and profile photo",teal){ ownerProfile() }
        r.addView(button("LOGOUT",Color.DKGRAY){ prefs.edit().putBoolean("logged_in",false).apply(); history.clear(); ownerRegistration() }); show(r,false)
    }

    private fun metric(r:LinearLayout,title:String,value:String,color:Int,click:()->Unit){
        val b=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(14),dp(9),dp(14),dp(9));background=bg(Color.WHITE,color,18);setOnClickListener{click()}}
        b.addView(label(title,13f,Color.DKGRAY));b.addView(label(value,20f,color));b.addView(label("Tap to open records ›",11f,color));r.addView(b);r.addView(gap(7))
    }
    private fun action(r:LinearLayout,icon:String,title:String,sub:String,color:Int,click:()->Unit){
        val b=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12),dp(10),dp(8),dp(10));background=bg(Color.WHITE,Color.rgb(190,210,225),18);setOnClickListener{click()}}
        b.addView(label(icon,22f,color),LinearLayout.LayoutParams(dp(48),dp(52)));val t=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;layoutParams=LinearLayout.LayoutParams(0,-2,1f)};t.addView(label(title,16f));t.addView(label(sub,11f,Color.DKGRAY));b.addView(t);b.addView(label("›",28f,color));r.addView(b);r.addView(gap(7))
    }

    private fun searchProfiles(role:String){
        val r=page(if(role=="BORROWER")"Search Borrower" else "Search Guarantor","Name • mobile • PAN • Aadhaar • GSTIN • unique ID");val q=field("Search");val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        r.addView(q,LinearLayout.LayoutParams(-1,dp(56)));r.addView(button("SEARCH",blue){refreshProfiles(q,list,role)});r.addView(button("CREATE NEW PROFILE",green){profileForm(role,null)});r.addView(gap(5));r.addView(list);refreshProfiles(q,list,role);r.addView(button("BACK",Color.DKGRAY){history.clear();dashboard()});show(r)
    }
    private fun refreshProfiles(q:EditText,list:LinearLayout,role:String){list.removeAllViews();val rows=db.searchProfiles(role,q.text.toString());if(rows.isEmpty())list.addView(label("No profile found."));rows.forEach{p->action(list,"●",p.name,"${p.mobile} • ${p.id}",blue){if(role=="BORROWER")borrowerSummary(p.rowId)else profileForm(role,p.rowId)}}}

    private fun profileForm(role:String,rowId:Long?){
        val p=rowId?.let{db.profileData(it)};photoUri=p?.photo?.let{Uri.parse(it)};val r=page(if(role=="BORROWER")"Borrower Profile" else "Guarantor Profile","Photo optional • mobile limited to 10 digits")
        val img=photoView(p?.photo);r.addView(img);r.addView(button("ADD / CHANGE PHOTO (OPTIONAL)",teal){choosePhoto()})
        val name=field("Full name *").apply{setText(p?.name?:"")};val mobile=field("Mobile number * (10 digits)",10).apply{setText(p?.mobile?:"")};val alternate=field("Alternate mobile (optional)",10).apply{setText(p?.alternate?:"")};val address=field("Full address *").apply{setText(p?.address?:"")};val city=field("City").apply{setText(p?.city?:"")};val state=field("State").apply{setText(p?.state?:"")};val pin=field("PIN code",6).apply{setText(p?.pin?:"")};val pan=field("PAN").apply{setText(p?.pan?:"")};val aadhaar=field("Aadhaar",12).apply{setText(p?.aadhaar?:"")};val gst=field("GSTIN (optional)").apply{setText(p?.gstin?:"")}
        listOf(name,mobile,alternate,address,city,state,pin,pan,aadhaar,gst).forEach{r.addView(it,LinearLayout.LayoutParams(-1,dp(56)).apply{setMargins(0,dp(2),0,dp(2))})}
        r.addView(button(if(p==null)"SAVE PROFILE" else "UPDATE PROFILE",green){
            if(name.text.toString().trim().length<2||mobile.text.toString().length!=10||address.text.toString().trim().length<5){toast("Name, address and exactly 10-digit mobile are required");return@button}
            if(alternate.text.toString().isNotBlank()&&alternate.text.toString().length!=10){toast("Alternate mobile must be 10 digits");return@button}
            val id=p?.id?:"${if(role=="BORROWER")"BOR" else "GUA"}-${System.currentTimeMillis()}"
            db.upsertProfile(p?.rowId,role,id,name.text.toString().trim(),mobile.text.toString(),alternate.text.toString(),address.text.toString().trim(),city.text.toString(),state.text.toString(),pin.text.toString(),pan.text.toString().uppercase(Locale.US),aadhaar.text.toString(),gst.text.toString().uppercase(Locale.US),photoUri?.toString()?:p?.photo);toast("Profile saved: $id");searchProfiles(role)
        });r.addView(button("BACK",Color.DKGRAY){history.clear();searchProfiles(role)});show(r)
    }

    private fun borrowerSummary(id:Long){
        val p=db.profileData(id)?:return;val s=db.borrowerSummary(id);val r=page("Borrower Summary","Review complete history before registering new credit");r.addView(photoView(p.photo,88));r.addView(label(p.name,24f));r.addView(label("Unique ID: ${p.id}"));r.addView(label("Mobile: ${p.mobile} • PAN: ${p.pan.ifBlank{"—"}}"));r.addView(label("Total credit: ${money(s.total)} • Outstanding: ${money(s.outstanding)}",16f));r.addView(label("Active: ${s.active} • Overdue: ${s.overdue}",14f,if(s.overdue>0)red else green));db.creditsForBorrower(id).forEach{c->action(r,"₹",c.creditId,"${c.type} • ${money(c.amount)} • ${c.status}",if(c.status=="OVERDUE")red else blue){creditDetail(c.id)}};r.addView(button("REGISTER NEW CREDIT FOR THIS BORROWER",blue){registerCredit(id)});r.addView(button("BACK",Color.DKGRAY){searchProfiles("BORROWER")});show(r)
    }

    private fun registerCredit(preselected:Long?){
        val borrowers=db.searchProfiles("BORROWER","");if(borrowers.isEmpty()){toast("Create a borrower profile first");profileForm("BORROWER",null);return}
        val r=page("Register Credit","Borrower → terms → guarantor → document → OTP consent");val borrower=Spinner(this).apply{adapter=ArrayAdapter(this@V323Activity,android.R.layout.simple_spinner_dropdown_item,borrowers.map{"${it.name} • ${it.mobile}"});preselected?.let{id->val i=borrowers.indexOfFirst{it.rowId==id};if(i>=0)setSelection(i)}}
        val type=Spinner(this).apply{adapter=ArrayAdapter(this@V323Activity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Personal Credit","Business Credit","Trade Credit","Advance","Rental / Lease","Other"))};val direction=Spinner(this).apply{adapter=ArrayAdapter(this@V323Activity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Credit Given","Credit Received"))};val amount=field("Principal / amount *");val roi=field("Annual ROI %");val tenor=field("Tenor in months").apply{setText("1")};val method=Spinner(this).apply{adapter=ArrayAdapter(this@V323Activity,android.R.layout.simple_spinner_dropdown_item,arrayOf("EMI","Principal + Interest","Bullet / Full payment"))}
        r.addView(label("Borrower *"));r.addView(borrower);r.addView(label("Credit type"));r.addView(type);r.addView(label("Direction"));r.addView(direction);listOf(amount,roi,tenor).forEach{r.addView(it,LinearLayout.LayoutParams(-1,dp(56)).apply{setMargins(0,dp(3),0,dp(3))})};r.addView(label("Repayment method"));r.addView(method)
        val yesNo=Spinner(this).apply{adapter=ArrayAdapter(this@V323Activity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Guarantor not available","Guarantor available"))};val guarantors=db.searchProfiles("GUARANTOR","");val guarantor=Spinner(this).apply{adapter=ArrayAdapter(this@V323Activity,android.R.layout.simple_spinner_dropdown_item,if(guarantors.isEmpty())listOf("Create guarantor profile first") else guarantors.map{"${it.name} • ${it.mobile}"});visibility=View.GONE};yesNo.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(parent:AdapterView<*>?){guarantor.visibility=View.GONE};override fun onItemSelected(parent:AdapterView<*>?,view:View?,position:Int,id:Long){guarantor.visibility=if(position==1)View.VISIBLE else View.GONE}}
        r.addView(label("Guarantor?"));r.addView(yesNo);r.addView(guarantor);r.addView(button("UPLOAD INVOICE / SUPPORTING PDF (OPTIONAL)",teal){chooseInvoice()})
        r.addView(button("REVIEW TERMS + SEND OTP",blue){
            val p=amount.text.toString().toDoubleOrNull();val rate=roi.text.toString().toDoubleOrNull()?:0.0;val months=tenor.text.toString().toIntOrNull()?:0;if(p==null||p<=0||months<=0){toast("Enter valid amount and tenor");return@button}
            val monthly=rate/1200.0;val installment=if(method.selectedItem.toString()=="EMI"&&monthly>0)p*monthly*(1+monthly).pow(months)/((1+monthly).pow(months)-1) else if(method.selectedItem.toString()=="EMI")p/months.toDouble() else p/months.toDouble();val interest=if(method.selectedItem.toString()=="EMI")installment*months-p else p*rate/100.0*months/12.0;val payable=p+interest;val start=today();val end=Calendar.getInstance().apply{add(Calendar.MONTH,months)}.let{SimpleDateFormat("yyyy-MM-dd",Locale.US).format(it.time)}
            val gId=if(yesNo.selectedItemPosition==1&&!guarantors.isEmpty())guarantors[guarantor.selectedItemPosition].rowId else null
            otp=Random.nextInt(100000,1000000).toString();val details="Amount ${money(p)}\nROI ${rate}%\nMethod ${method.selectedItem}\nInstallment ${money(installment)}\nTotal payable ${money(payable)}\nPeriod $start to $end\n\nI have reviewed the credit terms and supporting document."
            val input=EditText(this).apply{hint="Enter 6-digit OTP";setSingleLine(true);filters=arrayOf(android.text.InputFilter.LengthFilter(6))}
            val dialog=AlertDialog.Builder(this).setTitle("Digital document & consent").setMessage(details).setView(input).setNegativeButton("CANCEL",null).setPositiveButton("SEND OTP",null).create()
            dialog.setOnShowListener{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{toast("Demo OTP: $otp");dialog.dismiss();AlertDialog.Builder(this).setTitle("Confirm consent").setMessage(details+"\n\nDemo OTP: $otp").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM & SAVE"){_,_->if(input.text.toString()==otp){val cid=db.addCredit(borrowers[borrower.selectedItemPosition].rowId,gId,type.selectedItem.toString(),direction.selectedItem.toString(),p,rate,months,method.selectedItem.toString(),installment,interest,payable,start,end,"",invoiceUri?.toString(),true);db.createSchedule(cid,installment,months,end);toast("Credit registered successfully");history.clear();dashboard()}else toast("Incorrect OTP")}.show()}}
            dialog.show()
        });r.addView(button("BACK",Color.DKGRAY){history.clear();dashboard()});show(r)
    }

    private fun history(direction:String?){val r=page("Credit History",if(direction==null)"All registered transactions" else direction);val rows=db.credits(direction);if(rows.isEmpty())r.addView(label("No credit records yet."));rows.forEach{c->action(r,"₹",c.creditId,"${c.borrowerName} • ${c.type} • ${money(c.amount)} • ${c.status}",if(c.status=="OVERDUE")red else blue){creditDetail(c.id)}};r.addView(button("BACK",Color.DKGRAY){history.clear();dashboard()});show(r)}
    private fun creditDetail(id:Long){val c=db.creditDetail(id)?:return;val r=page("Credit ${c.creditId}","Complete digital credit record");r.addView(label("Borrower: ${c.borrowerName}"));r.addView(label("Type: ${c.type} • Direction: ${c.direction}"));r.addView(label("Principal: ${money(c.amount)} • ROI: ${c.roi}%"));r.addView(label("Method: ${c.method} • Payable: ${money(c.payable)}"));r.addView(label("Period: ${c.start} to ${c.end}"));r.addView(label("Status: ${c.status}"));r.addView(button("VIEW REPAYMENT SCHEDULE",green){repayments(false)});r.addView(button("BACK",Color.DKGRAY){history(null)});show(r)}

    private fun repayments(overdue:Boolean){val r=page("Repayment Centre",if(overdue)"Overdue payments" else "Due and upcoming payments");val rows=db.schedules(null,overdue);if(rows.isEmpty())r.addView(label("No payments in this list."));rows.forEach{s->action(r,"₹",s.creditId,"Due ${s.dueDate} • ${money(s.amount)} • ${s.status}",if(s.status=="OVERDUE")red else green){recordPayment(s.id,s.creditDbId,s.amount)}};r.addView(button("BACK",Color.DKGRAY){history.clear();dashboard()});show(r)}
    private fun recordPayment(scheduleId:Long,creditId:Long,due:Double){val input=field("Payment amount");input.setText(String.format(Locale.US,"%.2f",due));AlertDialog.Builder(this).setTitle("Record repayment").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("SAVE"){_,_->val amount=input.text.toString().toDoubleOrNull()?:0.0;if(amount<=0)toast("Enter a valid amount")else{db.recordPayment(scheduleId,creditId,amount);toast("Repayment recorded");repayments(false)}}.show()}

    private fun documents(){val r=page("Digital Documents & Consent","Borrower-readable verification document");r.addView(label("UDHAARDAAR DIGITAL CREDIT ACKNOWLEDGEMENT",19f));r.addView(label("Before consent, review borrower identity, credit type, principal, ROI, repayment method, schedule, guarantor details and supporting invoice/document. The record is retained for future verification and repayment history."));r.addView(button("I HAVE READ THE DOCUMENT",green){toast("Acknowledged. Final consent is OTP-gated during credit registration.")});r.addView(button("BACK",Color.DKGRAY){history.clear();dashboard()});show(r)}
    private fun ownerProfile(){val u=db.userData()?:return;val r=page("Lender Profile","Identity and profile photo");r.addView(photoView(u.photo,110));r.addView(label("Unique ID: ${u.id}"));r.addView(label("Name: ${u.name}",19f));r.addView(label("Mobile: ${u.mobile}"));r.addView(label("Address: ${u.address}"));r.addView(label("Email: ${u.email.ifBlank{"—"}}"));r.addView(button("BACK",Color.DKGRAY){history.clear();dashboard()});show(r)}
}
