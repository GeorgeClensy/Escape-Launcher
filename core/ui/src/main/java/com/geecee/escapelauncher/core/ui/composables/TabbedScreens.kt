package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class TabbedScreen(
    var title: String,
    var icon: ImageVector,
    var content: @Composable BoxScope.(PaddingValues) -> Unit = {}
)

@Composable
fun Tab(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = true,
    showIcon: Boolean = true,
    showText: Boolean = true,
    selectedRadius: Dp = 16.dp,
    unselectedRadius: Dp = 56.dp,
    disabled: Boolean = false,
    useSecondaryColors: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    var isMorphed by remember { mutableStateOf(false) }
    val isPressed by interactionSource.collectIsPressedAsState()

    val radius = if (isMorphed || isPressed || selected) selectedRadius else unselectedRadius

    val animatedRadius by animateDpAsState(
        targetValue = radius, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "FabCornerAnimation"
    )

    val targetContainerColor = when {
        useSecondaryColors -> MaterialTheme.colorScheme.secondary
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val targetDisabledContainerColor = when {
        useSecondaryColors -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }
    val targetContentColor = when {
        useSecondaryColors -> MaterialTheme.colorScheme.onSecondary
        selected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val targetDisabledContentColor = when {
        useSecondaryColors -> MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
        selected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    val containerColor by animateColorAsState(
        targetValue = targetContainerColor, label = "containerColor"
    )
    val disabledContainerColor by animateColorAsState(
        targetValue = targetDisabledContainerColor, label = "disabledContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = targetContentColor, label = "contentColor"
    )
    val disabledContentColor by animateColorAsState(
        targetValue = targetDisabledContentColor, label = "disabledContentColor"
    )

    val innerHorizontalPadding by animateDpAsState(
        targetValue = if (showText) {
            20.dp
        } else 0.dp
    )

    Card(
        modifier = modifier.height(56.dp), interactionSource = interactionSource, onClick = {
            if (!isMorphed) {
                isMorphed = true
                scope.launch {
                    delay(200.milliseconds)
                    isMorphed = false
                }
            } else {
                isMorphed = false
            }

            onClick()
        }, shape = RoundedCornerShape(animatedRadius), colors = CardColors(
            containerColor = containerColor,
            disabledContainerColor = disabledContainerColor,
            contentColor = contentColor,
            disabledContentColor = disabledContentColor
        ), enabled = !disabled
    ) {
        Box(
            Modifier
                .weight(1f)
                .defaultMinSize(minWidth = 56.dp)
                .padding(
                    horizontal = innerHorizontalPadding
                )
        ) {
            Row(
                Modifier
                    .padding(5.dp)
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(showIcon) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            })
                }
                AnimatedVisibility(showText) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = if (showIcon) 5.dp else 0.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TabBar(
    screens: List<TabbedScreen>,
    selectedTabIndex: MutableIntState,
    modifier: Modifier = Modifier,
    reverse: Boolean = false,

    // Search stuff
    showSearch: Boolean = true,
    searchText: String = "",
    searchExpanded: Boolean = false,
    onSearchExpandedChange: (Boolean) -> Unit = {},
    onSearchTextChanged: (String) -> Unit = {},
    onSearchDone: (String, SoftwareKeyboardController?) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()

    val showLeftFade by remember(reverse) {
        derivedStateOf { if (!reverse) scrollState.value > 0 else scrollState.value < scrollState.maxValue }
    }
    val showRightFade by remember(reverse) {
        derivedStateOf { if (!reverse) scrollState.value < scrollState.maxValue else scrollState.value > 0 }
    }

    val leftAlpha by animateFloatAsState(
        targetValue = if (showLeftFade) 1f else 0f, label = "LeftFadeAlpha"
    )
    val rightAlpha by animateFloatAsState(
        targetValue = if (showRightFade) 1f else 0f, label = "RightFadeAlpha"
    )

    // Go to all apps when search pressed (for now, we may implement searching within lists later)
    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            selectedTabIndex.intValue = 0
        }
    }

    var searchBarBounds by remember { mutableStateOf(Rect.Zero) }

    Row(
        modifier = modifier
            .graphicsLayer { alpha = 0.99f }
            .drawWithContent {
                drawContent()

                val fadeWidth = 30.dp.toPx()

                // Left edge fade
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = leftAlpha), Color.Transparent),
                        startX = 0f,
                        endX = fadeWidth
                    ), blendMode = BlendMode.DstOut
                )

                // Right edge fade
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = rightAlpha)),
                        startX = size.width - fadeWidth,
                        endX = size.width
                    ), blendMode = BlendMode.DstOut
                )
            }
            .horizontalScroll(
                state = scrollState, reverseScrolling = reverse
            )
            .pointerInput(searchExpanded) {
                if (!searchExpanded) return@pointerInput

                awaitEachGesture {
                    // Intercept down event before child views consume it
                    val downEvent = awaitFirstDown(pass = PointerEventPass.Initial)

                    // Only proceed if the tap occurred outside the search bar bounds
                    if (!searchBarBounds.contains(downEvent.position)) {
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            onSearchExpandedChange(false)
                        }
                    }
                }
            }, horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (reverse) {
            Spacer(Modifier.width(15.dp))
        }

        if (!reverse && showSearch) {
            AnimatedPillSearchBar(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    searchBarBounds = coordinates.boundsInParent()
                },
                searchText = searchText,
                isExpanded = searchExpanded,
                autoFocus = false,
                onExpandedChange = onSearchExpandedChange,
                onSearchTextChanged = onSearchTextChanged,
                onSearchDone = onSearchDone
            )
        }

        val displayList = if (reverse) screens.reversed() else screens
        displayList.forEach { screen ->
            Tab(
                text = screen.title,
                icon = screen.icon,
                selected = selectedTabIndex.intValue == screens.indexOf(screen),
                showText = selectedTabIndex.intValue == screens.indexOf(screen),
                disabled = searchExpanded,
                onClick = {
                    selectedTabIndex.intValue = screens.indexOf(screen)
                })
        }

        if (reverse && showSearch) {
            AnimatedPillSearchBar(
                searchText = searchText,
                isExpanded = searchExpanded,
                autoFocus = false,
                onExpandedChange = onSearchExpandedChange,
                onSearchTextChanged = onSearchTextChanged,
                onSearchDone = onSearchDone
            )
        }

        if (!reverse) {
            Spacer(Modifier.width(15.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabScreen(
    modifier: Modifier = Modifier,
    screens: List<TabbedScreen>,
    selectedTabIndex: MutableIntState,
    alignment: Alignment.Horizontal = Alignment.Start,

    // Search stuff
    showSearch: Boolean = true,
    searchText: String = "",
    searchExpanded: Boolean = false,
    onSearchExpandedChange: (Boolean) -> Unit = {},
    onSearchTextChanged: (String) -> Unit = {},
    onSearchDone: (String, SoftwareKeyboardController?) -> Unit = { _, _ -> }
) {
    val topOfTabBarHeight =
        56.dp + 30.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val pagerState = rememberPagerState(
        initialPage = 0, initialPageOffsetFraction = 0f, pageCount = { screens.size })

    LaunchedEffect(selectedTabIndex.intValue) {
        pagerState.animateScrollToPage(selectedTabIndex.intValue)
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = false
        ) { page ->
            Box(Modifier.fillMaxSize()) {
                screens[page].content(this, PaddingValues(bottom = topOfTabBarHeight))
            }
        }

        Box(
            modifier = Modifier
                .padding(
                    bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                )
                .fillMaxWidth()
                .height(topOfTabBarHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .align(Alignment.BottomCenter)
        )

        TabBar(
            modifier = Modifier
                .align(
                    when (alignment) {
                        Alignment.End -> {
                            Alignment.BottomEnd
                        }

                        Alignment.CenterHorizontally -> {
                            Alignment.BottomCenter
                        }

                        else -> {
                            Alignment.BottomStart
                        }
                    }
                )
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                )
                .padding(
                    start = if (alignment == Alignment.Start || alignment == Alignment.CenterHorizontally) 30.dp else 0.dp,
                    top = 0.dp,
                    bottom = 30.dp,
                    end = if (alignment == Alignment.End || alignment == Alignment.CenterHorizontally) 30.dp else 0.dp
                ),
            screens = screens,
            selectedTabIndex = selectedTabIndex,
            reverse = alignment == Alignment.End,
            showSearch = showSearch,
            searchText = searchText,
            searchExpanded = searchExpanded,
            onSearchExpandedChange = onSearchExpandedChange,
            onSearchTextChanged = onSearchTextChanged,
            onSearchDone = onSearchDone
        )
    }
}

//Previews
@Preview(device = "id:pixel_10")
@Composable
fun PrevTab() {
    EscapeThemePreview {
        Row {
            Tab(
                text = "Apps", icon = Icons.Rounded.Apps
            )
        }
    }
}

@Preview
@Composable
fun PrevTabBar() {
    EscapeThemePreview {
        TabBar(
            listOf(
                TabbedScreen(
                    title = "All Apps", icon = Icons.AutoMirrored.Filled.List
                ),
                TabbedScreen(
                    title = "Private", icon = Icons.Default.Lock
                ),
                TabbedScreen(
                    title = "Money", icon = Icons.Default.AttachMoney
                ),
            ), selectedTabIndex = remember { mutableIntStateOf(0) })
    }
}

@Preview(device = "id:pixel_6a")
@Composable
fun PrevTabScreen() {
    EscapeThemePreview {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TabScreen(
                screens = listOf(
                TabbedScreen(
                    title = "All Apps", icon = Icons.Rounded.Apps, content = { padding ->
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 30.dp)
                                .align(Alignment.BottomStart)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Spacer(Modifier.height(30.dp))
                            repeat(30) {
                                Text(
                                    "App List Item $it",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                            Spacer(Modifier.height(padding.calculateBottomPadding()))
                        }
                    }),
                TabbedScreen(
                    title = "Private", icon = Icons.Default.Lock, content = {
                        Text("Locked Content", Modifier.padding(16.dp))
                    }),
                TabbedScreen(
                    title = "Money", icon = Icons.Default.AttachMoney
                ),
            ),
                selectedTabIndex = remember { mutableIntStateOf(0) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}