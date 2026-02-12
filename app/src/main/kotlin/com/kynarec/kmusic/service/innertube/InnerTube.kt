package com.kynarec.kmusic.service.innertube

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class InnerTube(
    val clientName: ClientName,
) {
    val musicResponsiveListItemRendererMask = "musicResponsiveListItemRenderer(flexColumns,fixedColumns,thumbnail,navigationEndpoint,badges)"
    val _QPmE9fYezr = "lEi9YM74OL"
    val _1Vv31MecRl = "CrQ0JjAXgv"


    @JvmInline
    value class SearchFilter(val value: String) {
        companion object {
            val Song = SearchFilter("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
            val Video = SearchFilter("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D")
            val Album = SearchFilter("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D")
            val Artist = SearchFilter("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D")
            val CommunityPlaylist = SearchFilter("EgeKAQQoAEABagoQAxAEEAoQCRAF")
            val FeaturedPlaylist = SearchFilter("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D")
            val Podcast = SearchFilter("EgWKAQJQAWoIEBAQERADEBU%3D")
        }
    }


    @OptIn(ExperimentalSerializationApi::class)
    private fun buildClient() = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            //protobuf()
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            //brotli(1.0F)
            gzip(0.9F)
            deflate(0.8F)
        }

        install(HttpCache)



        engine {
//            addInterceptor(
//                HttpLoggingInterceptor().apply {
//                    level = HttpLoggingInterceptor.Level.BODY
//                }
//            )

            config {
                followRedirects(true)
                followSslRedirects(true)
                retryOnConnectionFailure(true)
                pingInterval(1, TimeUnit.SECONDS)
            }

        }

        defaultRequest {
            url(scheme = "https", host = _1Vv31MecRl) {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                parameters.append("prettyPrint", "false")
            }
        }
    }

    var client = buildClient()

    internal fun HttpRequestBuilder.mask(value: String = "*") =
        header("X-Goog-FieldMask", value)

    @Serializable
    data class SearchSuggestion(
        val input: String,
        val context: SearchSuggestionContext,
    )

    @Serializable
    data class SearchSuggestionContext(
        val client: SearchSuggestionClient,
    )

    @Serializable
    data class SearchSuggestionClient(
        val clientName: String,
        val clientVersion: String,
    )

    suspend fun getYoutubeMusicSearchSuggestion(query: String): String {

        val url = "https://music.youtube.com/youtubei/v1/music/get_search_suggestions"

        val requestBody = SearchSuggestion(
            input = query,
            context = SearchSuggestionContext(
                client = SearchSuggestionClient(
                    clientName = clientName.label,
                    clientVersion = clientName.version
                )
            ),
        )
        val json = Json { ignoreUnknownKeys = true }

        println(json.encodeToString(SearchSuggestion.serializer(), requestBody))

        try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)

                headers {
                    append("Accept", "application/json")
                    append("Accept-Charset", "UTF-8")
                    append("User-Agent", clientName.userAgent)
                }
            }

            if (!response.status.isSuccess()) {
                throw kotlinx.io.IOException("Unexpected response code: ${response.status}")
            }

            return response.bodyAsText()
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    suspend fun getYoutubeThumbnail(
        videoId: String,
    ): String? {
        val resolutions = listOf("maxresdefault", "sddefault", "hqdefault", "mqdefault", "default")
        val baseUrl = "https://img.youtube.com/vi/$videoId/"

        for (res in resolutions) {
            val url = "$baseUrl$res.jpg"
            try {
                val response: HttpResponse = client.head(url)
                if (response.status.isSuccess()) {
                    println("Found thumbnail for $videoId with resolution: $res")
                    return url
                }
            } catch (e: Exception) {
                println("Error checking $url: ${e.message}")
                continue
            }
        }

        println("No standard thumbnail found for video $videoId")
        return null
    }


    @Serializable
    data class SearchRequest(
        val context: SearchContext,
        val query: String,
        val params: String,
        val continuation: String? = null
    )

    @Serializable
    data class SearchContext(
        val client: SearchClient,
        val request: SearchRequestInfo = SearchRequestInfo(),
        val user: SearchUser = SearchUser()
    )

    @Serializable
    data class SearchClient(
        val clientName: String,
        val clientVersion: String,
        val hl: String = "en",
        val gl: String = "US",
        val visitorData: String,
        val userAgent: String,
        val xClientName: Int = 67,
        val loginSupported: Boolean = true,
        val loginRequired: Boolean = false,
        val useSignatureTimestamp: Boolean = true,
        val useWebPoTokens: Boolean = true,
        val isEmbedded: Boolean = false
    )

    @Serializable
    data class SearchRequestInfo(
        val internalExperimentFlags: List<String> = emptyList(),
        val useSsl: Boolean = true
    )

    @Serializable
    data class SearchUser(
        val lockedSafetyMode: Boolean = false
    )

    suspend fun search(
        query: String,
        params: String,
        continuation: String? = null,
    ): String {
        val url = "https://music.youtube.com/youtubei/v1/search?prettyPrint=false"

        val requestBody = SearchRequest(
            context = SearchContext(
                client = SearchClient(
                    clientName = clientName.label,
                    clientVersion = clientName.version,
                    visitorData = "CgtvVjVmYkVwajRBUSjvn-nHBjIKCgJERRIEEgAgaw%3D%3D",
                    userAgent = clientName.userAgent
                )
            ),
            query = query,
            params = params,
            continuation = continuation
        )

        val json = Json { ignoreUnknownKeys = true }

        println(json.encodeToString(SearchRequest.serializer(), requestBody))

        try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)

                headers {
                    append(
                        "X-Goog-FieldMask",
                        "contents.tabbedSearchResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents.musicShelfRenderer(continuations,contents.musicResponsiveListItemRenderer(flexColumns,fixedColumns,thumbnail,navigationEndpoint,badges))"
                    )
                    append("Accept", "application/json")
                    append("Accept-Charset", "UTF-8")
                    append("User-Agent", clientName.userAgent)
                }
            }

            if (!response.status.isSuccess()) {
                throw Exception("Unexpected response code: ${response.status}")
            }

            return response.bodyAsText()

        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }


    fun oldSearch(
        query: String,
        params: String,
        continuation: String?
    ): String {
        client

        val url = "https://music.youtube.com/youtubei/v1/search?prettyPrint=false"

        val jsonBody = """
        {
            "context": {
                "client": {
                    "clientName": "${clientName.label}",
                    "clientVersion": "${clientName.version}",
                    "hl": "en",
                    "gl": "US",
                    "visitorData": "CgtvVjVmYkVwajRBUSjvn-nHBjIKCgJERRIEEgAgaw%3D%3D",
                    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0",
                    "xClientName": 67,
                    "loginSupported": true,
                    "loginRequired": false,
                    "useSignatureTimestamp": true,
                    "useWebPoTokens": true,
                    "isEmbedded": false
                },
                "request": {
                    "internalExperimentFlags": [],
                    "useSsl": true
                },
                "user": {
                    "lockedSafetyMode": false
                }
            },
            "query": "$query",
            "params": "$params"
        }
    """.trimIndent()

        println(jsonBody)

        val mediaType = "application/json".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("X-Goog-FieldMask", "contents.tabbedSearchResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents.musicShelfRenderer(continuations,contents.musicResponsiveListItemRenderer(flexColumns,fixedColumns,thumbnail,navigationEndpoint,badges))")
            // Don't manually set Accept-Encoding - let OkHttp handle it for automatic decompression
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Charset", "UTF-8")
            .addHeader("User-Agent", clientName.userAgent)
            .addHeader("Content-Type", "application/json")
            .build()
//
//        try {
//            client.newCall(request).execute().use { response ->
//                if (!response.isSuccessful) {
//                    throw IOException("Unexpected response code: $response")
//                }
//
//                val responseBody = response.body?.string()
//                return responseBody ?: ""
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
        return ""
    }

    @Serializable
    data class PlayerRequest(
        val videoId: String,
        val context: PlayerContext
    )

    @Serializable
    data class PlayerContext(
        val client: PlayerClient
    )

    @Serializable
    data class PlayerClient(
        val clientName: String,
        val clientVersion: String
    )

    suspend fun player(
        id: String,
    ): String {
        val url = "https://music.youtube.com/youtubei/v1/player"

        val requestBody = PlayerRequest(
            videoId = id,
            context = PlayerContext(
                client = PlayerClient(
                    clientName = clientName.label,
                    clientVersion = clientName.version
                )
            )
        )

        Json { ignoreUnknownKeys = true }

        try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)

                headers {
                    append("Host", "youtubei.googleapis.com")
                    append("Accept", "application/json")
                    append("Accept-Charset", "UTF-8")
                    append("Connection", "keep-alive")
                    append("User-Agent", clientName.userAgent)
                }
            }

            val body = response.bodyAsText()

            if (!response.status.isSuccess()) {
                println("Request failed: ${response.status}")
            }

            return body

        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun next(
        videoId: String,
        playlistId: String?,
        params: String?,
        continuation: String?
    ): String {
        val url = "https://music.youtube.com/youtubei/v1/next"
        // JSON body from your screenshot
        val json = JSONObject().apply {
            put("videoId", videoId)
            if (playlistId != null) put("playlistId", playlistId)
            if (params != null) put("params", params)
            if (continuation != null) put("continuation", continuation)

            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", clientName.label)
                    put("clientVersion", clientName.version)
                })
            })
        }


        val mediaType = "application/json".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)
        println(json.toString(2))
        val client = OkHttpClient.Builder()
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Host", "youtubei.googleapis.com")
//            .header("Accept", "*/*")
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Charset", "UTF-8")
            .header("Connection", "keep-alive")
            .addHeader("User-Agent", clientName.userAgent)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response: Response ->
            val bodyString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                println("Request failed: ${response.code}")
            } else {
                //println(bodyString)
                return bodyString
            }
        }
        println("Request failed and no response received.")
        return ""
    }

    @Serializable
    data class NextRequest(
        val videoId: String,
        val playlistId: String? = null,
        val params: String? = null,
        val continuation: String? = null,
        val context: NextContext
    )

    @Serializable
    data class NextContext(
        val client: NextClientInfo
    )

    @Serializable
    data class NextClientInfo(
        val clientName: String,
        val clientVersion: String
    )

    private val jsonSetting = Json { prettyPrint = true }

    suspend fun betterNext(
        videoId: String,
        playlistId: String?,
        params: String?,
        continuation: String?,
    ): String {

        val url = "https://music.youtube.com/youtubei/v1/next"

        val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

        val requestBody = NextRequest(
            videoId = videoId,
            playlistId = playlistId,
            params = params,
            continuation = continuation,
            context = NextContext(
                client = NextClientInfo(
                    clientName = clientName.label,
                    clientVersion = clientName.version
                )
            )
        )

        val bodyString = json.encodeToString(requestBody)
        println(json.encodeToString(jsonSetting.encodeToJsonElement(requestBody)))

        try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(bodyString)

                headers {
                    append("Host", "youtubei.googleapis.com")
                    append("Accept", "application/json")
                    append("Accept-Charset", "UTF-8")
                    append("Connection", "keep-alive")
                    append("User-Agent", clientName.userAgent)
                }
            }

            if (!response.status.isSuccess()) {
                println("Request failed: ${response.status.value}")
                return ""
            }

            return response.bodyAsText()

        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    @Serializable
    data class BrowseRequest(
        val browseId: String,
        val params: String? = null,
        val context: BrowseContext,
        val continuation: String? = null
    )

    @Serializable
    data class BrowseContext(
        val client: BrowseClient
    )

    @Serializable
    data class BrowseClient(
        val clientName: String,
        val clientVersion: String
    )

    suspend fun browse(
        browseId: String,
        params: String?,
        continuation: String? = null,
    ): NetworkResult<String> {
        val url = "https://music.youtube.com/youtubei/v1/browse"

        val requestBody = BrowseRequest(
            browseId = browseId,
            params = params,
            continuation = continuation,
            context = BrowseContext(
                client = BrowseClient(
                    clientName = ClientName.WebRemix.label,
                    clientVersion = ClientName.WebRemix.version
                )
            )
        )

        val json = Json { ignoreUnknownKeys = true }

        println(json.encodeToString(BrowseRequest.serializer(), requestBody))

        try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)

                headers {
                    append("Accept", "*/*")
                    append("Accept-Charset", "UTF-8")
                    append("User-Agent", clientName.userAgent)
                }
            }

            if (!response.status.isSuccess()) {
                return NetworkResult.Failure.NetworkError
            }

            return NetworkResult.Success(response.bodyAsText())

        } catch (e: Exception) {
            e.printStackTrace()
            return NetworkResult.Failure.NetworkError
        }
    }
}