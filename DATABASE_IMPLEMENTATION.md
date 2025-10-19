# Room Database Implementation - Lab Exam 4

This document describes the SQLite and Room ORM implementation for the WellnessTracker app.

## Overview

The app has been upgraded from using SharedPreferences (Lab Exam 3) to using Room Database (Lab Exam 4) for persistent data storage. This provides better performance, type safety, and query capabilities.

## Architecture

### Database Package Structure

```
database/
├── entities/
│   ├── HabitEntity.kt          # Entity for habits table
│   └── MoodEntity.kt            # Entity for moods table
├── dao/
│   ├── HabitDao.kt              # Data Access Object for habits
│   └── MoodDao.kt               # Data Access Object for moods
├── Converters.kt                # Type converters for complex types
├── WellnessDatabase.kt          # Main database class
├── HabitRepository.kt           # Repository for habit operations
├── MoodRepository.kt            # Repository for mood operations
└── DataMigrationHelper.kt       # Helper to migrate from SharedPreferences
```

## Key Components

### 1. Entities

**HabitEntity** (`database/entities/HabitEntity.kt`)
- Table: `habits`
- Fields:
  - `id: String` (Primary Key)
  - `name: String`
  - `description: String`
  - `completedDates: Set<String>` (stored as JSON via TypeConverter)

**MoodEntity** (`database/entities/MoodEntity.kt`)
- Table: `moods`
- Fields:
  - `id: String` (Primary Key)
  - `emoji: String`
  - `note: String`
  - `timestamp: Long`

### 2. Type Converters

**Converters** (`database/Converters.kt`)
- Converts `Set<String>` to JSON string and vice versa
- Used by Room to store complex data types in SQLite

### 3. Data Access Objects (DAOs)

**HabitDao** (`database/dao/HabitDao.kt`)
- Provides CRUD operations for habits
- Supports both Flow (reactive) and suspend (one-time) queries
- Methods:
  - `getAllHabits()`: Returns Flow<List<HabitEntity>>
  - `getAllHabitsList()`: Returns List<HabitEntity> (suspend)
  - `getHabitById(id)`: Returns HabitEntity? (suspend)
  - `insertHabit(habit)`: Insert single habit (suspend)
  - `insertHabits(habits)`: Insert multiple habits (suspend)
  - `updateHabit(habit)`: Update habit (suspend)
  - `deleteHabit(habit)`: Delete habit (suspend)
  - `deleteAllHabits()`: Delete all habits (suspend)

**MoodDao** (`database/dao/MoodDao.kt`)
- Provides CRUD operations for moods
- Supports both Flow (reactive) and suspend (one-time) queries
- Methods similar to HabitDao

### 4. Database Class

**WellnessDatabase** (`database/WellnessDatabase.kt`)
- Main Room database class
- Singleton pattern ensures one instance per app
- Database name: `wellness_database`
- Version: 1
- Provides access to DAOs

### 5. Repositories

**HabitRepository** (`database/HabitRepository.kt`)
- Abstracts data operations for habits
- Converts between domain models (Habit) and entities (HabitEntity)
- Provides clean API for data access

**MoodRepository** (`database/MoodRepository.kt`)
- Abstracts data operations for moods
- Converts between domain models (MoodEntry) and entities (MoodEntity)
- Provides clean API for data access

### 6. Data Migration

**DataMigrationHelper** (`database/DataMigrationHelper.kt`)
- Automatically migrates data from SharedPreferences to Room
- Runs once on first app launch after upgrade
- Ensures no data loss for existing users

## Build Configuration

### Project-level `build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false  // Added KSP plugin
}
```

### App-level `build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)  // Added KSP plugin
}

dependencies {
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)  // Using KSP instead of kapt
    // ... other dependencies
}
```

### `gradle/libs.versions.toml`
```toml
[versions]
agp = "8.1.4"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"  # Added KSP version
room = "2.6.1"         # Added Room version

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }  # Added KSP plugin
```

## Usage

### DataManager (Updated)

The `DataManager` class has been updated to use Room database while maintaining the same API:

```kotlin
val dataManager = DataManager(context)

// Save habits
dataManager.saveHabits(habitsList)

// Load habits
val habits = dataManager.loadHabits()

// Save moods
dataManager.saveMoods(moodsList)

// Load moods
val moods = dataManager.loadMoods()
```

**Note**: All database operations are handled asynchronously using Kotlin Coroutines to avoid blocking the main thread.

## Benefits of Room Implementation

1. **Type Safety**: Compile-time verification of SQL queries
2. **Better Performance**: Optimized database operations
3. **Reactive Data**: Support for Flow to observe data changes
4. **Easier Testing**: Room provides in-memory database for testing
5. **Migration Support**: Built-in database migration support
6. **Less Boilerplate**: Less code compared to raw SQLite
7. **Coroutine Support**: Native support for Kotlin Coroutines

## Migration from Lab Exam 3

The app automatically migrates data from SharedPreferences to Room on first launch:
1. Existing habits are loaded from SharedPreferences JSON
2. Habits are inserted into Room database
3. Existing moods are loaded from SharedPreferences JSON
4. Moods are inserted into Room database
5. Migration flag is set to prevent repeated migration

## Testing

To test the implementation:
1. Build the project: `./gradlew build`
2. Run the app on an emulator or device
3. Add habits and mood entries
4. Verify data persists after app restart
5. Check SQLite database using Database Inspector in Android Studio

## Future Enhancements

Potential improvements for future versions:
- Add database migrations for schema changes
- Implement LiveData observers in UI
- Add more complex queries (e.g., filter by date range)
- Add data export/import functionality
- Implement database backup
