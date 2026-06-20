package com.nutriplan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutriplan.app.data.local.entity.MoodEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * A hangulat-napló adathozzáférési rétege.
 */
@Dao
interface MoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MoodEntryEntity)

    @Query("SELECT * FROM mood_log ORDER BY epochDay")
    fun all(): Flow<List<MoodEntryEntity>>

    @Query("DELETE FROM mood_log WHERE epochDay = :epochDay")
    suspend fun deleteByDay(epochDay: Long)
}
