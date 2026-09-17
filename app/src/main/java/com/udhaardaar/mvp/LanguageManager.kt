package com.udhaardaar.mvp

import android.content.Context
import java.util.Locale

object LanguageManager {
    private const val PREFS="udhaardaar_accounts"; private const val KEY="preferred_language"
    const val EN="en"; const val HI="hi"
    fun get(c:Context)=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(KEY,EN)?:EN
    fun set(c:Context,l:String){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(KEY,l).apply()}
    fun isHindi(c:Context)=get(c)==HI
    fun locale(c:Context)=if(isHindi(c)) Locale("hi","IN") else Locale.ENGLISH
    fun t(c:Context,key:String)=if(isHindi(c))HI_TEXT[key]?:EN_TEXT[key]?:key else EN_TEXT[key]?:key
    private val EN_TEXT=mapOf("app" to "ARTHSAATHI","tagline" to "Navigate Your Financial Journey","pillars" to "Plan • Protect • Grow • Nominate","home" to "Home","credit" to "Credit","vault" to "Vault","repay" to "Repayments","services" to "Services","profile" to "Profile","good_morning" to "Good Morning","good_afternoon" to "Good Afternoon","good_evening" to "Good Evening","good_night" to "Good Night","overview" to "Your financial picture at a glance","to_receive" to "TO RECEIVE","to_pay" to "TO PAY","assets" to "ASSETS","liabilities" to "LIABILITIES","register" to "Register Credit","repay_title" to "Repayment Centre","vault_title" to "Asset & Liability Vault","insurance" to "Insurance & Benefits","legacy" to "Will & Legacy","legal" to "Legal Assistance","ttmm" to "TTMM Group Expenses","ai" to "AI Financial Advisor","search" to "Search / Create Profile","language" to "Language","logout" to "Log Out","quick_actions" to "Quick Access","more" to "More")
    private val HI_TEXT=mapOf("app" to "अर्थसाथी","tagline" to "आपकी वित्तीय यात्रा का साथी","pillars" to "योजना • सुरक्षा • विकास • नामांकन","home" to "होम","credit" to "उधार","vault" to "संपत्ति","repay" to "भुगतान","services" to "सेवाएँ","profile" to "प्रोफ़ाइल","good_morning" to "सुप्रभात","good_afternoon" to "नमस्कार","good_evening" to "शुभ संध्या","good_night" to "शुभ रात्रि","overview" to "आपकी वित्तीय स्थिति एक नज़र में","to_receive" to "लेना है","to_pay" to "देना है","assets" to "संपत्ति","liabilities" to "देयताएँ","register" to "उधार दर्ज करें","repay_title" to "भुगतान केन्द्र","vault_title" to "संपत्ति और देयता वॉल्ट","insurance" to "बीमा और लाभ","legacy" to "वसीयत और विरासत","legal" to "कानूनी सहायता","ttmm" to "TTMM समूह खर्च","ai" to "AI वित्तीय सलाहकार","search" to "प्रोफ़ाइल खोजें / बनाएँ","language" to "भाषा","logout" to "लॉग आउट","quick_actions" to "त्वरित कार्य","more" to "और")
}
