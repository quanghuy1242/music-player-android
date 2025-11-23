package dev.quanghuy.mpcareal.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.quanghuy.mpcareal.components.MediaPlaybackControlBar
import dev.quanghuy.mpcareal.data.sampleTracks
import dev.quanghuy.mpcareal.screens.HomeScreen
import dev.quanghuy.mpcareal.screens.LibraryScreen
import dev.quanghuy.mpcareal.screens.NowPlayingScreen
import dev.quanghuy.mpcareal.screens.PersonalScreen
import dev.quanghuy.mpcareal.screens.SearchScreen
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

enum class PlayerState {
    COLLAPSED,
    EXPANDED
}

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun AppNavigation() {
    val playbackViewModel: PlaybackViewModel = viewModel<PlaybackViewModel>()
    // using full-screen overlay instead of ModalBottomSheet for Now Playing.
    val navItems =
        listOf(
            "Library" to Icons.AutoMirrored.Filled.LibraryBooks,
            "Playlist" to Icons.AutoMirrored.Filled.QueueMusic,
            "Search" to Icons.Filled.Search,
            "Personal" to Icons.Filled.Person,
        )
    var selectedIndex by remember { mutableIntStateOf(0) }
    var previousIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedIndex) { previousIndex = selectedIndex }

    // Initialize with a sample track for demonstration
    LaunchedEffect(Unit) {
        if (playbackViewModel.currentTrack == null) {
            playbackViewModel.playTrack(sampleTracks.first())
        }
    }

    val transitionState = remember { SeekableTransitionState(PlayerState.COLLAPSED) }
    val transition = rememberTransition(transitionState)
    val scope = rememberCoroutineScope()

    // Sync ViewModel state to TransitionState
    LaunchedEffect(playbackViewModel.isPlayerExpanded) {
        if (playbackViewModel.isPlayerExpanded) {
            if (transitionState.currentState == PlayerState.COLLAPSED) {
                transitionState.animateTo(PlayerState.EXPANDED)
            }
        } else {
            if (transitionState.currentState == PlayerState.EXPANDED) {
                transitionState.animateTo(PlayerState.COLLAPSED)
            }
        }
    }

    // Sync TransitionState back to ViewModel (when animation finishes)
    LaunchedEffect(transition.currentState) {
        if (
            transition.currentState == PlayerState.EXPANDED && !playbackViewModel.isPlayerExpanded
        ) {
            playbackViewModel.togglePlayerExpanded()
        } else if (
            transition.currentState == PlayerState.COLLAPSED && playbackViewModel.isPlayerExpanded
        ) {
            playbackViewModel.togglePlayerExpanded()
        }
    }

    SharedTransitionLayout {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val heightPx = constraints.maxHeight.toFloat()

            // Helper to update transition based on drag delta
            fun updateTransition(delta: Float) {
                scope.launch {
                    val currentFraction = transitionState.fraction
                    val isCollapsed = transitionState.currentState == PlayerState.COLLAPSED
                    val isExpanded = transitionState.currentState == PlayerState.EXPANDED

                    if (isCollapsed && delta < 0) {
                        // Start expanding (Drag Up)
                        val newFraction = (currentFraction + (-delta / heightPx)).coerceIn(0f, 1f)
                        transitionState.seekTo(newFraction, targetState = PlayerState.EXPANDED)
                    } else if (isExpanded && delta > 0) {
                        // Start collapsing (Drag Down)
                        val newFraction = (currentFraction + (delta / heightPx)).coerceIn(0f, 1f)
                        transitionState.seekTo(newFraction, targetState = PlayerState.COLLAPSED)
                    } else if (transitionState.targetState != transitionState.currentState) {
                        // Already transitioning
                        val progressDelta =
                            if (transitionState.targetState == PlayerState.EXPANDED) {
                                -delta / heightPx
                            } else {
                                delta / heightPx
                            }
                        val newFraction = (currentFraction + progressDelta).coerceIn(0f, 1f)
                        transitionState.seekTo(newFraction)
                    }
                }
            }

            // Helper for drag stop (Snap logic)
            val onDragStopped: suspend (Float) -> Unit = { velocity ->
                val fraction = transitionState.fraction
                val targetState = transitionState.targetState

                // Simple snap logic: if > 50% or fast fling, complete. Else revert.
                // Since 'fraction' is progress towards 'targetState':
                // fraction 0.0 -> we are at start state
                // fraction 1.0 -> we are at target state

                val shouldComplete = fraction > 0.5f // Add velocity check if needed

                if (shouldComplete) {
                    transitionState.animateTo(targetState)
                } else {
                    // Revert to the *other* state (the one we started from)
                    // If target was EXPANDED, start was COLLAPSED.
                    val revertTarget = if (targetState == PlayerState.EXPANDED) PlayerState.COLLAPSED else PlayerState.EXPANDED
                    transitionState.animateTo(revertTarget)
                }
            }

            val draggableState = rememberDraggableState { delta ->
                updateTransition(delta)
            }

            Scaffold(
                floatingActionButton = {
                    // ToggleFloatingActionButton with menu
                    var expanded by rememberSaveable { mutableStateOf(false) }
                    // FAB Menu options (uses Material3 FloatingActionButtonMenu API)
                    FloatingActionButtonMenu(
                        expanded,
                        {
                            ToggleFloatingActionButton(
                                checked = expanded,
                                onCheckedChange = { expanded = !expanded },
                            ) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                            }
                        },
                        Modifier,
                        Alignment.End,
                    ) {
                        // Menu items: use 'icon' parameter (not leadingIcon)
                        FloatingActionButtonMenuItem(
                            onClick = { /* Handle shuffle action */ },
                            text = { Text("Shuffle and Play") },
                            icon = { Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle") },
                        )
                        FloatingActionButtonMenuItem(
                            onClick = { /* Handle surprise action */ },
                            text = { Text("Surprise Me") },
                            icon = {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Surprise")
                            },
                        )
                    }
                },
                bottomBar = {
                    Column {
                        // Only MiniPlayer is draggable
                        Box(
                            modifier = Modifier.draggable(
                                state = draggableState,
                                orientation = Orientation.Vertical,
                                onDragStopped = onDragStopped
                            )
                        ) {
                            transition.AnimatedVisibility(
                                visible = { it == PlayerState.COLLAPSED },
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                MediaPlaybackControlBar(
                                    currentTrack = playbackViewModel.currentTrack,
                                    isPlaying = playbackViewModel.isPlaying,
                                    onPlayPause = { playbackViewModel.togglePlayPause() },
                                    onNext = { playbackViewModel.nextTrack() },
                                    onPrevious = { playbackViewModel.previousTrack() },
                                    // Expand is now handled by drag, but click still works
                                    onExpand = { playbackViewModel.togglePlayerExpanded() },
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this,
                                )
                            }
                        }

                        // Navigation bar
                        NavigationBar {
                            navItems.forEachIndexed { index, (label, icon) ->
                                NavigationBarItem(
                                    selected = selectedIndex == index,
                                    onClick = { selectedIndex = index },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }
                },
            ) { paddingValues ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedContent<Int>(
                        targetState = selectedIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                // New screen from right, old to left with spring
                                slideInHorizontally(
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                    initialOffsetX = { width -> width },
                                ) togetherWith
                                    slideOutHorizontally(
                                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                                        targetOffsetX = { width -> -width },
                                    )
                            } else {
                                // New screen from left, old to right with spring
                                slideInHorizontally(
                                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                                    initialOffsetX = { width -> -width },
                                ) togetherWith
                                    slideOutHorizontally(
                                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                                        targetOffsetX = { width -> width },
                                    )
                            }
                        },
                        label = "ScreenTransition",
                    ) { index ->
                        when (index) {
                            0 -> LibraryScreen(playbackViewModel)
                            1 -> HomeScreen(playbackViewModel)
                            2 -> SearchScreen()
                            3 -> PersonalScreen()
                        }
                    }
                }
            }

            // Full Player is draggable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        onDragStopped = onDragStopped,
                        enabled = transitionState.currentState == PlayerState.EXPANDED || transitionState.targetState == PlayerState.EXPANDED
                    )
            ) {
                transition.AnimatedVisibility(
                    visible = { it == PlayerState.EXPANDED },
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    NowPlayingScreen(
                        playbackViewModel,
                        modifier = Modifier.fillMaxSize(),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }
    }
}
