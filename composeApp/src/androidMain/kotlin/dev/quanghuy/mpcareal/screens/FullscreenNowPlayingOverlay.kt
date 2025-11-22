package dev.quanghuy.mpcareal.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun FullscreenNowPlayingOverlay(
    show: Boolean,
    onDismiss: () -> Unit,
    content: @Composable (Float) -> Unit,
) {
    if (!show) return

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val dragOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    var isLeaving by remember { mutableStateOf(false) }
    val progressTarget =
        when {
            isLeaving -> 0f
            show -> 1f
            else -> 0f
        }
    val progress by animateFloatAsState(targetValue = progressTarget, animationSpec = tween(320))
    val offsetYValue = (1f - progress) * screenHeightPx + dragOffset.value
    val scrimAlpha = (progress * 0.6f * (1f - dragOffset.value / screenHeightPx)).coerceIn(0f, 0.6f)

    // Trigger parent onDismiss when the exit animation finishes
    LaunchedEffect(progress) {
        if (isLeaving && progress == 0f) {
            onDismiss()
            isLeaving = false
        }
    }

    // Use an overlay Box inside the root composition so coordinates align for shared-element
    // transitions
    Box(modifier = Modifier.fillMaxSize().zIndex(1f), contentAlignment = Alignment.BottomCenter) {
        // Scrim
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .pointerInput(Unit) { detectDragGestures { _, _ -> /* no-op */ } }
                    .clickable { isLeaving = true }
        )

        // sliding full-screen content anchored at bottom
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .offset { IntOffset(0, offsetYValue.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    val next =
                                        (dragOffset.value + dragAmount.y).coerceIn(
                                            0f,
                                            screenHeightPx,
                                        )
                                    dragOffset.snapTo(next)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val combined =
                                        (1f - progress) * screenHeightPx + dragOffset.value
                                    val shouldDismiss = combined > screenHeightPx * 0.25f
                                    if (shouldDismiss) {
                                        dragOffset.animateTo(
                                            screenHeightPx,
                                            animationSpec = tween(200),
                                        )
                                        isLeaving = true
                                    } else {
                                        dragOffset.animateTo(0f, animationSpec = tween(180))
                                    }
                                }
                            },
                        )
                    }
        ) {
            content(progress)
        }
    }
}
