package com.kynarec.kmusic

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kynarec.innertube.models.SongItem
import com.kynarec.innertube.models.Artist
import com.kynarec.kmusic.models.Song


class SongFragment : Fragment() {

//    private var song: SongItem? = null
//    private var song: SongItem = SongItem(
//    id = "@id_001",
//    title = "Smth",
//    artists = listOf(
//        Artist("idk", "@id_002")
//    ),
//    duration = 250,
//    thumbnail = "TODO()",
//    explicit = true,
//)
    //private var _binding: FragmentSongBinding? = null
    //private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate using data binding.
        //_binding = FragmentSongBinding.inflate(inflater, container, false)

        // Set the song variable on the binding.
        // (Make sure song is not null before setting, or handle the null case in your layout.)
        //binding.shownSong

        //return binding.root

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song, container, false)
//        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SongFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    fun playSong(view: View) {
        Log.i("song", "song was pressed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}