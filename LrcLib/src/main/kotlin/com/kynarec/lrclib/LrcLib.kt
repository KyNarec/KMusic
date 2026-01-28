package com.kynarec.lrclib

import com.kynarec.lrclib.model.Lyrics
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class LrcLib {
    @OptIn(ExperimentalSerializationApi::class)
    private fun buildClient() = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }

        install(HttpCache)

        engine {
            config {
                followRedirects(true)
                followSslRedirects(true)
                retryOnConnectionFailure(true)
                pingInterval(1, TimeUnit.SECONDS)
            }
        }
    }

    val client = buildClient()

    val baseUrl = "https://lrclib.net/api/"


   suspend fun search(title: String, artist: String, albumName: String? = null, duration: Int? = null) : String {
       try {
           val response = client.get("${baseUrl}search") {
               url {
                   parameters.append("track_name", title)
                   parameters.append("artist_name", artist)
                   if (albumName != null) parameters.append("album_name", albumName)
                   if (duration != null) parameters.append("duration", duration.toString())
               }
           }
           return response.bodyAsText()
       } catch (e: Exception) {
           e.printStackTrace()
           return ""
       }
   }

    suspend fun searchParsed(title: String, artist: String, albumName: String? = null): List<Lyrics> {
        val searchResult = search(title, artist, albumName)
        val json = Json { ignoreUnknownKeys = true }
        return json.decodeFromString<List<Lyrics>>(searchResult)
    }
}