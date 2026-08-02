package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.SoulSyncApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED ||
            intent.action == Intent.ACTION_TIME_CHANGED) {
            val app = context.applicationContext as? SoulSyncApplication ?: return
            val container = app.container
            val preferencesRepository = container.notificationPreferencesRepository
            val scheduler = container.notificationScheduler
            CoroutineScope(Dispatchers.IO).launch {
                val prefs = preferencesRepository.getPreferences()
                scheduler.scheduleAllReminders(prefs)
            }
        }
    }
}
