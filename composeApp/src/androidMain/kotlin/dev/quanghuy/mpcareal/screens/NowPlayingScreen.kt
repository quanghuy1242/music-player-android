package dev.quanghuy.mpcareal.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
// Using Modifier.blur for cross-platform consistent blur behavior
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NowPlayingScreen(
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier,
) {
    val currentTrack = playbackViewModel.currentTrack
    val isPlaying = playbackViewModel.isPlaying
    val progress = 0.3f // Placeholder - in a real app, this would be calculated from currentPosition
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
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.36f))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
        }

        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            // Connected ToggleButton group (tabs) using ButtonGroup and connected shapes
            ButtonGroup(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                val options = listOf("Now Playing", "Lyrics", "Queue")
                val modifiers = listOf(Modifier.weight(1f), Modifier.weight(1f), Modifier.weight(1f))

                options.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = selectedTab == index,
                        onCheckedChange = { selectedTab = index },
                        modifier = modifiers[index].semantics { role = Role.RadioButton },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content by tab
            when (selectedTab) {
                0 -> {
                    // Now playing content
                    if (currentTrack != null) {
                        AsyncImage(
                            model = currentTrack.imageUrl,
                            contentDescription = currentTrack.title,
                            modifier = Modifier.size(300.dp).clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = currentTrack.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Text(
                            text = currentTrack.artist,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // tools labeled button toolbar above slider (horizontally scrollable)
                        val tools = listOf(
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
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            items(tools) { tool ->
                                OutlinedButton(
                                    onClick = { /* TODO: implement action */ },
                                    modifier = Modifier
                                        .height(ButtonDefaults.MinHeight)
                                        .semantics { role = Role.Button },
                                    shape = MaterialTheme.shapes.small,
                                    contentPadding = ButtonDefaults.SmallContentPadding,
                                    colors = ButtonDefaults.outlinedButtonColors(),
                                    border = ButtonDefaults.outlinedButtonBorder(),
                                ) {
                                    Icon(
                                        imageVector = tool.second,
                                        contentDescription = tool.first,
                                        modifier = Modifier.size(ButtonDefaults.ExtraSmallIconSize),
                                    )
                                    Spacer(modifier = Modifier.width(ButtonDefaults.ExtraSmallIconSpacing))
                                    Text(
                                        text = tool.first,
                                        style = ButtonDefaults.textStyleFor(ButtonDefaults.MinHeight),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Seek bar
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)) {
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "3:45", // Placeholder duration
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            IconButton(onClick = { isShuffleEnabled = !isShuffleEnabled }) {
                                Icon(
                                    imageVector = Icons.Filled.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // prev / play / next row grouped
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = { playbackViewModel.previousTrack() }) {
                                    Icon(imageVector = Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(48.dp))
                                }
                                IconButton(
                                    onClick = { playbackViewModel.togglePlayPause() },
                                    modifier = Modifier.size(72.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                                IconButton(onClick = { playbackViewModel.nextTrack() }) {
                                    Icon(imageVector = Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(48.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // repeat
                            IconButton(onClick = { repeatMode = (repeatMode + 1) % 3 }) {
                                Icon(
                                    imageVector = Icons.Filled.Repeat,
                                    contentDescription = "Repeat",
                                    tint = if (repeatMode != 0) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No track selected",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                1 -> {
                    // Lyrics placeholder
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                        Text(text = "Lyrics will appear here", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Placeholder lyrics content.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                else -> {
                    // Queue placeholder
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(40.dp)) {
                        Text(text = "Up next (queue)", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Queue is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}