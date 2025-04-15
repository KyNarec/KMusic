package com.kynarec.kmusic

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.kynarec.kmusic.models.Song
import kotlinx.coroutines.DelicateCoroutinesApi


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
        //py = Python.getInstance()
        //module = py.getModule("backend")
//        val t = module.callAttr("searchOneSong", "Numb")
//        module.callAttr("getSongThumbnailURL", t)
//        val pyResult = module.callAttr("searchSongs", "Numb")
//        val videoIds = ArrayList<Song>()
//        for (item in pyResult.asList()) {
//            videoIds.add(Song(
//                id = item.toString(),
//                title = module.callAttr("getSongTitle", item.toString()).toString(),
//                artist = module.callAttr("getSongArtistName", item.toString()).toString(),
//                thumbnail = module.callAttr("getSongThumbnailURL", item.toString()).toString()
//            ))
//        }

        //val i = InnerTube()
        //GlobalScope.launch {
        //    print(i.getSearchSuggestions(YouTubeClient.WEB_REMIX, "numb"))
        //}
    }

    fun navigatePlaylists(view: View) {
        Log.i("Playlists", "Playlist button was clicked")

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, PlaylistsFragment())
            .commit()
    }

    fun navigateSongs(view: View) {

        val songsFragment = SongsFragment()
        val bundle = Bundle()

        // Assuming you have an ArrayList of Song objects
        //val songsList = ArrayList<Song>()
        // Add your songs to the list here
        val py = Python.getInstance()
        val module = py.getModule("backend")

        val pyResult = module.callAttr("searchSongs", "Numb")
        val songsList = ArrayList<Song>()
        for (item in pyResult.asList()) {
            songsList.add(Song(
                id = item.toString(),
                title = module.callAttr("getSongTitle", item.toString()).toString(),
                artist = module.callAttr("getSongArtistName", item.toString()).toString(),
                thumbnail = module.callAttr("getSongThumbnailURL", item.toString()).toString()
            ))
        }

        // Put the ArrayList in the bundle
        bundle.putParcelableArrayList("songs_list", songsList)

        // Set the arguments to the fragment
        songsFragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, songsFragment)
            .commit()
    }
}