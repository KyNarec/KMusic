package com.kynarec.kmusic.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kynarec.kmusic.data.db.dao.AlbumDao
import com.kynarec.kmusic.data.db.dao.ArtistDao
import com.kynarec.kmusic.data.db.dao.PersistedQueueDao
import com.kynarec.kmusic.data.db.dao.PlaylistDao
import com.kynarec.kmusic.data.db.dao.SearchQueryDao
import com.kynarec.kmusic.data.db.dao.SongDao
import com.kynarec.kmusic.data.db.entities.Album
import com.kynarec.kmusic.data.db.entities.Artist
import com.kynarec.kmusic.data.db.entities.PersistedQueueItem
import com.kynarec.kmusic.data.db.entities.Playlist
import com.kynarec.kmusic.data.db.entities.SearchQuery
import com.kynarec.kmusic.data.db.entities.Song
import com.kynarec.kmusic.data.db.entities.SongAlbumMap
import com.kynarec.kmusic.data.db.entities.SongPlaylistMap

@Database(
    entities = [
        Song::class,
        PersistedQueueItem::class,
        SearchQuery::class,
        Playlist::class,
        SongPlaylistMap::class,
        Album::class,
        SongAlbumMap::class,
        Artist::class
               ],
    version = 12
)
@TypeConverters(Converters::class)
abstract class KmusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    //    abstract fun queuedMediaItemDao(): QueuedMediaItemDao
    abstract fun persistedQueueDao(): PersistedQueueDao
    abstract fun searchQueryDao(): SearchQueryDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao

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
                    .addMigrations(MIGRATION_10_11)
                    .addMigrations(MIGRATION_11_12)
                    .fallbackToDestructiveMigration(false)
                    .build().also { INSTANCE = it }
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new table with 'DEFAULT 0' for position
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `SongPlaylistMap_new` (
                `songId` TEXT NOT NULL, 
                `playlistId` INTEGER NOT NULL, 
                `position` INTEGER NOT NULL DEFAULT 0, 
                PRIMARY KEY(`songId`, `playlistId`), 
                FOREIGN KEY(`songId`) REFERENCES `Song`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                FOREIGN KEY(`playlistId`) REFERENCES `Playlist`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())

                // 2. Copy the data
                db.execSQL("""
            INSERT INTO `SongPlaylistMap_new` (songId, playlistId, position)
            SELECT songId, playlistId, position FROM `SongPlaylistMap`
        """.trimIndent())

                // 3. Drop the old table
                db.execSQL("DROP TABLE `SongPlaylistMap`")

                // 4. Rename the new table
                db.execSQL("ALTER TABLE `SongPlaylistMap_new` RENAME TO `SongPlaylistMap`")

                // 5. Re-create indices (Room identifies indices by specific names)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_SongPlaylistMap_songId` ON `SongPlaylistMap` (`songId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_SongPlaylistMap_playlistId` ON `SongPlaylistMap` (`playlistId`)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE Playlist ADD COLUMN isEditable INTEGER NOT NULL DEFAULT 1"
                )
                database.execSQL(
                    "ALTER TABLE Playlist ADD COLUMN isYoutubePlaylist INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }

}
