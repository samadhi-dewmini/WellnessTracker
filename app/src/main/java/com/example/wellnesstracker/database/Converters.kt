package com.example.wellnesstracker.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Type converters for Room database
 * Converts complex data types to and from database-compatible types
 */
class Converters {
    private val gson = Gson()

    /**
     * Convert Set<String> to JSON string for storage
     */
    @TypeConverter
    fun fromStringSet(value: Set<String>?): String {
        return gson.toJson(value)
    }

    /**
     * Convert JSON string back to Set<String>
     */
    @TypeConverter
    fun toStringSet(value: String?): Set<String> {
        if (value == null) return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(value, type) ?: emptySet()
    }
}
