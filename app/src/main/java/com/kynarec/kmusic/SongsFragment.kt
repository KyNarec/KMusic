package com.kynarec.kmusic

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.utils.SongAdapter
import com.kynarec.kmusic.utils.createMediaItemFromSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
                    else Log.i(tag, "${song.id} is in Dap")
                }

                val context = requireContext()
                val sessionToken =
                    SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))
                val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

                controllerFuture.addListener(
                    {
                        // Step 2: Once connected, retrieve the controller and send the command
                        val mediaController = controllerFuture.get()

                        // Create a MediaItem from your Song data class
                        CoroutineScope(Dispatchers.IO).launch {
                            val mediaItem = createMediaItemFromSong(song, context)
                            mediaController.setMediaItem(mediaItem)
                            mediaController.prepare()
                            mediaController.play()

                        }

                        // Use the MediaController to set the media item and start playback.

                        // Optional: You could now navigate to the PlayerFragment
//                        (activity as? MainActivity)?.navigatePlayer()
                    },
                    MoreExecutors.directExecutor()
                )

                if (activity is MainActivity) {
//                    (activity as MainActivity).updatePlayerControlBar(song)
                }
            }

            recyclerView.adapter = songAdapter
        }

        return view
    }

}