package com.udhaardaar.mvp

import android.database.sqlite.SQLiteDatabase

/** Idempotent foundation tables. Keeps sensitive access and document history extensible. */
object V3Foundation {
    fun ensure(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS audit_events(id INTEGER PRIMARY KEY AUTOINCREMENT,actor_id TEXT,action TEXT NOT NULL,entity_type TEXT NOT NULL,entity_id TEXT NOT NULL,details TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS access_consents(id INTEGER PRIMARY KEY AUTOINCREMENT,subject_profile_id INTEGER NOT NULL,requester_profile_id INTEGER,scope TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'REQUESTED',requested_at TEXT NOT NULL,approved_at TEXT,revoked_at TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS agreement_documents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER,agreement_type TEXT NOT NULL,version INTEGER NOT NULL DEFAULT 1,content TEXT NOT NULL,document_hash TEXT,created_at TEXT NOT NULL,superseded_at TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS repayment_consents(id INTEGER PRIMARY KEY AUTOINCREMENT,repayment_id INTEGER NOT NULL,actor_id TEXT NOT NULL,consent_type TEXT NOT NULL,verification_method TEXT NOT NULL,verified_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS credit_score_snapshots(id INTEGER PRIMARY KEY AUTOINCREMENT,profile_id INTEGER NOT NULL,score INTEGER NOT NULL,model_version TEXT NOT NULL,reason TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS profile_verifications(id INTEGER PRIMARY KEY AUTOINCREMENT,profile_id INTEGER NOT NULL,verification_type TEXT NOT NULL,status TEXT NOT NULL,reference TEXT,verified_at TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS co_borrowers(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,profile_id INTEGER NOT NULL,consent_status TEXT NOT NULL DEFAULT 'PENDING',consented_at TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS rental_lease_terms(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER,landlord_profile_id INTEGER,tenant_profile_id INTEGER,property_address TEXT NOT NULL,monthly_rent REAL NOT NULL,security_deposit REAL NOT NULL,start_date TEXT NOT NULL,end_date TEXT NOT NULL,notice_days INTEGER NOT NULL,escalation_percent REAL NOT NULL,maintenance_by TEXT,utilities_by TEXT,payment_frequency TEXT,other_terms TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_events(entity_type,entity_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_access_subject ON access_consents(subject_profile_id,status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_scores_profile ON credit_score_snapshots(profile_id,created_at)")
    }
}
