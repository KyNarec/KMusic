package innertube

import com.kynarec.kmusic.data.db.entities.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

fun searchSongs(query: String): List<Song> {
    val results = mutableListOf<Song>()

    try {
        val data = InnerTube(CLIENTNAME.WEB_REMIX).search(
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
                    ?.optJSONArray("thumbnails")?.optJSONObject(0)?.optString("url", getYoutubeThumbnail(videoId))
                    ?: getYoutubeThumbnail(videoId)

            results.add(
                Song(
                    id = videoId,
                    title = title,
                    artist = artists.first(),
                    thumbnail = thumbnail ?: "",
                    duration = duration,
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

fun searchSongsFlow(query: String): Flow<Song> = flow {
    try {
        val data = InnerTube(CLIENTNAME.WEB_REMIX).search(
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
            return@flow
        }

        val sectionContents = tabs.optJSONObject(0)?.optJSONObject("tabRenderer")?.optJSONObject("content")
            ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")

        if (sectionContents == null) {
            println("No sectionListRenderer.contents found.")
            return@flow
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
            return@flow
        }

        val items = musicShelf.optJSONArray("contents") ?: JSONArray()
        println("musicShelfRenderer found. Number of items: ${items.length()}")

        for (i in 0 until items.length()) {
            val renderer = items.optJSONObject(i)?.optJSONObject("musicResponsiveListItemRenderer") ?: continue

            val flexColumns = renderer.optJSONArray("flexColumns") ?: continue

            val videoId = flexColumns.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs")?.optJSONObject(0)?.optJSONObject("navigationEndpoint")
                ?.optJSONObject("watchEndpoint")?.optString("videoId", "UnknownID") ?: "UnknownID"

            if (videoId == "UnknownID") continue

            val title = flexColumns.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text", "Unknown Title")
                ?: "Unknown Title"

            val artistRuns = flexColumns.optJSONObject(1)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                ?.optJSONObject("text")?.optJSONArray("runs") ?: JSONArray()

            val artists = mutableListOf<String>()
            for (j in 0 until artistRuns.length()) {
                val run = artistRuns.optJSONObject(j) ?: continue
                val text = run.optString("text") ?: continue

                val isArtist = run.optJSONObject("navigationEndpoint")?.optJSONObject("browseEndpoint")
                    ?.optJSONObject("browseEndpointContextSupportedConfigs")
                    ?.optJSONObject("browseEndpointContextMusicConfig")
                    ?.optString("pageType") == "MUSIC_PAGE_TYPE_ARTIST"

                if (isArtist) {
                    artists.add(text)
                }
            }
            if (artists.isEmpty()) artists.add("Unknown Artist")

            val duration = if (artistRuns.length() > 0) {
                artistRuns.optJSONObject(artistRuns.length() - 1)?.optString("text", "Unknown Duration")
                    ?: "Unknown Duration"
            } else "Unknown Duration"

            val thumbnail =
                renderer.optJSONObject("thumbnail")?.optJSONObject("musicThumbnailRenderer")?.optJSONObject("thumbnail")
                    ?.optJSONArray("thumbnails")?.optJSONObject(0)?.optString("url", getYoutubeThumbnail(videoId))
                    ?: getYoutubeThumbnail(videoId)

            val song = Song(
                id = videoId,
                title = title,
                artist = artists.first(),
                thumbnail = thumbnail ?: "",
                duration = duration
            )

            println("Emitting song #$i: $title by ${artists.joinToString()}")
            emit(song) // <-- emit each song progressively
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getYoutubeThumbnail(videoId: String): String? {
    val resolutions = listOf("maxresdefault", "sddefault", "hqdefault", "mqdefault", "default")
    val baseUrl = "https://img.youtube.com/vi/$videoId/"

    for (res in resolutions) {
        val url = "$baseUrl$res.jpg"
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).head().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    println("Found thumbnail for $videoId with resolution: $res")
                    return url
                }
            }
        } catch (e: IOException) {
            println("Error checking $url: ${e.message}")
            continue
        }
    }

    println("No standard thumbnail found for video $videoId")
    return null
}

fun playSongByIdWithBestBitrate(videoId: String): String {
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

fun getRadio(videoId: String): List<Song> {
    val results = mutableListOf<Song>()

    try {
        val data = InnerTube(CLIENTNAME.TVLITE).next(
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
                thumbnails?.optJSONObject(thumbnails.length() - 1)?.optString("url", getYoutubeThumbnail(songId))
                    ?: getYoutubeThumbnail(songId)

            results.add(
                Song(
                    id = videoId,
                    title = title,
                    artist = artists.first(),
                    thumbnail = thumbnailUrl ?: "",
                    duration = duration,
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