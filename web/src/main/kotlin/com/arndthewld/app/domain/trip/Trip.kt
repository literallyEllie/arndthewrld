package com.arndthewld.app.domain.trip

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.time.Month

data class Trip(
    val userId: Long,
    val name: String,
    val steps: List<TripStep<*>>,
)

data class Location(
    val name: String, val country: String,
    val latitude: Double, val longitude: Double,
)

fun main() {

    val myTrip = Trip(userId = 1, name = "My Trip", steps = listOf(
        // bus -> T4
        TransportStep(
            start = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 7, hour = 9, minute = 0),
            end = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 7, hour = 11, minute = 5),
            startLocation = Location(
                "Bristol Bus Station", "GB", 51.459104767533695, -2.5924911568346034,
            ),
            endLocation = Location(
                "LHR (T4)", "GB", 51.4597700040113, -0.44740511808079125
            ),
            details = TransportDetails(
                TransportDetails.Type.COACH,
                company = "National Express", bookingRef = "PTGGY789",
                payment = Payment(21.4, "GBP")
            )
        ),
        // LHR AUH
        TransportStep(
            start = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 7, hour = 13, minute = 55), // GMT
            end = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 8, hour = 0, minute = 55), // GMT+4
            startLocation = null, // inherit
            endLocation = Location(
                "AUH", "AE",
                24.452328722168502, 54.64273881110521
            ),
            details = TransportDetails(
                TransportDetails.Type.FLIGHT, transit = true,
                company = "Etihad", bookingRef = "LMME7V", regNumber = "EY20"
            )
        ),
        // AUH BOM
        TransportStep(
            start = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 8, hour = 2, minute = 50), // GMT+4
            end = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 8, hour = 7, minute = 25), // GMT+5:30
            startLocation = null, // inherit
            endLocation = Location(
                "BOM", "IND",
                19.090887059513232, 72.86261558095491
            ),
            details = TransportDetails(
                TransportDetails.Type.FLIGHT,
                company = "Etihad", bookingRef = "LMME7V", regNumber = "EY196",
                payment = Payment(326.61, "GBP") // prev. flight being transit should be implicit
            )
        ),
        // Mumbai Backpackers
        Accommodation(
            start = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 8, hour = 11, minute = 0), // GMT+5:30
            end = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 13, hour = 11, minute = 0), // GMT+5:30
            startLocation = Location(
                "Namastey Mumbai Backpackers", "IND",
                19.063696645643013, 72.82983634973179
            ),
            details = AccommodationDetails(
                AccommodationDetails.Type.HOSTEL, bookingRef = "4018742562",
                payment = Payment(5_495.0, "RS", paid = false) // pay in person
            )
        ),
        // BOM MAA
        TransportStep(
            start = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 8, hour = 15, minute = 0), // GMT+5:30
            end = LocalDateTime(year = 2024, month = Month.JANUARY, dayOfMonth = 8, hour = 16, minute = 55), // GMT+5:30
            startLocation = Location(
                "BOM", "IND",
                19.090887059513232, 72.86261558095491
            ),
            endLocation = Location(
                "MAA", "IND",
                12.983093175894762, 80.16397890735038
            ),
            details = TransportDetails(
                TransportDetails.Type.FLIGHT,
                company = "IndiGO", bookingRef = "HTEZ9G", regNumber = "6E179",
                payment = Payment(3_366.0, "RS")
            )
        ),
    ))

    // yeah.
    println("Your trip: ${myTrip.name} (steps: ${myTrip.steps.size})")
    println()

    var lastDate: LocalDate? = null

    myTrip.steps.forEachIndexed { index, step ->
        val startLocation = if (step.startLocation != null || index == 0) {
            step.startLocation
        } else {
            val tripStep = myTrip.steps[index - 1]
            if (tripStep is TransportStep) {
                tripStep.endLocation
            } else {
                tripStep.startLocation
            }
        }

        if (step is TransportStep) {
            println("#$index ${step.start.string()} ${step.details.type} from ${startLocation?.name} to ${step.endLocation.name}")
        } else if (step is Accommodation) {
            println("#$index ${step.start.string()}-${step.end.string()} Stay at: ${step.startLocation?.name}")
        }

        step.details.payment?.also {  println("  Cost: ${it.string()}") }
        println()
    }
}

fun LocalDateTime.string(): String {
    if (this.hour == 0 && this.minute == 0) {
        return "${this.dayOfMonth}.${this.month.value}.${this.year} (${this.hour}:${this.minute})"
    }

    return "${this.dayOfMonth}.${this.month.value}.${this.year}"
}

fun Payment.string(): String {
    return "${this.cost} ${this.currency} (paid? ${this.paid})"
}