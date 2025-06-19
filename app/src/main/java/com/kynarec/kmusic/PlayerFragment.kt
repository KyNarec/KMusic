package com.kynarec.kmusic

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerService
import com.kynarec.kmusic.utils.ACTION_PAUSE
import com.kynarec.kmusic.utils.ACTION_RESUME
import com.kynarec.kmusic.utils.ACTION_RESUME_UPDATES
import com.kynarec.kmusic.utils.ACTION_SEEK
import com.kynarec.kmusic.utils.ACTION_STOP_UPDATES
import com.kynarec.kmusic.utils.IS_PLAYING
import com.kynarec.kmusic.utils.PLAYBACK_STATE_CHANGED
import com.kynarec.kmusic.utils.PLAYER_PROGRESS_UPDATE
import com.kynarec.kmusic.utils.THUMBNAIL_ROUNDNESS
import com.kynarec.kmusic.utils.getPlayerIsPlaying
import com.kynarec.kmusic.utils.parseDurationToMillis
import com.kynarec.kmusic.utils.setPlayerOpen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class PlayerFragment : Fragment() {

    lateinit var song: Song

    private lateinit var seekBar: SeekBar
    private lateinit var currentTimeText: TextView
    private lateinit var totalTimeText: TextView

    private var currentDuration: Long = 0 // Track current duration

    // Smooth SeekBar Animation Variables
    private var currentAnimator: ValueAnimator? = null
    private var isUserSeeking = false
    private var lastPosition: Long = 0
    private var isPlaying = false

    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("PlayerSeekbar", "BroadcastReceiver.onReceive called with action: ${intent?.action}")

            if (intent?.action == PLAYER_PROGRESS_UPDATE) {
                val currentPosition = intent.getLongExtra("current_position", 0)
                val duration = intent.getLongExtra("duration", 0)
                isPlaying = intent.getBooleanExtra("is_playing", false)

                Log.d("PlayerSeekbar", "Received update - Position: $currentPosition, Duration: $duration, Playing: $isPlaying")

                currentDuration = duration // Store duration
                updateSeekBarSmooth(currentPosition, duration, isPlaying)
            }
        }
    }

    private val playbackStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isPlaying = intent?.getBooleanExtra(IS_PLAYING, false) ?: false
            if (isPlaying) {
                val pauseButton = requireView().findViewById<ImageButton>(R.id.player_pause_button)
                val playButton = requireView().findViewById<ImageButton>(R.id.player_play_button)

                playButton.visibility = View.INVISIBLE
                pauseButton.visibility = View.VISIBLE
            } else {
                val pauseButton = requireView().findViewById<ImageButton>(R.id.player_pause_button)
                val playButton = requireView().findViewById<ImageButton>(R.id.player_play_button)

                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.INVISIBLE
            }
        }
    }

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.setPlayerOpen(true)

        val pauseButton = view.findViewById<ImageButton>(R.id.player_pause_button)
        val playButton = view.findViewById<ImageButton>(R.id.player_play_button)
        val skipForwardButton = view.findViewById<ImageButton>(R.id.skip_forward_button)
        val skipBackButton = view.findViewById<ImageButton>(R.id.skip_back_button)

        val songThumbnail = view.findViewById<ImageButton>(R.id.song_thumbnail)
        val songTitle = view.findViewById<Button>(R.id.player_song_title)
        val songArtist = view.findViewById<Button>(R.id.player_song_artist)

        // Add SeekBar components
        seekBar = view.findViewById<SeekBar>(R.id.seek_bar)
        currentTimeText = view.findViewById<TextView>(R.id.current_time)
        totalTimeText = view.findViewById<TextView>(R.id.total_time)

        val goBackButton = view.findViewById<ImageButton>(R.id.button_down)

        songTitle.text = song.title
        songArtist.text = song.artist

        currentDuration = parseDurationToMillis(song.duration)
        totalTimeText.text = song.duration


        //Log.d("PlayerSeekbar", "BroadcastReceiver registered")

        val intent = Intent(context, PlayerService::class.java)

        // Set up SeekBar listener with smooth animation support
        setupSeekBarListener(intent)

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
            goBack()
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
            .into(songThumbnail)

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
    }

    private fun setupSeekBarListener(intent: Intent) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Calculate the time based on progress percentage and current duration
                    val estimatedTime = (progress * currentDuration) / 100
                    currentTimeText.text = formatTime(estimatedTime)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
                currentAnimator?.cancel() // Stop any ongoing animation

                // Tell the service to stop sending updates while user is dragging
                intent.action = ACTION_STOP_UPDATES
                context?.startService(intent)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Seek to the new position
                seekBar?.let { bar ->
                    val seekPosition = (bar.progress * currentDuration) / 100
                    Log.i(tag, "${bar.progress} * $currentDuration / 100 = $seekPosition")
                    val seekIntent = Intent(context, PlayerService::class.java)
                    seekIntent.action = ACTION_SEEK
                    seekIntent.putExtra("seek_position", seekPosition)
                    context?.startService(seekIntent)
                }

                // Tell the service to resume sending updates
                val resumeIntent = Intent(context, PlayerService::class.java)
                resumeIntent.action = ACTION_RESUME_UPDATES
                context?.startService(resumeIntent)

                isUserSeeking = false
            }
        })
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(progressReceiver, IntentFilter(PLAYER_PROGRESS_UPDATE))

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(playbackStateReceiver, IntentFilter(PLAYBACK_STATE_CHANGED))
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Clean up animation
        currentAnimator?.cancel()

        context?.setPlayerOpen(false)

        if (activity is MainActivity) {
            (activity as MainActivity).hidePlayerControlBar(false)
        }

        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(progressReceiver)

        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(playbackStateReceiver)
    }

    private fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
        context?.setPlayerOpen(false)
        if (activity is MainActivity) {
            (activity as MainActivity).hidePlayerControlBar(false)
        }
    }

    private fun updateSeekBarSmooth(currentPosition: Long, duration: Long, isPlaying: Boolean) {
        if (duration <= 0 || isUserSeeking) return

        // Calculate the target progress percentage
        val targetProgress = ((currentPosition * 100) / duration).toInt()

        // Only animate if there's a meaningful change and we're playing
        val positionDiff = Math.abs(currentPosition - lastPosition)

        if (isPlaying && positionDiff > 500) { // Only animate if position changed by more than 500ms
            animateToPosition(targetProgress, currentPosition, duration)
        } else if (!isPlaying) {
            // If paused, set position directly
            seekBar.progress = targetProgress
            updateTimeTexts(currentPosition, duration)
        } else if (positionDiff <= 500) {
            // Small change, set directly to avoid jitter
            seekBar.progress = targetProgress
            updateTimeTexts(currentPosition, duration)
        }

        lastPosition = currentPosition
    }

    private fun animateToPosition(targetProgress: Int, currentPosition: Long, duration: Long) {
        currentAnimator?.cancel()

        val currentProgress = seekBar.progress
        val progressDiff = Math.abs(targetProgress - currentProgress)

        // Skip animation for tiny changes
        if (progressDiff <= 1) {
            seekBar.progress = targetProgress
            updateTimeTexts(currentPosition, duration)
            return
        }

        // Adjust animation duration based on how far we need to move
        // Shorter animations for smaller movements, longer for bigger jumps
        val animationDuration = (progressDiff * 15).coerceIn(200, 1000) // 200ms to 1000ms

        currentAnimator = ValueAnimator.ofInt(currentProgress, targetProgress).apply {
            this.duration = animationDuration.toLong()
            interpolator = LinearInterpolator()

            addUpdateListener { animation ->
                if (!isUserSeeking) {
                    val animatedProgress = animation.animatedValue as Int
                    seekBar.progress = animatedProgress

                    // Update time text during animation
                    val animatedTime = (animatedProgress * duration) / 100
                    currentTimeText.text = formatTime(animatedTime)
                }
            }

            // Update final time texts when animation completes
            doOnEnd {
                if (!isUserSeeking) {
                    updateTimeTexts(currentPosition, duration)
                }
            }

            start()
        }
    }

    private fun updateTimeTexts(currentPosition: Long, duration: Long) {
        currentTimeText.text = formatTime(currentPosition)
        totalTimeText.text = formatTime(duration)
    }

    private fun formatTime(timeMs: Long): String {
        val seconds = (timeMs / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    // Extension function for ValueAnimator (add this at the end of the class or as a separate utility)
    private fun ValueAnimator.doOnEnd(action: () -> Unit) {
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) { action() }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }
}