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
 * - The home page must stay the top page. The home screen hosts a nested scroll
 *   connection (see [com.geecee.escapelauncher.feature.homescreen.HomeScreen])
 *   that expands the notification shade on downward overscroll. With a page
 *   above home, the pager would accept the downward leftover the connection
 *   declines below its drag threshold and drag that page in over home; from
 *   then on the pager either takes over the gesture or keeps racing the
 *   connection's accumulator, so the shade either never opens or fires together
 *   with the page dragging in — broken either way. With home on top the pager
 *   has no page above it and leaves downward swipes to the shade gesture.
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
 * - When home content overflows the viewport (large clock, widget, lots of
 *   favourites), a swipe up scrolls the home list first and the apps list opens
 *   from the list's end, the same continuous-scroll model other vertically paged
 *   launchers use. A home list that fits the viewport consumes no vertical
 *   scroll, so the swipe reaches the pager directly. Hosted widgets that
 *   consume vertical drags themselves can keep a swipe started on them from
 *   reaching the pager, like any launcher with vertical gestures. The main pager
 *   disables its own overscroll effect (see [MainPagerScreen]) so vertical drags
 *   it cannot scroll show no stretch, matching the horizontal pager, which never
 *   rendered vertical overscroll.
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
