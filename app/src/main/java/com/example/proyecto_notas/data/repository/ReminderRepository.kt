package com.example.proyecto_notas.data.repository

import com.example.proyecto_notas.data.local.Reminder
import com.example.proyecto_notas.data.local.ReminderDao
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    val allReminders: Flow<List<Reminder>>
    suspend fun insert(reminder: Reminder)
    suspend fun delete(reminder: Reminder)
    suspend fun update(reminder: Reminder)
    fun getReminderById(id: Long): Flow<Reminder?>
}

class ReminderRepositoryImpl(private val reminderDao: ReminderDao): ReminderRepository {
    override val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    override suspend fun insert(reminder: Reminder) {
        reminderDao.insert(reminder)
    }

    override suspend fun delete(reminder: Reminder) {
        reminderDao.delete(reminder)
    }

    override suspend fun update(reminder: Reminder) {
        reminderDao.update(reminder)
    }

    override fun getReminderById(id: Long): Flow<Reminder?> {
        return reminderDao.getReminderById(id)
    }
}
