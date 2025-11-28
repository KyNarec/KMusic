package com.kynarec.kmusic.service.update

import kotlinx.coroutines.flow.Flow

/**
 * Cross-platform update manager interface
 */
interface UpdateManager {
    /**
     * Checks if an update is available
     * @return UpdateInfo if available, null otherwise
     */
    suspend fun checkForUpdate(): UpdateInfo?

    /**
     * Downloads and installs the update (if platform supports it)
     * @return Flow of download progress
     */
    fun downloadAndInstall(updateInfo: UpdateInfo): Flow<DownloadStatus>

    /**
     * Opens the platform-specific store/download page
     */
    fun openStoreOrDownloadPage(updateInfo: UpdateInfo)

    /**
     * Whether this platform supports in-app installation
     */
    fun supportsInAppInstallation(): Boolean
}