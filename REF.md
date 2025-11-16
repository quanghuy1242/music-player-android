```
app.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package dev.quanghuy.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Removed platform-specific imports; use expect/actual loader instead
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween

import mpca.composeapp.generated.resources.compose_multiplatform

expect @Composable fun HorizontalFloatingToolbar(
    expanded: Boolean,
    floatingActionButton: @Composable () -> Unit,
    modifier: Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 300),
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
)

private sealed interface Screen {
    object Home : Screen
    object Albums : Screen
}

enum class SortBy { Title, Artist }

data class Album(val title: String, val artist: String, val imageUrl: String, val genre: String)

private val sampleAlbums = listOf(
    Album(
        title = "Nữ Thần Mất Trăng (Mônangel)",
        artist = "Bùi Lan Hương",
        imageUrl = "https://contents.quanghuy.dev/118CD291-17C4-4E0E-B51C-D8504A57E4D5_sk1.jpeg",
        genre = "Pop"
    ),
    Album(
        title = "The Human Era (Original Soundtrack)",
        artist = "Epic Mountain",
        imageUrl = "https://contents.quanghuy.dev/35F87834-A50F-40FB-9F76-E994D99D2656_sk1.jpeg",
        genre = "Soundtrack"
    ),
    Album(
        title = "Thiên Thần Sa Ngã",
        artist = "Bùi Lan Hương",
        imageUrl = "https://contents.quanghuy.dev/60080A59-43AF-448E-99C1-85887045E5DC_sk1.jpeg",
        genre = "Pop"
    ),
    Album(
        title = "Lust for Life",
        artist = "Lana Del Rey",
        imageUrl = "https://contents.quanghuy.dev/73494CD3-B6D7-4931-8978-CD3E3C6EC7EF_sk1.jpeg",
        genre = "Pop"
    ),
    Album(
        title = "Firewatch (Original Soundtrack)",
        artist = "Chris Remo",
        imageUrl = "https://contents.quanghuy.dev/79EEE411-BF3C-4F63-BD5E-39C673FFA737_sk1.jpeg",
        genre = "Soundtrack"
    ),
)

// RemoteImage moved to platform-specific implementations: expect/actual in commonMain/androidMain/iosMain


@Composable
@Preview
fun App() {
    MaterialTheme {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
        val snackbarHostState = remember { SnackbarHostState() }

        // Albums filter state (lift state up so TopAppBar can control it)
        val allAlbums = remember { List(500) { sampleAlbums[it % sampleAlbums.size] } }
        var filteredAlbums by remember { mutableStateOf(allAlbums) }
        // use top-level SortBy enum
        var sortBy by remember { mutableStateOf(SortBy.Title) }
        val allGenres = remember { sampleAlbums.map { it.genre }.distinct() }
        var selectedGenres by remember { mutableStateOf(allGenres.toSet()) }
        var filterMenuExpanded by remember { mutableStateOf(false) }
                var showToolbar by remember { mutableStateOf(false) }
        var selectedAlbum by remember { mutableStateOf<Album?>(null) }
        fun applyFilters() {
            filteredAlbums = allAlbums.filter { selectedGenres.isEmpty() || selectedGenres.contains(it.genre) }
                .let { list ->
                    when (sortBy) {
                        SortBy.Title -> list.sortedBy { it.title }
                        SortBy.Artist -> list.sortedBy { it.artist }
                    }
                }
        }

        // Player state
        var playlist by remember { mutableStateOf(allAlbums) }
        var playerIndex by remember { mutableStateOf<Int?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var progressFrac by remember { mutableStateOf(0f) } // 0..1

        // Simulate playback progress when playing
        LaunchedEffect(isPlaying, playerIndex) {
            while (isPlaying) {
                kotlinx.coroutines.delay(1000)
                progressFrac += 1f / 300f // simulate 5-minute track => 300s
                if (progressFrac >= 1f) {
                    progressFrac = 0f
                    // move to next
                    if (!playlist.isEmpty()) playerIndex = (playerIndex ?: 0 + 1) % playlist.size
                }
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(Modifier.height(20.dp))
                    NavigationDrawerItem(
                        label = { Text("Home") },
                        selected = currentScreen is Screen.Home,
                        onClick = {
                            currentScreen = Screen.Home
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    NavigationDrawerItem(
                        label = { Text("Albums") },
                        selected = currentScreen is Screen.Albums,
                        onClick = {
                            currentScreen = Screen.Albums
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.PhotoAlbum, contentDescription = "Albums") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    // Use TopAppBar which is available across material3 versions
                    TopAppBar(
                        title = { Text(text = when (currentScreen) {
                            Screen.Home -> "Home"
                            Screen.Albums -> "Albums"
                        }) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open drawer")
                            }
                        },
                        actions = {
                            if (currentScreen is Screen.Albums) {
                                IconButton(onClick = { filterMenuExpanded = true }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                                }
                                DropdownMenu(
                                    expanded = filterMenuExpanded,
                                    onDismissRequest = { filterMenuExpanded = false }
                                ) {
                                    // Sort by header
                                    DropdownMenuItem(text = { Text("Sort by") }, onClick = { /* no-op */ })
                                    DropdownMenuItem(text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = sortBy == SortBy.Title, onClick = { sortBy = SortBy.Title; applyFilters() })
                                            Spacer(Modifier.width(8.dp))
                                            Text("Title")
                                        }
                                    }, onClick = {
                                        sortBy = SortBy.Title
                                        applyFilters()
                                        filterMenuExpanded = false
                                    })
                                    DropdownMenuItem(text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = sortBy == SortBy.Artist, onClick = { sortBy = SortBy.Artist; applyFilters() })
                                            Spacer(Modifier.width(8.dp))
                                            Text("Artist")
                                        }
                                    }, onClick = {
                                        sortBy = SortBy.Artist
                                        applyFilters()
                                        filterMenuExpanded = false
                                    })
                                    Divider()
                                    // Genres
                                    DropdownMenuItem(text = { Text("Genres") }, onClick = { /* no-op */ })
                                    allGenres.forEach { g ->
                                        DropdownMenuItem(text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = selectedGenres.contains(g), onCheckedChange = { checked ->
                                                    selectedGenres = if (checked) selectedGenres + g else selectedGenres - g
                                                    applyFilters()
                                                })
                                                Spacer(Modifier.width(8.dp))
                                                Text(g)
                                            }
                                        }, onClick = {
                                            selectedGenres = if (selectedGenres.contains(g)) selectedGenres - g else selectedGenres + g
                                            applyFilters()
                                        })
                                    }
                                }
                            }
                        }
                    )
                }
                ,
                floatingActionButton = {
                    HorizontalFloatingToolbar(
                        expanded = showToolbar,
                        floatingActionButton = {
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FloatingActionButton(onClick = {
                                    filteredAlbums = filteredAlbums.shuffled()
                                    scope.launch { snackbarHostState.showSnackbar("Shuffled") }
                                }) { Icon(Icons.Default.Shuffle, contentDescription = "Shuffle") }
                            }
                        },
                        modifier = Modifier,
                        animationSpec = tween(durationMillis = 300),
                        content = {
                            IconButton(onClick = { showToolbar = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close")
                            }
                            IconButton(onClick = { selectedAlbum?.let { scope.launch { snackbarHostState.showSnackbar("Info: ${it.title}") } } }) {
                                Icon(Icons.Filled.Info, contentDescription = "Info")
                            }
                            IconButton(onClick = { selectedAlbum?.let { scope.launch { snackbarHostState.showSnackbar("Download: ${it.title}") } } }) {
                                Icon(Icons.Filled.Download, contentDescription = "Download")
                            }
                            IconButton(onClick = { selectedAlbum?.let { scope.launch { snackbarHostState.showSnackbar("Added to queue: ${it.title}") } } }) {
                                Icon(Icons.Filled.Add, contentDescription = "Add to queue")
                            }
                            FilledIconButton(onClick = { selectedAlbum?.let { scope.launch { snackbarHostState.showSnackbar("Play: ${it.title}") } } }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                            }
                        }
                    )
                },
                bottomBar = {
                    // Persistent player bar if a track is selected or always shown on Albums
                    Box(Modifier.fillMaxWidth()) {
                        PlayerBar(
                            current = playerIndex?.let { playlist[it] },
                            isPlaying = isPlaying,
                            progress = progressFrac,
                            onPlayPause = {
                                if (playerIndex == null && playlist.isNotEmpty()) playerIndex = 0
                                isPlaying = !isPlaying
                            },
                            onNext = {
                                if (!playlist.isEmpty()) {
                                    playerIndex = (playerIndex ?: 0).let { (it + 1) % playlist.size }
                                    isPlaying = true
                                    progressFrac = 0f
                                }
                            },
                            onPrev = {
                                if (!playlist.isEmpty()) {
                                    playerIndex = (playerIndex ?: 0).let { (it - 1 + playlist.size) % playlist.size }
                                    isPlaying = true
                                    progressFrac = 0f
                                }
                            },
                            onSeek = { frac -> progressFrac = frac }
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                ) {
                    when (currentScreen) {
                        Screen.Home -> HomeScreen(onNavigateToAlbums = { currentScreen = Screen.Albums })
                        Screen.Albums -> AlbumsScreen(
                            albums = filteredAlbums,
                            onShuffle = { filteredAlbums = filteredAlbums.shuffled() },
                            onPlay = { title -> scope.launch { snackbarHostState.showSnackbar("Play: $title") } },
                            onLongPress = { album ->
                                selectedAlbum = album
                                showToolbar = !showToolbar
                            },
                            onApplyFilters = {
                                // re-evaluate filteredAlbums when sortBy and selectedGenres change
                                filteredAlbums = allAlbums.filter { selectedGenres.isEmpty() || selectedGenres.contains(it.genre) }
                                    .let { list ->
                                        when (sortBy) {
                                            SortBy.Title -> list.sortedBy { it.title }
                                            SortBy.Artist -> list.sortedBy { it.artist }
                                        }
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun HomeScreen(onNavigateToAlbums: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(24.dp))
        var count by remember { mutableStateOf(0) }
        Button(onClick = { count++ }) {
            Text("Increment")
        }
        Spacer(Modifier.height(8.dp))
        Text("Count: $count", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNavigateToAlbums) {
            Text("Go to Albums")
        }
        Spacer(Modifier.height(16.dp))
        AnimatedVisibility(true) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Image(painterResource(Res.drawable.compose_multiplatform), null, contentScale = ContentScale.Fit)
                Text("Compose: $greeting")
            }
        }
    }
}


@Composable
fun AlbumsScreen(
    albums: List<Album>,
    onShuffle: () -> Unit = {},
    onPlay: (String) -> Unit = {},
    onLongPress: (Album) -> Unit = {},
    onApplyFilters: () -> Unit = {},
) {
    // Build 500 album items by repeating sample data
    val albumItems = albums
    LazyVerticalGrid(
        // Adaptive grid - column count depends on screen width (min size 160.dp)
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(albumItems) { item ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onPlay(item.title) }
                    .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress(item) }) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column() {
                    // Cover image
                    RemoteImage(
                        url = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            //.height(220.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Spacer(Modifier.height(8.dp))
                        Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp, end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(2.dp))
                        Text(text = item.artist, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, end = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}


@Composable
fun PlayerBar(
    current: Album?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Float) -> Unit,
) {
    Surface(
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(Modifier.fillMaxWidth().padding(8.dp)) {
            // Thin clickable progress bar that acts as the top border of the player
            var barWidth by remember { mutableStateOf(0f) }
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .onSizeChanged { barWidth = it.width.toFloat() }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (barWidth > 0f) {
                            val frac = (offset.x / barWidth).coerceIn(0f, 1f)
                            onSeek(frac)
                        }
                    }
                }
            ) {
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.primary)
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // thumbnail
                Box(modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant).clip(MaterialTheme.shapes.small), contentAlignment = Alignment.Center) {
                    current?.let {
                        RemoteImage(url = it.imageUrl, contentDescription = it.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(current?.title ?: "No track", fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(current?.artist ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onPrev) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous") }
                // larger play/pause FAB for emphasis
                Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    FloatingActionButton(onClick = onPlayPause, modifier = Modifier.size(56.dp)) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause"
                        )
                    }
                }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, contentDescription = "Next") }
            }
        }
    }
}
```

```
Toolbar.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package dev.quanghuy.*

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.HorizontalFloatingToolbar as MaterialHorizontalFloatingToolbar
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual @Composable fun HorizontalFloatingToolbar(
    expanded: Boolean,
    floatingActionButton: @Composable () -> Unit,
    modifier: Modifier,
    animationSpec: FiniteAnimationSpec<Float>,
    content: @Composable RowScope.() -> Unit
) {
    MaterialHorizontalFloatingToolbar(
        expanded = expanded,
        floatingActionButton = floatingActionButton,
        modifier = modifier,
        animationSpec = animationSpec,
        content = content
    )
}
```