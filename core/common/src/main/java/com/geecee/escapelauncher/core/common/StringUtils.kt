package com.geecee.escapelauncher.core.common

import java.util.concurrent.TimeUnit

/**
 * Formats screen time into string in the style of 5h 3m
 *
 * @param milliseconds The amount of time to return formatted
 *
 * @author George Clensy
 *
 * @return Returns a string that looks like this: 5h 3m
 */
fun formatScreenTime(milliseconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}