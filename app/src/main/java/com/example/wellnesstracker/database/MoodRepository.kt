package com.example.wellnesstracker.database

import com.example.wellnesstracker.MoodEntry
import com.example.wellnesstracker.database.dao.MoodDao
import com.example.wellnesstracker.database.entities.MoodEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for Mood data
 * Abstracts data operations and converts between domain models and entities
 */
class MoodRepository(private val moodDao: MoodDao) {
    
    /**
     * Get all moods as a Flow, converting entities to domain models
     */
    fun getAllMoods(): Flow<List<MoodEntry>> {
        return moodDao.getAllMoods().map { entities ->
            entities.map { it.toMoodEntry() }
        }
    }
    
    /**
     * Get all moods as a list
     */
    suspend fun getAllMoodsList(): List<MoodEntry> {
        return moodDao.getAllMoodsList().map { it.toMoodEntry() }
    }
    
    /**
     * Get a specific mood by ID
     */
    suspend fun getMoodById(moodId: String): MoodEntry? {
        return moodDao.getMoodById(moodId)?.toMoodEntry()
    }
    
    /**
     * Insert a new mood entry
     */
    suspend fun insertMood(mood: MoodEntry) {
        moodDao.insertMood(mood.toEntity())
    }
    
    /**
     * Insert multiple mood entries
     */
    suspend fun insertMoods(moods: List<MoodEntry>) {
        moodDao.insertMoods(moods.map { it.toEntity() })
    }
    
    /**
     * Update an existing mood entry
     */
    suspend fun updateMood(mood: MoodEntry) {
        moodDao.updateMood(mood.toEntity())
    }
    
    /**
     * Delete a mood entry
     */
    suspend fun deleteMood(mood: MoodEntry) {
        moodDao.deleteMood(mood.toEntity())
    }
    
    /**
     * Delete all mood entries
     */
    suspend fun deleteAllMoods() {
        moodDao.deleteAllMoods()
    }
    
    /**
     * Convert MoodEntry domain model to MoodEntity
     */
    private fun MoodEntry.toEntity() = MoodEntity(
        id = id,
        emoji = emoji,
        note = note,
        timestamp = timestamp
    )
    
    /**
     * Convert MoodEntity to MoodEntry domain model
     */
    private fun MoodEntity.toMoodEntry() = MoodEntry(
        id = id,
        emoji = emoji,
        note = note,
        timestamp = timestamp
    )
}
