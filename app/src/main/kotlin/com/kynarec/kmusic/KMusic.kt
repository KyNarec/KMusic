package com.kynarec.kmusic

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.kynarec.kmusic.data.db.KmusicDatabase
import com.kynarec.kmusic.service.di.appModule
import com.kynarec.kmusic.service.di.lrcLibModule
import com.kynarec.kmusic.service.di.mediaModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class KMusic : Application(), ImageLoaderFactory {
    lateinit var database: KmusicDatabase
        private set

    companion object {
        lateinit var instance: KMusic
            private set
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of the app's available memory
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .build()
            }
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = KmusicDatabase.getDatabase(this)
        startKoin {
            androidLogger()
            androidContext(this@KMusic)
            modules(lrcLibModule, appModule, mediaModule)
        }
    }
}
