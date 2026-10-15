package com.udhaardaar.mvp

/**
 * Vendor-neutral accounting integration boundary.
 * SAP, Tally and future systems implement/are mapped to this contract; UI never
 * depends on a vendor-specific payload.
 */
interface V62AccountingAdapter {
    val provider: String
    fun isConfigured(): Boolean
    fun importTradeCredit(sourceAccountId: String, fromDate: String? = null, toDate: String? = null): List<ArthSaathiV62Domain.TradeCreditRecord>
}

class V62GenericAccountingMapper(override val provider: String) : V62AccountingAdapter {
    override fun isConfigured(): Boolean = false

    override fun importTradeCredit(sourceAccountId: String, fromDate: String?, toDate: String?): List<ArthSaathiV62Domain.TradeCreditRecord> = emptyList()
}

object V62AccountingIntegrationRegistry {
    val supportedAdapters = listOf("TALLY", "SAP", "GENERIC_REST_API", "CSV_IMPORT", "WEBHOOK")

    fun validateTradeCredit(record: ArthSaathiV62Domain.TradeCreditRecord): Boolean =
        record.invoiceNumber.isNotBlank() &&
            record.invoiceAmount >= 0.0 &&
            record.creditPeriodDays >= 0 &&
            record.sourceSystem.isNotBlank() &&
            record.sourceRecordId.isNotBlank()
}
