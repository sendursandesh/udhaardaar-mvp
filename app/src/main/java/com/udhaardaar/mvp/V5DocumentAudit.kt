package com.udhaardaar.mvp

import java.security.MessageDigest

/** Versioned evidence chain. Completed consented documents are immutable by replacement, not overwrite. */
object V5DocumentAudit {
    data class VersionedDocument(val id:String,val type:String,val creditId:String?,val version:Int,val sha256:String,val createdAt:Long,val status:String)
    data class AuditEvent(val id:String,val entityId:String,val event:String,val actorId:String,val at:Long,val details:String)

    fun hash(bytes:ByteArray):String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){ "%02x".format(it) }
    fun nextVersion(previous:VersionedDocument?):Int = (previous?.version ?: 0)+1
    fun immutableReplacement(previous:VersionedDocument?, type:String, entityId:String?, bytes:ByteArray, actorId:String):Pair<VersionedDocument,AuditEvent>{
        val now=System.currentTimeMillis();val d=VersionedDocument("DOC-$now",type,entityId,nextVersion(previous),hash(bytes),now,"ACTIVE")
        return d to AuditEvent("AUD-$now",entityId ?: d.id,"DOCUMENT_VERSION_CREATED",actorId,now,"version=${d.version};sha256=${d.sha256}")
    }
}
