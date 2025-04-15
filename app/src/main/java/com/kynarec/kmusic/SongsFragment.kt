package com.kynarec.kmusic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kynarec.kmusic.models.Song
import com.kynarec.kmusic.utils.SongAdapter


/**
 * A simple [Fragment] subclass.
 * Use the [SongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SongsFragment : Fragment() {

    private var songsList: ArrayList<Song>? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter

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
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_songs, container, false)
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        recyclerView = view.findViewById(R.id.songs_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        songsList?.let {
            songAdapter = SongAdapter(it)
            recyclerView.adapter = songAdapter
        }

        return view
    }


}