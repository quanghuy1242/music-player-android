package dev.quanghuy.mpcareal.screens

// import androidx.compose.ui.unit.dp (duplicate removed)
// Using Modifier.blur for cross-platform consistent blur behavior
// import androidx.compose.ui.graphics.Color (duplicate removed)

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import coil3.compose.AsyncImage
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NowPlayingScreen(
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier,
    sheetProgress: Float = 1f, // 0f collapsed, 1f expanded
) {
    val currentTrack = playbackViewModel.currentTrack
    val isPlaying = playbackViewModel.isPlaying
    val progress =
        0.3f // Placeholder - in a real app, this would be calculated from currentPosition
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var isShuffleEnabled by rememberSaveable { mutableStateOf(false) }
    var repeatMode by rememberSaveable { mutableStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
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
                            onCheckedChange = { selectedTab = index },
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
                when (selectedTab) {
                    0 -> {
                        // Now playing content
                        if (currentTrack != null) {
                            val targetArtSize = lerp(48.dp, 300.dp, sheetProgress)
                            val artSize by animateDpAsState(targetValue = targetArtSize)
                            val contentAlignment =
                                if (sheetProgress > 0.85f) Alignment.Center else Alignment.TopCenter
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = contentAlignment,
                            ) {
                                Column(
                                    modifier = Modifier.wrapContentHeight(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    var fullArtX by remember { mutableFloatStateOf(0f) }
                                    var fullArtY by remember { mutableFloatStateOf(0f) }
                                    var fullArtW by remember { mutableFloatStateOf(0f) }
                                    var fullArtH by remember { mutableFloatStateOf(0f) }
                                    val mini = playbackViewModel.miniArtBounds
                                    val (targetTranslationX, targetTranslationY, targetScale) =
                                        if (mini == null || fullArtW == 0f || fullArtH == 0f) {
                                            Triple(0f, 0f, 1f)
                                        } else {
                                            val fullCenterX = fullArtX + fullArtW / 2f
                                            val fullCenterY = fullArtY + fullArtH / 2f
                                            val miniCenterX = mini.x + mini.width / 2f
                                            val miniCenterY = mini.y + mini.height / 2f
                                            // values will return via the Triple below
                                            val startScale =
                                                if (fullArtW > 0f) (mini.width / fullArtW) else 1f
                                            Triple(
                                                (miniCenterX - fullCenterX) * (1f - sheetProgress),
                                                (miniCenterY - fullCenterY) * (1f - sheetProgress),
                                                startScale + (1f - startScale) * sheetProgress,
                                            )
                                        }

                                    val animatedTx by
                                        animateFloatAsState(
                                            targetValue = targetTranslationX,
                                            animationSpec = tween(240),
                                        )
                                    val animatedTy by
                                        animateFloatAsState(
                                            targetValue = targetTranslationY,
                                            animationSpec = tween(240),
                                        )
                                    val animatedScale by
                                        animateFloatAsState(
                                            targetValue = targetScale,
                                            animationSpec = tween(240),
                                        )

                                    AsyncImage(
                                        model = currentTrack.imageUrl,
                                        contentDescription = currentTrack.title,
                                        modifier =
                                            Modifier.size(artSize)
                                                .clip(MaterialTheme.shapes.medium)
                                                .onGloballyPositioned { coords ->
                                                    val pos = coords.positionInWindow()
                                                    fullArtX = pos.x
                                                    fullArtY = pos.y
                                                    fullArtW = coords.size.width.toFloat()
                                                    fullArtH = coords.size.height.toFloat()
                                                }
                                                .graphicsLayer {
                                                    translationX = animatedTx
                                                    translationY = animatedTy
                                                    scaleX = animatedScale
                                                    scaleY = animatedScale
                                                },
                                        contentScale = ContentScale.Crop,
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Album title left-aligned, artist smaller + subdued
                                    Column(
                                        modifier = Modifier.fillMaxWidth(0.85f),
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        Text(
                                            text = currentTrack.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.padding(top = 12.dp),
                                            color = Color.White,
                                        )
                                        Text(
                                            text = currentTrack.artist,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Start,
                                            color = Color.White,
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
                                            Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                    ) {
                                        items(tools) { tool ->
                                            OutlinedButton(
                                                onClick = { /* TODO: implement action */ },
                                                modifier =
                                                    Modifier.height(
                                                            ButtonDefaults.ExtraSmallContainerHeight
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
                                                            ButtonDefaults.ExtraSmallContainerHeight
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
                                            IconButton(
                                                onClick = { playbackViewModel.previousTrack() }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.SkipPrevious,
                                                    contentDescription = "Previous",
                                                    modifier = Modifier.size(48.dp),
                                                )
                                            }
                                            IconButton(
                                                onClick = { playbackViewModel.togglePlayPause() },
                                                modifier =
                                                    Modifier.size(72.dp)
                                                        .background(
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape,
                                                        ),
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
                                            IconButton(
                                                onClick = { playbackViewModel.nextTrack() }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.SkipNext,
                                                    contentDescription = "Next",
                                                    modifier = Modifier.size(48.dp),
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
                            Text(
                                text = "No track selected",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White.copy(alpha = 0.9f),
                            )
                        }
                    }

                    1 -> {
                        // Lyrics placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
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

                    else -> {
                        // Queue placeholder
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                        ) {
                            Text(
                                text = "Up next (queue)",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Queue is empty.", color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
