package com.example.proyecto_notas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Switch
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
import com.example.proyecto_notas.data.local.Task
import com.example.proyecto_notas.di.Graph
import com.example.proyecto_notas.ui.viewmodel.TaskViewModel
import com.example.proyecto_notas.ui.viewmodel.TaskViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController, taskId: Int, modifier: Modifier = Modifier) {
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(Graph.taskRepository)
    )
    val tasks by viewModel.allTasks.collectAsState()

    val taskToEdit = if (taskId == 0) null else tasks.find { it.id == taskId }

    var title by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(taskToEdit) {
        if (taskToEdit != null) {
            title = taskToEdit.title
            isCompleted = taskToEdit.isCompleted
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (taskId == 0) R.string.add_task_title else R.string.edit_task_title)) },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(stringResource(R.string.completed_task_label))
                Switch(checked = isCompleted, onCheckedChange = {isCompleted = it})
            }

            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

            Button(onClick = {
                val task = taskToEdit?.copy(title = title, isCompleted = isCompleted)
                    ?: Task(title = title, isCompleted = isCompleted)

                if (taskId == 0) {
                    viewModel.insert(task)
                } else {
                    viewModel.update(task)
                }
                navController.navigateUp()
            }) {
                Text(stringResource(R.string.save_task_button))
            }
        }
    }
}
