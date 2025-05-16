package com.kynarec.kmusic

import android.app.Application
import com.kynarec.kmusic.data.db.KmusicDatabase

class MyApp : Application() {
    lateinit var database: KmusicDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = KmusicDatabase.getDatabase(this)
    }
}
