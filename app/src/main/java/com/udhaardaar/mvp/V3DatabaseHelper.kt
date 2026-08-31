package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class V3DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "udhaardaar_v3.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        // ---------------------------------------------------------
        // PARTIES
        // A party can be a person, borrower, lender, customer,
        // supplier, business, guarantor, etc.
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE parties (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                party_type TEXT NOT NULL DEFAULT 'PERSON',
                name TEXT NOT NULL,
                mobile TEXT,
                email TEXT,
                address TEXT,
                pan TEXT,
                gstin TEXT,
                business_name TEXT,
                notes TEXT,
                created_at TEXT NOT NULL
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------
        // CREDIT ACCOUNTS
        // One party can have multiple credit accounts.
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE credits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                party_id INTEGER NOT NULL,
                credit_type TEXT NOT NULL,
                direction TEXT NOT NULL DEFAULT 'GIVEN',

                principal_amount REAL NOT NULL,
                interest_rate REAL NOT NULL DEFAULT 0,

                repayment_method TEXT NOT NULL,
                repayment_amount REAL NOT NULL DEFAULT 0,
                periodicity TEXT,

                start_date TEXT NOT NULL,
                end_date TEXT,
                next_due_date TEXT,

                status TEXT NOT NULL DEFAULT 'ACTIVE',

                notes TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,

                FOREIGN KEY (party_id)
                    REFERENCES parties(id)
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------
        // REPAYMENTS
        // Multiple repayments can belong to one credit.
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE repayments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                credit_id INTEGER NOT NULL,

                repayment_date TEXT NOT NULL,
                amount REAL NOT NULL,

                principal_component REAL NOT NULL DEFAULT 0,
                interest_component REAL NOT NULL DEFAULT 0,

                payment_mode TEXT,
                reference_number TEXT,
                notes TEXT,

                created_at TEXT NOT NULL,

                FOREIGN KEY (credit_id)
                    REFERENCES credits(id)
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------
        // GUARANTORS
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE guarantors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                party_id INTEGER NOT NULL,

                relationship TEXT,
                consent_status TEXT NOT NULL DEFAULT 'PENDING',

                created_at TEXT NOT NULL,

                FOREIGN KEY (party_id)
                    REFERENCES parties(id)
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------
        // CREDIT ↔ GUARANTOR RELATIONSHIP
        // Allows multiple guarantors for one credit.
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE credit_guarantors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                credit_id INTEGER NOT NULL,
                guarantor_id INTEGER NOT NULL,

                FOREIGN KEY (credit_id)
                    REFERENCES credits(id),

                FOREIGN KEY (guarantor_id)
                    REFERENCES guarantors(id),

                UNIQUE(credit_id, guarantor_id)
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------
        // DOCUMENTS
        // Agreement, invoice, DPN, identity documents, etc.
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE documents (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                credit_id INTEGER,
                party_id INTEGER,

                document_type TEXT NOT NULL,
                document_name TEXT,
                document_uri TEXT,

                verification_status TEXT NOT NULL DEFAULT 'PENDING',

                created_at TEXT NOT NULL,

                FOREIGN KEY (credit_id)
                    REFERENCES credits(id),

                FOREIGN KEY (party_id)
                    REFERENCES parties(id)
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------
        // CONSENTS
        // Future OTP / digital acknowledgement / consent system.
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE consents (
                id INTEGER PRIMARY KEY AUTOINCREMENT,

                credit_id INTEGER NOT NULL,
                party_id INTEGER NOT NULL,

                consent_type TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',

                otp_verified INTEGER NOT NULL DEFAULT 0,

                consented_at TEXT,

                FOREIGN KEY (credit_id)
                    REFERENCES credits(id),

                FOREIGN KEY (party_id)
                    REFERENCES parties(id)
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------
        // ACTIVITY / AUDIT HISTORY
        // Every important change can eventually be recorded here.
        // ---------------------------------------------------------
        db.execSQL(
            """
            CREATE TABLE activity_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,

                credit_id INTEGER,
                party_id INTEGER,

                activity_type TEXT NOT NULL,
                description TEXT,

                created_at TEXT NOT NULL,

                FOREIGN KEY (credit_id)
                    REFERENCES credits(id),

                FOREIGN KEY (party_id)
                    REFERENCES parties(id)
            )
            """.trimIndent()
        )

        // ---------------------------------------------------------
        // INDEXES
        // These will make borrower-history and credit searches faster.
        // ---------------------------------------------------------
        db.execSQL(
            "CREATE INDEX idx_parties_mobile ON parties(mobile)"
        )

        db.execSQL(
            "CREATE INDEX idx_credits_party ON credits(party_id)"
        )

        db.execSQL(
            "CREATE INDEX idx_credits_status ON credits(status)"
        )

        db.execSQL(
            "CREATE INDEX idx_repayments_credit ON repayments(credit_id)"
        )

        db.execSQL(
            "CREATE INDEX idx_repayments_date ON repayments(repayment_date)"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Future V3 schema upgrades will be added here.
    }

    // -------------------------------------------------------------
    // CREATE PARTY
    // -------------------------------------------------------------
    fun addParty(
        partyType: String,
        name: String,
        mobile: String = "",
        email: String = "",
        address: String = "",
        pan: String = "",
        gstin: String = "",
        businessName: String = "",
        notes: String = "",
        createdAt: String
    ): Long {

        val values = ContentValues().apply {
            put("party_type", partyType)
            put("name", name)
            put("mobile", mobile)
            put("email", email)
            put("address", address)
            put("pan", pan)
            put("gstin", gstin)
            put("business_name", businessName)
            put("notes", notes)
            put("created_at", createdAt)
        }

        return writableDatabase.insert(
            "parties",
            null,
            values
        )
    }

    // -------------------------------------------------------------
    // CREATE CREDIT
    // -------------------------------------------------------------
    fun addCredit(
        partyId: Long,
        creditType: String,
        direction: String,
        principalAmount: Double,
        interestRate: Double,
        repaymentMethod: String,
        repaymentAmount: Double,
        periodicity: String?,
        startDate: String,
        endDate: String?,
        nextDueDate: String?,
        notes: String?,
        createdAt: String
    ): Long {

        val values = ContentValues().apply {
            put("party_id", partyId)
            put("credit_type", creditType)
            put("direction", direction)
            put("principal_amount", principalAmount)
            put("interest_rate", interestRate)
            put("repayment_method", repaymentMethod)
            put("repayment_amount", repaymentAmount)
            put("periodicity", periodicity)
            put("start_date", startDate)
            put("end_date", endDate)
            put("next_due_date", nextDueDate)
            put("status", "ACTIVE")
            put("notes", notes)
            put("created_at", createdAt)
            put("updated_at", createdAt)
        }

        return writableDatabase.insert(
            "credits",
            null,
            values
        )
    }

    // -------------------------------------------------------------
    // ADD REPAYMENT
    // -------------------------------------------------------------
    fun addRepayment(
        creditId: Long,
        repaymentDate: String,
        amount: Double,
        principalComponent: Double,
        interestComponent: Double,
        paymentMode: String?,
        referenceNumber: String?,
        notes: String?,
        createdAt: String
    ): Long {

        val values = ContentValues().apply {
            put("credit_id", creditId)
            put("repayment_date", repaymentDate)
            put("amount", amount)
            put("principal_component", principalComponent)
            put("interest_component", interestComponent)
            put("payment_mode", paymentMode)
            put("reference_number", referenceNumber)
            put("notes", notes)
            put("created_at", createdAt)
        }

        return writableDatabase.insert(
            "repayments",
            null,
            values
        )
    }
}
