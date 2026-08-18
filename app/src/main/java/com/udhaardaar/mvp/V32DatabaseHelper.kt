package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class V32DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "udhaardaar_v32.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE user_profile (id INTEGER PRIMARY KEY AUTOINCREMENT, unique_id TEXT UNIQUE NOT NULL, name TEXT NOT NULL, mobile TEXT NOT NULL, address TEXT, email TEXT, photo_uri TEXT, created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE profiles (id INTEGER PRIMARY KEY AUTOINCREMENT, unique_id TEXT UNIQUE NOT NULL, role TEXT NOT NULL, name TEXT NOT NULL, mobile TEXT NOT NULL, alternate_mobile TEXT, address TEXT, city TEXT, state TEXT, pin TEXT, email TEXT, pan TEXT, aadhaar TEXT, occupation TEXT, business_name TEXT, gstin TEXT, photo_uri TEXT, created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE credits (id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id TEXT UNIQUE NOT NULL, borrower_id INTEGER NOT NULL, guarantor_id INTEGER, credit_type TEXT NOT NULL, direction TEXT NOT NULL, principal REAL NOT NULL, roi REAL NOT NULL, tenor_months INTEGER NOT NULL, repayment_method TEXT NOT NULL, installment REAL NOT NULL, total_interest REAL NOT NULL, total_payable REAL NOT NULL, start_date TEXT NOT NULL, end_date TEXT NOT NULL, otp_verified INTEGER NOT NULL, status TEXT NOT NULL, created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE repayments (id INTEGER PRIMARY KEY AUTOINCREMENT, credit_id INTEGER NOT NULL, amount REAL NOT NULL, principal_component REAL NOT NULL, interest_component REAL NOT NULL, repayment_date TEXT NOT NULL)")
        db.execSQL("CREATE TABLE activity_log (id INTEGER PRIMARY KEY AUTOINCREMENT, actor_id TEXT, profile_id INTEGER, credit_id INTEGER, activity_type TEXT NOT NULL, description TEXT, created_at TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun user(): android.database.Cursor = readableDatabase.rawQuery("SELECT * FROM user_profile LIMIT 1", null)

    fun saveUser(id: String, name: String, mobile: String, address: String, email: String, photo: String?): Long {
        val v = ContentValues().apply { put("unique_id", id); put("name", name); put("mobile", mobile); put("address", address); put("email", email); put("photo_uri", photo); put("created_at", now()) }
        return writableDatabase.insert("user_profile", null, v)
    }

    fun addProfile(role: String, id: String, name: String, mobile: String, alternate: String, address: String, city: String, state: String, pin: String, email: String, pan: String, aadhaar: String, occupation: String, business: String, gstin: String, photo: String?): Long {
        val v = ContentValues().apply {
            put("unique_id", id); put("role", role); put("name", name); put("mobile", mobile); put("alternate_mobile", alternate); put("address", address); put("city", city); put("state", state); put("pin", pin); put("email", email); put("pan", pan); put("aadhaar", aadhaar); put("occupation", occupation); put("business_name", business); put("gstin", gstin); put("photo_uri", photo); put("created_at", now())
        }
        return writableDatabase.insert("profiles", null, v)
    }

    fun profiles(role: String) = readableDatabase.rawQuery("SELECT id, unique_id, name, mobile FROM profiles WHERE role=? ORDER BY name COLLATE NOCASE", arrayOf(role))

    fun profile(id: Long) = readableDatabase.rawQuery("SELECT * FROM profiles WHERE id=?", arrayOf(id.toString()))

    fun addCredit(borrower: Long, guarantor: Long?, type: String, direction: String, principal: Double, roi: Double, tenor: Int, method: String, installment: Double, interest: Double, payable: Double, start: String, end: String): Long {
        val creditId = "CR-${System.currentTimeMillis()}"
        val v = ContentValues().apply { put("credit_id", creditId); put("borrower_id", borrower); if (guarantor != null) put("guarantor_id", guarantor) else putNull("guarantor_id"); put("credit_type", type); put("direction", direction); put("principal", principal); put("roi", roi); put("tenor_months", tenor); put("repayment_method", method); put("installment", installment); put("total_interest", interest); put("total_payable", payable); put("start_date", start); put("end_date", end); put("otp_verified", 1); put("status", "ACTIVE"); put("created_at", now()) }
        return writableDatabase.insert("credits", null, v)
    }

    fun log(actor: String?, profile: Long?, credit: Long?, type: String, description: String) {
        val v = ContentValues().apply { put("actor_id", actor); if (profile != null) put("profile_id", profile) else putNull("profile_id"); if (credit != null) put("credit_id", credit) else putNull("credit_id"); put("activity_type", type); put("description", description); put("created_at", now()) }
        writableDatabase.insert("activity_log", null, v)
    }

    fun borrowerCount(): Int { val c=readableDatabase.rawQuery("SELECT COUNT(*) FROM profiles WHERE role='BORROWER'",null); c.moveToFirst(); val n=c.getInt(0); c.close(); return n }
    fun guarantorCount(): Int { val c=readableDatabase.rawQuery("SELECT COUNT(*) FROM profiles WHERE role='GUARANTOR'",null); c.moveToFirst(); val n=c.getInt(0); c.close(); return n }
    fun creditCount(): Int { val c=readableDatabase.rawQuery("SELECT COUNT(*) FROM credits",null); c.moveToFirst(); val n=c.getInt(0); c.close(); return n }
    fun now() = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
}
