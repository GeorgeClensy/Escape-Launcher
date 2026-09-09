package com.geecee.escapelauncher.core.domain.search

import com.geecee.escapelauncher.core.model.InstalledApp
import java.text.Normalizer

// Compiled once; this used to be rebuilt several times per app per keystroke
private val combiningMarks = Regex("\\p{M}+")

/**
 * Lower-cases the text and strips accents/diacritics so "Café" matches "cafe".
 */
internal fun normalizeForSearch(text: String): String {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(combiningMarks, "")
        .lowercase()
}

internal fun fuzzyMatch(text: String, pattern: String): Boolean {
    // Case-insensitive contains check
    if (text.contains(pattern, ignoreCase = true)) {
        return true
    }

    val normalizedText = normalizeForSearch(text)
    val normalizedPattern = normalizeForSearch(pattern)

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
    val normalizedQuery = normalizeForSearch(query)

    // Compute each app's rank once rather than inside the comparator (which runs O(n log n) times)
    return apps
        .map { app ->
            val normalizedName = normalizeForSearch(app.displayName)
            val rank = when {
                normalizedName.startsWith(normalizedQuery) -> 0
                normalizedName.contains(normalizedQuery) -> 1
                else -> 2
            }
            Triple(app, rank, normalizedName)
        }
        .sortedWith(compareBy<Triple<InstalledApp, Int, String>> { it.second }.thenBy { it.third })
        .map { it.first }
}
