package com.kynarec.kmusic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerService
import com.kynarec.kmusic.utils.ACTION_PLAY
import com.kynarec.kmusic.utils.ACTION_RESUME
import com.kynarec.kmusic.utils.SongAdapter
import kotlinx.coroutines.launch


class SongsFragment : Fragment() {

    private var songsList: List<Song>? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private val tag = "SongsFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            songsList = it.getParcelableArrayList("songs_list")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)
        recyclerView = view.findViewById(R.id.songs_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val db = (requireActivity().application as MyApp).database
        val songDao = db.songDao()

        songsList?.let { songs ->
            songAdapter = SongAdapter(songs) { song ->
                // This is your click handler

                lifecycleScope.launch {
                    if (songDao.getSongById(song.id) == null) {
                        Log.i(tag, "${song.id} is not in Dap")
                        songDao.insertSong(song)
                    }
                    if (songDao.getSongById(song.id) == null) {
                        Log.i(tag, "${song.id} is not in Dap")
                        songDao.insertSong(song)
                    }
                    else Log.i(tag, "${song.id} is in Dap")
                }

                val context = requireContext()
                Intent(context, PlayerService::class.java).apply {
                    action = ACTION_PLAY
                    putExtra("SONG", song)
                    context.startService(this)
                }

                Intent(context, PlayerService::class.java).apply {
                    action = ACTION_RESUME
                    context.startService(this)
                }

                if (activity is MainActivity) {
                    (activity as MainActivity).updatePlayerControlBar(song)
                }
            }

            recyclerView.adapter = songAdapter
        }

        return view
    }


}