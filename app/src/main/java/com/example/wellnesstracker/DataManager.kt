package com.example.wellnesstracker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("WellnessData", Context.MODE_PRIVATE)
    private val gson = Gson()

    // save habits as JSON
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString("habits", json).apply()
    }
     //load habits from JSON
    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString("habits", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    //  MOODS
    fun saveMoods(moods: List<MoodEntry>) {
        val json = gson.toJson(moods)
        prefs.edit().putString("moods", json).apply()
    }

    fun loadMoods(): MutableList<MoodEntry> {
        val json = prefs.getString("moods", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<MoodEntry>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
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

