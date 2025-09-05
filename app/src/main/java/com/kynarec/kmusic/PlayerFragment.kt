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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
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

    // UI elements to display song metadata
    private var songThumbnail: ImageButton? = null
    private var songTitleButton: TextView? = null
    private var songArtistButton: TextView? = null

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
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (isPlaying) {
                playButton?.visibility = View.INVISIBLE
                pauseButton?.visibility = View.VISIBLE
            } else {
                playButton?.visibility = View.VISIBLE
                pauseButton?.visibility = View.INVISIBLE
            }
            // Also update the UI when playback state changes.
            updateUIForMediaItem(mediaController?.currentMediaItem)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_READY) {
                mediaController?.let { controller ->
                    seekBar?.max = controller.duration.toInt()
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            // This is the key callback. When the song changes, this is where we get the new data.
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

        // Find the new views to update
        songThumbnail = view.findViewById(R.id.song_thumbnail)
        songTitleButton = view.findViewById(R.id.player_song_title)
        songArtistButton = view.findViewById(R.id.player_song_artist)
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlayerServiceModern::class.java))
        val controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                setupUIListeners()
                mediaController?.addListener(playerListener)
                handler.post(updateSeekBarRunnable)

                // Get the initial media item and update the UI when the connection is established.
                updateUIForMediaItem(mediaController?.currentMediaItem)
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupUIListeners() {
        playButton?.setOnClickListener { mediaController?.play() }
        pauseButton?.setOnClickListener { mediaController?.pause() }
        skipForwardButton?.setOnClickListener { mediaController?.seekToNextMediaItem() }
        skipBackButton?.setOnClickListener { mediaController?.seekToPreviousMediaItem() }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaController?.seekTo(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // New function to handle updating the UI with song metadata
    private fun updateUIForMediaItem(mediaItem: MediaItem?) {
        mediaItem?.mediaMetadata?.let { metadata ->
            // Use the metadata from the MediaItem to set the views
            songTitleButton?.text = metadata.title
            songArtistButton?.text = metadata.artist

            // For the thumbnail, you would use an image loading library like Glide or Coil
            // For now, this is a placeholder. You'll need to implement your own image loading logic here.
            metadata.artworkUri?.let { uri ->
                // Your image loading library here, e.g., Glide.with(this).load(uri).into(songThumbnail)
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(THUMBNAIL_ROUNDNESS)))
                    .into(songThumbnail!!)
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
}