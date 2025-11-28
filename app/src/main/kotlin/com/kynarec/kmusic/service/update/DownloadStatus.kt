package com.kynarec.kmusic.service.update

sealed class DownloadStatus {
    object NotStarted : DownloadStatus()
    data class Progress(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadStatus()
    data class Completed(val filePath: String) : DownloadStatus()
    data class Error(val message: String, val throwable: Throwable?) : DownloadStatus()
}