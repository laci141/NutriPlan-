package com.nutriplan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nutriplan.app.data.local.entity.IngredientEntity
import com.nutriplan.app.data.local.entity.RecipeEntity
import com.nutriplan.app.data.local.relation.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

/**
 * Recept DAO – a recipes és ingredients táblák lekérdezései.
 */
@Dao
interface RecipeDao {

    /** Új recept beszúrása, visszaadja a generált azonosítót. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    /** Hozzávalók beszúrása egy recepthez. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    /** Meglévő recept frissítése. */
    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    /** Egy recept kedvenc állapotának beállítása. */
    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    /** Recept törlése (a hozzávalók CASCADE módon törlődnek). */
    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    /** Egy recept hozzávalóinak törlése (szerkesztés előtti újraírásnál). */
    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Long)

    /** Összes recept a hozzávalókkal együtt, élő (Flow) lekérdezés. */
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name COLLATE NOCASE ASC")
    fun getRecipesWithIngredients(): Flow<List<RecipeWithIngredients>>

    /** Receptek keresése név alapján (rész-egyezés). */
    @Transaction
    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :query || '%' ORDER BY name COLLATE NOCASE ASC")
    fun searchRecipesWithIngredients(query: String): Flow<List<RecipeWithIngredients>>

    /** Egy konkrét recept lekérése azonosító alapján. */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeWithIngredients(id: Long): Flow<RecipeWithIngredients?>

    /** Egy konkrét recept egyszeri lekérése (nem Flow). */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeWithIngredientsOnce(id: Long): RecipeWithIngredients?

    /** Receptek számának lekérése (alapadat-betöltés ellenőrzéséhez). */
    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun countRecipes(): Int

    /** Egy étkezéshez tartozó receptek lekérése (recept-választóhoz). */
    @Transaction
    @Query("SELECT * FROM recipes WHERE mealType = :mealType ORDER BY name COLLATE NOCASE ASC")
    fun getRecipesByMealType(mealType: String): Flow<List<RecipeWithIngredients>>
}
