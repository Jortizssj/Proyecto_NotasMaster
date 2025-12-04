package com.example.proyecto_notas.di

import android.content.Context
import androidx.room.Room
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.example.proyecto_notas.data.local.AppDatabase
import com.example.proyecto_notas.data.repository.NoteRepository
import com.example.proyecto_notas.data.repository.NoteRepositoryImpl
import com.example.proyecto_notas.data.repository.ReminderRepository
import com.example.proyecto_notas.data.repository.ReminderRepositoryImpl
import com.example.proyecto_notas.data.repository.TaskRepository
import com.example.proyecto_notas.data.repository.TaskRepositoryImpl

object Graph {
    lateinit var database: AppDatabase
        private set

    lateinit var imageLoader: ImageLoader
        private set

    val noteRepository: NoteRepository by lazy { NoteRepositoryImpl(database.noteDao()) }
    val taskRepository: TaskRepository by lazy { TaskRepositoryImpl(database.taskDao()) }
    val reminderRepository: ReminderRepository by lazy { ReminderRepositoryImpl(database.reminderDao()) }

    fun provide(context: Context) {
        database = Room.databaseBuilder(context, AppDatabase::class.java, "notas_database.db")
            .fallbackToDestructiveMigration()
            .build()

        imageLoader = ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
