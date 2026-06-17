package com.nutriplan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nutriplan.app.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

/**
 * Heti terv DAO – a meal_plans tábla kezelése.
 */
@Dao
interface MealPlanDao {

    /** Recept hozzárendelése egy naphoz és étkezéshez. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealPlan: MealPlanEntity): Long

    /** Hozzárendelés törlése azonosító alapján. */
    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Az összes heti terv bejegyzés élő lekérdezése. */
    @Query("SELECT * FROM meal_plans")
    fun getAll(): Flow<List<MealPlanEntity>>

    /** Az összes heti terv bejegyzés egyszeri lekérése (bevásárlólistához). */
    @Query("SELECT * FROM meal_plans")
    suspend fun getAllOnce(): List<MealPlanEntity>

    /** A teljes heti terv törlése. */
    @Query("DELETE FROM meal_plans")
    suspend fun clearAll()

    /** Egy adott nap összes hozzárendelésének törlése. */
    @Query("DELETE FROM meal_plans WHERE weekDay = :weekDay")
    suspend fun deleteByDay(weekDay: String)

    /** Egy adott recepthez tartozó összes hozzárendelés törlése (recept törlésekor). */
    @Query("DELETE FROM meal_plans WHERE recipeId = :recipeId")
    suspend fun deleteByRecipe(recipeId: Long)
}
