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

import kotlinx.coroutines.*
import android.widget.Toast


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

        val py = Python.getInstance()
        val module = py.getModule("backend")

        val pyResult = module.callAttr("searchSongsWithDetails", "Numb")

        val songsList = ArrayList<Song>()
        for (item in pyResult.asList()) {
            songsList.add(Song(
                id = item.callAttr("get", "id").toString(),
                title = item.callAttr("get", "title").toString(),
                artist = item.callAttr("get", "artist").toString(),
                thumbnail = item.callAttr("get", "thumbnail").toString(),
                duration = item.callAttr("get", "duration").toString()
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