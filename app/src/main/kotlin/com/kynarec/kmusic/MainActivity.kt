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
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.service.innertube.NetworkResult
import com.kynarec.kmusic.service.innertube.getAlbumAndSongs
import com.kynarec.kmusic.service.update.PlatformContext
import com.kynarec.kmusic.ui.screens.MainScreen
import com.kynarec.kmusic.ui.viewModels.AppAction
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.setJustStartedUp
import com.kynarec.kmusic.utils.setPlayerOpen
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private var mediaController: MediaController? = null

    private val playerRepository: PlayerRepository by inject()
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
                "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                    val browseId = "VL$playlistId"

                    if (playlistId.startsWith("OLAK5uy_")) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            when(val res = getAlbumAndSongs(browseId)) {
                                is NetworkResult.Success -> withContext(Dispatchers.Main) {
                                    if (res.data.songs.isEmpty())
                                        appViewModel.onAction(AppAction.OpenPlaylistOnlineDetailScreen(browseId))
                                    else
                                        appViewModel.onAction(AppAction.OpenAlbumDetailScreen(res.data.songs.first().albumId!!))
                                }
                                else -> withContext(Dispatchers.Main) {
                                    SmartMessage(
                                        "NetworkError",
                                        PopupType.Error,
                                        false,
                                        this@MainActivity
                                    )
                                }
                            }
                        }
                    } else {
                        appViewModel.onAction(AppAction.OpenPlaylistOnlineDetailScreen(browseId))
                    }
                }

                // Todo: Handle @Metallica e.g.
                "channel", "c" -> uri.lastPathSegment?.let { channelId ->
                    appViewModel.onAction(AppAction.OpenArtistDetailScreen(artistId = channelId))
                }

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