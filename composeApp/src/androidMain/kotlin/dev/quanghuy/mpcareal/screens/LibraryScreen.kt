package dev.quanghuy.mpcareal.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.quanghuy.mpcareal.data.sampleAlbums
import dev.quanghuy.mpcareal.data.sampleTracks
import dev.quanghuy.mpcareal.models.Album
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun LibraryScreen(playbackViewModel: PlaybackViewModel) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState { 3 }
    val scope = rememberCoroutineScope()
    val tabs = listOf("Songs", "Albums", "Artists")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Option 1") },
                            onClick = { expanded = false },
                        )
                        DropdownMenuItem(
                            text = { Text("Option 2") },
                            onClick = { expanded = false },
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0),
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                flingBehavior =
                    PagerDefaults.flingBehavior(
                        state = pagerState,
                        snapAnimationSpec = spring(stiffness = Spring.StiffnessLow),
                    ),
            ) { page ->
                when (page) {
                    0 -> SongsTab(scrollBehavior, playbackViewModel)
                    1 -> AlbumsTab(scrollBehavior, playbackViewModel)
                    2 -> ArtistsTab(scrollBehavior)
                }
            }
        }
    }
}

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun SongsTab(scrollBehavior: TopAppBarScrollBehavior, playbackViewModel: PlaybackViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        items(20) { index -> Text("Song $index", modifier = Modifier.padding(16.dp)) }
    }
}

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun AlbumsTab(scrollBehavior: TopAppBarScrollBehavior, playbackViewModel: PlaybackViewModel) {
    val albums = remember { List(50) { sampleAlbums[it % sampleAlbums.size] } }
    val scope = rememberCoroutineScope()
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Modal Bottom Sheet
    if (showBottomSheet && selectedAlbum != null) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    AsyncImage(
                        model = selectedAlbum!!.imageUrl,
                        contentDescription = selectedAlbum!!.title,
                        modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = selectedAlbum!!.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedAlbum!!.artist,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Album Options with Icons
                listOf(
                        "Play Album" to Icons.AutoMirrored.Filled.PlaylistAdd,
                        "Add to Favorites" to Icons.Default.Favorite,
                        "Share" to Icons.Default.Share,
                        "Download" to Icons.Default.Download,
                    )
                    .forEach { (option, icon) ->
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            sheetState.hide()
                                            showBottomSheet = false
                                        }
                                        // Handle option selection
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = option,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = option, fontSize = 16.sp)
                        }
                    }
            }
        }
    }

    // Main Grid Content
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(albums) { album ->
            OutlinedCard(
                modifier =
                    Modifier.fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .combinedClickable(
                            onClick = {
                                // Play the first track from this album
                                val sampleTrack =
                                    sampleTracks[albums.indexOf(album) % sampleTracks.size]
                                playbackViewModel.playTrack(sampleTrack)
                            },
                            onLongClick = {
                                selectedAlbum = album
                                showBottomSheet = true
                            },
                        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column {
                    AsyncImage(
                        model = album.imageUrl,
                        contentDescription = album.title,
                        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop,
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = album.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = album.artist,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun ArtistsTab(scrollBehavior: TopAppBarScrollBehavior) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        items(20) { index -> Text("Artist $index", modifier = Modifier.padding(16.dp)) }
    }
}
