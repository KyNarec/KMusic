package com.kynarec.kmusic.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.dao.GithubDao
import com.kynarec.kmusic.data.db.entities.GitHubRelease
import com.kynarec.kmusic.service.update.AppVersionProvider
import com.kynarec.kmusic.service.update.DownloadStatus
import com.kynarec.kmusic.service.update.UpdateInfo
import com.kynarec.kmusic.service.update.Version
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds

class UpdateRepository(
    private val database: KmusicDatabase,
    private val githubDao: GithubDao,
    appVersionProvider: AppVersionProvider,
) {
    private lateinit var client: HttpClient
    private val json = Json { ignoreUnknownKeys = true }
    private var currentVersion: String

    init {
        CoroutineScope(Dispatchers.IO).launch {
            client = buildClient()
        }
        currentVersion = appVersionProvider.currentVersion
    }

    fun fetchReleases(): Flow<UpdateResult<List<GitHubRelease>>> = flow {
        try {
            emit(UpdateResult.Loading(0.2f))
            val response = client.get(
                "https://api.github.com/repos/KyNarec/KMusic/releases"
            ) {
                header("Accept", "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2026-03-10")
            }
            emit(UpdateResult.Loading(0.6f))
            println(response.bodyAsText())
            if (!response.status.isSuccess()) {
                val message = runCatching {
                    val error = json.decodeFromString<GithubRateLimitError>(
                        response.bodyAsText()
                    )
                    "${error.message}\n${error.documentationUrl}"
                }.getOrElse {
                    response.bodyAsText()
                }

                throw Exception(message)
            }
            val releases = json.decodeFromString<List<GitHubRelease>>(
                response.bodyAsText()
            )
            emit(UpdateResult.Loading(1f))
            delay(700.milliseconds)
            emit(UpdateResult.Success(releases))
            database.withTransaction {
                githubDao.insertReleases(releases)
            }
        } catch (e: Exception) {
            emit(UpdateResult.Error(e.message ?: "Unknown error"))
        }
    }

    suspend fun fetchDBReleases(): List<GitHubRelease> = githubDao.getAllReleases()

    @OptIn(ExperimentalSerializationApi::class)
    private fun buildClient() = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        engine {
            config {
                followRedirects(true)
                followSslRedirects(true)
                retryOnConnectionFailure(true)
                pingInterval(1, TimeUnit.SECONDS)
            }

        }
    }


    fun downloadAndInstall(release: GitHubRelease, context: Context): Flow<DownloadStatus> = flow {
        val downloadUrl = release.assets.first().browserDownloadUrl


        val apkFile = File(context.getExternalFilesDir("updates"), "KMusic-${release.tagName}.apk")
        apkFile.parentFile?.mkdirs()

        if (apkFile.exists()) {
            emit(DownloadStatus.Completed(apkFile.absolutePath))
            installApk(apkFile, context)
            return@flow
        }
        emit(DownloadStatus.Progress(0, 0, 0))


        val tempFile = File(apkFile.parent, "${apkFile.name}.download")

        try {
            val response: HttpResponse = client.get(downloadUrl)
            val body: ByteReadChannel = response.body()

            // Determine the total size for accurate percentage tracking
            val totalBytes = response.contentLength() ?: -1L
            var downloadedBytes = 0L

            tempFile.outputStream().use { output ->
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
            tempFile.renameTo(apkFile)
        } catch (e: Exception) {
            Log.e("UpdateRepository", "Error during file download", e)
            emit(DownloadStatus.Error(e.message?: "Unknown error", e))
            return@flow
        }

        emit(DownloadStatus.Completed(apkFile.absolutePath))
        installApk(apkFile, context)
    }

    private fun installApk(file: File, context: Context) {
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

    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            Log.i("PlatformUpdateManager", "Checking for updates...")

            val release = client.get("https://api.github.com/repos/KyNarec/KMusic/releases/latest")
                .body<GitHubRelease>()

            val currentVersionString = currentVersion
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
                    storeUrl = null,
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
}

sealed interface UpdateResult<out T> {
    data class Success<out T>(val data: T): UpdateResult<T>

    data class Error(val message: String):UpdateResult<Nothing>

    data class Loading(val progress: Float = 0f):UpdateResult<Nothing>
}

@Serializable
data class GithubRateLimitError(
    val message: String,
    @SerialName("documentation_url") val documentationUrl: String
)