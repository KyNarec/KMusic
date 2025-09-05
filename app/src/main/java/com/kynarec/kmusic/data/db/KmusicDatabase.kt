package com.kynarec.kmusic.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kynarec.kmusic.data.db.dao.QueuedMediaItemDao
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.Song

@Database(
    entities = [Song::class],  // add others as needed
    version = 1
)
abstract class KmusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun queuedMediaItemDao(): QueuedMediaItemDao


    companion object {
        @Volatile private var INSTANCE: KmusicDatabase? = null

        fun getDatabase(context: Context): KmusicDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    KmusicDatabase::class.java,
                    "kmusic.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
