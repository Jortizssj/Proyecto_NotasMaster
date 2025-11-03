package com.example.proyecto_notas.data.repository

import com.example.proyecto_notas.data.local.Note
import com.example.proyecto_notas.data.local.NoteDao
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    val allNotes: Flow<List<Note>>
    suspend fun insert(note: Note)
    suspend fun delete(note: Note)
    suspend fun update(note: Note)
}

class NoteRepositoryImpl(private val noteDao: NoteDao): NoteRepository {
    override val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    override suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    override suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    override suspend fun update(note: Note) {
        noteDao.update(note)
    }
}
