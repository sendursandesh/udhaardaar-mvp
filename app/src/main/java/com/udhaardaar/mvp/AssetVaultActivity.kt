package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*

class AssetVaultActivity : Activity() {
    private lateinit var db: AssetVaultDb
    private var documentUri: Uri? = null
    private lateinit var summary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AssetVaultDb(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,24,24,24) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)

        content.addView(TextView(this).apply { text = "Personal Asset Vault"; textSize = 26f; setTypeface(null, Typeface.BOLD) })
        content.addView(TextView(this).apply { text = "Record assets, nominees, legal heirs and supporting documents securely."; textSize = 15f; setPadding(0,8,0,20) })
        summary = TextView(this).apply { textSize = 16f; setPadding(0,0,0,16) }
        content.addView(summary)

        fun field(hint: String): EditText = EditText(this).apply { this.hint = hint; setSingleLine(true) }
        val category = Spinner(this).apply { adapter = ArrayAdapter(this@AssetVaultActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Bank / FD / RD","Investment","Insurance","Property / Land","Gold / Jewellery","Vehicle","Business Interest","Digital Asset","Other")) }
        val name = field("Asset name / description *")
        val institution = field("Bank / institution / location")
        val identifier = field("Account / policy / registration reference")
        val value = field("Estimated current value (₹)")
        value.inputType = 2
        val nominee = field("Nominee / beneficiary")
        val nomineeRelation = field("Nominee relationship")
        val heir = field("Intended / recorded legal heir (if applicable)")
        val notes = field("Notes / special instructions")
        content.addView(category); content.addView(name); content.addView(institution); content.addView(identifier); content.addView(value)
        content.addView(nominee); content.addView(nomineeRelation); content.addView(heir); content.addView(notes)
        val doc = Button(this).apply { text = "UPLOAD SUPPORTING DOCUMENT" }
        doc.setOnClickListener { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type="*/*"; addCategory(Intent.CATEGORY_OPENABLE) }, 1001) }
        content.addView(doc)
        val save = Button(this).apply { text = "SAVE ASSET" }
        save.setOnClickListener {
            if (name.text.toString().trim().isEmpty()) { name.error = "Required"; return@setOnClickListener }
            val amount = value.text.toString().toDoubleOrNull() ?: 0.0
            db.addAsset(category.selectedItem.toString(), name.text.toString().trim(), institution.text.toString().trim(), identifier.text.toString().trim(), amount, nominee.text.toString().trim(), nomineeRelation.text.toString().trim(), heir.text.toString().trim(), notes.text.toString().trim(), documentUri?.toString())
            Toast.makeText(this, "Asset saved", Toast.LENGTH_SHORT).show()
            listSummary()
            name.text.clear(); value.text.clear(); nominee.text.clear(); nomineeRelation.text.clear(); heir.text.clear(); notes.text.clear(); documentUri=null
        }
        content.addView(save)

        content.addView(TextView(this).apply { text = "Family / Legal-Heir Contacts"; textSize=20f; setTypeface(null,Typeface.BOLD); setPadding(0,28,0,8) })
        val fn = field("Name *"); val fr = field("Relationship *"); val fm = field("Mobile"); val fe = field("Email"); val fnotes = field("Notes")
        content.addView(fn); content.addView(fr); content.addView(fm); content.addView(fe); content.addView(fnotes)
        val addFamily = Button(this).apply { text="SAVE FAMILY / HEIR CONTACT" }
        addFamily.setOnClickListener {
            if (fn.text.toString().trim().isEmpty() || fr.text.toString().trim().isEmpty()) { Toast.makeText(this,"Name and relationship are required",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            db.addFamily(fn.text.toString().trim(),fr.text.toString().trim(),fm.text.toString().trim(),fe.text.toString().trim(),fnotes.text.toString().trim()); Toast.makeText(this,"Family contact saved",Toast.LENGTH_SHORT).show()
            fn.text.clear(); fr.text.clear(); fm.text.clear(); fe.text.clear(); fnotes.text.clear()
        }
        content.addView(addFamily)
        listSummary()
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0).apply { weight = 1f })
        setContentView(root)
    }

    private fun listSummary() { summary.text = "Assets recorded: ${db.assetCount()}\nEstimated asset value: ₹${String.format("%,.2f",db.totalValue())}" }
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) { super.onActivityResult(requestCode,resultCode,data); if(requestCode==1001 && resultCode==RESULT_OK) documentUri=data?.data; if(documentUri!=null) Toast.makeText(this,"Document attached",Toast.LENGTH_SHORT).show() }
}
