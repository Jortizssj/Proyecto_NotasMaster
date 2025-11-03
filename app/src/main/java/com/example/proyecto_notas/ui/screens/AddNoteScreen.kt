package com.example.proyecto_notas.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto_notas.R
import com.example.proyecto_notas.data.local.Note
import com.example.proyecto_notas.di.Graph
import com.example.proyecto_notas.ui.viewmodel.NoteViewModel
import com.example.proyecto_notas.ui.viewmodel.NoteViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    navController: NavController,
    noteId: Long,
    modifier: Modifier = Modifier
) {
    val viewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(Graph.noteRepository))
    val notes by viewModel.allNotes.collectAsState()

    // Buscamos la nota a editar. Será null si es una nota nueva (noteId = 0L)
    val noteToEdit = remember(noteId, notes) {
        if (noteId == 0L) null else notes.find { it.id == noteId.toInt() }
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Rellenamos los campos si estamos editando una nota
    LaunchedEffect(noteToEdit) {
        if (noteToEdit != null) {
            title = noteToEdit.title
            description = noteToEdit.description
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleRes = if (noteToEdit == null) R.string.add_note_title else R.string.edit_note_title
                    Text(stringResource(titleRes))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (noteToEdit != null) {
                    // Actualizar nota existente
                    viewModel.update(noteToEdit.copy(title = title, description = description))
                } else {
                    // Insertar nota nueva
                    viewModel.insert(Note(title = title, description = description))
                }
                navController.navigateUp()
            }) {
                Text(stringResource(R.string.save_note_button))
            }
        }
    }
}
