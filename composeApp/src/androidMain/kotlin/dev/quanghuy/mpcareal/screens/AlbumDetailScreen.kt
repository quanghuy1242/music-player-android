package dev.quanghuy.mpcareal.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import dev.quanghuy.mpcareal.data.expandedSampleTracks
import dev.quanghuy.mpcareal.models.Album
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun AlbumDetailScreen(
    album: Album,
    playbackViewModel: PlaybackViewModel,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(album.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0),
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        AlbumDetailContent(
            album = album,
            playbackViewModel = playbackViewModel,
            scrollBehavior = scrollBehavior,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            paddingValues = paddingValues,
        )
    }
}

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun AlbumDetailContent(
    album: Album,
    playbackViewModel: PlaybackViewModel,
    scrollBehavior: TopAppBarScrollBehavior,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    paddingValues: PaddingValues,
) {
    val tracks = remember {
        expandedSampleTracks.filter { it.artist == album.artist }
    } // Mock tracks for the album

    LazyColumn(
        modifier =
            Modifier.fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Album cover with shared transition
                val context = LocalContext.current
                val request =
                    ImageRequest.Builder(context)
                        .data(album.imageUrl)
                        .size(600) // Fixed size for cache consistency
                        .build()

                with(sharedTransitionScope) {
                    AsyncImage(
                        model = request,
                        contentDescription = album.title,
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(16.dp)
                                .aspectRatio(1f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(Color.Gray.copy(alpha = 0.1f))
                                .sharedElement(
                                    rememberSharedContentState(key = "album-${album.title}"),
                                    animatedVisibilityScope,
                                    clipInOverlayDuringTransition =
                                        OverlayClip(MaterialTheme.shapes.medium),
                                ),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color.Gray.copy(alpha = 0.2f)),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Album info
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = album.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = album.artist,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = album.genre, fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Play buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = {
                                // Play all tracks
                                if (tracks.isNotEmpty()) {
                                    playbackViewModel.playTrack(tracks.first())
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Play")
                        }
                        OutlinedButton(
                            onClick = { /* Shuffle */ },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Shuffle, contentDescription = "Shuffle")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Shuffle")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Tracks list
        items(tracks, key = { it.title }) { track ->
            ListItem(
                headlineContent = { Text(track.title) },
                supportingContent = {
                    Text(
                        "${track.artist} • ${track.duration / 1000 / 60}:${(track.duration / 1000 % 60).toString().padStart(2, '0')}"
                    )
                },
                leadingContent = {
                    Text(
                        text = (tracks.indexOf(track) + 1).toString(),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.clickable { playbackViewModel.playTrack(track) },
            )
        }
    }
}
