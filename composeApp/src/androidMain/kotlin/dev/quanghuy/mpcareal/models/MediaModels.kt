package dev.quanghuy.mpcareal.models

data class Album(val title: String, val artist: String, val imageUrl: String, val genre: String)

data class Track(
    val title: String,
    val artist: String,
    val imageUrl: String,
    val duration: Long = 180000L,
)

data class Artist(val name: String, val imageUrl: String)
