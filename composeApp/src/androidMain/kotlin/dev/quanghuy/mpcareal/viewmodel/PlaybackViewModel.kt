package dev.quanghuy.mpcareal.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.quanghuy.mpcareal.models.Track

class PlaybackViewModel : ViewModel() {
    var currentTrack by mutableStateOf<Track?>(null)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var currentPosition by mutableStateOf(0L)
        private set

    var isPlayerExpanded by mutableStateOf(false)
        private set

    data class MiniArtBounds(val x: Float, val y: Float, val width: Float, val height: Float)

    var miniArtBounds by mutableStateOf<MiniArtBounds?>(null)
        private set

    fun updateMiniArtBounds(x: Float, y: Float, width: Float, height: Float) {
        miniArtBounds = MiniArtBounds(x, y, width, height)
    }

    fun clearMiniArtBounds() {
        miniArtBounds = null
    }

    fun playTrack(track: Track) {
        currentTrack = track
        isPlaying = true
        currentPosition = 0L
    }

    fun togglePlayPause() {
        isPlaying = !isPlaying
    }

    fun nextTrack() {
        // In a real app, this would cycle through a playlist
        isPlaying = false
        currentPosition = 0L
    }

    fun previousTrack() {
        // In a real app, this would cycle through a playlist
        isPlaying = false
        currentPosition = 0L
    }

    fun togglePlayerExpanded() {
        isPlayerExpanded = !isPlayerExpanded
    }
}
