package com.kynarec.kmusic.service.innertube

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.http.content.ByteArrayContent
import io.ktor.util.toMap
import kotlinx.coroutines.runBlocking
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException

class KtorDownloader : Downloader() {

    private val client = HttpClient(OkHttp)

    override fun execute(request: Request): Response = runBlocking {
        try {
            val response = client.request(request.url()) {
                method = HttpMethod(request.httpMethod())

                // NewPipe headers
                request.headers().forEach { (name, values) ->
                    values.forEach { value ->
                        header(name, value)
                    }
                }

                // POST body, if present
                request.dataToSend()?.let { data ->
                    setBody(ByteArrayContent(data))
                }
            }

            val body = response.bodyAsText()

            Response(
                response.status.value,
                response.status.description,
                response.headers.toMap(),
                body,
                response.request.url.toString()
            )
        } catch (e: Exception) {
            throw IOException("Request failed: ${request.url()}", e)
        }
    }
}