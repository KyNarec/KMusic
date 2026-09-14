package com.kynarec.kmusic.service.di

import com.kynarec.lrclib.LrcLib
import com.kynarec.lrclib.LrcLibRepository
import org.koin.dsl.module

val lrcLibModule = module {
    single { LrcLib() }
    single { LrcLibRepository(get()) }
}