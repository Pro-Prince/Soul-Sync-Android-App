package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.MainActivity
import com.example.data.SettingsRepository
import com.example.data.dataStore
import kotlinx.coroutines.runBlocking

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val settingsRepo = SettingsRepository(applicationContext.dataStore)
        
        val (email, userId, onboardingDone) = runBlocking {
            Triple(settingsRepo.getUserEmail(), settingsRepo.getUserId(), settingsRepo.isOnboardingComplete())
        }
        
        val dest = when {
            email.isNullOrEmpty() && userId.isNullOrEmpty() -> AuthActivity::class.java
            else -> MainActivity::class.java
        }
        
        startActivity(Intent(this, dest))
        finish()
    }
}
