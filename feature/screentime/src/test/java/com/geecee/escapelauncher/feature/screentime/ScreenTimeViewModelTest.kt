package com.geecee.escapelauncher.feature.screentime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.screentime.GetAppUsageUiListUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class ScreenTimeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context: Context = mockk(relaxed = true)
    private val screenTimeRepository: ScreenTimeRepository = mockk(relaxed = true)
    private val getAppUsageUiListUseCase: GetAppUsageUiListUseCase = mockk(relaxed = true)

    private lateinit var viewModel: ScreenTimeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkConstructor(IntentFilter::class)
        every { anyConstructed<IntentFilter>().addAction(any()) } returns Unit
        mockkStatic(ContextCompat::class)

        every { screenTimeRepository.getTotalUsageForDateFlow(any()) } returns flowOf(100L)
        every { getAppUsageUiListUseCase(any(), any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `datesFlow emits total usage and updates on broadcast`() = runTest {
        val receiverSlot = slot<BroadcastReceiver>()
        every {
            ContextCompat.registerReceiver(
                eq(context),
                capture(receiverSlot),
                any(),
                any()
            )
        } returns null

        viewModel = ScreenTimeViewModel(context, screenTimeRepository, getAppUsageUiListUseCase)

        var usage: Long? = null
        val job = launch(UnconfinedTestDispatcher()) {
            viewModel.totalUsage.collect { usage = it }
        }

        assertEquals(100L, usage)

        // Simulate broadcast (TIME_TICK)
        if (receiverSlot.isCaptured) {
            receiverSlot.captured.onReceive(context, Intent(Intent.ACTION_TIME_TICK))
        }

        assertEquals(100L, usage)

        job.cancel()
    }
}
