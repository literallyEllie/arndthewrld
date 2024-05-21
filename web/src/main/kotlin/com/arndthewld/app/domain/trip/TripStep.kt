package com.arndthewld.app.domain.trip

import kotlinx.datetime.LocalDateTime

interface TripStep<Details : StepDetails> {
    val start: LocalDateTime
    val end: LocalDateTime
    val startLocation: Location?
    val details: Details
}

interface StepDetails {
    val bookingRef: String?
    val payment: Payment?
    val notes: String?
}

data class Payment(
    val cost: Double, val currency: String, val paid: Boolean = true
)