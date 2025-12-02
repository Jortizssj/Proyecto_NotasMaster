package com.example.proyecto_notas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.proyecto_notas.data.local.Note
import com.example.proyecto_notas.data.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la pantalla de edición de notas.
 */
data class NoteUiState(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUris: List<String> = emptyList(),
    val isNewNote: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    // Flujo con todas las notas para la pantalla principal
    val allNotes: StateFlow<List<Note>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estado de la UI para la pantalla de añadir/editar
    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    /**
     * Carga una nota existente en el estado de la UI para su edición.
     */
    fun getNote(id: Int) {
        viewModelScope.launch {
            repository.getNoteById(id).collect { note ->
                if (note != null) {
                    _uiState.value = NoteUiState(
                        id = note.id,
                        title = note.title,
                        description = note.description,
                        imageUris = note.imageUris,
                        isNewNote = false
                    )
                }
            }
        }
    }

    /**
     * Prepara el estado de la UI para crear una nota nueva.
     */
    fun prepareNewNote() {
        _uiState.value = NoteUiState()
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
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

    /**
     * Guarda la nota actual, ya sea insertando una nueva o actualizando una existente.
     */
    fun saveNote() {
        viewModelScope.launch {
            val currentUiState = _uiState.value
            val note = Note(
                id = if (currentUiState.isNewNote) 0 else currentUiState.id,
                title = currentUiState.title,
                description = currentUiState.description,
                imageUris = currentUiState.imageUris
            )
            if (currentUiState.isNewNote) {
                repository.insert(note)
            } else {
                repository.update(note)
            }
        }
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}