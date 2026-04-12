package com.kynarec.kmusic.data.repository

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.enums.PlayerRepeatMode
import com.kynarec.kmusic.service.PlayerServiceModern
import com.kynarec.kmusic.service.innertube.getRadioFlow
import com.kynarec.kmusic.utils.createMediaItemFromSong
import com.kynarec.kmusic.utils.createPartialMediaItemFromSong
import com.kynarec.kmusic.utils.parseDurationToMillis
import com.kynarec.kmusic.utils.toSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(UnstableApi::class)
class PlayerRepository(private val context: Context) {
    private val tag = "PlayerRepository"

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private var playlistLoadJob: Job? = null
    private var currentLoadingPlaylistId: String? = null
    private var sleepTimerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.i(tag, "onMediaItemTransition called with reason: $reason")

            val songsList = _playerState.value.songsList
            val currentSong = songsList.find { it.song.id == mediaItem?.mediaId }?.song

            _playerState.update {
                it.copy(
                    currentSong = currentSong,
                    currentDurationLong = currentSong?.duration?.parseDurationToMillis() ?: 0L,
                )
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            // Update queue on timeline changes if needed, but typically we handle via logic
        }
        
        override fun onRepeatModeChanged(repeatMode: Int) {
            _playerState.update { it.copy(repeatMode = repeatMode) }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _playerState.update { it.copy(shuffleModeEnabled = shuffleModeEnabled) }
        }
    }

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerServiceModern::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture = future

        future.addListener({
            try {
                val controller = future.get()
                this.mediaController = controller

                val itemsInQueue = mutableListOf<PlaylistItem>()
                for (i in 0 until controller.mediaItemCount) {
                    itemsInQueue.add(PlaylistItem(song = controller.getMediaItemAt(i).toSong()))
                }

                val currentSong = controller.currentMediaItem?.toSong()

                _playerState.update { it.copy(
                    songsList = itemsInQueue,
                    currentSong = currentSong,
                    isPlaying = controller.isPlaying,
                    repeatMode = controller.repeatMode,
                    shuffleModeEnabled = controller.shuffleModeEnabled
                ) }

                controller.addListener(playerListener)
                updatePosition()
            } catch (e: Exception) {
                Log.e(tag, "Failed to connect", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun updatePosition() {
        repositoryScope.launch {
            while (true) {
                val currentPosition = mediaController?.currentPosition ?: 0L
                val totalDuration = mediaController?.duration ?: 0L

                _playerState.update {
                    if (it.currentPosition != currentPosition || it.currentDurationLong != totalDuration) {
                        it.copy(
                            currentPosition = currentPosition,
                            currentDurationLong = totalDuration
                        )
                    } else {
                        it
                    }
                }
                delay(16L) // 60 fps
            }
        }
    }

    fun playSong(song: Song) {
        val controller = mediaController ?: return
        
        _playerState.update { it.copy(currentSong = song) }
        val alreadyInQueue = _playerState.value.songsList.any { it.song.id == song.id }

        if (!alreadyInQueue) {
            val newItem = PlaylistItem(song = song)
            _playerState.update { it.copy(songsList = it.songsList + newItem) }
        }

        repositoryScope.launch(Dispatchers.IO) {
            val mediaItem = createMediaItemFromSong(song, context)
            withContext(Dispatchers.Main) {
                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()
            }
        }
    }

    @kotlin.OptIn(ExperimentalUuidApi::class)
    fun playPlaylist(
        songs: List<Song>,
        startSong: Song,
        playlistId: String = Uuid.random().toString()
    ) {
        playlistLoadJob?.cancel()
        currentLoadingPlaylistId = playlistId

        val controller = mediaController ?: return
        controller.stop()

        _playerState.update { it ->
            it.copy(
                songsList = songs.map { song -> song.toPlaylistItem() },
                currentSong = startSong
            )
        }

        playlistLoadJob = repositoryScope.launch {
            val startIndex = songs.indexOfFirst { it.id == startSong.id }.takeIf { it != -1 } ?: 0
            val windowSize = 2
            val windowStart = (startIndex - windowSize).coerceAtLeast(0)
            val windowEnd = (startIndex + windowSize).coerceAtMost(songs.size - 1)

            val initialSongs = songs.subList(windowStart, windowEnd + 1)
            val startSongIndexInWindow = startIndex - windowStart

            val initialMediaItems = withContext(Dispatchers.IO) {
                initialSongs.map { createMediaItemFromSong(it, context) }
            }

            if (currentLoadingPlaylistId != playlistId) return@launch

            controller.setMediaItems(initialMediaItems, startSongIndexInWindow, 0L)
            controller.prepare()
            controller.play()

            if (windowEnd < songs.size - 1) {
                val afterSongs = songs.subList(windowEnd + 1, songs.size)
                loadChunks(afterSongs, append = true, playlistId)
            }

            if (windowStart > 0) {
                val beforeSongs = songs.subList(0, windowStart)
                loadChunks(beforeSongs, append = false, playlistId)
            }
        }
    }

    private suspend fun loadChunks(songs: List<Song>, append: Boolean, playlistId: String) {
        val chunkSize = 30
        val chunks = songs.chunked(chunkSize)
        val chunksToProcess = if (append) chunks else chunks.asReversed()

        chunksToProcess.forEach { chunk ->
            yield()
            if (currentLoadingPlaylistId != playlistId) return@forEach
            val mediaItems = withContext(Dispatchers.IO) {
                chunk.map { createPartialMediaItemFromSong(it, context) }
            }

            withContext(Dispatchers.Main) {
                if (currentLoadingPlaylistId == playlistId) {
                    if (append) {
                        mediaController?.addMediaItems(mediaItems)
                    } else {
                        mediaController?.addMediaItems(0, mediaItems)
                    }
                }
            }
        }
    }

    fun playShuffledPlaylist(songs: List<Song>) {
        val shuffledSongs = songs.shuffled()
        playPlaylist(shuffledSongs, shuffledSongs.first())
    }

    fun playSongByIdWithRadio(song: Song, removeViewModelList: Boolean = true) {
        if (removeViewModelList) {
            _playerState.update { it.copy(songsList = emptyList()) }
        }

        playSong(song)

        repositoryScope.launch {
            try {
                getRadioFlow(song.id)
                    .flowOn(Dispatchers.IO)
                    .collect { radioSong ->
                        if (radioSong.id != song.id) {
                            val mediaItem = createPartialMediaItemFromSong(radioSong, context)
                            _playerState.update {
                                it.copy(songsList = it.songsList + radioSong.toPlaylistItem())
                            }
                            withContext(Dispatchers.Main) {
                                mediaController?.addMediaItem(mediaItem)
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                val nextMediaItem = createMediaItemFromSong(_playerState.value.songsList[1].song, context)
                withContext(Dispatchers.Main) {
                    mediaController?.replaceMediaItem(1, nextMediaItem)
                }
            }
        }
    }

    fun playNext(song: Song) {
        repositoryScope.launch {
            try {
                val mediaItem = createMediaItemFromSong(song, context)
                val currentMediaIndex = mediaController?.currentMediaItemIndex ?: 0
                val nextIndex = currentMediaIndex + 1

                mediaController?.addMediaItem(nextIndex, mediaItem)
                _playerState.update { currentState ->
                    val updatedList = currentState.songsList.toMutableList()
                    if (nextIndex <= updatedList.size) {
                        updatedList.add(nextIndex, song.toPlaylistItem())
                    } else {
                        updatedList.add(song.toPlaylistItem())
                    }
                    currentState.copy(songsList = updatedList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun enqueueSong(song: Song) {
        repositoryScope.launch {
            try {
                val mediaItem = if (_playerState.value.songsList.size < 2)
                    createMediaItemFromSong(song, context)
                else createPartialMediaItemFromSong(song, context)
                mediaController?.addMediaItem(mediaItem)

                _playerState.update { currentState ->
                    currentState.copy(songsList = currentState.songsList + song.toPlaylistItem())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playNextList(songs: List<Song>) {
        repositoryScope.launch {
            try {
                val mediaItems = songs.map { song -> createPartialMediaItemFromSong(song, context) }
                val currentMediaIndex = mediaController?.currentMediaItemIndex ?: 0
                val nextIndex = currentMediaIndex + 1

                mediaController?.addMediaItems(nextIndex, mediaItems)
                _playerState.update { currentState ->
                    val currentList = currentState.songsList.toMutableList()
                    if (nextIndex <= currentList.size) {
                        currentList.addAll(nextIndex, songs.map { it.toPlaylistItem() })
                    } else {
                        currentList.addAll(nextIndex, songs.map { it.toPlaylistItem() })
                    }
                    currentState.copy(songsList = currentList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun enqueueSongList(songs: List<Song>) {
        repositoryScope.launch {
            try {
                val mediaItems = songs.map { song -> createPartialMediaItemFromSong(song, context) }
                mediaController?.addMediaItems(mediaItems)

                _playerState.update { currentState ->
                    currentState.copy(songsList = currentState.songsList + songs.map { it.toPlaylistItem() })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCurrentPlayingIndex(): Int {
        return mediaController?.currentMediaItemIndex ?: 0
    }

    fun moveSong(fromIndex: Int, toIndex: Int) {
        _playerState.update { currentState ->
            val newList = currentState.songsList.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
            currentState.copy(songsList = newList)
        }
        repositoryScope.launch {
            mediaController?.moveMediaItem(fromIndex, toIndex)
        }
    }

    fun pause() {
        mediaController?.pause()
    }

    fun resume() {
        mediaController?.play()
    }

    fun seekTo(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
        mediaController?.seekTo(position)
    }

    fun skipToNext() {
        if (mediaController?.hasNextMediaItem() == true) {
            mediaController?.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        if (mediaController?.hasPreviousMediaItem() == true) {
            mediaController?.seekToPreviousMediaItem()
        }
    }

    fun skipToSong(index: Int) {
        mediaController?.seekTo(index, 0L)
    }

    fun removeSongFromQueue(id: Long) {
        val indexOfSong = _playerState.value.songsList.indexOfFirst { it.id == id }
        if (indexOfSong != -1) {
            mediaController?.removeMediaItem(indexOfSong)
            _playerState.update {
                it.copy(songsList = it.songsList.filter { songItem -> songItem.id != id })
            }
        }
    }

    fun startSleepTimer(minutes: Long) {
        sleepTimerJob?.cancel()
        val endTime = System.currentTimeMillis() + (minutes * 60 * 1000)

        sleepTimerJob = repositoryScope.launch {
            while (System.currentTimeMillis() < endTime) {
                _playerState.update {
                    it.copy(timeLeftMillis = endTime - System.currentTimeMillis())
                }
                delay(1000) 
            }
            _playerState.update { it.copy(timeLeftMillis = 0L) }
            pause()
        }
    }

    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        _playerState.update { it.copy(timeLeftMillis = 0L) }
    }

    fun changePlayerRepeatMode(repeatMode: PlayerRepeatMode) {
        mediaController?.repeatMode = when(repeatMode) {
            PlayerRepeatMode.RepeatModeAll -> Player.REPEAT_MODE_ALL
            PlayerRepeatMode.RepeatModeOne -> Player.REPEAT_MODE_ONE
            PlayerRepeatMode.RepeatModeOff -> Player.REPEAT_MODE_OFF
        }
    }
}
