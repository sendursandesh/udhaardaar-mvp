package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UdhaarDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "udhaardaar.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE udhaar_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                person_name TEXT NOT NULL,
                amount REAL NOT NULL,
                roi REAL NOT NULL DEFAULT 0,
                repayment_method TEXT NOT NULL,
                periodicity TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                emi_amount REAL DEFAULT 0,
                due_date TEXT,
                notes TEXT,
                status TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {

            db.execSQL(
                "ALTER TABLE udhaar_records ADD COLUMN roi REAL NOT NULL DEFAULT 0"
            )

            db.execSQL(
                "ALTER TABLE udhaar_records ADD COLUMN repayment_method TEXT NOT NULL DEFAULT 'PRINCIPAL_INTEREST'"
            )

            db.execSQL(
                "ALTER TABLE udhaar_records ADD COLUMN periodicity TEXT NOT NULL DEFAULT 'MONTHLY'"
            )

            db.execSQL(
                "ALTER TABLE udhaar_records ADD COLUMN start_date TEXT NOT NULL DEFAULT ''"
            )

            db.execSQL(
                "ALTER TABLE udhaar_records ADD COLUMN end_date TEXT NOT NULL DEFAULT ''"
            )

            db.execSQL(
                "ALTER TABLE udhaar_records ADD COLUMN emi_amount REAL DEFAULT 0"
            )
        }
    }

    fun addRecord(
        personName: String,
        amount: Double,
        roi: Double,
        repaymentMethod: String,
        periodicity: String,
        startDate: String,
        endDate: String,
        emiAmount: Double,
        notes: String
    ): Long {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("person_name", personName)
            put("amount", amount)
            put("roi", roi)
            put("repayment_method", repaymentMethod)
            put("periodicity", periodicity)
            put("start_date", startDate)
            put("end_date", endDate)
            put("emi_amount", emiAmount)
            put("due_date", endDate)
            put("notes", notes)
            put("status", "UNPAID")
        }

        return db.insert("udhaar_records", null, values)
    }
}
