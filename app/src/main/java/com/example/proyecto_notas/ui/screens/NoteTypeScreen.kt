package com.example.proyecto_notas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto_notas.R
import com.example.proyecto_notas.data.local.Note
import com.example.proyecto_notas.data.local.Task
import com.example.proyecto_notas.di.Graph
import com.example.proyecto_notas.ui.viewmodel.NoteViewModel
import com.example.proyecto_notas.ui.viewmodel.NoteViewModelFactory
import com.example.proyecto_notas.ui.viewmodel.TaskViewModel
import com.example.proyecto_notas.ui.viewmodel.TaskViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTypeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(Graph.noteRepository))
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(Graph.taskRepository))

    val notes by noteViewModel.allNotes.collectAsState()
    val tasks by taskViewModel.allTasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Botones para añadir una nueva nota o tarea
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { navController.navigate("addNote/0") }) {
                    Text(stringResource(R.string.add_note_button))
                }
                Button(onClick = { navController.navigate("addTask/0") }) {
                    Text(stringResource(R.string.add_task_button))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de Notas
            Text("Notas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(notes) { note ->
                    NoteItem(
                        note = note,
                        onDeleteClick = { noteViewModel.delete(note) },
                        onNoteClick = { noteToEdit -> navController.navigate("addNote/${noteToEdit.id}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de Tareas
            Text("Tareas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onCheckedChange = { isChecked -> taskViewModel.update(task.copy(isCompleted = isChecked)) },
                        onDeleteClick = { taskViewModel.delete(task) },
                        onTaskClick = { taskToEdit -> navController.navigate("addTask/${taskToEdit.id}")}
                    )
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onDeleteClick: () -> Unit,
    onNoteClick: (Note) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onNoteClick(note) }
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                Text(text = note.title, fontWeight = FontWeight.Bold)
                if (note.description.isNotEmpty()) {
                    Text(text = note.description)
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Note")
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onTaskClick: (Task) -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .clickable { onTaskClick(task) }
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = task.title, modifier = Modifier.weight(1f))
            Checkbox(checked = task.isCompleted, onCheckedChange = onCheckedChange)
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Task")
            }
        }
    }
}
