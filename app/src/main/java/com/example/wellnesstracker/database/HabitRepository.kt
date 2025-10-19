package com.example.wellnesstracker.database

import com.example.wellnesstracker.Habit
import com.example.wellnesstracker.database.dao.HabitDao
import com.example.wellnesstracker.database.entities.HabitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for Habit data
 * Abstracts data operations and converts between domain models and entities
 */
class HabitRepository(private val habitDao: HabitDao) {
    
    /**
     * Get all habits as a Flow, converting entities to domain models
     */
    fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits().map { entities ->
            entities.map { it.toHabit() }
        }
    }
    
    /**
     * Get all habits as a list
     */
    suspend fun getAllHabitsList(): List<Habit> {
        return habitDao.getAllHabitsList().map { it.toHabit() }
    }
    
    /**
     * Get a specific habit by ID
     */
    suspend fun getHabitById(habitId: String): Habit? {
        return habitDao.getHabitById(habitId)?.toHabit()
    }
    
    /**
     * Insert a new habit
     */
    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit.toEntity())
    }
    
    /**
     * Insert multiple habits
     */
    suspend fun insertHabits(habits: List<Habit>) {
        habitDao.insertHabits(habits.map { it.toEntity() })
    }
    
    /**
     * Update an existing habit
     */
    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit.toEntity())
    }
    
    /**
     * Delete a habit
     */
    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit.toEntity())
    }
    
    /**
     * Delete all habits
     */
    suspend fun deleteAllHabits() {
        habitDao.deleteAllHabits()
    }
    
    /**
     * Convert Habit domain model to HabitEntity
     */
    private fun Habit.toEntity() = HabitEntity(
        id = id,
        name = name,
        description = description,
        completedDates = completedDates
    )
    
    /**
     * Convert HabitEntity to Habit domain model
     */
    private fun HabitEntity.toHabit() = Habit(
        id = id,
        name = name,
        description = description,
        completedDates = completedDates.toMutableSet()
    )
}
