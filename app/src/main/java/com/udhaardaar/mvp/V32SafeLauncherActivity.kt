package com.udhaardaar.mvp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class V32SafeLauncherActivity : AppCompatActivity() {
    private lateinit var db: V32DatabaseHelper
    private var pendingOtp = ""
    private var selectedPhoto: Uri? = null
    private var photoView: ImageView? = null

    private fun edit(hint: String, numeric: Boolean = false) = EditText(this).apply {
        this.hint = hint
        setPadding(18, 12, 18, 12)
        if (numeric) inputType = InputType.TYPE_CLASS_NUMBER
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = V32DatabaseHelper(this)
        val c = db.user()
        val exists = c.moveToFirst()
        c.close()
        if (exists && getSharedPreferences("udhaardaar_v32_session", MODE_PRIVATE).getBoolean("logged_in", false)) {
            openApp()
        } else {
            showProfileScreen()
        }
    }

    private fun showProfileScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 30)
        }
        root.addView(TextView(this).apply {
            text = "UDHAARDAAR"
            textSize = 30f
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)
        })
        root.addView(TextView(this).apply {
            text = "V3.2.1 • Secure profile-first credit management"
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(8, 4, 8, 18)
        })
        root.addView(TextView(this).apply { text = "Create your Lender / Account Owner profile"; textSize = 20f; setPadding(8, 12, 8, 12) })

        val name = edit("Full name *")
        val mobile = edit("Mobile number *", true).apply {
            filters = arrayOf(InputFilter.LengthFilter(10))
        }
        val address = edit("Address")
        val email = edit("Email")
        val otp = edit("Enter 6-digit OTP", true).apply {
            filters = arrayOf(InputFilter.LengthFilter(6))
            visibility = android.view.View.GONE
        }

        root.addView(name)
        root.addView(mobile)
        root.addView(address)
        root.addView(email)

        val photo = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 180)
            setImageResource(android.R.drawable.ic_menu_camera)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        photoView = photo
        root.addView(photo)
        root.addView(button("Add profile photo (optional)") { pickPhoto() })

        val send = button("Create Profile + Send OTP") { }
        val verify = button("Verify OTP & Save Profile") { }
        verify.isEnabled = false

        send.setOnClickListener {
            if (name.text.toString().trim().isEmpty() || mobile.text.toString().trim().length != 10) {
                Toast.makeText(this, "Enter name and exactly 10-digit mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pendingOtp = Random.nextInt(100000, 1000000).toString()
            otp.visibility = android.view.View.VISIBLE
            send.isEnabled = false
            verify.isEnabled = true
            Toast.makeText(this, "OTP sent. Demo OTP: $pendingOtp", Toast.LENGTH_LONG).show()
        }

        verify.setOnClickListener {
            if (otp.text.toString() != pendingOtp || pendingOtp.isEmpty()) {
                otp.error = "Incorrect OTP"
                return@setOnClickListener
            }
            val userId = "USR-${System.currentTimeMillis()}"
            val row = db.saveUser(userId, name.text.toString().trim(), mobile.text.toString().trim(), address.text.toString().trim(), email.text.toString().trim(), selectedPhoto?.toString())
            if (row > 0) {
                getSharedPreferences("udhaardaar_v32_session", MODE_PRIVATE).edit().putBoolean("logged_in", true).apply()
                Toast.makeText(this, "Profile saved. User ID: $userId", Toast.LENGTH_LONG).show()
                openApp()
            } else {
                Toast.makeText(this, "Could not save profile", Toast.LENGTH_LONG).show()
            }
        }

        root.addView(send)
        root.addView(otp)
        root.addView(verify)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun pickPhoto() {
        startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }, 901)
    }

    @Deprecated("Compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 901 && resultCode == RESULT_OK) {
            selectedPhoto = data?.data
            photoView?.setImageURI(selectedPhoto)
        }
    }

    private fun openApp() {
        startActivity(Intent(this, V32Activity::class.java))
        finish()
    }
}
