package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class V3DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "udhaardaar_v3.db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE parties (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            party_type TEXT NOT NULL DEFAULT 'PERSON', name TEXT NOT NULL,
            mobile TEXT, email TEXT, address TEXT, pincode TEXT, city TEXT, state TEXT,
            aadhaar TEXT, pan TEXT, gstin TEXT, business_name TEXT, photo_uri TEXT,
            notes TEXT, created_at TEXT NOT NULL)""".trimIndent())
        db.execSQL("""CREATE TABLE credits (
            id INTEGER PRIMARY KEY AUTOINCREMENT, party_id INTEGER NOT NULL,
            credit_type TEXT NOT NULL, direction TEXT NOT NULL DEFAULT 'GIVEN',
            principal_amount REAL NOT NULL, interest_rate REAL NOT NULL DEFAULT 0,
            repayment_method TEXT NOT NULL, repayment_amount REAL NOT NULL DEFAULT 0,
            periodicity TEXT, start_date TEXT NOT NULL, end_date TEXT, next_due_date TEXT,
            grace_days INTEGER NOT NULL DEFAULT 0, invoice_number TEXT, invoice_uri TEXT,
            status TEXT NOT NULL DEFAULT 'ACTIVE', notes TEXT, created_at TEXT NOT NULL,
            updated_at TEXT NOT NULL, FOREIGN KEY(party_id) REFERENCES parties(id))""".trimIndent())
        db.execSQL("""CREATE TABLE repayments (
            id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER NOT NULL,
            repayment_date TEXT NOT NULL, amount REAL NOT NULL,
            principal_component REAL NOT NULL DEFAULT 0, interest_component REAL NOT NULL DEFAULT 0,
            payment_mode TEXT, reference_number TEXT, notes TEXT, created_at TEXT NOT NULL,
            FOREIGN KEY(credit_id) REFERENCES credits(id))""".trimIndent())
        db.execSQL("""CREATE TABLE guarantors (
            id INTEGER PRIMARY KEY AUTOINCREMENT, party_id INTEGER NOT NULL,
            relationship TEXT, consent_status TEXT NOT NULL DEFAULT 'PENDING', created_at TEXT NOT NULL,
            FOREIGN KEY(party_id) REFERENCES parties(id))""".trimIndent())
        db.execSQL("""CREATE TABLE credit_guarantors (
            id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER NOT NULL, guarantor_id INTEGER NOT NULL,
            FOREIGN KEY(credit_id) REFERENCES credits(id), FOREIGN KEY(guarantor_id) REFERENCES guarantors(id),
            UNIQUE(credit_id, guarantor_id))""".trimIndent())
        db.execSQL("""CREATE TABLE documents (
            id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER, party_id INTEGER,
            document_type TEXT NOT NULL, document_name TEXT, document_uri TEXT,
            verification_status TEXT NOT NULL DEFAULT 'PENDING', created_at TEXT NOT NULL,
            FOREIGN KEY(credit_id) REFERENCES credits(id), FOREIGN KEY(party_id) REFERENCES parties(id))""".trimIndent())
        db.execSQL("""CREATE TABLE consents (
            id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER NOT NULL, party_id INTEGER NOT NULL,
            consent_type TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'PENDING',
            otp_verified INTEGER NOT NULL DEFAULT 0, consented_at TEXT, created_at TEXT,
            FOREIGN KEY(credit_id) REFERENCES credits(id), FOREIGN KEY(party_id) REFERENCES parties(id))""".trimIndent())
        db.execSQL("""CREATE TABLE credit_access (
            id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER NOT NULL, party_id INTEGER NOT NULL,
            role TEXT NOT NULL, can_view INTEGER NOT NULL DEFAULT 1, can_repay INTEGER NOT NULL DEFAULT 0,
            can_edit INTEGER NOT NULL DEFAULT 0, consent_required INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY(credit_id) REFERENCES credits(id), FOREIGN KEY(party_id) REFERENCES parties(id),
            UNIQUE(credit_id, party_id, role))""".trimIndent())
        db.execSQL("""CREATE TABLE activity_log (
            id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER, party_id INTEGER,
            activity_type TEXT NOT NULL, description TEXT, created_at TEXT NOT NULL,
            FOREIGN KEY(credit_id) REFERENCES credits(id), FOREIGN KEY(party_id) REFERENCES parties(id))""".trimIndent())
        createIndexes(db)
    }

    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_parties_mobile ON parties(mobile)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_parties_pan ON parties(pan)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_parties_gstin ON parties(gstin)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_parties_aadhaar ON parties(aadhaar)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_credits_party ON credits(party_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_credits_status ON credits(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_repayments_credit ON repayments(credit_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_repayments_date ON repayments(repayment_date)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_access_credit_party ON credit_access(credit_id, party_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            addColumn(db, "parties", "pincode", "TEXT")
            addColumn(db, "parties", "city", "TEXT")
            addColumn(db, "parties", "state", "TEXT")
            addColumn(db, "parties", "aadhaar", "TEXT")
            addColumn(db, "parties", "photo_uri", "TEXT")
        }
        if (oldVersion < 3) {
            addColumn(db, "credits", "grace_days", "INTEGER NOT NULL DEFAULT 0")
            addColumn(db, "credits", "invoice_number", "TEXT")
            addColumn(db, "credits", "invoice_uri", "TEXT")
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS credit_access (id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER NOT NULL, party_id INTEGER NOT NULL, role TEXT NOT NULL, can_view INTEGER NOT NULL DEFAULT 1, can_repay INTEGER NOT NULL DEFAULT 0, can_edit INTEGER NOT NULL DEFAULT 0, consent_required INTEGER NOT NULL DEFAULT 1, FOREIGN KEY(credit_id) REFERENCES credits(id), FOREIGN KEY(party_id) REFERENCES parties(id), UNIQUE(credit_id, party_id, role))")
            db.execSQL("CREATE TABLE IF NOT EXISTS activity_log (id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER, party_id INTEGER, activity_type TEXT NOT NULL, description TEXT, created_at TEXT NOT NULL)")
        }
        createIndexes(db)
    }

    private fun addColumn(db: SQLiteDatabase, table: String, column: String, type: String) {
        try { db.execSQL("ALTER TABLE $table ADD COLUMN $column $type") } catch (_: Exception) { }
    }

    fun addParty(partyType: String, name: String, mobile: String = "", email: String = "", address: String = "", pan: String = "", gstin: String = "", businessName: String = "", notes: String = "", createdAt: String, pincode: String = "", city: String = "", state: String = "", aadhaar: String = "", photoUri: String = ""): Long {
        val values = ContentValues().apply {
            put("party_type", partyType); put("name", name); put("mobile", mobile); put("email", email)
            put("address", address); put("pincode", pincode); put("city", city); put("state", state)
            put("aadhaar", aadhaar); put("pan", pan); put("gstin", gstin); put("business_name", businessName)
            put("photo_uri", photoUri); put("notes", notes); put("created_at", createdAt)
        }
        return writableDatabase.insert("parties", null, values)
    }

    fun addCredit(partyId: Long, creditType: String, direction: String, principalAmount: Double, interestRate: Double, repaymentMethod: String, repaymentAmount: Double, periodicity: String?, startDate: String, endDate: String?, nextDueDate: String?, notes: String?, createdAt: String, graceDays: Int = 0, invoiceNumber: String? = null, invoiceUri: String? = null): Long {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val values = ContentValues().apply {
                put("party_id", partyId); put("credit_type", creditType); put("direction", direction)
                put("principal_amount", principalAmount); put("interest_rate", interestRate)
                put("repayment_method", repaymentMethod); put("repayment_amount", repaymentAmount)
                put("periodicity", periodicity); put("start_date", startDate); put("end_date", endDate)
                put("next_due_date", nextDueDate); put("grace_days", graceDays)
                put("invoice_number", invoiceNumber); put("invoice_uri", invoiceUri)
                put("status", "ACTIVE"); put("notes", notes); put("created_at", createdAt); put("updated_at", createdAt)
            }
            val id = db.insertOrThrow("credits", null, values)
            val access = ContentValues().apply { put("credit_id", id); put("party_id", partyId); put("role", if (direction == "GIVEN") "BORROWER" else "LENDER"); put("can_view", 1); put("can_repay", 1); put("can_edit", 0); put("consent_required", 1) }
            db.insert("credit_access", null, access)
            db.setTransactionSuccessful(); id
        } catch (_: Exception) { -1L } finally { db.endTransaction() }
    }

    fun canUpdateRepayment(creditId: Long, actorPartyId: Long, consentVerified: Boolean): Boolean {
        if (!consentVerified) return false
        val c = readableDatabase.rawQuery("SELECT can_repay FROM credit_access WHERE credit_id=? AND party_id=? AND can_repay=1 LIMIT 1", arrayOf(creditId.toString(), actorPartyId.toString()))
        val allowed = c.moveToFirst(); c.close(); return allowed
    }

    fun addRepaymentAuthorized(creditId: Long, actorPartyId: Long, repaymentDate: String, amount: Double, principalComponent: Double, interestComponent: Double, paymentMode: String?, referenceNumber: String?, notes: String?, createdAt: String, consentVerified: Boolean): Long {
        if (amount <= 0 || !canUpdateRepayment(creditId, actorPartyId, consentVerified)) return -1L
        val values = ContentValues().apply {
            put("credit_id", creditId); put("repayment_date", repaymentDate); put("amount", amount)
            put("principal_component", principalComponent.coerceAtLeast(0.0)); put("interest_component", interestComponent.coerceAtLeast(0.0))
            put("payment_mode", paymentMode); put("reference_number", referenceNumber); put("notes", notes); put("created_at", createdAt)
        }
        return writableDatabase.insert("repayments", null, values)
    }

    fun addRepayment(creditId: Long, repaymentDate: String, amount: Double, principalComponent: Double, interestComponent: Double, paymentMode: String?, referenceNumber: String?, notes: String?, createdAt: String): Long {
        return if (amount > 0) {
            val values = ContentValues().apply { put("credit_id", creditId); put("repayment_date", repaymentDate); put("amount", amount); put("principal_component", principalComponent); put("interest_component", interestComponent); put("payment_mode", paymentMode); put("reference_number", referenceNumber); put("notes", notes); put("created_at", createdAt) }
            writableDatabase.insert("repayments", null, values)
        } else -1L
    }
}
