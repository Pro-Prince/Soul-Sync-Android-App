package com.example.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.navigation.NavController
import com.example.MainActivity

object NotificationDeepLinkHandler {
    const val EXTRA_ROUTE = "notification_route"
    private const val TAG = "DeepLinkHandler"

    fun createPendingIntent(context: Context, route: String, notificationId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROUTE, route)
        }
        
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, notificationId, intent, flags)
    }

    fun handleIntent(intent: Intent?, navController: NavController?) {
        if (intent == null || navController == null) return
        val route = intent.getStringExtra(EXTRA_ROUTE) ?: return
        Log.d(TAG, "Handling deep-link notification intent. Route destination: $route")
        try {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            // Remove the extra so it's not processed multiple times on activity recreation
            intent.removeExtra(EXTRA_ROUTE)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing deep-link navigation for route: $route", e)
        }
    }
}
