package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class V3DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "udhaardaar_v3.db", null, 8) {
    override fun onConfigure(db: SQLiteDatabase) { super.onConfigure(db); db.setForeignKeyConstraintsEnabled(true) }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE parties(id INTEGER PRIMARY KEY AUTOINCREMENT,party_type TEXT NOT NULL DEFAULT 'PERSON',name TEXT NOT NULL,mobile TEXT,email TEXT,address TEXT,pincode TEXT,city TEXT,state TEXT,aadhaar TEXT,pan TEXT,gstin TEXT,business_name TEXT,photo_uri TEXT,notes TEXT,created_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE credits(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_type TEXT NOT NULL,direction TEXT NOT NULL DEFAULT 'GIVEN',principal_amount REAL NOT NULL,interest_rate REAL NOT NULL DEFAULT 0,repayment_method TEXT NOT NULL,repayment_amount REAL NOT NULL DEFAULT 0,periodicity TEXT,start_date TEXT NOT NULL,end_date TEXT,next_due_date TEXT,grace_days INTEGER NOT NULL DEFAULT 0,lending_method TEXT DEFAULT 'OTHER',transaction_reference TEXT,invoice_number TEXT,invoice_uri TEXT,status TEXT NOT NULL DEFAULT 'ACTIVE',notes TEXT,created_at TEXT NOT NULL,updated_at TEXT NOT NULL)")
        db.execSQL("CREATE TABLE credit_parties(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,party_id INTEGER NOT NULL,role TEXT NOT NULL,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(party_id) REFERENCES parties(id) ON DELETE RESTRICT,UNIQUE(credit_id,party_id,role))")
        db.execSQL("CREATE TABLE repayments(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,repayment_date TEXT NOT NULL,amount REAL NOT NULL,principal_component REAL NOT NULL DEFAULT 0,interest_component REAL NOT NULL DEFAULT 0,payment_mode TEXT,reference_number TEXT,notes TEXT,created_at TEXT NOT NULL,actor_party_id INTEGER NOT NULL,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(actor_party_id) REFERENCES parties(id) ON DELETE RESTRICT)")
        db.execSQL("CREATE TABLE guarantors(id INTEGER PRIMARY KEY AUTOINCREMENT,party_id INTEGER NOT NULL,relationship TEXT,consent_status TEXT NOT NULL DEFAULT 'PENDING',created_at TEXT NOT NULL,FOREIGN KEY(party_id) REFERENCES parties(id) ON DELETE RESTRICT)")
        db.execSQL("CREATE TABLE credit_guarantors(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,guarantor_id INTEGER NOT NULL,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(guarantor_id) REFERENCES guarantors(id) ON DELETE RESTRICT,UNIQUE(credit_id,guarantor_id))")
        db.execSQL("CREATE TABLE documents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER,party_id INTEGER,document_type TEXT NOT NULL,document_name TEXT,document_uri TEXT,verification_status TEXT NOT NULL DEFAULT 'PENDING',created_at TEXT NOT NULL,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(party_id) REFERENCES parties(id) ON DELETE RESTRICT)")
        db.execSQL("CREATE TABLE consents(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,party_id INTEGER NOT NULL,consent_type TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'PENDING',otp_verified INTEGER NOT NULL DEFAULT 0,consented_at TEXT,created_at TEXT NOT NULL,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(party_id) REFERENCES parties(id) ON DELETE RESTRICT)")
        db.execSQL("CREATE TABLE credit_access(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,party_id INTEGER NOT NULL,role TEXT NOT NULL,can_view INTEGER NOT NULL DEFAULT 0,can_repay INTEGER NOT NULL DEFAULT 0,can_edit INTEGER NOT NULL DEFAULT 0,consent_required INTEGER NOT NULL DEFAULT 1,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(party_id) REFERENCES parties(id) ON DELETE RESTRICT,UNIQUE(credit_id,party_id,role))")
        db.execSQL("CREATE TABLE activity_log(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER,party_id INTEGER,activity_type TEXT NOT NULL,description TEXT,created_at TEXT NOT NULL,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(party_id) REFERENCES parties(id) ON DELETE RESTRICT)")
        createIndexes(db)
    }
    private fun createIndexes(db: SQLiteDatabase) { listOf("CREATE INDEX IF NOT EXISTS idx_parties_mobile ON parties(mobile)","CREATE INDEX IF NOT EXISTS idx_parties_pan ON parties(pan)","CREATE INDEX IF NOT EXISTS idx_parties_gstin ON parties(gstin)","CREATE INDEX IF NOT EXISTS idx_parties_aadhaar ON parties(aadhaar)","CREATE INDEX IF NOT EXISTS idx_credit_parties_credit ON credit_parties(credit_id)","CREATE INDEX IF NOT EXISTS idx_credit_parties_party ON credit_parties(party_id)","CREATE INDEX IF NOT EXISTS idx_repayments_credit ON repayments(credit_id)","CREATE INDEX IF NOT EXISTS idx_access_credit_party ON credit_access(credit_id,party_id)") .forEach(db::execSQL) }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS credit_parties(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,party_id INTEGER NOT NULL,role TEXT NOT NULL,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(party_id) REFERENCES parties(id) ON DELETE RESTRICT,UNIQUE(credit_id,party_id,role))")
            if (hasColumn(db,"credits","party_id")) db.rawQuery("SELECT id,party_id,direction FROM credits WHERE party_id IS NOT NULL",null).use{c->while(c.moveToNext()){val role=if(c.getString(2)=="GIVEN")"BORROWER" else "LENDER";db.execSQL("INSERT OR IGNORE INTO credit_parties(credit_id,party_id,role) VALUES(?,?,?)",arrayOf(c.getLong(0),c.getLong(1),role))}}
        }
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS credit_access(id INTEGER PRIMARY KEY AUTOINCREMENT,credit_id INTEGER NOT NULL,party_id INTEGER NOT NULL,role TEXT NOT NULL,can_view INTEGER NOT NULL DEFAULT 0,can_repay INTEGER NOT NULL DEFAULT 0,can_edit INTEGER NOT NULL DEFAULT 0,consent_required INTEGER NOT NULL DEFAULT 1,FOREIGN KEY(credit_id) REFERENCES credits(id) ON DELETE CASCADE,FOREIGN KEY(party_id) REFERENCES parties(id) ON DELETE RESTRICT,UNIQUE(credit_id,party_id,role))")
            db.rawQuery("SELECT credit_id,party_id,role FROM credit_parties",null).use{c->while(c.moveToNext()){val role=c.getString(2);db.execSQL("INSERT OR IGNORE INTO credit_access(credit_id,party_id,role,can_view,can_repay,can_edit,consent_required) VALUES(?,?,?,?,?,?,?)",arrayOf(c.getLong(0),c.getLong(1),role,1,1,0,1))}}
        }
        if (oldVersion < 8) {
            if (!hasColumn(db,"credits","lending_method")) db.execSQL("ALTER TABLE credits ADD COLUMN lending_method TEXT DEFAULT 'OTHER'")
            if (!hasColumn(db,"credits","transaction_reference")) db.execSQL("ALTER TABLE credits ADD COLUMN transaction_reference TEXT")
        }
        createIndexes(db)
    }
    private fun hasColumn(db:SQLiteDatabase,table:String,column:String):Boolean=db.rawQuery("PRAGMA table_info($table)",null).use{c->while(c.moveToNext())if(c.getString(1)==column)return true;false}

    fun addCredit(borrower:Long,guarantor:Long?,type:String,direction:String,principal:Double,roi:Double,tenor:Int,method:String,installment:Double,interest:Double,payable:Double,start:String,end:String,invoiceRef:String,invoiceUri:String?,verified:Boolean,lendingMethod:String="OTHER",transactionReference:String?=null):Long {
        require(profileExists(borrower,"BORROWER")) { "Invalid borrower profile" }
        if(guarantor!=null) require(profileExists(guarantor,"GUARANTOR")) { "Invalid guarantor profile" }
        require(principal>0.0&&tenor in 1..240&&payable>=principal) { "Invalid credit terms" }
        val now=now(); val db=writableDatabase
        val id=db.insertOrThrow("credits",null,ContentValues().apply{put("credit_type",type);put("direction",direction);put("principal_amount",principal);put("interest_rate",roi);put("repayment_method",method);put("repayment_amount",installment);put("periodicity","MONTHLY");put("start_date",start);put("end_date",end);put("next_due_date",end);put("grace_days",0);put("lending_method",lendingMethod);put("transaction_reference",transactionReference);put("invoice_number",invoiceRef);put("invoice_uri",invoiceUri);put("status","ACTIVE");put("notes","formal/ informal credit registration");put("created_at",now);put("updated_at",now)})
        db.insertOrThrow("credit_parties",null,ContentValues().apply{put("credit_id",id);put("party_id",borrower);put("role",if(direction=="GIVEN")"BORROWER" else "LENDER")})
        db.insertOrThrow("credit_access",null,ContentValues().apply{put("credit_id",id);put("party_id",borrower);put("role",if(direction=="GIVEN")"BORROWER" else "LENDER");put("can_view",1);put("can_repay",1);put("can_edit",0);put("consent_required",1)})
        db.insertOrThrow("activity_log",null,ContentValues().apply{put("credit_id",id);put("party_id",borrower);put("activity_type","CREDIT_CREATED");put("description","Credit registered");put("created_at",now)})
        return id
    }
    private fun profileExists(id:Long,role:String)=readableDatabase.rawQuery("SELECT 1 FROM parties WHERE id=? LIMIT 1",arrayOf(id.toString())).use{it.moveToFirst()} || readableDatabase.rawQuery("SELECT 1 FROM parties WHERE id=? LIMIT 1",arrayOf(id.toString())).use{it.moveToFirst()}
    fun canView(creditId:Long,actorPartyId:Long,consentVerified:Boolean)=authorised(creditId,actorPartyId,"can_view",consentVerified)
    fun canUpdateRepayment(creditId:Long,actorPartyId:Long,consentVerified:Boolean)=authorised(creditId,actorPartyId,"can_repay",consentVerified)
    private fun authorised(creditId:Long,partyId:Long,column:String,consentVerified:Boolean)=readableDatabase.rawQuery("SELECT $column,consent_required FROM credit_access WHERE credit_id=? AND party_id=? LIMIT 1",arrayOf(creditId.toString(),partyId.toString())).use{it.moveToFirst()&&it.getInt(0)==1&&(it.getInt(1)==0||consentVerified)}
    fun addRepaymentAuthorized(creditId:Long,actorPartyId:Long,repaymentDate:String,amount:Double,principalComponent:Double,interestComponent:Double,paymentMode:String?,referenceNumber:String?,notes:String?,createdAt:String,consentVerified:Boolean):Long{if(amount<=0||!canUpdateRepayment(creditId,actorPartyId,consentVerified))return -1L;val db=writableDatabase;db.beginTransaction();return try{val id=db.insertOrThrow("repayments",null,ContentValues().apply{put("credit_id",creditId);put("repayment_date",repaymentDate);put("amount",amount);put("principal_component",principalComponent.coerceAtLeast(0.0));put("interest_component",interestComponent.coerceAtLeast(0.0));put("payment_mode",paymentMode);put("reference_number",referenceNumber);put("notes",notes);put("created_at",createdAt);put("actor_party_id",actorPartyId)});db.insertOrThrow("activity_log",null,ContentValues().apply{put("credit_id",creditId);put("party_id",actorPartyId);put("activity_type","REPAYMENT_RECORDED");put("description","Authorised manual repayment recorded");put("created_at",createdAt)});db.setTransactionSuccessful();id}finally{db.endTransaction()}}
    fun upcomingCredits():List<Pair<Long,String>>{val out=mutableListOf<Pair<Long,String>>();readableDatabase.rawQuery("SELECT id,next_due_date FROM credits WHERE status='ACTIVE' AND next_due_date IS NOT NULL",null).use{c->while(c.moveToNext())out.add(c.getLong(0) to c.getString(1))};return out}
    private fun now():String=java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",java.util.Locale.US).format(java.util.Date())
}
