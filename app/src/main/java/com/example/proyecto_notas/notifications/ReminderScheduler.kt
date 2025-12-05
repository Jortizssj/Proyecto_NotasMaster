package com.example.proyecto_notas.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.proyecto_notas.data.local.Reminder

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        reminder.reminderDates.forEachIndexed { index, date ->
            val requestCode = (reminder.id.toInt() * 1000) + index

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("REMINDER_ID", reminder.id.toInt())
                putExtra("REMINDER_TITLE", reminder.title)
                putExtra("REQUEST_CODE", requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (date > System.currentTimeMillis()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    date,
                    pendingIntent
                )
            }
        }
    }

    fun cancel(reminder: Reminder) {
        reminder.reminderDates.forEachIndexed { index, _ ->
            val requestCode = (reminder.id.toInt() * 1000) + index
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
