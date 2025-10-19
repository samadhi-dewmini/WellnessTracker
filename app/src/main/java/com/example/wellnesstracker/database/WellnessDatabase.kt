package com.example.wellnesstracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wellnesstracker.database.dao.HabitDao
import com.example.wellnesstracker.database.dao.MoodDao
import com.example.wellnesstracker.database.entities.HabitEntity
import com.example.wellnesstracker.database.entities.MoodEntity

/**
 * Room database for the Wellness Tracker app
 * Manages the SQLite database and provides DAOs for data access
 */
@Database(
    entities = [HabitEntity::class, MoodEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WellnessDatabase : RoomDatabase() {
    
    /**
     * Get DAO for habit operations
     */
    abstract fun habitDao(): HabitDao
    
    /**
     * Get DAO for mood operations
     */
    abstract fun moodDao(): MoodDao
    
    companion object {
        @Volatile
        private var INSTANCE: WellnessDatabase? = null
        
        /**
         * Get database instance (singleton pattern)
         * Creates database if it doesn't exist
         */
        fun getDatabase(context: Context): WellnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellnessDatabase::class.java,
                    "wellness_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
