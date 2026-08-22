package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.model.AppShortcut
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAppShortcutsUseCaseTest {

    private lateinit var repository: AppsRepository
    private lateinit var useCase: GetAppShortcutsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAppShortcutsUseCase(repository)
    }

    @Test
    fun `invoke should call repository getShortcuts and return result`() {
        // Given
        val packageName = "com.example.app"
        val expectedShortcuts = listOf(
            AppShortcut("id1", "Shortcut 1", 1),
            AppShortcut("id2", "Shortcut 2", 2)
        )
        every { repository.getShortcuts(packageName) } returns expectedShortcuts

        // When
        val result = useCase(packageName)

        // Then
        assertEquals(expectedShortcuts, result)
        verify { repository.getShortcuts(packageName) }
    }

    @Test
    fun `invoke should return empty list when repository returns empty list`() {
        // Given
        val packageName = "com.example.app"
        every { repository.getShortcuts(packageName) } returns emptyList()

        // When
        val result = useCase(packageName)

        // Then
        assertEquals(0, result.size)
        verify { repository.getShortcuts(packageName) }
    }
}
