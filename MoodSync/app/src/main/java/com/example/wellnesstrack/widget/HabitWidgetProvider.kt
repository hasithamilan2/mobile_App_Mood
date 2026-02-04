package com.example.wellnesstrack.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.wellnesstrack.activities.MainActivity
import com.example.wellnesstrack.R
import com.example.wellnesstrack.repository.HabitRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.wellnesstrack.ACTION_UPDATE_WIDGET"
        
        fun updateWidget(context: Context) {
            val intent = Intent(context, HabitWidgetProvider::class.java)
            intent.action = ACTION_UPDATE_WIDGET
            context.sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, HabitWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(
        context: Context, 
        appWidgetManager: AppWidgetManager, 
        appWidgetIds: IntArray
    ) {
        // Update each widget
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Create RemoteViews for our widget layout
        val views = RemoteViews(context.packageName, R.layout.widget_habit_progress)
        
        // Get habit completion data from repository
        val repository = HabitRepository(context)
        val (completed, total) = repository.getTodayCompletionStats()
        
        // Calculate percentage
        val percentage = if (total > 0) (completed * 100) / total else 0
        
        // Update views
        views.setTextViewText(R.id.tvCompletionPercentage, "$percentage%")
        views.setTextViewText(R.id.tvHabitCount, "$completed/$total completed")
        views.setProgressBar(R.id.circularProgressBar, 100, percentage, false)
        
        // Set last updated time
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        views.setTextViewText(R.id.tvLastUpdated, "Last updated: $currentTime")
        
        // Set up click intent to open the app
        val intent = Intent(context, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
        views.setOnClickPendingIntent(R.id.tvCompletionPercentage, pendingIntent)
        
        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}