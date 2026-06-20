package com.nutriplan.app.domain.repository

import com.nutriplan.app.domain.model.FoodLogEntry
import com.nutriplan.app.domain.model.MealAssignment
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MoodEntry
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.model.ShoppingItem
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.domain.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Étkezés-napló tároló interfész.
 */
interface FoodLogRepository {
    fun entriesForDay(date: LocalDate): Flow<List<FoodLogEntry>>
    fun entriesForRange(from: LocalDate, to: LocalDate): Flow<List<FoodLogEntry>>
    suspend fun rangeOnce(from: LocalDate, to: LocalDate): List<FoodLogEntry>
    fun recent(): Flow<List<FoodLogEntry>>
    suspend fun add(entry: FoodLogEntry): Long
    suspend fun delete(id: Long)
}

/**
 * Testsúly-napló tároló interfész.
 */
interface WeightRepository {
    fun all(): Flow<List<WeightEntry>>
    suspend fun set(entry: WeightEntry)
    suspend fun deleteByDay(date: LocalDate)
}

/**
 * Hangulat-napló tároló interfész.
 */
interface MoodRepository {
    fun all(): Flow<List<MoodEntry>>
    suspend fun set(entry: MoodEntry)
    suspend fun deleteByDay(date: LocalDate)
}

/**
 * Recept tároló interfész – a recept CRUD műveletek absztrakciója.
 */
interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>
    fun searchRecipes(query: String): Flow<List<Recipe>>
    fun getRecipe(id: Long): Flow<Recipe?>
    fun getRecipesByMealType(mealType: MealType): Flow<List<Recipe>>
    suspend fun saveRecipe(recipe: Recipe): Long
    suspend fun deleteRecipe(recipe: Recipe)
    suspend fun countRecipes(): Int
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}

/**
 * Heti terv tároló interfész – recept-hozzárendelések kezelése.
 */
interface MealPlanRepository {
    fun getWeeklyPlan(): Flow<List<MealAssignment>>
    suspend fun assignRecipe(weekDay: WeekDay, mealType: MealType, recipeId: Long)
    suspend fun removeAssignment(id: Long)
    suspend fun clearWeek()
    /** Egy nap összes hozzárendelésének átmásolása egy másik napra (a cél nap előző tartalmát törli). */
    suspend fun copyDay(from: WeekDay, to: WeekDay)
}

/**
 * Bevásárlólista tároló interfész.
 */
interface ShoppingRepository {
    fun getShoppingList(): Flow<List<ShoppingItem>>
    suspend fun generateFromPlan(): Int
    suspend fun setPurchased(id: Long, purchased: Boolean)
    suspend fun deleteItem(id: Long)
    suspend fun clearPurchased()
    suspend fun clearAll()
}

/**
 * Mentés/visszatöltés tároló interfész (JSON export/import).
 */
interface BackupRepository {
    suspend fun exportToJson(): String
    suspend fun importFromJson(json: String)
}
