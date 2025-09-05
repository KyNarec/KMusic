package com.kynarec.kmusic

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
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

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var mediaController: MediaController? = null

    // View references
    private var playButton: ImageButton? = null
    private var pauseButton: ImageButton? = null
    private var skipForwardButton: ImageButton? = null
    private var skipBackButton: ImageButton? = null
    private var seekBar: SeekBar? = null
    private var currentTimeTextView: TextView? = null
    private var totalTimeTextView: TextView? = null
    private var goBackButton: ImageButton? = null

    // UI elements to display song metadata
    private var songThumbnail: ImageView? = null
    private var songTitleButton: TextView? = null
    private var songArtistButton: TextView? = null

    // Handler and Runnable for updating the seek bar
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaController?.let { controller ->
                val currentPosition = controller.currentPosition
                val totalDuration = controller.duration

                // Only update if duration is valid and not a live stream
                if (totalDuration > 0 &&!controller.isCurrentMediaItemLive) {
                    seekBar?.max = totalDuration.toInt()
                    totalTimeTextView?.text = formatTime(totalDuration)
                }

                seekBar?.progress = currentPosition.toInt()
                currentTimeTextView?.text = formatTime(currentPosition)

                // Continue updating only if player is playing
                if (controller.isPlaying) {
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    // Listener to handle player state changes
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                playButton?.visibility = View.INVISIBLE
                pauseButton?.visibility = View.VISIBLE
                handler.post(updateSeekBarRunnable) // Start updates when playing
            } else {
                playButton?.visibility = View.VISIBLE
                pauseButton?.visibility = View.INVISIBLE
                handler.removeCallbacks(updateSeekBarRunnable) // Stop updates when paused
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                // When the song ends, reset the seek bar
                seekBar?.progress = 0
                currentTimeTextView?.text = formatTime(0)
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            updateUIForMediaItem(mediaItem)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find and store references to your views from the XML
        playButton = view.findViewById(R.id.player_play_button)
        pauseButton = view.findViewById(R.id.player_pause_button)
        skipForwardButton = view.findViewById(R.id.skip_forward_button)
        skipBackButton = view.findViewById(R.id.skip_back_button)
        seekBar = view.findViewById(R.id.seek_bar)
        currentTimeTextView = view.findViewById(R.id.current_time)
        totalTimeTextView = view.findViewById(R.id.total_time)
        goBackButton = view.findViewById(R.id.button_down)

        // Find the new views to update
        songThumbnail = view.findViewById(R.id.song_thumbnail)
        songTitleButton = view.findViewById(R.id.player_song_title)
        songArtistButton = view.findViewById(R.id.player_song_artist)

        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null || e2 == null) return false

                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                if (Math.abs(diffY) > Math.abs(diffX) && diffY > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    // Swipe down detected
                    goBack()
                    return true
                }
                return false
            }
        })

        // Set the gesture detector on the fragment root view
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        goBackButton?.setOnClickListener {
            goBack()
        }
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlayerServiceModern::class.java))
        val controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                setupUI()
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupUI() {
        mediaController?.let { controller ->
            // Set up button click listeners.
            playButton?.setOnClickListener { controller.play() }
            pauseButton?.setOnClickListener { controller.pause() }
            skipForwardButton?.setOnClickListener { controller.seekToNextMediaItem() }
            skipBackButton?.setOnClickListener { controller.seekToPreviousMediaItem() }

            // Set up seek bar change listener.
            seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        controller.seekTo(progress.toLong())
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            // Add the listener for player state changes.
            controller.addListener(playerListener)

            // Explicitly synchronize the UI on connection.
            updateUIForMediaItem(controller.currentMediaItem)
            playerListener.onIsPlayingChanged(controller.isPlaying)

            // Manually set initial seek bar progress and duration
            // This is the key change to fix the timing issue.
            val totalDuration = controller.duration
            if (totalDuration > 0 &&!controller.isCurrentMediaItemLive) {
                seekBar?.max = totalDuration.toInt()
                totalTimeTextView?.text = formatTime(totalDuration)
            }
            seekBar?.progress = controller.currentPosition.toInt()
            currentTimeTextView?.text = formatTime(controller.currentPosition)
        }
    }

    private fun updateUIForMediaItem(mediaItem: MediaItem?) {
        mediaItem?.mediaMetadata?.let { metadata ->
            songTitleButton?.text = metadata.title?: "Unknown Title"
            songArtistButton?.text = metadata.artist?: "Unknown Artist"

            metadata.artworkUri?.let { uri ->
                songThumbnail?.let { imageView ->
                    Glide.with(this)
                        .load(uri)
                        .centerCrop()
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

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateSeekBarRunnable)
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playButton = null
        pauseButton = null
        skipForwardButton = null
        skipBackButton = null
        seekBar = null
        currentTimeTextView = null
        totalTimeTextView = null
        songThumbnail = null
        songTitleButton = null
        songArtistButton = null
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}