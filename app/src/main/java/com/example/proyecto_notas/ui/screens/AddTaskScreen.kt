package com.example.proyecto_notas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.proyecto_notas.R
import com.example.proyecto_notas.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    taskViewModel: TaskViewModel,
    onNavigateUp: () -> Unit,
    onAddImagesClick: () -> Unit,
    onMediaClick: (String) -> Unit,
    onTakePhotoClick: () -> Unit,
    onRecordVideoClick: () -> Unit,
    onRecordAudioClick: () -> Unit
) {
    val uiState by taskViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_task_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        taskViewModel.saveTask()
                        onNavigateUp()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_task_button_description))
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = onAddImagesClick) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add from Gallery")
                    }
                    IconButton(onClick = onTakePhotoClick) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Take Photo")
                    }
                    IconButton(onClick = onRecordVideoClick) {
                        Icon(Icons.Default.Videocam, contentDescription = "Record Video")
                    }
                    IconButton(onClick = onRecordAudioClick) {
                        Icon(Icons.Default.Mic, contentDescription = "Record Audio")
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { taskViewModel.onTitleChange(it) },
                label = { Text(stringResource(R.string.title_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { taskViewModel.onDescriptionChange(it) },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.imageUris) { uri ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .clickable { onMediaClick(uri) },
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_launcher_background),
                        )
                        IconButton(onClick = { taskViewModel.onImageDelete(uri) }) {
                            Icon(Icons.Default.Cancel, contentDescription = stringResource(R.string.delete_image_button_description))
                        }
                    }
                }
            }
        }
    }
}