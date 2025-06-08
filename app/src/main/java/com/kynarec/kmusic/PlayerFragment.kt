package com.kynarec.kmusic

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerService
import com.kynarec.kmusic.utils.ACTION_PAUSE
import com.kynarec.kmusic.utils.ACTION_RESUME
import com.kynarec.kmusic.utils.THUMBNAIL_ROUNDNESS
import com.kynarec.kmusic.utils.getPlayerIsPlaying
import com.kynarec.kmusic.utils.setPlayerOpen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class PlayerFragment : Fragment() {

    lateinit var song: Song
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            song = it.getParcelable("song")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.setPlayerOpen(true)

        val pauseButton = view.findViewById<ImageButton>(R.id.pause_button)
        val playButton = view.findViewById<ImageButton>(R.id.play_button)
        val skipForwardButton = view.findViewById<ImageButton>(R.id.skip_forward_button)
        val skipBackButton = view.findViewById<ImageButton>(R.id.skip_back_button)

        val songThumbnail = view.findViewById<ImageButton>(R.id.song_thumbnail)
        val songTitle = view.findViewById<Button>(R.id.player_song_title)
        val songArtist = view.findViewById<Button>(R.id.player_song_artist)

        val goBackButton = view.findViewById<ImageButton>(R.id.button_down)

        songTitle.text = song.title
        songArtist.text = song.artist

        val intent = Intent(context, PlayerService::class.java)

        if (context?.getPlayerIsPlaying() == false) {
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.INVISIBLE
        } else {
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE
        }

        // Pauses playback
        playButton.setOnClickListener {
            intent.action = ACTION_RESUME
            context?.startService(intent)
            sleep(200)
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE

        }

        // Resumes playback
        pauseButton.setOnClickListener {
            intent.action = ACTION_PAUSE
            context?.startService(intent)
            sleep(200)
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.INVISIBLE

        }

        goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
            context?.setPlayerOpen(false)
            if (activity is MainActivity) {
                (activity as MainActivity).hidePlayerControlBar(false)
            }
        }

        Glide.with(this)
            .load(song.thumbnail)
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(THUMBNAIL_ROUNDNESS)))
            .into(songThumbnail)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        context?.setPlayerOpen(false)
    }

//    companion object {
//
//        @JvmStatic
//        fun newInstance() =
//            PlayerFragment().apply {
//                arguments = Bundle().apply {
//
//                }
//            }
//    }
}