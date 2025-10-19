package com.example.wellnesstracker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for Mood data
 * Represents a mood entry in the wellness app
 */
@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey
    val id: String,
    val emoji: String,
    val note: String,
    val timestamp: Long
)
