package dev.quanghuy.mpcareal.data

import dev.quanghuy.mpcareal.models.Album
import dev.quanghuy.mpcareal.models.Artist
import dev.quanghuy.mpcareal.models.Track

val sampleTracks =
    listOf(
        Track(
            title = "Nữ Thần Mất Trăng",
            artist = "Bùi Lan Hương",
            imageUrl =
                "https://contents.quanghuy.dev/118CD291-17C4-4E0E-B51C-D8504A57E4D5_sk1.jpeg",
            duration = 210000L,
        ),
        Track(
            title = "The Human Era",
            artist = "Epic Mountain",
            imageUrl =
                "https://contents.quanghuy.dev/35F87834-A50F-40FB-9F76-E994D99D2656_sk1.jpeg",
            duration = 180000L,
        ),
        Track(
            title = "Thiên Thần Sa Ngã",
            artist = "Bùi Lan Hương",
            imageUrl =
                "https://contents.quanghuy.dev/60080A59-43AF-448E-99C1-85887045E5DC_sk1.jpeg",
            duration = 195000L,
        ),
        Track(
            title = "Lust for Life",
            artist = "Lana Del Rey",
            imageUrl =
                "https://contents.quanghuy.dev/73494CD3-B6D7-4931-8978-CD3E3C6EC7EF_sk1.jpeg",
            duration = 240000L,
        ),
        Track(
            title = "Firewatch Theme",
            artist = "Chris Remo",
            imageUrl =
                "https://contents.quanghuy.dev/79EEE411-BF3C-4F63-BD5E-39C673FFA737_sk1.jpeg",
            duration = 165000L,
        ),
    )

val expandedSampleTracks by lazy { generateSampleTracks(500) }

fun generateSampleTracks(count: Int): List<Track> {
    val baseTracks = sampleTracks
    return (1..count).map { i ->
        val base = baseTracks[(i - 1) % baseTracks.size]
        base.copy(
            title = "${base.title} ($i)",
            artist = "${base.artist} ($i)"
        )
    }
}

val expandedSampleAlbums by lazy { generateSampleAlbums(500) }

fun generateSampleAlbums(count: Int): List<Album> {
    val baseAlbums = sampleAlbums
    return (1..count).map { i ->
        val base = baseAlbums[(i - 1) % baseAlbums.size]
        base.copy(
            title = "${base.title} ($i)",
            artist = "${base.artist} ($i)"
        )
    }
}

val expandedSampleArtists by lazy { generateSampleArtists(500) }

fun generateSampleArtists(count: Int): List<Artist> {
    val baseArtists = sampleArtists
    return (1..count).map { i ->
        val base = baseArtists[(i - 1) % baseArtists.size]
        base.copy(name = "${base.name} ($i)")
    }
}

val sampleAlbums =
    listOf(
        Album(
            title = "Nữ Thần Mất Trăng (Mônangel)",
            artist = "Bùi Lan Hương",
            imageUrl =
                "https://contents.quanghuy.dev/118CD291-17C4-4E0E-B51C-D8504A57E4D5_sk1.jpeg",
            genre = "Pop",
        ),
        Album(
            title = "The Human Era (Original Soundtrack)",
            artist = "Epic Mountain",
            imageUrl =
                "https://contents.quanghuy.dev/35F87834-A50F-40FB-9F76-E994D99D2656_sk1.jpeg",
            genre = "Soundtrack",
        ),
        Album(
            title = "Thiên Thần Sa Ngã",
            artist = "Bùi Lan Hương",
            imageUrl =
                "https://contents.quanghuy.dev/60080A59-43AF-448E-99C1-85887045E5DC_sk1.jpeg",
            genre = "Pop",
        ),
        Album(
            title = "Lust for Life",
            artist = "Lana Del Rey",
            imageUrl =
                "https://contents.quanghuy.dev/73494CD3-B6D7-4931-8978-CD3E3C6EC7EF_sk1.jpeg",
            genre = "Pop",
        ),
        Album(
            title = "Firewatch (Original Soundtrack)",
            artist = "Chris Remo",
            imageUrl =
                "https://contents.quanghuy.dev/79EEE411-BF3C-4F63-BD5E-39C673FFA737_sk1.jpeg",
            genre = "Soundtrack",
        ),
    )

val sampleArtists =
    listOf(
        Artist(
            name = "Bùi Lan Hương",
            imageUrl =
                "https://contents.quanghuy.dev/118CD291-17C4-4E0E-B51C-D8504A57E4D5_sk1.jpeg",
        ),
        Artist(
            name = "Epic Mountain",
            imageUrl =
                "https://contents.quanghuy.dev/35F87834-A50F-40FB-9F76-E994D99D2656_sk1.jpeg",
        ),
        Artist(
            name = "Lana Del Rey",
            imageUrl =
                "https://contents.quanghuy.dev/73494CD3-B6D7-4931-8978-CD3E3C6EC7EF_sk1.jpeg",
        ),
        Artist(
            name = "Chris Remo",
            imageUrl =
                "https://contents.quanghuy.dev/79EEE411-BF3C-4F63-BD5E-39C673FFA737_sk1.jpeg",
        ),
    )
