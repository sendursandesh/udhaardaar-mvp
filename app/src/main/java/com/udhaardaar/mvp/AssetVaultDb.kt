package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AssetVaultDb(context: Context) : SQLiteOpenHelper(context, "udhaardaar_v4_assets.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE assets (id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT NOT NULL, name TEXT NOT NULL, institution TEXT, identifier TEXT, estimated_value REAL DEFAULT 0, nominee TEXT, nominee_relation TEXT, legal_heir TEXT, notes TEXT, document_uri TEXT, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE family_contacts (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, relation TEXT NOT NULL, mobile TEXT, email TEXT, notes TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun addAsset(category: String, name: String, institution: String, identifier: String, value: Double, nominee: String, nomineeRelation: String, legalHeir: String, notes: String, documentUri: String?): Long {
        val v = ContentValues().apply {
            put("category", category); put("name", name); put("institution", institution); put("identifier", identifier)
            put("estimated_value", value); put("nominee", nominee); put("nominee_relation", nomineeRelation); put("legal_heir", legalHeir)
            put("notes", notes); put("document_uri", documentUri); put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("assets", null, v)
    }

    fun addFamily(name: String, relation: String, mobile: String, email: String, notes: String): Long {
        val v = ContentValues().apply { put("name", name); put("relation", relation); put("mobile", mobile); put("email", email); put("notes", notes) }
        return writableDatabase.insert("family_contacts", null, v)
    }

    fun assetCount(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM assets", null).use { if (it.moveToFirst()) it.getInt(0) else 0 }
    fun totalValue(): Double = readableDatabase.rawQuery("SELECT COALESCE(SUM(estimated_value),0) FROM assets", null).use { if (it.moveToFirst()) it.getDouble(0) else 0.0 }
}
