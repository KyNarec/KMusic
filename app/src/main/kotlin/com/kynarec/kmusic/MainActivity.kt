package com.kynarec.kmusic


//import com.kynarec.kmusic.service.PlayerService
import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.service.update.PlatformContext
import com.kynarec.kmusic.ui.screens.MainScreen
import com.kynarec.kmusic.utils.setJustStartedUp
import com.kynarec.kmusic.utils.setPlayerOpen
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init


class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"
    private var mediaController: MediaController? = null

    // Player.Listener to handle state changes and update the UI accordingly.
    private val playerListener = object : Player.Listener {
        // This callback is triggered whenever the player's state changes.
//        override fun onPlaybackStateChanged(playbackState: Int) {
//            super.onPlaybackStateChanged(playbackState)
//            when (playbackState) {
//                Player.STATE_READY, Player.STATE_BUFFERING -> {
//                    // Show the control bar whenever the player is ready or buffering.
//                    hidePlayerControlBar(false)
//                }
//                else -> {
//                    // Hide the control bar when playback is stopped, ended, or has an error.
//                    hidePlayerControlBar(true)
//                }
//            }
//        }
        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            // Hide the control bar if the playlist is empty, show it if not.
            //hidePlayerControlBar(timeline.isEmpty)
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. You can now show notifications.
            } else {
                // Permission denied. Explain to the user why it's needed.
                // You can show a custom dialog or message here.
            }
        }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        PlatformContext.initialize(applicationContext)
        FileKit.init(this)
        setContent {
            MainScreen()
        }


        this.setJustStartedUp(true)
        this.setPlayerOpen(false)

        val serviceIntent = Intent(this, PlayerServiceModern::class.java)
        startService(serviceIntent)
        Log.i(tag, "Started Media3 PlayerServiceModern")
    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        // Connect to the service's MediaSession on start.
        // This is a crucial step for the UI to be able to send commands to the service.
        val sessionToken = SessionToken(this, ComponentName(this, PlayerServiceModern::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                // Once the controller is ready, attach the listener to it.
                mediaController?.addListener(playerListener)
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onStop() {
        super.onStop()
        // It's important to release the MediaController when the app is no longer in the foreground.
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }
}