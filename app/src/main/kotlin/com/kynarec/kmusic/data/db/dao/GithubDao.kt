package com.kynarec.kmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kynarec.kmusic.data.db.entities.GitHubRelease

@Dao
interface GithubDao {
    @Query("SELECT * FROM GithubRelease ORDER BY id DESC")
    suspend fun getAllReleases(): List<GitHubRelease>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReleases(releases: List<GitHubRelease>)

    @Query("DELETE FROM GithubRelease")
    suspend fun deleteAllReleases()

    @Update
    suspend fun updateReleases(releases: List<GitHubRelease>)
}