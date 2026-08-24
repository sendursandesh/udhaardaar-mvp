package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import kotlin.math.pow
import kotlin.random.Random

class V32Activity : AppCompatActivity() {
    private lateinit var db: V32DatabaseHelper
    private var otp = ""
    private var photoUri: Uri? = null
    private var invoiceUri: Uri? = null
    private var photoTarget: ImageView? = null
    private var invoiceTarget: TextView? = null
    private val navy=Color.rgb(17,42,75); private val blue=Color.rgb(35,105,210); private val teal=Color.rgb(0,145,135)
    private val green=Color.rgb(25,145,78); private val red=Color.rgb(205,55,55); private val amber=Color.rgb(232,151,22); private val bg=Color.rgb(246,248,252)
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    private fun box(fill:Int,stroke:Int,r:Int)=android.graphics.drawable.GradientDrawable().apply{setColor(fill);cornerRadius=dp(r).toFloat();setStroke(dp(1),stroke)}
    private fun text(s:String,size:Float=16f,color:Int=Color.DKGRAY)=TextView(this).apply{text=s;textSize=size;setTextColor(color);setPadding(dp(5),dp(5),dp(5),dp(5))}
    private fun field(h:String,num:Boolean=false,max:Int=0)=EditText(this).apply{hint=h;textSize=16f;setSingleLine(false);setPadding(dp(12),dp(8),dp(12),dp(8));background=box(Color.WHITE,Color.rgb(215,222,232),14);if(num)inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL;if(max>0)filters=arrayOf(InputFilter.LengthFilter(max))}
    private fun button(s:String,c:Int=blue,a:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=14f;setTextColor(Color.WHITE);background=box(c,c,18);setOnClickListener{a() } }
    private fun root()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(15),dp(12),dp(15),dp(25));setBackgroundColor(bg)}
    private fun gap(h:Int)=Space(this).apply{layoutParams=LinearLayout.LayoutParams(1,dp(h))}
    private fun show(r:LinearLayout){setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})}
    private fun page(title:String,sub:String):LinearLayout{val r=root();val h=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL};val t=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;layoutParams=LinearLayout.LayoutParams(0,-2,1f)};t.addView(text(title,25f,navy));t.addView(text(sub,12f,Color.GRAY));h.addView(t);h.addView(button("HOME",blue){dashboard()},LinearLayout.LayoutParams(dp(78),dp(44)));r.addView(h);r.addView(gap(8));return r}
    private fun money(v:Double)="₹"+String.format(Locale.US,"%,.2f",v)

    override fun onCreate(b:Bundle?){super.onCreate(b);db=V32DatabaseHelper(this);if(db.hasUser())dashboard() else ownerRegistration()}

    private fun ownerRegistration(){
        val r=root();r.addView(text("UDHAARDAAR",31f,Color.WHITE).apply{gravity=Gravity.CENTER;background=box(navy,navy,22);setPadding(dp(12),dp(18),dp(12),dp(18))});r.addView(text("V3.2.2 • Smart informal credit manager",14f,navy).apply{gravity=Gravity.CENTER});r.addView(gap(8))
        val name=field("Lender / account owner name *");val mobile=field("Mobile number * (10 digits) ",true,10);val address=field("Full address *");val email=field("Email (optional) ");val otpBox=field("Enter 6-digit OTP",true,6);otpBox.visibility=View.GONE
        listOf(name,mobile,address,email).forEach{r.addView(it,LinearLayout.LayoutParams(-1,dp(58)).apply{setMargins(0,dp(3),0,dp(3))})}
        val img=ImageView(this).apply{layoutParams=LinearLayout.LayoutParams(-1,dp(150));setImageResource(android.R.drawable.ic_menu_camera);scaleType=ImageView.ScaleType.CENTER_INSIDE;background=box(Color.WHITE,Color.LTGRAY,16)};r.addView(img);r.addView(button("ADD PROFILE PHOTO (OPTIONAL) ",teal){photoTarget=img;pick(100)});r.addView(otpBox)
        r.addView(button("CREATE PROFILE + SEND OTP",blue){if(name.text.toString().trim().length<2||mobile.text.toString().length!=10||address.text.toString().trim().length<5){toast("Name, address and exactly 10-digit mobile are required");return@button};if(!validEmail(email.text.toString())){email.error="Invalid email";return@button};otp=Random.nextInt(100000,1000000).toString();otpBox.visibility=View.VISIBLE;toast("Trial OTP: $otp\nConnect an SMS provider for live delivery.")})
        r.addView(button("VERIFY OTP + SAVE PROFILE",green){if(otp.isEmpty()||otpBox.text.toString()!=otp){otpBox.error="Incorrect OTP";return@button};db.saveUser("USR-${System.currentTimeMillis()}",name.text.toString().trim(),mobile.text.toString(),address.text.toString().trim(),email.text.toString().trim(),photoUri?.toString());dashboard()});show(r)
    }

    private fun dashboard(){
        val u=db.user();val r=page("Dashboard","V3.2.5 • secure profiles • consent-based repayment");val photo=ImageView(this).apply{layoutParams=LinearLayout.LayoutParams(dp(82),dp(82)).apply{gravity=Gravity.CENTER};scaleType=ImageView.ScaleType.CENTER_CROP;try{if(!u?.photo.isNullOrBlank())setImageURI(Uri.parse(u?.photo)) else setImageResource(android.R.drawable.ic_menu_camera)}catch(_:Exception){setImageResource(android.R.drawable.ic_menu_camera) } };r.addView(photo);r.addView(text("Good day, "+(u?.name?:"Lender"),21f,navy).apply{gravity=Gravity.CENTER});r.addView(text("Unique ID: "+(u?.id?:"—"),12f,Color.GRAY).apply{gravity=Gravity.CENTER});r.addView(gap(5))
        val a=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};a.addView(metric("CREDIT EXTENDED",money(db.total("Credit Given")),blue){history("Credit Given")},LinearLayout.LayoutParams(0,dp(94),1f));a.addView(metric("CREDIT TAKEN",money(db.total("Credit Received")),teal){history("Credit Received")},LinearLayout.LayoutParams(0,dp(94),1f));r.addView(a);r.addView(gap(6))
        val b=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};b.addView(metric("DUE / UPCOMING",db.dueCount(false).toString(),amber){repayments(false)},LinearLayout.LayoutParams(0,dp(94),1f));b.addView(metric("OVERDUE",db.dueCount(true).toString(),red){repayments(true)},LinearLayout.LayoutParams(0,dp(94),1f));r.addView(b);r.addView(gap(10))
        card(r,"＋","REGISTER CREDIT","Borrower → credit nature → terms → guarantor → bank/NACH → documents → OTP",blue){registerCredit(null)}
        card(r,"⌕","SEARCH BORROWER / PROFILE","Name, mobile, PAN, Aadhaar, GSTIN or unique ID",teal){search("BORROWER")}
        card(r,"▣","CREDIT HISTORY","View, verify and open full transaction history",navy){history(null)}
        card(r,"₹","REPAYMENT CENTRE","Payments, schedules, due dates and overdue items",green){repayments(false)}
        card(r,"▤","DIGITAL DOCUMENTS & CONSENT","T&C, acknowledgement and shareable documentation",amber){documents()}
        card(r,"◎","MY LENDER PROFILE","Photo, identity and optional bank/NACH link",teal){ownerProfile()}
        r.addView(button("LOGOUT",Color.DKGRAY){ownerRegistration()});show(r)
    }
    private fun metric(l:String,v:String,c:Int,a:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;background=box(Color.WHITE,Color.LTGRAY,16);setOnClickListener{a()};addView(text(v,20f,c).apply{gravity=Gravity.CENTER});addView(text(l,10f,Color.DKGRAY).apply{gravity=Gravity.CENTER})}
    private fun card(r:LinearLayout,ic:String,title:String,sub:String,c:Int,a:()->Unit){val b=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12),dp(11),dp(9),dp(11));background=box(Color.WHITE,Color.LTGRAY,18);elevation=dp(2).toFloat();setOnClickListener{a() } };b.addView(text(ic,22f,c).apply{gravity=Gravity.CENTER;layoutParams=LinearLayout.LayoutParams(dp(46),dp(46))});val t=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;layoutParams=LinearLayout.LayoutParams(0,-2,1f);setPadding(dp(8),0,0,0)};t.addView(text(title,16f,navy));t.addView(text(sub,11f,Color.GRAY));b.addView(t);b.addView(text("›",28f,c));r.addView(b);r.addView(gap(7))}
    private fun toast(s:String){Toast.makeText(this,s,Toast.LENGTH_LONG).show()}
    private fun validPan(v:String)=v.isBlank() || Regex("[A-Z]{5}[0-9]{4}[A-Z]").matches(v.trim().uppercase(Locale.US))
    private fun validGstin(v:String)=v.isBlank() || Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]").matches(v.trim().uppercase(Locale.US))
    private fun validPin(v:String)=v.isBlank() || Regex("[0-9]{6}").matches(v.trim())
    private fun validAadhaar(v:String)=v.isBlank() || Regex("[0-9]{12}").matches(v.trim())
    private fun validEmail(v:String)=v.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(v.trim()).matches()
    private fun validDate(v:String):Boolean=runCatching{java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).apply{isLenient=false}.parse(v.trim());true}.getOrDefault(false)
    private fun endNotBeforeStart(start:String,end:String):Boolean=runCatching{val f=java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).apply{isLenient=false};!f.parse(end)!!.before(f.parse(start)!!)}.getOrDefault(false)
    private fun emi(principal:Double,annualRate:Double,months:Int):Double{if(months<=0)return 0.0;val r=annualRate/1200.0;return if(r==0.0)principal/months else principal*r*(1+r).pow(months)/( (1+r).pow(months)-1)}
    private fun attachRoiSuffix(e:EditText){
        e.setOnFocusChangeListener{_,hasFocus->
            if(!hasFocus){val n=e.text.toString().replace("%","").trim();if(n.isNotEmpty())e.setText(n+"%")}
            else{val n=e.text.toString().replace("%","").trim();if(n!=e.text.toString())e.setText(n);e.setSelection(e.text.length)}
        }
    }

    private fun ownerProfile(){val u=db.user()?:return;val r=page("Lender Profile","Photo and optional bank/NACH link");val img=ImageView(this).apply{layoutParams=LinearLayout.LayoutParams(-1,dp(170));scaleType=ImageView.ScaleType.CENTER_CROP;if(u.photo!=null)setImageURI(Uri.parse(u.photo)) else setImageResource(android.R.drawable.ic_menu_camera)};r.addView(img);r.addView(button("CHANGE PHOTO",teal){photoTarget=img;pick(100)});r.addView(text("Unique ID: ${u.id}",13f,Color.GRAY));r.addView(text("Name: ${u.name}",17f,navy));r.addView(text("Mobile: ${u.mobile}"));r.addView(text("Address: ${u.address}"));r.addView(text("Email: ${u.email}"));r.addView(button("LINK BANK / NACH (OPTIONAL) ",teal){bankForm("LENDER",u.id)});r.addView(button("BACK",Color.DKGRAY){dashboard()}); show(r)}

    private fun search(role:String){val r=page(if(role=="BORROWER") "Search Borrower" else "Search Guarantor","Name • mobile • PAN • Aadhaar • GSTIN • unique ID");val q=field("Search");val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};fun refresh(){list.removeAllViews();val rows=db.profiles(role,q.text.toString());if(rows.isEmpty())list.addView(text("No profile found. Create a new profile.",14f,Color.GRAY));rows.forEach{p->card(list,"●",p.name,"${p.mobile} • PAN ${p.pan.ifBlank {"—" } } • ID ${p.id}",blue){if(role=="BORROWER")borrowerSummary(p.rowId) else profileForm(role,p.rowId) } }};r.addView(q,LinearLayout.LayoutParams(-1,dp(58)));r.addView(button("SEARCH",blue){refresh()});r.addView(button("CREATE NEW PROFILE",green){profileForm(role,null)});r.addView(list);refresh();r.addView(button("BACK",Color.DKGRAY){dashboard()}); show(r)}
    private fun profileForm(role:String,id:Long?){
        val p=id?.let{db.profile(it)}
        val r=page(if(role=="BORROWER") "Borrower Profile" else "Guarantor Profile","Validated identity fields")
        val name=field("Full name *").apply{setText(p?.name?:"")}
        val mob=field("Mobile number * (10 digits) ",true,10).apply{setText(p?.mobile?:"")}
        val addr=field("Full address *").apply{setText(p?.address?:"")}
        val city=field("City").apply{setText(p?.city?:"")}
        val state=field("State").apply{setText(p?.state?:"")}
        val pin=field("PIN code (6 digits) ",true,6).apply{setText(p?.pin?:"")}
        val pan=field("PAN").apply{setText(p?.pan?:"")}
        val aad=field("Aadhaar (12 digits) ",true,12).apply{setText(p?.aadhaar?:"")}
        val gst=field("GSTIN (optional) ").apply{setText(p?.gstin?:"")}
        val img=ImageView(this).apply{layoutParams=LinearLayout.LayoutParams(-1,dp(150));scaleType=ImageView.ScaleType.CENTER_CROP;try{if(!p?.photo.isNullOrBlank())setImageURI(Uri.parse(p?.photo)) else setImageResource(android.R.drawable.ic_menu_camera)}catch(_:Exception){setImageResource(android.R.drawable.ic_menu_camera) } }
        listOf(name,mob,addr,city,state,pin,pan,aad,gst).forEach{r.addView(it,LinearLayout.LayoutParams(-1,dp(58)).apply{setMargins(0,dp(3),0,dp(3))})}
        r.addView(img);r.addView(button("ADD / CHANGE PHOTO (OPTIONAL) ",teal){photoTarget=img;pick(100)})
        r.addView(button(if(id==null) "SAVE PROFILE" else "UPDATE PROFILE",green){
            val n=name.text.toString().trim();val m=mob.text.toString().trim();val ad=addr.text.toString().trim();val pi=pin.text.toString().trim();val pa=pan.text.toString().trim().uppercase(Locale.US);val aa=aad.text.toString().trim();val gg=gst.text.toString().trim().uppercase(Locale.US)
            if(n.length<2||m.length!=10||ad.length<5){toast("Name, address and exactly 10-digit mobile are required");return@button}
            if(!validEmail(email.text.toString())){email.error="Invalid email";return@button}
            if(!validPin(pi)){pin.error="PIN must be exactly 6 digits";return@button}
            if(!validPan(pa)){pan.error="Invalid PAN format (e.g. ABCDE1234F) ";return@button}
            if(!validAadhaar(aa)){aad.error="Aadhaar must contain exactly 12 digits";return@button}
            if(!validGstin(gg)){gst.error="Invalid GSTIN format";return@button}
            db.saveProfile(id,role,p?.id?:"ID-"+System.currentTimeMillis(),n,m,ad,city.text.toString().trim(),state.text.toString().trim(),pi,pa,aa,gg,photoUri?.toString()?:p?.photo)
            toast("Profile saved");search(role)
        });r.addView(button("BACK",Color.DKGRAY){search(role)});show(r)
    }

    private fun borrowerSummary(id:Long){val p=db.profile(id)?:return;val r=page("Borrower Summary","Review history before creating new credit");r.addView(text(p.name,24f,navy));r.addView(text("Unique ID: ${p.id}",12f,Color.GRAY));r.addView(text("Mobile: ${p.mobile} • PAN: ${p.pan.ifBlank {"—" } } • Aadhaar: ${p.aadhaar.ifBlank {"—" } }"));val cs=db.credits().filter{it.borrowerId==id};r.addView(text("Total registered credits: ${cs.size}",16f,navy));cs.forEach{card(r,"₹",it.code,"${it.type} • ${it.direction} • ${money(it.amount)} • ${it.status}",if(it.status=="OVERDUE")red else blue){creditDetail(it.rowId) } };r.addView(button("REGISTER NEW CREDIT FOR THIS BORROWER",blue){registerCredit(id)});r.addView(button("BACK",Color.DKGRAY){search("BORROWER")});show(r)}

    private fun registerCredit(preselected:Long?){
        val borrowers=db.profiles("BORROWER");if(borrowers.isEmpty()){toast("Create a borrower profile first");profileForm("BORROWER",null);return}
        val r=page("Register Credit","Borrower → terms → guarantor → documents → OTP")
        val bs=Spinner(this);bs.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,borrowers.map{it.name+" • "+it.mobile+" • "+it.id});preselected?.let{x->val i=borrowers.indexOfFirst{it.rowId==x};if(i>=0)bs.setSelection(i)};r.addView(text("1. Borrower selection *",15f,navy));r.addView(bs)
        val type=Spinner(this);type.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Personal Credit","Business Credit","Trade Credit","Advance","Rental / Lease","Other"));r.addView(text("2. Nature of credit *",15f,navy));r.addView(type)
        val direction=Spinner(this);direction.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Credit Given","Credit Received"));r.addView(text("Direction",15f,navy));r.addView(direction)
        val amount=field("Principal / credit amount",true);val roi=field("ROI",true);roi.filters=arrayOf(InputFilter.LengthFilter(7));attachRoiSuffix(roi);val months=field("Tenor / months",true,3);val dueDay=field("Due day of month (rental) ",true,2);val start=field("Start date YYYY-MM-DD");val end=field("End / maturity date YYYY-MM-DD");val gst=field("GSTIN for trade credit");val method=Spinner(this);method.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("EMI","Principal + Interest","Single Payment","Monthly Rent","Trade Invoice Due"))
        listOf(amount,roi,months,start,end).forEach{r.addView(it,LinearLayout.LayoutParams(-1,dp(58)).apply{setMargins(0,dp(3),0,dp(3))})};r.addView(method);r.addView(dueDay,LinearLayout.LayoutParams(-1,dp(58)));r.addView(gst,LinearLayout.LayoutParams(-1,dp(58)))
        r.addView(text("3. Guarantor available?",15f,navy));val ga=Spinner(this);ga.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("No","Yes"));r.addView(ga)
        val invoiceStatus=text("No invoice selected.",12f,Color.GRAY);invoiceTarget=invoiceStatus;r.addView(invoiceStatus);val gs=db.profiles("GUARANTOR");val g=Spinner(this);g.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("Select guarantor")+gs.map{it.name+" • "+it.mobile+" • "+it.id});g.visibility=View.GONE;r.addView(g)
        val gb=button("SEARCH GUARANTOR",teal){search("GUARANTOR")};gb.visibility=View.GONE;r.addView(gb)
        ga.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(parent:AdapterView<*>?){g.visibility=View.GONE;gb.visibility=View.GONE};override fun onItemSelected(parent:AdapterView<*>?,view:View?,position:Int,id:Long){val yes=position==1;g.visibility=if(yes)View.VISIBLE else View.GONE;gb.visibility=if(yes)View.VISIBLE else View.GONE } }
        r.addView(button("UPLOAD / SCAN TRADE INVOICE (OPTIONAL) ",teal){pick(200)})
        val consent=CheckBox(this).apply{text="I have reviewed the digital credit document and agree to the T&C"};r.addView(consent)
        val otpBox=field("Enter 6-digit consent OTP",true,6);r.addView(otpBox)
        r.addView(button("SEND CONSENT OTP",blue){try{otp=Random.nextInt(100000,1000000).toString();toast("Trial OTP: "+otp+"\nLive SMS provider must be connected for real SMS delivery.")}catch(_:Exception){toast("OTP could not be started. Please retry.") } })
        r.addView(button("FINAL VERIFY + REGISTER CREDIT",green){
            try{
                if(!consent.isChecked){toast("Digital consent is required");return@button}
                if(otp.isEmpty()||otpBox.text.toString()!=otp){toast("Enter the consent OTP");return@button}
                val t=type.selectedItem.toString();val a=amount.text.toString().toDoubleOrNull()?:0.0;val rate=roi.text.toString().replace("%","").toDoubleOrNull()?:0.0;val n=months.text.toString().toIntOrNull()?:1
                if(a<=0||n<=0||rate<0){toast("Enter valid credit amount, ROI and tenor");return@button}
                if(t=="Trade Credit"&&!validGstin(gst.text.toString().trim().uppercase(Locale.US))){gst.error="Invalid GSTIN format";return@button}
                val st=start.text.toString().trim().ifBlank {today()};val en=end.text.toString().trim().ifBlank {if(t=="Rental / Lease")dateAfter(st,n) else st};if(!validDate(st)||!validDate(en)||!endNotBeforeStart(st,en)){toast("Dates must be valid YYYY-MM-DD values and end date cannot be before start date");return@button};if(dueDay.text.toString().isNotBlank()&&dueDay.text.toString().toIntOrNull() !in 1..31){dueDay.error="Due day must be 1-31";return@button};val monthlyEmi=if(method.selectedItem.toString()=="EMI")emi(a,rate,n) else 0.0;val interest=when{t=="Rental / Lease"->0.0;method.selectedItem.toString()=="EMI"->(monthlyEmi*n-a).coerceAtLeast(0.0);else->a*rate/100.0*n/12.0};val payable=if(t=="Rental / Lease")a*n else a+interest;val inst=when{t=="Trade Credit"||t=="Advance"||method.selectedItem.toString()=="Single Payment"||method.selectedItem.toString()=="Trade Invoice Due"->payable;t=="Rental / Lease"->a;method.selectedItem.toString()=="EMI"->monthlyEmi;else->payable/n.coerceAtLeast(1)}
                val b=borrowers[bs.selectedItemPosition];val gid=if(ga.selectedItemPosition==1&&g.selectedItemPosition>0)gs[g.selectedItemPosition-1].rowId else null
                val id=db.addCredit(b.rowId,t,direction.selectedItem.toString(),a,rate,method.selectedItem.toString(),inst,interest,payable,st,en,if(t=="Trade Credit"||t=="Advance")en else st,gst.text.toString().trim().uppercase(Locale.US),invoiceUri?.toString()?:"",false);if(id<=0L){toast("Credit could not be saved");return@button}
                db.makeSchedule(id,t,st,en,inst,n,dueDay.text.toString().toIntOrNull()?:0);toast("Credit registered successfully");creditDetail(id)
            }catch(_:Exception){toast("Credit registration could not be completed safely. Review the fields and retry.")}
        });r.addView(button("BACK",Color.DKGRAY){dashboard()}); show(r)
    }

    private fun today()=java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(java.util.Date())
    private fun dateAfter(s:String,n:Int):String{val c=java.util.Calendar.getInstance();runCatching{c.time=java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(s)!!};c.add(java.util.Calendar.MONTH,n);return java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(c.time)}

    private fun history(direction:String?){val r=page("Credit History","All registered transactions • tap for verification details");val rows=db.credits(direction);if(rows.isEmpty())r.addView(text("No credit records yet.",15f,Color.GRAY));rows.forEach{c->card(r,"₹",c.code,"${c.borrowerName} • ${c.type} • ${c.direction} • ${money(c.amount)}",if(c.status=="OVERDUE")red else blue){creditDetail(c.rowId) } };r.addView(button("BACK",Color.DKGRAY){dashboard()}); show(r)}
    private fun creditDetail(id:Long){val c=db.credit(id)?:return;val r=page("Credit ${c.code}","Full transaction / verification record");r.addView(text("Borrower: ${c.borrowerName}",19f,navy));r.addView(text("Nature: ${c.type}"));r.addView(text("Direction: ${c.direction}"));r.addView(text("Amount: ${money(c.amount)}"));r.addView(text("ROI: ${String.format(Locale.US,"%.2f",c.roi)}%"));r.addView(text("Method: ${c.method}"));r.addView(text("Installment: ${money(c.installment)}"));r.addView(text("Interest: ${money(c.interest)}"));r.addView(text("Total payable: ${money(c.payable)}"));r.addView(text("Start: ${c.start} • End: ${c.end}"));r.addView(text("Due: ${c.due}"));if(c.type=="Trade Credit")r.addView(text("GSTIN: ${c.gstin.ifBlank {"—" } }"));r.addView(text("Bank/NACH: ${if(c.nach) "Linked" else "Not linked"}"));r.addView(text("OTP / digital consent: VERIFIED",14f,green));if(c.invoice.isNotBlank())r.addView(button("OPEN INVOICE",teal){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(c.invoice)).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)) });r.addView(button("VIEW / SHARE DOCUMENT",amber){shareCredit(c)});r.addView(button("REPAYMENT SCHEDULE",green){repaymentFor(id)});r.addView(button("BACK",Color.DKGRAY){history(null)}); show(r)}

    private fun repayments(overdue:Boolean){val r=page(if(overdue) "Overdue Payments" else "Repayment Centre","Schedules • payments • due-date follow-up");val rows=db.schedules(null,overdue);if(rows.isEmpty())r.addView(text("No ${if(overdue) "overdue" else "pending"} installments.",15f,Color.GRAY));rows.forEach{card(r,"₹",it.code,"Due ${it.due} • ${money(it.amount-it.paid)} • ${it.status}",if(it.status=="OVERDUE")red else green){payDialog(it) } };r.addView(button("BACK",Color.DKGRAY){dashboard()}); show(r)}
    private fun repaymentFor(id:Long){val r=page("Repayment Schedule","Due dates and payment recording");val rows=db.schedules(id);rows.forEach{card(r,"₹","Installment ${rows.indexOf(it)+1}","${it.due} • ${money(it.amount-it.paid)} • ${it.status}",if(it.status=="OVERDUE")red else green){payDialog(it) } };r.addView(button("BACK",Color.DKGRAY){creditDetail(id)}); show(r)}
    private fun payDialog(s:V32DatabaseHelper.Schedule){
        val amount=field("Payment amount",true);amount.setText(String.format(Locale.US,"%.2f",(s.amount-s.paid).coerceAtLeast(0.0)))
        val role=Spinner(this);role.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Lender initiating repayment","Borrower initiating repayment"))
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(role);addView(amount)}
        val confirmDialog=AlertDialog.Builder(this)
            .setTitle("Repayment — consent required")
            .setMessage("Repayment is recorded only after the initiating party confirms the amount and the consent step is completed.")
            .setView(box)            .setNegativeButton("CANCEL",null)
            .setPositiveButton("CONTINUE",null)
            .create()
        confirmDialog.setOnShowListener {
            confirmDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val av=amount.text.toString().toDoubleOrNull()?:0.0
                if(av<=0){toast("Enter a valid payment amount");return@setOnClickListener}
                if(av>(s.amount-s.paid)+0.005){toast("Payment cannot exceed the outstanding amount");return@setOnClickListener}
                val proposer=if(role.selectedItemPosition==0) "LENDER" else "BORROWER"
                val token=Random.nextInt(100000,1000000).toString()
                confirmDialog.dismiss()
                AlertDialog.Builder(this)
                    .setTitle("Confirm repayment consent")
                    .setMessage("Amount: "+money(av)+"\nInitiated by: "+proposer+"\n\nTrial consent OTP: "+token+"\nBoth parties should verify before confirmation.")
                    .setNegativeButton("CANCEL",null)
                    .setPositiveButton("CONFIRM + RECORD"){_,_->
                        try{
                            db.recordRepaymentWithConsent(s.id,s.creditId,av,proposer,token)
                            toast("Repayment recorded with consent audit trail")
                            repayments(false)
                        }catch(_:Exception){
                            toast("Repayment could not be recorded safely")
                        }
                    }
                    .show()
            }
        }
        confirmDialog.show()
    }

    private fun bankForm(type:String,id:String){val r=page("Bank / NACH","Optional linkage • do not make this mandatory");val h=field("Account holder name");val bank=field("Bank name");val acc=field("Account number",true);val ifsc=field("IFSC");val upi=field("UPI ID");val nach=CheckBox(this).apply{text="Enable NACH"};listOf(h,bank,acc,ifsc,upi).forEach{r.addView(it,LinearLayout.LayoutParams(-1,dp(58)).apply{setMargins(0,dp(3),0,dp(3))})};r.addView(nach);r.addView(button("SAVE BANK / NACH",green){db.saveBank(type,id,h.text.toString(),bank.text.toString(),acc.text.toString(),ifsc.text.toString(),upi.text.toString(),nach.isChecked);toast("Optional bank/NACH link saved");dashboard()});r.addView(button("BACK",Color.DKGRAY){dashboard()}); show(r)}

    private fun documents(){val r=page("Digital Documents","Standard T&C and consent documentation");r.addView(text("Udhaardaar V3.2.2 Standard Lending Terms",20f,navy));r.addView(text("1. Parties and identity are recorded before registration.\n2. Credit nature determines applicable repayment fields.\n3. Borrower receives the digital acknowledgement before OTP consent.\n4. Repayments are recorded against the registered schedule.\n5. Trade credit may include GSTIN and invoice evidence.\n6. Bank/NACH linkage is optional.\n7. Users should retain copies of all documents and comply with applicable law."));r.addView(button("SHARE STANDARD T&C",amber){shareTerms()});r.addView(button("BACK",Color.DKGRAY){dashboard()}); show(r)}
    private fun shareTerms(){val i=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_SUBJECT,"Udhaardaar Standard Lending Terms V3.2.2");putExtra(Intent.EXTRA_TEXT,"UDHAARDAAR — KEY FACT / CREDIT DOCUMENT\n\nBorrower identity: recorded and verified before registration\nCredit nature: disclosed\nPrincipal, annualised interest/APR, tenor and repayment schedule: disclosed before consent\nFees/charges: must be disclosed before consent\nRecovery mechanism: must be disclosed before consent\nGrievance contact: configure before production use\nCooling-off/look-up period: configure according to the applicable lender/regulatory framework\nData sharing: OFF unless the user gives explicit consent\n\nThis trial build is a record-keeping prototype and is not a substitute for a regulated lender’s final KFS, sanction letter, loan agreement or legally executed digital signature.")};startActivity(Intent.createChooser(i,"Share T&C"))}
    private fun shareCredit(c:V32DatabaseHelper.Credit){val i=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_SUBJECT,"Udhaardaar Credit ${c.code}");putExtra(Intent.EXTRA_TEXT,"Udhaardaar Credit Record\nCode: ${c.code}\nBorrower: ${c.borrowerName}\nNature: ${c.type}\nDirection: ${c.direction}\nAmount: ${money(c.amount)}\nROI: ${String.format(Locale.US,"%.2f",c.roi)}%\nMethod: ${c.method}\nInstallment: ${money(c.installment)}\nTotal payable: ${money(c.payable)}\nStart: ${c.start}\nEnd: ${c.end}\nDigital consent: VERIFIED")};startActivity(Intent.createChooser(i,"Share credit document"))}

    private fun pick(code:Int){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type=if(code==200)"*/*" else "image/*";if(code==200)putExtra(Intent.EXTRA_MIME_TYPES,arrayOf("image/*","application/pdf"));putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)},code)}
    override fun onActivityResult(request:Int,result:Int,data:Intent?){super.onActivityResult(request,result,data);if(result!=Activity.RESULT_OK||data?.data==null)return;val u=data.data!!;runCatching{contentResolver.takePersistableUriPermission(u,data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))};if(request==100){photoUri=u;photoTarget?.setImageURI(u)} else if(request==200){invoiceUri=u;invoiceTarget?.text="Invoice selected: ${u.lastPathSegment?:"document"}" } }
}