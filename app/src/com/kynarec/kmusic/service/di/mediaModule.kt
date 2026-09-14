package com.kynarec.kmusic.service.di

import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File
import java.util.concurrent.Executors

@UnstableApi
val mediaModule = module {
    single { StandaloneDatabaseProvider(androidContext()) }

    single {
        val downloadContentDirectory = File(
            androidContext().getExternalFilesDir(null),
            "downloads"
        )
        SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), get<StandaloneDatabaseProvider>())
    }

    single { DefaultHttpDataSource.Factory() }

    single {
        DownloadManager(
            androidContext(),
            get<StandaloneDatabaseProvider>(),
            get<SimpleCache>(),
            get<DefaultHttpDataSource.Factory>(),
            Executors.newFixedThreadPool(6)
        )
    }
}