package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UdhaarDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "udhaardaar.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE udhaar_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                person_name TEXT NOT NULL,
                amount REAL NOT NULL,
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
        db.execSQL("DROP TABLE IF EXISTS udhaar_records")
        onCreate(db)
    }

    fun addRecord(
        personName: String,
        amount: Double,
        dueDate: String,
        notes: String
    ): Long {

        val db = writableDatabase

        val values = ContentValues().apply {
            put("person_name", personName)
            put("amount", amount)
            put("due_date", dueDate)
            put("notes", notes)
            put("status", "UNPAID")
        }

        return db.insert("udhaar_records", null, values)
    }
}
