package com.example.proyecto_notas.data.repository

import com.example.proyecto_notas.data.local.Task
import com.example.proyecto_notas.data.local.TaskDao
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    val allTasks: Flow<List<Task>>
    suspend fun insert(task: Task)
    suspend fun delete(task: Task)
    suspend fun update(task: Task)
    suspend fun getTaskById(id: Int): Task?
}

class TaskRepositoryImpl(private val taskDao: TaskDao) : TaskRepository {
    override val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    override suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    override suspend fun delete(task: Task) {
        taskDao.delete(task)
    }

    override suspend fun update(task: Task) {
        taskDao.update(task)
    }

    override suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }
}
