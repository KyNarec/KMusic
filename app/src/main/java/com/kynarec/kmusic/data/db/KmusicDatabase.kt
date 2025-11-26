package com.kynarec.kmusic.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kynarec.kmusic.data.db.dao.PersistedQueueDao
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.PersistedQueueItem
import com.kynarec.kmusic.data.db.entities.Song

@Database(
    entities = [Song::class, PersistedQueueItem::class],  // add others as needed
    version = 2
)
abstract class KmusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
//    abstract fun queuedMediaItemDao(): QueuedMediaItemDao
    abstract fun persistedQueueDao(): PersistedQueueDao


    companion object {
        @Volatile private var INSTANCE: KmusicDatabase? = null

        fun getDatabase(context: Context): KmusicDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    KmusicDatabase::class.java,
                    "kmusic.db"
                )
                    //                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(false)
                    .build().also { INSTANCE = it }
            }
        }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Example: Adding a new column 'last_played_timestamp' to an 'songs' table
            //db.execSQL("ALTER TABLE songs ADD COLUMN last_played_timestamp INTEGER")

            // Example: Creating a new table 'playlists'
            // db.execSQL("CREATE TABLE IF NOT EXISTS `playlists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        }
    }
}
