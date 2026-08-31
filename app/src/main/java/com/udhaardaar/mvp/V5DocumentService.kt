package com.udhaardaar.mvp

import android.content.Context
import java.io.File

/** Secure document metadata boundary. Actual files are kept in app-private storage by the UI adapter. */
class V5DocumentService(private val context:Context){
 fun copyIntoVault(source:File,type:String,entityId:String):V5Document{
  require(source.exists()&&source.isFile)
  val dir=File(context.filesDir,"v5_vault").apply{mkdirs()}
  val dest=File(dir,"${System.currentTimeMillis()}_${source.name.replace(Regex("[^A-Za-z0-9._-]"),"_")}")
  source.copyTo(dest,true)
  val bytes=dest.readBytes()
  return V5Document("DOC-${System.currentTimeMillis()}",type,dest.toURI().toString(),V5DocumentAudit.hash(bytes),java.time.Instant.now(),1)
 }
}
