package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class TaxReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "tax_due_alarms_channel"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_CLIENT_NAME = "extra_client_name"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_RING_ALARM = "extra_ring_alarm"

        var currentRingtone: Ringtone? = null

        fun stopActiveAlarm() {
            try {
                currentRingtone?.stop()
                currentRingtone = null
            } catch (ignored: Exception) {}
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Tax Return Due Alarm"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Compliance deadline approaching!"
        val clientName = intent.getStringExtra(EXTRA_CLIENT_NAME) ?: "Client"
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, System.currentTimeMillis())
        val ringAlarm = intent.getBooleanExtra(EXTRA_RING_ALARM, true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel with HIGH importance and sound
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttr = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tax Compliance Due Date Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority ringing reminders for GST & Income Tax deadlines"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(alarmSound, audioAttr)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Ring audio if requested
        if (ringAlarm) {
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, alarmUri)
                currentRingtone?.stop()
                currentRingtone = ringtone
                ringtone?.play()

                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Tap to open app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "REMINDERS")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss / Stop Alarm Intent
        val stopAlarmIntent = Intent(context, TaxReminderAlarmReceiver::class.java).apply {
            action = "ACTION_STOP_ALARM"
        }
        val stopAlarmPendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + 999).toInt(),
            stopAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (intent.action == "ACTION_STOP_ALARM") {
            stopActiveAlarm()
            notificationManager.cancel(reminderId.toInt())
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⚖️ $title")
            .setContentText("$clientName: $message")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Client: $clientName\n$message\nShree Navya Lekha Associates"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Alarm", stopAlarmPendingIntent)
            .build()

        notificationManager.notify(reminderId.toInt(), notification)
    }
}
