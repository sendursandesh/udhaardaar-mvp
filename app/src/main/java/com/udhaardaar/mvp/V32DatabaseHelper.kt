package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class V32DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "udhaardaar_v32.db", null, 5) {
    data class User(val id: String, val name: String, val mobile: String, val address: String, val email: String, val photo: String?)
    data class ProfileRow(val rowId: Long, val id: String, val name: String, val mobile: String, val alternate: String, val address: String, val city: String, val state: String, val pin: String, val pan: String, val aadhaar: String, val gstin: String, val photo: String?)
    data class CreditRow(val id: Long, val creditId: String, val borrowerName: String, val type: String, val direction: String, val amount: Double, val roi: Double, val method: String, val payable: Double, val start: String, val end: String, val status: String)
    data class ScheduleRow(val id: Long, val creditId: String, val creditDbId: Long, val dueDate: String, val amount: Double, val status: String)
    data class Summary(val total: Double, val outstanding: Double, val active: Int, val overdue: Int)

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE user_profile(id INTEGER PRIMARY KEY AUTOINCREMENT,unique_id TEXT UNIQUE NOT NULL,name TEXT NOT NULL,mobile TEXT NOT NULL,address TEXT,email TEXT,photo_uri TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE profiles(id INTEGER PRIMARY KEY AUTOINCREMENT,unique_id TEXT UNIQUE NOT NULL,role TEXT NOT NULL,name TEXT NOT NULL,mobile TEXT NOT NULL,alternate_mobile TEXT,address TEXT,city TEXT,state TEXT,pin TEXT,pan TEXT,aadhaar TEXT,gstin TEXT,photo_uri TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE credits(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id TEXT UNIQUE NOT NULL,borrower_id INTEGER NOT NULL,guarantor_id INTEGER,credit_type TEXT NOT NULL,direction TEXT NOT NULL,principal REAL NOT NULL,roi REAL NOT NULL,tenor_months INTEGER NOT NULL,repayment_method TEXT NOT NULL,installment REAL NOT NULL,total_interest REAL NOT NULL,total_payable REAL NOT NULL,start_date TEXT NOT NULL,end_date TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'ACTIVE',invoice_ref TEXT,invoice_uri TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE repayment_schedule(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,installment_no INTEGER NOT NULL,due_date TEXT NOT NULL,amount REAL NOT NULL,paid_amount REAL NOT NULL DEFAULT 0,status TEXT NOT NULL DEFAULT 'DUE')")
        db.execSQL("CREATE TABLE repayments(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,amount REAL NOT NULL,repayment_date TEXT NOT NULL)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS repayment_schedule(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,installment_no INTEGER NOT NULL,due_date TEXT NOT NULL,amount REAL NOT NULL,paid_amount REAL NOT NULL DEFAULT 0,status TEXT NOT NULL DEFAULT 'DUE')")
            db.execSQL("CREATE TABLE IF NOT EXISTS repayments(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,amount REAL NOT NULL,repayment_date TEXT NOT NULL)")
        }
    }

    private fun now(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    private fun day(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun profile(c: Cursor): ProfileRow = ProfileRow(
        c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
        c.getString(4) ?: "", c.getString(5) ?: "", c.getString(6) ?: "",
        c.getString(7) ?: "", c.getString(8) ?: "", c.getString(9) ?: "",
        c.getString(10) ?: "", c.getString(11) ?: "", c.getString(12)
    )

    fun hasUser(): Boolean = readableDatabase.rawQuery("SELECT 1 FROM user_profile LIMIT 1", null).use { it.moveToFirst() }

    fun userData(): User? = readableDatabase.rawQuery("SELECT unique_id,name,mobile,address,email,photo_uri FROM user_profile LIMIT 1", null).use {
        if (!it.moveToFirst()) null else User(it.getString(0), it.getString(1), it.getString(2), it.getString(3) ?: "", it.getString(4) ?: "", it.getString(5))
    }

    fun saveUser(id: String, name: String, mobile: String, address: String, email: String, photo: String?) = writableDatabase.insert("user_profile", null, ContentValues().apply {
        put("unique_id", id); put("name", name); put("mobile", mobile); put("address", address); put("email", email); put("photo_uri", photo); put("created_at", now())
    })

    fun upsertProfile(rowId: Long?, role: String, id: String, name: String, mobile: String, alternate: String, address: String, city: String, state: String, pin: String, pan: String, aadhaar: String, gstin: String, photo: String?): Long {
        val v = ContentValues().apply {
            put("unique_id", id); put("role", role); put("name", name); put("mobile", mobile); put("alternate_mobile", alternate)
            put("address", address); put("city", city); put("state", state); put("pin", pin); put("pan", pan); put("aadhaar", aadhaar); put("gstin", gstin); put("photo_uri", photo); put("created_at", now())
        }
        return if (rowId == null) writableDatabase.insert("profiles", null, v) else {
            writableDatabase.update("profiles", v, "id=?", arrayOf(rowId.toString())); rowId
        }
    }

    fun searchProfiles(role: String, q: String): List<ProfileRow> {
        val out = mutableListOf<ProfileRow>()
        val term = "%${q.trim()}%"
        val sql = "SELECT id,unique_id,name,mobile,alternate_mobile,address,city,state,pin,pan,aadhaar,gstin,photo_uri FROM profiles WHERE role=? AND (name LIKE ? OR mobile LIKE ? OR alternate_mobile LIKE ? OR pan LIKE ? OR aadhaar LIKE ? OR unique_id LIKE ? OR gstin LIKE ?) ORDER BY name"
        readableDatabase.rawQuery(sql, arrayOf(role, term, term, term, term, term, term, term)).use { while (it.moveToNext()) out.add(profile(it)) }
        return out
    }

    fun profileData(id: Long): ProfileRow? = readableDatabase.rawQuery("SELECT id,unique_id,name,mobile,alternate_mobile,address,city,state,pin,pan,aadhaar,gstin,photo_uri FROM profiles WHERE id=?", arrayOf(id.toString())).use { if (it.moveToFirst()) profile(it) else null }

    fun addCredit(borrower: Long, guarantor: Long?, type: String, direction: String, principal: Double, roi: Double, tenor: Int, method: String, installment: Double, interest: Double, payable: Double, start: String, end: String, invoiceRef: String, invoiceUri: String?, verified: Boolean): Long {
        val v = ContentValues().apply {
            put("credit_id", "CR-${System.currentTimeMillis()}"); put("borrower_id", borrower)
            if (guarantor == null) putNull("guarantor_id") else put("guarantor_id", guarantor)
            put("credit_type", type); put("direction", direction); put("principal", principal); put("roi", roi); put("tenor_months", tenor)
            put("repayment_method", method); put("installment", installment); put("total_interest", interest); put("total_payable", payable)
            put("start_date", start); put("end_date", end); put("status", "ACTIVE"); put("invoice_ref", invoiceRef); put("invoice_uri", invoiceUri); put("created_at", now())
        }
        return writableDatabase.insertOrThrow("credits", null, v)
    }

    fun createSchedule(creditId: Long, amount: Double, count: Int, end: String) {
        val base = Calendar.getInstance()
        for (i in 1..count.coerceIn(1, 240)) {
            val c = base.clone() as Calendar
            c.add(Calendar.MONTH, i - 1)
            val calculated = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
            val due = if (i == count) end else calculated
            val status = if (due < day()) "OVERDUE" else "DUE"
            writableDatabase.insert("repayment_schedule", null, ContentValues().apply {
                put("credit_id", creditId); put("installment_no", i); put("due_date", due); put("amount", amount); put("paid_amount", 0.0); put("status", status)
            })
        }
    }

    private fun credit(c: Cursor): CreditRow = CreditRow(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getDouble(5), c.getDouble(6), c.getString(7), c.getDouble(8), c.getString(9), c.getString(10), c.getString(11))
    private val creditSql = "SELECT c.id,c.credit_id,p.name,c.credit_type,c.direction,c.principal,c.roi,c.repayment_method,c.total_payable,c.start_date,c.end_date,c.status FROM credits c JOIN profiles p ON p.id=c.borrower_id"

    fun credits(direction: String?): List<CreditRow> {
        val out = mutableListOf<CreditRow>()
        val sql: String
        val args: Array<String>?
        if (direction == null) { sql = "$creditSql ORDER BY c.id DESC"; args = null } else { sql = "$creditSql WHERE c.direction=? ORDER BY c.id DESC"; args = arrayOf(direction) }
        readableDatabase.rawQuery(sql, args).use { while (it.moveToNext()) out.add(credit(it)) }
        return out
    }

    private fun borrowerId(id: Long): Long = readableDatabase.rawQuery("SELECT borrower_id FROM credits WHERE id=?", arrayOf(id.toString())).use { if (it.moveToFirst()) it.getLong(0) else -1L }
    fun creditsForBorrower(id: Long): List<CreditRow> = credits(null).filter { borrowerId(it.id) == id }
    fun creditDetail(id: Long): CreditRow? = readableDatabase.rawQuery("$creditSql WHERE c.id=?", arrayOf(id.toString())).use { if (it.moveToFirst()) credit(it) else null }

    fun paidForCredit(id: Long): Double = readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM repayments WHERE credit_id=?", arrayOf(id.toString())).use { it.moveToFirst(); it.getDouble(0) }

    fun borrowerSummary(id: Long): Summary {
        var total = 0.0; var outstanding = 0.0; var active = 0; var overdue = 0
        creditsForBorrower(id).forEach {
            total += it.amount
            outstanding += (it.payable - paidForCredit(it.id)).coerceAtLeast(0.0)
            if (it.status == "ACTIVE") active++
            if (schedules(it.id, true).isNotEmpty()) overdue++
        }
        return Summary(total, outstanding, active, overdue)
    }

    fun totalCredit(direction: String): Double = readableDatabase.rawQuery("SELECT COALESCE(SUM(principal),0) FROM credits WHERE direction=?", arrayOf(direction)).use { it.moveToFirst(); it.getDouble(0) }
    fun dueCount(overdue: Boolean): Int = schedules(null, overdue).size

    fun schedules(creditId: Long?, overdueOnly: Boolean): List<ScheduleRow> {
        writableDatabase.execSQL("UPDATE repayment_schedule SET status='OVERDUE' WHERE due_date < ? AND status='DUE'", arrayOf(day()))
        val out = mutableListOf<ScheduleRow>()
        val statusClause = if (overdueOnly) "s.status='OVERDUE'" else "s.status IN ('DUE','OVERDUE')"
        val where: String
        val args: Array<String>?
        if (creditId != null) { where = "WHERE s.credit_id=? AND $statusClause"; args = arrayOf(creditId.toString()) } else { where = "WHERE $statusClause"; args = null }
        val sql = "SELECT s.id,c.credit_id,c.id,s.due_date,s.amount,s.status FROM repayment_schedule s JOIN credits c ON c.id=s.credit_id $where ORDER BY s.due_date"
        readableDatabase.rawQuery(sql, args).use { while (it.moveToNext()) out.add(ScheduleRow(it.getLong(0), it.getString(1), it.getLong(2), it.getString(3), it.getDouble(4), it.getString(5))) }
        return out
    }

    fun recordPayment(scheduleId: Long, creditId: Long, amount: Double) {
        if (amount <= 0.0) return
        writableDatabase.insert("repayments", null, ContentValues().apply { put("credit_id", creditId); put("amount", amount); put("repayment_date", now()) })
        readableDatabase.rawQuery("SELECT amount,paid_amount FROM repayment_schedule WHERE id=?", arrayOf(scheduleId.toString())).use {
            if (it.moveToFirst()) {
                val paid = it.getDouble(1) + amount
                val status = if (paid >= it.getDouble(0) - 0.01) "PAID" else "DUE"
                writableDatabase.update("repayment_schedule", ContentValues().apply { put("paid_amount", paid); put("status", status) }, "id=?", arrayOf(scheduleId.toString()))
            }
        }
    }
}
