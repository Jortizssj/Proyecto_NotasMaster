package com.example.proyecto_notas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val imageUris: List<String> = emptyList(),
    val isCompleted: Boolean = false
)
