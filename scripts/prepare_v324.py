from pathlib import Path

p = Path('app/src/main/java/com/udhaardaar/mvp/V323Activity.kt')
s = p.read_text(encoding='utf-8')

old = 'if (db.hasUser() && prefs.getBoolean("logged_in", false)) dashboard() else ownerRegistration()'
new = 'if (db.hasUser() && prefs.getBoolean("logged_in", false)) dashboard() else loginScreen()'
if old not in s:
    raise SystemExit('Expected V3 launch gate not found')
s = s.replace(old, new, 1)

marker = '    private fun ownerRegistration() {'
if 'private fun loginScreen()' not in s:
    login = '''    private fun loginScreen() {
        history.clear()
        val r = root()
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(16), dp(18), dp(16), dp(18))
            background = bg(Color.WHITE, Color.rgb(185, 218, 235), 26)
        }
        hero.addView(logo(92))
        hero.addView(label("Udhaardaar", 30f, navy).apply { gravity = Gravity.CENTER })
        hero.addView(label("Your Credit. Your Trust. Our Record.", 14f, teal).apply { gravity = Gravity.CENTER })
        hero.addView(label("SECURE LOGIN", 18f, blue).apply { gravity = Gravity.CENTER })
        r.addView(hero)
        r.addView(gap(14))
        val mobile = field("Mobile number * (10 digits)", 10)
        val otpBox = field("Enter 6-digit OTP", 6).apply { visibility = View.GONE }
        var generatedOtp = ""
        r.addView(label("Login with your registered mobile number", 19f, navy))
        r.addView(label("Only your own authorised account should be accessed. Third-party credit information remains protected until consent is recorded.", 12f, Color.rgb(70,90,105)))
        r.addView(mobile, LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(0, dp(8), 0, dp(4)) })
        r.addView(otpBox, LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(0, dp(4), 0, dp(4)) })
        r.addView(button("SEND LOGIN OTP", blue) {
            val m = mobile.text.toString().trim()
            if (!validMobile(m)) { toast("Enter exactly 10 digits"); return@button }
            val user = db.userData()
            if (user == null || user.mobile != m) { toast("No registered profile found. Please create your profile."); return@button }
            generatedOtp = Random.nextInt(100000,1000000).toString()
            otpBox.visibility = View.VISIBLE
            toast("Demo OTP: $generatedOtp")
        })
        r.addView(button("VERIFY OTP + LOGIN", green) {
            if (generatedOtp.isBlank() || otpBox.text.toString().trim() != generatedOtp) { toast("Incorrect or expired OTP"); return@button }
            prefs.edit().putBoolean("logged_in", true).apply()
            dashboard()
        })
        r.addView(gap(8))
        r.addView(button("NEW USER — CREATE PROFILE", teal) { ownerRegistration() })
        r.addView(gap(5))
        r.addView(label("Privacy-first • Consent-controlled disclosure • Digital records", 11f, Color.rgb(70,90,105)).apply { gravity = Gravity.CENTER })
        show(r, false)
    }

'''
    if marker not in s:
        raise SystemExit('Owner registration marker not found')
    s = s.replace(marker, login + marker, 1)

if 'private fun validPan' not in s:
    marker2 = 'private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()'
    helpers = '''private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    private fun validPan(v:String)=Regex("[A-Z]{5}[0-9]{4}[A-Z]").matches(v.trim().uppercase(Locale.US))
    private fun validGstin(v:String)=Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]").matches(v.trim().uppercase(Locale.US))
    private fun validEmail(v:String)=v.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(v.trim()).matches()
    private fun validMobile(v:String)=Regex("[0-9]{10}").matches(v)
    private fun validAadhaar(v:String)=v.isBlank() || Regex("[0-9]{12}").matches(v)
    private fun validPin(v:String)=v.isBlank() || Regex("[0-9]{6}").matches(v)'''
    if marker2 in s: s=s.replace(marker2, helpers, 1)

p.write_text(s, encoding='utf-8')
print('Prepared V3.2.4 launch candidate')
