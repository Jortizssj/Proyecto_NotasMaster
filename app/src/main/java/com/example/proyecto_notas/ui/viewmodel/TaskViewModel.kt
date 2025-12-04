package com.example.proyecto_notas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto_notas.data.local.Task
import com.example.proyecto_notas.data.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskUiState(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUris: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val isNewTask: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val tasks: StateFlow<List<Task>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allTasks
            } else {
                repository.searchTasks(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun addImages(uris: List<String>) {
        _uiState.update { it.copy(imageUris = it.imageUris + uris) }
    }

    fun onImageDelete(uri: String) {
        _uiState.update { currentState ->
            val updatedUris = currentState.imageUris.toMutableList().apply {
                remove(uri)
            }
            currentState.copy(imageUris = updatedUris)
        }
    }

    fun getTask(id: Int) {
        viewModelScope.launch {
            val task = repository.getTaskById(id)
            if (task != null) {
                _uiState.update {
                    it.copy(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        imageUris = task.imageUris,
                        isCompleted = task.isCompleted,
                        isNewTask = false
                    )
                }
            } else {
                _uiState.value = TaskUiState()
            }
        }
    }

    fun prepareNewTask() {
        _uiState.value = TaskUiState()
    }

    fun saveTask() {
        if (_uiState.value.title.isBlank() && _uiState.value.description.isBlank()) {
            return
        }
        viewModelScope.launch {
            val taskState = _uiState.value
            val task = Task(
                id = taskState.id,
                title = taskState.title,
                description = taskState.description,
                imageUris = taskState.imageUris,
                isCompleted = taskState.isCompleted
            )
            if (taskState.isNewTask) {
                repository.insert(task)
            } else {
                repository.update(task)
            }
        }
    }

    fun toggleTaskCompletion(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.update(task.copy(isCompleted = isCompleted))
        }
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}