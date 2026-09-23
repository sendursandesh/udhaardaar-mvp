package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

/**
 * ArthSaathi V7 architecture boundary.
 *
 * This is the single contract layer between V7 presentation/application code
 * and the existing V5/V6.2 implementation adapters. Legacy code must not be
 * exposed directly to V7 screens.
 */
object V7Architecture {
    const val CONTRACT_VERSION = "7.0.1"

    enum class Entity { PERSON, RELATIONSHIP, ADDRESS, CREDIT, REPAYMENT, DOCUMENT, CONSENT, ASSET, LIABILITY, POLICY, CLAIM, NOMINEE, HOLDING, FUNDING, ALERT, SESSION }

    enum class Event {
        PROFILE_CHANGED, PERSON_CHANGED, RELATIONSHIP_CHANGED, ADDRESS_CHANGED,
        CREDIT_CREATED, CREDIT_CHANGED, REPAYMENT_CHANGED, DOCUMENT_CHANGED,
        CONSENT_GRANTED, CONSENT_REVOKED, ASSET_CHANGED, LIABILITY_CHANGED,
        POLICY_CHANGED, CLAIM_CHANGED, NOMINEE_CHANGED, HOLDING_CHANGED,
        FUNDING_CHANGED, ALERT_CREATED, SESSION_CHANGED
    }

    data class EventRecord(
        val event: Event,
        val entityId: String,
        val actorId: String,
        val timestamp: Long = System.currentTimeMillis(),
        val correlationId: String = UUID.randomUUID().toString()
    )

    interface EventPublisher {
        fun publish(record: EventRecord)
        fun subscribe(listener: (EventRecord) -> Unit): AutoCloseable
    }

    object Events : EventPublisher {
        private val listeners = CopyOnWriteArraySet<(EventRecord) -> Unit>()
        override fun publish(record: EventRecord) { listeners.forEach { it(record) } }
        override fun subscribe(listener: (EventRecord) -> Unit): AutoCloseable {
            listeners.add(listener)
            return AutoCloseable { listeners.remove(listener) }
        }
    }

    data class ConsentRequest(
        val subjectId: String,
        val purpose: String,
        val scope: String,
        val actorId: String,
        val expiresAt: Long,
        val otpRequired: Boolean = true
    )

    data class ConsentRecord(
        val id: String,
        val request: ConsentRequest,
        val status: Status,
        val grantedAt: Long? = null,
        val revokedAt: Long? = null,
        val otpVerified: Boolean = false
    ) {
        enum class Status { PENDING, GRANTED, REVOKED, EXPIRED }
        fun active(now: Long = System.currentTimeMillis()): Boolean =
            status == Status.GRANTED && now < request.expiresAt
    }

    interface ConsentService {
        fun request(request: ConsentRequest): ConsentRecord
        fun grant(consentId: String, otpVerified: Boolean): ConsentRecord?
        fun revoke(consentId: String): ConsentRecord?
        fun getActive(subjectId: String, purpose: String, now: Long = System.currentTimeMillis()): ConsentRecord?
        fun require(subjectId: String, purpose: String, now: Long = System.currentTimeMillis()): ConsentRecord
    }

    /**
     * Local implementation used by the current Android build.
     * Production OTP delivery/verification is intentionally injected at this boundary.
     */
    class LocalConsentService(private val context: Context) : ConsentService {
        private val key = V7Core.Keys.CONSENTS

        override fun request(request: ConsentRequest): ConsentRecord {
            val record = ConsentRecord(
                id = V7Core.id("CONS"),
                request = request,
                status = ConsentRecord.Status.PENDING
            )
            persist(record)
            return record
        }

        override fun grant(consentId: String, otpVerified: Boolean): ConsentRecord? {
            val existing = V7Core.find(context, key, consentId) ?: return null
            val requestedOtp = existing.optBoolean("otpRequired", true)
            if (requestedOtp && !otpVerified) return null
            existing.put("status", "GRANTED")
            existing.put("verified", otpVerified || !requestedOtp)
            existing.put("grantedAt", System.currentTimeMillis())
            V7Core.replace(context, key, existing)
            V7Architecture.Events.publish(EventRecord(Event.CONSENT_GRANTED, consentId, V7Core.user(context)))
            return fromJson(existing)
        }

        override fun revoke(consentId: String): ConsentRecord? {
            val existing = V7Core.find(context, key, consentId) ?: return null
            existing.put("status", "REVOKED")
            existing.put("withdrawn", true)
            existing.put("revokedAt", System.currentTimeMillis())
            V7Core.replace(context, key, existing)
            V7Architecture.Events.publish(EventRecord(Event.CONSENT_REVOKED, consentId, V7Core.user(context)))
            return fromJson(existing)
        }

        override fun getActive(subjectId: String, purpose: String, now: Long): ConsentRecord? =
            V7Core.all(context, key).asSequence()
                .filter { it.optString("subjectId") == subjectId && it.optString("purpose") == purpose }
                .map { fromJson(it) }
                .firstOrNull { it.active(now) && it.otpVerified }

        override fun require(subjectId: String, purpose: String, now: Long): ConsentRecord =
            getActive(subjectId, purpose, now)
                ?: throw SecurityException("Active consent required for $purpose")

        private fun persist(record: ConsentRecord) {
            V7Core.add(context, key, JSONObject().apply {
                put("id", record.id)
                put("subjectId", record.request.subjectId)
                put("purpose", record.request.purpose)
                put("scope", record.request.scope)
                put("actorId", record.request.actorId)
                put("expiresAt", record.request.expiresAt)
                put("otpRequired", record.request.otpRequired)
                put("status", record.status.name)
                put("verified", false)
                put("withdrawn", false)
            })
        }

        private fun fromJson(o: JSONObject) = ConsentRecord(
            id = o.optString("id"),
            request = ConsentRequest(
                subjectId = o.optString("subjectId"),
                purpose = o.optString("purpose"),
                scope = o.optString("scope"),
                actorId = o.optString("actorId"),
                expiresAt = o.optLong("expiresAt"),
                otpRequired = o.optBoolean("otpRequired", true)
            ),
            status = runCatching { ConsentRecord.Status.valueOf(o.optString("status")) }
                .getOrDefault(ConsentRecord.Status.PENDING),
            grantedAt = o.optLong("grantedAt").takeIf { it > 0 },
            revokedAt = o.optLong("revokedAt").takeIf { it > 0 },
            otpVerified = o.optBoolean("verified")
        )
    }

    interface Repository<T> {
        fun get(id: String): T?
        fun all(): List<T>
        fun save(value: T): T
        fun delete(id: String): Boolean
    }

    /**
     * Transitional repository adapter. It deliberately isolates V5LocalStore
     * behind the V7 repository contract while migration to relational storage
     * is performed and tested.
     */
    class JsonRepository(
        private val context: Context,
        private val key: String,
        private val entityId: (JSONObject) -> String = { it.optString("id") },
        private val encode: (TBD) -> JSONObject = { error("Use JsonObjectRepository") }
    )

    class JsonObjectRepository(
        private val context: Context,
        private val key: String
    ) : Repository<JSONObject> {
        override fun get(id: String): JSONObject? = V7Core.find(context, key, id)
        override fun all(): List<JSONObject> = V7Core.all(context, key)
        override fun save(value: JSONObject): JSONObject {
            val id = value.optString("id").ifBlank { V7Core.id("REC") }.also { value.put("id", it) }
            if (get(id) == null) V7Core.add(context, key, value) else V7Core.replace(context, key, value)
            return value
        }
        override fun delete(id: String): Boolean {
            val existing = get(id) ?: return false
            existing.put("status", "DELETED")
            V7Core.replace(context, key, existing)
            return true
        }
    }
}
