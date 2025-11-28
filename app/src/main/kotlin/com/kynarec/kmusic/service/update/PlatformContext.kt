package com.kynarec.kmusic.service.update

import android.content.Context

object PlatformContext {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun get(): Any = appContext ?: throw IllegalStateException(
        "PlatformContext not initialized. Call PlatformContext.initialize(context) in your Application class."
    )
}