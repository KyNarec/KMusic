package com.kynarec.kmusic


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
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"

    companion object {
        var instance: MainActivity? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        db = KmusicDatabase.getDatabase(applicationContext)
//        songDao = db.songDao()

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }
        val py = Python.getInstance()
        val module = py.getModule("backend")


//        this.setPlayerIsPlaying(false)
        hidePlayerControlBar(true)

        val serviceIntent = Intent(this, PlayerService::class.java)
        startService(serviceIntent)

        supportFragmentManager.beginTransaction()
            .replace(R.id.player_control_bar, PlayerControlBar())
            .commit()

//        val playerControlBar = findViewById<FragmentContainerView>(R.id.player_control_bar)
//        val shouldBeHidden = applicationContext.getPlayerJustStartedUp()
//
//        playerControlBar.visibility = if (shouldBeHidden) View.GONE else View.VISIBLE
    }


    fun hidePlayerControlBar(value: Boolean) {
        val playerControlBar = findViewById<FragmentContainerView>(R.id.player_control_bar)
        playerControlBar.visibility = if (value) View.GONE else View.VISIBLE
        Log.i(tag, "hidePlayerControlBar was triggered $value")
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

        var counter = 1

        for (item in pyResult.asList()) {
            CoroutineScope(Dispatchers.Main).launch {

                val d = item.callAttr("get", "duration").toString()
                songsList.add(
                    Song(
                        id = item.callAttr("get", "id").toString(),
                        title = item.callAttr("get", "title").toString(),
                        artist = item.callAttr("get", "artist").toString(),
                        thumbnail = item.callAttr("get", "thumbnail").toString(),
                        // filtering out wrong durations here and not in backend, because Kotlin is faster
                        duration = (if (Regex("""^(\d{1,2}):(\d{1,2})$""").matchEntire(d) == null) "NA" else d ).toString()
                    )
                )
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

    fun updatePlayerControlBar(song: Song) {
        val playerControlBar = PlayerControlBar()

        val  bundle = Bundle().apply {
            putParcelable("song", song)
        }
        playerControlBar.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.player_control_bar, playerControlBar)
            .commit()
    }
}