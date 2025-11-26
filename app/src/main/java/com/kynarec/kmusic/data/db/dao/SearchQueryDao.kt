package com.kynarec.kmusic.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kynarec.kmusic.data.db.entities.SearchQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchQueryDao {
    // Insert or replace (same id = replace)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(query: SearchQuery)

    // Get all queries (optional: order by your preferred logic)
    @Query("SELECT * FROM SearchQuery ORDER BY id DESC")
    suspend fun getAllQueries(): List<SearchQuery>

    // Live/Reactive: Automatically updates UI using Flow
    @Query("SELECT * FROM SearchQuery ORDER BY id DESC")
    fun observeQueries(): Flow<List<SearchQuery>>

    @Query("SELECT * FROM SearchQuery ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentQueries(limit: Int): Flow<List<SearchQuery>>

    // Get the last 10 search queries
    @Query("SELECT * FROM SearchQuery ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentQueries(limit: Int = 10): List<SearchQuery>

    // Delete a specific query
    @Query("DELETE FROM SearchQuery WHERE query = :query")
    suspend fun deleteQuery(query: String)

    // Clear all saved queries
    @Query("DELETE FROM SearchQuery")
    suspend fun clearQueries()
}