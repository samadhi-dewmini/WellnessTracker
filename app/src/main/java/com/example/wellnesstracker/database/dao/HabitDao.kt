package com.example.wellnesstracker.database.dao

import androidx.room.*
import com.example.wellnesstracker.database.entities.HabitEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Habit operations
 * Provides methods to interact with the habits table
 */
@Dao
interface HabitDao {
    
    /**
     * Get all habits as a Flow for reactive updates
     */
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>
    
    /**
     * Get all habits as a list (for one-time queries)
     */
    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsList(): List<HabitEntity>
    
    /**
     * Get a specific habit by ID
     */
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: String): HabitEntity?
    
    /**
     * Insert a new habit
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)
    
    /**
     * Insert multiple habits
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)
    
    /**
     * Update an existing habit
     */
    @Update
    suspend fun updateHabit(habit: HabitEntity)
    
    /**
     * Delete a habit
     */
    @Delete
    suspend fun deleteHabit(habit: HabitEntity)
    
    /**
     * Delete all habits
     */
    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()
}
