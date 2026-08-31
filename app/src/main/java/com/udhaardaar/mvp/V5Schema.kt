package com.udhaardaar.mvp

/** Version 5 persistence contract. A future Room/SQL implementation can map these tables 1:1. */
object V5Schema {
    const val VERSION = 5
    val tables = listOf(
        "profiles", "guarantors", "credits", "credit_terms", "repayment_schedules", "repayment_requests",
        "consents", "documents", "audit_events", "leases", "financial_assets", "non_financial_assets",
        "trusted_people", "nominees", "succession_events", "claims", "claim_documents", "reminders", "charge_audits"
    )

    val criticalIndexes = listOf(
        "profiles.mobile", "profiles.pan", "profiles.gstin", "credits.profileId", "repayment_requests.creditId",
        "repayment_requests.status", "consents.entityId", "documents.entityId", "audit_events.entityId",
        "financial_assets.ownerId", "non_financial_assets.ownerId", "claims.assetId", "claims.claimantId"
    )

    /** V5 migration must preserve legacy records and never overwrite existing audit/document history. */
    fun migrationPolicy() = "READ_LEGACY -> MAP -> VALIDATE -> WRITE_V5 -> VERIFY_COUNTS -> RETAIN_LEGACY_BACKUP"
}
