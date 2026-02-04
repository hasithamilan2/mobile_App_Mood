package com.example.wellnesstrack.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.ExistingWorkPolicy
import androidx.work.Data
import com.example.wellnesstrack.activities.MainActivity
import com.example.wellnesstrack.R
import com.example.wellnesstrack.fragments.HydrationFragment
import java.util.concurrent.TimeUnit

class HydrationReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val intervalMinutes = inputData.getInt("intervalMinutes", 0)
        
        // Show a notification
        showNotification(
            inputData.getString("title") ?: "Hydration Reminder",
            inputData.getString("message") ?: "Time to drink water! Stay hydrated."
        )
        
        // Reschedule if interval is less than 15 minutes
        if (intervalMinutes in 1..14) {
            val inputData = Data.Builder()
                .putString("title", inputData.getString("title"))
                .putString("message", inputData.getString("message"))
                .putInt("intervalMinutes", intervalMinutes)
                .build()

            val reminderRequest = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
                .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                HydrationFragment.HYDRATION_REMINDER_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
        }

        return Result.success()
    }
    
    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android 8.0 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water throughout the day"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation_destination", R.id.navigation_hydration)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_hydration)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "hydration_channel"
        private const val NOTIFICATION_ID = 1
    }
}