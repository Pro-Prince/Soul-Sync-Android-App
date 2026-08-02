package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val periodTrackerEnabled: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.PERIOD_TRACKER_ENABLED] ?: false }
    val colorTheme: Flow<String> = dataStore.data.map { it[PreferencesKeys.COLOR_THEME] ?: "SOUL_PINK" }
    val darkMode: Flow<Boolean?> = dataStore.data.map { it[PreferencesKeys.DARK_MODE] }
    val userEmail: Flow<String?> = dataStore.data.map { it[PreferencesKeys.USER_EMAIL] }
    val userId: Flow<String?> = dataStore.data.map { it[PreferencesKeys.USER_ID] }
    val displayName: Flow<String?> = dataStore.data.map { it[PreferencesKeys.USER_NAME] }
    val onboardingComplete: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.ONBOARDING_COMPLETE] ?: false }
    val cycleOnboardingComplete: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.CYCLE_ONBOARDING_COMPLETE] ?: false }

    suspend fun getUserEmail(): String? {
        return dataStore.data.map { it[PreferencesKeys.USER_EMAIL] }.firstOrNull()
    }

    suspend fun getUserId(): String? {
        return dataStore.data.map { it[PreferencesKeys.USER_ID] }.firstOrNull()
    }

    suspend fun isOnboardingComplete(): Boolean {
        return dataStore.data.map { it[PreferencesKeys.ONBOARDING_COMPLETE] ?: false }.firstOrNull() ?: false
    }

    suspend fun saveUserEmail(email: String) {
        dataStore.edit { it[PreferencesKeys.USER_EMAIL] = email }
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { it[PreferencesKeys.USER_ID] = userId }
    }

    suspend fun saveUserName(name: String) {
        dataStore.edit { it[PreferencesKeys.USER_NAME] = name }
    }

    suspend fun setUserInfo(userId: String, email: String, name: String) {
        dataStore.edit { 
            it[PreferencesKeys.USER_ID] = userId
            it[PreferencesKeys.USER_EMAIL] = email
            it[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun clearUserCredentials() {
        dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.USER_ID)
            prefs.remove(PreferencesKeys.USER_EMAIL)
            prefs.remove(PreferencesKeys.USER_NAME)
            prefs.remove(PreferencesKeys.ONBOARDING_COMPLETE)
            prefs[PreferencesKeys.DARK_MODE] = false
            prefs[PreferencesKeys.COLOR_THEME] = "SOUL_PINK"
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setCycleOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[PreferencesKeys.CYCLE_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setPeriodTrackerEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.PERIOD_TRACKER_ENABLED] = enabled }
    }

    suspend fun saveCyclePreferences(
        flow: String,
        cramps: String,
        moods: String,
        energy: String,
        symptoms: String,
        aiSupport: Boolean,
        reminders: Boolean
    ) {
        dataStore.edit {
            it[PreferencesKeys.USUAL_FLOW] = flow
            it[PreferencesKeys.USUAL_CRAMPS] = cramps
            it[PreferencesKeys.PRE_PERIOD_MOODS] = moods
            it[PreferencesKeys.PRE_PERIOD_ENERGY] = energy
            it[PreferencesKeys.USUAL_SYMPTOMS] = symptoms
            it[PreferencesKeys.CYCLE_AI_SUPPORT_ENABLED] = aiSupport
            it[PreferencesKeys.CYCLE_REMINDERS_ENABLED] = reminders
        }
    }

    suspend fun setColorTheme(theme: String) {
        dataStore.edit { it[PreferencesKeys.COLOR_THEME] = theme }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DARK_MODE] = enabled }
    }
}
