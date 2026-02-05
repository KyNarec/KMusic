package com.kynarec.kmusic.service

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import androidx.annotation.RequiresPermission
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import com.kynarec.kmusic.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@UnstableApi
class DownloadService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_CHANNEL_ID,
    R.string.download_channel_name,
    0
), KoinComponent {

    private val customDownloadManager: DownloadManager by inject()

    override fun getDownloadManager(): DownloadManager = customDownloadManager

    @RequiresPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED)
    override fun getScheduler(): Scheduler = PlatformScheduler(this, 1)

    override fun getForegroundNotification(
        downloads: List<Download>,
        notMetRequirements: Int
    ): Notification {
        val helper = DownloadNotificationHelper(this, DOWNLOAD_CHANNEL_ID)
        val lastDownload = downloads.lastOrNull()
        return if (lastDownload?.state == Download.STATE_COMPLETED) {
            val songTitle = lastDownload.request.data.decodeToString()

            helper.buildDownloadCompletedNotification(
                this,
                R.drawable.rounded_downloading_24,
                null, // ContentIntent (null or point to your Activity)
                "Download successful: $songTitle"
            ).apply {
                flags = flags or Notification.FLAG_AUTO_CANCEL
            }
        } else {
            helper.buildProgressNotification(
                this,
                R.drawable.rounded_downloading_24,
                null,
                null,
                downloads,
                notMetRequirements
            )
        }
    }

    init {

        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                if (download.state == Download.STATE_COMPLETED && finalException == null) {
                    val title = download.request.data.decodeToString()
                    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

                    val songNotification = Notification.Builder(this@DownloadService, DOWNLOAD_CHANNEL_ID)
                        .setSmallIcon(R.drawable.rounded_download_done_24)
                        .setContentTitle("Download Complete")
                        .setContentText(title)
                        .setGroup(DOWNLOAD_GROUP_KEY)
                        .setAutoCancel(true)
                        .build()

                    val summaryNotification = Notification.Builder(this@DownloadService, DOWNLOAD_CHANNEL_ID)
                        .setSmallIcon(R.drawable.rounded_download_done_24)
                        .setGroup(DOWNLOAD_GROUP_KEY)
                        .setGroupSummary(true)
                        .build()

                    nm.notify(download.request.id.hashCode(), songNotification)

                    nm.notify(SUMMARY_ID, summaryNotification)
                }
            }
        })
    }

    companion object {
        private const val DOWNLOAD_CHANNEL_ID = "download_channel"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val DOWNLOAD_GROUP_KEY = "com.kmusic.DOWNLOADS"
        private const val SUMMARY_ID = 42

    }
}