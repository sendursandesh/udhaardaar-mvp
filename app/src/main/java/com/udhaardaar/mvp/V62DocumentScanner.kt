package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/** On-device OCR pipeline. It reads image documents and renders PDF pages before OCR. */
object V62DocumentScanner {
    fun scan(context:Context,uri:Uri,done:(String)->Unit,failed:(Exception)->Unit){
        val resolver=context.contentResolver
        runCatching{
            val type=resolver.getType(uri).orEmpty()
            if(type.equals("application/pdf",true)||uri.toString().lowercase().endsWith(".pdf")) scanPdf(context,uri,done,failed)
            else { val bmp=android.provider.MediaStore.Images.Media.getBitmap(resolver,uri); scanBitmap(bmp,done,failed) }
        }.onFailure{failed(it as? Exception ?: RuntimeException(it))}
    }
    private fun scanBitmap(bitmap:Bitmap,done:(String)->Unit,failed:(Exception)->Unit){val recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);recognizer.process(InputImage.fromBitmap(bitmap,0)).addOnSuccessListener{done(it.text)}.addOnFailureListener{failed(it)}}
    private fun scanPdf(context:Context,uri:Uri,done:(String)->Unit,failed:(Exception)->Unit){Thread{var fd:ParcelFileDescriptor?=null;var renderer:PdfRenderer?=null;try{fd=context.contentResolver.openFileDescriptor(uri,"r")?:error("Cannot open PDF");renderer=PdfRenderer(fd);val pages=mutableListOf<String>();fun next(i:Int){if(i>=renderer!!.pageCount){done(pages.joinToString("\n"));renderer!!.close();fd!!.close();return};val page=renderer!!.openPage(i);val bmp=Bitmap.createBitmap(page.width*2,page.height*2,Bitmap.Config.ARGB_8888);page.render(bmp,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);page.close();val recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);recognizer.process(InputImage.fromBitmap(bmp,0)).addOnSuccessListener{pages.add("[PAGE ${i+1}]\n"+it.text);recognizer.close();next(i+1)}.addOnFailureListener{recognizer.close();failed(it);runCatching{renderer?.close();fd?.close()}}};next(0)}catch(e:Exception){runCatching{renderer?.close();fd?.close()};failed(e)}}.start()}
}
