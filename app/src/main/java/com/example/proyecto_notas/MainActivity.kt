package com.example.proyecto_notas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_notas.di.Graph
import com.example.proyecto_notas.ui.screens.AddNoteScreen
import com.example.proyecto_notas.ui.screens.AddTaskScreen
import com.example.proyecto_notas.ui.screens.NoteTypeScreen
import com.example.proyecto_notas.ui.theme.Proyecto_notasTheme
import com.example.proyecto_notas.ui.viewmodel.NoteViewModel
import com.example.proyecto_notas.ui.viewmodel.NoteViewModelFactory

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    private val noteViewModel: NoteViewModel by viewModels { NoteViewModelFactory(Graph.noteRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.provide(this)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                // Persistir permisos para las URIs
                val persistedUris = uris.map { uri ->
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    uri.toString()
                }
                noteViewModel.addImages(persistedUris)
            }
        }

        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)
            val windowWidthSizeClass = windowSizeClass.widthSizeClass
            Proyecto_notasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "noteType",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("noteType") {
                            NoteTypeScreen(
                                onAddNoteClick = {
                                    noteViewModel.prepareNewNote()
                                    navController.navigate("addNote")
                                },
                                onNoteClick = { noteId ->
                                    noteViewModel.getNote(noteId)
                                    navController.navigate("addNote")
                                },
                                noteViewModel = noteViewModel,
                                windowSize = windowWidthSizeClass
                            )
                        }
                        composable("addNote") {
                            AddNoteScreen(
                                noteViewModel = noteViewModel,
                                onNavigateUp = { navController.navigateUp() },
                                onAddImagesClick = {
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                        }
                        composable(
                            route = "addTask/{taskId}",
                            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                        ) {
                            val taskId = it.arguments?.getInt("taskId") ?: 0
                            AddTaskScreen(navController = navController, taskId = taskId)
                        }
                    }
                }
            }
        }
    }
}