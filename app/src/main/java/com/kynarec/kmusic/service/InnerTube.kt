package com.kynarec.kmusic.service

import android.util.Log
import com.kynarec.innertube.InnerTube
import com.kynarec.innertube.YouTube
import com.kynarec.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.kynarec.innertube.models.Album
import com.kynarec.innertube.models.Artist
import com.kynarec.innertube.models.MusicResponsiveListItemRenderer
import com.kynarec.innertube.models.SongItem
import com.kynarec.innertube.models.YouTubeClient
import com.kynarec.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.kynarec.innertube.models.oddElements
import com.kynarec.innertube.models.response.NextResponse
import com.kynarec.innertube.models.response.PlayerResponse
import com.kynarec.innertube.models.response.SearchResponse
import com.kynarec.innertube.models.splitBySeparator
import com.kynarec.innertube.utils.*
import com.kynarec.innertube.pages.NextResult
import com.kynarec.innertube.pages.SearchPage
import com.kynarec.innertube.pages.SearchResult
import com.kynarec.kmusic.models.Song
import com.mewsic.innertube.InnertubeClient
import com.mewsic.innertube.enums.Client
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.cio.Response
import io.ktor.util.InternalAPI
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class InnerTube {
    val web = InnertubeClient(Client.WEB_REMIX)
    val ios = InnertubeClient(Client.ANDROID)
    val tag = "INNERTUBE SERVICE"

    // ID for Lost by Linkin Park   auGo9bUpBf8

    suspend fun main() {
        ios.config()
        val data = ios.player("auGo9bUpBf8")
        println("Here comes a lot of data: ")
        println()
        //println(data)

        val idk = InnerTube().player(
            YouTubeClient.ANDROID,
            "auGo9bUpBf8",
            playlistId = ""
        )

        println(idk)
    }

    fun getUrlById(videoId: String): String {
//        Log.i("InnerTube", "LetsTrySmth")
//        Log.i("InnerTube", "")
        var url = ""
        CoroutineScope(Dispatchers.Main).launch {

            val innertube = InnerTube().player(
                YouTubeClient.ANDROID,
                videoId,
                ""
            ).body<PlayerResponse>()

            for (i in innertube.streamingData?.adaptiveFormats?.toList()!!) {
                if (i.isAudio && i.bitrate > 100000) {
                    url = i.url ?: ""
                }
//                Log.i("InnerTube", "found something")

            }

        }
        return url
    }

    @OptIn(InternalAPI::class)
    fun getArtistById(videoId: String): String {
        var artist = ""
        CoroutineScope(Dispatchers.Main).launch {

//            val innertube = InnerTube().next(
//                YouTubeClient.WEB_REMIX,
//                videoId,
//                playlistId = "",
//                playlistSetVideoId = "",
//                index = null,
//                params = "",
//                continuation = ""
//            ).body<NextResponse>()
//            artist = innertube.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[0].tabRenderer.endpoint.toString()
            val results = YouTube.search(videoId, FILTER_SONG)
            val r = InnerTube().search(WEB_REMIX, videoId, FILTER_SONG.toString()).body<Response>()
            println(r)
//            listOf(results)[0].onSuccess { searchResult ->
//                val items = searchResult
//                artist = items
//                println(items)
//
//            }
        }
        return artist
    }

    fun getDurationById(videoId: String): String {
        var duration = ""
        CoroutineScope(Dispatchers.Main).launch {

            val innertube = InnerTube().next(
                YouTubeClient.ANDROID,
                videoId,
                playlistId = "",
                playlistSetVideoId = "",
                index = null,
                params = "",
                continuation = ""
            ).body<NextResult>()
            duration = innertube.items[0].duration.toString()
        }
        return duration
    }

    fun getSearchResultsByQuery2(query: String)
            : List<Song> {
        val songs = ArrayList<Song>()
        CoroutineScope(Dispatchers.Main).launch {
            val response = InnerTube().search(WEB_REMIX, query).body<SearchResponse>()
            println("response:$response")
            val puh = response.contents?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.lastOrNull()
                ?.musicShelfRenderer?.contents
            println(puh)
            val idk = response.contents?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.lastOrNull()
                ?.musicShelfRenderer?.contents?.mapNotNull {
                    searchResultsToSong(it.musicResponsiveListItemRenderer)?.let { it1 ->
                        songs.add(
                            it1
                        )
                    }
                }.orEmpty()
        }

        return songs
    }

    fun searchResultsToSong(renderer: MusicResponsiveListItemRenderer): Song? {
        val secondaryLine = renderer.flexColumns.getOrNull(1)
            ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator()
            ?: return null
//        return when {
//            renderer.isSong -> {
        println( "song Title: " +
            renderer.flexColumns.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                ?.firstOrNull()?.text
        )
        return Song(
            id = renderer.playlistItemData?.videoId ?: return null,
            title = renderer.flexColumns.firstOrNull()
                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                ?.firstOrNull()?.text ?: return null,
            artist = (secondaryLine.firstOrNull()?.oddElements()?.map {
                it.text
            } ?: return null).toString(),
//                    album = secondaryLine.getOrNull(1)?.firstOrNull()?.takeIf { it.navigationEndpoint?.browseEndpoint != null }?.let {
//                        Album(
//                            name = it.text,
//                            id = it.navigationEndpoint?.browseEndpoint?.browseId!!
//                        )
//                    },
            duration = secondaryLine.lastOrNull()?.firstOrNull()?.text?.parseTime().toString(),
            thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
                ?: return null,
//                    explicit = renderer.badges?.find {
//                        it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
//                    } != null
        )
    }
//
//            else -> null
    //}
    //   }

    fun getSearchResultsByQuery(query: String)
            : List<Song> {
        var list = ArrayList<Song>()
        CoroutineScope(Dispatchers.Main).launch {

//            val innertube =
//                InnerTube().search(YouTubeClient.ANDROID, query, FILTER_SONG.toString(), null)
            val results = YouTube.search(query, FILTER_SONG)
            Log.i("InnerTube", results.toString())
//            for (i in listOf(results)) {
//                Log.i("InnerTube", i.toString())
//                //i.toString()
//                break
//            }
            list = processSearchResults(listOf(results))
        }
        return list
    }

    fun processSearchResults(results: List<Result<SearchResult>>): ArrayList<Song> {
        val songList = ArrayList<Song>()
        results.forEach { result ->
            result.onSuccess { searchResult ->
                val items = searchResult.items
                // Process the items
                items.forEach { item ->
                    // Do something with each YTItem
                    println("this is the Item: $item")
                    songList.add(
                        Song(
                            item.id,
                            item.title,
                            getArtistById(item.id),
                            item.thumbnail,
                            getDurationById(item.id)
                        )
                    )
                }

                val continuation = searchResult.continuation
            }

            result.onFailure { exception ->
                println("Error processing search result: ${exception.message}")
            }

        }
        println("song list [1]: ${songList[0].artist}")
        return songList
    }

    fun searchSong(query: String): ArrayList<Song> {
        val i = InnerTube()
        val songList = ArrayList<Song>()
        CoroutineScope(Dispatchers.Main).launch {
            val response = i.search(WEB_REMIX, query, FILTER_SONG.toString()).body<SearchResponse>()
            val d = response.contents?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.lastOrNull()
                ?.musicShelfRenderer?.contents?.mapNotNull { it ->
//                    SearchPage.toYTItem(it.musicResponsiveListItemRenderer)
                    val renderer = it.musicResponsiveListItemRenderer
                    val secondaryLine = renderer.flexColumns.getOrNull(1)
                        ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.splitBySeparator()
                    Log.i(tag, "idk whats happening")
//                    if (renderer.isSong) {

                        songList.add(Song(
                            id = renderer.playlistItemData?.videoId ?: "NA",
                            title = renderer.flexColumns.firstOrNull()
                                ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs
                                ?.firstOrNull()?.text ?: "NA",
                            artist = secondaryLine?.firstOrNull()?.oddElements()?.map { it.text }.toString(),
//                            artist = secondaryLine?.firstOrNull()?.oddElements()?.toString().toString(),
                            thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: "NA",
                            duration = secondaryLine?.lastOrNull()?.firstOrNull()?.text?.parseTime().toString()
                        ))
//                    }
                    if (renderer.isAlbum) Log.i(tag, "its a album")
                    if (renderer.isPlaylist) Log.i(tag, "its a playlist")
                    if (renderer.isArtist) Log.i(tag, "its a artist")

                }
        }

        return songList
    }
}