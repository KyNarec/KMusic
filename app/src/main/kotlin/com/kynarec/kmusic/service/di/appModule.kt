package com.kynarec.kmusic.service.di

import com.kynarec.kmusic.KMusic
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.service.update.PlatformUpdateManager
import com.kynarec.kmusic.service.update.UpdateManager
import com.kynarec.kmusic.ui.screens.player.PlayerViewModel
import com.kynarec.kmusic.ui.viewModels.MusicViewModel
import com.kynarec.kmusic.ui.viewModels.SettingsViewModel
import com.kynarec.kmusic.ui.viewModels.UpdateViewModel
import eu.anifantakis.lib.ksafe.KSafe
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { (androidApplication() as KMusic).database }
    single { get<KmusicDatabase>().songDao() }
    single { get<KmusicDatabase>().playlistDao() }
    single { get<KmusicDatabase>().albumDao() }
    single { get<KmusicDatabase>().artistDao() }

    viewModel {
        MusicViewModel(
            application = androidApplication(),
            songDao = get(),
            playlistDao = get(),
            albumDao = get(),
            artistDao = get(),
            lyricsRepository = get() // This comes from LrcLib module
        )
    }

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
}