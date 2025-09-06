package com.kynarec.kmusic

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.kynarec.kmusic.utils.MARQUEE_DELAY
import com.kynarec.kmusic.utils.THUMBNAIL_ROUNDNESS
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerControlBar : Fragment(R.layout.fragment_player_control_bar) {

    private var mediaController: MediaController? = null

    // View references
    private var playButton: ImageButton? = null
    private var pauseButton: ImageButton? = null
    private var skipForwardButton: ImageButton? = null
    private var titleText: TextView? = null
    private var artistText: TextView? = null
    private var thumbnail: ImageView? = null
    private var playerControlBarLayout: FrameLayout? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayPauseButton(isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateUI(mediaItem)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize views
        playButton = view.findViewById(R.id.play_button)
        pauseButton = view.findViewById(R.id.pause_button)
        skipForwardButton = view.findViewById(R.id.skip_forward_button)
        titleText = view.findViewById(R.id.song_title)
        artistText = view.findViewById(R.id.song_artist)
        thumbnail = view.findViewById(R.id.thumbnail)
        playerControlBarLayout = view.findViewById(R.id.control_bar)

        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlayerServiceModern::class.java))
        val controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
            // Initial UI update
            updateUI(mediaController?.currentMediaItem)
            updatePlayPauseButton(mediaController?.isPlaying ?: false)
        }, MoreExecutors.directExecutor())
    }

    private fun setupClickListeners() {
        playButton?.setOnClickListener { mediaController?.play() }
        pauseButton?.setOnClickListener { mediaController?.pause() }
        skipForwardButton?.setOnClickListener { mediaController?.seekToNextMediaItem() }

        playerControlBarLayout?.setOnClickListener {
//            (activity as? MainActivity)?.navigatePlayer()
        }
    }

    private fun updateUI(mediaItem: MediaItem?) {
        if (mediaItem == null) return

        val metadata = mediaItem.mediaMetadata
        titleText?.text = metadata.title
        artistText?.text = metadata.artist

        // Marquee effect
        viewLifecycleOwner.lifecycleScope.launch {
            delay(MARQUEE_DELAY)
            titleText?.isSelected = true
            artistText?.isSelected = true
        }

        thumbnail?.let {
            Glide.with(this)
                .load(metadata.artworkUri)
                .apply(
                    RequestOptions.bitmapTransform(
                        MultiTransformation(
                            CenterCrop(),
                            RoundedCorners(THUMBNAIL_ROUNDNESS)
                        )
                    )
                )
                .into(it)
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        if (isPlaying) {
            playButton?.visibility = View.INVISIBLE
            pauseButton?.visibility = View.VISIBLE
        } else {
            playButton?.visibility = View.VISIBLE
            pauseButton?.visibility = View.INVISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Nullify view references to avoid memory leaks
        playButton = null
        pauseButton = null
        skipForwardButton = null
        titleText = null
        artistText = null
        thumbnail = null
        playerControlBarLayout = null
    }
}
