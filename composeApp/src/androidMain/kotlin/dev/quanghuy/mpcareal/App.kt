@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package dev.quanghuy.mpcareal

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.quanghuy.mpcareal.navigation.AppNavigation
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme { AppNavigation() }
}
