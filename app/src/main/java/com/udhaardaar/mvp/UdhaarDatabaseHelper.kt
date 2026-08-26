package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UdhaarDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "udhaardaar.db", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE udhaar_records (id INTEGER PRIMARY KEY AUTOINCREMENT, person_name TEXT NOT NULL, amount REAL NOT NULL, roi REAL NOT NULL DEFAULT 0, repayment_method TEXT NOT NULL, periodicity TEXT NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL, emi_amount REAL DEFAULT 0, due_date TEXT, notes TEXT, status TEXT NOT NULL, lender_user_id TEXT, borrower_user_id TEXT, consent_granted INTEGER NOT NULL DEFAULT 0, consent_revoked INTEGER NOT NULL DEFAULT 0)""")
        db.execSQL("""CREATE TABLE repayments (id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER NOT NULL, amount REAL NOT NULL, recorded_by TEXT NOT NULL, recorded_at TEXT NOT NULL, note TEXT, FOREIGN KEY(credit_id) REFERENCES udhaar_records(id))""")
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
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN lender_user_id TEXT")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN borrower_user_id TEXT")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN consent_granted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE udhaar_records ADD COLUMN consent_revoked INTEGER NOT NULL DEFAULT 0")
            db.execSQL("CREATE TABLE IF NOT EXISTS repayments (id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER NOT NULL, amount REAL NOT NULL, recorded_by TEXT NOT NULL, recorded_at TEXT NOT NULL, note TEXT, FOREIGN KEY(credit_id) REFERENCES udhaar_records(id))")
        }
    }

    fun addRecord(personName: String, amount: Double, roi: Double, repaymentMethod: String, periodicity: String, startDate: String, endDate: String, emiAmount: Double, notes: String): Long {
        val values = ContentValues().apply {
            put("person_name", personName); put("amount", amount); put("roi", roi); put("repayment_method", repaymentMethod); put("periodicity", periodicity); put("start_date", startDate); put("end_date", endDate); put("emi_amount", emiAmount); put("due_date", endDate); put("notes", notes); put("status", "UNPAID")
        }
        return writableDatabase.insertOrThrow("udhaar_records", null, values)
    }

    fun persistAuthorisedRepayment(receipt: RepaymentService.RepaymentReceipt): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("credit_id", receipt.creditId); put("amount", receipt.amount); put("recorded_by", receipt.recordedBy); put("recorded_at", receipt.recordedAt); put("note", receipt.note)
        }
        return db.insert("repayments", null, values) != -1L
    }

    fun totalRepayments(creditId: Long): Double {
        readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM repayments WHERE credit_id=?", arrayOf(creditId.toString())).use { c -> return if (c.moveToFirst()) c.getDouble(0) else 0.0 }
    }
}
