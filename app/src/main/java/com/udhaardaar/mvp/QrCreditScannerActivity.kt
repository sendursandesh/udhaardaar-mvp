package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject

/** Scans a QR payload and returns normalized credit fields to the caller. */
class QrCreditScannerActivity : Activity() {
    private val scanner = registerForActivityResult(ScanContract()) { result ->
        if (result.contents.isNullOrBlank()) { setResult(RESULT_CANCELED); finish(); return@registerForActivityResult }
        val data = parse(result.contents)
        setResult(RESULT_OK, Intent().apply {
            putExtra("vendor", data.vendor)
            putExtra("invoice", data.invoice)
            putExtra("date", data.date)
            putExtra("amount", data.amount)
            putExtra("raw", result.contents)
        })
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanner.launch(ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan credit / invoice QR code")
            setBeepEnabled(true)
            setOrientationLocked(false)
        })
    }

    private data class Fields(val vendor:String="", val invoice:String="", val date:String="", val amount:String="")

    private fun parse(raw:String):Fields {
        return try {
            val j=JSONObject(raw)
            Fields(
                first(j,"vendor","vendor_name","seller","supplier","merchant","party"),
                first(j,"invoice","invoice_no","invoice_number","bill_no","bill_number"),
                first(j,"date","invoice_date","transaction_date"),
                first(j,"amount","total","total_amount","invoice_amount","grand_total")
            )
        } catch (_:Exception) {
            val map=raw.split('&','\n',';').mapNotNull{p->val x=p.split('=',':',limit=2);if(x.size==2)x[0].trim().lowercase() to x[1].trim() else null}.toMap()
            Fields(map["vendor"]?:map["seller"]?:map["supplier"]?:"",map["invoice"]?:map["invoice_no"]?:"",map["date"]?:map["invoice_date"]?:"",map["amount"]?:map["total"]?:"")
        }
    }
    private fun first(j:JSONObject,vararg keys:String):String=keys.firstNotNullOfOrNull{if(j.has(it)&&!j.isNull(it))j.optString(it).takeIf{v->v.isNotBlank()} else null}?:""
}
