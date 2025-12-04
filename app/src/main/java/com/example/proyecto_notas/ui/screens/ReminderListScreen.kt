package com.example.proyecto_notas.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto_notas.data.local.Reminder
import com.example.proyecto_notas.ui.viewmodel.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    modifier: Modifier = Modifier,
    reminderViewModel: ReminderViewModel,
    onAddReminder: () -> Unit,
    onReminderClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val reminders by reminderViewModel.allReminders.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder) {
                Icon(Icons.Default.Add, contentDescription = "Agregar recordatorio")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.padding(paddingValues)
        ) {
            items(reminders) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onDeleteClick = { reminderViewModel.delete(reminder) },
                    onReminderClick = { onReminderClick(reminder.id) },
                    onCompletedChange = { isCompleted ->
                        reminderViewModel.onCompletedChange(reminder, isCompleted)
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderItem(
    reminder: Reminder,
    onDeleteClick: () -> Unit,
    onReminderClick: () -> Unit,
    onCompletedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onReminderClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = onCompletedChange
            )
            Column(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
                Text(text = reminder.title, fontWeight = FontWeight.Bold)
                if (reminder.description.isNotEmpty()) {
                    Text(text = reminder.description)
                }
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar recordatorio")
            }
        }
    }
}
