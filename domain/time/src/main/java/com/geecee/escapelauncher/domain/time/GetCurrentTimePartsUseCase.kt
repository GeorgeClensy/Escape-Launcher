package com.geecee.escapelauncher.domain.time

import java.time.LocalTime
import javax.inject.Inject

class GetCurrentTimePartsUseCase @Inject constructor() {
    fun invoke(twelveHourDisplay: Boolean): Triple<Int, Int, Boolean> {
        val now = LocalTime.now()
        val isPm = now.hour >= 12
        val hour = if (twelveHourDisplay) {
            val h = now.hour % 12
            if (h == 0) 12 else h
        } else {
            now.hour
        }
        return Triple(hour, now.minute, isPm)
    }
}