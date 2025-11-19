package dev.quanghuy.mpcareal.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import dev.quanghuy.mpcareal.data.sampleAlbums
import dev.quanghuy.mpcareal.data.sampleTracks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Combine tracks and albums for search
    val allItems = remember {
        (sampleTracks.map { "${it.title} - ${it.artist}" } +
                sampleAlbums.map { "${it.title} - ${it.artist}" })
            .distinct()
    }

    // Filter items based on query
    val filteredItems =
        remember(query) {
            if (query.isEmpty()) {
                allItems.take(5) // Show some suggestions when empty
            } else {
                allItems.filter { it.contains(query, ignoreCase = true) }
            }
        }

    Box(modifier = Modifier.fillMaxSize().semantics { isTraversalGroup = true }) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter).semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search tracks and albums") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            // Display search results
            LazyColumn {
                items(filteredItems.size) { index ->
                    val item = filteredItems[index]
                    ListItem(
                        headlineContent = { Text(item) },
                        modifier =
                            Modifier.clickable {
                                    query = item
                                    expanded = false
                                }
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
