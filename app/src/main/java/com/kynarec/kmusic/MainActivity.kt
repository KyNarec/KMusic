package com.kynarec.kmusic


//import com.kynarec.kmusic.service.PlayerService
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.PopupType
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.utils.SmartMessage
import com.kynarec.kmusic.utils.setJustStartedUp
import com.kynarec.kmusic.utils.setPlayerOpen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"
    private var mediaController: MediaController? = null

    // Player.Listener to handle state changes and update the UI accordingly.
    private val playerListener = object : Player.Listener {
        // This callback is triggered whenever the player's state changes.
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            when (playbackState) {
                Player.STATE_READY, Player.STATE_BUFFERING -> {
                    // Show the control bar whenever the player is ready or buffering.
                    hidePlayerControlBar(false)
                }
                else -> {
                    // Hide the control bar when playback is stopped, ended, or has an error.
                    hidePlayerControlBar(true)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }
        val py = Python.getInstance()
        val module = py.getModule("backend")


//        this.setPlayerIsPlaying(false)
        this.setJustStartedUp(true)
        //hidePlayerControlBar(true)
        this.setPlayerOpen(false)

        val serviceIntent = Intent(this, PlayerServiceModern::class.java)
        startService(serviceIntent)
        Log.i(tag, "Started Media3 PlayerServiceModern")

//        supportFragmentManager.beginTransaction()
//            .replace(R.id.player_control_bar, PlayerControlBar())
//            .commit()

//        val playerControlBar = findViewById<FragmentContainerView>(R.id.player_control_bar)
//        val shouldBeHidden = applicationContext.getPlayerJustStartedUp()
//
//        playerControlBar.visibility = if (shouldBeHidden) View.GONE else View.VISIBLE
        // The player control bar is now controlled by the player's state.
        // It's still a good practice to initialize it here, but it should be
        // a stateless fragment that listens to MediaController.
        supportFragmentManager.beginTransaction()
            .replace(R.id.player_control_bar, PlayerControlBar())
            .commit()
    }

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
        mediaController?.release()
        mediaController = null
    }


    // Now, the hidePlayerControlBar function can be called by the state listener.
    private fun hidePlayerControlBar(value: Boolean) {
        val playerControlBar = findViewById<FragmentContainerView>(R.id.player_control_bar)
        playerControlBar.visibility = if (value) View.GONE else View.VISIBLE
        Log.i(tag, "hidePlayerControlBar was triggered: $value")
    }

    fun navigatePlaylists(view: View) {
        Log.i("Playlists", "Playlist button was clicked")
        val playlistsButton = findViewById<Button>(R.id.playlists)
        playlistsButton.setTextColor(getResources().getColor(R.color.white))

        val songsButton = findViewById<Button>(R.id.songs)
        songsButton.setTextColor(getResources().getColor(R.color.hint))

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlaylistsFragment())
            .commit()
    }

    fun navigateSongs(view: View) {
        val songsButton = findViewById<Button>(R.id.songs)
        songsButton.setTextColor(Color.WHITE)

        val playlistsButton = findViewById<Button>(R.id.playlists)
        playlistsButton.setTextColor(getResources().getColor(R.color.hint))
        val songsList = ArrayList<Song>()

        val db = (application as MyApp).database
        val songDao = db.songDao()


        CoroutineScope(Dispatchers.Main).launch {
            val songs = songDao.getSongsWithPlaytime()
            for (s in songs){
                songsList.add(Song(
                    id = s.id,
                    title = s.title,
                    artist = s.artist,
                    thumbnail = s.thumbnail,
                    duration = s.duration
                ))
            }
        }

        // Now we're back on the Main thread—prepare the fragment.
        val songsFragment = SongsFragment()
        val bundle = Bundle().apply {
            putParcelableArrayList("songs_list", songsList)
        }
        songsFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, songsFragment)
            .commit()

        }

    fun navigateSearch(view: View) {
        Log.i("Search", "Search button was clicked")

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SearchFragment())
            .commit()
    }

    fun navigateSearchResult(view: View, query: String) {
        val py = Python.getInstance()
        val module = py.getModule("backend")
        val pyResult = module.callAttr("searchSongs", query)
        val songsList = ArrayList<Song>()

        if (pyResult.asList() == emptyList<PyObject>()) {
            SmartMessage("Error while fetching", PopupType.Warning, false, this)
        }
        else {
            for (item in pyResult.asList()) {
                CoroutineScope(Dispatchers.IO).launch {

                    val d = item.callAttr("get", "duration").toString()
                    songsList.add(
                        Song(
                            id = item.callAttr("get", "id").toString(),
                            title = item.callAttr("get", "title").toString(),
                            artist = item.callAttr("get", "artist").toString(),
                            thumbnail = item.callAttr("get", "thumbnail").toString(),
                            // filtering out wrong durations here and not in backend, because Kotlin is faster
                            duration = (if (Regex("""^(\d{1,2}):(\d{1,2})$""").matchEntire(d) == null) "NA" else d).toString()
                        )
                    )
                }
            }
        }


        val songsFragment = SongsFragment()
        val bundle = Bundle().apply {
            putParcelableArrayList("songs_list", songsList)
        }
        songsFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, songsFragment)
            .commit()
    }

    fun updatePlayerControlBar(song: Song) {
        val playerControlBar = PlayerControlBar()

        val bundle = Bundle().apply {
            putParcelable("song", song)
        }
        playerControlBar.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.player_control_bar, playerControlBar)
            .commit()
    }

    fun navigatePlayer() {
        val player = PlayerFragment()
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_bottom,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_bottom
            )
            .add(R.id.main, player)
            .addToBackStack(null)
            .commit()
    }


    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()

        // If no fragments are on top, show the control bar again
        if (supportFragmentManager.backStackEntryCount == 0) {
            hidePlayerControlBar(false)
        }
    }
}