package com.example.wellnesstracker.database.dao

import androidx.room.*
import com.example.wellnesstracker.database.entities.MoodEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Mood operations
 * Provides methods to interact with the moods table
 */
@Dao
interface MoodDao {
    
    /**
     * Get all moods as a Flow for reactive updates, ordered by timestamp descending
     */
    @Query("SELECT * FROM moods ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntity>>
    
    /**
     * Get all moods as a list (for one-time queries)
     */
    @Query("SELECT * FROM moods ORDER BY timestamp DESC")
    suspend fun getAllMoodsList(): List<MoodEntity>
    
    /**
     * Get a specific mood by ID
     */
    @Query("SELECT * FROM moods WHERE id = :moodId")
    suspend fun getMoodById(moodId: String): MoodEntity?
    
    /**
     * Insert a new mood entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntity)
    
    /**
     * Insert multiple mood entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<MoodEntity>)
    
    /**
     * Update an existing mood entry
     */
    @Update
    suspend fun updateMood(mood: MoodEntity)
    
    /**
     * Delete a mood entry
     */
    @Delete
    suspend fun deleteMood(mood: MoodEntity)
    
    /**
     * Delete all mood entries
     */
    @Query("DELETE FROM moods")
    suspend fun deleteAllMoods()
}
