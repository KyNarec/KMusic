package com.kynarec.kmusic

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.utils.THUMBNAIL_ROUNDNESS

class PlayerControlBar : Fragment(R.layout.fragment_player_control_bar) {

    private var mediaController: MediaController? = null

    // View references
    private var playButton: ImageButton? = null
    private var pauseButton: ImageButton? = null
    private var skipForwardButton: ImageButton? = null
    private var skipBackButton: ImageButton? = null
    private var songTitleText: TextView? = null
    private var songArtistText: TextView? = null
    private var songThumbnail: ImageView? = null
    private var playerControlBarView: FrameLayout? = null

    // Player.Listener to receive state updates from the MediaController
    private val playerListener = object : Player.Listener {
        // This callback is triggered when the player's playing state changes.
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                playButton?.visibility = View.INVISIBLE
                pauseButton?.visibility = View.VISIBLE
            } else {
                playButton?.visibility = View.VISIBLE
                pauseButton?.visibility = View.INVISIBLE
            }
        }

        // This callback is triggered when the current media item changes.
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            updateUIForMediaItem(mediaItem)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find and store references to your views from the XML
        playButton = view.findViewById(R.id.play_button)
        pauseButton = view.findViewById(R.id.pause_button)
        skipForwardButton = view.findViewById(R.id.skip_forward_button)
        skipBackButton = view.findViewById(R.id.skip_back_button)
        songTitleText = view.findViewById(R.id.song_title)
        songArtistText = view.findViewById(R.id.song_artist)
        songThumbnail = view.findViewById(R.id.thumbnail)
        playerControlBarView = view.findViewById(R.id.control_bar)

        // The feedback button logic is a UI detail and can remain as-is.
        val feedbackCirclePlayButton = view.findViewById<View>(R.id.feedback_circle_play_button)
        val feedbackCircleSkipForwardButton = view.findViewById<View>(R.id.feedback_circle_skip_forward)
        val feedbackCircleSkipBackButton = view.findViewById<View>(R.id.feedback_circle_skip_back)

        // Set up button click listeners. This is done here as it doesn't depend on the controller.
        playButton?.setOnClickListener {
            mediaController?.play()
            animateFeedbackButton(feedbackCirclePlayButton)
        }
        pauseButton?.setOnClickListener {
            mediaController?.pause()
            animateFeedbackButton(feedbackCirclePlayButton)
        }
        skipForwardButton?.setOnClickListener {
            mediaController?.seekToNextMediaItem()
            animateFeedbackButton(feedbackCircleSkipForwardButton)
        }
        skipBackButton?.setOnClickListener {
            mediaController?.seekToPreviousMediaItem()
            animateFeedbackButton(feedbackCircleSkipBackButton)
        }

        playerControlBarView?.setOnClickListener {
            if (activity is MainActivity) {
                (activity as MainActivity).navigatePlayer()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Create a SessionToken for your service and build the MediaController asynchronously.
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlayerServiceModern::class.java))
        val controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                // Now that the controller is ready, add our listener to it.
                mediaController?.addListener(playerListener)
                // Perform an initial synchronization of the UI state.
                updateUIForMediaItem(mediaController?.currentMediaItem)
                playerListener.onIsPlayingChanged(mediaController?.isPlaying?: false)
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onStop() {
        super.onStop()
        // It's crucial to release the MediaController when the fragment is no longer in use.
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    private fun updateUIForMediaItem(mediaItem: MediaItem?) {
        mediaItem?.mediaMetadata?.let { metadata ->
            songTitleText?.text = metadata.title?: "Unknown Title"
            songArtistText?.text = metadata.artist?: "Unknown Artist"

            metadata.artworkUri?.let { uri ->
                songThumbnail?.let { imageView ->
                    Glide.with(this)
                        .load(uri)
                        .apply(
                            RequestOptions.bitmapTransform(
                                MultiTransformation(
                                    CenterCrop(),
                                    RoundedCorners(THUMBNAIL_ROUNDNESS)
                                )
                            )
                        )
                        .into(imageView)
                }
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