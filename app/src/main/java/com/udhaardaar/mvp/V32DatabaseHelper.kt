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

class V32DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "udhaardaar_v32.db", null, 7) {
    data class User(val id: String, val name: String, val mobile: String, val address: String, val email: String, val photo: String?)
    data class ProfileRow(val rowId: Long, val id: String, val name: String, val mobile: String, val alternate: String, val address: String, val city: String, val state: String, val pin: String, val pan: String, val aadhaar: String, val gstin: String, val photo: String?, val role: String)
    data class CreditRow(val id: Long, val creditId: String, val borrowerName: String, val type: String, val direction: String, val amount: Double, val roi: Double, val method: String, val payable: Double, val start: String, val end: String, val status: String)
    data class ScheduleRow(val id: Long, val creditId: String, val creditDbId: Long, val dueDate: String, val amount: Double, val status: String)
    data class Summary(val total: Double, val outstanding: Double, val active: Int, val overdue: Int)

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE user_profile(id INTEGER PRIMARY KEY AUTOINCREMENT,unique_id TEXT UNIQUE NOT NULL,name TEXT NOT NULL,mobile TEXT NOT NULL,address TEXT,email TEXT,photo_uri TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE profiles(id INTEGER PRIMARY KEY AUTOINCREMENT,unique_id TEXT UNIQUE NOT NULL,role TEXT NOT NULL,name TEXT NOT NULL,mobile TEXT NOT NULL,alternate_mobile TEXT,address TEXT,city TEXT,state TEXT,pin TEXT,pan TEXT,aadhaar TEXT,gstin TEXT,photo_uri TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE credits(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id TEXT UNIQUE NOT NULL,borrower_id INTEGER NOT NULL,guarantor_id INTEGER,credit_type TEXT NOT NULL,direction TEXT NOT NULL,principal REAL NOT NULL,roi REAL NOT NULL,tenor_months INTEGER NOT NULL,repayment_method TEXT NOT NULL,installment REAL NOT NULL,total_interest REAL NOT NULL,total_payable REAL NOT NULL,start_date TEXT NOT NULL,end_date TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'ACTIVE',invoice_ref TEXT,invoice_uri TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE repayment_schedule(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,installment_no INTEGER NOT NULL,due_date TEXT NOT NULL,amount REAL NOT NULL,paid_amount REAL NOT NULL DEFAULT 0,status TEXT NOT NULL DEFAULT 'DUE')")
        db.execSQL("CREATE TABLE repayments(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,amount REAL NOT NULL,repayment_date TEXT NOT NULL)")
        db.execSQL("CREATE TABLE credit_documents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,document_type TEXT NOT NULL,document_uri TEXT NOT NULL,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE credit_consents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,verification_method TEXT NOT NULL,consent_type TEXT NOT NULL,verified_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE credit_defaults(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER UNIQUE NOT NULL,reason TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'OPEN',declared_at TEXT NOT NULL,resolved_at TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS repayment_schedule(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,installment_no INTEGER NOT NULL,due_date TEXT NOT NULL,amount REAL NOT NULL,paid_amount REAL NOT NULL DEFAULT 0,status TEXT NOT NULL DEFAULT 'DUE')")
            db.execSQL("CREATE TABLE IF NOT EXISTS repayments(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,amount REAL NOT NULL,repayment_date TEXT NOT NULL)")
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS credit_documents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,document_type TEXT NOT NULL,document_uri TEXT NOT NULL,created_at TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS credit_consents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,verification_method TEXT NOT NULL,consent_type TEXT NOT NULL,verified_at TEXT NOT NULL)")
        }
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS credit_defaults(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER UNIQUE NOT NULL,reason TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'OPEN',declared_at TEXT NOT NULL,resolved_at TEXT)")
        }
    }

    private fun now(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    private fun day(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun profile(c: Cursor): ProfileRow = ProfileRow(
        c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
        c.getString(4) ?: "", c.getString(5) ?: "", c.getString(6) ?: "",
        c.getString(7) ?: "", c.getString(8) ?: "", c.getString(9) ?: "",
        c.getString(10) ?: "", c.getString(11) ?: "", c.getString(12), c.getString(13)
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
        val sql = "SELECT id,unique_id,name,mobile,alternate_mobile,address,city,state,pin,pan,aadhaar,gstin,photo_uri,role FROM profiles WHERE role=? AND (name LIKE ? OR mobile LIKE ? OR alternate_mobile LIKE ? OR pan LIKE ? OR aadhaar LIKE ? OR unique_id LIKE ? OR gstin LIKE ?) ORDER BY name"
        readableDatabase.rawQuery(sql, arrayOf(role, term, term, term, term, term, term, term)).use { while (it.moveToNext()) out.add(profile(it)) }
        return out
    }

    fun profileData(id: Long): ProfileRow? = readableDatabase.rawQuery("SELECT id,unique_id,name,mobile,alternate_mobile,address,city,state,pin,pan,aadhaar,gstin,photo_uri,role FROM profiles WHERE id=?", arrayOf(id.toString())).use { if (it.moveToFirst()) profile(it) else null }

    fun addCredit(borrower: Long, guarantor: Long?, type: String, direction: String, principal: Double, roi: Double, tenor: Int, method: String, installment: Double, interest: Double, payable: Double, start: String, end: String, invoiceRef: String, invoiceUri: String?, verified: Boolean): Long {
        require(profileData(borrower)?.let { it.role == "BORROWER" } == true) { "Invalid borrower profile" }
        if (guarantor != null) require(profileData(guarantor)?.let { it.role == "GUARANTOR" } == true) { "Invalid guarantor profile" }
        require(principal > 0.0 && tenor in 1..240 && payable >= principal) { "Invalid credit terms" }
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

    fun saveConsent(creditId: Long, verificationMethod: String, consentType: String): Long =
        writableDatabase.insertOrThrow("credit_consents", null, ContentValues().apply {
            put("credit_id", creditId); put("verification_method", verificationMethod); put("consent_type", consentType); put("verified_at", now())
        })

    fun saveDocument(creditId: Long, documentType: String, uri: String): Long =
        writableDatabase.insertOrThrow("credit_documents", null, ContentValues().apply {
            put("credit_id", creditId); put("document_type", documentType); put("document_uri", uri); put("created_at", now())
        })

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

    fun recordPayment(scheduleId: Long, creditId: Long, amount: Double): Double {
        if (amount <= 0.0) return 0.0
        val db = writableDatabase
        db.beginTransaction()
        return try {
            val payable = db.rawQuery("SELECT total_payable FROM credits WHERE id=?", arrayOf(creditId.toString())).use { if (it.moveToFirst()) it.getDouble(0) else 0.0 }
            val alreadyPaid = paidForCredit(creditId)
            val accepted = amount.coerceAtMost((payable - alreadyPaid).coerceAtLeast(0.0))
            if (accepted <= 0.0) return 0.0
            db.insertOrThrow("repayments", null, ContentValues().apply {
                put("credit_id", creditId); put("amount", accepted); put("repayment_date", now())
            })
            var remaining = accepted
            val c = db.rawQuery("SELECT id,amount,paid_amount FROM repayment_schedule WHERE credit_id=? AND status<>'PAID' ORDER BY CASE WHEN id=? THEN 0 ELSE 1 END, installment_no", arrayOf(creditId.toString(), scheduleId.toString()))
            c.use {
                while (it.moveToNext() && remaining > 0.0) {
                    val sid = it.getLong(0); val due = it.getDouble(1); val paid = it.getDouble(2)
                    val allocation = remaining.coerceAtMost((due - paid).coerceAtLeast(0.0))
                    if (allocation > 0.0) {
                        val newPaid = paid + allocation
                        val status = if (newPaid >= due - 0.01) "PAID" else "DUE"
                        db.update("repayment_schedule", ContentValues().apply { put("paid_amount", newPaid); put("status", status) }, "id=?", arrayOf(sid.toString()))
                        remaining -= allocation
                    }
                }
            }
            refreshCreditStatus(creditId, db)
            db.setTransactionSuccessful()
            accepted
        } finally { db.endTransaction() }
    }

    private fun refreshCreditStatus(creditId: Long, db: SQLiteDatabase = writableDatabase) {
        val payable = db.rawQuery("SELECT total_payable FROM credits WHERE id=?", arrayOf(creditId.toString())).use { if (it.moveToFirst()) it.getDouble(0) else return }
        val paid = db.rawQuery("SELECT COALESCE(SUM(amount),0) FROM repayments WHERE credit_id=?", arrayOf(creditId.toString())).use { it.moveToFirst(); it.getDouble(0) }
        val overdue = db.rawQuery("SELECT 1 FROM repayment_schedule WHERE credit_id=? AND status='OVERDUE' AND paid_amount < amount LIMIT 1", arrayOf(creditId.toString())).use { it.moveToFirst() }
        val status = when {
            paid >= payable - 0.01 -> "CLOSED"
            overdue -> "OVERDUE"
            else -> "ACTIVE"
        }
        db.update("credits", ContentValues().apply { put("status", status) }, "id=?", arrayOf(creditId.toString()))
        if (overdue) {
            db.insertWithOnConflict("credit_defaults", null, ContentValues().apply {
                put("credit_id", creditId); put("reason", "Unpaid scheduled amount past due date"); put("status", "OPEN"); put("declared_at", now())
            }, SQLiteDatabase.CONFLICT_IGNORE)
        } else {
            db.update("credit_defaults", ContentValues().apply { put("status", "RESOLVED"); put("resolved_at", now()) }, "credit_id=? AND status='OPEN'", arrayOf(creditId.toString()))
        }
    }
}
