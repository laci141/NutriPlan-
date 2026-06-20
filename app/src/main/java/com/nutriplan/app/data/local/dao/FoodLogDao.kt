package com.nutriplan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nutriplan.app.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Az étkezés-napló adathozzáférési rétege.
 */
@Dao
interface FoodLogDao {

    @Insert
    suspend fun insert(entry: FoodLogEntity): Long

    @Query("SELECT * FROM food_log WHERE epochDay = :epochDay ORDER BY id DESC")
    fun forDay(epochDay: Long): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_log WHERE epochDay BETWEEN :from AND :to ORDER BY epochDay")
    fun forRange(from: Long, to: Long): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_log WHERE epochDay BETWEEN :from AND :to ORDER BY epochDay")
    suspend fun forRangeOnce(from: Long, to: Long): List<FoodLogEntity>

    /** A legutóbb naplózott ételek (gyors újra-hozzáadáshoz), névre szűrve egyedire. */
    @Query("SELECT * FROM food_log GROUP BY name ORDER BY MAX(id) DESC LIMIT 12")
    fun recentDistinct(): Flow<List<FoodLogEntity>>

    @Query("DELETE FROM food_log WHERE id = :id")
    suspend fun deleteById(id: Long)
}
