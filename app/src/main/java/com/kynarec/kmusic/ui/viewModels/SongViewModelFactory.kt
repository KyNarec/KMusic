package com.kynarec.kmusic.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kynarec.kmusic.data.db.dao.SongDao

/**
 * A custom ViewModel Factory that allows us to pass a dependency (SongDao)
 * into the SongViewModel's constructor.
 *
 * @param songDao The SongDao instance to be injected.
 */
@Deprecated("Use new MusicViewModel")
class SongViewModelFactory(
    private val songDao: SongDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
            return SongViewModel(songDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}