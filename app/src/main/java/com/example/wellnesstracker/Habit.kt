package com.example.wellnesstracker

import java.text.SimpleDateFormat
import java.util.*

// Habit data class
data class Habit(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String = "",
    var completedDates: MutableSet<String> = mutableSetOf()
) {
    // Check if habit is completed today
    fun isCompletedToday(): Boolean {
        val today = getCurrentDate()
        return completedDates.contains(today)
    }

    // Toggle completion for today
    fun toggleCompletion() {
        val today = getCurrentDate()
        if (completedDates.contains(today)) {
            completedDates.remove(today)
        } else {
            completedDates.add(today)
        }
    }

    // Get completion streak
    fun getStreak(): Int {
        var streak = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        while (true) {
            val dateStr = sdf.format(calendar.time)
            if (completedDates.contains(dateStr)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    companion object {
        fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}

// Mood entry data class
data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

