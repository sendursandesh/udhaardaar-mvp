package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.security.MessageDigest

class V32DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "udhaardaar_v322.db", null, 5) {
    data class User(val id:String,val name:String,val mobile:String,val address:String,val email:String,val photo:String?)
    data class Profile(val rowId:Long,val id:String,val role:String,val name:String,val mobile:String,val alternateMobile:String,val address:String,val city:String,val state:String,val pin:String,val pan:String,val aadhaar:String,val gstin:String,val photo:String?)
    data class Credit(val rowId:Long,val code:String,val borrowerId:Long,val guarantorId:Long?,val borrowerName:String,val type:String,val direction:String,val amount:Double,val roi:Double,val method:String,val installment:Double,val interest:Double,val payable:Double,val start:String,val end:String,val due:String,val gstin:String,val invoice:String,val nach:Boolean,val status:String)
    data class Schedule(val id:Long,val creditId:Long,val code:String,val due:String,val amount:Double,val paid:Double,val status:String)

    private fun now() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
    private fun consentHash(token:String)=MessageDigest.getInstance("SHA-256").digest(token.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
    private fun date(s:String):Date = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(s)!! }.getOrElse { Date() }
    private fun fmt(d:Date) = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d)

    override fun onCreate(db:SQLiteDatabase) {
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT,uid TEXT UNIQUE,name TEXT,mobile TEXT,address TEXT,email TEXT,photo TEXT,created TEXT) ")
        db.execSQL("CREATE TABLE profiles(id INTEGER PRIMARY KEY AUTOINCREMENT,uid TEXT UNIQUE,role TEXT,name TEXT,mobile TEXT,alternate_mobile TEXT,address TEXT,city TEXT,state TEXT,pin TEXT,pan TEXT,aadhaar TEXT,gstin TEXT,photo TEXT,created TEXT) ")
        db.execSQL("CREATE TABLE credits(id INTEGER PRIMARY KEY AUTOINCREMENT,code TEXT UNIQUE,borrower_id INTEGER,guarantor_id INTEGER,type TEXT,direction TEXT,amount REAL,roi REAL,method TEXT,installment REAL,interest REAL,payable REAL,start_date TEXT,end_date TEXT,due_date TEXT,gstin TEXT,invoice TEXT,nach INTEGER,status TEXT,otp INTEGER,created TEXT) ")
        db.execSQL("CREATE TABLE schedules(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER,no INTEGER,due_date TEXT,amount REAL,paid REAL DEFAULT 0,status TEXT) ")
        db.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER,schedule_id INTEGER,amount REAL,date TEXT) ")
        db.execSQL("CREATE TABLE repayment_consents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER,schedule_id INTEGER,amount REAL,proposer_role TEXT,consent_token TEXT,status TEXT,created TEXT,confirmed TEXT) ")
        db.execSQL("CREATE TABLE banks(id INTEGER PRIMARY KEY AUTOINCREMENT,owner_type TEXT,owner_id TEXT,holder TEXT,bank TEXT,account TEXT,ifsc TEXT,upi TEXT,nach INTEGER,created TEXT) ")
    }
    override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int) {
        if(oldVersion<2){
            runCatching{db.execSQL("ALTER TABLE profiles ADD COLUMN city TEXT")}
            runCatching{db.execSQL("ALTER TABLE profiles ADD COLUMN state TEXT")}
            runCatching{db.execSQL("ALTER TABLE profiles ADD COLUMN pin TEXT")}
        }
        if(oldVersion<3) db.execSQL("CREATE TABLE IF NOT EXISTS repayment_consents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER,schedule_id INTEGER,amount REAL,proposer_role TEXT,consent_token TEXT,status TEXT,created TEXT,confirmed TEXT) ")
        if(oldVersion<4) runCatching{db.execSQL("ALTER TABLE credits ADD COLUMN guarantor_id INTEGER")}
        if(oldVersion<5) runCatching{db.execSQL("ALTER TABLE profiles ADD COLUMN alternate_mobile TEXT")}
    }

    fun hasUser() = readableDatabase.rawQuery("SELECT id FROM users LIMIT 1",null).use { it.moveToFirst() }
    fun user():User? = readableDatabase.rawQuery("SELECT uid,name,mobile,address,email,photo FROM users LIMIT 1",null).use { if(!it.moveToFirst()) null else User(it.getString(0),it.getString(1),it.getString(2),it.getString(3),it.getString(4)?:(""),it.getString(5)) }
    fun saveUser(uid:String,name:String,mobile:String,address:String,email:String,photo:String?) : Long {
        val v=ContentValues().apply { put("uid",uid);put("name",name);put("mobile",mobile);put("address",address);put("email",email);put("photo",photo);put("created",now()) }
        return writableDatabase.insert("users",null,v)
    }
    fun saveProfile(existing:Long?,role:String,uid:String,name:String,mobile:String,alternateMobile:String,address:String,city:String,state:String,pin:String,pan:String,aadhaar:String,gstin:String,photo:String?):Long {
        val v=ContentValues().apply { put("uid",uid);put("role",role);put("name",name);put("mobile",mobile);put("alternate_mobile",alternateMobile);put("address",address);put("city",city);put("state",state);put("pin",pin);put("pan",pan);put("aadhaar",aadhaar);put("gstin",gstin);put("photo",photo);put("created",now()) }
        if(existing==null) return writableDatabase.insert("profiles",null,v)
        writableDatabase.update("profiles",v,"id=?",arrayOf(existing.toString())); return existing
    }
    private fun profile(c:android.database.Cursor)=Profile(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5)?:"",c.getString(6)?:"",c.getString(7)?:"",c.getString(8)?:"",c.getString(9)?:"",c.getString(10)?:"",c.getString(11)?:"",c.getString(12)?:"",c.getString(13))
    fun profiles(role:String,q:String=""):List<Profile> {
        val out=mutableListOf<Profile>(); val x="%${q.trim()}%"
        readableDatabase.rawQuery("SELECT id,uid,role,name,mobile,alternate_mobile,address,city,state,pin,pan,aadhaar,gstin,photo FROM profiles WHERE role=? AND(name LIKE ? OR mobile LIKE ? OR alternate_mobile LIKE ? OR pan LIKE ? OR aadhaar LIKE ? OR uid LIKE ? OR gstin LIKE ?) ORDER BY name",arrayOf(role,x,x,x,x,x,x,x)).use { while(it.moveToNext()) out.add(profile(it)) }
        return out
    }
    fun profile(id:Long):Profile? = readableDatabase.rawQuery("SELECT id,uid,role,name,mobile,alternate_mobile,address,city,state,pin,pan,aadhaar,gstin,photo FROM profiles WHERE id=?",arrayOf(id.toString())).use { if(!it.moveToFirst()) null else profile(it) }
    fun credit(id:Long):Credit? {
        val sql="SELECT c.id,c.code,c.borrower_id,c.guarantor_id,p.name,c.type,c.direction,c.amount,c.roi,c.method,c.installment,c.interest,c.payable,c.start_date,c.end_date,c.due_date,COALESCE(c.gstin,''),COALESCE(c.invoice,''),c.nach,c.status FROM credits c JOIN profiles p ON p.id=c.borrower_id WHERE c.id=?"
        return readableDatabase.rawQuery(sql,arrayOf(id.toString())).use { if(!it.moveToFirst()) null else Credit(it.getLong(0),it.getString(1),it.getLong(2),if(it.isNull(3)) null else it.getLong(3),it.getString(4),it.getString(5),it.getString(6),it.getDouble(7),it.getDouble(8),it.getString(9),it.getDouble(10),it.getDouble(11),it.getDouble(12),it.getString(13),it.getString(14),it.getString(15),it.getString(16),it.getString(17),it.getInt(18)==1,it.getString(19)) }
    }
    fun credits(direction:String?=null):List<Credit> {
        val out=mutableListOf<Credit>()
        val sql="SELECT c.id,c.code,c.borrower_id,c.guarantor_id,p.name,c.type,c.direction,c.amount,c.roi,c.method,c.installment,c.interest,c.payable,c.start_date,c.end_date,c.due_date,COALESCE(c.gstin,''),COALESCE(c.invoice,''),c.nach,c.status FROM credits c JOIN profiles p ON p.id=c.borrower_id "+if(direction==null) "" else "WHERE c.direction=? "+"ORDER BY c.id DESC"
        readableDatabase.rawQuery(sql,if(direction==null)null else arrayOf(direction)).use {
            while(it.moveToNext()) out.add(Credit(it.getLong(0),it.getString(1),it.getLong(2),if(it.isNull(3)) null else it.getLong(3),it.getString(4),it.getString(5),it.getString(6),it.getDouble(7),it.getDouble(8),it.getString(9),it.getDouble(10),it.getDouble(11),it.getDouble(12),it.getString(13),it.getString(14),it.getString(15),it.getString(16),it.getString(17),it.getInt(18)==1,it.getString(19)))
        }
        return out
    }
    fun addCredit(borrower:Long,guarantorId:Long?,type:String,direction:String,amount:Double,roi:Double,method:String,installment:Double,interest:Double,payable:Double,start:String,end:String,due:String,gstin:String,invoice:String,nach:Boolean):Long {
        val code="CR-${System.currentTimeMillis()}"; val v=ContentValues().apply { put("code",code);put("borrower_id",borrower);put("guarantor_id",guarantorId);put("type",type);put("direction",direction);put("amount",amount);put("roi",roi);put("method",method);put("installment",installment);put("interest",interest);put("payable",payable);put("start_date",start);put("end_date",end);put("due_date",due);put("gstin",gstin);put("invoice",invoice);put("nach",if(nach)1 else 0);put("status","ACTIVE");put("otp",1);put("created",now()) }
        return writableDatabase.insert("credits",null,v)
    }
    fun makeSchedule(creditId:Long,type:String,start:String,end:String,amount:Double,months:Int,dueDay:Int) {
        val db=writableDatabase; val cal=Calendar.getInstance();cal.time=date(start); val n=if(type=="Trade Credit"||type=="Advance") 1 else months.coerceIn(1,240)
        for(i in 1..n){ val c=cal.clone() as Calendar; if(i>1)c.add(Calendar.MONTH,i-1); if(type=="Rental / Lease"&&dueDay in 1..31)c.set(Calendar.DAY_OF_MONTH,minOf(dueDay,c.getActualMaximum(Calendar.DAY_OF_MONTH))); if(n==1)c.time=date(end); val due=fmt(c.time); val status=if(c.time.before(Date())) "OVERDUE" else "DUE"; val v=ContentValues().apply{put("credit_id",creditId);put("no",i);put("due_date",due);put("amount",amount);put("paid",0.0);put("status",status)};db.insert("schedules",null,v) }
    }
    private fun refreshStatuses(){writableDatabase.execSQL("UPDATE schedules SET status='OVERDUE' WHERE due_date<? AND status='DUE'",arrayOf(fmt(Date())))}
    fun schedules(creditId:Long?=null,overdue:Boolean=false):List<Schedule>{refreshStatuses();val out=mutableListOf<Schedule>();val where=when{creditId!=null->"WHERE s.credit_id=?";overdue->"WHERE s.status='OVERDUE'";else->"WHERE s.status IN('DUE','OVERDUE') "};val args=if(creditId!=null)arrayOf(creditId.toString()) else null;readableDatabase.rawQuery("SELECT s.id,s.credit_id,c.code,s.due_date,s.amount,s.paid,s.status FROM schedules s JOIN credits c ON c.id=s.credit_id $where ORDER BY s.due_date",args).use{while(it.moveToNext())out.add(Schedule(it.getLong(0),it.getLong(1),it.getString(2),it.getString(3),it.getDouble(4),it.getDouble(5),it.getString(6)))};return out}
    fun pay(scheduleId:Long,creditId:Long,amount:Double){writableDatabase.insert("payments",null,ContentValues().apply{put("credit_id",creditId);put("schedule_id",scheduleId);put("amount",amount);put("date",now())});readableDatabase.rawQuery("SELECT amount,paid FROM schedules WHERE id=?",arrayOf(scheduleId.toString())).use{if(it.moveToFirst()){val p=it.getDouble(1)+amount;val st=if(p+0.005>=it.getDouble(0)) "PAID" else "DUE";writableDatabase.update("schedules",ContentValues().apply{put("paid",p);put("status",st)},"id=?",arrayOf(scheduleId.toString())) } }}
    fun recordRepaymentWithConsent(scheduleId:Long,creditId:Long,amount:Double,proposerRole:String,token:String){
        require(amount>0){"Invalid repayment amount"}
        require(token.isNotBlank()){"Consent token missing"}
        require(proposerRole=="LENDER"||proposerRole=="BORROWER"){"Invalid proposer role"}
        val db=writableDatabase
        db.beginTransaction()
        try{
            db.rawQuery("SELECT amount,paid FROM schedules WHERE id=? AND credit_id=?",arrayOf(scheduleId.toString(),creditId.toString())).use{
                require(it.moveToFirst()){"Repayment schedule not found"}
                val scheduled=it.getDouble(0);val paid=it.getDouble(1);val outstanding=(scheduled-paid).coerceAtLeast(0.0)
                require(amount<=outstanding+0.005){"Repayment exceeds outstanding amount"}
                db.insertOrThrow("repayment_consents",null,ContentValues().apply{put("credit_id",creditId);put("schedule_id",scheduleId);put("amount",amount);put("proposer_role",proposerRole);put("consent_token",consentHash(token));put("status","CONSENTED");put("created",now());put("confirmed",now())})
                db.insertOrThrow("payments",null,ContentValues().apply{put("credit_id",creditId);put("schedule_id",scheduleId);put("amount",amount);put("date",now())})
                val newPaid=paid+amount
                val st=if(newPaid+0.005>=scheduled)"PAID" else "DUE"
                db.update("schedules",ContentValues().apply{put("paid",newPaid.coerceAtMost(scheduled));put("status",st)},"id=?",arrayOf(scheduleId.toString()))
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun total(direction:String):Double=readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM credits WHERE direction=?",arrayOf(direction)).use{it.moveToFirst();it.getDouble(0)}
    fun dueCount(overdue:Boolean):Int=schedules(null,overdue).size
    fun saveBank(type:String,id:String,holder:String,bank:String,account:String,ifsc:String,upi:String,nach:Boolean){writableDatabase.insert("banks",null,ContentValues().apply{put("owner_type",type);put("owner_id",id);put("holder",holder);put("bank",bank);put("account",account);put("ifsc",ifsc);put("upi",upi);put("nach",if(nach)1 else 0);put("created",now())})}
}
