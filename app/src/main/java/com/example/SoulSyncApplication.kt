package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer

class SoulSyncApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        
        // Fail-fast verification of BuildConfig values at runtime (except during Robolectric unit tests)
        if (android.os.Build.FINGERPRINT != "robolectric") {
            validateAuthConfig()
        }
        
        container = DefaultAppContainer(this)
        
        // Initialize Notification Channels
        container.notificationManager
    }

    private fun validateAuthConfig() {
        val supabaseUrl = BuildConfig.SUPABASE_URL
        val supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY
        val googleClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        
        if (supabaseUrl.isBlank() || 
            supabaseUrl == "https://your-project.supabase.co" || 
            supabaseUrl.contains("placeholder") || 
            supabaseUrl.contains("your-project")
        ) {
            throw IllegalStateException("Invalid SUPABASE_URL configured: '$supabaseUrl'. Please configure the correct SUPABASE_URL in your secrets/.env.")
        }
        
        if (supabaseAnonKey.isBlank() || 
            supabaseAnonKey == "your-supabase-anon-key" || 
            supabaseAnonKey.contains("placeholder") || 
            supabaseAnonKey.contains("anon-key")
        ) {
            throw IllegalStateException("Invalid SUPABASE_ANON_KEY configured. Please configure the correct SUPABASE_ANON_KEY in your secrets/.env.")
        }
        
        if (googleClientId.isBlank() || 
            googleClientId == "your-google-web-client-id" || 
            googleClientId.contains("placeholder")
        ) {
            throw IllegalStateException("Invalid GOOGLE_WEB_CLIENT_ID configured: '$googleClientId'. Please configure the correct GOOGLE_WEB_CLIENT_ID in your secrets/.env.")
        }
    }

    fun resetContainer() {
        container = DefaultAppContainer(this)
    }
}
