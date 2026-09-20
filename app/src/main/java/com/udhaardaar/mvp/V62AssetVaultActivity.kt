package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class V62AssetVaultActivity:AppCompatActivity(){
    private val s by lazy{V5LocalStore(this)}
    private val d get()=resources.displayMetrics.density
    private lateinit var root:LinearLayout
    private var kind="PROPERTY"
    private var docId=""
    private var docUri=""
    private val types=arrayOf("Property / Land","House / Building","Bank Account / Deposit","Mutual Fund","Shares / Securities","Insurance Policy","Gold / Jewellery","Vehicle","Loan Given to Others","Business / Partnership Interest","Pension / Retirement","Other Financial / Non-financial Asset")
    private fun add(v:View,g:Int=6)=ArthSaathiV62Design.add(root,v,g)
    private fun input(h:String)=ArthSaathiV62Design.input(this,h)
    override fun onCreate(b:Bundle?){super.onCreate(b);render()}
    private fun render(){
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding((16*d).toInt(),(8*d).toInt(),(16*d).toInt(),(28*d).toInt());setBackgroundColor(ArthSaathiV62Design.BG)}
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
        add(ArthSaathiV62Design.title(this,"Asset Vault","Own • Record • Protect • Claim"),2)
        add(ArthSaathiV62Design.text(this,"Select the actual asset class first. Each record feeds MIS automatically. Scanning is optional and never blocks manual entry.",11f,ArthSaathiV62Design.MUTED),7)
        val type=Spinner(this).apply{adapter=ArrayAdapter(this@V62AssetVaultActivity,android.R.layout.simple_spinner_dropdown_item,types.toList());onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){};override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){kind=types[pos]}}}
        add(type,5)
        add(ArthSaathiV62Design.button(this,"SCAN / ATTACH SUPPORTING DOCUMENT",ArthSaathiV62Design.TEAL){pick()},6)
        add(ArthSaathiV62Design.button(this,"ENTER ASSET DETAILS",ArthSaathiV62Design.BLUE){form()},6)
        add(ArthSaathiV62Design.section(this,"CURRENT ASSET RECORDS"),10)
        val owner=V62Integration.currentUserId(this);val rows=s.all(V62Store.ASSETS).filter{it.optString("ownerUserId")==owner&&it.optString("lifecycleStatus","ACTIVE")=="ACTIVE"}
        if(rows.isEmpty())add(ArthSaathiV62Design.text(this,"No current assets recorded.",12f,ArthSaathiV62Design.MUTED),5)
        rows.take(20).forEach{add(ArthSaathiV62Design.text(this,"${it.optString("type")} • ${it.optString("Asset name / description")}\n₹${it.optDouble("value",0.0)}",12f,ArthSaathiV62Design.NAVY,true),4)}
    }
    private fun form(){
        root.removeAllViews();add(ArthSaathiV62Design.title(this,"Asset Vault","Select type • Enter details • Link documents"),2)
        add(ArthSaathiV62Design.section(this,"ASSET TYPE"),5)
        val type=Spinner(this).apply{
            adapter=ArrayAdapter(this@V62AssetVaultActivity,android.R.layout.simple_spinner_dropdown_item,types.toList())
            setSelection(types.indexOf(kind).coerceAtLeast(0))
            onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(p:AdapterView<*>?) {}
                override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){kind=types[pos]}
            }
        }
        add(type,4)
        val name=input("Asset name / description *");val owner=input("Owner / account holder");val id=input("Account / folio / registration / policy number");val value=input("Current value ₹ *");val liability=input("Outstanding liability ₹");val nominee=input("Nominee");val yield=input("Expected / recorded yield %");val risk=input("Risk / notes")
        listOf(name,owner,id,value,liability,nominee,yield,risk).forEach{add(it,4)}
        add(ArthSaathiV62Design.button(this,"SAVE ASSET",ArthSaathiV62Design.GREEN){
            val v=value.text.toString().replace(",","").toDoubleOrNull()?:0.0;if(name.text.isBlank()||v<0){Toast.makeText(this,"Asset name and valid value are required.",Toast.LENGTH_LONG).show();return@button}
            val idv=V62Store.id("AST");s.add(V62Store.ASSETS,JSONObject().apply{put("id",idv);put("ownerUserId",V62Integration.currentUserId(this@V62AssetVaultActivity));put("type",kind);put("Asset name / description",name.text.toString().trim());put("owner",owner.text.toString().trim());put("reference",id.text.toString().trim());put("value",v);put("outstandingLiability",liability.text.toString().replace(",","").toDoubleOrNull()?:0.0);put("nominee",nominee.text.toString().trim());put("yieldPercent",yield.text.toString().toDoubleOrNull()?:0.0);put("risk",risk.text.toString().trim());put("documentId",docId);put("documentUri",docUri);put("lifecycleStatus","ACTIVE");put("currentAsset",true);put("createdAt",System.currentTimeMillis())});V62EventBus.publish(V62Event(V62Events.ASSET_CHANGED,idv));Toast.makeText(this,"Asset saved and connected to MIS.",Toast.LENGTH_LONG).show();render()
        },10)
        add(ArthSaathiV62Design.button(this,"BACK",ArthSaathiV62Design.NAVY){render()},5)
    }
    private fun pick(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);addCategory(Intent.CATEGORY_OPENABLE)},43)}
    override fun onActivityResult(r:Int,c:Int,data:Intent?){super.onActivityResult(r,c,data);if(r==43&&c==Activity.RESULT_OK&&data?.data!=null){val uri=data.data!!;val retained=runCatching{ArthSaathiV62Core.saveDocument(this,uri,"ASSET_SUPPORT")}.getOrNull();docId=retained?.optString("id").orEmpty();docUri=retained?.optString("uri").orEmpty().ifBlank{uri.toString()};Toast.makeText(this,"Document attached. You can continue with manual entry.",Toast.LENGTH_LONG).show();form()}}
}