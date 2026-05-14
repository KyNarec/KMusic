package com.kynarec.kmusic


//import com.kynarec.kmusic.service.PlayerService
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import com.kynarec.kmusic.data.repository.PlayerRepository
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.service.update.PlatformContext
import com.kynarec.kmusic.ui.screens.MainScreen
import com.kynarec.kmusic.ui.viewModels.AppAction
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.utils.setJustStartedUp
import com.kynarec.kmusic.utils.setPlayerOpen
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private var mediaController: MediaController? = null

    private val playerRepository : PlayerRepository by inject()
    private val appViewModel: AppViewModel by viewModel()


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
        enableEdgeToEdge()
        requestNotificationPermission()
        PlatformContext.initialize(applicationContext)
        FileKit.init(this)
        handleIntent(intent)
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
    }

    override fun onStop() {
        super.onStop()
        mediaController?.release()
        mediaController = null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val uri: Uri? = intent.data

        Log.i(tag, "Received Intent: ${intent.data}")
        if (Intent.ACTION_VIEW == action && uri != null) {

            when (val path = uri.pathSegments.firstOrNull()) {
                else -> when {
                    path == "watch" -> uri.getQueryParameter("v")
                    else -> null
                }?.let { videoId ->
                    playerRepository.playSongByIdWithRadio(videoId)
                    appViewModel.onAction(AppAction.OpenPlayerSheet)

                    /**
                     * When the app is closed and an intent comes in the playerSheet would not open
                     * because of a timing issue.
                     */
                    lifecycleScope.launch(Dispatchers.IO) {
                        delay(500)
                        appViewModel.onAction(AppAction.OpenPlayerSheet)

                    }
                }
            }
        }
    }
}

/**
 *                 LaunchedEffect(intentUriData) {
 *                     val uri = intentUriData ?: return@LaunchedEffect
 *
 *                     SmartMessage(
 *                         message = "${"RiMusic "}${getString(R.string.opening_url)}",
 *                         durationLong = true,
 *                         context = this@MainActivity
 *                     )
 *
 *                     lifecycleScope.launch(Dispatchers.Main) {
 *                         when (val path = uri.pathSegments.firstOrNull()) {
 *                             "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
 *                                 val browseId = "VL$playlistId"
 *
 *                                 if (playlistId.startsWith("OLAK5uy_")) {
 *                                     Environment.playlistPage(BrowseBody(browseId = browseId))
 *                                         ?.getOrNull()?.let {
 *                                             it.songsPage?.items?.firstOrNull()?.album?.endpoint?.browseId?.let { browseId ->
 *                                                 navController.navigate(route = "${NavRoutes.album.name}/$browseId")
 *
 *                                             }
 *                                         }
 *                                 } else {
 *                                     navController.navigate(route = "${NavRoutes.playlist.name}/$browseId")
 *                                 }
 *                             }
 *
 *                             "channel", "c" -> uri.lastPathSegment?.let { channelId ->
 *                                 try {
 *                                     navController.navigate(route = "${NavRoutes.artist.name}/$channelId")
 *                                 } catch (e: Exception) {
 *                                     Timber.e("MainActivity.onCreate intentUriData ${e.stackTraceToString()}")
 *                                 }
 *                             }
 *
 *                             "search" -> uri.getQueryParameter("q")?.let { query ->
 *                                 navController.navigate(route = "${NavRoutes.searchResults.name}/$query")
 *                             }
 *
 *                             else -> when {
 *                                 path == "watch" -> uri.getQueryParameter("v")
 *                                 uri.host == "youtu.be" -> path
 *                                 else -> null
 *                             }?.let { videoId ->
 *                                 Environment.song(videoId)?.getOrNull()?.let { song ->
 *                                     val binder = snapshotFlow { binder }.filterNotNull().first()
 *                                     withContext(Dispatchers.Main) {
 *                                         if (!song.explicit && !preferences.getBoolean(
 *                                                 parentalControlEnabledKey,
 *                                                 false
 *                                             )
 *                                         )
 *                                             binder?.player?.forcePlay(song.asMediaItem)
 *                                         else
 *                                             SmartMessage(
 *                                                 "Parental control is enabled",
 *                                                 PopupType.Warning,
 *                                                 context = this@MainActivity
 *                                             )
 *                                     }
 *                                 }
 *                             }
 *                         }
 *                     }
 *                     intentUriData = null
 *                 }
 *
 *
 *                 //throw RuntimeException("This is a simulated exception to crash");
 *             //}
 *         }
 *     }
 *
 *
 */