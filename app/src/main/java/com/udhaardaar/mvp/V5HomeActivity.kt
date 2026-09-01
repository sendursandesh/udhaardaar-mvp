package com.udhaardaar.mvp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/** V5 command centre. Every module writes to the same V5LocalStore boundary and exposes its state. */
class V5HomeActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val store by lazy { V5LocalStore(this) }
    private var pendingDocumentType = ""
    private var pendingDocumentOwner = ""

    private fun add(r: LinearLayout, v: View, h: Int = 52) { r.addView(v, LinearLayout.LayoutParams(-1, h).apply { setMargins(0, 3, 0, 3) }) }
    private fun btn(s: String, c: Int = Color.rgb(25,111,220), f: () -> Unit) = Button(this).apply { text=s;isAllCaps=false;textSize=13f;setTextColor(Color.WHITE);setBackgroundColor(c);setOnClickListener{f()} }
    private fun tile(r: LinearLayout,t:String,sub:String,f:()->Unit){add(r,LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(12,6,8,6);setBackgroundColor(Color.WHITE);setOnClickListener{f()};addView(TextView(this@V5HomeActivity).apply{text=t;textSize=16f;setTextColor(Color.rgb(24,58,92))});addView(TextView(this@V5HomeActivity).apply{text=sub;textSize=11f})},64)}
    private fun page(title:String,sub:String)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,12,16,16);setBackgroundColor(Color.rgb(238,248,253));addView(TextView(this@V5HomeActivity).apply{text=title;textSize=22f;setTextColor(Color.rgb(24,58,92))});addView(TextView(this@V5HomeActivity).apply{text=sub;textSize=12f})}
    private fun field(h:String)=EditText(this).apply{hint=h;setSingleLine(true);textSize=14f}
    private fun form(r:LinearLayout,e:EditText){add(r,e,50)}
    private fun save(key:String, data:JSONObject){data.put("updatedAt",System.currentTimeMillis());store.replace(key,data)}
    private fun showList(r:LinearLayout,key:String,label:String){val items=store.all(key);add(r,TextView(this).apply{text="$label: ${items.size}";textSize=12f;setTextColor(Color.rgb(24,58,92))},34);if(items.isNotEmpty())add(r,TextView(this).apply{text=items.takeLast(8).joinToString("\n"){it.optString("id")+" • "+it.optString("status","SAVED")};textSize=11f},Math.min(140,24*items.size))}

    override fun onCreate(b:Bundle?){super.onCreate(b);home()}
    override fun onBackPressed(){home()}

    private fun home(){
        if(!prefs.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,12,14,16);setBackgroundColor(Color.rgb(238,248,253))}
        add(r,TextView(this).apply{text="UDHAARDAAR V5";textSize=24f;setTextColor(Color.rgb(24,58,92))},42)
        add(r,TextView(this).apply{text="Unified financial obligations • documents • assets • succession";textSize=12f},34)
        add(r,btn("PARTY & IDENTITY • Search / Create / History",Color.rgb(0,145,135)){startActivity(Intent(this,V5PartyActivity::class.java))})
        add(r,btn("SCAN QR • Pay or Request / Offer Credit"){startActivity(Intent(this,V5QrCreditActivity::class.java))})
        add(r,TextView(this).apply{text="CREDIT & OBLIGATIONS";textSize=12f;setTextColor(Color.rgb(25,111,220))},28)
        tile(r,"Credit Registration","Personal • business • trade • formal/informal • given/received"){startActivity(Intent(this,V5CreditRegistrationActivity::class.java))}
        tile(r,"Repayment Centre","EMI • principal+interest • three bullet modes • payable/receivable • evidence/consent"){startActivity(Intent(this,V5RepaymentActivity::class.java))}
        tile(r,"Formal Credit Audit","Sanction + statement → extracted/entered terms → actual charges → variance report"){formal()}
        tile(r,"Rental / Lease Engine","Persistent lease terms • due calendar • arrears • escalation • expiry/renewal"){rental()}
        add(r,TextView(this).apply{text="DOCUMENTS • ASSETS • SUCCESSION";textSize=12f;setTextColor(Color.rgb(210,135,15))},28)
        tile(r,"Document & Consent Vault","Indexed documents • preview/open/share • version/hash • consent/execution trail"){documents()}
        tile(r,"Financial / Non-Financial Assets","Ownership • proof • value • nominee • maturity/renewal"){assets()}
        tile(r,"Nominee / Trusted Person","Permission-controlled discovery/view/claim preparation"){trusted()}
        tile(r,"Inheritance & Claims","Heir/nominee • checklist • submission • query • approval • transfer/closure"){claims()}
        tile(r,"Legal Assistance","Evidence bundle • timeline • recovery/claim professional referral"){legal()}
        tile(r,"Score & Readiness","Consent-controlled explainable score • factors • confidence • version/date"){score()}
        add(r,btn("MY PROFILE",Color.rgb(0,145,135)){profile()})
        add(r,btn("LOGOUT",Color.rgb(90,110,125)){prefs.edit().putBoolean("logged_in",false).apply();startActivity(Intent(this,LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish()})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }

    private fun formal(){
        val r=page("Formal Credit Audit","Upload/source-link sanction + statement, record contractual terms and reconcile actual debits.")
        val ref=field("Audit ID (optional)");val sanction=field("Sanction document reference");val statement=field("Account statement reference")
        val sRoi=field("Sanctioned ROI %");val aRoi=field("Actual/charged ROI %");val sFees=field("Sanctioned fees + charges + taxes + penalties (₹)");val aFees=field("Actual debited fees + charges + taxes + penalties (₹)")
        listOf(ref,sanction,statement,sRoi,aRoi,sFees,aFees).forEach{form(r,it)}
        add(r,btn("UPLOAD SANCTION / STATEMENT DOCUMENT",Color.rgb(0,145,135)){pendingDocumentType="FORMAL_AUDIT_SOURCE";pendingDocumentOwner=ref.text.toString();startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/pdf";addCategory(Intent.CATEGORY_OPENABLE)},7003)})
        add(r,btn("SAVE + RUN RECONCILIATION",Color.rgb(25,145,78)){
            val sf=sFees.text.toString().toDoubleOrNull()?:0.0;val af=aFees.text.toString().toDoubleOrNull()?:0.0;val variance=af-sf
            val id=if(ref.text.isBlank())"AUDIT-${System.currentTimeMillis()}" else ref.text.toString()
            save("formal_audits",JSONObject().apply{put("id",id);put("sanction",sanction.text.toString());put("statement",statement.text.toString());put("sanctionedRoi",sRoi.text.toString());put("actualRoi",aRoi.text.toString());put("sanctionedFees",sf);put("actualFees",af);put("variance",variance);put("status",if(variance>0)"EXCESS_CHARGE_REVIEW" else "RECONCILED")})
            AlertDialog.Builder(this).setTitle(if(variance>0)"EXCESS / VARIANCE FLAGGED" else "RECONCILIATION RESULT").setMessage("Sanctioned charges: ₹$sf\nActual debits: ₹$af\nVariance: ₹$variance\nROI: ${sRoi.text}% vs ${aRoi.text}%\nReview the source statement and sanction evidence before raising any recovery claim.").setPositiveButton("OK",null).show()
        })
        showList(r,"formal_audits","Saved audits");add(r,btn("HOME",Color.rgb(24,58,92)){home()});setContentView(ScrollView(this).apply{addView(r)})
    }

    private fun rental(){
        val r=page("Rental / Lease Engine","Rental is a separate obligation. Dates are DDMMYYYY; due day must be 1–31.")
        val id=field("Lease ID (optional)"),tenant=field("Tenant / counterparty *"),landlord=field("Landlord / owner *"),property=field("Property / premises *"),rent=field("Monthly rent ₹ *"),deposit=field("Security deposit ₹"),start=field("Start date DDMMYYYY *"),end=field("End date DDMMYYYY *"),due=field("Due day 1–31 *"),esc=field("Escalation %"),notice=field("Notice days"),util=field("Utilities / maintenance")
        listOf(tenant,landlord,property,rent,deposit,start,end,due,esc,notice,util).forEach{form(r,it)}
        add(r,btn("PICK START DATE",Color.rgb(0,145,135)){pickDate(start)});add(r,btn("PICK END DATE",Color.rgb(0,145,135)){pickDate(end)})
        add(r,btn("SAVE LEASE + GENERATE DUE CALENDAR",Color.rgb(25,145,78)){
            val d=due.text.toString().toIntOrNull();if(d==null||d !in 1..31){due.error="Enter a due day from 1 to 31";return@btn};if(tenant.text.isBlank()||landlord.text.isBlank()||property.text.isBlank()||rent.text.toString().toDoubleOrNull()==null||start.text.length!=8||end.text.length!=8){Toast.makeText(this,"Complete required lease fields and DDMMYYYY dates",Toast.LENGTH_LONG).show();return@btn}
            val lid=if(id.text.isBlank())"LEASE-${System.currentTimeMillis()}" else id.text.toString();val monthly=rent.text.toString().toDouble();val escPct=esc.text.toString().toDoubleOrNull()?:0.0
            save("rentals",JSONObject().apply{put("id",lid);put("tenant",tenant.text.toString());put("landlord",landlord.text.toString());put("property",property.text.toString());put("monthlyRent",monthly);put("deposit",deposit.text.toString().toDoubleOrNull()?:0.0);put("startDate",start.text.toString());put("endDate",end.text.toString());put("dueDay",d);put("escalationPercent",escPct);put("noticeDays",notice.text.toString().toIntOrNull()?:0);put("utilities",util.text.toString());put("status","ACTIVE")})
            val first=Calendar.getInstance();first.set(Calendar.DAY_OF_MONTH,d);save("rental_schedules",JSONObject().apply{put("id","SCHEDULE-$lid");put("leaseId",lid);put("monthlyRent",monthly);put("escalationPercent",escPct);put("firstDueDay",d);put("status","GENERATED")});Toast.makeText(this,"Lease saved and due schedule generated",Toast.LENGTH_LONG).show();showList(r,"rentals","Saved leases")
        })
        showList(r,"rentals","Saved leases");add(r,btn("HOME",Color.rgb(24,58,92)){home()});setContentView(ScrollView(this).apply{addView(r)})
    }

    private fun pickDate(target:EditText){val c=Calendar.getInstance();DatePickerDialog(this,{_,y,m,d->target.setText(String.format(Locale.US,"%02d%02d%04d",d,m+1,y))},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}

    private fun documents(){
        val r=page("Document & Consent Vault","All indexed evidence is local, versioned and linked to its module until a production vault/API is connected.")
        val id=field("Credit / party / asset reference");form(r,id);val t=Spinner(this);t.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("DPN","Credit Agreement","Guarantor Guarantee","Trade Invoice","Sanction Letter","Account Statement","Lease","Repayment Receipt","Asset Proof","Claim Document","Consent / OTP Evidence"));add(r,t,50)
        add(r,btn("SELECT + INDEX DOCUMENT",Color.rgb(0,145,135)){pendingDocumentType=t.selectedItem.toString();pendingDocumentOwner=id.text.toString();startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/pdf";addCategory(Intent.CATEGORY_OPENABLE)},7001)})
        add(r,TextView(this).apply{text="Critical lifecycle: DRAFT → SENT → VIEWED → CONSENT PENDING → OTP VERIFIED → COMPLETED → ARCHIVED\nExecution records include timestamp, actor, version and SHA-256 when generated by the platform engine.";textSize=12f},92)
        showList(r,"documents","Indexed documents");add(r,btn("OPEN LAST DOCUMENT",Color.rgb(25,145,78)){val x=store.all("documents").lastOrNull();if(x==null)Toast.makeText(this,"No indexed document",Toast.LENGTH_SHORT).show()else try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(x.optString("uri"))).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))}catch(_:Exception){Toast.makeText(this,"No viewer available",Toast.LENGTH_SHORT).show()}})
        add(r,btn("HOME",Color.rgb(24,58,92)){home()});setContentView(ScrollView(this).apply{addView(r)})
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(resultCode==RESULT_OK&&data?.data!=null&&(requestCode==7001||requestCode==7002||requestCode==7003)){val uri=data.data!!;contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);val id="DOC-${System.currentTimeMillis()}";val obj=JSONObject().apply{put("id",id);put("type",pendingDocumentType);put("ownerRef",pendingDocumentOwner);put("uri",uri.toString());put("version",1);put("status","INDEXED");put("createdAt",System.currentTimeMillis())};store.add("documents",obj);Toast.makeText(this,"Document indexed: $id",Toast.LENGTH_LONG).show()}}

    private fun assets(){
        val r=page("Asset Vault","Financial and non-financial assets with ownership/proof/nominee/maturity linkage.");val id=field("Asset ID (optional)"),cat=field("Asset category / institution *"),title=field("Asset title *"),owner=field("Owner / co-owner *"),ref=field("Account / registration reference"),value=field("Estimated value ₹"),nom=field("Nominee / beneficiary"),mat=field("Maturity / renewal date DDMMYYYY")
        listOf(cat,title,owner,ref,value,nom,mat).forEach{form(r,it)};add(r,btn("UPLOAD ASSET PROOF",Color.rgb(0,145,135)){pendingDocumentType="ASSET_PROOF";pendingDocumentOwner=if(id.text.isBlank())title.text.toString() else id.text.toString();startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="application/pdf";addCategory(Intent.CATEGORY_OPENABLE)},7002)})
        add(r,btn("SAVE ASSET",Color.rgb(25,145,78)){if(cat.text.isBlank()||title.text.isBlank()||owner.text.isBlank()){Toast.makeText(this,"Complete required asset fields",Toast.LENGTH_LONG).show();return@btn};val aid=if(id.text.isBlank())"ASSET-${System.currentTimeMillis()}" else id.text.toString();save("assets",JSONObject().apply{put("id",aid);put("category",cat.text.toString());put("title",title.text.toString());put("owner",owner.text.toString());put("reference",ref.text.toString());put("value",value.text.toString().toDoubleOrNull()?:0.0);put("nominee",nom.text.toString());put("maturity",mat.text.toString());put("status","ACTIVE")});Toast.makeText(this,"Asset saved",Toast.LENGTH_LONG).show()});showList(r,"assets","Saved assets");add(r,btn("HOME",Color.rgb(24,58,92)){home()});setContentView(ScrollView(this).apply{addView(r)})
    }

    private fun trusted(){val r=page("Nominee / Trusted Person","Permission is explicit and does not transfer ownership automatically.");val id=field("Permission ID (optional)"),name=field("Name *"),rel=field("Relationship *"),mobile=field("Mobile *"),level=Spinner(this);level.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("VIEW_ONLY","CLAIM_PREPARATION","DOCUMENT_ACCESS","EMERGENCY_CONTACT"));listOf(name,rel,mobile).forEach{form(r,it)};add(r,level,50);add(r,btn("SAVE PERMISSION",Color.rgb(25,145,78)){if(name.text.isBlank()||rel.text.isBlank()||!V5Validation.mobile(mobile.text.toString())){Toast.makeText(this,"Enter valid name, relationship and 10-digit mobile",Toast.LENGTH_LONG).show();return@btn};val pid=if(id.text.isBlank())"PERM-${System.currentTimeMillis()}" else id.text.toString();save("trusted_people",JSONObject().apply{put("id",pid);put("name",name.text.toString());put("relationship",rel.text.toString());put("mobile",mobile.text.toString());put("permission",level.selectedItem.toString());put("status","ACTIVE")});Toast.makeText(this,"Permission saved with audit timestamp",Toast.LENGTH_LONG).show()});showList(r,"trusted_people","Permissions");add(r,btn("HOME",Color.rgb(24,58,92)){home()});setContentView(ScrollView(this).apply{addView(r)})}

    private fun claims(){val r=page("Inheritance & Claims","Evidence-driven succession workflow with explicit states.");val id=field("Claim ID (optional)"),owner=field("Owner / deceased person *"),claimant=field("Claimant / legal heir *"),rel=field("Relationship *"),date=field("Succession date DDMMYYYY"),inst=field("Institution / authority *"),asset=field("Asset / policy reference *"),st=Spinner(this);st.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("IDENTIFIED","DOCUMENTS_PENDING","PREPARED","SUBMITTED","QUERY_RECEIVED","APPROVED","TRANSFERRED_CLOSED","DISPUTED"));listOf(owner,claimant,rel,date,inst,asset).forEach{form(r,it)};add(r,st,50);add(r,btn("CREATE / UPDATE CLAIM FILE",Color.rgb(25,145,78)){if(owner.text.isBlank()||claimant.text.isBlank()||rel.text.isBlank()||inst.text.isBlank()||asset.text.isBlank()){Toast.makeText(this,"Complete required claim fields",Toast.LENGTH_LONG).show();return@btn};val cid=if(id.text.isBlank())"CLAIM-${System.currentTimeMillis()}" else id.text.toString();save("claims",JSONObject().apply{put("id",cid);put("owner",owner.text.toString());put("claimant",claimant.text.toString());put("relationship",rel.text.toString());put("successionDate",date.text.toString());put("institution",inst.text.toString());put("assetRef",asset.text.toString());put("status",st.selectedItem.toString())});Toast.makeText(this,"Claim file saved",Toast.LENGTH_LONG).show()});showList(r,"claims","Claim files");add(r,btn("HOME",Color.rgb(24,58,92)){home()});setContentView(ScrollView(this).apply{addView(r)})}

    private fun legal(){val r=page("Legal Assistance","Evidence preparation and professional referral; this is not legal advice.");val ref=field("Credit / claim / asset reference");form(r,ref);val issue=field("Issue / default / claim summary");form(r,issue);add(r,btn("PREPARE EVIDENCE BUNDLE",Color.rgb(210,135,15)){val id="BUNDLE-${System.currentTimeMillis()}";save("legal_bundles",JSONObject().apply{put("id",id);put("reference",ref.text.toString());put("issue",issue.text.toString());put("sources","DPN/agreement/guarantee; consent/OTP; repayment history; formal audit; asset proof; claim records");put("status","READY_FOR_REVIEW")});AlertDialog.Builder(this).setTitle("Evidence bundle checklist").setMessage("Bundle: contractual documents, consent/OTP evidence, repayment ledger, audit timeline, outstanding calculation, source documents and asset/claim evidence.").setPositiveButton("OK",null).show()});add(r,btn("PROFESSIONAL REFERRAL",Color.rgb(24,58,92)){Toast.makeText(this,"Professional referral request recorded as a future integration point",Toast.LENGTH_LONG).show()};showList(r,"legal_bundles","Prepared bundles");add(r,btn("HOME",Color.rgb(24,58,92)){home()});setContentView(ScrollView(this).apply{addView(r)})}

    private fun score(){val r=page("Udhaardaar Score & Readiness","Consent-controlled, explainable, versioned local score; not a statutory bureau score.");val p=field("Profile ID");form(r,p);add(r,btn("CALCULATE SCORE",Color.rgb(25,111,220)){val x=V5PlatformEngine(this).calculateScore(p.text.toString());save("score_snapshots",JSONObject().apply{put("id","SCORE-${System.currentTimeMillis()}");put("profileId",p.text.toString());put("score",x.optInt("score"));put("version",x.optString("version"));put("confidence",x.optString("confidence"));put("factors",x.optString("factors"));put("consent","REQUIRED_FOR_SHARING")});AlertDialog.Builder(this).setTitle("V5 Score: ${x.optInt("score")}").setMessage("Version: ${x.optString("version")}\nConfidence: ${x.optString("confidence")}\nFactors: ${x.optString("factors")}\nSharing: explicit consent required").setPositiveButton("OK",null).show()});showList(r,"score_snapshots","Score snapshots");add(r,btn("HOME",Color.rgb(24,58,92)){home()});setContentView(ScrollView(this).apply{addView(r)})}

    private fun profile(){val m=prefs.getString("current_mobile","")?:"";val name=prefs.getString("name_$m","User")?:"User";AlertDialog.Builder(this).setTitle("My Profile").setMessage("$name\nMobile: $m\nV5 private-by-default vault; explicit consent controls sharing.").setPositiveButton("OK",null).show()}
}
