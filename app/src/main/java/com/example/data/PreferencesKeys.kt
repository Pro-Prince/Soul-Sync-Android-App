package com.example.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val COLOR_THEME = stringPreferencesKey("color_theme")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val PERIOD_TRACKER_ENABLED = booleanPreferencesKey("period_tracker_enabled")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val CYCLE_ONBOARDING_COMPLETE = booleanPreferencesKey("cycle_onboarding_complete")
    val USUAL_FLOW = stringPreferencesKey("usual_flow")
    val USUAL_CRAMPS = stringPreferencesKey("usual_cramps")
    val PRE_PERIOD_MOODS = stringPreferencesKey("pre_period_moods")
    val PRE_PERIOD_ENERGY = stringPreferencesKey("pre_period_energy")
    val USUAL_SYMPTOMS = stringPreferencesKey("usual_symptoms")
    val CYCLE_AI_SUPPORT_ENABLED = booleanPreferencesKey("cycle_ai_support_enabled")
    val CYCLE_REMINDERS_ENABLED = booleanPreferencesKey("cycle_reminders_enabled")
    
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_ID = stringPreferencesKey("user_id")
    val USER_NAME = stringPreferencesKey("user_name")
}
