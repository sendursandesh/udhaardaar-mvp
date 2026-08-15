package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toReceive = findViewById<TextView>(R.id.tvToReceive)
        val toPay = findViewById<TextView>(R.id.tvToPay)
        val activeRecords = findViewById<TextView>(R.id.tvActiveRecords)

        val addUdhaar = findViewById<Button>(R.id.btnAddUdhaar)
        val viewRecords = findViewById<Button>(R.id.btnViewRecords)

        addUdhaar.setOnClickListener {
            val intent = Intent(this, AddUdhaarActivity::class.java)
            startActivity(intent)
        }

        viewRecords.setOnClickListener {
            activeRecords.text = "Active Records: 0"
        }
    }
}
