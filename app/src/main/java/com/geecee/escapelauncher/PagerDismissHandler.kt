package com.geecee.escapelauncher

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * The pure decisions of the vertical pager's dismiss gesture: what page a
 * released drag settles on, when a drag may take over as a dismissal, and
 * when an app launch may still snap the pager home.
 *
 * Everything here is side-effect-free and touches nothing but numbers, so
 * the launcher's release behaviour can be unit tested directly. Velocities
 * follow the screen convention (down = positive); offsets follow the
 * fraction convention of [PagerState.currentPageOffsetFraction] (within
 * [-0.5, 0.5) around [PagerState.currentPage]).
 */
internal object PagerScrollPolicy {

    /**
     * The page a dismiss drag should settle on when the finger lifts.
     *
     * The nearest page is the one geometrically closest to the released
     * position: the drag sits on [currentPage] unless its offset fraction
     * has crossed -0.5 — the crossing point where the pager itself flips
     * the current page backwards. The release then lands by velocity: a
     * downward flick at or above [flingVelocityThreshold] carries the user
     * to [dismissTargetPage], but only while the drag has not already
     * swept past the target page ([currentPage] >= [dismissTargetPage]);
     * past it the flick simply continues to the nearest page. A release
     * with zero or upward velocity always lands on the nearest page, so a
     * dismissal can never strand between pages and an upward release can
     * never slingshot the user dismissward.
     *
     * [currentPageOffsetFraction] is within [-0.5, 0.5) by contract; the
     * out-of-contract sides are clamped defensively so the function stays
     * total for any input.
     */
    fun settlePage(
        dismissTargetPage: Int,
        currentPage: Int,
        currentPageOffsetFraction: Float,
        flingVelocityY: Float,
        flingVelocityThreshold: Float
    ): Int {
        val nearestPage = when {
            currentPageOffsetFraction < -0.5f -> currentPage - 1
            // Defensive only: the contract keeps the fraction below +0.5
            currentPageOffsetFraction >= 0.5f -> currentPage + 1
            else -> currentPage
        }
        val flickCarries = flingVelocityY >= flingVelocityThreshold &&
            currentPage >= dismissTargetPage
        return if (flickCarries) dismissTargetPage else nearestPage
    }

    /**
     * Whether a gesture has proven itself a dismissal yet: the handler
     * accumulates the downward pre-scroll deltas of the current gesture and
     * may only take over once they exceed the system touch slop, so the
     * downward jitter riding another gesture — a horizontal tab swipe with
     * a downward tilt, a sloppy tap — cannot trigger a page dismissal.
     */
    fun shouldStartDismissDrag(gestureDownwardPx: Float, touchSlopPx: Float): Boolean =
        gestureDownwardPx > touchSlopPx

    /**
     * Whether the delayed return-to-main-page snap after an app launch may
     * still run: only when no user gesture is in flight (a jump during a
     * live drag session would be rejected by the pager's MutatorMutex
     * anyway — the user's hands win) and the user is actually away from
     * the main page.
     */
    fun shouldSnapToMainPageAfterAppLaunch(
        currentPage: Int,
        mainPage: Int,
        isScrollInProgress: Boolean
    ): Boolean = !isScrollInProgress && currentPage != mainPage
}

/**
 * A nested scroll connection that dismisses the pager's current page
 * towards [dismissTargetPage] on a downward swipe, from any scroll position
 * of the page's own scrolling content.
 *
 * Nested scroll deltas and fling velocities follow the pointer's screen
 * convention, so a downward drag or fling arrives as positive y even
 * though it scrolls the content and the pager backwards. The takeover
 * happens in pre-scroll because a swipe started mid-content would
 * otherwise be consumed by the content scrolling itself back towards its
 * top before the pager ever sees it.
 *
 * The drag does not feed the pager raw deltas: once the slop gate
 * ([PagerScrollPolicy.shouldStartDismissDrag]) passes, this connection
 * opens a real scroll session on the pager — a UserInput-priority scroll
 * mutation whose block drains a channel of the gesture's deltas — so the
 * pager experiences the dismissal exactly as it experiences its own
 * drags:
 * - `isScrollInProgress` is true for the drag's duration, so the settled
 *   page stays frozen (the apps-list cleanup does not fire under the
 *   finger) and the target page derived state flips on the position
 *   threshold like it does for native drags;
 * - a fresh user grab (the pager's own drag loops run the same priority)
 *   cancels the session through the pager's MutatorMutex, so a settle in
 *   flight is always cleanly interruptible;
 * - once the pager is displaced, its own nested scroll connection drives
 *   the delta — bounded per event, exactly as for a native boundary drag —
 *   and this connection gets out of the way to avoid double-driving.
 *
 * The release settles through [PagerScrollPolicy.settlePage] with the
 * pager's own animation, and consumes the whole release velocity so the
 * page's content cannot fling on top of the dismissal — the same choice
 * Material3 bottom sheets make for their hide swipe.
 */
internal class PagerDismissHandler(
    private val pagerState: PagerState,
    private val scope: CoroutineScope,
    private val dismissTargetPage: () -> Int,
    private val touchSlopPx: Float,
    private val flingVelocityThreshold: Float
) : NestedScrollConnection {

    /** Cumulative downward pre-scroll delta of the current gesture, reset at gesture end. */
    private var gestureDownwardPx = 0f

    /** Whether the current gesture has already started its dismiss drag session. */
    private var dismissDragActive = false

    /**
     * The current gesture's delta channel. One per gesture: the drain
     * session is started once (when the slop gate passes) and torn down at
     * gesture end, so deltas can never leak from one gesture into the
     * next.
     */
    private var deltaChannel: Channel<Float>? = null

    /** The current gesture's drain job, cancelled at gesture end. */
    private var drainJob: Job? = null

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // Only a user drag participates: upward browsing belongs to the
        // page's own content (its scrollable keeps every pixel), and
        // non-user sources (layout, accessibility) never drive gestures
        if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
        // Once the pager is displaced its own nested scroll connection
        // drives the drag: pre-scroll dispatch walks from the innermost
        // scrollable outwards, so this connection sees the delta before
        // the pager's connection does, and leaving it unconsumed here is
        // what lets the pager's connection drive it — bounded per event,
        // exactly as for a native drag. Consuming as well would drive the
        // same delta through the session twice. Displacement now only
        // ever comes from real scroll sessions, so this guard is exact
        if (pagerState.currentPageOffsetFraction != 0f) return Offset.Zero
        // The slop gate: nothing starts before the gesture has carried
        // more than touch slop of downward travel
        gestureDownwardPx += available.y
        if (!PagerScrollPolicy.shouldStartDismissDrag(gestureDownwardPx, touchSlopPx)) {
            return Offset.Zero
        }
        if (!dismissDragActive) {
            // Nothing to dismiss towards: the pager already sits at or
            // above the target page
            if (pagerState.currentPage <= dismissTargetPage()) return Offset.Zero
            startDismissDrag()
        }
        // Sign flip into the scroll-position convention: the negated
        // downward delta scrolls the pager backwards, towards the target
        // page. The channel is unlimited, so this never needs to block —
        // pre-scroll cannot suspend by contract
        deltaChannel?.trySend(-available.y)
        // Consume only the y: horizontal leftovers (a tab swipe in
        // flight) keep flowing to their own scrollable
        return Offset(0f, available.y)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (dismissDragActive) {
            // The settle decision reads the live position and the release
            // velocity together (see [PagerScrollPolicy.settlePage]): a
            // dismissward flick carries to the target page while the drag
            // has not already swept past it, and a zero-velocity or
            // upward release lands on the nearest page — so a dismiss drag
            // always settles somewhere sane, whatever the release
            val settleTarget = PagerScrollPolicy.settlePage(
                dismissTargetPage = dismissTargetPage(),
                currentPage = pagerState.currentPage,
                currentPageOffsetFraction = pagerState.currentPageOffsetFraction,
                flingVelocityY = available.y,
                flingVelocityThreshold = flingVelocityThreshold
            )
            // Stop the session before launching the settle. Completing
            // the channel ends the drain gracefully: it finishes its
            // queued deltas, the scroll block returns and releases the
            // pager's MutatorMutex, and the job completes on its own.
            // The settle is then launched from the job's completion
            // handler, which runs strictly after the session has ended —
            // the Default-priority settle would be rejected by the
            // MutatorMutex if it raced ahead of the still-active
            // UserInput session, so the settle must not be launched
            // directly here. Completion handlers of an already-completed
            // job run immediately, so this is safe in every ordering.
            // Pre-fling must not await anything (it is the last call of
            // the gesture), so the drain is never waited on here
            deltaChannel?.close()
            deltaChannel = null
            drainJob?.invokeOnCompletion {
                // A real Default-priority mutation: a fresh user grab
                // (UserInput outranks it) cancels the settle cleanly, and
                // the pager animates to the decided page with its own
                // spring
                scope.launch {
                    pagerState.animateScrollToPage(settleTarget)
                }
            }
            drainJob = null
            // Eat the whole release velocity: the page's own content must
            // not fling backwards on top of the dismissal settle
            dismissDragActive = false
            gestureDownwardPx = 0f
            return available
        }
        // No dismiss drag this gesture: leave the release to the native
        // machinery — a displaced native drag settles through the pager's
        // own fling, an undisplaced release flings the page's content.
        // Pre-fling always fires at gesture end (cancelled gestures
        // included, with a zero velocity), so this is where the gesture
        // bookkeeping resets even when the slop gate never passed
        gestureDownwardPx = 0f
        return Velocity.Zero
    }

    /**
     * Opens the gesture's dismiss drag session: a UserInput-priority scroll
     * mutation on the pager whose block drains the gesture's channel. The
     * session is what makes the dismissal a real pager scroll:
     * `isScrollInProgress` stays true for its duration (freezing the
     * settled page and letting the target page derived state flip on the
     * position threshold), and a later mutation of equal or higher
     * priority — a fresh user grab — cancels it through the pager's
     * MutatorMutex.
     */
    private fun startDismissDrag() {
        dismissDragActive = true
        val channel = Channel<Float>(capacity = Channel.UNLIMITED)
        deltaChannel = channel
        drainJob = scope.launch {
            pagerState.scroll(MutatePriority.UserInput) {
                // A mirror of the pager's own drag loops: scrollBy alone
                // keeps the current page and its offset coherent (its
                // measure bookkeeping applies every delta), and while this
                // session is in flight the target page derived state flips
                // on the position threshold exactly like a native drag's.
                // The ScrollScope extensions are deliberately not called:
                // updateCurrentPage is an absolute jump (the measure pass
                // owns the per-delta bookkeeping), and updateTargetPage
                // pins a programmatic destination, which a drag whose
                // settle page is only decided at release must not do
                for (delta in channel) {
                    var remaining = delta
                    while (remaining != 0f) {
                        val consumed = scrollBy(remaining)
                        // The pager is at its scroll bound: the drag
                        // cannot carry further in this direction
                        if (consumed == 0f) break
                        remaining -= consumed
                    }
                }
            }
        }
    }
}

/**
 * Remembers the dismiss connection for one page of the vertical pager:
 * downward swipes on that page dismiss the pager towards [targetPage].
 *
 * Only the vertical layout composes this — the horizontal layout has no
 * dismissal, and a connection constructed there would be dead state that
 * no gesture could ever reach.
 */
@Composable
internal fun rememberPagerDismissConnection(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    targetPage: () -> Int,
    touchSlopPx: Float,
    flingVelocityThreshold: Float
): NestedScrollConnection = remember {
    PagerDismissHandler(
        pagerState = pagerState,
        scope = coroutineScope,
        dismissTargetPage = targetPage,
        touchSlopPx = touchSlopPx,
        flingVelocityThreshold = flingVelocityThreshold
    )
}
