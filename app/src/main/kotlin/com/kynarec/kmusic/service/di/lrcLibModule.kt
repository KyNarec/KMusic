package com.kynarec.kmusic.service.di

import com.kynarec.lrclib.LrcLib
import com.kynarec.lrclib.LyricsRepository
import org.koin.dsl.module

val lrcLibModule = module {
    single { LrcLib() }
    single { LyricsRepository(get()) }
}