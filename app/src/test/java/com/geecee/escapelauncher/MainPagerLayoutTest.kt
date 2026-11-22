package com.geecee.escapelauncher

import com.geecee.escapelauncher.core.common.DefaultSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the load-bearing page layout invariants described by [MainPagerLayout].
 * These tests are layout pin tests: they document and lock the page order the
 * launcher's gestures (swipe up opens the apps list, swipe down on home opens
 * the notification shade, the dashboard sits below the apps list) depend on.
 */
class MainPagerLayoutTest {

    @Test
    fun `home is the first page of the pager`() {
        assertEquals(0, MainPagerLayout.HOME_PAGE_INDEX)
    }

    @Test
    fun `apps list page is adjacent below home`() {
        assertEquals(MainPagerLayout.HOME_PAGE_INDEX + 1, MainPagerLayout.APPS_LIST_PAGE_INDEX)
    }

    @Test
    fun `dashboard is adjacent below the apps list when shown`() {
        assertEquals(
            MainPagerLayout.APPS_LIST_PAGE_INDEX + 1,
            MainPagerLayout.screenTimePageIndex(hideScreenTimePage = false)
        )
    }

    @Test
    fun `dashboard is the last page when shown`() {
        assertEquals(
            MainPagerLayout.pageCount(hideScreenTimePage = false) - 1,
            MainPagerLayout.screenTimePageIndex(hideScreenTimePage = false)
        )
    }

    @Test
    fun `page count is two when the dashboard is hidden and three when it is shown`() {
        assertEquals(2, MainPagerLayout.pageCount(hideScreenTimePage = true))
        assertEquals(3, MainPagerLayout.pageCount(hideScreenTimePage = false))
    }

    @Test
    fun `hidden dashboard index is outside the composed page range`() {
        val hiddenIndex = MainPagerLayout.screenTimePageIndex(hideScreenTimePage = true)
        val pageCount = MainPagerLayout.pageCount(hideScreenTimePage = true)

        assertFalse(hiddenIndex in 0 until pageCount)
    }

    @Test
    fun `every composed page maps to content when the dashboard is shown`() {
        val dashboardIndex = MainPagerLayout.screenTimePageIndex(hideScreenTimePage = false)

        for (page in 0 until MainPagerLayout.pageCount(hideScreenTimePage = false)) {
            val hasContent = page == MainPagerLayout.HOME_PAGE_INDEX ||
                page == MainPagerLayout.APPS_LIST_PAGE_INDEX ||
                page == dashboardIndex

            assertTrue("page $page has no content", hasContent)
        }
    }

    @Test
    fun `every composed page maps to content when the dashboard is hidden`() {
        val dashboardIndex = MainPagerLayout.screenTimePageIndex(hideScreenTimePage = true)

        for (page in 0 until MainPagerLayout.pageCount(hideScreenTimePage = true)) {
            val hasContent = page == MainPagerLayout.HOME_PAGE_INDEX ||
                page == MainPagerLayout.APPS_LIST_PAGE_INDEX ||
                page == dashboardIndex

            assertTrue("page $page has no content", hasContent)
        }
    }

    @Test
    fun `relocation guard truth table covers every settled page in both setting states`() {
        // Showing the dashboard: no page is removed, from any existing page
        for (page in 0 until MainPagerLayout.pageCount(hideScreenTimePage = false)) {
            assertFalse(
                "page $page must not be removed",
                MainPagerLayout.isPageBeingRemoved(
                    currentPage = page,
                    targetPage = page,
                    hideScreenTimePage = false
                )
            )
        }

        // Hiding the dashboard: only the dashboard page is removed
        val removedPage = MainPagerLayout.screenTimePageIndex(hideScreenTimePage = false)
        assertFalse(
            MainPagerLayout.isPageBeingRemoved(
                currentPage = MainPagerLayout.HOME_PAGE_INDEX,
                targetPage = MainPagerLayout.HOME_PAGE_INDEX,
                hideScreenTimePage = true
            )
        )
        assertFalse(
            MainPagerLayout.isPageBeingRemoved(
                currentPage = MainPagerLayout.APPS_LIST_PAGE_INDEX,
                targetPage = MainPagerLayout.APPS_LIST_PAGE_INDEX,
                hideScreenTimePage = true
            )
        )
        assertTrue(
            MainPagerLayout.isPageBeingRemoved(currentPage = removedPage, targetPage = removedPage, hideScreenTimePage = true)
        )
    }

    @Test
    fun `relocation guard catches an in-flight scroll towards the removed dashboard`() {
        // Flinging up from the apps list towards the dashboard, past the page's
        // target threshold, as the dashboard is hidden. A live read may already
        // be coerced by then; both misses converge on the same guard input as
        // the below threshold test below pins
        assertTrue(
            MainPagerLayout.isPageBeingRemoved(
                currentPage = 1,
                targetPage = 2,
                hideScreenTimePage = true
            )
        )
    }

    @Test
    fun `relocation guard misses a fling towards the removed dashboard below the snap threshold`() {
        // A fling that has not crossed the page snap threshold yet still
        // reports its target as the page it came from, so the guard misses it
        // and the pager's measure coercion lands the user on the apps list
        assertFalse(
            MainPagerLayout.isPageBeingRemoved(
                currentPage = 1,
                targetPage = 1,
                hideScreenTimePage = true
            )
        )
    }

    @Test
    fun `relocation guard catches a user leaving the removed dashboard mid-scroll`() {
        // Heading home from the dashboard as it is hidden: relocated to the
        // home page they were heading to anyway. The concrete positions are
        // deliberately literal so a reorder of the layout fails loudly here
        assertTrue(
            MainPagerLayout.isPageBeingRemoved(
                currentPage = 2,
                targetPage = 0,
                hideScreenTimePage = true
            )
        )

        // Heading to the apps list from the dashboard as it is hidden
        assertTrue(
            MainPagerLayout.isPageBeingRemoved(
                currentPage = 2,
                targetPage = 1,
                hideScreenTimePage = true
            )
        )
    }

    @Test
    fun `relocation guard leaves the user alone when every page in play survives`() {
        // Scrolling home from the dashboard while the dashboard stays
        assertFalse(
            MainPagerLayout.isPageBeingRemoved(
                currentPage = 2,
                targetPage = 0,
                hideScreenTimePage = false
            )
        )

        // Settling onto the apps list while the dashboard is hidden
        assertFalse(
            MainPagerLayout.isPageBeingRemoved(
                currentPage = 0,
                targetPage = 1,
                hideScreenTimePage = true
            )
        )
    }

    @Test
    fun `horizontal layout keeps the original screen order`() {
        // The original upstream order: dashboard, home page, apps list, opened
        // with sideways swipes. The indices shift with the hide setting
        // exactly like the original did: hiding the dashboard removes the page
        // in front of them
        assertEquals(1, MainPagerLayout.horizontalHomePageIndex(hideScreenTimePage = false))
        assertEquals(0, MainPagerLayout.horizontalHomePageIndex(hideScreenTimePage = true))
        assertEquals(2, MainPagerLayout.horizontalAppsListPageIndex(hideScreenTimePage = false))
        assertEquals(1, MainPagerLayout.horizontalAppsListPageIndex(hideScreenTimePage = true))
        assertEquals(0, MainPagerLayout.horizontalScreenTimePageIndex(hideScreenTimePage = false))
        assertEquals(-1, MainPagerLayout.horizontalScreenTimePageIndex(hideScreenTimePage = true))
    }

    @Test
    fun `both layouts use the same page counts`() {
        // MainPagerLayout has one pageCount for both layouts: the horizontal
        // pager composes exactly as many pages as the vertical one
        assertEquals(2, MainPagerLayout.pageCount(hideScreenTimePage = true))
        assertEquals(3, MainPagerLayout.pageCount(hideScreenTimePage = false))

        // The hidden dashboard never matches a page the pager composes
        val hiddenIndex = MainPagerLayout.horizontalScreenTimePageIndex(hideScreenTimePage = true)
        assertFalse(hiddenIndex in 0 until MainPagerLayout.pageCount(hideScreenTimePage = true))
    }

    @Test
    fun `main page index follows the screen layout setting`() {
        // The vertical layout's main page is stable across the hide setting
        assertEquals(
            MainPagerLayout.HOME_PAGE_INDEX,
            MainPagerLayout.mainPageIndex(DefaultSettings.SCREEN_LAYOUT_VERTICAL, hideScreenTimePage = false)
        )
        assertEquals(
            MainPagerLayout.HOME_PAGE_INDEX,
            MainPagerLayout.mainPageIndex(DefaultSettings.SCREEN_LAYOUT_VERTICAL, hideScreenTimePage = true)
        )
        // The horizontal layout's main page shifts with the hide setting
        assertEquals(
            1,
            MainPagerLayout.mainPageIndex(DefaultSettings.SCREEN_LAYOUT_HORIZONTAL, hideScreenTimePage = false)
        )
        assertEquals(
            0,
            MainPagerLayout.mainPageIndex(DefaultSettings.SCREEN_LAYOUT_HORIZONTAL, hideScreenTimePage = true)
        )
    }

    @Test
    fun `every composed page maps to content in the horizontal layout when the dashboard is shown or hidden`() {
        for (hideDashboard in listOf(false, true)) {
            val dashboardIndex = MainPagerLayout.horizontalScreenTimePageIndex(hideScreenTimePage = hideDashboard)
            val homeIndex = MainPagerLayout.horizontalHomePageIndex(hideScreenTimePage = hideDashboard)
            val appsListIndex = MainPagerLayout.horizontalAppsListPageIndex(hideScreenTimePage = hideDashboard)

            for (page in 0 until MainPagerLayout.pageCount(hideScreenTimePage = hideDashboard)) {
                val hasContent = page == homeIndex ||
                    page == appsListIndex ||
                    page == dashboardIndex

                assertTrue("page $page has no content with the dashboard hidden = $hideDashboard", hasContent)
            }
        }
    }
}
