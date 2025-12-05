package com.example.proyecto_notas

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.core.content.FileProvider
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
import java.io.File

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    private val noteViewModel: NoteViewModel by viewModels { NoteViewModelFactory(Graph.noteRepository) }
    private val taskViewModel: TaskViewModel by viewModels { TaskViewModelFactory(Graph.taskRepository) }
    private val reminderViewModel: ReminderViewModel by viewModels { ReminderViewModelFactory(Graph.reminderRepository, Graph.reminderScheduler) }

    private var isPickingForNote: Boolean = true

    // region Notification Permissions
    private val requestNotificationPermissionLauncher = registerForActivityResult(
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
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
             onPermissionGranted()
        }
    }
    // endregion

    // region Media Launchers
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

    // For managing camera/mic permissions
    private var onCameraPermissionGranted: (() -> Unit)? = null
    private var onAudioPermissionGranted: (() -> Unit)? = null

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onCameraPermissionGranted?.invoke()
        } else {
            // Handle permission denial
        }
    }

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onAudioPermissionGranted?.invoke()
        } else {
            // Handle permission denial
        }
    }

    // For taking a picture
    private var latestTmpUri: Uri? = null
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                val uriString = uri.toString()
                if (isPickingForNote) {
                    noteViewModel.addImages(listOf(uriString))
                } else {
                    taskViewModel.addImages(listOf(uriString))
                }
            }
        }
    }

    // For recording a video
    private val recordVideoLauncher = registerForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                val uriString = uri.toString()
                if (isPickingForNote) {
                    noteViewModel.addImages(listOf(uriString))
                } else {
                    taskViewModel.addImages(listOf(uriString))
                }
            }
        }
    }

    // For recording audio
    private val recordAudioLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                val uriString = uri.toString()
                if (isPickingForNote) {
                    noteViewModel.addImages(listOf(uriString))
                } else {
                    taskViewModel.addImages(listOf(uriString))
                }
            }
        }
    }
    // endregion

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
                                },
                                onTakePhotoClick = { takePhoto(forNote = true) },
                                onRecordVideoClick = { recordVideo(forNote = true) },
                                onRecordAudioClick = { recordAudio(forNote = true) }
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
                                },
                                onTakePhotoClick = { takePhoto(forNote = false) },
                                onRecordVideoClick = { recordVideo(forNote = false) },
                                onRecordAudioClick = { recordAudio(forNote = false) }
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
            if (intent.action == "NAVIGATE_TO_REMINDER") {
                val reminderId = intent.getLongExtra("REMINDER_ID", -1L)
                if (reminderId != -1L) {
                    reminderViewModel.getReminder(reminderId)
                    navController.navigate("addReminder")
                }
            }
        }
    }

    private fun getTmpFileUri(extension: String): Uri {
        val tmpFile = File.createTempFile("tmp_media_file", ".$extension", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    private fun takePhoto(forNote: Boolean) {
        isPickingForNote = forNote
        onCameraPermissionGranted = {
            latestTmpUri = getTmpFileUri("jpg")
            latestTmpUri?.let { uri ->
                takePictureLauncher.launch(uri)
            }
        }
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> onCameraPermissionGranted?.invoke()
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun recordVideo(forNote: Boolean) {
        isPickingForNote = forNote
        onCameraPermissionGranted = {
            latestTmpUri = getTmpFileUri("mp4")
            latestTmpUri?.let { uri ->
                recordVideoLauncher.launch(uri)
            }
        }
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> onCameraPermissionGranted?.invoke()
            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun recordAudio(forNote: Boolean) {
        isPickingForNote = forNote
        onAudioPermissionGranted = {
            val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            recordAudioLauncher.launch(intent)
        }
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
            PackageManager.PERMISSION_GRANTED -> onAudioPermissionGranted?.invoke()
            else -> requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
