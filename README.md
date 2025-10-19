# WellnessTracker
SLIIT | Y2 S2 | MAD | Lab Exam 04

## Overview
WellnessTracker is an Android app for tracking habits, mood entries, and water intake. This version (Lab Exam 04) implements proper data persistence using SQLite with Room ORM Library.

## Features
- **Habit Tracking**: Create, track, and manage daily habits
- **Mood Journal**: Log daily mood with emoji and notes
- **Water Reminder**: Track water intake and set reminders
- **Data Persistence**: SQLite database with Room ORM
- **Settings**: Customize app preferences

## Database Implementation
This app uses Room Database for data persistence. See [DATABASE_IMPLEMENTATION.md](DATABASE_IMPLEMENTATION.md) for detailed documentation.

## Build
```bash
./gradlew build
```

## Technologies Used
- Kotlin
- Android SDK
- Room Database (SQLite)
- WorkManager
- Kotlin Coroutines
- Material Design Components
