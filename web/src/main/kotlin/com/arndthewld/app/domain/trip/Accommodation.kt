package com.arndthewld.app.domain.trip

import kotlinx.datetime.LocalDateTime

data class Accommodation(
    override val start: LocalDateTime,
    override val end: LocalDateTime,
    override val startLocation: Location?,
    override val details: AccommodationDetails
) : TripStep<AccommodationDetails>

data class AccommodationDetails(
    val type: Type,
    override val bookingRef: String? = null,
    override val payment: Payment? = null,
    override val notes: String? = null
) : StepDetails {

    enum class Type {
        HOSTEL, HOTEL, HOMESTAY
    }
}