package com.example.proyecto_notas.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.proyecto_notas.NoteApp
import com.example.proyecto_notas.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Recordatorio"
        val reminderId = intent.getIntExtra("REMINDER_ID", 0)

        // No mostrar notificación si el ID no es válido
        if (reminderId == 0) return

        // --- Acción: Marcar como completado ---
        val completeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_COMPLETE
            putExtra(ReminderActionReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId, // Usamos el ID del recordatorio como código de solicitud único
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NoteApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener este drawable
            .setContentTitle(reminderTitle)
            .setContentText("¡Es hora de tu recordatorio!")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta para que aparezca como notificación emergente
            .setAutoCancel(true) // La notificación se cierra al pulsarla
            .addAction(android.R.drawable.checkbox_on_background, "Completado", completePendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(reminderId, notification)
    }
}
