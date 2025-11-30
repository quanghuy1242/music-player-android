package dev.quanghuy.mpcareal.screens

// import androidx.compose.ui.unit.dp (duplicate removed)
// Using Modifier.blur for cross-platform consistent blur behavior
// import androidx.compose.ui.graphics.Color (duplicate removed)

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun NowPlayingScreen(
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    // Handle back press to collapse the player
    BackHandler(enabled = playbackViewModel.isPlayerExpanded) {
        playbackViewModel.togglePlayerExpanded()
    }

    val currentTrack = playbackViewModel.currentTrack
    val isPlaying = playbackViewModel.isPlaying
    val progress =
        0.3f // Placeholder - in a real app, this would be calculated from currentPosition
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var isShuffleEnabled by rememberSaveable { mutableStateOf(false) }
    var repeatMode by rememberSaveable { mutableStateOf(0) }

    val pagerState = rememberPagerState(initialPage = selectedTab) { 3 }
    LaunchedEffect(pagerState.currentPage) { selectedTab = pagerState.currentPage }

    // Swipe down to dismiss logic: progressive drag with snapping and animation
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val dismissThreshold = with(density) { 150.dp.toPx() }

    val dragScope = rememberCoroutineScope()
    val swipeModifier =
        Modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = { dragOffset = 0f },
                onDragEnd = {
                    if (dragOffset > dismissThreshold) {
                        // trigger collapse immediately so shared element transition can run
                        playbackViewModel.setPlayerExpandedState(false)
                        dragScope.launch {
                            offsetY.snapTo(0f)
                            dragOffset = 0f
                        }
                    } else {
                        dragScope.launch {
                            offsetY.animateTo(0f)
                            dragOffset = 0f
                        }
                    }
                },
            ) { change, dragAmount ->
                change.consume()
                dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                dragScope.launch { offsetY.snapTo(dragOffset.coerceAtMost(screenHeightPx)) }
            }
        }

    Box(
        modifier =
            modifier.fillMaxSize().then(swipeModifier).offset {
                IntOffset(0, offsetY.value.roundToInt())
            }
    ) {
        // background: album image if available
        if (currentTrack != null) {
            val bgModifier = Modifier.fillMaxSize().blur(60.dp)

            AsyncImage(
                model = currentTrack.imageUrl,
                contentDescription = null,
                modifier = bgModifier,
                contentScale = ContentScale.Crop,
            )
            // Darker scrim to reduce brightness of the blurred background and improve text contrast
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.62f))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
        }

        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Connected ToggleButton group (tabs) using ButtonGroup and connected shapes
                ButtonGroup(
                    modifier =
                        Modifier.wrapContentWidth(Alignment.CenterHorizontally)
                            .padding(horizontal = 16.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                ) {
                    val options = listOf("Playing", "Lyrics", "Queue")
                    val modifiers =
                        listOf(
                            Modifier.semantics { role = Role.RadioButton },
                            Modifier.semantics { role = Role.RadioButton },
                            Modifier.semantics { role = Role.RadioButton },
                        )

                    options.forEachIndexed { index, label ->
                        ToggleButton(
                            checked = selectedTab == index,
                            onCheckedChange = {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            modifier =
                                modifiers[index]
                                    .height(ButtonDefaults.ExtraSmallContainerHeight)
                                    .widthIn(min = 56.dp),
                            shapes =
                                when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    options.lastIndex ->
                                        ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                        ) {
                            Text(
                                text = label,
                                style =
                                    ButtonDefaults.textStyleFor(
                                        ButtonDefaults.ExtraSmallContainerHeight
                                    ),
                                color = LocalContentColor.current,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content by tab
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    flingBehavior =
                        PagerDefaults.flingBehavior(
                            state = pagerState,
                            snapAnimationSpec = spring(stiffness = Spring.StiffnessLow),
                        ),
                ) { page ->
                    when (page) {
                        0 -> {
                            // Now playing content
                            if (currentTrack != null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        modifier = Modifier.wrapContentHeight(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        val artModifier =
                                            Modifier.size(300.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .run {
                                                    if (
                                                        sharedTransitionScope != null &&
                                                            animatedVisibilityScope != null
                                                    ) {
                                                        with(sharedTransitionScope) {
                                                            sharedElement(
                                                                sharedContentState =
                                                                    rememberSharedContentState(
                                                                        key = "album_art"
                                                                    ),
                                                                animatedVisibilityScope =
                                                                    animatedVisibilityScope,
                                                                clipInOverlayDuringTransition =
                                                                    OverlayClip(
                                                                        MaterialTheme.shapes.medium
                                                                    ),
                                                            )
                                                        }
                                                    } else {
                                                        this
                                                    }
                                                }

                                        AsyncImage(
                                            model = currentTrack.imageUrl,
                                            contentDescription = currentTrack.title,
                                            modifier = artModifier,
                                            contentScale = ContentScale.Crop,
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Album title left-aligned, artist smaller + subdued
                                        Column(
                                            modifier = Modifier.fillMaxWidth(0.85f),
                                            horizontalAlignment = Alignment.Start,
                                        ) {
                                            val titleModifier =
                                                Modifier.run {
                                                    if (
                                                        sharedTransitionScope != null &&
                                                            animatedVisibilityScope != null
                                                    ) {
                                                        with(sharedTransitionScope) {
                                                            sharedElement(
                                                                sharedContentState =
                                                                    rememberSharedContentState(
                                                                        key = "track_title"
                                                                    ),
                                                                animatedVisibilityScope =
                                                                    animatedVisibilityScope,
                                                            )
                                                        }
                                                    } else {
                                                        this
                                                    }
                                                }

                                            Text(
                                                text = currentTrack.title,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Start,
                                                modifier =
                                                    Modifier.padding(top = 12.dp)
                                                        .then(titleModifier),
                                                color = Color.White,
                                            )
                                            val artistModifier =
                                                Modifier.run {
                                                    if (
                                                        sharedTransitionScope != null &&
                                                            animatedVisibilityScope != null
                                                    ) {
                                                        with(sharedTransitionScope) {
                                                            sharedElement(
                                                                sharedContentState =
                                                                    rememberSharedContentState(
                                                                        key = "track_artist"
                                                                    ),
                                                                animatedVisibilityScope =
                                                                    animatedVisibilityScope,
                                                            )
                                                        }
                                                    } else {
                                                        this
                                                    }
                                                }
                                            Text(
                                                text = currentTrack.artist,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Start,
                                                color = Color.White,
                                                modifier = artistModifier,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // tools labeled button toolbar above slider (horizontally
                                        // scrollable)
                                        val tools =
                                            listOf(
                                                "Sleep" to Icons.Filled.Schedule,
                                                "Favorite" to Icons.Filled.Favorite,
                                                "Download" to Icons.Filled.Download,
                                                "Like" to Icons.Filled.FavoriteBorder,
                                                "Share" to Icons.Filled.Share,
                                                "Save" to Icons.Filled.Save,
                                                "Add" to Icons.Filled.Add,
                                                "More" to Icons.Filled.MoreHoriz,
                                            )

                                        LazyRow(
                                            modifier =
                                                Modifier.padding(horizontal = 8.dp)
                                                    .fillMaxWidth()
                                                    .pointerInput(Unit) {
                                                        detectDragGestures { change, dragAmount ->
                                                            if (
                                                                abs(dragAmount.x) >
                                                                    abs(dragAmount.y)
                                                            ) {
                                                                change.consume()
                                                            }
                                                        }
                                                    },
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                        ) {
                                            items(tools) { tool ->
                                                OutlinedButton(
                                                    onClick = { /* TODO: implement action */ },
                                                    modifier =
                                                        Modifier.height(
                                                                ButtonDefaults
                                                                    .ExtraSmallContainerHeight
                                                            )
                                                            .semantics { role = Role.Button },
                                                    shapes = ButtonDefaults.shapes(),
                                                    contentPadding =
                                                        PaddingValues(
                                                            horizontal = 8.dp,
                                                            vertical = 0.dp,
                                                        ),
                                                    colors =
                                                        ButtonDefaults.outlinedButtonColors(
                                                            contentColor = LocalContentColor.current
                                                        ),
                                                    border =
                                                        BorderStroke(
                                                            1.dp,
                                                            LocalContentColor.current.copy(
                                                                alpha = 0.18f
                                                            ),
                                                        ),
                                                ) {
                                                    Icon(
                                                        imageVector = tool.second,
                                                        contentDescription = tool.first,
                                                        modifier =
                                                            Modifier.size(
                                                                ButtonDefaults.ExtraSmallIconSize
                                                            ),
                                                        tint = LocalContentColor.current,
                                                    )
                                                    Spacer(
                                                        modifier =
                                                            Modifier.width(
                                                                ButtonDefaults.ExtraSmallIconSpacing
                                                            )
                                                    )
                                                    Text(
                                                        text = tool.first,
                                                        style =
                                                            ButtonDefaults.textStyleFor(
                                                                ButtonDefaults
                                                                    .ExtraSmallContainerHeight
                                                            ),
                                                        color = LocalContentColor.current,
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Seek bar
                                        Column(
                                            modifier =
                                                Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                                        ) {
                                            Slider(
                                                value = progress,
                                                onValueChange = { /* TODO: Implement seek */ },
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    text = "0:00", // Placeholder
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                )
                                                Text(
                                                    text = "3:45", // Placeholder duration
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.8f),
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        // Playback controls with shuffle and repeat
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            // shuffle
                                            IconButton(
                                                onClick = { isShuffleEnabled = !isShuffleEnabled }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Shuffle,
                                                    contentDescription = "Shuffle",
                                                    tint =
                                                        if (isShuffleEnabled)
                                                            MaterialTheme.colorScheme.primary
                                                        else LocalContentColor.current,
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // prev / play / next row grouped
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                val prevModifier =
                                                    Modifier.size(48.dp).run {
                                                        if (
                                                            sharedTransitionScope != null &&
                                                                animatedVisibilityScope != null
                                                        ) {
                                                            with(sharedTransitionScope) {
                                                                sharedElement(
                                                                    sharedContentState =
                                                                        rememberSharedContentState(
                                                                            key = "btn_prev"
                                                                        ),
                                                                    animatedVisibilityScope =
                                                                        animatedVisibilityScope,
                                                                )
                                                            }
                                                        } else {
                                                            this
                                                        }
                                                    }
                                                IconButton(
                                                    onClick = { playbackViewModel.previousTrack() }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.SkipPrevious,
                                                        contentDescription = "Previous",
                                                        modifier = prevModifier,
                                                    )
                                                }
                                                val playModifier =
                                                    Modifier.size(72.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape,
                                                        )
                                                        .run {
                                                            if (
                                                                sharedTransitionScope != null &&
                                                                    animatedVisibilityScope != null
                                                            ) {
                                                                with(sharedTransitionScope) {
                                                                    sharedElement(
                                                                        sharedContentState =
                                                                            rememberSharedContentState(
                                                                                key =
                                                                                    "btn_play_pause"
                                                                            ),
                                                                        animatedVisibilityScope =
                                                                            animatedVisibilityScope,
                                                                    )
                                                                }
                                                            } else {
                                                                this
                                                            }
                                                        }

                                                IconButton(
                                                    onClick = {
                                                        playbackViewModel.togglePlayPause()
                                                    },
                                                    modifier = playModifier,
                                                ) {
                                                    Icon(
                                                        imageVector =
                                                            if (isPlaying) Icons.Filled.Pause
                                                            else Icons.Filled.PlayArrow,
                                                        contentDescription =
                                                            if (isPlaying) "Pause" else "Play",
                                                        modifier = Modifier.size(48.dp),
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                    )
                                                }
                                                val nextModifier =
                                                    Modifier.size(48.dp).run {
                                                        if (
                                                            sharedTransitionScope != null &&
                                                                animatedVisibilityScope != null
                                                        ) {
                                                            with(sharedTransitionScope) {
                                                                sharedElement(
                                                                    sharedContentState =
                                                                        rememberSharedContentState(
                                                                            key = "btn_next"
                                                                        ),
                                                                    animatedVisibilityScope =
                                                                        animatedVisibilityScope,
                                                                )
                                                            }
                                                        } else {
                                                            this
                                                        }
                                                    }
                                                IconButton(
                                                    onClick = { playbackViewModel.nextTrack() }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.SkipNext,
                                                        contentDescription = "Next",
                                                        modifier = nextModifier,
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // repeat
                                            IconButton(
                                                onClick = { repeatMode = (repeatMode + 1) % 3 }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Repeat,
                                                    contentDescription = "Repeat",
                                                    tint =
                                                        if (repeatMode != 0)
                                                            MaterialTheme.colorScheme.primary
                                                        else LocalContentColor.current,
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "No track selected",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White.copy(alpha = 0.9f),
                                    )
                                }
                            }
                        }

                        1 -> {
                            // Lyrics placeholder
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.align(Alignment.Center),
                                ) {
                                    Text(
                                        text = "Lyrics will appear here",
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Placeholder lyrics content.",
                                        color = Color.White.copy(alpha = 0.85f),
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Queue placeholder
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.align(Alignment.Center),
                                ) {
                                    Text(
                                        text = "Up next (queue)",
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Queue is empty.",
                                        color = Color.White.copy(alpha = 0.85f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
