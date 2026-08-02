package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.SettingsRepository
import com.example.data.dataStore
import com.example.data.local.SoulSyncDatabase
import com.example.data.local.entity.UserAccount
import com.example.data.supabase.SupabaseClient
import com.example.data.sync.SupabaseSyncHelper
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    private val TAG = "AuthRepository"
    private val settingsRepository = SettingsRepository(context.dataStore)

    suspend fun signUp(email: String, passwordKey: String, displayName: String): SignUpResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || passwordKey.isBlank()) {
            return@withContext SignUpResult.Error("Email and password cannot be empty.")
        }

        try {
            SupabaseClient.auth.signUpWith(Email) {
                this.email = normalizedEmail
                this.password = passwordKey
            }
            Log.d(TAG, "Successfully registered user with Supabase Auth: $normalizedEmail")
            
            val currentUser = SupabaseClient.auth.currentUserOrNull()
                ?: throw Exception("Could not retrieve registered user profile")
            val userId = currentUser.id
            val finalDisplayName = displayName.trim().ifBlank { normalizedEmail.substringBefore("@") }

            val database = SoulSyncDatabase.getDatabase(context, userId)
            val userAccountDao = database.userAccountDao()

            val newUser = UserAccount(
                id = userId,
                email = normalizedEmail,
                passwordKey = passwordKey,
                displayName = finalDisplayName
            )
            userAccountDao.insertUser(newUser)
            
            // Save info to DataStore
            settingsRepository.setUserInfo(userId, normalizedEmail, finalDisplayName)

            try {
                SupabaseSyncHelper.syncUserAccount(newUser)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync user account to Supabase on signup", e)
            }

            SignUpResult.Success(newUser)
        } catch (e: Exception) {
            Log.e(TAG, "Supabase Auth signUp failed", e)
            SignUpResult.Error(e.message ?: "Sign up failed on Supabase. Please try again.")
        }
    }

    suspend fun registerOrUpdateExternalUser(userId: String, email: String, displayName: String): UserAccount = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        val database = SoulSyncDatabase.getDatabase(context, userId)
        val userAccountDao = database.userAccountDao()
        val existing = userAccountDao.getUserById(userId)
        
        val finalUser = if (existing != null) {
            existing
        } else {
            val newUser = UserAccount(
                id = userId,
                email = normalizedEmail,
                passwordKey = "",
                displayName = displayName.trim().ifBlank { normalizedEmail.substringBefore("@") }
            )
            userAccountDao.insertUser(newUser)
            newUser
        }
        finalUser
    }

    suspend fun signInWithGoogleIdToken(idToken: String, email: String, displayName: String): SignInResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        try {
            Log.d(TAG, "Attempting Supabase Google OAuth login with email: $normalizedEmail, idToken length: ${idToken.length}")
            SupabaseClient.auth.signInWith(IDToken) {
                provider = Google
                this.idToken = idToken
            }
            
            val currentSession = SupabaseClient.auth.currentSessionOrNull()
            val accessTokenLength = currentSession?.accessToken?.length ?: 0
            val refreshTokenLength = currentSession?.refreshToken?.length ?: 0
            Log.d(TAG, "Supabase authentication call completed successfully. Session exists: ${currentSession != null}, accessToken length: $accessTokenLength, refreshToken length: $refreshTokenLength")
            
            val currentUser = SupabaseClient.auth.currentUserOrNull()
                ?: throw Exception("Could not retrieve Google authenticated user profile from Supabase session")
            
            Log.d(TAG, "Current Supabase Authenticated User: id=${currentUser.id}, email=${currentUser.email}, authState=${SupabaseClient.auth.sessionStatus.value}")
            
            val userId = currentUser.id
            val resolvedEmail = currentUser.email ?: normalizedEmail
            val resolvedName = currentUser.userMetadata?.get("full_name")?.toString() ?: displayName

            // Migrate local database from email to UUID if needed
            SoulSyncDatabase.migrateDatabaseIfNecessary(context, resolvedEmail, userId)

            val user = registerOrUpdateExternalUser(userId, resolvedEmail, resolvedName)
            
            // Save info to DataStore
            settingsRepository.setUserInfo(userId, resolvedEmail, user.displayName)

            val database = SoulSyncDatabase.getDatabase(context, userId)
            try {
                SupabaseSyncHelper.syncUserAccount(user)
                SupabaseSyncHelper.pullAllFromSupabase(userId, database)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync/pull after Google Sign-In: class=${e.javaClass.name}, message=${e.message}", e)
            }
            SignInResult.Success(user)
        } catch (e: io.github.jan.supabase.exceptions.RestException) {
            Log.e(TAG, "Supabase Google sign-in REST exception: details=${e.toString()}", e)
            SignInResult.Error("Supabase Google Auth API error: ${e.message ?: e.toString()}")
        } catch (e: io.github.jan.supabase.exceptions.HttpRequestException) {
            Log.e(TAG, "Supabase Google sign-in Network exception: details=${e.toString()}", e)
            SignInResult.Error("Network error during Google sign-in. Please check your internet connection: ${e.message ?: e.toString()}")
        } catch (e: Exception) {
            Log.e(TAG, "Supabase Google sign-in unexpected exception: class=${e.javaClass.name}, message=${e.message}, cause=${e.cause?.message}", e)
            SignInResult.Error("Google authentication failed on Supabase: ${e.message}")
        }
    }

    suspend fun signIn(email: String, passwordKey: String): SignInResult = withContext(Dispatchers.IO) {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank() || passwordKey.isBlank()) {
            return@withContext SignInResult.Error("Email and password cannot be empty.")
        }

        try {
            SupabaseClient.auth.signInWith(Email) {
                this.email = normalizedEmail
                this.password = passwordKey
            }
            Log.d(TAG, "Successfully authenticated with Supabase Auth: $normalizedEmail")
            
            val currentUser = SupabaseClient.auth.currentUserOrNull()
                ?: throw Exception("Could not retrieve authenticated user profile")
            val userId = currentUser.id
            val resolvedEmail = currentUser.email ?: normalizedEmail

            // Migrate local database from email to UUID if needed
            SoulSyncDatabase.migrateDatabaseIfNecessary(context, resolvedEmail, userId)

            val database = SoulSyncDatabase.getDatabase(context, userId)
            val userAccountDao = database.userAccountDao()

            var user = userAccountDao.getUserById(userId)
            val resolvedName = currentUser.userMetadata?.get("full_name")?.toString() 
                ?: user?.displayName 
                ?: resolvedEmail.substringBefore("@")

            if (user == null) {
                user = UserAccount(
                    id = userId,
                    email = resolvedEmail,
                    passwordKey = passwordKey,
                    displayName = resolvedName
                )
                userAccountDao.insertUser(user)
            } else if (user.passwordKey != passwordKey || user.email != resolvedEmail || user.displayName != resolvedName) {
                user = user.copy(passwordKey = passwordKey, email = resolvedEmail, displayName = resolvedName)
                userAccountDao.insertUser(user)
            }

            // Save info to DataStore
            settingsRepository.setUserInfo(userId, resolvedEmail, user.displayName)

            try {
                SupabaseSyncHelper.syncUserAccount(user)
                SupabaseSyncHelper.pullAllFromSupabase(userId, database)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync user account or pull records on signin", e)
            }

            SignInResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Supabase Auth signIn failed.", e)
            SignInResult.Error(e.message ?: "Incorrect email or password.")
        }
    }
}

sealed class SignUpResult {
    data class Success(val userAccount: UserAccount) : SignUpResult()
    data class Error(val message: String) : SignUpResult()
}

sealed class SignInResult {
    data class Success(val userAccount: UserAccount) : SignInResult()
    data class Error(val message: String) : SignInResult()
}
