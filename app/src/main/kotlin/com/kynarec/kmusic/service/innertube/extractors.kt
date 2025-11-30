package com.kynarec.kmusic.service.innertube

import com.kynarec.kmusic.data.db.entities.Song
import innertube.CLIENTNAME
import innertube.InnerTube
import innertube.PARAMS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject


@Serializable
data class SearchSuggestionsResponse(
    val contents: List<SuggestionsSection> = emptyList()
)

@Serializable
data class SuggestionsSection(
    val searchSuggestionsSectionRenderer: SearchSuggestionsSectionRenderer? = null
)

@Serializable
data class SearchSuggestionsSectionRenderer(
    val contents: List<SuggestionItem> = emptyList()
)

@Serializable
data class SuggestionItem(
    val searchSuggestionRenderer: SearchSuggestionRenderer? = null
)

@Serializable
data class SearchSuggestionRenderer(
    val suggestion: SuggestionText? = null
)

@Serializable
data class SuggestionText(
    val runs: List<SuggestionRun> = emptyList()
)

@Serializable
data class SuggestionRun(
    val text: String? = null
)

fun searchSuggestions(input: String): Flow<String> = flow {
    val json = Json { ignoreUnknownKeys = true }
    val innerTubeClient = InnerTube(CLIENTNAME.WEB_REMIX)

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

@Serializable
data class SearchResponse(
    val contents: SearchContents? = null
)

@Serializable
data class SearchContents(
    val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer? = null
)

@Serializable
data class TabbedSearchResultsRenderer(
    val tabs: List<Tab>? = null
)

@Serializable
data class Tab(
    val tabRenderer: TabRenderer? = null
)

@Serializable
data class TabRenderer(
    val content: TabContent? = null
)

@Serializable
data class TabContent(
    val sectionListRenderer: SectionListRenderer? = null
)

@Serializable
data class SectionListRenderer(
    val contents: List<SectionContent>? = null
)

@Serializable
data class SectionContent(
    val musicShelfRenderer: MusicShelfRenderer? = null
)

@Serializable
data class MusicShelfRenderer(
    val contents: List<MusicItem>? = null
)

@Serializable
data class MusicItem(
    val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer? = null
)

@Serializable
data class MusicResponsiveListItemRenderer(
    val flexColumns: List<FlexColumn>? = null,
    val thumbnail: ThumbnailContainer? = null
)

@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: FlexColumnRenderer? = null
)

@Serializable
data class FlexColumnRenderer(
    val text: TextRenderer? = null
)

@Serializable
data class TextRenderer(
    val runs: List<TextRun>? = null
)

@Serializable
data class TextRun(
    val text: String? = null,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class NavigationEndpoint(
    val browseEndpoint: BrowseEndpoint? = null,
    val watchEndpoint: WatchEndpoint? = null
)

@Serializable
data class BrowseEndpoint(
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null
)

@Serializable
data class BrowseEndpointContextSupportedConfigs(
    val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig? = null
)

@Serializable
data class BrowseEndpointContextMusicConfig(
    val pageType: String? = null
)


@Serializable
data class WatchEndpoint(
    val videoId: String? = null
)

@Serializable
data class ThumbnailContainer(
    val musicThumbnailRenderer: MusicThumbnailRenderer? = null
)

@Serializable
data class MusicThumbnailRenderer(
    val thumbnail: Thumbnail? = null
)

@Serializable
data class Thumbnail(
    val thumbnails: List<ThumbnailItem>? = null
)

@Serializable
data class ThumbnailItem(
    val url: String? = null
)

fun searchSongsFlow(query: String): Flow<Song> = flow {
    val json = Json { ignoreUnknownKeys = true }
    val innerTubeClient = InnerTube(CLIENTNAME.WEB_REMIX)

    try {
        val raw = innerTubeClient.search(
            query,
            params = InnerTube.SearchFilter.Song.value,
            continuation = null
        )

        val response = json.decodeFromString<SearchResponse>(raw)
        val tabs = response.contents?.tabbedSearchResultsRenderer?.tabs ?: emptyList()
        if (tabs.isEmpty()) return@flow

        val sectionContents = tabs.first().tabRenderer?.content?.sectionListRenderer?.contents ?: emptyList()
        val musicShelf = sectionContents.firstOrNull { it.musicShelfRenderer != null }?.musicShelfRenderer ?: return@flow

        for (item in musicShelf.contents.orEmpty()) {
            val renderer = item.musicResponsiveListItemRenderer ?: continue

            // Video ID & title
            val flex0 = renderer.flexColumns?.getOrNull(0)?.musicResponsiveListItemFlexColumnRenderer
            val textRun0 = flex0?.text?.runs?.firstOrNull()
            val videoId = textRun0?.navigationEndpoint?.watchEndpoint?.videoId ?: continue
            val title = textRun0.text ?: "Unknown Title"

            // Artists
            val flex1 = renderer.flexColumns?.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer
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
//            val thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.firstOrNull()?.url
//                ?: innerTubeClient.getYoutubeThumbnail(videoId)
            val thumbnail = innerTubeClient.getYoutubeThumbnail(videoId)

            val song = Song(
                id = videoId,
                title = title,
                artist = artists.joinToString(", "),
                thumbnail = thumbnail ?: "",
                duration = duration
            )

            println("Emitting song: $title by ${artists.joinToString()}")
            emit(song)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}



suspend fun oldSearchSongs(query: String): List<Song> {
    val results = mutableListOf<Song>()
    val innerTubeClient = InnerTube(CLIENTNAME.WEB_REMIX)

    try {
        val data = innerTubeClient.search(
            query,
            params = PARAMS.SONG.label,
            continuation = null
        )

        println("Raw JSON response: $data")

        val jsonData = JSONObject(data)
        val tabs =
            jsonData.optJSONObject("contents")?.optJSONObject("tabbedSearchResultsRenderer")?.optJSONArray("tabs")

        if (tabs == null || tabs.length() == 0) {
            println("No tabs found in JSON.")
            return emptyList()
        }

        val sectionContents = tabs.optJSONObject(0)?.optJSONObject("tabRenderer")?.optJSONObject("content")
            ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")

        if (sectionContents == null) {
            println("No sectionListRenderer.contents found.")
            return emptyList()
        }

        println("Contents found, length: ${sectionContents.length()}")

        // Find musicShelfRenderer
        var musicShelf: JSONObject? = null
        for (i in 0 until sectionContents.length()) {
            val item = sectionContents.optJSONObject(i)
            if (item?.has("musicShelfRenderer") == true) {
                musicShelf = item.getJSONObject("musicShelfRenderer")
                break
            }
        }

        if (musicShelf == null) {
            println("musicShelfRenderer not found!")
            return emptyList()
        }

        val items = musicShelf.optJSONArray("contents") ?: JSONArray()
        println("musicShelfRenderer found. Number of items: ${items.length()}")

        for (i in 0 until items.length()) {
            val renderer = items.optJSONObject(i)?.optJSONObject("musicResponsiveListItemRenderer") ?: continue

            val flexColumns = renderer.optJSONArray("flexColumns")
            if (flexColumns == null) {
                println("flexColumns missing at index $i")
                continue
            }

            // Extract videoId from first flexColumn -> navigationEndpoint -> watchEndpoint
            val videoId = flexColumns.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs")?.optJSONObject(0)?.optJSONObject("navigationEndpoint")
                ?.optJSONObject("watchEndpoint")?.optString("videoId", "UnknownID") ?: "UnknownID"

            if (videoId == "UnknownID") {
                println("videoId missing at index $i")
                continue
            }

            // Title
            val title = flexColumns.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "Unknown Title")
                ?: "Unknown Title"

            // Artist(s)
            val artistRuns = flexColumns.optJSONObject(1)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs") ?: JSONArray()

            val artists = mutableListOf<String>()
            for (j in 0 until artistRuns.length()) {
                val run = artistRuns.optJSONObject(j) ?: continue
                val text = run.optString("text") ?: continue

                // Only include runs that have a browseEndpoint for artist pages
                val isArtist = run.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")
                    ?.optJSONObject("browseEndpointContextSupportedConfigs")
                    ?.optJSONObject("browseEndpointContextMusicConfig")
                    ?.optString("pageType") == "MUSIC_PAGE_TYPE_ARTIST"

                if (isArtist) {
                    artists.add(text)
                }
            }

            // Fallback if no artists were found
            if (artists.isEmpty()) artists.add("Unknown Artist")

            // Duration (usually last run)
            val duration = if (artistRuns.length() > 0) {
                artistRuns.optJSONObject(artistRuns.length() - 1)?.optString("text", "Unknown Duration")
                    ?: "Unknown Duration"
            } else "Unknown Duration"

            // Thumbnail
            val thumbnail =
                renderer.optJSONObject("thumbnail")?.optJSONObject("musicThumbnailRenderer")?.optJSONObject("thumbnail")
                    ?.optJSONArray("thumbnails")?.optJSONObject(0)?.optString("url", innerTubeClient.getYoutubeThumbnail(videoId))
                    ?: innerTubeClient.getYoutubeThumbnail(videoId)

            results.add(
                Song(
                    id = videoId,
                    title = title,
                    artist = artists.ifEmpty { listOf("Unknown Artist") }.first(),
                    thumbnail = thumbnail?: "",
                    duration = duration
                )
            )

            println("Parsed song #$i: id=$videoId, title=$title, artists=${artists.joinToString()}, duration=$duration")
        }

        println("Total songs parsed: ${results.size}")

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return results
}


suspend fun oldPlaySongByIdWithBestBitrate(videoId: String): String {
    val player = InnerTube(CLIENTNAME.ANDROID).player(
        videoId
    )
    //println(player)
    val data = JSONObject(player)
    val streamingData = data.optJSONObject("streamingData")
    val adaptiveFormats = streamingData?.optJSONArray("adaptiveFormats") ?: JSONArray()

    var highestBitrate = 0
    var bestUrl = ""

    for (i in 0 until adaptiveFormats.length()) {
        val item = adaptiveFormats.optJSONObject(i) ?: continue
        val audioQuality = item.optString("audioQuality", "")
        if (audioQuality == "AUDIO_QUALITY_HIGH" || audioQuality == "AUDIO_QUALITY_MEDIUM") {
            val bitrate = item.optInt("averageBitrate", -1)
            val url = item.optString("url", "")

            println("Bitrate: $bitrate  URL: $url")

            if (bitrate > highestBitrate && url.isNotEmpty()) {
                highestBitrate = bitrate
                bestUrl = url
            }
        }
    }

    println("Highest bitrate found: $highestBitrate")
    println("Highest bitrate URL: $bestUrl")

    return bestUrl
}

@Serializable
data class PlayerResponse(
    val streamingData: StreamingData? = null
)

@Serializable
data class StreamingData(
    val adaptiveFormats: List<AdaptiveFormat>? = null
)

@Serializable
data class AdaptiveFormat(
    val averageBitrate: Int? = null,
    val url: String? = null,
    val audioQuality: String? = null
)

suspend fun playSongByIdWithBestBitrate(videoId: String): String {
    val json = Json { ignoreUnknownKeys = true }

    val raw = InnerTube(CLIENTNAME.ANDROID).player(videoId)
    val response = json.decodeFromString<PlayerResponse>(raw)

    val best = response.streamingData
        ?.adaptiveFormats
        ?.filter { it.audioQuality in listOf("AUDIO_QUALITY_HIGH", "AUDIO_QUALITY_MEDIUM") }
        ?.filter { it.url != null }
        ?.maxByOrNull { it.averageBitrate ?: 0 }

    println("Best bitrate URL: ${best?.url}")

    return best?.url ?: ""
}



@Serializable
data class NextResponse(
    val contents: Contents? = null
)

@Serializable
data class Contents(
    val singleColumnWatchNextResults: SingleColumnWatchNextResults? = null
)

@Serializable
data class SingleColumnWatchNextResults(
    val playlist: InnerPlaylistWrapper? = null
)

@Serializable
data class InnerPlaylistWrapper(
    val playlist: Playlist? = null
)

@Serializable
data class Playlist(
    val title: String? = null,
    val contents: List<PlaylistContent>? = null
)

@Serializable
data class PlaylistContent(
    val playlistPanelVideoRenderer: PanelVideoRenderer? = null
)

@Serializable
data class PanelVideoRenderer(
    val videoId: String? = null,
    val title: TextRuns? = null,
    val shortBylineText: TextRuns? = null,
    val lengthText: TextRuns? = null,
    val thumbnail: Thumbnail? = null
)

@Serializable
data class TextRuns(
    val runs: List<Run>? = null
)

@Serializable
data class Run(
    val text: String? = null
)

fun getRadioFlow(
    videoId: String,
): Flow<Song> = flow {
    val json = Json { ignoreUnknownKeys = true }
    val innerTubeClient = InnerTube(CLIENTNAME.TVLITE)

    try {
        val raw = innerTubeClient.betterNext(
            videoId = videoId,
            playlistId = "RDAMVM$videoId",
            params = PARAMS.SONG.label,
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

            val thumbnail =
                renderer.thumbnail?.thumbnails?.lastOrNull()?.url
                    ?: innerTubeClient.getYoutubeThumbnail(id)

            val song = Song(
                id = id,
                title = title,
                artist = artists.joinToString(", "),
                thumbnail = thumbnail ?: "",
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


suspend fun oldGetRadio(videoId: String): List<Song> {
    val results = mutableListOf<Song>()
    val innerTubeClient = InnerTube(CLIENTNAME.TVLITE)

    try {
        val data = innerTubeClient.next(
            videoId = videoId,
            playlistId = "RDAMVM$videoId",
            params = null,
            continuation = null
        )
        println("Raw JSON response for radio: ${data.take(100000)}")
        // 3️⃣ Parse the JSON response
        val json = JSONObject(data)
        val playlist =
            json.optJSONObject("contents")
                ?.optJSONObject("singleColumnWatchNextResults")
                ?.optJSONObject("playlist")
                ?.optJSONObject("playlist")
                ?: run {
                    println("❌ Playlist not found in JSON.")
                    return emptyList()
                }

        val title = playlist.optString("title", "Unknown Mix")
        println("🎵 Radio playlist title: $title")

        val contents = playlist.optJSONArray("contents") ?: JSONArray()
        println("Found ${contents.length()} items in playlist.")

        // 4️⃣ Loop through playlist videos
        for (i in 0 until contents.length()) {
            val item = contents.optJSONObject(i)
                ?.optJSONObject("playlistPanelVideoRenderer")
                ?: continue

            val songId = item.optString("videoId", "UnknownID")
            val titleText =
                item.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "Unknown Title")
                    ?: "Unknown Title"

            val artistRuns =
                item.optJSONObject("shortBylineText")?.optJSONArray("runs") ?: JSONArray()

            val artists = mutableListOf<String>()
            for (j in 0 until artistRuns.length()) {
                val run = artistRuns.optJSONObject(j) ?: continue
                val artist = run.optString("text", "")
                if (artist.isNotEmpty()) artists.add(artist)
            }

            val duration =
                item.optJSONObject("lengthText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "") ?: ""

            val thumbnails =
                item.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
            val thumbnailUrl =
                thumbnails?.optJSONObject(thumbnails.length() - 1)?.optString("url", innerTubeClient.getYoutubeThumbnail(songId))
                    ?: innerTubeClient.getYoutubeThumbnail(songId)

            results.add(
                Song(
                    id = songId,
                    title = titleText,
                    artist = "asd",
                    thumbnail = thumbnailUrl?: "",
                    duration = duration
                )
            )

            println("Parsed song #$i: id=$songId, title=$titleText, artists=${artists.joinToString()}, duration=$duration")
        }

        println("✅ Total radio songs parsed: ${results.size}")

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return results
}