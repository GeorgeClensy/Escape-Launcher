package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.search.fuzzyMatch
import org.junit.Test
import org.junit.Assert.*

class SearchAppsUseCaseTest {

    @Test
    fun `fuzzyMatch should return true for exact match`() {
        assertTrue(fuzzyMatch("YouTube", "YouTube"))
    }

    @Test
    fun `fuzzyMatch should return true for case-insensitive match`() {
        assertTrue(fuzzyMatch("YouTube", "youtube"))
    }

    @Test
    fun `fuzzyMatch should return true for substring match`() {
        assertTrue(fuzzyMatch("YouTube", "you"))
    }

    @Test
    fun `fuzzyMatch should return true for trimmed query with trailing space`() {
        val query = "you "
        assertTrue(fuzzyMatch("YouTube", query.trim()))
    }

    @Test
    fun `fuzzyMatch should match initials`() {
        assertTrue(fuzzyMatch("Google Maps", "gm"))
    }

    @Test
    fun `fuzzyMatch should match with gaps`() {
        assertTrue(fuzzyMatch("YouTube", "ytb"))
    }

    @Test
    fun `fuzzyMatch should handle accents`() {
        assertTrue(fuzzyMatch("Café", "cafe"))
    }
}
