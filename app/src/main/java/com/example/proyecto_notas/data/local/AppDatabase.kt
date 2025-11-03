package com.example.proyecto_notas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
}
