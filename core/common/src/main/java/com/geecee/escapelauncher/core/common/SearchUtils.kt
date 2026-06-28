package com.geecee.escapelauncher.core.common

import java.text.Normalizer

fun fuzzyMatch(text: String, pattern: String): Boolean {
    // Case-insensitive contains check (original behavior)
    if (text.contains(pattern, ignoreCase = true)) {
        return true
    }

    val regexUnaccent = "\\p{M}+"
    val normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex(regexUnaccent), "")
        .lowercase()

    val normalizedPattern = Normalizer.normalize(pattern, Normalizer.Form.NFD)
        .replace(Regex(regexUnaccent), "")
        .lowercase()

    // Check for initials match (e.g., "gm" matches "Google Maps")
    if (pattern.length >= 2) {
        val words = normalizedText.split(" ")
        if (words.size > 1) {
            val initials = words.joinToString("") { it.firstOrNull()?.toString() ?: "" }
            if (initials.contains(normalizedPattern)) {
                return true
            }
        }
    }

    // Check for character sequence match with gaps
    var textIndex = 0
    var patternIndex = 0
    while (textIndex < normalizedText.length && patternIndex < normalizedPattern.length) {
        if (normalizedText[textIndex] == normalizedPattern[patternIndex]) {
            patternIndex++
        }
        textIndex++
    }

    // If we matched all characters in pattern, it's a fuzzy match
    return patternIndex == normalizedPattern.length
}