package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class StartShortcutUseCaseTest {

    private lateinit var repository: AppsRepository
    private lateinit var useCase: StartShortcutUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = StartShortcutUseCase(repository)
    }

    @Test
    fun `invoke should call repository startShortcut`() {
        // Given
        val packageName = "com.example.app"
        val shortcutId = "shortcut_123"
        io.mockk.every { repository.startShortcut(packageName, shortcutId) } just runs

        // When
        useCase(packageName, shortcutId)

        // Then
        verify { repository.startShortcut(packageName, shortcutId) }
    }
}
