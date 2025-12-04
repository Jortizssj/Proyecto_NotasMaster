package com.example.proyecto_notas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto_notas.data.local.Reminder
import com.example.proyecto_notas.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReminderUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val reminderDate: Long = 0L,
    val isCompleted: Boolean = false,
    val isNewReminder: Boolean = true
)

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    val allReminders: StateFlow<List<Reminder>> = repository.allReminders.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    fun getReminder(id: Long) {
        viewModelScope.launch {
            repository.getReminderById(id).collect { reminder ->
                if (reminder != null) {
                    _uiState.value = ReminderUiState(
                        id = reminder.id,
                        title = reminder.title,
                        description = reminder.description,
                        reminderDate = reminder.reminderDate,
                        isCompleted = reminder.isCompleted,
                        isNewReminder = false
                    )
                }
            }
        }
    }

    fun prepareNewReminder() {
        _uiState.value = ReminderUiState()
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }



    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onDateChange(newDate: Long) {
        _uiState.update { it.copy(reminderDate = newDate) }
    }

    fun onCompletedChange(reminder: Reminder, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.update(reminder.copy(isCompleted = isCompleted))
        }
    }

    fun saveReminder() {
        viewModelScope.launch {
            val currentUiState = _uiState.value
            val reminder = Reminder(
                id = if (currentUiState.isNewReminder) 0 else currentUiState.id,
                title = currentUiState.title,
                description = currentUiState.description,
                reminderDate = currentUiState.reminderDate,
                isCompleted = currentUiState.isCompleted
            )
            if (currentUiState.isNewReminder) {
                repository.insert(reminder)
            } else {
                repository.update(reminder)
            }
        }
    }

    fun delete(reminder: Reminder) = viewModelScope.launch {
        repository.delete(reminder)
    }
}

class ReminderViewModelFactory(private val repository: ReminderRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
