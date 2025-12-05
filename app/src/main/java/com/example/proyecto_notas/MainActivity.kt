package com.example.proyecto_notas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_notas.di.Graph
import com.example.proyecto_notas.ui.screens.AddNoteScreen
import com.example.proyecto_notas.ui.screens.AddTaskScreen
import com.example.proyecto_notas.ui.screens.MediaViewerScreen
import com.example.proyecto_notas.ui.screens.NoteTypeScreen
import com.example.proyecto_notas.ui.screens.AddReminderScreen
import com.example.proyecto_notas.ui.screens.ReminderListScreen
import com.example.proyecto_notas.ui.theme.Proyecto_notasTheme
import com.example.proyecto_notas.ui.viewmodel.NoteViewModel
import com.example.proyecto_notas.ui.viewmodel.NoteViewModelFactory
import com.example.proyecto_notas.ui.viewmodel.ReminderViewModel
import com.example.proyecto_notas.ui.viewmodel.ReminderViewModelFactory
import com.example.proyecto_notas.ui.viewmodel.TaskViewModel
import com.example.proyecto_notas.ui.viewmodel.TaskViewModelFactory

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    private val noteViewModel: NoteViewModel by viewModels { NoteViewModelFactory(Graph.noteRepository) }
    private val taskViewModel: TaskViewModel by viewModels { TaskViewModelFactory(Graph.taskRepository) }
    private val reminderViewModel: ReminderViewModel by viewModels { ReminderViewModelFactory(Graph.reminderRepository, Graph.reminderScheduler) }

    private var isPickingForNote: Boolean = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. You can now schedule notifications.
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied.
        }
    }

    private fun askNotificationPermission(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
             onPermissionGranted()
        }
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            val persistedUris = uris.map { uri ->
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                uri.toString()
            }
            if (isPickingForNote) {
                noteViewModel.addImages(persistedUris)
            } else {
                taskViewModel.addImages(persistedUris)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.provide(this)

        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)
            val windowWidthSizeClass = windowSizeClass.widthSizeClass

            Proyecto_notasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()

                    handleIntent(navController = navController, intent = intent)

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
                                onAddTaskClick = {
                                    taskViewModel.prepareNewTask()
                                    navController.navigate("addTask")
                                },
                                onTaskClick = { taskId ->
                                    taskViewModel.getTask(taskId)
                                    navController.navigate("addTask")
                                },
                                onRemindersClick = { 
                                    askNotificationPermission {
                                        navController.navigate("reminderList")
                                    }
                                },
                                noteViewModel = noteViewModel,
                                taskViewModel = taskViewModel,
                                windowSize = windowWidthSizeClass
                            )
                        }
                        composable("addNote") {
                            AddNoteScreen(
                                noteViewModel = noteViewModel,
                                onNavigateUp = { navController.navigateUp() },
                                onAddImagesClick = {
                                    isPickingForNote = true
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                },
                                onMediaClick = {
                                    navController.navigate("mediaViewer/${Uri.encode(it)}")
                                }
                            )
                        }
                        composable("addTask") {
                            AddTaskScreen(
                                taskViewModel = taskViewModel,
                                onNavigateUp = { navController.navigateUp() },
                                onAddImagesClick = {
                                    isPickingForNote = false
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                },
                                onMediaClick = {
                                    navController.navigate("mediaViewer/${Uri.encode(it)}")
                                }
                            )
                        }
                        composable("reminderList") {
                            ReminderListScreen(
                                reminderViewModel = reminderViewModel,
                                onAddReminder = {
                                    reminderViewModel.prepareNewReminder()
                                    navController.navigate("addReminder")
                                },
                                onReminderClick = { reminderId ->
                                    reminderViewModel.getReminder(reminderId)
                                    navController.navigate("addReminder")
                                },
                                onBack = { navController.navigateUp() }
                            )
                        }
                        composable("addReminder") {
                            AddReminderScreen(
                                reminderViewModel = reminderViewModel,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                        composable(
                            "mediaViewer/{uri}",
                            arguments = listOf(navArgument("uri") { type = NavType.StringType })
                        ) {
                            val uri = it.arguments?.getString("uri") ?: ""
                            MediaViewerScreen(
                                uri = uri,
                                onBack = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    @Composable
    private fun handleIntent(navController: NavController, intent: Intent) {
        LaunchedEffect(intent) {
            if (intent.hasExtra("REMINDER_ID_EXTRA")) {
                val reminderId = intent.getIntExtra("REMINDER_ID_EXTRA", 0)
                if (reminderId != 0) {
                    reminderViewModel.getReminder(reminderId.toLong())
                    navController.navigate("addReminder")
                    intent.removeExtra("REMINDER_ID_EXTRA")
                }
            }
        }
    }
}
