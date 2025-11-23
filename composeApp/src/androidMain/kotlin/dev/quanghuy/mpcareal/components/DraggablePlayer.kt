package dev.quanghuy.mpcareal.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.quanghuy.mpcareal.screens.NowPlayingScreen
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

enum class PlayerState {
    Collapsed,
    Expanded,
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DraggablePlayer(
    playbackViewModel: PlaybackViewModel,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // SeekableTransitionState to control the transition progress
    val transitionState = remember { SeekableTransitionState(PlayerState.Collapsed) }

    // Dimensions
    val miniPlayerHeight = 80.dp
    val navBarHeight = 80.dp // Approximate/Standard height.
    val collapsedHeight = miniPlayerHeight + navBarHeight

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val constraintsScope = this
        val maxHeightPx = with(density) { constraintsScope.maxHeight.toPx() }
        val miniPlayerHeightPx = with(density) { miniPlayerHeight.toPx() }
        val navBarHeightPx = with(density) { navBarHeight.toPx() }

        val collapsedOffset = maxHeightPx - (miniPlayerHeightPx + navBarHeightPx)

        val anchors = DraggableAnchors {
            PlayerState.Expanded at 0f
            PlayerState.Collapsed at collapsedOffset
        }

        val draggableState = remember(anchors) {
            AnchoredDraggableState(
                initialValue = PlayerState.Collapsed,
                anchors = anchors,
                positionalThreshold = { distance: Float -> distance * 0.5f },
                velocityThreshold = { with(density) { 100.dp.toPx() } },
                animationSpec = spring(),
            )
        }

        // Sync Draggable State to SeekableTransitionState
        LaunchedEffect(draggableState.offset) {
            if (!collapsedOffset.isNaN() && collapsedOffset > 0) {
                val offset = draggableState.offset
                if (offset.isNaN()) return@LaunchedEffect

                // Fraction: 0 = Collapsed, 1 = Expanded.
                // Offset: Max = Collapsed, 0 = Expanded.
                val fraction = 1f - (offset / collapsedOffset).coerceIn(0f, 1f)

                // We always seek towards Expanded to visualize the progress
                transitionState.seekTo(fraction, PlayerState.Expanded)
            }
        }

        // Sync external changes
        LaunchedEffect(playbackViewModel.isPlayerExpanded) {
            val target = if (playbackViewModel.isPlayerExpanded) PlayerState.Expanded else PlayerState.Collapsed
            if (draggableState.currentValue != target) {
                 draggableState.animateTo(target)
            }
        }

        // Update ViewModel on settle
        LaunchedEffect(draggableState.currentValue) {
             playbackViewModel.setPlayerExpandedState(draggableState.currentValue == PlayerState.Expanded)
        }

        // 1. Main Content (Scaffold)
        // We only provide padding for the Mini Player itself.
        // The NavBar height is usually handled by the Scaffold's internal padding if the NavBar is in the Scaffold.
        // But here we are wrapping the Scaffold.
        // In AppNavigation, the NavigationBar IS in the Scaffold.
        // So 'content' (Scaffold) will have internal padding for the NavBar.
        // We need to add padding for the Mini Player which sits ON TOP of the content (above NavBar).
        content(PaddingValues(bottom = miniPlayerHeight))

        // 2. Player Layer
        val currentOffset = if (draggableState.offset.isNaN()) collapsedOffset else draggableState.offset
        val currentHeightPx = (maxHeightPx - currentOffset).coerceAtLeast(miniPlayerHeightPx)
        val currentHeight = with(density) { currentHeightPx.toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentHeight)
                .align(Alignment.BottomCenter)
                .anchoredDraggable(draggableState, Orientation.Vertical)
        ) {
             // Shared Transition Content
             if (playbackViewModel.currentTrack != null) {
                 val transition = rememberTransition(transitionState)
                 transition.AnimatedContent(
                     transitionSpec = {
                         fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                     },
                     modifier = Modifier.fillMaxSize()
                 ) { state ->
                     if (state == PlayerState.Collapsed) {
                         Box(
                             modifier = Modifier.fillMaxSize(),
                             contentAlignment = Alignment.BottomCenter
                         ) {
                             MediaPlaybackControlBar(
                                 currentTrack = playbackViewModel.currentTrack,
                                 isPlaying = playbackViewModel.isPlaying,
                                 onPlayPause = { playbackViewModel.togglePlayPause() },
                                 onNext = { playbackViewModel.nextTrack() },
                                 onPrevious = { playbackViewModel.previousTrack() },
                                 onExpand = {
                                     // Animate to Expanded
                                     scope.launch { draggableState.animateTo(PlayerState.Expanded) }
                                 },
                                 // Add extra padding for NavBar space
                                 modifier = Modifier.padding(bottom = navBarHeight),
                                 sharedTransitionScope = sharedTransitionScope,
                                 animatedVisibilityScope = this@AnimatedContent
                             )
                         }
                     } else {
                         NowPlayingScreen(
                             playbackViewModel = playbackViewModel,
                             modifier = Modifier.fillMaxSize(),
                             sharedTransitionScope = sharedTransitionScope,
                             animatedVisibilityScope = this@AnimatedContent
                         )
                     }
                 }
             }
        }
    }
}
