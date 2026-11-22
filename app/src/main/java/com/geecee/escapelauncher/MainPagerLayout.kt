package com.geecee.escapelauncher

/**
 * Page layout of the launcher's main vertical pager.
 *
 * The pages stack top to bottom: the home page sits on top, the apps list lives
 * directly below it so it slides up from the bottom of the screen when the user
 * swipes up on the home page, and the optional screen time dashboard sits below
 * the apps list.
 *
 * This order is load bearing, so do not reorder pages without checking the
 * gestures below:
 * - The home page must stay the top page. The home screen observes downward
 *   swipes for the notification shade (see
 *   [com.geecee.escapelauncher.feature.homescreen.HomeScreen]) and only counts
 *   movement nothing else consumed. With a page above home, the pager would
 *   consume those swipes to drag the page in over home, so the shade would
 *   never open. With home on top the pager has no page above it and downward
 *   swipes belong to the shade.
 * - The apps list must sit directly below home so a swipe up on home opens it.
 *   On devices with gesture navigation the bottom edge of the screen belongs to
 *   the system home gesture, so like every launcher with a vertical drawer the
 *   swipe has to start above that edge.
 * - The dashboard sits below the apps list, so it is reached by continuing the
 *   swipe past the end of the apps list. It cannot sit above home without
 *   starving the shade gesture, and it cannot sit between home and the apps list
 *   without breaking the swipe up. Returning home from the dashboard with the
 *   back or home gesture sweeps the apps list page across the screen: the layout
 *   is a linear stack and the animated return keeps the original animation, so
 *   the sweep is inherent to it (the instant jump used when launching an app
 *   skips it).
 * - The home page does not scroll: a swipe up from anywhere on it engages the
 *   pager directly, so the apps list is one short swipe away regardless of how
 *   much content the home page has, and content taller than the viewport is
 *   cropped — this launcher's home is meant to stay short. Hosted widgets that
 *   consume vertical drags themselves can keep a swipe started on them from
 *   reaching the pager, like any launcher with vertical gestures. The main pager
 *   disables its own overscroll effect (see [MainPagerScreen]): a swipe down on
 *   home is a drag the pager cannot scroll, and a stretch effect would consume
 *   it before the home page's shade observer sees it.
 */
object MainPagerLayout {
    const val HOME_PAGE_INDEX = 0
    const val APPS_LIST_PAGE_INDEX = 1
    private const val SCREEN_TIME_PAGE_INDEX = 2

    /**
     * Total number of pages for the given state of the hide screen time page setting.
     */
    fun pageCount(hideScreenTimePage: Boolean): Int =
        if (hideScreenTimePage) APPS_LIST_PAGE_INDEX + 1 else SCREEN_TIME_PAGE_INDEX + 1

    /**
     * Index of the screen time dashboard, or -1 when the dashboard page is
     * hidden. -1 never matches a page the pager composes, so the dashboard
     * branch of the pager content is skipped.
     */
    fun screenTimePageIndex(hideScreenTimePage: Boolean): Int =
        if (hideScreenTimePage) -1 else SCREEN_TIME_PAGE_INDEX

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
