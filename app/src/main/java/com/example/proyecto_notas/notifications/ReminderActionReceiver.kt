package com.example.proyecto_notas.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.proyecto_notas.di.Graph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Graph.provide(context)
        val reminderRepository = Graph.reminderRepository

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, 0)
                if (reminderId == 0) return@launch

                val reminder = reminderRepository.getReminder(reminderId)

                if (reminder != null) {
                    when (intent.action) {
                        ACTION_COMPLETE -> {
                            val updatedReminder = reminder.copy(isCompleted = true)
                            reminderRepository.update(updatedReminder)
                        }
                    }
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(reminderId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.example.proyecto_notas.ACTION_COMPLETE"
        const val EXTRA_REMINDER_ID = "EXTRA_REMINDER_ID"
    }
}
