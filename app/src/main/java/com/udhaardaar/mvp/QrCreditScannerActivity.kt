package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import org.json.JSONObject

class QrCreditScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IntentIntegrator(this).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Scan credit / invoice QR code")
            setBeepEnabled(true)
            setOrientationLocked(false)
        }.initiateScan()
    }
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        val result:IntentResult?=IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
        if(result!=null){
            val raw=result.contents
            if(raw.isNullOrBlank()){setResult(RESULT_CANCELED);finish();return}
            val f=parse(raw)
            setResult(RESULT_OK,Intent().apply{putExtra("vendor",f.vendor);putExtra("invoice",f.invoice);putExtra("date",f.date);putExtra("amount",f.amount);putExtra("raw",raw)})
            finish();return
        }
        super.onActivityResult(requestCode,resultCode,data)
    }
    private data class Fields(val vendor:String="",val invoice:String="",val date:String="",val amount:String="")
    private fun parse(raw:String):Fields{
        try{
            val j=JSONObject(raw)
            return Fields(first(j,"vendor","vendor_name","seller","supplier","merchant","party"),first(j,"invoice","invoice_no","invoice_number","bill_no","bill_number"),first(j,"date","invoice_date","transaction_date"),first(j,"amount","total","total_amount","invoice_amount","grand_total"))
        }catch(_:Exception){}
        val map=raw.split('&','\n',';').mapNotNull{p->val x=p.split('=',':',limit=2);if(x.size==2)x[0].trim().lowercase() to x[1].trim()else null}.toMap()
        return Fields(map["vendor"]?:map["vendor_name"]?:map["seller"]?:map["supplier"]?:"",map["invoice"]?:map["invoice_no"]?:map["invoice_number"]?:"",map["date"]?:map["invoice_date"]?:map["transaction_date"]?:"",map["amount"]?:map["total"]?:map["total_amount"]?:map["invoice_amount"]?:map["grand_total"]?:"")
    }
    private fun first(j:JSONObject,vararg keys:String):String=keys.firstNotNullOfOrNull{k->if(j.has(k)&&!j.isNull(k))j.optString(k).takeIf{it.isNotBlank()}else null}?:""
}
