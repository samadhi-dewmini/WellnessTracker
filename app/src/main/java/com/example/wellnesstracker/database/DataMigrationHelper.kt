package com.example.wellnesstracker.database

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnesstracker.Habit
import com.example.wellnesstracker.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class to migrate data from SharedPreferences to Room database
 * This ensures a smooth transition for existing users
 */
class DataMigrationHelper(context: Context) {
    
    private val prefs: SharedPreferences =
        context.getSharedPreferences("WellnessData", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val database = WellnessDatabase.getDatabase(context)
    private val habitRepository = HabitRepository(database.habitDao())
    private val moodRepository = MoodRepository(database.moodDao())
    
    /**
     * Check if migration has been completed
     */
    private fun isMigrationComplete(): Boolean {
        return prefs.getBoolean("migration_complete", false)
    }
    
    /**
     * Mark migration as complete
     */
    private fun markMigrationComplete() {
        prefs.edit().putBoolean("migration_complete", true).apply()
    }
    
    /**
     * Migrate all data from SharedPreferences to Room
     * Should be called once when the app starts
     */
    suspend fun migrateDataIfNeeded() = withContext(Dispatchers.IO) {
        if (isMigrationComplete()) {
            return@withContext
        }
        
        try {
            // Migrate habits
            val habitsJson = prefs.getString("habits", null)
            if (habitsJson != null) {
                val type = object : TypeToken<MutableList<Habit>>() {}.type
                val habits: List<Habit>? = gson.fromJson(habitsJson, type)
                habits?.let {
                    if (it.isNotEmpty()) {
                        habitRepository.insertHabits(it)
                    }
                }
            }
            
            // Migrate moods
            val moodsJson = prefs.getString("moods", null)
            if (moodsJson != null) {
                val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
                val moods: List<MoodEntry>? = gson.fromJson(moodsJson, type)
                moods?.let {
                    if (it.isNotEmpty()) {
                        moodRepository.insertMoods(it)
                    }
                }
            }
            
            // Mark migration as complete
            markMigrationComplete()
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
        }
    }
}
