package com.kynarec.kmusic

import android.app.Application
import com.kynarec.kmusic.data.db.KmusicDatabase
import eu.anifantakis.lib.ksafe.KSafe

class KMusic : Application() {
    lateinit var database: KmusicDatabase
        private set

    lateinit var ksafe: KSafe
        private set

    companion object {
        // Singleton instance of the Application
        lateinit var instance: KMusic
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = KmusicDatabase.getDatabase(this)
        ksafe = KSafe(applicationContext)
    }
}
