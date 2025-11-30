package dev.quanghuy.mpcareal.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import dev.quanghuy.mpcareal.data.expandedSampleAlbums
import dev.quanghuy.mpcareal.data.expandedSampleArtists
import dev.quanghuy.mpcareal.data.expandedSampleTracks
import dev.quanghuy.mpcareal.models.Album
import dev.quanghuy.mpcareal.models.Track
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel
import kotlinx.coroutines.launch

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun LibraryScreen(
    playbackViewModel: PlaybackViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pagerState = rememberPagerState { 3 }
    val albumsGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val tabs = listOf("Songs", "Albums", "Artists")
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }

    BackHandler(enabled = selectedAlbum != null) { selectedAlbum = null }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = selectedAlbum,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                },
                label = "TopBarTransition",
            ) { album ->
                if (album != null) {
                    TopAppBar(
                        title = { Text(album.title) },
                        navigationIcon = {
                            IconButton(onClick = { selectedAlbum = null }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                )
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
                } else {
                    TopAppBar(
                        title = { Text("Library") },
                        actions = {
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ) {
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
                }
            }
        },
        contentWindowInsets = WindowInsets(0),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            SharedTransitionLayout {
                val localScope = this
                AnimatedContent(
                    targetState = selectedAlbum,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                    },
                    label = "LibraryToAlbumTransition",
                ) { album ->
                    val localAnimatedVisibilityScope = this
                    if (album != null) {
                        AlbumDetailContent(
                            album = album,
                            playbackViewModel = playbackViewModel,
                            scrollBehavior = scrollBehavior,
                            sharedTransitionScope = localScope,
                            animatedVisibilityScope = localAnimatedVisibilityScope,
                            paddingValues = PaddingValues(0.dp),
                        )
                    } else {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize().padding(top = 48.dp),
                            flingBehavior =
                                PagerDefaults.flingBehavior(
                                    state = pagerState,
                                    snapAnimationSpec = spring(stiffness = Spring.StiffnessLow),
                                ),
                        ) { page ->
                            when (page) {
                                0 -> SongsTab(scrollBehavior, playbackViewModel)
                                1 ->
                                    AlbumsTab(
                                        scrollBehavior,
                                        playbackViewModel,
                                        onAlbumClick = { album -> selectedAlbum = album },
                                        sharedTransitionScope = localScope,
                                        animatedVisibilityScope = localAnimatedVisibilityScope,
                                        state = albumsGridState,
                                    )
                                2 -> ArtistsTab(scrollBehavior)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedAlbum == null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title) },
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
fun SongsTab(scrollBehavior: TopAppBarScrollBehavior, playbackViewModel: PlaybackViewModel) {
    val scope = rememberCoroutineScope()
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    // Modal Bottom Sheet
    if (showBottomSheet && selectedTrack != null) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    AsyncImage(
                        model = selectedTrack!!.imageUrl,
                        contentDescription = selectedTrack!!.title,
                        modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = selectedTrack!!.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedTrack!!.artist,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Song Options with Icons
                listOf(
                        "Play Song" to Icons.Default.PlayArrow,
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

    // Main List Content
    LazyColumn(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        items(expandedSampleTracks, key = { it.title }) { track ->
            ListItem(
                headlineContent = { Text(track.title) },
                supportingContent = { Text(track.artist) },
                leadingContent = {
                    AsyncImage(
                        model = track.imageUrl,
                        contentDescription = track.title,
                        modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop,
                    )
                },
                modifier =
                    Modifier.combinedClickable(
                        onClick = { playbackViewModel.playTrack(track) },
                        onLongClick = {
                            selectedTrack = track
                            showBottomSheet = true
                        },
                    ),
            )
        }
    }
}

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun AlbumsTab(
    scrollBehavior: TopAppBarScrollBehavior,
    playbackViewModel: PlaybackViewModel,
    onAlbumClick: (Album) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    state: LazyGridState,
) {
    val albums = remember { expandedSampleAlbums }
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
        state = state,
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(albums, key = { it.title }) { album ->
            with(sharedTransitionScope) {
                OutlinedCard(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .combinedClickable(
                                onClick = { onAlbumClick(album) },
                                onLongClick = {
                                    selectedAlbum = album
                                    showBottomSheet = true
                                },
                            ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column {
                        val context = LocalContext.current
                        val request =
                            ImageRequest.Builder(context)
                                .data(album.imageUrl)
                                .size(600) // Fixed size for cache consistency
                                .build()

                        AsyncImage(
                            model = request,
                            contentDescription = album.title,
                            modifier =
                                Modifier.fillMaxWidth()
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
        items(expandedSampleArtists, key = { it.name }) { artist ->
            ListItem(
                headlineContent = { Text(artist.name) },
                leadingContent = {
                    AsyncImage(
                        model = artist.imageUrl,
                        contentDescription = artist.name,
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                },
                modifier =
                    Modifier.clickable {
                        // TODO: Navigate to artist detail or play artist songs
                    },
            )
        }
    }
}
