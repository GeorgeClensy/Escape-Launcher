package com.geecee.escapelauncher

import com.geecee.escapelauncher.core.common.DefaultSettings

/**
 * Page layouts of the launcher's main pager.
 *
 * The vertical layout (the default) stacks the pages top to bottom: the home
 * page sits on top, the apps list lives directly below it so it slides up from
 * the bottom of the screen when the user swipes up on the home page, and the
 * optional screen time dashboard sits below the apps list. The horizontal
 * layout is the original screen order: the dashboard, the home page and the
 * apps list side by side, opened with sideways swipes.
 *
 * Both layouts are load bearing, so do not reorder pages without checking the
 * gestures below:
 * - In the vertical layout the home page must stay the top page. The home
 *   screen observes downward swipes for the notification shade (see
 *   [com.geecee.escapelauncher.feature.homescreen.HomeScreen]) and only counts
 *   movement nothing else consumed. With a page above home, the pager would
 *   consume those swipes to drag the page in over home, so the shade would
 *   never open. With home on top the pager has no page above it and downward
 *   swipes belong to the shade.
 * - In the vertical layout the apps list must sit directly below home so a
 *   swipe up on home opens it. On devices with gesture navigation the bottom
 *   edge of the screen belongs to the system home gesture, so like every
 *   launcher with a vertical drawer the swipe has to start above that edge.
 * - In the vertical layout the dashboard sits below the apps list, so it is
 *   reached by continuing the swipe past the end of the apps list. It cannot
 *   sit above home without starving the shade gesture, and it cannot sit
 *   between home and the apps list without breaking the swipe up. Returning
 *   home from the dashboard with the back or home gesture sweeps the apps list
 *   page across the screen: the layout is a linear stack and the animated
 *   return keeps the original animation, so the sweep is inherent to it (the
 *   instant jump used when launching an app skips it).
 * - In the vertical layout the home page does not scroll: a swipe up from
 *   anywhere on it engages the pager directly, so the apps list is one short
 *   swipe away regardless of how much content the home page has, and content
 *   taller than the viewport is cropped — this launcher's home is meant to
 *   stay short. Symmetrically, a swipe down on the apps list returns home from
 *   any scroll position instead of scrolling the list back to its top first,
 *   and a swipe down on the dashboard returns to the apps list the same way
 *   (see [MainPagerScreen]) — the list resets to its top every time the drawer
 *   closes. Hosted widgets that consume vertical drags themselves can keep a
 *   swipe started on them from reaching the pager, like any launcher with
 *   vertical gestures. The main pager disables its own overscroll effect (see
 *   [MainPagerScreen]): a swipe down on home is a drag the pager cannot
 *   scroll, and a stretch effect would consume it before the home page's shade
 *   observer sees it.
 * - In the horizontal layout the pages keep the original upstream behaviour:
 *   the home page scrolls, the apps list scrolls in both directions, and the
 *   page indices shift with the hide setting exactly like the original did
 *   (the dashboard sits in front of them, so removing it shifts them towards
 *   the front of the pager), which is why its callers snap to the main page on
 *   every dashboard toggle; the vertical layout's indices are stable across
 *   the setting instead.
 */
object MainPagerLayout {
    // Vertical layout (the default): home on top, apps list below, dashboard below that
    const val HOME_PAGE_INDEX = 0
    const val APPS_LIST_PAGE_INDEX = 1
    private const val SCREEN_TIME_PAGE_INDEX = 2

    /**
     * Total number of pages for the given state of the hide screen time page
     * setting. Both layouts show the same pages, so the counts match.
     */
    fun pageCount(hideScreenTimePage: Boolean): Int =
        if (hideScreenTimePage) APPS_LIST_PAGE_INDEX + 1 else SCREEN_TIME_PAGE_INDEX + 1

    /**
     * Index of the screen time dashboard in the vertical layout, or -1 when
     * the dashboard page is hidden. -1 never matches a page the pager
     * composes, so the dashboard branch of the pager content is skipped.
     */
    fun screenTimePageIndex(hideScreenTimePage: Boolean): Int =
        if (hideScreenTimePage) -1 else SCREEN_TIME_PAGE_INDEX

    /**
     * Index of the screen time dashboard in the horizontal layout, or -1 when
     * the dashboard page is hidden.
     */
    fun horizontalScreenTimePageIndex(hideScreenTimePage: Boolean): Int =
        if (hideScreenTimePage) -1 else 0

    /**
     * Index of the home page in the horizontal layout, shifting with the
     * hide setting exactly like the original did: the dashboard sits in
     * front of it, so hiding the dashboard promotes the home page to the
     * front of the pager.
     */
    fun horizontalHomePageIndex(hideScreenTimePage: Boolean): Int =
        if (hideScreenTimePage) 0 else 1

    /**
     * Index of the apps list in the horizontal layout, shifting with the
     * hide setting exactly like the original did: it sits behind the home
     * page, so hiding the dashboard shifts it along with the home page.
     */
    fun horizontalAppsListPageIndex(hideScreenTimePage: Boolean): Int =
        if (hideScreenTimePage) 1 else 2

    /**
     * Index of the main page for the given screen layout setting. The
     * horizontal layout's main page shifts with the hide screen time page
     * setting, so its callers pass that along; the vertical layout's does
     * not.
     */
    fun mainPageIndex(screenLayout: String, hideScreenTimePage: Boolean): Int =
        if (screenLayout == DefaultSettings.SCREEN_LAYOUT_HORIZONTAL) horizontalHomePageIndex(hideScreenTimePage) else HOME_PAGE_INDEX

    /**
     * Whether the page the user is currently on — or, while a scroll is still
     * in flight and [currentPage] has not yet followed its target, the page
     * they are heading towards — stops existing once the hide screen time page
     * setting takes the given value, i.e. the dashboard is being removed while
     * the user is on it or heading towards it. At rest the target equals the
     * current page, so a settled user is never relocated unless their page is
     * the one being removed, in which case the caller should relocate them
     * (scrolling to the home page, best-effort: a scroll the user is still
     * driving outranks and cancels that jump).
     *
     * This is the vertical layout's guard: the horizontal layout's page
     * indices shift with the setting, so its caller always snaps to the main
     * page like the original behaviour did instead.
     *
     * A scroll towards the dashboard that has not yet crossed the page's target
     * threshold reports its target as the page it came from, and once the new
     * page count lands a read here may already see coerced values, so an
     * in-flight scroll can be missed. The pager coerces its pages into range on
     * its next measure whatever this returns, so a missed user still lands on
     * a page that exists — relocation here only decides whether it is the
     * home page. After a relocation, a later toggle that restores the dashboard
     * leaves the user where the relocation put them.
     */
    fun isPageBeingRemoved(currentPage: Int, targetPage: Int, hideScreenTimePage: Boolean): Boolean =
        maxOf(currentPage, targetPage) >= pageCount(hideScreenTimePage)
}
