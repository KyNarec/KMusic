package com.kynarec.kmusic.service.innertube

import android.util.Log
import com.kynarec.kmusic.service.innertube.responses.PlayerResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo

class UrlFetcher {
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }


    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            gzip()
        }
    }

    private var visitorId: String? = null

    suspend fun resolve(id: String): NetworkResult<String> {
        Log.i("UrlFetcher", "Resolving URL for ID: $id")
        if (visitorId == null) {
            when (val visitorIdRequest = getVisitorId()) {
                is NetworkResult.Success -> {

                    visitorId = visitorIdRequest.data
                }
                else -> return visitorIdRequest
            }
        }

        return when (val play = play(id, visitorId!!)) {
            is NetworkResult.Failure.ParsingError -> {
                Log.i("UrlFetcher", "Using backup resolve because of a parsing error")
                backupResolve(id)
            }
            else -> play
        }
    }

    private suspend fun play(id: String, visitorId: String): NetworkResult<String> {
        val request =
            client.post("https://youtubei.googleapis.com/youtubei/v1/player?prettyPrint=false&t=A5ayUnr8bhZG&id=$id") {
                headers {
                    append("accept", "*/*")
                    append("accept-encoding", "gzip")
                    append("accept-language", "en-GB, en;q=0.9")
                    append("connection", "Keep-Alive")
                    append("content-type", "application/json")
                    append("host", "youtubei.googleapis.com")
                    append(
                        "user-agent",
                        " com.google.visionos.youtube/1.02(RealityDevice14,1; U; CPU visionOS 25_6_0 like Mac OS X; GB)"
                    )
                    append("x-goog-api-format-version", "2")
                }

                setBody(
                    """
                    {
                      "context": {
                        "client": {
                          "clientName": "VISIONOS",
                          "clientVersion": "1.02",
                          "clientScreen": "WATCH",
                          "platform": "MOBILE",
                          "visitorData": "$visitorId",
                          "deviceMake": "Apple",
                          "deviceModel": "RealityDevice14,1",
                          "osName": "visionOS",
                          "osVersion": "25.6.0.23O471",
                          "hl": "en-GB",
                          "gl": "GB",
                          "utcOffsetMinutes": 0
                        },
                        "request": {
                          "internalExperimentFlags": [],
                          "useSsl": true
                        },
                        "user": {
                          "lockedSafetyMode": false
                        }
                      },
                      "videoId": "$id",
                      "cpn": "uSL632ZuKsCT3LKK",
                      "contentCheckOk": true,
                      "racyCheckOk": true
                    }
                """.trimIndent()
                )
            }
        if (request.status != HttpStatusCode.OK) return NetworkResult.Failure.NetworkError

        val text = request.bodyAsText()
        val response = json.decodeFromString<PlayerResponse>(text)

        val url: String? =
            response.streamingData?.adaptiveFormats
                ?.firstOrNull { it.itag == 251 }
                ?.url
                ?: response.streamingData?.formats
                    ?.firstOrNull { it.itag == 18 }
                    ?.url
        url?.let { return NetworkResult.Success(it) }

        return NetworkResult.Failure.ParsingError
    }

    private suspend fun getVisitorId(): NetworkResult<String> {
        val request = client.post("https://youtubei.googleapis.com/youtubei/v1/visitor_id?prettyPrint=false") {
            headers {
                append("accept", "*/*")
                append("accept-encoding", "gzip")
                append("accept-language", "en-GB, en;q=0.9")
                append("connection", "Keep-Alive")
                append("content-type", "application/json")
                append("host", "www.youtube.com")
                append("user-agent", "com.google.android.youtube/21.03.36 (Linux; U; Android 15; GB) gzip")
                append("x-goog-api-format-version", "2")
            }

            setBody(
                """
                    {
                      "context": {
                        "client": {
                          "clientName": "VISIONOS",
                          "clientVersion": "1.02",
                          "clientScreen": "WATCH",
                          "platform": "MOBILE",
                          "deviceMake": "Apple",
                          "deviceModel": "RealityDevice14,1",
                          "osName": "visionOS",
                          "osVersion": "25.6.0.23O471",
                          "hl": "en-GB",
                          "gl": "GB",
                          "utcOffsetMinutes": 0
                        },
                        "request": {
                          "internalExperimentFlags": [],
                          "useSsl": true
                        },
                        "user": {
                          "lockedSafetyMode": false
                        }
                      }
                    }
                """.trimIndent()
            )
        }

        @Serializable
        data class ResponseContext(
            val visitorData: String? = null
        )
        @Serializable
        data class VisitorIdResponse(
            val responseContext: ResponseContext
        )

        if (request.status != HttpStatusCode.OK) return NetworkResult.Failure.NetworkError

        val text = request.bodyAsText()
        json.decodeFromString<VisitorIdResponse>(text).responseContext.visitorData?.let {
            return NetworkResult.Success(it)
        }

        return NetworkResult.Failure.ParsingError
    }

    private suspend fun backupResolve(id: String): NetworkResult<String> {
        try {
            val info = StreamInfo.getInfo(
                ServiceList.YouTube,
                "https://www.youtube.com/watch?v=$id"
            )

            val audioStream = info.audioStreams
                .filter {
                    it.codec?.contains("opus", ignoreCase = true) == true
                }
                .maxByOrNull { it.averageBitrate }
                ?: info.audioStreams.maxByOrNull { it.averageBitrate }

            audioStream?.content?.let { return NetworkResult.Success(it) }
            return NetworkResult.Failure.ParsingError
        } catch (e: Exception) {
            e.printStackTrace()
            return NetworkResult.Failure.NetworkError
        }
    }
}