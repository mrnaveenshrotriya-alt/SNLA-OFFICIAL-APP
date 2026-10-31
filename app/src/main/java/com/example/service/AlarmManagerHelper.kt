package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.TaxReminderEntity
import java.text.SimpleDateFormat
import java.util.*

object AlarmManagerHelper {

    fun scheduleReminderAlarm(context: Context, reminder: TaxReminderEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, TaxReminderAlarmReceiver::class.java).apply {
            putExtra(TaxReminderAlarmReceiver.EXTRA_TITLE, "${reminder.returnType} Due Reminder")
            putExtra(TaxReminderAlarmReceiver.EXTRA_MESSAGE, "Return for ${reminder.taxPeriod} is due on ${reminder.dueDate}")
            putExtra(TaxReminderAlarmReceiver.EXTRA_CLIENT_NAME, reminder.clientName)
            putExtra(TaxReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(TaxReminderAlarmReceiver.EXTRA_RING_ALARM, reminder.isAlarmRinging)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate trigger epoch
        val triggerTime = parseReminderEpoch(reminder.dueDate, reminder.reminderTime, reminder.reminderType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun triggerTestAlarm(context: Context, clientName: String, returnType: String) {
        val intent = Intent(context, TaxReminderAlarmReceiver::class.java).apply {
            putExtra(TaxReminderAlarmReceiver.EXTRA_TITLE, "Test Alarm: $returnType Due")
            putExtra(TaxReminderAlarmReceiver.EXTRA_MESSAGE, "Urgent: Please file $returnType for $clientName immediately!")
            putExtra(TaxReminderAlarmReceiver.EXTRA_CLIENT_NAME, clientName)
            putExtra(TaxReminderAlarmReceiver.EXTRA_REMINDER_ID, System.currentTimeMillis())
            putExtra(TaxReminderAlarmReceiver.EXTRA_RING_ALARM, true)
        }
        context.sendBroadcast(intent)
    }

    fun snoozeAlarm(context: Context, reminderId: Long, snoozeMinutes: Int = 10) {
        TaxReminderAlarmReceiver.stopActiveAlarm()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaxReminderAlarmReceiver::class.java).apply {
            putExtra(TaxReminderAlarmReceiver.EXTRA_TITLE, "Snoozed Tax Due Alarm")
            putExtra(TaxReminderAlarmReceiver.EXTRA_MESSAGE, "Snoozed reminder is ringing again. Action required!")
            putExtra(TaxReminderAlarmReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(TaxReminderAlarmReceiver.EXTRA_RING_ALARM, true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TaxReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        TaxReminderAlarmReceiver.stopActiveAlarm()
    }

    private fun parseReminderEpoch(dueDateStr: String, timeStr: String, reminderType: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val fullDateStr = "$dueDateStr $timeStr"
        val calendar = Calendar.getInstance()
        try {
            val date = sdf.parse(fullDateStr)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            return System.currentTimeMillis() + 60_000
        }

        when (reminderType) {
            "15_DAYS" -> calendar.add(Calendar.DAY_OF_YEAR, -15)
            "7_DAYS" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "3_DAYS" -> calendar.add(Calendar.DAY_OF_YEAR, -3)
            "1_DAY" -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            "ON_DUE_DATE" -> {} // On date
            "DAILY_AFTER" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val computed = calendar.timeInMillis
        return if (computed <= System.currentTimeMillis()) {
            System.currentTimeMillis() + 10_000 // if past, fire in 10s
        } else {
            computed
        }
    }
}
