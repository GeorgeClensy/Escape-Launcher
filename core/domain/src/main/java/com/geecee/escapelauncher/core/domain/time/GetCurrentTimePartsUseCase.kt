package com.geecee.escapelauncher.core.domain.time

import jakarta.inject.Inject
import java.time.LocalTime

class GetCurrentTimePartsUseCase @Inject constructor() {
    fun invoke(time: LocalTime, twelveHourDisplay: Boolean): Triple<Int, Int, Boolean> {
        val isPm = time.hour >= 12
        val hour = if (twelveHourDisplay) {
            val h = time.hour % 12
            if (h == 0) 12 else h
        } else {
            time.hour
        }
        return Triple(hour, time.minute, isPm)
    }
}