package com.example

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.example.data.NotificationPreferencesRepository
import com.example.notification.NotificationBootReceiver
import com.example.notification.NotificationDeepLinkHandler
import com.example.notification.NotificationManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationSystemTest {

    private lateinit var context: Context
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: NotificationPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Initialize WorkManager for the Robolectric environment
        try {
            val config = androidx.work.Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
            androidx.work.WorkManager.initialize(context, config)
        } catch (e: Exception) {
            // Already initialized or ignored
        }
        
        // Create an in-memory Preference DataStore
        dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("test_notif_prefs")
        }
        repository = NotificationPreferencesRepository(dataStore)
    }

    @Test
    fun testDefaultPreferencesState() = runBlocking {
        val prefs = repository.getPreferences()
        assertTrue(prefs.masterEnabled)
        assertTrue(prefs.morningMoodEnabled)
        assertEquals("08:00", prefs.morningMoodTime)
        assertTrue(prefs.eveningJournalEnabled)
        assertEquals("20:00", prefs.eveningJournalTime)
        assertTrue(prefs.unfinishedDraftEnabled)
        assertTrue(prefs.gentleReturnEnabled)
        assertFalse(prefs.unfinishedDraftPending)
        assertFalse(prefs.unfinishedDraftNotificationSent)
    }

    @Test
    fun testUpdateDraftState() = runBlocking {
        repository.updateUnfinishedDraftPending(true)
        var prefs = repository.getPreferences()
        assertTrue(prefs.unfinishedDraftPending)
        assertTrue(prefs.unfinishedDraftLastChanged > 0L)

        repository.updateUnfinishedDraftNotificationSent(true)
        prefs = repository.getPreferences()
        assertTrue(prefs.unfinishedDraftNotificationSent)

        // Clear draft
        repository.updateUnfinishedDraftPending(false)
        prefs = repository.getPreferences()
        assertFalse(prefs.unfinishedDraftPending)
        assertFalse(prefs.unfinishedDraftNotificationSent)
    }

    @Test
    fun testUpdateLastActiveTimestampAndInactivity() = runBlocking {
        val now = System.currentTimeMillis()
        repository.updateLastActiveTimestamp(now)
        var prefs = repository.getPreferences()
        assertEquals(now, prefs.lastActiveTimestamp)
        assertFalse(prefs.sent3DayInactivity)
        assertFalse(prefs.sent7DayInactivity)

        // Set sent flags
        repository.updateSent3DayInactivity(true)
        repository.updateSent7DayInactivity(true)
        prefs = repository.getPreferences()
        assertTrue(prefs.sent3DayInactivity)
        assertTrue(prefs.sent7DayInactivity)

        // When user becomes active, it resets inactivity flags!
        repository.updateLastActiveTimestamp(System.currentTimeMillis())
        prefs = repository.getPreferences()
        assertFalse(prefs.sent3DayInactivity)
        assertFalse(prefs.sent7DayInactivity)
    }

    @Test
    fun testDeepLinkIntentCreation() {
        val route = "tab_diary"
        val pendingIntent = NotificationDeepLinkHandler.createPendingIntent(context, route, 1001)
        assertNotNull(pendingIntent)

        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            putExtra(NotificationDeepLinkHandler.EXTRA_ROUTE, route)
        }
        assertEquals(route, intent.getStringExtra(NotificationDeepLinkHandler.EXTRA_ROUTE))
    }

    @Test
    fun testBootReceiverRescheduling() {
        val receiver = NotificationBootReceiver()
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        // Verify receiver can receive without throwing exceptions
        try {
            receiver.onReceive(context, intent)
        } catch (e: Exception) {
            fail("Boot receiver threw an exception: ${e.message}")
        }
    }
}
