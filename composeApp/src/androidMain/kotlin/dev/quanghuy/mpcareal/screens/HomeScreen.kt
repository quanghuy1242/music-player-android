package dev.quanghuy.mpcareal.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.quanghuy.mpcareal.Greeting
import dev.quanghuy.mpcareal.viewmodel.PlaybackViewModel
import mpcareal.composeapp.generated.resources.Res
import mpcareal.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeScreen(playbackViewModel: PlaybackViewModel) {
    var showContent by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
