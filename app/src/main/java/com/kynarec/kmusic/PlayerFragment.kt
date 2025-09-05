package com.kynarec.kmusic

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.kynarec.kmusic.service.PlayerServiceModern

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

    // Handler and Runnable for updating the seek bar
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaController?.let { controller ->
                val currentPosition = controller.currentPosition
                val totalDuration = controller.duration
                seekBar?.progress = currentPosition.toInt()
                currentTimeTextView?.text = formatTime(currentPosition)
                totalTimeTextView?.text = formatTime(totalDuration)
            }
            handler.postDelayed(this, 1000)
        }
    }

    // Listener to handle player state changes
    private val playerListener = object : Player.Listener {
        // This is called when the playback state changes (e.g., from playing to paused)
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                // If the player is playing, show the pause button and hide the play button
                playButton?.visibility = View.INVISIBLE
                pauseButton?.visibility = View.VISIBLE
            } else {
                // If the player is paused, show the play button and hide the pause button
                playButton?.visibility = View.VISIBLE
                pauseButton?.visibility = View.INVISIBLE
            }
        }

        // This is called when the playback state changes (e.g., from buffering to ready)
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_READY) {
                // Once the player is ready, set the max value of the seek bar
                mediaController?.let { controller ->
                    seekBar?.max = controller.duration.toInt()
                }
            }
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
    }

    override fun onStart() {
        super.onStart()
        // Create a SessionToken that uniquely identifies your service.
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlayerServiceModern::class.java))

        // Build the MediaController asynchronously to avoid blocking the UI thread.
        val controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()

        // Use a listener to retrieve the controller once it's ready.
        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                // Now that we have the controller, we can set up the UI listeners and state.
                setupUIListeners()
                // Add the Player.Listener to the controller to get state updates.
                mediaController?.addListener(playerListener)
                // Begin updating the seek bar and time labels.
                handler.post(updateSeekBarRunnable)
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupUIListeners() {
        // Use the MediaController to send playback commands to the service.
        playButton?.setOnClickListener { mediaController?.play() }
        pauseButton?.setOnClickListener { mediaController?.pause() }
        skipForwardButton?.setOnClickListener { mediaController?.seekToNextMediaItem() }
        skipBackButton?.setOnClickListener { mediaController?.seekToPreviousMediaItem() }

        // Handle seek bar changes by seeking the player.
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaController?.seekTo(progress.toLong())
                }
            }
            // These methods are not needed for simple seeking.
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onStop() {
        super.onStop()
        // It's crucial to release the controller when the fragment is not visible to
        // prevent resource leaks and avoid unnecessary connections.
        handler.removeCallbacks(updateSeekBarRunnable)
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Null out view references to prevent memory leaks in fragments.
        playButton = null
        pauseButton = null
        skipForwardButton = null
        skipBackButton = null
        seekBar = null
        currentTimeTextView = null
        totalTimeTextView = null
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
}