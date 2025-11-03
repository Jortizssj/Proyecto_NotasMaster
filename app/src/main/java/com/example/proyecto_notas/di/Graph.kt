package com.example.proyecto_notas.di

import android.content.Context
import androidx.room.Room
import com.example.proyecto_notas.data.local.AppDatabase
import com.example.proyecto_notas.data.repository.NoteRepository
import com.example.proyecto_notas.data.repository.NoteRepositoryImpl
import com.example.proyecto_notas.data.repository.TaskRepository
import com.example.proyecto_notas.data.repository.TaskRepositoryImpl

object Graph {
    lateinit var database: AppDatabase
        private set

    val noteRepository: NoteRepository by lazy { NoteRepositoryImpl(database.noteDao()) }
    val taskRepository: TaskRepository by lazy { TaskRepositoryImpl(database.taskDao()) }

    fun provide(context: Context) {
        database = Room.databaseBuilder(context, AppDatabase::class.java, "notas_database.db").build()
    }
}
