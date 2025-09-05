package com.kynarec.kmusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerServiceModern
//import com.kynarec.kmusic.service.PlayerService
import com.kynarec.kmusic.utils.ACTION_PAUSE
import com.kynarec.kmusic.utils.ACTION_RESUME
import com.kynarec.kmusic.utils.IS_PLAYING
import com.kynarec.kmusic.utils.MARQUEE_DELAY
import com.kynarec.kmusic.utils.PLAYBACK_STATE_CHANGED
import com.kynarec.kmusic.utils.THUMBNAIL_ROUNDNESS
import com.kynarec.kmusic.utils.getPlayerIsPlaying
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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

    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isPlaying = intent?.getBooleanExtra(IS_PLAYING, false) ?: false
            if (isPlaying) {
                val pauseButton = requireView().findViewById<ImageButton>(R.id.pause_button)
                val playButton = requireView().findViewById<ImageButton>(R.id.play_button)

                playButton.visibility = View.INVISIBLE
                pauseButton.visibility = View.VISIBLE
            } else {
                val pauseButton = requireView().findViewById<ImageButton>(R.id.pause_button)
                val playButton = requireView().findViewById<ImageButton>(R.id.play_button)

                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.INVISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PLAYBACK_STATE_CHANGED)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(playbackStateReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(playbackStateReceiver)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pauseButton = view.findViewById<ImageButton>(R.id.pause_button)
        val playButton = view.findViewById<ImageButton>(R.id.play_button)
        val skipForwardButton = view.findViewById<ImageButton>(R.id.skip_forward_button)
        val skipBackButton = view.findViewById<ImageButton>(R.id.skip_back_button)


        val feedbackCirclePlayButton = view.findViewById<View>(R.id.feedback_circle_play_button)
        val feedbackCircleSkipForwardButton = view.findViewById<View>(R.id.feedback_circle_skip_forward)
        val feedbackCircleSkipBackButton = view.findViewById<View>(R.id.feedback_circle_skip_back)


        val titleText: TextView = view.findViewById(R.id.song_title)
        val artistText: TextView = view.findViewById(R.id.song_artist)
        val thumbnail: ImageView = view.findViewById(R.id.thumbnail)

        val playerControlBar = view.findViewById<FrameLayout>(R.id.control_bar)

        val intent = Intent(context, PlayerServiceModern::class.java)

        feedbackCirclePlayButton.visibility = View.INVISIBLE

        if (context?.getPlayerIsPlaying() == false) {
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.INVISIBLE
        } else {
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE
        }

        // Pauses playback
        playButton.setOnClickListener {
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE
            animateFeedbackButton(feedbackCirclePlayButton)
            intent.action = ACTION_RESUME
            context?.startService(intent)
        }

        // Resumes playback
        pauseButton.setOnClickListener {
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.INVISIBLE
            animateFeedbackButton(feedbackCirclePlayButton)
            intent.action = ACTION_PAUSE
            context?.startService(intent)
        }

        skipForwardButton.setOnClickListener {
            animateFeedbackButton(feedbackCircleSkipForwardButton)
        }

        skipBackButton.setOnClickListener {
            animateFeedbackButton(feedbackCircleSkipBackButton)
        }

        titleText.text = song.title
        titleText.isSelected = false

        lifecycleScope.launch {
            delay(MARQUEE_DELAY)
            titleText.isSelected = true
        }

        artistText.text = song.artist
        artistText.isSelected = false

        lifecycleScope.launch {
            delay(MARQUEE_DELAY)
            artistText.isSelected = true
        }


        Glide.with(this)
            .load(song.thumbnail)
            .apply(
                RequestOptions.bitmapTransform(
                    MultiTransformation(
                        CenterCrop(),
                        RoundedCorners(THUMBNAIL_ROUNDNESS)
                    )
                )
            )
            .into(thumbnail)

        playerControlBar.setOnClickListener {
            if (activity is MainActivity) {
                (activity as MainActivity).navigatePlayer()
            }
        }
    }

    private fun animateFeedbackButton(feedbackCircle: View){
        feedbackCircle.alpha = 0f
        feedbackCircle.visibility = View.VISIBLE
        feedbackCircle.animate()
            .alpha(.5f)
            .setDuration(100)
            .withEndAction {
                feedbackCircle.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        feedbackCircle.visibility = View.GONE
                    }
                    .start()
            }
            .start()
    }
}