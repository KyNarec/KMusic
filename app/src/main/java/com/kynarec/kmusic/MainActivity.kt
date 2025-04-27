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
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.kynarec.kmusic.models.Song
import com.kynarec.kmusic.service.InnerTube
import com.kynarec.kmusic.service.PlayerService
import kotlinx.coroutines.DelicateCoroutinesApi

import kotlinx.coroutines.*



class MainActivity : AppCompatActivity() {

    @OptIn(DelicateCoroutinesApi::class)
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

        val serviceIntent = Intent(this, PlayerService::class.java)
        startService(serviceIntent)

        supportFragmentManager.beginTransaction()
            .replace(R.id.player_control_bar, PlayerControlBar())
            .commit()

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

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SongsFragment())
            .commit()
        //TODO("Display songs from DB")

        }

    fun navigateSearch(view: View) {
        Log.i("Search", "Search button was clicked")

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SearchFragment())
            .commit()
    }

    fun navigateSearchResult(view: View, query: String) {
        Log.i("Search Results", "Search Results should be shown")
        val py = Python.getInstance()
        val module = py.getModule("backend")
        val pyResult = module.callAttr("searchSongsWithDetails", query)
        val songsList = ArrayList<Song>()

        var counter = 1

        for (item in pyResult.asList()) {
            CoroutineScope(Dispatchers.Main).launch {
                //InnerTube().main()
                songsList.add(
                    Song(
                        id = item.callAttr("get", "id").toString(),
                        title = item.callAttr("get", "title").toString(),
                        artist = item.callAttr("get", "artist").toString(),
                        thumbnail = item.callAttr("get", "thumbnail").toString(),
                        duration = item.callAttr("get", "duration").toString()
                    )
                )
                Log.i("Search Result", "This is search result number $counter")
//                println("This is search result number $count")
                counter++
            }
        }

//        CoroutineScope(Dispatchers.Main).launch {
//            // Move the Python calls and processing to a background thread:
//            val songsList = withContext(Dispatchers.IO) {
//
//                val resultList = ArrayList<Song>()
//                for (item in pyResult.asList()) {
//                    // Using callAttr("get", ...) to retrieve values from the Python dict.
//                    resultList.add(
//                        Song(
//                            id = item.callAttr("get", "id").toString(),
//                            title = item.callAttr("get", "title").toString(),
//                            artist = item.callAttr("get", "artist").toString(),
//                            thumbnail = item.callAttr("get", "thumbnail").toString(),
//                            duration = item.callAttr("get", "duration").toString()
//                        )
//                    )
//                }
//                resultList
//            }

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