package innertube

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class InnerTube(
    val clientName: CLIENTNAME,
) {
    fun search(
        query: String,
        params: String,
        continuation: String?
    ): String {
        val client = OkHttpClient()

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

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("X-Goog-FieldMask", "contents.tabbedSearchResultsRenderer.tabs.tabRenderer.content.sectionListRenderer.contents.musicShelfRenderer(continuations,contents.musicResponsiveListItemRenderer(flexColumns,fixedColumns,thumbnail,navigationEndpoint,badges))")
            // Don't manually set Accept-Encoding - let OkHttp handle it for automatic decompression
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Charset", "UTF-8")
            .addHeader("User-Agent", clientName.userAgent)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Unexpected response code: $response")
                }

                val responseBody = response.body?.string()
                return responseBody ?: ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun player(
        id: String
    ): String {
        val url = "https://youtubei.googleapis.com/youtubei/v1/player"
        // JSON body from your screenshot
        val json = """
        {
          "videoId": "$id",
          "context": {
            "client": {
              "clientName": "${clientName.label}",
              "clientVersion": "${clientName.version}"
            }
          }
        }
    """.trimIndent()

        val mediaType = "application/json".toMediaType()
        val requestBody = json.toRequestBody(mediaType)

        // Optional: Add logging for debugging

        // OkHttp client with logging interceptor
        val client = OkHttpClient.Builder()
            // Add proxy if you want to test with mitmproxy
            //.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 8080)))
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Host", "youtubei.googleapis.com")
//            .header("Accept", "*/*")
            .addHeader("Accept", "application/json")
//            .header("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Accept-Charset", "UTF-8")
            .header("Connection", "keep-alive")
//            .header("X-Goog-Api-Format-Version", "1")
//            .header("X-YouTube-Client-Name", "3")
//            .header("X-YouTube-Client-Version", "19.17.34")
            .addHeader("User-Agent", clientName.userAgent)
            //.header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
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

    fun next(
        videoId: String,
        playlistId: String?,
        params: String?,
        continuation: String?
    ): String {
        val url = "https://youtubei.googleapis.com/youtubei/v1/next"
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
}