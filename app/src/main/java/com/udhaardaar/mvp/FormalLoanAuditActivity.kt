package com.udhaardaar.mvp

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class FormalLoanAuditActivity : AppCompatActivity() {
    private lateinit var db: FormalLoanAuditDb
    private var sanctionUri: Uri? = null
    private var statementUri: Uri? = null
    private var loanId: Long = -1L
    private lateinit var result: TextView
    private lateinit var lender: EditText
    private lateinit var account: EditText
    private lateinit var sanctioned: EditText
    private lateinit var disbursed: EditText
    private lateinit var roi: EditText
    private lateinit var tenure: EditText
    private lateinit var emi: EditText
    private lateinit var processing: EditText
    private lateinit var documentation: EditText
    private lateinit var insurance: EditText
    private lateinit var penal: EditText
    private lateinit var bounce: EditText
    private lateinit var prepayment: EditText
    private lateinit var other: EditText

    override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(R.layout.activity_formal_loan_audit); db=FormalLoanAuditDb(this)
        lender=f(R.id.etLender);account=f(R.id.etAccount);sanctioned=f(R.id.etSanctioned);disbursed=f(R.id.etDisbursed);roi=f(R.id.etRoi);tenure=f(R.id.etTenure);emi=f(R.id.etEmi);processing=f(R.id.etProcessing);documentation=f(R.id.etDocumentation);insurance=f(R.id.etInsurance);penal=f(R.id.etPenal);bounce=f(R.id.etBounce);prepayment=f(R.id.etPrepayment);other=f(R.id.etOther);result=f(R.id.tvAuditResult)
        findViewById<Button>(R.id.btnSanction).setOnClickListener{pick(10)}
        findViewById<Button>(R.id.btnOcr).setOnClickListener{ocrSanction()}
        findViewById<Button>(R.id.btnSaveBaseline).setOnClickListener{saveBaseline()}
        findViewById<Button>(R.id.btnStatement).setOnClickListener{pick(11)}
        findViewById<Button>(R.id.btnCompare).setOnClickListener{compareCsv()}
    }
    private fun <T:android.view.View>T.f(id:Int)=findViewById<T>(id)
    private fun pick(code:Int){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";addCategory(Intent.CATEGORY_OPENABLE)},code)}
    override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(c!=Activity.RESULT_OK||d?.data==null)return;val u=d.data!!;contentResolver.takePersistableUriPermission(u,d.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION));if(r==10){sanctionUri=u;toast("Sanction letter attached: ${name(u)}")}else{statementUri=u;toast("Statement attached: ${name(u)}")}}
    private fun ocrSanction(){val u=sanctionUri?:return toast("Upload a sanction letter image first");try{val image=InputImage.fromFilePath(this,u);TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(image).addOnSuccessListener{parseTerms(it.text);result.text="Sanction document OCR completed. Please review the detected terms before saving the baseline."}.addOnFailureListener{toast("OCR could not read this document. Enter terms manually.")}}catch(e:Exception){toast("This file cannot be OCR-read. Use a clear JPG/PNG page.")}}
    private fun parseTerms(t:String){val s=t.replace("₹","").replace(",","");fun find(p:Regex)=p.find(s)?.groupValues?.getOrNull(1)?.toDoubleOrNull();find(Regex("(?i)(?:sanctioned|sanction amount)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let{sanctioned.setText(it.toString())};find(Regex("(?i)(?:disbursed|disbursement amount)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let{disbursed.setText(it.toString())};find(Regex("(?i)(?:rate of interest|interest rate|ROI)[^0-9]{0,20}([0-9]+(?:\\.[0-9]+)?)"))?.let{roi.setText(it.toString())};find(Regex("(?i)(?:processing fee|processing charges)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let{processing.setText(it.toString())};find(Regex("(?i)(?:documentation fee|documentation charges)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let{documentation.setText(it.toString())};find(Regex("(?i)(?:insurance)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let{insurance.setText(it.toString())};find(Regex("(?i)(?:penal interest|penal rate)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let{penal.setText(it.toString())};find(Regex("(?i)(?:bounce charge|bounce charges)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let{bounce.setText(it.toString())};find(Regex("(?i)(?:prepayment|foreclosure)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let{prepayment.setText(it.toString())}}
    private fun saveBaseline(){val v=ContentValues();v.put("lender",lender.text.toString().trim());v.put("account_no",account.text.toString().trim());v.put("loan_type","Formal Loan");v.put("sanctioned",n(sanctioned));v.put("disbursed",n(disbursed));v.put("roi",n(roi));v.put("tenure_months",n(tenure).toInt());v.put("emi",n(emi));v.put("processing_fee",n(processing));v.put("documentation_fee",n(documentation));v.put("insurance",n(insurance));v.put("penal_rate",n(penal));v.put("bounce_charge",n(bounce));v.put("prepayment_charge",n(prepayment));v.put("other_charge",n(other));v.put("sanction_uri",sanctionUri?.toString());v.put("created_at",FormalLoanAuditDb.now());loanId=db.addLoan(v);toast("Sanction baseline saved");result.text="Baseline saved. Now upload the statement and compare."}
    private fun compareCsv(){if(loanId<=0)return toast("Save the sanction baseline first");val u=statementUri?:return toast("Upload a statement first");db.clearEntries(loanId);var totalActual=0.0;var totalExpected=0.0;var flags=0;try{BufferedReader(InputStreamReader(contentResolver.openInputStream(u)!!)).use{br->br.lineSequence().drop(1).forEach{line->val p=line.split(",");if(p.size<3)return@forEach;val date=p[0].trim();val desc=p[1].trim();val amount=p[2].trim().toDoubleOrNull()?:return@forEach;val type=if(p.size>3)p[3].trim().uppercase(Locale.US) else classify(desc);val expected=expected(type);val variance=if(expected>0)amount-expected else 0.0;val review=if(expected==0.0&&type!="EMI")"REVIEW" else if(kotlin.math.abs(variance)>0.01)"REVIEW" else "MATCH";db.addEntry(ContentValues().apply{put("loan_id",loanId);put("entry_date",date);put("description",desc);put("amount",amount);put("charge_type",type);put("expected",expected);put("variance",variance);put("review",review)});if(type!="EMI"){totalActual+=amount;totalExpected+=expected};if(review=="REVIEW")flags++}}};result.text="CHARGE AUDIT\n\nExpected charges: ₹${money(totalExpected)}\nActual charge entries: ₹${money(totalActual)}\nVariance: ₹${money(totalActual-totalExpected)}\nItems requiring review: $flags\n\nNote: REVIEW means the statement differs from the stored sanction baseline and requires human/contract verification; it is not an automatic finding of wrongful charging.";toast("Statement comparison completed")}catch(e:Exception){toast("For this first audit version, upload a CSV statement with columns: date,description,amount,charge_type")}}
    private fun classify(d:String)=when{d.contains("interest",true)->"INTEREST";d.contains("processing",true)->"PROCESSING";d.contains("bounce",true)->"BOUNCE";d.contains("penal",true)||d.contains("late",true)->"PENAL";d.contains("insurance",true)->"INSURANCE";d.contains("documentation",true)->"DOCUMENTATION";d.contains("prepayment",true)||d.contains("foreclosure",true)->"PREPAYMENT";d.contains("emi",true)||d.contains("repayment",true)->"EMI";else->"OTHER"}
    private fun expected(t:String)=when(t){"PROCESSING"->n(processing);"DOCUMENTATION"->n(documentation);"INSURANCE"->n(insurance);"BOUNCE"->n(bounce);"PREPAYMENT"->n(prepayment);"OTHER"->n(other);else->0.0}
    private fun n(v:EditText)=v.text.toString().replace(",","").toDoubleOrNull()?:0.0
    private fun money(v:Double)=String.format(Locale.US,"%,.2f",v);private fun name(u:Uri):String{var x="document";contentResolver.query(u,null,null,null,null)?.use{if(it.moveToFirst()){val i=it.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(i>=0)x=it.getString(i)}};return x};private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
