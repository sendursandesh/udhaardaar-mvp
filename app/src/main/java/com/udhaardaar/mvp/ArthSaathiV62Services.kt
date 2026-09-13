package com.udhaardaar.mvp

import android.content.Context
import android.location.Geocoder
import android.location.Location
import java.util.Locale
import kotlin.math.max

/** Address assistance: GPS is a suggestion; user confirmation remains authoritative. */
object V62AddressService {
    fun fromLocation(context: Context, location: Location): ArthSaathiV62Domain.Address {
        var line1 = ""
        var locality = ""
        var city = ""
        var district = ""
        var state = ""
        var pin = ""
        runCatching {
            @Suppress("DEPRECATION")
            val result = Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            if (result != null) {
                line1 = listOfNotNull(result.subThoroughfare, result.thoroughfare).joinToString(" ").trim()
                locality = result.subLocality.orEmpty()
                city = result.locality ?: result.subAdminArea.orEmpty()
                district = result.subAdminArea.orEmpty()
                state = result.adminArea.orEmpty()
                pin = result.postalCode.orEmpty()
            }
        }
        return ArthSaathiV62Domain.Address(line1, locality = locality, city = city, district = district,
            state = state, pinCode = pin, latitude = location.latitude, longitude = location.longitude,
            source = "LOCATION_SUGGESTION", userConfirmed = false)
    }

    fun mergePinSuggestion(current: ArthSaathiV62Domain.Address, city: String, district: String, state: String, postOffice: String = "") =
        current.copy(city = city, district = district, state = state,
            locality = if (postOffice.isBlank()) current.locality else postOffice,
            source = "PIN_LOOKUP", userConfirmed = false)
}

/** Pure QR relationship policy. Financial history is never exposed by scanning alone. */
object V62QrKhataService {
    fun createRelationshipDraft(ownerId: String, counterparty: ArthSaathiV62Domain.PersonRef): ArthSaathiV62Domain.Relationship =
        ArthSaathiV62Domain.Relationship(
            id = "REL-${System.currentTimeMillis()}", ownerId = ownerId,
            counterparty = counterparty, type = ArthSaathiV62Domain.RelationshipType.PERSONAL_CREDIT,
            status = "DRAFT"
        )

    fun canShowHistory(consent: ArthSaathiV62Domain.ConsentRecord): Boolean =
        consent.eventType == "HISTORY_SHARING" && consent.granted && consent.otpVerified

    fun canRequestFunding(consent: ArthSaathiV62Domain.ConsentRecord): Boolean =
        consent.eventType == "FUNDING_PROFILE_SHARING" && consent.granted && consent.otpVerified
}

/** Transparent cost comparison for formal-credit offers. */
object V62ChargeCheckService {
    fun effectiveCost(offer: ArthSaathiV62Domain.FormalCreditOffer): Double =
        max(0.0, offer.totalCost - offer.principal)

    fun compare(offers: List<ArthSaathiV62Domain.FormalCreditOffer>): List<ArthSaathiV62Domain.FormalCreditOffer> =
        offers.sortedWith(compareBy<ArthSaathiV62Domain.FormalCreditOffer> { it.totalCost }.thenBy { it.effectiveCostPercent })

    fun variance(result: ArthSaathiV62Domain.ChargeCheckResult): Double =
        result.actualInterest + result.processingFee + result.documentationFee + result.insuranceCharge +
            result.taxes + result.penalties + result.otherCharges - result.refunds - result.sanctionedInterest
}

/** Funding marketplace policy; actual provider onboarding/API is a later adapter boundary. */
object V62FundingService {
    fun eligibleForSharing(request: ArthSaathiV62Domain.FundingRequest, consent: ArthSaathiV62Domain.ConsentRecord): Boolean =
        request.consentGranted && consent.eventType == "FUNDING_PROFILE_SHARING" && consent.granted && consent.otpVerified
}
}
