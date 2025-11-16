@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package dev.quanghuy.mpcareal

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import mpcareal.composeapp.generated.resources.Res
import mpcareal.composeapp.generated.resources.compose_multiplatform

import kotlinx.coroutines.launch

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


@Composable
@Preview
fun App() {
    MaterialTheme {
        val navItems = listOf(
            "Home" to Icons.Filled.Home,
            "Library" to Icons.AutoMirrored.Filled.LibraryBooks,
            "Personal" to Icons.Filled.Person,
            "Settings" to Icons.Filled.Settings
        )
        var selectedIndex by remember { mutableIntStateOf(0) }
        var previousIndex by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedIndex) {
            previousIndex = selectedIndex
        }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    navItems.forEachIndexed { index, (label, icon) ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            AnimatedContent<Int>(
                targetState = selectedIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        // New screen from right, old to left with spring
                        slideInHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            initialOffsetX = { width -> width }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            targetOffsetX = { width -> -width }
                        )
                    } else {
                        // New screen from left, old to right with spring
                        slideInHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            initialOffsetX = { width -> -width }
                        ) togetherWith slideOutHorizontally(
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            targetOffsetX = { width -> width }
                        )
                    }
                },
                label = "ScreenTransition"
            ) { index ->
                when (index) {
                    0 -> HomeScreen()
                    1 -> LibraryScreen()
                    2 -> PersonalScreen()
                    3 -> SettingsScreen()
                }
            }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    var showContent by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home Page", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { showContent = !showContent }) {
            Icon(imageVector = Icons.Filled.Home, contentDescription = "Home")
            Text("Click me!!!")
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
    }
}

@Composable
fun LibraryScreen() {
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
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Option 1") },
                            onClick = { expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Option 2") },
                            onClick = { expanded = false }
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(title) }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapAnimationSpec = spring(stiffness = Spring.StiffnessLow)
                )
            ) { page ->
                when (page) {
                    0 -> SongsTab(scrollBehavior)
                    1 -> AlbumsTab(scrollBehavior)
                    2 -> ArtistsTab(scrollBehavior)
                }
            }
        }
    }
}

@Composable
fun SongsTab(scrollBehavior: TopAppBarScrollBehavior) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        items(20) { index ->
            Text("Song $index", modifier = Modifier.padding(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsTab(scrollBehavior: TopAppBarScrollBehavior) {
    val albums = remember { List(50) { sampleAlbums[it % sampleAlbums.size] } }
    val scope = rememberCoroutineScope()
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    // Modal Bottom Sheet
    if (showBottomSheet && selectedAlbum != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    AsyncImage(
                        model = selectedAlbum!!.imageUrl,
                        contentDescription = selectedAlbum!!.title,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = selectedAlbum!!.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedAlbum!!.artist,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Album Options with Icons
                listOf(
                    "Play Album" to Icons.AutoMirrored.Filled.PlaylistAdd,
                    "Add to Favorites" to Icons.Default.Favorite,
                    "Share" to Icons.Default.Share,
                    "Download" to Icons.Default.Download
                ).forEach { (option, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { 
                                    sheetState.hide()
                                    showBottomSheet = false
                                }
                                // Handle option selection
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = option,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
    
    // Main Grid Content
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(albums) { album ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .combinedClickable(
                        onClick = { /* TODO: play album */ },
                        onLongClick = {
                            selectedAlbum = album
                            showBottomSheet = true
                        }
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    AsyncImage(
                        model = album.imageUrl,
                        contentDescription = album.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = album.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = album.artist,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistsTab(scrollBehavior: TopAppBarScrollBehavior) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        items(20) { index ->
            Text("Artist $index", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun PersonalScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Personal Page", style = MaterialTheme.typography.headlineMedium)
        Text("Personal information here")
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Settings Page", style = MaterialTheme.typography.headlineMedium)
        Text("Settings options here")
    }
}