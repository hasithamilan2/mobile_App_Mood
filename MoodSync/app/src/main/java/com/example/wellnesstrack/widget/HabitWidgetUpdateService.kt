package com.example.wellnesstrack.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.JobIntentService

class HabitWidgetUpdateService : Service() {
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Update the widget
        HabitWidgetProvider.updateWidget(this)
        
        // Schedule the next update
        scheduleNextUpdate()
        
        // Stop the service
        stopSelf()
        
        return START_NOT_STICKY
    }
    
    private fun scheduleNextUpdate() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, HabitWidgetProvider::class.java)
        intent.action = HabitWidgetProvider.ACTION_UPDATE_WIDGET
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, flags)
        
        // Update every 30 minutes (same as updatePeriodMillis in widget_info.xml)
        val updateInterval = 30 * 60 * 1000L // 30 minutes in milliseconds
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + updateInterval,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + updateInterval,
                pendingIntent
            )
        }
    }
    
    companion object {
        fun startUpdateService(context: Context) {
            val intent = Intent(context, HabitWidgetUpdateService::class.java)
            context.startService(intent)
        }
    }
}