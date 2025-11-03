package com.example.proyecto_notas.ui.screens

import androidx.compose.foundation.border
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.proyecto_notas.NoteContentType
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
fun NoteTypeScreen(navController: NavController,windowSize: WindowWidthSizeClass,modifier: Modifier = Modifier) {
    val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModelFactory(Graph.noteRepository))
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(Graph.taskRepository))

    val notes by noteViewModel.allNotes.collectAsState()
    val tasks by taskViewModel.allTasks.collectAsState()

    val contentType = when (windowSize) {
        WindowWidthSizeClass.Expanded -> NoteContentType.LIST_AND_DETAIL
        else -> NoteContentType.LIST_ONLY
    }

    DisposableEffect(contentType) {
        if (contentType == NoteContentType.LIST_ONLY) {
            // Limpia la selección cuando volvemos a la vista de solo lista
            noteViewModel.clearSelection()
        }
        onDispose {}
    }
    if (contentType == NoteContentType.LIST_AND_DETAIL) {
        // LISTA Y DETALLES (Para pantallas expandidas)
        ListAndDetailLayout(
            notes = notes,
            tasks = tasks,
            noteViewModel = noteViewModel,
            taskViewModel = taskViewModel,
            navController = navController
        )
    } else {
        // SOLO LISTA (Para teléfonos)
        ListOnlyLayout(
            navController = navController,
            notes = notes,
            tasks = tasks,
            noteViewModel = noteViewModel,
            taskViewModel = taskViewModel,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOnlyLayout(
    navController: NavController,
    notes: List<Note>,
    tasks: List<Task>,
    noteViewModel: NoteViewModel,
    taskViewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
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
fun ListAndDetailLayout(
    notes: List<Note>,
    tasks: List<Task>,
    noteViewModel: NoteViewModel,
    taskViewModel: TaskViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // 1. Observa el estado reactivo de la nota seleccionada
    // Usaremos 'val note = selectedNote' dentro del bloque 'if' para un manejo seguro de nulos.
    val selectedNote by noteViewModel.selectedNote.collectAsState()

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .border(width = 1.dp, color = Color.LightGray)
                .padding(16.dp)
        ) {
            // Botón para crear nueva nota
            Button(onClick = {
                noteViewModel.clearSelection() // Limpia la selección al navegar fuera
                navController.navigate("addNote/0")
            }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.add_note_button), maxLines = 1)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título de la sección de Notas
            Text("Notas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Notas (Usamos LazyColumn para eficiencia)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(notes) { note ->
                    NoteItem(
                        note = note,
                        onDeleteClick = { noteViewModel.delete(note) },
                        onNoteClick = { noteEdit ->
                            noteViewModel.setSelectedNoteId(noteEdit.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título de la sección de Tareas (Ejemplo: podrías usar otra lista aquí)
            Text("Tareas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            // ... Aquí iría el LazyColumn de Tareas si lo deseas...
        }
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            selectedNote?.let { note ->
                // Si selectedNote NO es nulo, se le asigna el nombre 'note'
                NoteDetailView(note = note, navController = navController)
            } ?: run {
                // Muestra este mensaje si selectedNote ES nulo
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "👋 Selecciona una nota para ver los detalles aquí.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun NoteDetailView(
    note: Note,
    navController: NavController, // Necesario para la navegación de Edición
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Permite el scroll para notas largas
    ) {
        // Título de la Nota
        Text(
            text = note.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Espacio
        Spacer(modifier = Modifier.height(16.dp))

        // Descripción de la Nota
        Text(
            text = note.description,
            style = MaterialTheme.typography.bodyLarge
        )

        // Alinea el botón de edición al final
        Spacer(modifier = Modifier.weight(1f))

        // Botón de Edición (Usa el NavController)
        Button(
            onClick = { navController.navigate("addNote/${note.id}") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar Nota")
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
