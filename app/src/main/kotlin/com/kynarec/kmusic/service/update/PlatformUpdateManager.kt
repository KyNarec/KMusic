package com.kynarec.kmusic.service.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.text.contains
import kotlin.text.get

class PlatformUpdateManager() : UpdateManager {
    val _1Vv31MecRl = "CrQ0JjAXgv"

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

//        install(ContentEncoding) {
//            //brotli(1.0F)
//            gzip(0.9F)
//            deflate(0.8F)
//        }

        install(HttpCache)



        engine {
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
    private val context: Context
        get() = PlatformContext.get() as Context

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            Log.i("PlatformUpdateManager", "Checking for updates...")

            val release = client.get("https://api.github.com/repos/KyNarec/KMusic/releases/latest")
                .body<GitHubRelease>()

            val currentVersionString = getCurrentVersion()
            Log.i("PlatformUpdateManager", "Current version string: $currentVersionString")

            val currentVersion = Version.parse(currentVersionString)
            Log.i("PlatformUpdateManager", "Current version parsed: $currentVersion")

            if (currentVersion == null) {
                Log.e("PlatformUpdateManager", "Failed to parse current version")
                return@withContext null
            }

            val latestVersion = Version.parse(release.tagName.removePrefix("v"))
            Log.i("PlatformUpdateManager", "Latest version parsed: $latestVersion")

            if (latestVersion == null) {
                Log.e("PlatformUpdateManager", "Failed to parse latest version")
                return@withContext null
            }

            Log.i("PlatformUpdateManager", "Current version: $currentVersion, Latest version: $latestVersion")

            if (latestVersion > currentVersion) {
                // Log all available assets
                Log.i("PlatformUpdateManager", "Available assets:")
                release.assets.forEach { asset ->
                    Log.i("PlatformUpdateManager", "  - ${asset.name}")
                }
                Log.i("PlatformUpdateManager", "Device ABIs: ${Build.SUPPORTED_ABIS.joinToString()}")

                // Try to find APK matching device ABI
                var apkAsset = release.assets.find {
                    it.name.endsWith(".apk") && it.name.contains(Build.SUPPORTED_ABIS[0])
                }

                // Fallback: try any supported ABI
                if (apkAsset == null) {
                    apkAsset = release.assets.find { asset ->
                        asset.name.endsWith(".apk") && Build.SUPPORTED_ABIS.any { abi ->
                            asset.name.contains(abi, ignoreCase = true)
                        }
                    }
                }

                // Last resort: just grab any APK (universal build)
                if (apkAsset == null) {
                    apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                }

                Log.i("PlatformUpdateManager", "Selected APK: ${apkAsset?.name}")

                if (apkAsset == null) {
                    Log.e("PlatformUpdateManager", "No APK found in release assets!")
                    return@withContext null
                }

                UpdateInfo(
                    version = release.tagName,
                    releaseNotes = release.body,
                    downloadUrl = apkAsset.browserDownloadUrl,
                    storeUrl = "https://play.google.com/store/apps/details?id=${context.packageName}",
                    releaseDate = release.publishedAt
                )
            } else {
                Log.i("PlatformUpdateManager", "No update available")
                null
            }
        } catch (e: Exception) {
            Log.e("PlatformUpdateManager", "Error checking for updates", e)
            e.printStackTrace()
            null
        }
    }

    override fun downloadAndInstall(updateInfo: UpdateInfo): Flow<DownloadStatus> = flow {
        val downloadUrl = updateInfo.downloadUrl ?: throw IllegalStateException("No download URL")

        emit(DownloadStatus.Progress(0, 0, 0))

        val apkFile = File(context.getExternalFilesDir("updates"), "latest.apk")
        apkFile.parentFile?.mkdirs()

        // --- Ktor File Download ---
        val response: HttpResponse = client.get(downloadUrl) {
            // Tell Ktor not to check for success codes for the download flow
            // Though not strictly necessary here since it's an override flow,
            // it's good practice for downloads if expectSuccess is true globally.
        }

        val body: ByteReadChannel = response.body() // Get the streaming body

        try {
            // Determine the total size for accurate percentage tracking
            val totalBytes = response.contentLength() ?: -1L
            var downloadedBytes = 0L

            apkFile.outputStream().use { output ->
                val buffer = ByteArray(8192)

                // This loop reads chunks directly from the channel into the buffer
                // readAvailable() returns the number of bytes read, or -1 if the channel is closed.
                var bytesRead: Int
                while (body.readAvailable(buffer).also { bytesRead = it } >= 0) {

                    // If bytesRead is greater than zero, write the chunk
                    if (bytesRead > 0) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        // Emit progress
                        if (totalBytes > 0) {
                            val percent = ((downloadedBytes * 100) / totalBytes).toInt()
                            emit(DownloadStatus.Progress(percent, downloadedBytes, totalBytes))
                        } else {
                            // Handle unknown length (if totalBytes is -1)
                            emit(DownloadStatus.Progress(0, downloadedBytes, totalBytes))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PlatformUpdateManager", "Error during file download", e)
            emit(DownloadStatus.Error(e.message?: "Unknown error", e))
            return@flow
        }

        emit(DownloadStatus.Completed(apkFile.absolutePath))
        // Trigger installation
        installApk(apkFile)
    }.flowOn(Dispatchers.IO)

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    }

    override fun openStoreOrDownloadPage(updateInfo: UpdateInfo) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.storeUrl))
        context.startActivity(intent)
    }

    override fun supportsInAppInstallation(): Boolean = true
}