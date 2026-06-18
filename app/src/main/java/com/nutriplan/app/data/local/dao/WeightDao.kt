package com.nutriplan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutriplan.app.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * A testsúly-napló adathozzáférési rétege.
 */
@Dao
interface WeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WeightEntryEntity)

    @Query("SELECT * FROM weight_log ORDER BY epochDay")
    fun all(): Flow<List<WeightEntryEntity>>

    @Query("DELETE FROM weight_log WHERE epochDay = :epochDay")
    suspend fun deleteByDay(epochDay: Long)
}
