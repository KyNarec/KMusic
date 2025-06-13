package com.kynarec.kmusic

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
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.service.PlayerService
import com.kynarec.kmusic.utils.ACTION_PAUSE
import com.kynarec.kmusic.utils.ACTION_RESUME
import com.kynarec.kmusic.utils.ACTION_RESUME_UPDATES
import com.kynarec.kmusic.utils.ACTION_SEEK
import com.kynarec.kmusic.utils.ACTION_STOP_UPDATES
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


    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PLAYER_PROGRESS_UPDATE) {
                val currentPosition = intent.getLongExtra("current_position", 0)
                val duration = intent.getLongExtra("duration", 0)
                Log.d("PlayerFragment", "Received update - Position: $currentPosition, Duration: $duration")

                currentDuration = duration // Store duration
                updateSeekBar(currentPosition, duration)
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

        val filter = IntentFilter(PLAYER_PROGRESS_UPDATE)
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(progressReceiver, filter)

        val intent = Intent(context, PlayerService::class.java)

// Set up SeekBar listener
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Calculate the time based on progress percentage and current duration
                    // We need to store the duration somewhere accessible
                    val estimatedTime = (progress * currentDuration) / 100
                    currentTimeText.text = formatTime(estimatedTime)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
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
            }
        })

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
            .centerCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(THUMBNAIL_ROUNDNESS)))
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

    override fun onDestroyView() {
        super.onDestroyView()
        context?.setPlayerOpen(false)
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(progressReceiver)
    }

    private fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
        context?.setPlayerOpen(false)
        if (activity is MainActivity) {
            (activity as MainActivity).hidePlayerControlBar(false)
        }
    }

    private fun updateSeekBar(currentPosition: Long, duration: Long) {
        if (duration > 0) {
            // Store duration in seekBar's tag for later use
            seekBar.tag = duration

            val progress = ((currentPosition * 100) / duration).toInt()
            Log.d("PlayerFragment", "Updating SeekBar - Progress: $progress")

            seekBar.progress = progress

            currentTimeText.text = formatTime(currentPosition)
            totalTimeText.text = formatTime(duration)
        } else  Log.d("PlayerFragment", "Duration is 0, not updating SeekBar")
    }

    private fun formatTime(timeMs: Long): String {
        val seconds = (timeMs / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

}