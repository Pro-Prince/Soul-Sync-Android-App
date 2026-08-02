package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.data.SettingsRepository
import com.example.data.dataStore
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.theme.SoulSyncTheme
import kotlinx.coroutines.launch
import androidx.activity.enableEdgeToEdge
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class AuthActivity : ComponentActivity() {
    private lateinit var settingsRepo: SettingsRepository
    val snackbarHostState = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as SoulSyncApplication).container
        settingsRepo = appContainer.settingsRepository
        
        setContent {
            val isDarkMode by settingsRepo.darkMode.collectAsState(initial = false)
            val themeName by settingsRepo.colorTheme.collectAsState(initial = "SOUL_PINK")

            SoulSyncTheme(
                themeName = themeName,
                forceDark = false // Force Auth screens to light mode
            ) {
                AuthScreen(
                    appContainer = appContainer,
                    snackbarHostState = snackbarHostState,
                    onGoogleSignInClick = { launchSignIn() },
                    onAuthSuccess = { email, name, isSignUp -> onSignInSuccess(email, name, isSignUp) }
                )
            }
        }
    }

    private fun launchSignIn() {
        val credentialManager = CredentialManager.create(this)
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                Log.d("AUTH", "Initiating CredentialManager.getCredential. Google Web Client ID length: ${BuildConfig.GOOGLE_WEB_CLIENT_ID.length}")
                val result = credentialManager.getCredential(
                    context = this@AuthActivity,
                    request = request
                )
                Log.d("AUTH", "CredentialManager.getCredential succeeded. Result class: ${result.javaClass.name}")
                handleCredential(result)
            } catch (e: GetCredentialException) {
                Log.e("AUTH", "Google Sign-In failed with GetCredentialException: class=${e.javaClass.name}, message=${e.message}, type=${e.type}", e)
                snackbarHostState.showSnackbar("Google Sign-In failed: [${e.type}] ${e.message}. Please try again.")
            } catch (e: Exception) {
                Log.e("AUTH", "Unexpected error in launchSignIn: class=${e.javaClass.name}, message=${e.message}", e)
                snackbarHostState.showSnackbar("Unexpected Login Error: ${e.message}. Please try again.")
            }
        }
    }

    private fun handleCredential(result: GetCredentialResponse) {
        val credential = result.credential
        Log.d("AUTH", "handleCredential received credential type: ${credential.type}")
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val name = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: email.substringBefore("@")
                
                Log.d("AUTH", "Google ID Token credential parsed successfully. Email: $email, Name: $name, ID Token length: ${idToken?.length ?: 0}")
                
                if (idToken.isNullOrBlank()) {
                    Log.e("AUTH", "ID Token parsed from credential is null or empty.")
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar("Failed to retrieve Google security token. Please try again.")
                    }
                    return
                }

                lifecycleScope.launch {
                    val appContainer = (application as SoulSyncApplication).container
                    val authResult = appContainer.authRepository.signInWithGoogleIdToken(idToken, email, name)
                    when (authResult) {
                        is com.example.data.repository.SignInResult.Success -> {
                            Log.d("AUTH", "Successfully logged in with Google ID Token.")
                            onSignInSuccess(email, name)
                        }
                        is com.example.data.repository.SignInResult.Error -> {
                            Log.e("AUTH", "Supabase sign-in reported error: ${authResult.message}")
                            snackbarHostState.showSnackbar(authResult.message)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AUTH", "Failed to parse Google ID Token credential details: class=${e.javaClass.name}, message=${e.message}", e)
                lifecycleScope.launch {
                    snackbarHostState.showSnackbar("Failed to process Google Account details: ${e.message}")
                }
            }
        } else {
            Log.e("AUTH", "Unexpected credential type returned from CredentialManager: ${credential.type}")
            lifecycleScope.launch {
                snackbarHostState.showSnackbar("Unsupported login credential type: ${credential.type}")
            }
        }
    }

    private fun onSignInSuccess(email: String, name: String, isSignUp: Boolean = false) {
        lifecycleScope.launch {
            val userId = com.example.data.supabase.SupabaseClient.auth.currentUserOrNull()?.id ?: ""
            if (userId.isNotBlank()) {
                settingsRepo.setUserInfo(userId, email, name)
            } else {
                settingsRepo.saveUserEmail(email)
                settingsRepo.saveUserName(name)
            }
            settingsRepo.setOnboardingComplete(true) // Mark complete to be safe

            (application as SoulSyncApplication).resetContainer()
            val newAppContainer = (application as SoulSyncApplication).container

            try {
                if (userId.isNotBlank()) {
                    newAppContainer.authRepository.registerOrUpdateExternalUser(userId, email, name)
                }
            } catch (e: Exception) {
                Log.e("AUTH", "Failed to register/sync external user in DB", e)
            }
            if (isSignUp) {
                android.widget.Toast.makeText(this@AuthActivity, "Welcome", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this@AuthActivity, "Welcome Back", android.widget.Toast.LENGTH_SHORT).show()
            }
            val dest = MainActivity::class.java
            startActivity(Intent(this@AuthActivity, dest))
            finish()
        }
    }
}
