# WellnessTracker Database Architecture

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          UI Layer (Fragments)                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │ HabitsFragment   │  │  MoodFragment    │  │ SettingsFragment │  │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘  │
└───────────┼────────────────────┼────────────────────┼──────────────┘
            │                    │                    │
            └────────────────────┼────────────────────┘
                                 │
                                 ▼
         ┌────────────────────────────────────────────────┐
         │           DataManager (Data Layer)             │
         │  - Maintains backward compatibility            │
         │  - Handles coroutine operations                │
         │  - Auto-migrates SharedPreferences data        │
         └───────────┬─────────────────────┬──────────────┘
                     │                     │
         ┌───────────▼──────────┐  ┌──────▼───────────────┐
         │  HabitRepository     │  │  MoodRepository      │
         │  - Data abstraction  │  │  - Data abstraction  │
         │  - Model conversion  │  │  - Model conversion  │
         └───────────┬──────────┘  └──────┬───────────────┘
                     │                     │
         ┌───────────▼──────────┐  ┌──────▼───────────────┐
         │     HabitDao         │  │      MoodDao         │
         │  - CRUD operations   │  │  - CRUD operations   │
         │  - SQL queries       │  │  - SQL queries       │
         └───────────┬──────────┘  └──────┬───────────────┘
                     │                     │
                     └──────────┬──────────┘
                                │
                ┌───────────────▼──────────────────┐
                │      WellnessDatabase            │
                │   (Room Database - Singleton)    │
                │  - Database management           │
                │  - DAO access                    │
                │  - Type converters               │
                └───────────────┬──────────────────┘
                                │
                ┌───────────────▼──────────────────┐
                │    SQLite Database File          │
                │  wellness_database.db            │
                │  ┌─────────────────────────────┐ │
                │  │  habits table               │ │
                │  │  - id (PK)                  │ │
                │  │  - name                     │ │
                │  │  - description              │ │
                │  │  - completedDates (JSON)    │ │
                │  └─────────────────────────────┘ │
                │  ┌─────────────────────────────┐ │
                │  │  moods table                │ │
                │  │  - id (PK)                  │ │
                │  │  - emoji                    │ │
                │  │  - note                     │ │
                │  │  - timestamp                │ │
                │  └─────────────────────────────┘ │
                └──────────────────────────────────┘
```

## Data Flow

### Saving Data (Write Operation)
```
Fragment → DataManager.saveHabits(habits)
    ↓
HabitRepository.insertHabits(habits)
    ↓
Convert Habit → HabitEntity
    ↓
HabitDao.insertHabits(entities) [suspend]
    ↓
Room executes SQL INSERT
    ↓
SQLite Database
```

### Loading Data (Read Operation)
```
Fragment → DataManager.loadHabits()
    ↓
HabitRepository.getAllHabitsList() [runBlocking]
    ↓
HabitDao.getAllHabitsList() [suspend]
    ↓
Room executes SQL SELECT
    ↓
Convert HabitEntity → Habit
    ↓
Return MutableList<Habit>
```

## Component Relationships

### Entity Layer
```
Habit (Domain Model)     ←→     HabitEntity (Database Entity)
    ├─ id                            ├─ id (PK)
    ├─ name                          ├─ name
    ├─ description                   ├─ description
    └─ completedDates                └─ completedDates (via TypeConverter)

MoodEntry (Domain Model) ←→     MoodEntity (Database Entity)
    ├─ id                            ├─ id (PK)
    ├─ emoji                         ├─ emoji
    ├─ note                          ├─ note
    └─ timestamp                     └─ timestamp
```

### Repository Pattern
```
┌─────────────────────────────────────────────────────┐
│ Repository Pattern Benefits                         │
├─────────────────────────────────────────────────────┤
│ 1. Abstracts data source details                    │
│ 2. Provides clean API for data operations           │
│ 3. Handles domain ↔ entity conversions              │
│ 4. Easy to test with mock repositories              │
│ 5. Can switch data sources without UI changes       │
│ 6. Centralizes business logic for data operations   │
└─────────────────────────────────────────────────────┘
```

## Threading Model

```
┌────────────────────┐
│   Main Thread      │  ← UI operations (Fragments)
└─────────┬──────────┘
          │
          │ DataManager methods
          ▼
┌────────────────────┐
│  IO Thread Pool    │  ← Database operations (Coroutines)
│  (Dispatchers.IO)  │     - All DAO operations
└────────────────────┘     - Repository operations
                          - Data migration
```

### Coroutine Usage
- **CoroutineScope(Dispatchers.IO).launch**: For fire-and-forget saves
- **runBlocking(Dispatchers.IO)**: For synchronous loads (maintains API compatibility)
- **suspend functions**: All DAO and Repository methods
- **Flow**: Available for reactive updates (not currently used by DataManager)

## Type Conversion

```
┌─────────────────────────────────────────────────────────┐
│  Converters.kt - Room Type Converters                   │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Set<String>  ──────→ JSON String  ──────→ SQLite TEXT  │
│  (Kotlin)    fromSet()            (Database)            │
│                                                          │
│  Set<String>  ←────── JSON String  ←────── SQLite TEXT  │
│  (Kotlin)     toSet()              (Database)           │
│                                                          │
│  Example:                                               │
│  ["2024-01-01", "2024-01-02"]  →  '["2024-01-01"...]'  │
└─────────────────────────────────────────────────────────┘
```

## Database Initialization

```kotlin
// Singleton Pattern
WellnessDatabase.getDatabase(context)
    ↓
Check if INSTANCE exists?
    ├─ Yes → Return existing INSTANCE
    └─ No  → Create new instance
             ↓
         synchronized(this) {
             Room.databaseBuilder()
                 .fallbackToDestructiveMigration()
                 .build()
         }
             ↓
         Set INSTANCE
             ↓
         Return INSTANCE
```

## Migration Strategy

```
┌──────────────────────────────────────────────────────────┐
│  First Launch After Upgrade                              │
├──────────────────────────────────────────────────────────┤
│  1. DataManager created                                  │
│  2. DataMigrationHelper.migrateDataIfNeeded() called     │
│  3. Check migration_complete flag                        │
│     ├─ true:  Skip migration                             │
│     └─ false: Continue migration                         │
│  4. Read habits JSON from SharedPreferences              │
│  5. Parse JSON to List<Habit>                            │
│  6. Insert habits into Room database                     │
│  7. Read moods JSON from SharedPreferences               │
│  8. Parse JSON to List<MoodEntry>                        │
│  9. Insert moods into Room database                      │
│  10. Set migration_complete = true                       │
└──────────────────────────────────────────────────────────┘
```

## Key Design Patterns

### 1. Singleton Pattern
- **Used in:** WellnessDatabase
- **Purpose:** Ensure only one database instance

### 2. Repository Pattern
- **Used in:** HabitRepository, MoodRepository
- **Purpose:** Abstract data source, provide clean API

### 3. DAO Pattern
- **Used in:** HabitDao, MoodDao
- **Purpose:** Separate persistence logic from business logic

### 4. Type Converter Pattern
- **Used in:** Converters
- **Purpose:** Convert complex types for database storage

### 5. Builder Pattern
- **Used in:** Room.databaseBuilder()
- **Purpose:** Flexible database configuration

## Dependencies

```gradle
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Coroutines (for async operations)
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Gson (for migration from SharedPreferences)
implementation("com.google.code.gson:gson:2.10.1")
```

## Performance Considerations

### Database Operations
- ✅ All database operations run on IO thread
- ✅ No blocking on main thread
- ✅ Efficient batch insertions
- ✅ Indexed primary keys
- ✅ Singleton prevents multiple instances

### Memory
- ✅ Lazy initialization of database
- ✅ Entities use data classes (efficient)
- ✅ Flow support for memory-efficient streaming

### Future Optimizations
- 📝 Add database indices for frequently queried fields
- 📝 Implement pagination for large datasets
- 📝 Use Flow in DataManager for reactive updates
- 📝 Add database query caching

## Testing Strategy

### Unit Tests (Recommended)
```kotlin
// Test Repository
@Test
fun insertHabit_retrievesHabit() = runBlocking {
    val habit = Habit(name = "Test", description = "Test")
    habitRepository.insertHabit(habit)
    val result = habitRepository.getHabitById(habit.id)
    assertEquals(habit, result)
}
```

### Instrumented Tests (Recommended)
```kotlin
// Test DAO with in-memory database
@RunWith(AndroidJUnit4::class)
class HabitDaoTest {
    private lateinit var database: WellnessDatabase
    private lateinit var habitDao: HabitDao
    
    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WellnessDatabase::class.java
        ).build()
        habitDao = database.habitDao()
    }
}
```

## Error Handling

### Current Implementation
- Migration errors are caught and logged (won't crash app)
- Database operations use try-catch blocks
- Fallback to destructive migration on schema conflicts

### Recommendations
- Add proper error logging
- Implement retry logic for failed operations
- Add user-facing error messages
- Implement data backup before migrations

## Summary

This architecture provides:
- ✅ Clean separation of concerns
- ✅ Type-safe database operations
- ✅ Efficient async operations
- ✅ Easy testing with in-memory database
- ✅ Smooth migration from SharedPreferences
- ✅ Backward compatible API
- ✅ Scalable for future features
