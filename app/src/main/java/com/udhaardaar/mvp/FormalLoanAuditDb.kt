package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormalLoanAuditDb(context: Context) : SQLiteOpenHelper(context, "formal_loan_audit.db", null, 1) {
    override fun onCreate(db: android.database.sqlite.SQLiteDatabase) {
        db.execSQL("CREATE TABLE loans(id INTEGER PRIMARY KEY AUTOINCREMENT,lender TEXT NOT NULL,account_no TEXT,loan_type TEXT,sanctioned REAL,disbursed REAL,roi REAL,rate_type TEXT,tenure_months INTEGER,emi REAL,due_day INTEGER,processing_fee REAL DEFAULT 0,documentation_fee REAL DEFAULT 0,insurance REAL DEFAULT 0,penal_rate REAL DEFAULT 0,bounce_charge REAL DEFAULT 0,prepayment_charge REAL DEFAULT 0,other_charge REAL DEFAULT 0,sanction_uri TEXT,statement_uri TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE statement_entries(id INTEGER PRIMARY KEY AUTOINCREMENT,loan_id INTEGER NOT NULL,entry_date TEXT,description TEXT,amount REAL NOT NULL,charge_type TEXT,expected REAL DEFAULT 0,variance REAL DEFAULT 0,review TEXT NOT NULL,FOREIGN KEY(loan_id) REFERENCES loans(id) ON DELETE CASCADE)")
    }
    override fun onUpgrade(db: android.database.sqlite.SQLiteDatabase, oldVersion: Int, newVersion: Int) { }
    fun addLoan(v: ContentValues): Long = writableDatabase.insertOrThrow("loans", null, v)
    fun addEntry(v: ContentValues): Long = writableDatabase.insertOrThrow("statement_entries", null, v)
    fun latestLoan(): android.database.Cursor = readableDatabase.rawQuery("SELECT * FROM loans ORDER BY id DESC LIMIT 1", null)
    fun entries(loanId: Long): android.database.Cursor = readableDatabase.rawQuery("SELECT entry_date,description,amount,charge_type,expected,variance,review FROM statement_entries WHERE loan_id=? ORDER BY id", arrayOf(loanId.toString()))
    fun clearEntries(loanId: Long) { writableDatabase.delete("statement_entries", "loan_id=?", arrayOf(loanId.toString())) }
    companion object { fun now() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()) }
}
