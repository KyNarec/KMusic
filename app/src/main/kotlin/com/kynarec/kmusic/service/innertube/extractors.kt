package com.kynarec.kmusic.service.innertube

import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.data.db.entities.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlin.collections.maxByOrNull

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

            val artists = artistRuns
                .filter { run ->
                    run.navigationEndpoint?.browseEndpoint
                        ?.browseEndpointContextSupportedConfigs
                        ?.browseEndpointContextMusicConfig
                        ?.pageType == "MUSIC_PAGE_TYPE_ARTIST"
                }
                .mapNotNull { it.text }
                .ifEmpty { listOf("Unknown Artist") }


            // Duration (last run of flex1)
            val duration = artistRuns.lastOrNull()?.text ?: "Unknown Duration"

//            // Thumbnail
            val thumbnails = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails
//            thumbnails?.forEach {
//                Log.d("Thumbnail", "Thumbnail found: ${it.url} (${it.width}x${it.height})")
//            }
//            val desiredResolution = "=w1024-h1024-l90-rj"
            val fistPartOfThumbnailUrl = thumbnails?.maxByOrNull {
                it.width + it.height
            }?.url?.split("=")?.getOrNull(0)
//            val thumbnail = fistPartOfThumbnailUrl?.let {
//                "$it$desiredResolution"
//            }

            val song = Song(
                id = videoId,
                title = title,
                artist = artists.joinToString(", "),
                thumbnail = fistPartOfThumbnailUrl ?: "",
                duration = duration
            )

            println("Emitting song: $title by ${artists.joinToString()}")
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
            params = Params.Song.label,
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

            val artists = renderer.shortBylineText?.runs
                ?.mapNotNull { it.text }
                ?.takeIf { it.isNotEmpty() }
                ?: listOf("Unknown Artist")

            val duration = renderer.lengthText?.runs?.firstOrNull()?.text ?: ""

//            val thumbnail = getHighestDefinitionThumbnailFromPlayer(
//                InnerTube(CLIENTNAME.WEB_REMIX).player(id)
//            )?: innerTubeClient.getYoutubeThumbnail(id)

            val fistPartOfThumbnailUrl = renderer.thumbnail?.thumbnails?.maxByOrNull {
                it.width + it.height
            }?.url?.split("=")?.getOrNull(0)
//                renderer.thumbnail?.thumbnails?.lastOrNull()?.url
//                    ?: innerTubeClient.getYoutubeThumbnail(id)

            val song = Song(
                id = id,
                title = title,
                artist = artists.joinToString(", "),
                thumbnail = fistPartOfThumbnailUrl ?: "",
                duration = duration
            )

            println("Parsed song: id=$id, title=$title, artists=${artists.joinToString()}, duration=$duration")

            emit(song)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        // You can emit an error type or just end the flow
    }
}

fun getAlbum(browseId: String): Flow<Album> = flow {
    val innerTubeClient = InnerTube(ClientName.WebRemix)
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

        for (item in albumItems.orEmpty()) {
            val songId = item
                .musicResponsiveListItemRenderer
                ?.playlistItemData
                ?.videoId ?: continue

            val songIndex = item
                .musicResponsiveListItemRenderer
                .index
                ?.runs
                ?.firstOrNull()
                ?.text
            println("Emitting album with id: $songId at index: $songIndex")
        }

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
            ?.split(" (")?.getOrNull(0)

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


        println("Album thumbnail URL: $thumbnailURL")

        emit(
            Album(
                id = browseId,
                title = title ?: "",
                thumbnailUrl = thumbnailURL.toString(),
                year = year ?: "",
                authorsText = authorsText ?: "",
                copyright = copyright ?: "",
                shareUrl = shareUrl ?: "",
                timestamp = System.currentTimeMillis(),
                bookmarkedAt = null,
                isYoutubeAlbum = false
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