package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.UUID

class UdhaarDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "udhaardaar.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE udhaar_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                unique_credit_id TEXT NOT NULL,
                credit_type TEXT NOT NULL DEFAULT 'PERSONAL',
                person_name TEXT NOT NULL,
                borrower_mobile TEXT DEFAULT '',
                borrower_address TEXT DEFAULT '',
                borrower_aadhaar TEXT DEFAULT '',
                borrower_pan TEXT DEFAULT '',
                borrower_photo_uri TEXT DEFAULT '',
                amount REAL NOT NULL,
                roi REAL NOT NULL DEFAULT 0,
                repayment_method TEXT NOT NULL,
                periodicity TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                emi_amount REAL DEFAULT 0,
                due_date TEXT,
                guarantor_available TEXT NOT NULL DEFAULT 'NO',
                guarantor_name TEXT DEFAULT '',
                guarantor_mobile TEXT DEFAULT '',
                guarantor_address TEXT DEFAULT '',
                guarantor_photo_uri TEXT DEFAULT '',
                invoice_uri TEXT DEFAULT '',
                consent_status TEXT NOT NULL DEFAULT 'PENDING',
                otp_verified INTEGER NOT NULL DEFAULT 0,
                notes TEXT,
                status TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN roi REAL NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN repayment_method TEXT NOT NULL DEFAULT 'PRINCIPAL_INTEREST'")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN periodicity TEXT NOT NULL DEFAULT 'MONTHLY'")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN start_date TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN end_date TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN emi_amount REAL DEFAULT 0")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN unique_credit_id TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN credit_type TEXT NOT NULL DEFAULT 'PERSONAL'")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN borrower_mobile TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN borrower_address TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN borrower_aadhaar TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN borrower_pan TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN borrower_photo_uri TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN guarantor_available TEXT NOT NULL DEFAULT 'NO'")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN guarantor_name TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN guarantor_mobile TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN guarantor_address TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN guarantor_photo_uri TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN invoice_uri TEXT DEFAULT ''")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN consent_status TEXT NOT NULL DEFAULT 'PENDING'")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN otp_verified INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE udhaar_records SET unique_credit_id='UDH-' || printf('%08d', id) WHERE unique_credit_id=''")
        }
    }

    fun addRecord(
        personName: String, amount: Double, roi: Double, repaymentMethod: String,
        periodicity: String, startDate: String, endDate: String, emiAmount: Double, notes: String
    ): Long = addRecordV32(
        creditType = "PERSONAL", personName = personName, mobile = "", address = "",
        aadhaar = "", pan = "", borrowerPhotoUri = "", amount = amount, roi = roi,
        repaymentMethod = repaymentMethod, periodicity = periodicity, startDate = startDate,
        endDate = endDate, emiAmount = emiAmount, guarantorAvailable = false,
        guarantorName = "", guarantorMobile = "", guarantorAddress = "", guarantorPhotoUri = "",
        invoiceUri = "", consentStatus = "PENDING", otpVerified = false, notes = notes
    )

    fun addRecordV32(
        creditType: String, personName: String, mobile: String, address: String,
        aadhaar: String, pan: String, borrowerPhotoUri: String, amount: Double, roi: Double,
        repaymentMethod: String, periodicity: String, startDate: String, endDate: String,
        emiAmount: Double, guarantorAvailable: Boolean, guarantorName: String,
        guarantorMobile: String, guarantorAddress: String, guarantorPhotoUri: String,
        invoiceUri: String, consentStatus: String, otpVerified: Boolean, notes: String
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("unique_credit_id", "UDH-" + UUID.randomUUID().toString().take(8).uppercase())
            put("credit_type", creditType)
            put("person_name", personName)
            put("borrower_mobile", mobile)
            put("borrower_address", address)
            put("borrower_aadhaar", aadhaar)
            put("borrower_pan", pan)
            put("borrower_photo_uri", borrowerPhotoUri)
            put("amount", amount)
            put("roi", roi)
            put("repayment_method", repaymentMethod)
            put("periodicity", periodicity)
            put("start_date", startDate)
            put("end_date", endDate)
            put("emi_amount", emiAmount)
            put("due_date", endDate)
            put("guarantor_available", if (guarantorAvailable) "YES" else "NO")
            put("guarantor_name", guarantorName)
            put("guarantor_mobile", guarantorMobile)
            put("guarantor_address", guarantorAddress)
            put("guarantor_photo_uri", guarantorPhotoUri)
            put("invoice_uri", invoiceUri)
            put("consent_status", consentStatus)
            put("otp_verified", if (otpVerified) 1 else 0)
            put("notes", notes)
            put("status", "UNPAID")
        }
        return db.insert("udhaar_records", null, values)
    }
}
