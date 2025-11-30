package dev.quanghuy.mpcareal.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.quanghuy.mpcareal.models.Track

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MediaPlaybackControlBar(
    currentTrack: Track?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val progress =
        if (currentTrack != null) {
            0.3f // Placeholder - in a real app, this would be (currentPosition / duration)
        } else {
            0f
        }

    if (currentTrack != null) {
        var totalDragY by remember { mutableFloatStateOf(0f) }
        var isSwiping by remember { mutableStateOf(false) }

        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                totalDragY = 0f
                                isSwiping = false
                            },
                            onDragEnd = {
                                totalDragY = 0f
                                isSwiping = false
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            totalDragY += dragAmount
                            if (!isSwiping && totalDragY < -20f) { // Threshold for swipe up
                                onExpand()
                                isSwiping = true
                            }
                        }
                    }
                    .clickable(onClick = onExpand),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape =
                MaterialTheme.shapes.extraSmall.copy(
                    bottomStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp),
                ),
        ) {
            Box {
                // Progress indicator as top border
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth(progress)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.primary)
                    )
                }

                // Main content
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Album cover
                    val artModifier =
                        Modifier.size(48.dp).clip(MaterialTheme.shapes.medium).run {
                            if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                                with(sharedTransitionScope) {
                                    sharedElement(
                                        sharedContentState =
                                            rememberSharedContentState(key = "album_art"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        clipInOverlayDuringTransition =
                                            OverlayClip(MaterialTheme.shapes.medium),
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

                    Spacer(modifier = Modifier.width(12.dp))

                    // Track info
                    Column(modifier = Modifier.weight(1f)) {
                        val titleModifier =
                            Modifier.run {
                                if (
                                    sharedTransitionScope != null && animatedVisibilityScope != null
                                ) {
                                    with(sharedTransitionScope) {
                                        sharedElement(
                                            sharedContentState =
                                                rememberSharedContentState(key = "track_title"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        )
                                    }
                                } else {
                                    this
                                }
                            }
                        Text(
                            text = currentTrack.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = titleModifier,
                        )
                        val artistModifier =
                            Modifier.run {
                                if (
                                    sharedTransitionScope != null && animatedVisibilityScope != null
                                ) {
                                    with(sharedTransitionScope) {
                                        sharedElement(
                                            sharedContentState =
                                                rememberSharedContentState(key = "track_artist"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        )
                                    }
                                } else {
                                    this
                                }
                            }
                        Text(
                            text = currentTrack.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = artistModifier,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Playback controls
                    Row {
                        val prevModifier =
                            Modifier.size(24.dp).run {
                                if (
                                    sharedTransitionScope != null && animatedVisibilityScope != null
                                ) {
                                    with(sharedTransitionScope) {
                                        sharedElement(
                                            sharedContentState =
                                                rememberSharedContentState(key = "btn_prev"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        )
                                    }
                                } else {
                                    this
                                }
                            }
                        IconButton(onClick = onPrevious) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = prevModifier,
                            )
                        }

                        val playModifier =
                            Modifier.size(44.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .run {
                                    if (
                                        sharedTransitionScope != null &&
                                            animatedVisibilityScope != null
                                    ) {
                                        with(sharedTransitionScope) {
                                            sharedElement(
                                                sharedContentState =
                                                    rememberSharedContentState(
                                                        key = "btn_play_pause"
                                                    ),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                            )
                                        }
                                    } else {
                                        this
                                    }
                                }
                        IconButton(onClick = onPlayPause, modifier = playModifier) {
                            Icon(
                                imageVector =
                                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        val nextModifier =
                            Modifier.size(24.dp).run {
                                if (
                                    sharedTransitionScope != null && animatedVisibilityScope != null
                                ) {
                                    with(sharedTransitionScope) {
                                        sharedElement(
                                            sharedContentState =
                                                rememberSharedContentState(key = "btn_next"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        )
                                    }
                                } else {
                                    this
                                }
                            }
                        IconButton(onClick = onNext) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                modifier = nextModifier,
                            )
                        }
                    }
                }
            }
        }
    }
}
