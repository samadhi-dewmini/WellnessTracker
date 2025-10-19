package com.example.wellnesstracker.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.wellnesstracker.database.Converters

/**
 * Room entity for Habit data
 * Represents a habit tracked in the wellness app
 */
@Entity(tableName = "habits")
@TypeConverters(Converters::class)
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val completedDates: Set<String> = emptySet()
)
