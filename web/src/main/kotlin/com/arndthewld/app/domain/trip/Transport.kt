package com.arndthewld.app.domain.trip

import kotlinx.datetime.LocalDateTime

data class TransportStep(
    override val start: LocalDateTime, override val end: LocalDateTime,
    override val startLocation: Location?, val endLocation: Location,
    override val details: TransportDetails
) : TripStep<TransportDetails>

data class TransportDetails(
    val type: Type,
    val company: String? = null,
    val ticketUri: String? = null,
    val regNumber: String? = null,
    val transit: Boolean = false,
    override val bookingRef: String? = null,
    override val payment: Payment? = null,
    override val notes: String? = null
) : StepDetails {

    enum class Type {
        FLIGHT, TRAIN, BUS, COACH
    }
}
