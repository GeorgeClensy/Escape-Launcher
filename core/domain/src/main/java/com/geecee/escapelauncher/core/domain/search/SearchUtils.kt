package com.geecee.escapelauncher.core.domain.search

import com.geecee.escapelauncher.core.model.InstalledApp
import java.text.Normalizer

internal fun fuzzyMatch(text: String, pattern: String): Boolean {
    // Case-insensitive contains check
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

    return patternIndex == normalizedPattern.length
}

internal fun sortAppsByRelevance(apps: List<InstalledApp>, query: String): List<InstalledApp> {
    val regexUnaccent = "\\p{M}+"
    val normalizedQuery = Normalizer.normalize(query, Normalizer.Form.NFD)
        .replace(Regex(regexUnaccent), "")
        .lowercase()

    return apps.sortedWith(compareBy<InstalledApp> { app ->
        val normalizedName = Normalizer.normalize(app.displayName, Normalizer.Form.NFD)
            .replace(Regex(regexUnaccent), "")
            .lowercase()

        when {
            normalizedName.startsWith(normalizedQuery) -> 0
            normalizedName.contains(normalizedQuery) -> 1
            else -> 2
        }
    }.thenBy { it.displayName.lowercase() })
}
