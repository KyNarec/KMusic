package com.kynarec.kmusic.service.di

import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.data.db.entities.PlaylistPreview
import com.kynarec.kmusic.data.repository.LibraryRepository
import com.kynarec.kmusic.data.repository.LyricsRepository
import com.kynarec.kmusic.data.repository.PlayerRepository
import com.kynarec.kmusic.service.update.PlatformUpdateManager
import com.kynarec.kmusic.service.update.UpdateManager
import com.kynarec.kmusic.ui.viewModels.AppViewModel
import com.kynarec.kmusic.ui.viewModels.DataViewModel
import com.kynarec.kmusic.ui.viewModels.LibraryViewModel
import com.kynarec.kmusic.ui.viewModels.LyricsViewModel
import com.kynarec.kmusic.ui.viewModels.PlayerScreenViewModel
import com.kynarec.kmusic.ui.viewModels.PlayerViewModel
import com.kynarec.kmusic.ui.viewModels.PlaylistOfflineDetailViewModel
import com.kynarec.kmusic.ui.viewModels.PlaylistOnlineDetailViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { (androidApplication() as KMusic).database }
    single { get<KmusicDatabase>().songDao() }
    single { get<KmusicDatabase>().playlistDao() }
    single { get<KmusicDatabase>().albumDao() }
    single { get<KmusicDatabase>().artistDao() }

    single<CoroutineScope> {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    single { LibraryRepository(get(), get(), get(), get()) }
    single { PlayerRepository(androidApplication()) }
    single { LyricsRepository(get(), get(), get()) }

    viewModel { AppViewModel(get()) }
    viewModel { PlayerScreenViewModel(get(), get(), get()) }
    viewModel { LibraryViewModel(get(), get()) }

    viewModel { LyricsViewModel(get(), get()) }


    single { KSafe(androidApplication()) }
    viewModel {
        SettingsViewModel(
            get()
        )
    }

    single<UpdateManager> { PlatformUpdateManager() }
    viewModel {
        UpdateViewModel(
            updateManager = get()
        )
    }

    viewModel {
        PlayerViewModel()
    }

    viewModel {
        DataViewModel(
            application = androidApplication(),
            downloadManager = get(),
            downloadCache = get(),
            database = get()
        )
    }

    viewModel { (playlistId: Long) ->
        PlaylistOfflineDetailViewModel(playlistId = playlistId, database = get(), ksafe = get())
    }

    viewModel { (playlistPreview: PlaylistPreview) ->
        PlaylistOnlineDetailViewModel(playlistPreview = playlistPreview, application = get())
    }
}