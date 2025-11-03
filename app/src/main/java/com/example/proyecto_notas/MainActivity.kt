package com.example.proyecto_notas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.provide(this)
        enableEdgeToEdge()
        setContent {
            Proyecto_notasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "noteType",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("noteType") {
                            NoteTypeScreen(navController = navController)
                        }
                        composable(
                            route = "addNote/{noteId}",
                            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                            AddNoteScreen(
                                navController = navController,
                                noteId = noteId
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
