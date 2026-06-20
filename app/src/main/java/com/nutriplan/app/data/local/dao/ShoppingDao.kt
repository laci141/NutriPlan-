package com.nutriplan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutriplan.app.data.local.entity.ShoppingItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Bevásárlólista DAO – a shopping_items tábla kezelése.
 */
@Dao
interface ShoppingDao {

    /** Több bevásárlólista tétel beszúrása (generáláskor). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    /** Egy tétel beszúrása. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity): Long

    /** Az összes bevásárlólista tétel élő lekérdezése kategória és név szerint rendezve. */
    @Query("SELECT * FROM shopping_items ORDER BY category ASC, name COLLATE NOCASE ASC")
    fun getAll(): Flow<List<ShoppingItemEntity>>

    /** Egy tétel megvásárolt állapotának frissítése. */
    @Query("UPDATE shopping_items SET purchased = :purchased WHERE id = :id")
    suspend fun updatePurchased(id: Long, purchased: Boolean)

    /** Egy tétel törlése azonosító alapján. */
    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** A megvásárolt tételek törlése. */
    @Query("DELETE FROM shopping_items WHERE purchased = 1")
    suspend fun clearPurchased()

    /** A teljes bevásárlólista törlése. */
    @Query("DELETE FROM shopping_items")
    suspend fun clearAll()
}
