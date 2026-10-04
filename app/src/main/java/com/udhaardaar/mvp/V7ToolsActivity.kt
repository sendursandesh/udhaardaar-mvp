package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.widget.*
import com.journeyapps.barcodescanner.IntentIntegrator
import com.journeyapps.barcodescanner.IntentResult
import androidx.appcompat.app.AppCompatActivity

class V7ToolsActivity : V7SessionActivity() {
    private class DonutView(context: android.content.Context) : View(context) {
        private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect=RectF()
        private var values:List<Pair<String,Double>> = emptyList()
        private val palette=intArrayOf(0xFFD69408.toInt(),0xFF173B67.toInt(),0xFF5C8DB8.toInt(),0xFF7A9E5C.toInt(),0xFF9B6A8C.toInt(),0xFF8A7B5A.toInt())
        fun setValues(v:List<Pair<String,Double>>) { values=v.filter{it.second>0}.take(6); invalidate() }
        override fun onDraw(canvas:Canvas) {
            super.onDraw(canvas)
            val total=values.sumOf{it.second}
            if(total<=0){paint.color=0xFF6B7280.toInt();paint.textSize=34f;canvas.drawText("No data",width/2f-55f,height/2f,paint);return}
            val size=minOf(width,height)*0.56f
            val left=(width-size)/2f
            val top=24f
            rect.set(left,top,left+size,top+size)
            var angle=-90f
            values.forEachIndexed{i,p->
                val sweep=(p.second/total*360.0).toFloat()
                paint.color=palette[i%palette.size];paint.style=Paint.Style.FILL;canvas.drawArc(rect,angle,sweep,true,paint);angle+=sweep
            }
            paint.color=0xFFFFFAF1.toInt();paint.style=Paint.Style.FILL
            val hole=size*0.48f;val cx=left+size/2;val cy=top+size/2
            canvas.drawCircle(cx,cy,hole/2,paint)
            paint.color=0xFF173B67.toInt();paint.textSize=28f;paint.textAlign=Paint.Align.CENTER;canvas.drawText("100%",cx,cy+10,paint)
            paint.textAlign=Paint.Align.LEFT
            var y=top+size+38
            values.forEachIndexed{i,p->
                paint.color=palette[i%palette.size];canvas.drawCircle(18f,y-7,7f,paint)
                paint.color=0xFF173B67.toInt();paint.textSize=20f
                canvas.drawText(p.first + ": ₹" + "%.0f".format(p.second),32f,y,paint)
                y+=28
            }
        }
    }
    private var qrReferenceField: EditText? = null
    private var qrMerchantField: EditText? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                qrReferenceField?.setText(result.contents)
                Toast.makeText(this, "QR scanned. Review the details before recording.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "QR scan cancelled.", Toast.LENGTH_SHORT).show()
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private val d get()=resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()
    private fun input(h:String)=ArthSaathiV7Design.input(this,h)
    override fun onCreate(b:Bundle?){super.onCreate(b);render(intent.getStringExtra("tool")?:"PORTFOLIO")}
    private fun shell(title:String,sub:String):LinearLayout{
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),dp(7),dp(10),dp(22));setBackgroundColor(ArthSaathiV7Design.CREAM)}
        val h=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=ArthSaathiV7Design.bg(this@V7ToolsActivity);setPadding(dp(10),dp(8),dp(10),dp(12))}
        h.addView(ArthSaathiV7Design.masthead(this));h.addView(ArthSaathiV7Design.text(this,title,21f,android.graphics.Color.WHITE,true));h.addView(ArthSaathiV7Design.text(this,sub,10.5f,ArthSaathiV7Design.GOLD_PALE))
        r.addView(h);return r
    }
    private fun render(tool:String){
        val r=when(tool){"PORTFOLIO"->portfolio();"OPPORTUNITY"->opportunity();"SCENARIO"->scenario();"MARKET"->market();"REPORTS"->reports();"ADDRESS"->address();"REVENUE"->revenue();"MIS"->mis();"SCORE"->score();"INTEGRATION"->integration();"ADVOCATE"->advocate();"CLAIM"->claim();"AI"->ai();"SECURITY"->security();"WILL"->will();"DOCUMENTS"->documents();"PEOPLE"->people();"INSURANCE"->insurance();"TTMM_CREATE"->ttmmCreate();"TTMM_CONTRIBUTION"->ttmmContribution();"TTMM_SETTLE"->ttmmSettle();"TTMM_HISTORY"->ttmmHistory();"QR_SCAN"->qrScan();"QR_RECORD"->qrRecord();"QR_CONSENT"->qrConsent();"QR_BALANCE"->qrBalance();"LEGAL"->legal();"BENEFITS"->benefits();"CHARGECHECK"->chargeCheck();"NOMINEE"->nominee();"ALERTS"->alerts();else->unknownTool(tool)}
        r.addView(ArthSaathiV7Design.goldButton(this,"Back"){finish()},LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(12)})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }
    private fun will():LinearLayout {
        val r=shell("Will & Inheritance","Link recorded assets to a will record. This stores the relationship; legal drafting remains subject to the selected legal workflow.")
        val assets=V7Core.all(this,V7Core.Keys.ASSETS)
        val labels=if(assets.isEmpty()) listOf("No recorded assets") else assets.map{"₹ %.2f • %s".format(it.optDouble("currentValue",it.optDouble("value",0.0)),it.optString("type","Asset"))}
        val spinner=Spinner(this).apply{adapter=ArrayAdapter(this@V7ToolsActivity,android.R.layout.simple_spinner_dropdown_item,labels)}
        val testator=input("Testator / owner")
        val willRef=input("Will / document reference")
        r.addView(testator,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        r.addView(willRef,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        r.addView(ArthSaathiV7Design.text(this,"Asset to link",10f,ArthSaathiV7Design.MUTED,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        r.addView(spinner,LinearLayout.LayoutParams(-1,dp(48)))
        val out=ArthSaathiV7Design.text(this,"",11f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Link Asset to Will"){
            if(assets.isEmpty()){out.text="Record an asset first in Asset Vault.";return@goldButton}
            if(testator.text.isBlank()||willRef.text.isBlank()){out.text="Testator and will/document reference are required.";return@goldButton}
            val w=org.json.JSONObject().apply{
                put("id",V7Core.id("WILL"));put("testator",testator.text.toString().trim());put("documentReference",willRef.text.toString().trim())
                put("assetId",assets[spinner.selectedItemPosition].optString("id"));put("status","RECORDED");put("createdAt",V7Core.now())
            }
            V7Core.add(this,V7Core.Keys.WILL,w)
            out.text="Asset linked to will record "+w.getString("id")+"."
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        return r
    }

    private fun unknownTool(tool:String):LinearLayout {
        val r=shell("Unavailable V7 function","This function has no registered destination yet; it will not silently open another module.")
        r.addView(ArthSaathiV7Design.text(this,"Destination not registered: $tool",11f,ArthSaathiV7Design.NAVY,true))
        return r
    }
    private fun documents():LinearLayout {
        val r=shell("My Documents","Keep important papers and evidence references in one place. Documents can be linked to credits, assets, insurance and claims.")
        val name=input("Document name")
        val type=input("Document type (Aadhaar / PAN / deed / invoice / policy / other)")
        val reference=input("Document number / reference")
        listOf(name,type,reference).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})}
        val out=ArthSaathiV7Design.text(this,"",10.5f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Save Document Reference"){
            if(name.text.isBlank()){name.error="Document name is required";return@goldButton}
            V7Core.add(this,V7Core.Keys.DOCUMENTS,org.json.JSONObject().apply{
                put("id",V7Core.id("DOC"));put("name",name.text.toString().trim());put("type",type.text.toString().trim());put("reference",reference.text.toString().trim());put("status","RECORDED");put("createdAt",V7Core.now())
            })
            out.text="Document reference saved. You can link the evidence to the relevant asset, credit or claim."
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        val docs=V7Core.all(this,V7Core.Keys.DOCUMENTS)
        r.addView(ArthSaathiV7Design.section(this,"Saved Documents","Recorded document references."))
        r.addView(ArthSaathiV7Design.text(this,if(docs.isEmpty())"No documents recorded yet." else docs.takeLast(30).reversed().joinToString("\n\n"){"• "+it.optString("name")+" • "+it.optString("type")+" • "+it.optString("reference")},11f,ArthSaathiV7Design.NAVY))
        return r
    }

    private fun people():LinearLayout {
        val r=shell("People & Relationships","Borrowers, guarantors and authorised people recorded in V7.")
        val people=V7Core.all(this,V7Core.Keys.PEOPLE)
        r.addView(ArthSaathiV7Design.text(this,if(people.isEmpty()) "No profiles recorded." else people.joinToString("\n\n"){ "• "+it.optString("name")+"  •  "+it.optString("mobile") },11f,ArthSaathiV7Design.NAVY))
        r.addView(ArthSaathiV7Design.goldButton(this,"Edit Profiles"){startActivity(Intent(this,V7NativeModuleActivity::class.java).putExtra("module","RECORD"))},LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        return r
    }
    private fun insurance():LinearLayout {
        val r=shell("Insurance Policies","Record policy details, renewal dates, nominees and claim references.")
        val name=input("Policy / plan name");val insurer=input("Insurance company");val number=input("Policy number");val premium=input("Premium (₹)");val renewal=input("Renewal date");val nominee=input("Nominee")
        listOf(name,insurer,number,premium,renewal,nominee).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        val out=ArthSaathiV7Design.text(this,"",10.5f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Save Insurance Policy"){
            if(name.text.isBlank()||insurer.text.isBlank()){Toast.makeText(this,"Policy name and insurer are required.",Toast.LENGTH_SHORT).show();return@goldButton}
            val p=org.json.JSONObject().apply{
                put("id",V7Core.id("POL"));put("name",name.text.toString().trim());put("insurer",insurer.text.toString().trim());put("policyNumber",number.text.toString().trim())
                put("premium",premium.text.toString().toDoubleOrNull()?:0.0);put("renewalDate",renewal.text.toString().trim());put("nominee",nominee.text.toString().trim());put("status","ACTIVE")
            }
            V7Core.add(this,V7Core.Keys.POLICIES,p);out.text="Insurance policy saved. Renewal alerts will use the recorded date when available."
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        val policies=V7Core.all(this,V7Core.Keys.POLICIES)
        r.addView(ArthSaathiV7Design.section(this,"Saved Policies","Your recorded insurance policies."))
        r.addView(ArthSaathiV7Design.text(this,if(policies.isEmpty())"No policies recorded." else policies.takeLast(20).reversed().joinToString("\n\n"){"• "+it.optString("name")+" • "+it.optString("insurer")+" • "+it.optString("policyNumber")},11f,ArthSaathiV7Design.NAVY))
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        return r
    }

    private fun ttmm():LinearLayout {
        val r=shell("TTMM — Share & Settle","Shared expenses, contributions and settlements.")
        val records=V7Core.all(this,V7Core.Keys.TTMM)
        r.addView(ArthSaathiV7Design.text(this,if(records.isEmpty()) "No TTMM records yet." else records.joinToString("\n\n"){ "• "+it.optString("groupName",it.optString("description","Shared expense")) },11f,ArthSaathiV7Design.NAVY))
        r.addView(ArthSaathiV7Design.text(this,"Use a structured group record for equal, custom, percentage or shares-based splits. Each settlement should carry consent and an audit event.",10.5f,ArthSaathiV7Design.MUTED).apply{setPadding(0,dp(8),0,0)})
        return r
    }
    private fun qr():LinearLayout {
        val r=shell("QR Udhaar Khata","Record merchant credit and repayment without silently changing balances.")
        val qr=V7Core.all(this,V7Core.Keys.QR)
        r.addView(ArthSaathiV7Design.text(this,if(qr.isEmpty()) "No QR Khata records yet." else qr.joinToString("\n\n"){ "• "+it.optString("merchant",it.optString("partyId","Record"))+"  |  ₹"+it.optDouble("amount",0.0) },11f,ArthSaathiV7Design.NAVY))
        return r
    }
    private fun legal():LinearLayout {
        val r=shell("Legal & Claims","Claims, legal matters and advocate records in the V7 data model.")
        val claims=V7Core.all(this,V7Core.Keys.CLAIMS)
        val legal=V7Core.all(this,V7Core.Keys.LEGAL)
        r.addView(ArthSaathiV7Design.text(this,"Claims: "+claims.size+"\nLegal matters: "+legal.size,11f,ArthSaathiV7Design.NAVY,true))
        r.addView(ArthSaathiV7Design.outlineButton(this,"Open Claim Assistance"){startActivity(Intent(this,V7ToolsActivity::class.java).putExtra("tool","CLAIM"))},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(8)})
        return r
    }
    private fun benefits():LinearLayout {
        val r=shell("Benefits & Refunds","Record money or support you have actually received. ArthSaathi does not claim eligibility automatically.")
        val name=input("Benefit / refund name");val source=input("Source / department / institution");val amount=input("Amount received (₹)");val date=input("Received date");val reference=input("Reference / proof")
        listOf(name,source,amount,date,reference).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        val out=ArthSaathiV7Design.text(this,"",10.5f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Save Benefit / Refund"){
            val a=amount.text.toString().toDoubleOrNull()
            if(name.text.isBlank()||a==null||a<0){Toast.makeText(this,"Name and a valid amount are required.",Toast.LENGTH_SHORT).show();return@goldButton}
            V7Core.add(this,V7Core.Keys.BENEFITS,org.json.JSONObject().apply{
                put("id",V7Core.id("BEN"));put("name",name.text.toString().trim());put("source",source.text.toString().trim());put("amount",a);put("date",date.text.toString().trim());put("reference",reference.text.toString().trim());put("createdAt",V7Core.now())
            })
            out.text="Benefit/refund recorded as received."
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        return r
    }

    private fun chargeCheck():LinearLayout {
        val r=shell("Check Loan / Bank Charges","Compare what was sanctioned or promised with what was actually charged. Enter only amounts supported by your documents or statements.")
        val item=input("Charge name (processing / legal / insurance / other)")
        val sanctioned=input("Promised / sanctioned amount (₹)")
        val actual=input("Actual amount charged (₹)")
        val source=input("Source / document reference")
        val out=ArthSaathiV7Design.text(this,"",11f,ArthSaathiV7Design.NAVY)
        listOf(item,sanctioned,actual,source).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        r.addView(ArthSaathiV7Design.goldButton(this,"Compare Charges"){
            val s=sanctioned.text.toString().toDoubleOrNull();val a=actual.text.toString().toDoubleOrNull()
            if(s==null||a==null||s<0||a<0){out.text="Enter valid sanctioned and actual amounts.";return@goldButton}
            val diff=a-s
            out.text=when{diff>0.005->"Actual charge is ₹%.2f higher than promised.".format(diff);diff < -0.005->"Actual charge is ₹%.2f lower than promised.".format(-diff);else->"Actual charge matches the promised amount."}
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Save Comparison"){
            val s=sanctioned.text.toString().toDoubleOrNull();val a=actual.text.toString().toDoubleOrNull()
            if(item.text.isBlank()||s==null||a==null||s<0||a<0){out.text="Complete the charge comparison first.";return@outlineButton}
            V7Core.add(this,V7Core.Keys.CHARGE_CHECKS,org.json.JSONObject().apply{
                put("id",V7Core.id("CHG"));put("item",item.text.toString().trim());put("sanctioned",s);put("actual",a);put("difference",a-s);put("source",source.text.toString().trim());put("createdAt",V7Core.now())
            })
            out.text="Charge comparison saved for your records."
        },LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(7)})
        return r
    }

    private fun ttmmCreate():LinearLayout = shell("TTMM — Create / Open Group","Create a named shared-expense group and record its members.")
        .apply {
            val group=input("Group name"); val members=input("Members / participant references")
            addView(group); addView(members,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
            addView(ArthSaathiV7Design.goldButton(this@V7ToolsActivity,"Create Group"){
                if(group.text.isBlank()){group.error="Group name required";return@goldButton}
                val o=org.json.JSONObject().apply{put("id",V7Core.id("TTMMG"));put("groupName",group.text.toString().trim());put("members",members.text.toString().trim());put("createdAt",V7Core.now())}
                V7Core.add(this@V7ToolsActivity,V7Core.Keys.TTMM,o);Toast.makeText(this@V7ToolsActivity,"TTMM group created.",Toast.LENGTH_SHORT).show()
            },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        }

    private fun ttmmContribution():LinearLayout = shell("TTMM — Add Shared Expense","Enter one group expense. ArthSaathi calculates the equal share so the group can see who should settle.")
        .apply {
            val group=input("Group name")
            val payer=input("Who paid?")
            val participants=input("Participants (comma separated)")
            val amount=input("Total expense (₹)")
            val purpose=input("What was the expense for?")
            listOf(group,payer,participants,amount,purpose).forEach{addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
            val split=ArthSaathiV7Design.text(this@V7ToolsActivity,"Enter participants to calculate each person's equal share.",10.5f,ArthSaathiV7Design.MUTED)
            addView(split,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
            addView(ArthSaathiV7Design.outlineButton(this@V7ToolsActivity,"Calculate Equal Share"){
                val a=amount.text.toString().toDoubleOrNull()
                val names=participants.text.toString().split(",").map{it.trim()}.filter{it.isNotBlank()}.distinct()
                if(a==null||a<=0||names.isEmpty()){split.text="Enter a valid amount and at least one participant.";return@outlineButton}
                split.text="Participants: %d\nEqual share per person: ₹%.2f\n%s".format(names.size,a/names.size,names.joinToString(", "))
            },LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(8)})
            addView(ArthSaathiV7Design.goldButton(this@V7ToolsActivity,"Save Shared Expense"){
                val a=amount.text.toString().toDoubleOrNull()
                val names=participants.text.toString().split(",").map{it.trim()}.filter{it.isNotBlank()}.distinct()
                if(group.text.isBlank()||payer.text.isBlank()||a==null||a<=0||names.isEmpty()){Toast.makeText(this@V7ToolsActivity,"Group, payer, participants and valid amount are required.",Toast.LENGTH_SHORT).show();return@goldButton}
                val share=a/names.size
                V7Core.add(this@V7ToolsActivity,V7Core.Keys.TTMM,org.json.JSONObject().apply{
                    put("id",V7Core.id("TTME"));put("groupName",group.text.toString().trim());put("payer",payer.text.toString().trim());put("amount",a)
                    put("purpose",purpose.text.toString().trim());put("type","SHARED_EXPENSE");put("participants",names.joinToString(","));put("equalShare",share);put("createdAt",V7Core.now())
                })
                Toast.makeText(this@V7ToolsActivity,"Shared expense saved. Each participant's equal share is ₹%.2f.".format(share),Toast.LENGTH_LONG).show()
            },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        }

    private fun ttmmSettle():LinearLayout = shell("TTMM — Settle","Record a specific settlement between participants.")
        .apply {
            val group=input("Group ID / name"); val from=input("Paid by"); val to=input("Settled with"); val amount=input("Settlement amount (₹)")
            listOf(group,from,to,amount).forEach{addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
            addView(ArthSaathiV7Design.goldButton(this@V7ToolsActivity,"Record Settlement"){
                val a=amount.text.toString().toDoubleOrNull();if(group.text.isBlank()||from.text.isBlank()||to.text.isBlank()||a==null||a<=0){Toast.makeText(this@V7ToolsActivity,"Complete the settlement details.",Toast.LENGTH_SHORT).show();return@goldButton}
                V7Core.add(this@V7ToolsActivity,V7Core.Keys.TTMM,org.json.JSONObject().apply{put("id",V7Core.id("TTMS"));put("groupName",group.text.toString().trim());put("from",from.text.toString().trim());put("to",to.text.toString().trim());put("amount",a);put("type","SETTLEMENT");put("createdAt",V7Core.now())});Toast.makeText(this@V7ToolsActivity,"Settlement recorded.",Toast.LENGTH_SHORT).show()
            },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        }

    private fun ttmmHistory():LinearLayout = shell("TTMM — History","Review shared-expense contributions and settlements; this screen does not create or mutate records.")
        .apply {
            val records=V7Core.all(this@V7ToolsActivity,V7Core.Keys.TTMM).sortedBy{it.optLong("createdAt")}
            addView(ArthSaathiV7Design.text(this@V7ToolsActivity,if(records.isEmpty()) "No TTMM records yet." else records.joinToString("\n\n"){ "• "+it.optString("type","GROUP")+" | "+it.optString("groupName","")+" | ₹"+it.optDouble("amount",0.0)+" | "+it.optString("createdAt") },11f,ArthSaathiV7Design.NAVY))
        }

    private fun qrScan():LinearLayout = shell("QR Udhaar Khata — Scan / Identify","Scan a merchant QR or transaction code first. Nothing is recorded until you review and confirm.")
        .apply {
            val ref=input("QR / transaction reference")
            val merchant=input("Merchant / party")
            qrReferenceField=ref
            qrMerchantField=merchant
            addView(ref)
            addView(merchant,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
            addView(ArthSaathiV7Design.goldButton(this@V7ToolsActivity,"Scan QR Code"){
                IntentIntegrator(this@V7ToolsActivity).apply{
                    setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                    setPrompt("Scan the merchant or transaction QR")
                    setBeepEnabled(true)
                    setOrientationLocked(false)
                }.initiateScan()
            },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
            addView(ArthSaathiV7Design.text(this@V7ToolsActivity,"After scanning, review the merchant/reference and continue to Record Khata. Scanning never changes a balance by itself.",10.5f,ArthSaathiV7Design.MUTED).apply{setPadding(0,dp(8),0,0)})
        }

    private fun qrRecord():LinearLayout = shell("QR Khata — Record","Create a merchant credit/repayment ledger record after identification.")
        .apply {
            val merchant=input("Merchant / party");val amount=input("Transaction amount (₹)");val type=input("Type (CREDIT / REPAYMENT)");listOf(merchant,amount,type).forEach{addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
            addView(ArthSaathiV7Design.goldButton(this@V7ToolsActivity,"Record Khata Entry"){
                val a=amount.text.toString().toDoubleOrNull();if(merchant.text.isBlank()||a==null||a<=0){Toast.makeText(this@V7ToolsActivity,"Merchant and valid amount are required.",Toast.LENGTH_SHORT).show();return@goldButton}
                V7Core.add(this@V7ToolsActivity,V7Core.Keys.QR,org.json.JSONObject().apply{put("id",V7Core.id("QR"));put("merchant",merchant.text.toString().trim());put("amount",a);put("type",type.text.toString().trim().uppercase());put("createdAt",V7Core.now())});Toast.makeText(this@V7ToolsActivity,"QR Khata entry recorded.",Toast.LENGTH_SHORT).show()
            },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        }

    private fun qrConsent():LinearLayout = shell("QR Khata — Consent","Verify consent before a QR Khata mutation is treated as authorised.")
        .apply {
            val subject=input("Party / subject ID");val purpose=input("Consent purpose");addView(subject);addView(purpose,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
            addView(ArthSaathiV7Design.goldButton(this@V7ToolsActivity,"Record Verified Consent"){
                if(subject.text.isBlank()||purpose.text.isBlank()){Toast.makeText(this@V7ToolsActivity,"Subject and purpose are required.",Toast.LENGTH_SHORT).show();return@goldButton}
                V7Core.add(this@V7ToolsActivity,V7Core.Keys.CONSENTS,org.json.JSONObject().apply{put("id",V7Core.id("CONS"));put("subjectId",subject.text.toString().trim());put("purpose",purpose.text.toString().trim());put("verified",true);put("status","GRANTED");put("createdAt",V7Core.now());put("withdrawn",false)});Toast.makeText(this@V7ToolsActivity,"Verified consent recorded.",Toast.LENGTH_SHORT).show()
            },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        }

    private fun qrBalance():LinearLayout = shell("QR Khata — Balance","Review outstanding QR Khata entries without creating or changing records.")
        .apply {
            val rows=V7Core.all(this@V7ToolsActivity,V7Core.Keys.QR);val total=rows.filter{it.optString("type","CREDIT")=="CREDIT"}.sumOf{it.optDouble("amount",0.0)}-rows.filter{it.optString("type")=="REPAYMENT"}.sumOf{it.optDouble("amount",0.0)}
            addView(ArthSaathiV7Design.text(this@V7ToolsActivity,"Recorded QR Khata net balance: ₹%.2f".format(total),12f,ArthSaathiV7Design.NAVY,true))
        }

    private fun nominee():LinearLayout {
        val r=shell("Nominee & Family","Choose who should receive or access a recorded asset, subject to the relevant legal documents.")
        val people=V7Core.all(this,V7Core.Keys.PEOPLE)
        val ownerLabels=if(people.isEmpty()) listOf("No profile recorded") else people.map{it.optString("name")+" • "+it.optString("mobile")}
        val owner=Spinner(this).apply{adapter=ArrayAdapter(this@V7ToolsActivity,android.R.layout.simple_spinner_dropdown_item,ownerLabels)}
        val name=input("Nominee full name")
        val relation=input("Relationship (e.g. spouse, son, daughter)")
        val mobile=input("Nominee mobile (optional)")
        val share=input("Share % (optional)")
        r.addView(ArthSaathiV7Design.text(this,"Whose nominee?",10f,ArthSaathiV7Design.MUTED,true))
        r.addView(owner,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(3)})
        listOf(name,relation,mobile,share).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})}
        val out=ArthSaathiV7Design.text(this,"",10.5f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Save Nominee"){
            if(people.isEmpty()){out.text="Create a profile first.";return@goldButton}
            if(name.text.isBlank()||relation.text.isBlank()){out.text="Nominee name and relationship are required.";return@goldButton}
            val pct=share.text.toString().toDoubleOrNull()?:0.0
            if(pct<0||pct>100){share.error="Share must be 0–100%";return@goldButton}
            V7Core.add(this@V7ToolsActivity,V7Core.Keys.NOMINEES,org.json.JSONObject().apply{
                put("id",V7Core.id("NOM"));put("ownerId",people[owner.selectedItemPosition].optString("id"));put("name",name.text.toString().trim())
                put("relationship",relation.text.toString().trim());put("mobile",mobile.text.toString().trim());put("sharePercent",pct);put("status","ACTIVE");put("createdAt",V7Core.now())
            })
            out.text="Nominee saved. Link this nominee to individual assets in Asset Vault when required."
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        val existing=V7Core.all(this,V7Core.Keys.NOMINEES)
        r.addView(ArthSaathiV7Design.section(this,"Saved Nominees","Nominees currently recorded in ArthSaathi."))
        r.addView(ArthSaathiV7Design.text(this,if(existing.isEmpty())"No nominees recorded." else existing.joinToString("\n\n"){"• "+it.optString("name")+" • "+it.optString("relationship")+" • "+it.optString("sharePercent")+"%"},11f,ArthSaathiV7Design.NAVY))
        r.addView(out)
        return r
    }

    private fun alerts():LinearLayout {
        val r=shell("Renewal & Due Alerts","See reminders generated from recorded due dates, repayments, renewals and other events.")
        V7AlertEngine.evaluate(this)
        val alerts=V7Core.all(this,V7Core.Keys.ALERTS).sortedByDescending{it.optLong("createdAt")}
        r.addView(ArthSaathiV7Design.text(this,if(alerts.isEmpty())"No active alerts from the records available." else alerts.take(30).joinToString("\n\n"){
            "• "+it.optString("title",it.optString("type","Alert"))+"\n"+it.optString("message",it.optString("detail","Review the related record."))
        },11f,ArthSaathiV7Design.NAVY))
        return r
    }

    private fun security():LinearLayout{
        val r=shell("Security & Consent","Review consent records, audit history and the current protected session.")
        val user=V7Core.user(this)
        val consents=V7Core.all(this,V7Core.Keys.CONSENTS)
        val audits=V7Core.all(this,V7Core.Keys.AUDIT)
        r.addView(ArthSaathiV7Design.section(this,"Protection status","Actions that change shared financial records should carry consent and an audit trail."))
        r.addView(ArthSaathiV7Design.text(this,"Current session: "+user,11f,ArthSaathiV7Design.NAVY,true))
        r.addView(ArthSaathiV7Design.text(this,"Verified consents: "+consents.count{it.optBoolean("verified")&&!it.optBoolean("withdrawn")}+"\nAudit events: "+audits.size,11f,ArthSaathiV7Design.MUTED))
        r.addView(ArthSaathiV7Design.outlineButton(this,"Record Test Consent"){
            V7Core.consent(this,user,"USER_TEST","V7.0",true)
            Toast.makeText(this,"Consent recorded and audited.",Toast.LENGTH_SHORT).show()
        },LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(9)})
        return r
    }

    private fun portfolio():LinearLayout{
        val r=shell("Portfolio Intelligence","See what you own, how it is performing and what alternatives may mean.")
        val name=input("Portfolio name");val risk=input("Risk profile (Conservative / Moderate / Growth)");val constitution=input("Portfolio constitution / allocation")
        r.addView(ArthSaathiV7Design.section(this,"Portfolio Record","Portfolio feeds MIS, scenarios and opportunity-cost analysis."))
        listOf(name,risk,constitution).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})}
        r.addView(ArthSaathiV7Design.goldButton(this,"Create Portfolio"){
            if(name.text.isBlank()){name.error="Required";return@goldButton}
            V7PortfolioEngine.createPortfolio(this,name.text.toString(),risk.text.toString(),constitution.text.toString());Toast.makeText(this,"Portfolio recorded.",Toast.LENGTH_SHORT).show()
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Run Opportunity-Cost Scenario"){startActivity(Intent(this,V7ToolsActivity::class.java).putExtra("tool","OPPORTUNITY"))},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(7)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Record Market Data Snapshot"){startActivity(Intent(this,V7ToolsActivity::class.java).putExtra("tool","MARKET"))},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(7)})
        return r
    }
    private fun opportunity():LinearLayout{
        val r=shell("Opportunity Cost","Compare a recorded position with an alternative. Analysis only; no transaction is executed.")
        val amount=input("Current amount");val cr=input("Current annual return %");val ar=input("Alternative annual return %");val years=input("Period in years");val exit=input("Exit cost");val tax=input("Estimated tax cost")
        listOf(amount,cr,ar,years,exit,tax).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        val out=ArthSaathiV7Design.text(this,"Enter assumptions and calculate.",11f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Calculate Opportunity Cost"){
            val x=V7PortfolioEngine.opportunityCost(amount.text.toString().toDoubleOrNull()?:0.0,cr.text.toString().toDoubleOrNull()?:0.0,ar.text.toString().toDoubleOrNull()?:0.0,years.text.toString().toDoubleOrNull()?:0.0,0.0,exit.text.toString().toDoubleOrNull()?:0.0,tax.text.toString().toDoubleOrNull()?:0.0)
            out.text="Current projected: ₹%.2f\nAlternative projected: ₹%.2f\nGross opportunity difference: ₹%.2f\nEstimated costs: ₹%.2f\nNet difference: ₹%.2f".format(x.getDouble("currentProjectedValue"),x.getDouble("alternativeProjectedValue"),x.getDouble("grossOpportunityCost"),x.getDouble("estimatedCosts"),x.getDouble("netOpportunityDifference"))
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(12)});return r
    }
    private fun scenario():LinearLayout{
        val r=shell("Scenario Analysis","Model a defined what-if case separately from opportunity-cost analysis.")
        val base=input("Current value (₹)")
        val growth=input("Expected annual growth %")
        val years=input("Period (years)")
        val contribution=input("Annual contribution (₹)")
        val out=ArthSaathiV7Design.text(this,"Enter assumptions to model the scenario.",11f,ArthSaathiV7Design.NAVY)
        listOf(base,growth,years,contribution).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        r.addView(ArthSaathiV7Design.goldButton(this,"Run Scenario"){
            val b=base.text.toString().toDoubleOrNull()
            val g=growth.text.toString().toDoubleOrNull()
            val y=years.text.toString().toDoubleOrNull()
            val a=contribution.text.toString().toDoubleOrNull()?:0.0
            if(b==null||b<0||g==null||y==null||y<=0){out.text="Enter valid base value, growth and period.";return@goldButton}
            val baseValue=b!!; val growthValue=g!!; val yearsValue=y!!
            val factor=Math.pow(1.0+growthValue/100.0,yearsValue)
            val denominator=growthValue/100.0
            val future=baseValue*factor+a*((factor-1.0)/(denominator.takeIf{Math.abs(it)>1e-9}?:yearsValue))
            out.text="Scenario value after %.1f years: ₹%.2f".format(y,future)
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        return r
    }

    private fun reports():LinearLayout{
        val r=shell("Reports","Generated views from recorded ArthSaathi data. Reports are distinct from MIS.")
        val out=ArthSaathiV7Design.text(this,"Select a report to generate.",11f,ArthSaathiV7Design.NAVY)
        val types=Spinner(this).apply{adapter=ArrayAdapter(this@V7ToolsActivity,android.R.layout.simple_spinner_dropdown_item,
            listOf("Financial Position","Credit & Repayment","Assets & Nominees","Revenue & Payments","Audit & Consent"))}
        r.addView(types,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(7)})
        r.addView(ArthSaathiV7Design.goldButton(this,"Generate Report"){
            val m=V7MIS.snapshot(this)
            out.text=when(types.selectedItemPosition){
                0->"Financial Position\nAssets: ₹%.2f\nLiabilities: ₹%.2f\nNet worth: ₹%.2f".format(m.optDouble("assets"),m.optDouble("liabilities"),m.optDouble("netWorth"))
                1->"Credit & Repayment\nActive credits: %d\nReceivables: ₹%.2f\nPayables: ₹%.2f".format(m.optInt("activeCredits"),m.optDouble("receivables"),m.optDouble("payables"))
                2->"Assets & Nominees\nAssets recorded: ₹%.2f".format(m.optDouble("assets"))
                3->"Revenue & Payments\nReconciled revenue: ₹%.2f\nRecorded payments: ₹%.2f\nInvoices: %d".format(m.optDouble("reconciledRevenue"),m.optDouble("recordedPayments"),m.optInt("invoiceCount"))
                else->"Audit & Consent\nAudit events: %d".format(m.optInt("auditEvents"))
            }
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        return r
    }

    private fun market():LinearLayout{
        val r=shell("Market Data","Every market value is stored with source, timestamp and freshness.")
        val i=input("Instrument / product");val s=input("Source");val v=input("Observed value");val f=input("Freshness in minutes")
        listOf(i,s,v,f).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        r.addView(ArthSaathiV7Design.goldButton(this,"Save Market Snapshot"){
            val value=v.text.toString().toDoubleOrNull();if(i.text.isBlank()||s.text.isBlank()||value==null){Toast.makeText(this,"Instrument, source and value are required.",Toast.LENGTH_SHORT).show();return@goldButton}
            V7PortfolioEngine.recordMarketData(this,i.text.toString(),s.text.toString(),value,V7Core.now(),f.text.toString().toIntOrNull()?:60);Toast.makeText(this,"Market snapshot saved.",Toast.LENGTH_SHORT).show()
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)});return r
    }
    private fun address():LinearLayout{
        val r=shell("Address & Location","PIN-assisted and map-assisted capture. Suggestions always require user confirmation.")
        val owner=input("Person / business ID");val label=input("Address label (Current / Permanent / Business)");val addr=input("Address");val pin=input("PIN code");val city=input("City / Town");val district=input("District");val state=input("State")
        listOf(owner,label,addr,pin,city,district,state).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)})}
        val status=ArthSaathiV7Design.text(this,"",10.5f,ArthSaathiV7Design.MUTED)
        r.addView(ArthSaathiV7Design.goldButton(this,"Validate PIN"){status.text=V7LocationEngine.resolvePin(pin.text.toString()).message},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(8)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Open Map to Select / Confirm"){try{startActivity(V7LocationEngine.mapIntent(this,addr.text.toString()))}catch(_:Exception){Toast.makeText(this,"No map application is available.",Toast.LENGTH_SHORT).show()}},LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(6)})
        r.addView(ArthSaathiV7Design.goldButton(this,"Save Address"){
            if(!V7LocationEngine.validatePin(pin.text.toString())){pin.error="Invalid PIN";return@goldButton}
            V7Records.address(this,owner.text.toString(),label.text.toString(),addr.text.toString(),pin.text.toString(),state.text.toString(),district.text.toString(),city.text.toString(),"USER_CONFIRMED");Toast.makeText(this,"Address saved.",Toast.LENGTH_SHORT).show()
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(6)});r.addView(status,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)});return r
    }
    private fun revenue():LinearLayout{
        val r=shell("Revenue & Payments","Service catalogue, invoice, configurable gateway record, payment status and reconciliation.")
        val service=input("Service name");val price=input("Price (₹)");val gateway=input("Gateway name");val ref=input("Payment reference")
        listOf(service,price,gateway,ref).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        val status=ArthSaathiV7Design.text(this,"",11f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.goldButton(this,"Create Service + Invoice"){
            val amount=price.text.toString().toDoubleOrNull();if(service.text.isBlank()||amount==null){Toast.makeText(this,"Service and price required.",Toast.LENGTH_SHORT).show();return@goldButton}
            val svc=V7RevenueEngine.service(this,service.text.toString(),amount);val inv=V7RevenueEngine.invoice(this,svc.getString("id"),amount,service.text.toString())
            status.text="Invoice "+inv.getString("id")+" created for ₹%.2f.".format(amount)
            val pay=V7RevenueEngine.payment(this,inv.getString("id"),amount,gateway.text.toString().ifBlank{"CONFIGURED_GATEWAY"},ref.text.toString(),V7RevenueEngine.PaymentStatus.CREATED)
            status.append("\nPayment record: "+pay.getString("id"))
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(ArthSaathiV7Design.outlineButton(this,"Mark Last Payment Successful"){
            val last=V7Core.all(this,V7Core.Keys.PAYMENTS).lastOrNull()?:return@outlineButton
            last.put("status",V7RevenueEngine.PaymentStatus.SUCCESS.name);V7Core.replace(this,V7Core.Keys.PAYMENTS,last);status.text="Payment marked SUCCESS. Receipt/reconciliation can now follow."
        },LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(6)});r.addView(status,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)});return r
    }
    private fun mis():LinearLayout{
        val r=shell("My Money Report","See the numbers first, then the charts. This is your personal MIS / financial command centre.")
        val out=ArthSaathiV7Design.text(this,"",11f,ArthSaathiV7Design.NAVY)
        val assetChart=DonutView(this)
        val portfolioChart=DonutView(this)
        assetChart.layoutParams=LinearLayout.LayoutParams(-1,dp(270)).apply{topMargin=dp(8)}
        portfolioChart.layoutParams=LinearLayout.LayoutParams(-1,dp(270)).apply{topMargin=dp(8)}
        r.addView(ArthSaathiV7Design.section(this,"Your Numbers","Assets, liabilities, dues, portfolio and benefits."))
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        r.addView(ArthSaathiV7Design.section(this,"Asset Mix","See where your recorded assets are concentrated.").apply{setPadding(0,dp(10),0,0)})
        r.addView(assetChart)
        r.addView(ArthSaathiV7Design.section(this,"Investment Mix","See how your recorded portfolio is distributed.").apply{setPadding(0,dp(10),0,0)})
        r.addView(portfolioChart)
        fun refresh(){
            val m=V7MIS.snapshot(this)
            out.text="Total assets: ₹%.2f\nLiabilities: ₹%.2f\nReceivables: ₹%.2f\nPayables: ₹%.2f\nNet worth: ₹%.2f\nPortfolio value: ₹%.2f\nPortfolio gain/loss: ₹%.2f\nActive credits: %d\nBenefits/refunds: recorded in your benefit records\nRevenue reconciled: ₹%.2f".format(
                m.optDouble("assets"),m.optDouble("liabilities"),m.optDouble("receivables"),m.optDouble("payables"),m.optDouble("netWorth"),m.optDouble("portfolioValue"),m.optDouble("portfolioGain"),m.optInt("activeCredits"),m.optDouble("reconciledRevenue"))
            val assets=V7Core.all(this,V7Core.Keys.ASSETS).groupBy{it.optString("type","Other")}.map{it.key to it.value.sumOf{a->a.optDouble("currentValue",a.optDouble("value",0.0))}}
            val holdings=V7Core.all(this,V7Core.Keys.HOLDINGS).groupBy{it.optString("category","Other")}.map{it.key to it.value.sumOf{h->h.optDouble("currentValue",0.0)}}
            assetChart.setValues(assets)
            portfolioChart.setValues(holdings)
        }
        r.addView(ArthSaathiV7Design.goldButton(this,"Refresh My Money Report"){refresh()},LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(10)})
        refresh()
        return r
    }
    private fun score():LinearLayout{
        val r=shell("Credit Score","Explainable internal score; borrower/counterparty consent is required before disclosure.")
        val people=V7Core.all(this,V7Core.Keys.PEOPLE)
        val labels=if(people.isEmpty()) listOf("No person recorded") else people.map{it.optString("name")+" • "+it.optString("mobile")}
        val spinner=Spinner(this).apply{adapter=ArrayAdapter(this@V7ToolsActivity,android.R.layout.simple_spinner_dropdown_item,labels)}
        r.addView(spinner,LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(7)})
        val out=ArthSaathiV7Design.text(this,"Score is hidden until verified consent is supplied.",11f,ArthSaathiV7Design.NAVY)
        r.addView(ArthSaathiV7Design.outlineButton(this,"Calculate with Verified Consent"){
            if(people.isEmpty()) { out.text="Create a person and credit relationship first."; return@outlineButton }
            val person=people[spinner.selectedItemPosition]
            if(!V7Core.hasConsent(this,person.optString("id"),"SCORE_DISCLOSURE")) {
                out.text="Verified borrower/counterparty consent is required before the score can be disclosed."
                return@outlineButton
            }
            val result=V7ScoreEngine.calculate(this,person.optString("id"),true)
            out.text=result?.let{"Score: "+it.score+" • "+it.band+"\n"+it.factors.joinToString("\n")} ?: "Score unavailable."
        },LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(8)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        return r
    }
    private fun integration():LinearLayout{
        val r=shell("ERP & Tally Integration","Explicit export/connector boundary for future ERP and TallyPrime synchronisation. No silent transmission.")
        val format=input("Format (JSON / XML / CSV)")
        val connector=input("Connector (TALLY_PRIME / GENERIC_ERP / ACCOUNTING_ERP)")
        val out=ArthSaathiV7Design.text(this,"Reviewable export package will be generated locally.",10.5f,ArthSaathiV7Design.NAVY)
        r.addView(format,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        r.addView(connector,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        r.addView(ArthSaathiV7Design.goldButton(this,"Generate Integration Package"){
            val f=runCatching{V7ExternalIntegration.Format.valueOf(format.text.toString().trim().uppercase())}.getOrNull()
            val c=runCatching{V7ExternalIntegration.Connector.valueOf(connector.text.toString().trim().uppercase())}.getOrNull()
            if(f==null||c==null){out.text="Use JSON/XML/CSV and a supported connector.";return@goldButton}
            val p=V7ExternalIntegration.export(this,c,f)
            out.text="Connector: "+p.connector+"\nFormat: "+p.format+"\nRecords: "+p.recordCount+"\nPayload generated locally and not transmitted."
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        return r
    }

    private fun advocate():LinearLayout{
        val r=shell("Advocate Directory","Find advocates by city, practice area, court, language and consultation mode. Profiles are user-submitted until independently verified.")
        val q=input("Search name / practice area")
        val city=input("City")
        val domain=input("Practice area")
        val out=ArthSaathiV7Design.text(this,"",10.5f,ArthSaathiV7Design.NAVY)
        r.addView(q);r.addView(city,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)});r.addView(domain,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})
        r.addView(ArthSaathiV7Design.goldButton(this,"Search Advocates"){
            val rows=V7LegalEngine.search(this,q.text.toString(),city.text.toString(),domain.text.toString())
            out.text=if(rows.isEmpty())"No matching advocate profiles recorded yet." else rows.joinToString("\n\n"){"• "+it.optString("name")+"\n"+it.optString("city")+" • "+it.optString("domain")+" • "+it.optString("court")+"\n"+it.optString("language")+" • "+it.optString("consultationMode")}
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)})
        r.addView(ArthSaathiV7Design.section(this,"Add Advocate Profile","Use this only for a profile you have permission to record."))
        val n=input("Advocate name");val state=input("State");val court=input("Court");val lang=input("Language");val mode=input("Consultation mode")
        listOf(n,state,court,lang,mode).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)})}
        r.addView(ArthSaathiV7Design.outlineButton(this,"Save Advocate Profile"){
            if(n.text.isBlank()){n.error="Required";return@outlineButton}
            V7LegalEngine.professional(this,n.text.toString(),city.text.toString(),state.text.toString(),domain.text.toString(),court.text.toString(),lang.text.toString(),mode.text.toString(),"USER_SUBMITTED")
            out.text="Advocate profile recorded. Search again to view it."
        },LinearLayout.LayoutParams(-1,dp(46)).apply{topMargin=dp(8)})
        r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        return r
    }

    private fun claim():LinearLayout{
        val r=shell("Claim Assistance","Prepare the evidence path from ownership to claim closure.")
        val asset=input("Asset ID");val claimant=input("Claimant");val nominee=input("Nominee / Heir");val institution=input("Institution");val amount=input("Claim amount")
        listOf(asset,claimant,nominee,institution,amount).forEach{r.addView(it,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)})}
        r.addView(ArthSaathiV7Design.goldButton(this,"Create Claim Record"){V7LegalEngine.claim(this,asset.text.toString(),claimant.text.toString(),nominee.text.toString(),institution.text.toString(),amount.text.toString().toDoubleOrNull()?:0.0);Toast.makeText(this,"Claim record created.",Toast.LENGTH_SHORT).show()},LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(9)});return r
    }
    private fun ai():LinearLayout{
        val r=shell("Ask ArthSaathi","Ask about recorded financial information, analytics and documented scenarios. No silent execution.")
        val q=input("Ask your financial question");val out=ArthSaathiV7Design.text(this,"",11f,ArthSaathiV7Design.NAVY);r.addView(q)
        r.addView(ArthSaathiV7Design.goldButton(this,"Ask ArthSaathi"){
            val m=V7Core.metrics(this);out.text=when{
                q.text.contains("asset",true)->"Recorded current assets: ₹%.2f".format(m.optDouble("assets"))
                q.text.contains("liabil",true)->"Recorded liabilities: ₹%.2f".format(m.optDouble("liabilities"))
                q.text.contains("net worth",true)->"Recorded net position: ₹%.2f".format(m.optDouble("netWorth"))
                q.text.contains("credit",true)->"Active money relationships: "+m.optInt("activeCredits")
                q.text.contains("portfolio",true)->"Recorded portfolio value: ₹%.2f; gain/loss: ₹%.2f".format(m.optDouble("portfolioValue"),m.optDouble("portfolioGain"))
                else->"I can answer from your recorded ArthSaathi data: assets, liabilities, portfolio, credits, documents, protection and claims."
            }
        },LinearLayout.LayoutParams(-1,dp(48)).apply{topMargin=dp(8)});r.addView(out,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(12)});return r
    }
}