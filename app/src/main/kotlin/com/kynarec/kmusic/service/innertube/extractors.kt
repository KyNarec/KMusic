package com.kynarec.kmusic.service.innertube

import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongArtist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

fun searchSuggestions(input: String): Flow<String> = flow {
    val json = Json { ignoreUnknownKeys = true }
    val innerTubeClient = InnerTube(ClientName.WebRemix)

    try {
        val raw = innerTubeClient.getYoutubeMusicSearchSuggestion(
            input
        )

        val response = json.decodeFromString<SearchSuggestionsResponse>(raw)

        val suggestions = response.contents
            .asSequence() // Use sequence for efficiency with multiple chained operations
            .mapNotNull { it.searchSuggestionsSectionRenderer?.contents }
            .flatten()
            .mapNotNull { it.searchSuggestionRenderer?.suggestion?.runs }
            .map { runs -> runs.joinToString("") { it.text ?: "" } } // Join runs without spaces

        suggestions.forEach { suggestion ->
            emit(suggestion) // Emit each suggestion individually
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun getHighestDefinitionThumbnailFromPlayer(jsonString: String): String? {

    val json = Json {
        ignoreUnknownKeys = true // Safely ignore fields not defined in the data classes
        isLenient = true // Allow parsing of some non-standard JSON elements if present
    }

    return try {
        // 2. Deserialize the full JSON response
        val response = json.decodeFromString<FullResponse>(jsonString)

        // 3. Navigate the structure to get the list of thumbnails
        val thumbnails = response
            .videoDetails
            ?.thumbnail
            ?.thumbnails

        thumbnails?.forEach {
            println("Thumbnail found: ${it.url} (${it.width}x${it.height})")
        }

        // 4. Find the thumbnail with the largest area (width * height)
        val highestResThumbnail = thumbnails?.maxByOrNull { it.width * it.height }

        // 5. Return the URL
        highestResThumbnail?.url

    } catch (e: Exception) {
        // Handle JSON parsing errors gracefully
        println("Error during JSON deserialization or extraction: ${e.message}")
        null
    }
}

fun searchSongsFlow(query: String): Flow<Song> = flow {
    val json = Json { ignoreUnknownKeys = true }
    val innerTubeClient = InnerTube(ClientName.WebRemix)

    try {
        val raw = innerTubeClient.search(
            query,
            params = InnerTube.SearchFilter.Song.value,
            continuation = null
        )

        val response = json.decodeFromString<SearchResponse>(raw)
        val tabs = response.contents?.tabbedSearchResultsRenderer?.tabs ?: emptyList()
        if (tabs.isEmpty()) return@flow

        val sectionContents =
            tabs.first().tabRenderer?.content?.sectionListRenderer?.contents ?: emptyList()
        val musicShelf =
            sectionContents.firstOrNull { it.musicShelfRenderer != null }?.musicShelfRenderer
                ?: return@flow

        for (item in musicShelf.contents.orEmpty()) {
            val renderer = item.musicResponsiveListItemRenderer ?: continue

            // Video ID & title
            val flex0 =
                renderer.flexColumns?.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer
            val textRun0 = flex0?.text?.runs?.firstOrNull()
            val videoId = textRun0?.navigationEndpoint?.watchEndpoint?.videoId ?: continue
            val title = textRun0.text ?: "Unknown Title"

            // Artists
            val flex1 =
                renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer
            val artistRuns = flex1?.text?.runs.orEmpty()
            var duration = "Unknown Duration"
            var albumId = "Unknown AlbumId"
            val artistsList = mutableListOf<SongArtist>()
            var dotCount = 0
            for (index in 0..<artistRuns.size) {
                if (artistRuns[index].text != " • ") {
                    when (dotCount) {
                        0 -> {
                            val run = artistRuns[index]
                            val artistId = run.navigationEndpoint?.browseEndpoint?.browseId ?: continue
                            val artistName = run.text ?: "Unknown Artist"
                            artistsList.add(SongArtist(id = artistId, name = artistName))
                            continue
                        }
                        1 -> {
                            val run = artistRuns[index]
                            albumId = run.navigationEndpoint?.browseEndpoint?.browseId ?: continue
                        }
                        else -> {
                            val run = artistRuns[index]
                            duration = run.text ?: "Unknown Duration"
                        }
                    }

                } else {
                    dotCount++
                }
            }

//            // Thumbnail
            val thumbnails = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
            val fistPartOfThumbnailUrl = thumbnails?.maxByOrNull {
                it.width + it.height
            }?.url?.split("=")?.getOrNull(0)

            val song = Song(
                id = videoId,
                title = title,
                artists = artistsList,
                albumId = albumId,
                thumbnail = fistPartOfThumbnailUrl ?: "",
                duration = duration
            )

            println("Emitting song: $title")
            emit(song)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun playSongByIdWithBestBitrate(videoId: String): String {
    val json = Json { ignoreUnknownKeys = true }

    val raw = InnerTube(ClientName.Android).player(videoId)
    val response = json.decodeFromString<PlayerResponse>(raw)

    val best = response.streamingData
        ?.adaptiveFormats
        ?.filter { it.audioQuality in listOf("AUDIO_QUALITY_HIGH", "AUDIO_QUALITY_MEDIUM") }
        ?.filter { it.url != null }
        ?.maxByOrNull { it.averageBitrate ?: 0 }

    println("Best bitrate URL: ${best?.url}")

    return best?.url ?: ""
}

fun getRadioFlow(
    videoId: String,
): Flow<Song> = flow {
    val json = Json { ignoreUnknownKeys = true }
    val innerTubeClient = InnerTube(ClientName.TvLite)

    try {
        val raw = innerTubeClient.betterNext(
            videoId = videoId,
            playlistId = "RDAMVM$videoId",
            params = null,
            continuation = null
        )

        val parsed = json.decodeFromString<NextResponse>(raw)

        val playlist = parsed.contents
            ?.singleColumnWatchNextResults
            ?.playlist
            ?.playlist
            ?: run {
                println("❌ Playlist missing")
                return@flow      // exit flow cleanly
            }

        val contents = playlist.contents ?: run {
            println("❌ Playlist contents missing")
            return@flow
        }

        // Emit each song inside the Flow
        for (item in contents) {
            val renderer = item.playlistPanelVideoRenderer ?: continue

            val id = renderer.videoId ?: continue

            val title = renderer.title?.runs?.firstOrNull()?.text ?: "Unknown Title"

            val artistRuns = renderer.shortBylineText?.runs.orEmpty()
            var albumId = "Unknown AlbumId"
            val artistsList = mutableListOf<SongArtist>()
            var dotCount = 0
            for (index in 0..<artistRuns.size) {
                if (artistRuns[index].text != " • ") {
                    when (dotCount) {
                        0 -> {
                            val run = artistRuns[index]
                            val artistId = run.navigationEndpoint?.browseEndpoint?.browseId ?: continue
                            val artistName = run.text ?: "Unknown Artist"
                            artistsList.add(SongArtist(id = artistId, name = artistName))
                            continue
                        }
                        1 -> {
                            val run = artistRuns[index]
                            albumId = run.navigationEndpoint?.browseEndpoint?.browseId ?: continue
                        }
                        else -> {
                            val run = artistRuns[index]
//                            duration = run.text ?: "Unknown Duration"
                        }
                    }

                } else {
                    dotCount++
                }
            }

            val duration = renderer.lengthText?.runs?.firstOrNull()?.text ?: ""

            val fistPartOfThumbnailUrl = renderer.thumbnail?.thumbnails?.maxByOrNull {
                it.width + it.height
            }?.url?.split("=")?.getOrNull(0)

            val song = Song(
                id = id,
                title = title,
                artists = artistsList,
                thumbnail = fistPartOfThumbnailUrl ?: "",
                duration = duration
            )

            println("Parsed song: id=$id, title=$title, duration=$duration")

            emit(song)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        // You can emit an error type or just end the flow
    }
}

data class AlbumWithSongsAndIndices(
    val album: Album,
    val songs: List<Song>
)
fun getAlbumAndSongs(browseId: String): Flow<AlbumWithSongsAndIndices> = flow {
    val innerTubeClient = InnerTube(ClientName.WebRemix)
    var songList = emptyList<Song>()
    try {
        val raw = innerTubeClient.browse(browseId)

        val json = Json { ignoreUnknownKeys = true }

        val parsed = json.decodeFromString<AlbumBrowseResponse>(raw)

        val albumItems = parsed
            .contents
            ?.twoColumnBrowseResultsRenderer
            ?.secondaryContents
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicShelfRenderer
            ?.contents

        val album = parsed
            .contents
            ?.twoColumnBrowseResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.firstOrNull()
            ?.musicResponsiveHeaderRenderer

        val thumbnailURL = parsed
            .microformat
            ?.microformatDataRenderer
            ?.thumbnail
            ?.thumbnails
            ?.maxByOrNull {
                it.width + it.height
            }?.url?.split("=")?.getOrNull(0)

        val title = album
            ?.title
            ?.runs
            ?.firstOrNull()
            ?.text

        val year = album
            ?.subtitle
            ?.runs
            ?.lastOrNull()
            ?.text

        val authorsText = album
            ?.description
            ?.musicDescriptionShelfRenderer
            ?.description
            ?.runs
            ?.firstOrNull()
            ?.text
            ?.split("\nFrom Wikipedia")?.getOrNull(0)

        val copyright = album
            ?.description
            ?.musicDescriptionShelfRenderer
            ?.description
            ?.runs
            ?.get(2)
            ?.text
            ?.split(")")?.getOrNull(1)
            ?.split("(")?.getOrNull(0)

        val shareUrl = parsed
            .microformat
            ?.microformatDataRenderer
            ?.urlCanonical

        for (item in albumItems.orEmpty()) {
            val songId = item
                .musicResponsiveListItemRenderer
                ?.playlistItemData
                ?.videoId ?: continue

            val songTitle = item
                .musicResponsiveListItemRenderer
                .flexColumns
                ?.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.firstOrNull()
                ?.text

            val songIndex = item
                .musicResponsiveListItemRenderer
                .index
                ?.runs
                ?.firstOrNull()
                ?.text

            val songDuration = item
                .musicResponsiveListItemRenderer
                .fixedColumns
                ?.firstOrNull()
                ?.musicResponsiveListItemFixedColumnRenderer
                ?.text
                ?.runs
                ?.firstOrNull()
                ?.text

            val artistsList = mutableListOf<SongArtist>()
            if (item
                    .musicResponsiveListItemRenderer
                    .flexColumns?.get(1)
                    ?.musicResponsiveListItemFlexColumnRenderer
                    ?.text
                    ?.runs == null
            ) {
                val artistName = item.musicResponsiveListItemRenderer.overlay
                    ?.musicItemThumbnailOverlayRenderer
                    ?.content
                    ?.musicPlayButtonRenderer
                    ?.accessibilityPlayData
                    ?.accessibilityData
                    ?.label?.split("- ")?.getOrNull(1)
                val artist = item.musicResponsiveListItemRenderer
                    .menu
                    ?.menuRenderer
                    ?.items
                    ?.filter { item ->
                        item.menuNavigationItemRenderer?.text?.runs?.firstOrNull()?.text == "Go to artist"
                    }
                val artistId = artist?.firstOrNull()?.menuNavigationItemRenderer?.navigationEndpoint?.browseEndpoint?.browseId?: "S"

                artistsList.add(SongArtist(
                    id = artistId,
                    name = artistName ?: "Unknown Artist"
                ))
            } else {
                val artistRuns = item
                    .musicResponsiveListItemRenderer
                    .flexColumns
                    .get(1)
                    .musicResponsiveListItemFlexColumnRenderer
                    ?.text
                    ?.runs
                for (index in 0..<artistRuns!!.size) {
                    if (
                        artistRuns[index].text != " • "
                        && artistRuns[index].text != " & "
                        && artistRuns[index].text != ", "
                    ) {
                        // It's an artist name
                        val run = artistRuns[index]
                        val artistId = run.navigationEndpoint?.browseEndpoint?.browseId ?: continue
                        val artistName = run.text ?: "Unknown Artist"

                        artistsList.add(SongArtist(
                            id = artistId,
                            name = artistName
                        ))
                    }
                }
            }

            songList = songList + Song(
                id = songId,
                title = songTitle?: "",
                artists = artistsList,
                albumId = browseId,
                duration = songDuration?: "",
                thumbnail = thumbnailURL?: ""
            )

//            println("Emitting Song with id: $songId at index: $songIndex")
//            println("Thumbnail: $thumbnailURL, artist: $songArtist, $songDuration, $songTitle")

        }

        val artist = songList.firstOrNull()?.artists?.firstOrNull()?.name ?: "Various Artists"

        val finalAlbum = Album(
            id = browseId,
            title = title?: "",
            artist = artist,
            thumbnailUrl = thumbnailURL.toString(),
            year = year?: "",
            authorsText = authorsText?: "",
            copyright = ("From Wikipedia$copyright"),
            shareUrl = shareUrl?: "",
            timestamp = System.currentTimeMillis(),
            bookmarkedAt = null,
            isYoutubeAlbum = false
        )

        emit(
            AlbumWithSongsAndIndices(
                album = finalAlbum,
                songs = songList
            )
        )

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun searchAlbums(searchQuery: String): Flow<AlbumPreview> = flow {
    val innerTubeClient = InnerTube(ClientName.WebRemix)
    try {
        val raw = innerTubeClient
            .search(
                query = searchQuery,
                params = InnerTube.SearchFilter.Album.value
            )

        val json = Json { ignoreUnknownKeys = true }

        val parsed = json.decodeFromString<SearchAlbumsResponse>(raw)

        val contents = parsed
            .contents
            ?.tabbedSearchResultsRenderer
            ?.tabs
            ?.firstOrNull()
            ?.tabRenderer
            ?.content
            ?.sectionListRenderer
            ?.contents
            ?.get(1)
            ?.musicShelfRenderer

        for (album in contents?.contents.orEmpty()) {
            val musicResponsiveListItemRenderer = album
                .musicResponsiveListItemRenderer

            val id = musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
                ?.browseId

            val title = musicResponsiveListItemRenderer
                ?.flexColumns
                ?.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.firstOrNull()
                ?.text

            val runs = musicResponsiveListItemRenderer
                ?.flexColumns

            val artist = runs
                ?.get(1)
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.get(2)
                ?.text

            val year = runs
                ?.get(1)
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.lastOrNull()
                ?.text

            val thumbnail = musicResponsiveListItemRenderer
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.maxByOrNull {
                    it.width + it.height
                }?.url?.split("=")?.getOrNull(0)


            emit(
                AlbumPreview(
                    id = id ?: "",
                    title = title ?: "",
                    artist = artist ?: "",
                    year = year ?: "",
                    thumbnail = thumbnail ?: ""
                )
            )
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}