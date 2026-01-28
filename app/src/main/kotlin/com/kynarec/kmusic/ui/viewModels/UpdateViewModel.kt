package com.kynarec.kmusic.ui.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kynarec.kmusic.service.update.DownloadStatus
import com.kynarec.kmusic.service.update.UpdateInfo
import com.kynarec.kmusic.service.update.UpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateViewModel(private val updateManager: UpdateManager): ViewModel() {
    var updateInfo by mutableStateOf<UpdateInfo?>(null)
        private set

    var downloadStatus by mutableStateOf<DownloadStatus>(DownloadStatus.NotStarted)
        private set

    var showDialog by mutableStateOf(false)
        private set

    suspend fun checkForUpdates() {
        updateInfo = updateManager.checkForUpdate()
        showDialog = updateInfo != null
    }

    fun startDownload() {
        val info = updateInfo ?: return

        // Launch coroutine to collect download progress
        CoroutineScope(Dispatchers.Main).launch {
            updateManager.downloadAndInstall(info).collect { status ->
                downloadStatus = status

                if (status is DownloadStatus.Completed) {
                    // Installation will be triggered automatically
                    showDialog = false
                }

                if (status is DownloadStatus.Error) {
                    // Handle error - maybe show a toast or error dialog
                    showDialog = false
                }
            }
        }
    }

    fun openStore() {
        updateInfo?.let { updateManager.openStoreOrDownloadPage(it) }
        showDialog = false
    }

    fun dismissDialog() {
        if (downloadStatus !is DownloadStatus.Progress) {
            showDialog = false
            downloadStatus = DownloadStatus.NotStarted
        }
    }
}