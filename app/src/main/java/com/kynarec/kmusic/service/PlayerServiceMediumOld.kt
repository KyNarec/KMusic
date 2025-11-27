package com.kynarec.kmusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.kynarec.kmusic.MainActivity
import com.kynarec.kmusic.R

@Deprecated("Use PlayerServiceModern")
class PlayerServiceMediumOld : MediaLibraryService() {
    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var player: ExoPlayer
    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private lateinit var mediaNotification: MediaNotification


    // Create the MediaLibrarySession.Callback
    @UnstableApi
    private val callback = object : MediaLibrarySession.Callback {
//        override fun onGetLibraryRoot(
//            session: MediaLibrarySession,
//            browser: MediaSession.ControllerInfo,
//            params: MediaLibraryService.LibraryParams?
//        ): ListenableFuture<LibraryResult<MediaItem>> {
//            // Create a simple root item
//            val rootItem = MediaItem.Builder()
//                .setMediaId("root")
//                .setMediaMetadata(
//                    MediaMetadata.Builder()
//                    .setTitle("KMusic")
//                    .setIsBrowsable(true)
//                    .setIsPlayable(false)
//                    .build())
//                .build()
//
//            return Futures.immediateFuture(LibraryResult.ofItem(rootItem))
//        }

//        override fun onGetItem(
//            session: MediaLibrarySession,
//            browser: MediaSession.ControllerInfo,
//            mediaId: String
//        ): ListenableFuture<LibraryResult<MediaItem>> {
//            // Implement your logic to get a specific media item by ID
//            // For now, return an error since we're not fully implementing browsing yet
//            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
//        }
//
//        override fun onGetChildren(
//            session: MediaLibrarySession,
//            browser: MediaSession.ControllerInfo,
//            parentId: String,
//            page: Int,
//            pageSize: Int,
//            params: MediaLibraryService.LibraryParams?
//        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
//            // Return an empty list for now - we'll implement this later for Android Auto
//            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of()))
//        }
//
//        // Add other required methods with proper return types
//        override fun onSearch(
//            session: MediaLibrarySession,
//            browser: MediaSession.ControllerInfo,
//            query: String,
//            params: MediaLibraryService.LibraryParams?
//        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
//            return Futures.immediateFuture(LibraryResult.ofItemList(ImmutableList.of()))
//        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }

        override fun onUnsubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String
        ): ListenableFuture<LibraryResult<Void>> {
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }

        // This is crucial for handling MediaItems from your UI
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> {
            // This method is called when MediaItems are added from a controller (like your UI)
            // You need to add the playable URI here since it gets stripped out during IPC
            val updatedMediaItems = mediaItems.map { mediaItem ->
                // Use your Python backend to get the playable URI
                val uri = ""

                mediaItem.buildUpon()
                    .setUri(uri.toString())
                    .build()
            }
            return Futures.immediateFuture(updatedMediaItems)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Initialize the player
        player = ExoPlayer.Builder(this).build()

        mediaLibrarySession = MediaLibrarySession.Builder(
            this,
            player,
            callback
        )
            .setSessionActivity(getSessionActivity()) // This is important for notifications
            .build()

        // Create notification channel
        createNotificationChannel()

        // Start foreground service with a proper notification
        startForeground(NOTIFICATION_ID, createNotification())

        // Set up player listeners
        setupPlayerListeners()

        //startForeground(NOTIFICATION_ID, mediaNotification.notification)
    }

    // This is the crucial method that returns your session
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        // Release resources
        player.release()
        mediaLibrarySession.release()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = com.kynarec.kmusic.utils.Constants.NOTIFICATION_ID
        const val NOTIFICATION_CHANNEL_ID = "kmusic_playback_channel"
    }

    private fun createNotificationChannel() {
        // Create notification channel for media playback
        val channel = NotificationChannel(
            NOTIFICATION_ID.toString(),
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music playback controls"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun getSessionActivity(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createNotification(): Notification {
        // Create a MediaStyle notification
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("KMusic")
            .setContentText("Music player")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(getSessionActivity())
            .setStyle(Notification.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .build()
    }

    // Add this to handle notification updates
    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }


    private fun setupPlayerListeners() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateNotification()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateNotification()
            }
        })
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle media button events and other commands
        //mediaLibrarySession.handleMediaButtonEvent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun playSong(mediaId: String) {
        // Implement your logic to play a specific song
        // This will depend on how you structure your media items
    }

    fun skipToNext() {
        player.seekToNext()
    }

    fun skipToPrevious() {
        player.seekToPrevious()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

}