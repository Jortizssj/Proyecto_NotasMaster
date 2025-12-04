package com.example.proyecto_notas.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val reminderDate: Long,
    val isCompleted: Boolean = false
)
