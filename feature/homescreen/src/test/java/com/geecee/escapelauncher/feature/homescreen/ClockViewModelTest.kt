package com.geecee.escapelauncher.feature.homescreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.geecee.escapelauncher.core.domain.time.GetCurrentTimePartsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClockViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val getCurrentTimeParts: GetCurrentTimePartsUseCase = mockk()

    private lateinit var viewModel: ClockViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkConstructor(IntentFilter::class)
        every { anyConstructed<IntentFilter>().addAction(any()) } returns Unit
        mockkStatic(ContextCompat::class)

        every { getCurrentTimeParts.invoke(any(), any()) } returns Triple(12, 0, false)
        viewModel = ClockViewModel(context, getCurrentTimeParts)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startTicker updates time parts and triggers update on broadcast`() = runTest {
        val receiverSlot = slot<BroadcastReceiver>()
        every {
            ContextCompat.registerReceiver(
                eq(context),
                capture(receiverSlot),
                any(),
                any()
            )
        } returns null

        every { getCurrentTimeParts.invoke(any(), false) } returns Triple(10, 30, false)

        var timeParts: Triple<Int, Int, Boolean>? = null
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.timeParts.collect { timeParts = it }
        }

        viewModel.startTicker(false)
        testScheduler.advanceUntilIdle()

        assertEquals(Triple(10, 30, false), timeParts)

        // Simulate broadcast event (TIME_TICK)
        every { getCurrentTimeParts.invoke(any(), false) } returns Triple(10, 31, false)
        if (receiverSlot.isCaptured) {
            receiverSlot.captured.onReceive(context, Intent(Intent.ACTION_TIME_TICK))
        }

        assertEquals(Triple(10, 31, false), timeParts)

        job.cancel()
    }
}
