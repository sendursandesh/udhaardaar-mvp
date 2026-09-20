package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.util.Locale

class V62LegacyLegalAIActivity : androidx.appcompat.app.AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val d by lazy { resources.displayMetrics.density }
    private val Int.dp: Int get() = (this * d).toInt()
    private lateinit var scroll: ScrollView
    private val root by lazy { LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(16.dp,8.dp,16.dp,28.dp); setBackgroundColor(ArthSaathiV62Design.BG) } }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.setSoftInputMode(16)
        scroll = ScrollView(this).apply { isFillViewport = true; addView(root) }
        setContentView(scroll)
        render()
    }
    override fun onResume() { super.onResume(); if (!isFinishing) render() }
    private fun add(v: android.view.View, gap: Int = 8) = ArthSaathiV62Design.add(root,v,gap)

    private fun render() {
        root.removeAllViews()
        add(ArthSaathiV62Design.title(this,"Legacy • Claims • Legal • AI","Protect today • Prepare tomorrow"),2)
        add(ArthSaathiV62Design.text(this,"Connected to the same financial core: assets → nominees → claims → legal support → intelligence.",10f,ArthSaathiV62Design.MUTED),8)
        serviceCard("WILL & NOMINATION","Versioned will drafts linked to selected assets and beneficiaries") { willDialog() }
        serviceCard("CLAIM ASSISTANCE","Create a claim case from policies, assets and retained documents") { claimDialog() }
        serviceCard("LEGAL ASSISTANCE","Find or register advocate/counsel profiles by matter, city, language and expertise") { advocateDialog() }
        serviceCard("AI FINANCIAL ADVISOR","Compare recorded investment yield with an alternative scenario and review switch/rebalance considerations") { aiDialog() }
        scroll.post { scroll.scrollTo(0,0) }
    }

    private fun serviceCard(title:String,subtitle:String,click:()->Unit) {
        val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.setPadding(14.dp,12.dp,14.dp,12.dp);box.background=ArthSaathiV62Design.card();box.setOnClickListener{click()}
        box.addView(ArthSaathiV62Design.text(this,title,13f,ArthSaathiV62Design.NAVY,true));box.addView(ArthSaathiV62Design.text(this,subtitle,10f,ArthSaathiV62Design.MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=5.dp});add(box)
    }

    private fun willDialog() {
        val beneficiary=ArthSaathiV62Design.input(this,"Beneficiary / nominee name *")
        val executor=ArthSaathiV62Design.input(this,"Executor / trusted person")
        val instructions=ArthSaathiV62Design.input(this,"Distribution / legacy instructions *")
        val assets=store.all(V62Store.ASSETS).filter{it.optBoolean("currentAsset",true)}
        val selected=mutableSetOf<String>()
        val list=LinearLayout(this);list.orientation=LinearLayout.VERTICAL
        assets.forEach{asset->val cb=CheckBox(this);cb.text="${asset.optString("Asset name / description",asset.optString("type"))} • ₹${"%.2f".format(Locale.US,asset.optDouble("value"))}";cb.setOnCheckedChangeListener{_,checked->if(checked)selected.add(asset.optString("id"))else selected.remove(asset.optString("id"))};list.addView(cb)}
        val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.setPadding(12.dp,0,12.dp,0);box.addView(beneficiary);box.addView(executor);box.addView(ArthSaathiV62Design.text(this,"LINK ASSETS",10f,ArthSaathiV62Design.TEAL,true));box.addView(list);box.addView(instructions)
        AlertDialog.Builder(this).setTitle("Guided Will Draft").setMessage("Draft only. Independent legal review and lawful execution are required.").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("SAVE DRAFT"){_,_->
            if(beneficiary.text.isBlank()||instructions.text.isBlank()){Toast.makeText(this,"Beneficiary and instructions are required.",Toast.LENGTH_LONG).show();return@setPositiveButton}
            val id=V62Store.id("WILL");val owner=V62Integration.currentUserId(this);val version=store.all(V62Store.WILLS).count{it.optString("ownerUserId")==owner}+1
            store.add(V62Store.WILLS,JSONObject().apply{put("id",id);put("ownerUserId",owner);put("version",version);put("beneficiary",beneficiary.text.toString().trim());put("executor",executor.text.toString().trim());put("assetIds",selected.joinToString("|"));put("instructions",instructions.text.toString().trim());put("status","DRAFT");put("createdAt",System.currentTimeMillis())})
            val nom=V62Store.id("NOM");store.add(V62Store.NOMINEES,JSONObject().apply{put("id",nom);put("ownerUserId",owner);put("name",beneficiary.text.toString().trim());put("assetIds",selected.joinToString("|"));put("source","WILL_DRAFT");put("createdAt",System.currentTimeMillis())})
            V62EventBus.publish(V62Event(V62Events.WILL_CHANGED,id));V62EventBus.publish(V62Event(V62Events.NOMINEE_CHANGED,nom));Toast.makeText(this,"Version $version will draft saved and nominee linked.",Toast.LENGTH_LONG).show()
        }.show()
    }

    private fun claimDialog() {
        val claimant=ArthSaathiV62Design.input(this,"Claimant name / relation *");val event=ArthSaathiV62Design.input(this,"Claim event / date *")
        val policies=store.all(V62Store.INSURANCE);val assets=store.all(V62Store.ASSETS);val docs=store.all(V62Store.DOCUMENTS)
        val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.addView(claimant);box.addView(event)
        AlertDialog.Builder(this).setTitle("Claim Assistance").setMessage("Linked evidence: ${policies.size} policies • ${assets.size} assets • ${docs.size} documents.").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("CREATE CASE"){_,_->
            if(claimant.text.isBlank()||event.text.isBlank()){Toast.makeText(this,"Claimant and event are required.",Toast.LENGTH_LONG).show();return@setPositiveButton}
            val id=V62Store.id("CLAIM");store.add(V62Store.CLAIMS,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62LegacyLegalAIActivity));put("claimant",claimant.text.toString().trim());put("event",event.text.toString().trim());put("policyIds",policies.joinToString("|"){it.optString("id")});put("assetIds",assets.joinToString("|"){it.optString("id")});put("documentIds",docs.joinToString("|"){it.optString("id")});put("status","DRAFT");put("createdAt",System.currentTimeMillis())});V62EventBus.publish(V62Event(V62Events.CLAIM_CHANGED,id));Toast.makeText(this,"Claim case created with linked evidence.",Toast.LENGTH_LONG).show()
        }.show()
    }

    private fun advocateDialog() {
        val mode=Spinner(this);mode.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,arrayOf("Find an advocate","Register advocate profile"))
        val matter=ArthSaathiV62Design.input(this,"Matter / legal domain");val city=ArthSaathiV62Design.input(this,"City / locality");val language=ArthSaathiV62Design.input(this,"Language");val expertise=ArthSaathiV62Design.input(this,"Expertise / specialisation")
        val name=ArthSaathiV62Design.input(this,"Advocate / counsel name");val registration=ArthSaathiV62Design.input(this,"Bar registration / verification reference");val phone=ArthSaathiV62Design.input(this,"Contact number")
        val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.addView(mode);box.addView(matter);box.addView(city);box.addView(language);box.addView(expertise);box.addView(name);box.addView(registration);box.addView(phone)
        AlertDialog.Builder(this).setTitle("ArthSaathi Advocate Network").setMessage("Registered profiles are separate from ordinary user contacts. Local registration is PENDING until an external verification process confirms the advocate.").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("CONTINUE"){_,_->
            if(mode.selectedItemPosition==0){showAdvocates(matter.text.toString(),city.text.toString(),language.text.toString(),expertise.text.toString())}
            else if(name.text.isBlank()||registration.text.isBlank()||phone.text.isBlank()){Toast.makeText(this,"Name, registration reference and contact are required.",Toast.LENGTH_LONG).show()}
            else{store.add("v62_advocates",JSONObject().apply{put("id",V62Store.id("ADV"));put("name",name.text.toString().trim());put("barReference",registration.text.toString().trim());put("phone",phone.text.toString().trim());put("matter",matter.text.toString().trim());put("city",city.text.toString().trim());put("language",language.text.toString().trim());put("expertise",expertise.text.toString().trim());put("verificationStatus","PENDING");put("createdAt",System.currentTimeMillis())});Toast.makeText(this,"Advocate profile submitted as PENDING verification.",Toast.LENGTH_LONG).show()}
        }.show()
    }

    private fun showAdvocates(matter:String,city:String,language:String,expertise:String) {
        val terms=listOf(matter,city,language,expertise).filter{it.isNotBlank()}.map{it.lowercase()}
        val hits=store.all("v62_advocates").filter{a->terms.all{term->listOf(a.optString("matter"),a.optString("city"),a.optString("language"),a.optString("expertise"),a.optString("name")).joinToString(" ").lowercase().contains(term)}}
        val msg=if(hits.isEmpty())"No matching local profile. Register an advocate profile to add one." else hits.joinToString("\n\n"){a->"${a.optString("name")}\n${a.optString("matter")} • ${a.optString("expertise")}\n${a.optString("city")} • ${a.optString("language")}\nStatus: ${a.optString("verificationStatus")}"}
        AlertDialog.Builder(this).setTitle("Advocate Results (${hits.size})").setMessage(msg).setPositiveButton("OK",null).show()
    }

    private fun aiDialog() {
        val owner=V62Integration.currentUserId(this);val metrics=V62MisEngine.metrics(this);val assets=store.all(V62Store.ASSETS).filter{it.optString("ownerUserId")==owner&&it.optBoolean("currentAsset",true)}
        val investments=assets.filter{val type=it.optString("type").uppercase();type.contains("MUTUAL")||type.contains("FUND")||type.contains("STOCK")||type.contains("DEMAT")||type.contains("INVEST")}
        val names=investments.map{it.optString("Asset name / description",it.optString("type"))}.ifEmpty{listOf("No recorded investment asset")}
        val picker=Spinner(this);picker.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,names)
        val alternative=ArthSaathiV62Design.input(this,"Alternative expected return % (scenario)");val notes=ArthSaathiV62Design.input(this,"Alternative risk / suitability notes")
        val box=LinearLayout(this);box.orientation=LinearLayout.VERTICAL;box.addView(picker);box.addView(alternative);box.addView(notes)
        AlertDialog.Builder(this).setTitle("AI Portfolio Review").setMessage("Compare recorded yield with an alternative scenario. No switch is executed automatically; review risk, tax, liquidity, costs and suitability first.").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("COMPARE & SUGGEST"){_,_->
            val asset=if(investments.isEmpty())null else investments[picker.selectedItemPosition];val current=asset?.optDouble("yieldPercent",0.0)?:0.0;val alt=alternative.text.toString().toDoubleOrNull()
            val comparison=when{alt==null->"Enter an alternative expected return to compare.";alt>current->"Scenario return is ${"%.2f".format(Locale.US,alt-current)} percentage points above the recorded ${"%.2f".format(Locale.US,current)}%. Review suitability before considering a switch.";else->"The entered scenario is not above the recorded yield. Review diversification, risk, tax and costs before changing anything."}
            val name=asset?.let{it.optString("Asset name / description",it.optString("type"))} ?: "Portfolio"
            val context="Assets ₹${"%.0f".format(Locale.US,metrics.optDouble("assetValue"))} • liabilities ₹${"%.0f".format(Locale.US,metrics.optDouble("liabilities"))} • informal exposure ₹${"%.0f".format(Locale.US,metrics.optDouble("informalCreditExposure"))}."
            AlertDialog.Builder(this).setTitle("Explainable AI Suggestion").setMessage("$name\nRecorded yield: ${"%.2f".format(Locale.US,current)}%\n\n$comparison\n\nSuitability notes: ${notes.text.ifBlank{"Not provided"}}\n\n$context\n\nNo automatic investment switch is performed.").setPositiveButton("OK",null).show()
        }.show()
    }


}
