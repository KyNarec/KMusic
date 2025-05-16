package com.kynarec.kmusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.kynarec.kmusic.models.Song
import com.kynarec.kmusic.service.PlayerService
import com.kynarec.kmusic.utils.getPlayerIsPlaying


class PlayerControlBar : Fragment() {
    lateinit var song: Song
    var isPlaying = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        song = Song(
            id = "",
            title = "null",
            artist = "null",
            thumbnail = "",
            duration = ""
        )

        arguments?.let {
            song = it.getParcelable("song")!!
        }

        if(song == null) {
            song = Song(
                id = "",
                title = "null",
                artist = "null",
                thumbnail = "",
                duration = ""
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player_control_bar, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            PlayerControlBar().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isPlaying = intent?.getBooleanExtra("isPlaying", false) ?: false
            Log.d("PlayerControlBar", "Is playing: $isPlaying")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pauseButton = view.findViewById<ImageButton>(R.id.pause_button)
        val playButton = view.findViewById<ImageButton>(R.id.play_button)

        val titleText: TextView = view.findViewById(R.id.song_title)
        val artistText: TextView = view.findViewById(R.id.song_artist)
        val thumbnail: ImageView = view.findViewById(R.id.thumbnail)

        val intent = Intent(context, PlayerService::class.java)


        context?.let {
            ContextCompat.registerReceiver(
                it,
                statusReceiver,
                IntentFilter("PLAYER_STATUS"),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        val newIntent = intent.apply { action = "REQUEST_PLAYER_STATUS" }
        context?.startService(newIntent)

        if (context?.getPlayerIsPlaying() == false) {
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.INVISIBLE
        }

        if (context?.getPlayerIsPlaying() == true) {
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE
        }

        // Pauses playback
        playButton.setOnClickListener {
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE
            intent.action = "ACTION_RESUME"
            context?.startService(intent)
        }

        // Resumes playback
        pauseButton.setOnClickListener {
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.INVISIBLE
            intent.action = "ACTION_PAUSE"
            context?.startService(intent)
        }

        titleText.text = song.title
        artistText.text = song.artist

        Glide.with(this)
            .load(song.thumbnail)
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
            .into(thumbnail)
    }
}