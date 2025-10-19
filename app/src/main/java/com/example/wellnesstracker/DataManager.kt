package com.example.wellnesstracker

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnesstracker.database.WellnessDatabase
import com.example.wellnesstracker.database.HabitRepository
import com.example.wellnesstracker.database.MoodRepository
import com.example.wellnesstracker.database.DataMigrationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DataManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("WellnessData", Context.MODE_PRIVATE)
    
    // Room database components
    private val database = WellnessDatabase.getDatabase(context)
    private val habitRepository = HabitRepository(database.habitDao())
    private val moodRepository = MoodRepository(database.moodDao())
    private val migrationHelper = DataMigrationHelper(context)
    
    init {
        // Migrate data from SharedPreferences to Room on first launch
        CoroutineScope(Dispatchers.IO).launch {
            migrationHelper.migrateDataIfNeeded()
        }
    }

    // save habits using Room database
    fun saveHabits(habits: List<Habit>) {
        CoroutineScope(Dispatchers.IO).launch {
            habitRepository.insertHabits(habits)
        }
    }
    
    // load habits from Room database
    fun loadHabits(): MutableList<Habit> {
        return runBlocking(Dispatchers.IO) {
            habitRepository.getAllHabitsList().toMutableList()
        }
    }

    // save moods using Room database
    fun saveMoods(moods: List<MoodEntry>) {
        CoroutineScope(Dispatchers.IO).launch {
            moodRepository.insertMoods(moods)
        }
    }

    // load moods from Room database
    fun loadMoods(): MutableList<MoodEntry> {
        return runBlocking(Dispatchers.IO) {
            moodRepository.getAllMoodsList().toMutableList()
        }
    }

    // SETTINGS
    fun saveReminderInterval(minutes: Int) {
        prefs.edit().putInt("reminder_interval", minutes).apply()
    }

    fun getReminderInterval(): Int {
        return prefs.getInt("reminder_interval", 120) // Default 2 hours
    }

    fun saveReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("reminder_enabled", enabled).apply()
    }

    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean("reminder_enabled", false)
    }

    fun saveDailyWaterGoal(glasses: Int) {
        prefs.edit().putInt("water_goal", glasses).apply()
    }

    fun getDailyWaterGoal(): Int {
        return prefs.getInt("water_goal", 8)
    }

    fun saveWaterCount(count: Int) {
        val today = Habit.getCurrentDate()
        prefs.edit().putInt("water_$today", count).apply()
    }

    fun getWaterCount(): Int {
        val today = Habit.getCurrentDate()
        return prefs.getInt("water_$today", 0)
    }
}

