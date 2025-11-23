package dev.quanghuy.mpcareal.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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

    SharedTransitionLayout {
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
                        icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "Surprise") },
                    )
                }
            },
            bottomBar = {
                Column {
                    AnimatedVisibility(
                        visible = !playbackViewModel.isPlayerExpanded,
                        enter = fadeIn() + slideInVertically { it },
                        exit = fadeOut() + slideOutVertically { it },
                        label = "MiniPlayer",
                    ) {
                        MediaPlaybackControlBar(
                            currentTrack = playbackViewModel.currentTrack,
                            isPlaying = playbackViewModel.isPlaying,
                            onPlayPause = { playbackViewModel.togglePlayPause() },
                            onNext = { playbackViewModel.nextTrack() },
                            onPrevious = { playbackViewModel.previousTrack() },
                            onExpand = { playbackViewModel.togglePlayerExpanded() },
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                        )
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

        AnimatedVisibility(
            visible = playbackViewModel.isPlayerExpanded,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            label = "NowPlaying",
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
