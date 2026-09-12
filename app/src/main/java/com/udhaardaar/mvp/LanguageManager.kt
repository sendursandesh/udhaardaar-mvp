package com.udhaardaar.mvp

import android.content.Context
import java.util.Locale

object LanguageManager {
    private const val PREFS = "udhaardaar_accounts"
    private const val KEY = "preferred_language"
    const val EN = "en"
    const val HI = "hi"
    fun get(context: Context): String = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, EN) ?: EN
    fun set(context: Context, language: String) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, language).apply() }
    fun isHindi(context: Context) = get(context) == HI
    fun locale(context: Context): Locale = if (isHindi(context)) Locale("hi", "IN") else Locale.ENGLISH
    fun t(context: Context, key: String): String = if (isHindi(context)) HI_TEXT[key] ?: EN_TEXT[key] ?: key else EN_TEXT[key] ?: key
    private val EN_TEXT = mapOf(
        "app" to "ARTHSAATHI", "tagline" to "Your Asset. Your Record. Your Right.",
        "home" to "Home", "credit" to "Credit", "vault" to "Vault", "repay" to "Repayments", "services" to "Services", "profile" to "Profile",
        "good_morning" to "Good Morning", "good_afternoon" to "Good Afternoon", "good_evening" to "Good Evening", "good_night" to "Good Night",
        "overview" to "Your financial picture at a glance", "to_receive" to "TO RECEIVE", "to_pay" to "TO PAY", "assets" to "ASSETS", "liabilities" to "LIABILITIES",
        "active_credit" to "Active credit records", "register" to "Register Credit", "register_sub" to "Record a new loan or informal credit",
        "repay_title" to "Repayment Centre", "repay_sub" to "View dues, repayments and consent status",
        "vault_title" to "Asset & Liability Vault", "vault_sub" to "Keep assets, liabilities and evidence organised",
        "insurance" to "Insurance & Benefits", "insurance_sub" to "Policies, premiums and government benefits",
        "legacy" to "Will & Legacy", "legacy_sub" to "Plan nominees and prepare your Will",
        "legal" to "Legal Assistance", "legal_sub" to "Find lawyers by domain and expertise",
        "ttmm" to "TTMM Group Expenses", "ttmm_sub" to "Split expenses and settle balances",
        "ai" to "AI Financial Advisor", "ai_sub" to "Understand alerts, dues and opportunities",
        "search" to "Search / Create Profile", "search_sub" to "Find a registered person or business",
        "language" to "Language", "choose_language" to "Choose your preferred language",
        "english" to "English", "hindi" to "हिन्दी", "save" to "SAVE", "cancel" to "CANCEL", "settings" to "Settings",
        "logout" to "Log Out", "help" to "Simple guides and help", "rural" to "Designed for everyone — including first-time and rural users",
        "profile_photo" to "Tap to add your profile photo", "quick_actions" to "Quick Actions", "financial_tools" to "Financial Tools", "support" to "Support & Planning",
        "language_saved" to "Language updated", "selected" to "Selected", "more" to "More"
    )
    private val HI_TEXT = mapOf(
        "app" to "अर्थसाथी", "tagline" to "आपकी संपत्ति • आपका रिकॉर्ड • आपका अधिकार",
        "home" to "होम", "credit" to "उधार", "vault" to "संपत्ति", "repay" to "भुगतान", "services" to "सेवाएँ", "profile" to "प्रोफ़ाइल",
        "good_morning" to "सुप्रभात", "good_afternoon" to "नमस्कार", "good_evening" to "शुभ संध्या", "good_night" to "शुभ रात्रि",
        "overview" to "आपकी वित्तीय स्थिति एक नज़र में", "to_receive" to "लेना है", "to_pay" to "देना है", "assets" to "संपत्ति", "liabilities" to "देयताएँ",
        "active_credit" to "सक्रिय उधार रिकॉर्ड", "register" to "उधार दर्ज करें", "register_sub" to "नया ऋण या उधार रिकॉर्ड करें",
        "repay_title" to "भुगतान केन्द्र", "repay_sub" to "बकाया, भुगतान और सहमति देखें",
        "vault_title" to "संपत्ति और देयता वॉल्ट", "vault_sub" to "संपत्ति, देयता और दस्तावेज़ व्यवस्थित रखें",
        "insurance" to "बीमा और लाभ", "insurance_sub" to "पॉलिसी, प्रीमियम और सरकारी लाभ",
        "legacy" to "वसीयत और विरासत", "legacy_sub" to "नामित व्यक्ति तय करें और वसीयत तैयार करें",
        "legal" to "कानूनी सहायता", "legal_sub" to "क्षेत्र और विशेषज्ञता के अनुसार वकील खोजें",
        "ttmm" to "TTMM समूह खर्च", "ttmm_sub" to "खर्च बाँटें और हिसाब पूरा करें",
        "ai" to "AI वित्तीय सलाहकार", "ai_sub" to "अलर्ट, बकाया और अवसर समझें",
        "search" to "प्रोफ़ाइल खोजें / बनाएँ", "search_sub" to "पंजीकृत व्यक्ति या व्यवसाय खोजें",
        "language" to "भाषा", "choose_language" to "अपनी पसंद की भाषा चुनें",
        "english" to "English", "hindi" to "हिन्दी", "save" to "सहेजें", "cancel" to "रद्द करें", "settings" to "सेटिंग्स",
        "logout" to "लॉग आउट", "help" to "सरल मार्गदर्शन और सहायता", "rural" to "हर व्यक्ति के लिए — पहली बार और ग्रामीण उपयोगकर्ताओं के लिए भी",
        "profile_photo" to "प्रोफ़ाइल फोटो जोड़ने के लिए टैप करें", "quick_actions" to "त्वरित कार्य", "financial_tools" to "वित्तीय सुविधाएँ", "support" to "सहायता और योजना",
        "language_saved" to "भाषा बदल दी गई", "selected" to "चयनित", "more" to "और"
    )
}
