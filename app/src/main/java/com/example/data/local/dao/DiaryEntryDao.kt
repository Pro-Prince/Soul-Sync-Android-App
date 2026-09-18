package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntry)

    @Update
    suspend fun update(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getById(id: String): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    fun getByIdFlow(id: String): Flow<DiaryEntry?>

    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllOrderedByDate(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByDateRange(start: String, end: String): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE mood = :mood ORDER BY date DESC")
    fun getByMood(mood: String): Flow<List<DiaryEntry>>

    @Query("""SELECT * FROM diary_entries WHERE
              title LIKE '%' || :query || '%' OR
              contentPlain LIKE '%' || :query || '%' OR
              hashtags LIKE '%' || :query || '%'
              ORDER BY createdAt DESC""")
    fun searchByKeyword(query: String): Flow<List<DiaryEntry>>

    @Query("SELECT COUNT(DISTINCT date) FROM diary_entries WHERE date >= :weekStart")
    fun getThisWeekCount(weekStart: String): Flow<Int>

    @Query("SELECT mood, COUNT(*) as count FROM diary_entries WHERE date >= :cutoff GROUP BY mood")
    fun getMoodCountsSince(cutoff: String): Flow<List<com.example.data.local.dao.MoodCount>>

    @Query("SELECT DISTINCT date FROM diary_entries ORDER BY date ASC")
    fun getAllEntryDates(): Flow<List<String>>

    @Query("SELECT DISTINCT date FROM diary_entries ORDER BY date DESC")
    suspend fun getStreakEntries(): List<String>

    @Query("SELECT * FROM diary_entries WHERE date = :date AND (title = :title OR (:title IS NULL AND title IS NULL)) AND contentPlain = :contentPlain LIMIT 1")
    suspend fun findDuplicate(date: String, title: String?, contentPlain: String): DiaryEntry?

    @Query("""
        DELETE FROM diary_entries 
        WHERE id NOT IN (
            SELECT MIN(id) 
            FROM diary_entries 
            GROUP BY date, mood, contentPlain, COALESCE(title, '')
        )
    """)
    suspend fun deleteDuplicates()
}
