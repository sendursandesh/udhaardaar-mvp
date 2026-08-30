package com.udhaardaar.mvp

/** Central navigation contract for V5. Every feature gets a stable route rather than ad-hoc page jumps. */
object V5Navigation {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PROFILES = "profiles"
    const val INFORMAL_CREDIT = "credit/informal"
    const val FORMAL_CREDIT = "credit/formal"
    const val TRADE_CREDIT = "credit/trade"
    const val RENTAL = "rental"
    const val REPAYMENT = "repayment"
    const val DPN = "documents/dpn"
    const val GUARANTEE = "documents/guarantee"
    const val DOCUMENTS = "documents"
    const val FINANCIAL_ASSETS = "vault/assets/financial"
    const val NON_FINANCIAL_ASSETS = "vault/assets/non-financial"
    const val NOMINEES = "vault/nominees"
    const val INHERITANCE = "vault/inheritance"
    const val CLAIMS = "legal/claims"
    const val LEGAL_ASSISTANCE = "legal/assistance"
    const val CHARGE_AUDIT = "audit/charges"
    const val NOTIFICATIONS = "notifications"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
}
