package com.kynarec.kmusic.service.innertube

import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.AlbumPreview
import com.kynarec.kmusic.data.db.entities.Artist
import com.kynarec.kmusic.data.db.entities.ArtistPreview
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.PlaylistPreview
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongArtist
import com.kynarec.kmusic.service.innertube.responses.AlbumBrowseResponse
import com.kynarec.kmusic.service.innertube.responses.ArtistResponse
import com.kynarec.kmusic.service.innertube.responses.BrowseAlbumsResponse
import com.kynarec.kmusic.service.innertube.responses.BrowsePlaylistSongsContinuationResponse
import com.kynarec.kmusic.service.innertube.responses.FullResponse
import com.kynarec.kmusic.service.innertube.responses.GetPlaylistAndSongsResponse
import com.kynarec.kmusic.service.innertube.responses.NextResponse
import com.kynarec.kmusic.service.innertube.responses.PlayerResponse
import com.kynarec.kmusic.service.innertube.responses.SearchAlbumsResponse
import com.kynarec.kmusic.service.innertube.responses.SearchArtistsResponse
import com.kynarec.kmusic.service.innertube.responses.SearchCommunityPlaylistsResponse
import com.kynarec.kmusic.service.innertube.responses.SearchResponse
import com.kynarec.kmusic.service.innertube.responses.SearchSuggestionsResponse
import innertube.responses.BrowseSongsResponse
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
    try {
        val response = json.decodeFromString<PlayerResponse>(raw)

        val best = response.streamingData
            ?.adaptiveFormats
            ?.filter { it.audioQuality in listOf("AUDIO_QUALITY_HIGH", "AUDIO_QUALITY_MEDIUM") }
            ?.filter { it.url != null }
            ?.maxByOrNull { it.averageBitrate ?: 0 }

        println("Best bitrate URL: ${best?.url}")

        return best?.url ?: "NA"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

suspend fun playSongById(videoId: String): String {
    val json = Json { ignoreUnknownKeys = true }

    val raw = InnerTube(ClientName.Android).player(videoId)
    try {
        val response = json.decodeFromString<PlayerResponse>(raw)

        val best = response.streamingData
            ?.formats?.first { it.itag == 18 }

//            ?.adaptiveFormats
//            ?.filter { it.audioQuality in listOf("AUDIO_QUALITY_HIGH", "AUDIO_QUALITY_MEDIUM") }
//            ?.filter { it.url != null }
//            ?.maxByOrNull { it.averageBitrate ?: 0 }

        println("Song URL: ${best?.url}")

        return best?.url ?: "NA"
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
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
        val raw = innerTubeClient.browse(browseId, null)

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


fun searchArtists(searchQuery: String): Flow<ArtistPreview> = flow {
    val innerTubeClient = InnerTube(ClientName.WebRemix)
    try {
        val raw = innerTubeClient
            .search(
                query = searchQuery,
                params = InnerTube.SearchFilter.Artist.value
            )

        println(raw)

        val json = Json { ignoreUnknownKeys = true }

        val parsed = json.decodeFromString<SearchArtistsResponse>(raw)

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
            ?.contents

        for (content in contents.orEmpty()) {
            val thumbnail = content
                .musicResponsiveListItemRenderer
                ?.thumbnail
                ?.musicThumbnailRenderer
                ?.thumbnail
                ?.thumbnails
                ?.maxByOrNull {
                    it.width + it.height
                }?.url?.split("=")?.getOrNull(0)

            val id = content
                .musicResponsiveListItemRenderer
                ?.navigationEndpoint
                ?.browseEndpoint
                ?.browseId

            val name = content
                .musicResponsiveListItemRenderer
                ?.flexColumns
                ?.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.firstOrNull()
                ?.text

            val monthlyListeners = content
                .musicResponsiveListItemRenderer
                ?.flexColumns
                ?.get(1)
                ?.musicResponsiveListItemFlexColumnRenderer
                ?.text
                ?.runs
                ?.lastOrNull()
                ?.text

            emit(
                ArtistPreview(
                    id = id ?: "",
                    name = name ?: "",
                    thumbnailUrl = thumbnail ?: "",
                    monthlyListeners = monthlyListeners ?: ""
                )
            )
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

data class ArtistPage(
    val artist: Artist,
    val topSongs: List<Song>,
    val topSongsBrowseId: String = "",
    val topSongsParams: String = "",
    val albums: List<AlbumPreview>,
    val albumsBrowseId: String = "",
    val albumsParams: String = "",
    val singlesAndEps: List<AlbumPreview>,
    val singlesAndEpsBrowseId: String = "",
    val singlesAndEpsParams: String = ""
)

fun getArtist(browseId: String): Flow<ArtistPage> = flow {
    val innerTubeClient = InnerTube(ClientName.WebRemix)
    val topSongs = mutableListOf<Song>()
    val albums = mutableListOf<AlbumPreview>()
    val singleAndEps = mutableListOf<AlbumPreview>()

    try {
        val raw = innerTubeClient.browse(browseId, null)
//        val file = File("output.json")
//        file.writeText(raw)
//        println("File saved to: ${file.absolutePath}")

        val json = Json { ignoreUnknownKeys = true }

        val parsed = json.decodeFromString<ArtistResponse>(raw)

        val artistName = parsed
            .artistHeader
            ?.artistMusicImmersiveHeaderRenderer
            ?.artistTitle
            ?.artistRuns
            ?.firstOrNull()
            ?.artistText

        val artistSubscribers = parsed
            .artistHeader
            ?.artistMusicImmersiveHeaderRenderer
            ?.artistSubscriptionButton
            ?.artistSubscribeButtonRenderer
            ?.artistSubscriberCountText
            ?.artistRuns
            ?.firstOrNull()
            ?.artistText

        val description = parsed
            .artistMicroformat
            ?.artistMicroformatDataRenderer
            ?.artistDescription

        val thumbnail = parsed
            .artistHeader
            ?.artistMusicImmersiveHeaderRenderer
            ?.artistThumbnail
            ?.artistMusicThumbnailRenderer
            ?.artistThumbnail
            ?.artistThumbnails
            ?.maxByOrNull {
                it.artistWidth + it.artistHeight
            }?.artistUrl?.split("=")?.getOrNull(0)?: ""

        println("Artist Thumbnail Url: $thumbnail")

        val artist = Artist(
            id = browseId,
            name = artistName?: "",
            thumbnailUrl = thumbnail,
            subscriber = artistSubscribers,
            description = description,
            timestamp = System.currentTimeMillis(),
            bookmarkedAt = null,
            isYoutubeArtist = true
        )

        val artistInfo = parsed
            .artistContents
            ?.artistSingleColumnBrowseResultsRenderer
            ?.artistTabs
            ?.firstOrNull()
            ?.artistTabRenderer

        val topSongsBrowseId = artistInfo
            ?.artistContent
            ?.artistSectionListRenderer
            ?.artistContents
            ?.firstOrNull()
            ?.artistMusicShelfRenderer
            ?.artistBottomEndpoint
            ?.artistBrowseEndpoint
            ?.artistBrowseId?: ""

        val topSongsParams = artistInfo
            ?.artistContent
            ?.artistSectionListRenderer
            ?.artistContents
            ?.firstOrNull()
            ?.artistMusicShelfRenderer
            ?.artistBottomEndpoint
            ?.artistBrowseEndpoint
            ?.artistParams?: ""

        var singleAndEPsBrowseId: String? = null
        var singleAndEPsParams: String? = null
        var albumsBrowseId: String? = null
        var albumsParams: String? = null

        for (song in artistInfo
            ?.artistContent
            ?.artistSectionListRenderer
            ?.artistContents
            ?.firstOrNull()
            ?.artistMusicShelfRenderer
            ?.artistContents.orEmpty()) {

            val thumbnail = song
                .artistMusicResponsiveListItemRenderer
                ?.artistThumbnail
                ?.artistMusicThumbnailRenderer
                ?.artistThumbnail
                ?.artistThumbnails
                ?.maxByOrNull {
                    it.artistWidth + it.artistHeight
                }?.artistUrl?.split("=")?.getOrNull(0)

            val id = song
                .artistMusicResponsiveListItemRenderer
                ?.artistOverlay
                ?.artistMusicItemThumbnailOverlayRenderer
                ?.artistContent
                ?.artistMusicPlayButtonRenderer
                ?.artistPlayNavigationEndpoint
                ?.artistWatchEndpoint
                ?.artistVideoId?: ""

            val title = song
                .artistMusicResponsiveListItemRenderer
                ?.artistFlexColumns
                ?.firstOrNull()
                ?.artistMusicResponsiveListItemFlexColumnRenderer
                ?.artistText
                ?.artistRuns
                ?.firstOrNull()
                ?.artistText?: "Unknown Title"

            val artistsList = mutableListOf<SongArtist>()
            var albumBrowseId: String? = null
            val flexColumns = song
                .artistMusicResponsiveListItemRenderer
                ?.artistFlexColumns

            for (flexColumn in flexColumns.orEmpty()) {
                val runs = flexColumn
                    .artistMusicResponsiveListItemFlexColumnRenderer
                    ?.artistText
                    ?.artistRuns
                for (index in 0..<runs!!.size) {
                    if (runs[index].artistText != " & "
                        && runs[index].artistText != ", "
                        && runs[index].artistNavigationEndpoint
                            ?.artistBrowseEndpoint
                            ?.artistBrowseEndpointContextSupportedConfigs
                            ?.artistBrowseEndpointContextMusicConfig
                            ?.artistPageType == "MUSIC_PAGE_TYPE_ARTIST"
                    ) {
                        val run = runs[index]
                        val artistId = run
                            .artistNavigationEndpoint
                            ?.artistBrowseEndpoint
                            ?.artistBrowseId ?: continue
                        val artistName = run.artistText ?: "Unknown Artist"
                        artistsList.add(SongArtist(id = artistId, name = artistName))
                    } else if (runs[index].artistNavigationEndpoint
                            ?.artistBrowseEndpoint
                            ?.artistBrowseEndpointContextSupportedConfigs
                            ?.artistBrowseEndpointContextMusicConfig
                            ?.artistPageType == "MUSIC_PAGE_TYPE_ALBUM")
                    {
                        albumBrowseId = runs[index]
                            .artistNavigationEndpoint
                            ?.artistBrowseEndpoint
                            ?.artistBrowseId
                    }
                }
            }

            val s = Song(
                id = id,
                title = title,
                artists = artistsList,
                albumId = albumBrowseId,
                duration = "",
                thumbnail = thumbnail?: ""
            )
            topSongs.add(s)

            println("Extracted top song: $title, $id")
            val artistNames = s.artists.joinToString(separator = ", ") { artist -> artist.name }
            println("Artist: $artistNames")
            val artistIds = s.artists.joinToString(separator = ", ") { artist -> artist.id }
            println("Artist Ids: $artistIds")
            println("Album Id: $albumBrowseId")
            println("Thumbnail: $thumbnail")
        }

        for (carousel in artistInfo
            ?.artistContent
            ?.artistSectionListRenderer
            ?.artistContents.orEmpty()) {
            if (carousel
                    .artistMusicCarouselShelfRenderer
                    ?.artistHeader
                    ?.artistMusicCarouselShelfBasicHeaderRenderer
                    ?.artistTitle
                    ?.artistRuns
                    ?.firstOrNull()
                    ?.artistText == "Albums") {
                for (album in carousel
                    .artistMusicCarouselShelfRenderer
                    .artistContents.orEmpty()) {

                    val thumbnail = album
                        .artistMusicTwoRowItemRenderer
                        ?.artistThumbnailRenderer
                        ?.artistMusicThumbnailRenderer
                        ?.artistThumbnail
                        ?.artistThumbnails
                        ?.maxByOrNull {
                            it.artistWidth + it.artistHeight
                        }?.artistUrl?.split("=")?.getOrNull(0)

                    val title = album
                        .artistMusicTwoRowItemRenderer
                        ?.artistTitle
                        ?.artistRuns
                        ?.firstOrNull()
                        ?.artistText

                    val id = album
                        .artistMusicTwoRowItemRenderer
                        ?.artistTitle
                        ?.artistRuns
                        ?.firstOrNull()
                        ?.artistNavigationEndpoint
                        ?.artistBrowseEndpoint
                        ?.artistBrowseId

                    val year = album
                        .artistMusicTwoRowItemRenderer
                        ?.artistSubtitle
                        ?.artistRuns
                        ?.lastOrNull()
                        ?.artistText


                    val a = AlbumPreview(
                        id = id?: "",
                        title = title?: "",
                        artist = artistName?: "",
                        year = year?: "",
                        thumbnail = thumbnail?: ""
                    )

                    albums.add(a)
                    println("Extracted album: $title, $id, $year, $artistName,  $thumbnail")
                }
                albumsBrowseId = carousel
                    .artistMusicCarouselShelfRenderer
                    .artistHeader
                    .artistMusicCarouselShelfBasicHeaderRenderer
                    .artistMoreContentButton
                    ?.artistButtonRenderer
                    ?.artistNavigationEndpoint
                    ?.artistBrowseEndpoint
                    ?.artistBrowseId

                albumsParams = carousel
                    .artistMusicCarouselShelfRenderer
                    .artistHeader
                    .artistMusicCarouselShelfBasicHeaderRenderer
                    .artistMoreContentButton
                    ?.artistButtonRenderer
                    ?.artistNavigationEndpoint
                    ?.artistBrowseEndpoint
                    ?.artistParams

            } else if (
                carousel
                    .artistMusicCarouselShelfRenderer
                    ?.artistHeader
                    ?.artistMusicCarouselShelfBasicHeaderRenderer
                    ?.artistTitle
                    ?.artistRuns
                    ?.firstOrNull()
                    ?.artistText == "Singles & EPs"
            ) {
                singleAndEPsBrowseId = carousel
                    .artistMusicCarouselShelfRenderer
                    .artistHeader
                    .artistMusicCarouselShelfBasicHeaderRenderer
                    .artistMoreContentButton
                    ?.artistButtonRenderer
                    ?.artistNavigationEndpoint
                    ?.artistBrowseEndpoint
                    ?.artistBrowseId

                singleAndEPsParams = carousel
                    .artistMusicCarouselShelfRenderer
                    .artistHeader
                    .artistMusicCarouselShelfBasicHeaderRenderer
                    .artistMoreContentButton
                    ?.artistButtonRenderer
                    ?.artistNavigationEndpoint
                    ?.artistBrowseEndpoint
                    ?.artistParams

                for (singleAndEP in carousel
                    .artistMusicCarouselShelfRenderer
                    .artistContents.orEmpty())
                {
                    val thumbnail = singleAndEP
                        .artistMusicTwoRowItemRenderer
                        ?.artistThumbnailRenderer
                        ?.artistMusicThumbnailRenderer
                        ?.artistThumbnail
                        ?.artistThumbnails
                        ?.maxByOrNull {
                            it.artistWidth + it.artistHeight
                        }?.artistUrl?.split("=")?.getOrNull(0)

                    val title = singleAndEP
                        .artistMusicTwoRowItemRenderer
                        ?.artistTitle
                        ?.artistRuns
                        ?.firstOrNull()
                        ?.artistText

                    val id = singleAndEP
                        .artistMusicTwoRowItemRenderer
                        ?.artistTitle
                        ?.artistRuns
                        ?.firstOrNull()
                        ?.artistNavigationEndpoint
                        ?.artistBrowseEndpoint
                        ?.artistBrowseId

                    val year = singleAndEP
                        .artistMusicTwoRowItemRenderer
                        ?.artistSubtitle
                        ?.artistRuns
                        ?.lastOrNull()
                        ?.artistText

                    val singleAndEP = AlbumPreview(
                        id = id?: "",
                        title = title?: "",
                        artist = artistName?: "",
                        year = year?: "",
                        thumbnail = thumbnail?: ""
                    )

                    singleAndEps.add(singleAndEP)
                }
            }
        }

        println("Top Songs BrowseId: $topSongsBrowseId")
        println("Top Songs Params: $topSongsParams")

        println("Artist description: $description")

        emit(
            ArtistPage(
                artist = artist,
                topSongs = topSongs,
                topSongsBrowseId = topSongsBrowseId,
                topSongsParams = topSongsParams,
                albums = albums,
                albumsBrowseId = albumsBrowseId?: "",
                albumsParams = albumsParams?: "",
                singlesAndEps = singleAndEps,
                singlesAndEpsBrowseId = singleAndEPsBrowseId?: "",
                singlesAndEpsParams = singleAndEPsParams?: ""
            )
        )

    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun browseSongs(browseId: String, params: String): Flow<Song> = flow {
    val innerTubeClient = InnerTube(ClientName.WebRemix)

    try {
        val raw = innerTubeClient.browse(
            browseId = browseId,
            params = params,
        )

        val json = Json { ignoreUnknownKeys = true }

//        val file = File("browseSongsOutput.json")
//        file.writeText(raw)
//        println("File saved to: ${file.absolutePath}")

        val parsed = json.decodeFromString<BrowseSongsResponse>(raw)

        val contents = parsed
            .browseSongsContents
            ?.browseSongsSingleColumnBrowseResultsRenderer
            ?.browseSongsTabs
            ?.firstOrNull()
            ?.browseSongsTabRenderer
            ?.browseSongsContent
            ?.browseSongsSectionListRenderer
            ?.browseSongsContents
            ?.firstOrNull()
            ?.browseSongsMusicPlaylistShelfRenderer
            ?.browseSongsContents

        for (item in contents.orEmpty()) {
            val renderer = item
                .browseSongsMusicResponsiveListItemRenderer
                ?: continue

            val id = renderer
                .browseSongsFlexColumns
                ?.firstOrNull()
                ?.browseSongsMusicResponsiveListItemFlexColumnRenderer
                ?.browseSongsText
                ?.browseSongsRuns
                ?.firstOrNull()
                ?.browseSongsNavigationEndpoint
                ?.browseSongsWatchEndpoint
                ?.browseSongsVideoId

            val title = renderer
                .browseSongsFlexColumns
                ?.firstOrNull()
                ?.browseSongsMusicResponsiveListItemFlexColumnRenderer
                ?.browseSongsText
                ?.browseSongsRuns
                ?.firstOrNull()
                ?.browseSongsText

            val thumbnail = renderer
                .browseSongsThumbnail
                ?.browseSongsMusicThumbnailRenderer
                ?.browseSongsThumbnail
                ?.browseSongsThumbnails
                ?.maxByOrNull {
                    it.browseSongsWidth + it.browseSongsHeight
                }?.browseSongsUrl?.split("=")?.getOrNull(0)

            val artistList = mutableListOf<SongArtist>()
            var albumBrowseId = ""

            for (flexColumn in renderer.browseSongsFlexColumns.orEmpty()) {
                val artistRuns = flexColumn
                    .browseSongsMusicResponsiveListItemFlexColumnRenderer
                    ?.browseSongsText
                    ?.browseSongsRuns

                for (index in 0..<artistRuns!!.size) {
                    if (artistRuns[index].browseSongsText != " & "
                        && artistRuns[index].browseSongsText != ", "
                        && artistRuns[index].browseSongsNavigationEndpoint
                            ?.browseSongsBrowseEndpoint
                            ?.browseSongsBrowseEndpointContextSupportedConfigs
                            ?.browseSongsBrowseEndpointContextMusicConfig
                            ?.browseSongsPageType == "MUSIC_PAGE_TYPE_ARTIST"
                    ) {
                        val run = artistRuns[index]
                        val artistId = run
                            .browseSongsNavigationEndpoint
                            ?.browseSongsBrowseEndpoint
                            ?.browseSongsBrowseId ?: continue
                        val artistName = run.browseSongsText
                        artistList.add(SongArtist(id = artistId, name = artistName))
                    }
                }

                if (flexColumn.
                    browseSongsMusicResponsiveListItemFlexColumnRenderer
                        .browseSongsText
                        .browseSongsRuns
                        .firstOrNull()
                        ?.browseSongsNavigationEndpoint
                        ?.browseSongsBrowseEndpoint
                        ?.browseSongsBrowseEndpointContextSupportedConfigs
                        ?.browseSongsBrowseEndpointContextMusicConfig
                        ?.browseSongsPageType == "MUSIC_PAGE_TYPE_ALBUM"
                ) {
                    albumBrowseId = flexColumn
                        .browseSongsMusicResponsiveListItemFlexColumnRenderer
                        .browseSongsText
                        .browseSongsRuns
                        .firstOrNull()
                        ?.browseSongsNavigationEndpoint
                        ?.browseSongsBrowseEndpoint
                        ?.browseSongsBrowseId?: ""
                }
            }

            val duration = renderer
                .browseSongsFixedColumns
                ?.firstOrNull()
                ?.browseSongsMusicResponsiveListItemFixedColumnRenderer
                ?.browseSongsText
                ?.browseSongsRuns
                ?.firstOrNull()
                ?.browseSongsText ?: ""

            val fetchedSong = Song(
                id = id?: "",
                title = title?: "",
                artists = artistList,
                albumId = albumBrowseId,
                duration = duration,
                thumbnail = thumbnail?: ""
            )
            emit(fetchedSong)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun browseAlbums(browseId: String, params: String): Flow<AlbumPreview> = flow {
    val innerTubeClient = InnerTube(ClientName.WebRemix)

    try {
        val raw = innerTubeClient.browse(
            browseId = browseId,
            params = params,
        )

        val json = Json { ignoreUnknownKeys = true }

        val parsed = json.decodeFromString<BrowseAlbumsResponse>(raw)

        val items = parsed
            .browseAlbumsContents
            ?.browseAlbumsSingleColumnBrowseResultsRenderer
            ?.browseAlbumsTabs
            ?.firstOrNull()
            ?.browseAlbumsTabRenderer
            ?.browseAlbumsContent
            ?.browseAlbumsSectionListRenderer
            ?.browseAlbumsContents
            ?.firstOrNull()
            ?.browseAlbumsGridRenderer
            ?.browseAlbumsItems

        val artists = parsed
            .browseAlbumsHeader
            ?.browseAlbumsMusicHeaderRenderer
            ?.browseAlbumsTitle
            ?.browseAlbumsRuns
            ?.firstOrNull()
            ?.browseAlbumsText

        for (album in items.orEmpty()) {
            val thumbnail = album
                .browseAlbumsMusicTwoRowItemRenderer
                ?.browseAlbumsThumbnailRenderer
                ?.browseAlbumsMusicThumbnailRenderer
                ?.browseAlbumsThumbnail
                ?.browseAlbumsThumbnails
                ?.maxByOrNull {
                    it.browseAlbumsWidth + it.browseAlbumsHeight
                }?.browseAlbumsUrl?.split("=")?.getOrNull(0)

            val title = album
                .browseAlbumsMusicTwoRowItemRenderer
                ?.browseAlbumsTitle
                ?.browseAlbumsRuns
                ?.firstOrNull()
                ?.browseAlbumsText

            val id = album
                .browseAlbumsMusicTwoRowItemRenderer
                ?.browseAlbumsTitle
                ?.browseAlbumsRuns
                ?.firstOrNull()
                ?.browseAlbumsNavigationEndpoint
                ?.browseAlbumsBrowseEndpoint
                ?.browseAlbumsBrowseId

            val year = album
                .browseAlbumsMusicTwoRowItemRenderer
                ?.browseAlbumsSubtitle
                ?.browseAlbumsRuns
                ?.lastOrNull()
                ?.browseAlbumsText

            emit(AlbumPreview(
                id = id?: "",
                title = title?: "",
                artist = artists?:"",
                year = year?: "",
                thumbnail = thumbnail?: ""
            ))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun searchCommunityPlaylists(searchQuery: String): Flow<PlaylistPreview> = flow {
    val innerTubeClient = InnerTube(ClientName.WebRemix)
    try {
        val raw = innerTubeClient
            .search(
                query = searchQuery,
                params = InnerTube.SearchFilter.CommunityPlaylist.value
            )
        val json = Json { ignoreUnknownKeys = true }

        val parsed = json.decodeFromString<SearchCommunityPlaylistsResponse>(raw)

        val contents = parsed
            .searchCommunityPlaylistsContents
            ?.searchCommunityPlaylistsTabbedSearchResultsRenderer
            ?.searchCommunityPlaylistsTabs
            ?.firstOrNull()
            ?.searchCommunityPlaylistsTabRenderer
            ?.searchCommunityPlaylistsContent
            ?.searchCommunityPlaylistsSectionListRenderer
            ?.searchCommunityPlaylistsContents
            ?.get(1)
            ?.searchCommunityPlaylistsMusicShelfRenderer
            ?.searchCommunityPlaylistsContents

        for (playlist in contents.orEmpty()) {
            val musicResponsiveListItemRenderer = playlist
                .searchCommunityPlaylistsMusicResponsiveListItemRenderer

            val id = musicResponsiveListItemRenderer
                ?.searchCommunityPlaylistsNavigationEndpoint
                ?.searchCommunityPlaylistsBrowseEndpoint
                ?.searchCommunityPlaylistsBrowseId

            val thumbnail = musicResponsiveListItemRenderer
                ?.searchCommunityPlaylistsThumbnail
                ?.searchCommunityPlaylistsMusicThumbnailRenderer
                ?.searchCommunityPlaylistsThumbnail
                ?.searchCommunityPlaylistsThumbnails
                ?.maxByOrNull {
                    it.searchCommunityPlaylistsWidth + it.searchCommunityPlaylistsHeight
                }?.searchCommunityPlaylistsUrl?.split("=")?.getOrNull(0)

            val title = musicResponsiveListItemRenderer
                ?.searchCommunityPlaylistsFlexColumns
                ?.firstOrNull()
                ?.searchCommunityPlaylistsMusicResponsiveListItemFlexColumnRenderer
                ?.searchCommunityPlaylistsText
                ?.searchCommunityPlaylistsRuns
                ?.firstOrNull()
                ?.searchCommunityPlaylistsText

            val author = musicResponsiveListItemRenderer
                ?.searchCommunityPlaylistsFlexColumns
                ?.get(1)
                ?.searchCommunityPlaylistsMusicResponsiveListItemFlexColumnRenderer
                ?.searchCommunityPlaylistsText
                ?.searchCommunityPlaylistsRuns
                ?.firstOrNull()
                ?.searchCommunityPlaylistsText

            val views = if ((musicResponsiveListItemRenderer
                    ?.searchCommunityPlaylistsFlexColumns
                    ?.get(1)
                    ?.searchCommunityPlaylistsMusicResponsiveListItemFlexColumnRenderer
                    ?.searchCommunityPlaylistsText
                    ?.searchCommunityPlaylistsRuns?.size ?: 0) > 2
            ) musicResponsiveListItemRenderer
                ?.searchCommunityPlaylistsFlexColumns
                ?.get(1)
                ?.searchCommunityPlaylistsMusicResponsiveListItemFlexColumnRenderer
                ?.searchCommunityPlaylistsText
                ?.searchCommunityPlaylistsRuns?.get(2)?.searchCommunityPlaylistsText
            else {
                ""
            }


            emit(PlaylistPreview(
                id = id?: "",
                title = title?: "",
                author = author?: "",
                thumbnail = thumbnail?: "",
                views = views?: ""
            ))
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

data class PlaylistWithSongsAndIndices(
    val playlist: Playlist,
    val songs: List<Song>,
    val views: String,
    val year: String
)

suspend fun getPlaylistAndSongs(browseId: String): PlaylistWithSongsAndIndices? {
    val innerTubeClient = InnerTube(ClientName.WebRemix)
    val songsList = mutableListOf<Song>()
    try {
        val raw = innerTubeClient.browse(
            browseId = browseId,
            null
        )

        val json = Json { ignoreUnknownKeys = true }

        val parsed = json.decodeFromString<GetPlaylistAndSongsResponse>(raw)

//        public final val id: Long = 0, auto generated
//        public final val name: String,
//        public final val browseId: String? = null,
//        public final val isEditable: Boolean = true,
//        public final val isYoutubePlaylist: Boolean = false

        val playlistId = browseId
        val playlist = parsed
            .getPlaylistAndSongsContents
            ?.getPlaylistAndSongsTwoColumnBrowseResultsRenderer
            ?.getPlaylistAndSongsTabs
            ?.firstOrNull()
            ?.getPlaylistAndSongsTabRenderer
            ?.getPlaylistAndSongsContent
            ?.getPlaylistAndSongsSectionListRenderer
            ?.getPlaylistAndSongsContents
            ?.firstOrNull()
            ?.getPlaylistAndSongsMusicResponsiveHeaderRenderer

        val playlistName = playlist
            ?.getPlaylistAndSongsTitle
            ?.getPlaylistAndSongsRuns
            ?.firstOrNull()
            ?.getPlaylistAndSongsText

        val year = playlist
            ?.getPlaylistAndSongsSubtitle
            ?.getPlaylistAndSongsRuns
            ?.getOrNull(2)
            ?.getPlaylistAndSongsText

        val views = playlist
            ?.getPlaylistAndSongsSecondSubtitle
            ?.getPlaylistAndSongsRuns
            ?.firstOrNull()
            ?.getPlaylistAndSongsText
            ?.split(" ")
            ?.firstOrNull()

        val songs = parsed
            .getPlaylistAndSongsContents
            ?.getPlaylistAndSongsTwoColumnBrowseResultsRenderer
            ?.getPlaylistAndSongsSecondaryContents
            ?.getPlaylistAndSongsSectionListRenderer
            ?.getPlaylistAndSongsContents
            ?.firstOrNull()
            ?.getPlaylistAndSongsMusicPlaylistShelfRenderer
            ?.getPlaylistAndSongsContents

        /*
        public final data class Song(
        public final val id: String,
        public final val title: String,
        public final val artists: List<SongArtist>,
        public final val albumId: String? = null,
        public final val duration: String,
        public final val thumbnail: String
        )
         */

        for (item in songs.orEmpty()) {
            val song = item.getPlaylistAndSongsMusicResponsiveListItemRenderer

            val thumbnail = song
                ?.getPlaylistAndSongsThumbnail
                ?.getPlaylistAndSongsMusicThumbnailRenderer
                ?.getPlaylistAndSongsThumbnail
                ?.getPlaylistAndSongsThumbnails
                ?.maxByOrNull {
                    it.getPlaylistAndSongsWidth + it.getPlaylistAndSongsHeight
                }?.getPlaylistAndSongsUrl?.split("=")?.getOrNull(0)

            val id = song
                ?.getPlaylistAndSongsOverlay
                ?.getPlaylistAndSongsMusicItemThumbnailOverlayRenderer
                ?.getPlaylistAndSongsContent
                ?.getPlaylistAndSongsMusicPlayButtonRenderer
                ?.getPlaylistAndSongsPlayNavigationEndpoint
                ?.getPlaylistAndSongsWatchEndpoint
                ?.getPlaylistAndSongsVideoId

            val title = song
                ?.getPlaylistAndSongsFlexColumns
                ?.firstOrNull()
                ?.getPlaylistAndSongsMusicResponsiveListItemFlexColumnRenderer
                ?.getPlaylistAndSongsText
                ?.getPlaylistAndSongsRuns
                ?.firstOrNull()
                ?.getPlaylistAndSongsText

            var duration = song
                ?.getPlaylistAndSongsFixedColumns
                ?.firstOrNull()
                ?.getPlaylistAndSongsMusicResponsiveListItemFixedColumnRenderer
                ?.getPlaylistAndSongsText
                ?.getPlaylistAndSongsRuns
                ?.firstOrNull()
                ?.getPlaylistAndSongsText

            if (duration.isNullOrEmpty()) {
                duration = song
                    ?.getPlaylistAndSongsFlexColumns
                    ?.get(song.getPlaylistAndSongsFlexColumns.size - 2)
                    ?.getPlaylistAndSongsMusicResponsiveListItemFlexColumnRenderer
                    ?.getPlaylistAndSongsText
                    ?.getPlaylistAndSongsRuns
                    ?.firstOrNull()
                    ?.getPlaylistAndSongsText ?: ""
            }

            var albumId = ""
            val artistList = mutableListOf<SongArtist>()
            for (flexColumn in song?.getPlaylistAndSongsFlexColumns.orEmpty()) {
                val artistRuns = flexColumn
                    .getPlaylistAndSongsMusicResponsiveListItemFlexColumnRenderer
                    ?.getPlaylistAndSongsText
                    ?.getPlaylistAndSongsRuns

                for (index in 0..<artistRuns.orEmpty().size) {
                    if (artistRuns?.get(index)?.getPlaylistAndSongsText != " & "
                        && artistRuns?.get(index)?.getPlaylistAndSongsText != ", "
                    ) {
                        if (
                            artistRuns?.get(index)?.getPlaylistAndSongsNavigationEndpoint
                                ?.getPlaylistAndSongsBrowseEndpoint
                                ?.getPlaylistAndSongsBrowseEndpointContextSupportedConfigs
                                ?.getPlaylistAndSongsBrowseEndpointContextMusicConfig
                                ?.getPlaylistAndSongsPageType == "MUSIC_PAGE_TYPE_ARTIST"
                            || artistRuns?.get(index)?.getPlaylistAndSongsNavigationEndpoint
                                ?.getPlaylistAndSongsBrowseEndpoint
                                ?.getPlaylistAndSongsBrowseEndpointContextSupportedConfigs
                                ?.getPlaylistAndSongsBrowseEndpointContextMusicConfig
                                ?.getPlaylistAndSongsPageType == "MUSIC_PAGE_TYPE_USER_CHANNEL"
                        ) {
                            val run = artistRuns[index]
                            val artistId = run
                                .getPlaylistAndSongsNavigationEndpoint
                                ?.getPlaylistAndSongsBrowseEndpoint
                                ?.getPlaylistAndSongsBrowseId ?: continue
                            val artistName = run.getPlaylistAndSongsText
                            artistList.add(SongArtist(id = artistId, name = artistName?: ""))
                        }
                    }
                }

                val browseEndpoint = flexColumn.getPlaylistAndSongsMusicResponsiveListItemFlexColumnRenderer
                    ?.getPlaylistAndSongsText
                    ?.getPlaylistAndSongsRuns
                    ?.firstOrNull()
                    ?.getPlaylistAndSongsNavigationEndpoint
                    ?.getPlaylistAndSongsBrowseEndpoint


                if (browseEndpoint
                        ?.getPlaylistAndSongsBrowseEndpointContextSupportedConfigs
                        ?.getPlaylistAndSongsBrowseEndpointContextMusicConfig
                        ?.getPlaylistAndSongsPageType == "MUSIC_PAGE_TYPE_ALBUM"
                ) {
                    albumId = browseEndpoint.getPlaylistAndSongsBrowseId?: ""
                }
            }

            if (id != null)
                songsList.add(
                    Song(
                        id = id,
                        title = title?: "",
                        artists = artistList,
                        albumId = albumId,
                        duration = duration?: "",
                        thumbnail = thumbnail?: ""
                    )
                )

            val continuationToken = item.getPlaylistAndSongsContinuationItemRenderer?.getPlaylistAndSongsContinuationEndpoint?.getPlaylistAndSongsContinuationCommand?.getPlaylistAndSongsToken

            if (continuationToken != null) {
                println("continuationToken found: $continuationToken")
                browsePlaylistSongsContinuation(browseId, continuationToken, innerTubeClient).forEach {
                    songsList.add(it)
                }
            }
        }

        return PlaylistWithSongsAndIndices(
            playlist = Playlist(
                name = playlistName?: "",
                browseId = playlistId,
                isEditable = false,
                isYoutubePlaylist = true
            ),
            songs = songsList,
            views = views ?: "",
            year = year ?: ""
        )

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

suspend fun browsePlaylistSongsContinuation(
    browseId: String,
    initialToken: String,
    innerTubeClient: InnerTube? = null
): List<Song> {
    val allSongsList = mutableListOf<Song>()
    val client = innerTubeClient ?: InnerTube(ClientName.WebRemix)
    val json = Json { ignoreUnknownKeys = true }

    var currentToken: String? = initialToken

    while (currentToken != null) {
        try {
            val raw = client.browse(browseId, params = null, continuation = currentToken)
            val parsed = json.decodeFromString<BrowsePlaylistSongsContinuationResponse>(raw)

            val items = parsed
                .browsePlaylistSongsContinuationOnResponseReceivedActions
                ?.firstOrNull()
                ?.browsePlaylistSongsContinuationAppendContinuationItemsAction
                ?.browsePlaylistSongsContinuationContinuationItems
                .orEmpty()


            for (item in items) {

                val songRenderer = item.browsePlaylistSongsContinuationMusicResponsiveListItemRenderer

                val id = songRenderer?.browsePlaylistSongsContinuationOverlay
                    ?.browsePlaylistSongsContinuationMusicItemThumbnailOverlayRenderer
                    ?.browsePlaylistSongsContinuationContent
                    ?.browsePlaylistSongsContinuationMusicPlayButtonRenderer
                    ?.browsePlaylistSongsContinuationPlayNavigationEndpoint
                    ?.browsePlaylistSongsContinuationWatchEndpoint
                    ?.browsePlaylistSongsContinuationVideoId ?: continue

                val thumbnail = songRenderer.browsePlaylistSongsContinuationThumbnail
                    ?.browsePlaylistSongsContinuationMusicThumbnailRenderer
                    ?.browsePlaylistSongsContinuationThumbnail
                    ?.browsePlaylistSongsContinuationThumbnails
                    ?.maxByOrNull { it.browsePlaylistSongsContinuationWidth + it.browsePlaylistSongsContinuationHeight }
                    ?.browsePlaylistSongsContinuationUrl?.split("=")?.getOrNull(0) ?: ""

                var duration = songRenderer.browsePlaylistSongsContinuationFixedColumns
                    ?.firstOrNull()
                    ?.browsePlaylistSongsContinuationMusicResponsiveListItemFixedColumnRenderer
                    ?.browsePlaylistSongsContinuationText
                    ?.browsePlaylistSongsContinuationRuns
                    ?.firstOrNull()
                    ?.browsePlaylistSongsContinuationText ?: ""

                var title = ""
                var albumId = ""
                val artistList = mutableListOf<SongArtist>()

                songRenderer.browsePlaylistSongsContinuationFlexColumns?.forEachIndexed { index, flexColumn ->
                    val runs = flexColumn.browsePlaylistSongsContinuationMusicResponsiveListItemFlexColumnRenderer
                        ?.browsePlaylistSongsContinuationText
                        ?.browsePlaylistSongsContinuationRuns.orEmpty()

                    if (index == 0) {
                        title = runs.firstOrNull()?.browsePlaylistSongsContinuationText ?: ""
                    } else {
                        runs.forEach { run ->
                            val nav = run.browsePlaylistSongsContinuationNavigationEndpoint
                                ?.browsePlaylistSongsContinuationBrowseEndpoint
                            val pageType = nav?.browsePlaylistSongsContinuationBrowseEndpointContextSupportedConfigs
                                ?.browsePlaylistSongsContinuationBrowseEndpointContextMusicConfig
                                ?.browsePlaylistSongsContinuationPageType

                            when (pageType) {
                                "MUSIC_PAGE_TYPE_ARTIST", "MUSIC_PAGE_TYPE_USER_CHANNEL" -> {
                                    val aId = nav.browsePlaylistSongsContinuationBrowseId
                                    if (aId != null) artistList.add(SongArtist(id = aId, name = run.browsePlaylistSongsContinuationText ?: ""))
                                }
                                "MUSIC_PAGE_TYPE_ALBUM" -> {
                                    albumId = nav.browsePlaylistSongsContinuationBrowseId?: ""
                                }
                            }
                        }
                    }
                }

                if (duration.isEmpty()) {
                    duration = songRenderer
                        .browsePlaylistSongsContinuationFlexColumns
                        ?.get(songRenderer.browsePlaylistSongsContinuationFlexColumns.size - 2)
                        ?.browsePlaylistSongsContinuationMusicResponsiveListItemFlexColumnRenderer
                        ?.browsePlaylistSongsContinuationText
                        ?.browsePlaylistSongsContinuationRuns
                        ?.firstOrNull()
                        ?.browsePlaylistSongsContinuationText ?: ""
                }

                allSongsList.add(
                    Song(
                        id = id,
                        title = title,
                        artists = artistList,
                        albumId = albumId,
                        duration = duration,
                        thumbnail = thumbnail
                    )
                )
            }

            val continuationToken = items.last().browsePlaylistSongsContinuationContinuationItemRenderer
                ?.browsePlaylistSongsContinuationContinuationEndpoint
                ?.browsePlaylistSongsContinuationContinuationCommand
                ?.browsePlaylistSongsContinuationToken

            currentToken = continuationToken

            println("Fetched page. Current total songs: ${allSongsList.size}")

        } catch (e: Exception) {
            e.printStackTrace()
            currentToken = null
        }
    }
    return allSongsList
}