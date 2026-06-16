package com.nutriplan.app.data.repository

import com.nutriplan.app.data.local.dao.MealPlanDao
import com.nutriplan.app.data.local.dao.RecipeDao
import com.nutriplan.app.data.mapper.toDomain
import com.nutriplan.app.data.mapper.toEntity
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.repository.RecipeRepository
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recept tároló megvalósítás Room adatbázis felett.
 */
@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val mealPlanDao: MealPlanDao
) : RecipeRepository {

    override fun getRecipes(): Flow<List<Recipe>> {
        Logger.d(Logger.Tags.REPOSITORY, "Receptek lekérdezése (Flow) indítása")
        return recipeDao.getRecipesWithIngredients().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchRecipes(query: String): Flow<List<Recipe>> {
        Logger.d(Logger.Tags.REPOSITORY, "Recept keresés lekérdezése: '$query'")
        return recipeDao.searchRecipesWithIngredients(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getRecipe(id: Long): Flow<Recipe?> {
        Logger.d(Logger.Tags.REPOSITORY, "Recept lekérdezése azonosító alapján: $id")
        return recipeDao.getRecipeWithIngredients(id).map { it?.toDomain() }
    }

    override fun getRecipesByMealType(mealType: MealType): Flow<List<Recipe>> {
        Logger.d(Logger.Tags.REPOSITORY, "Receptek lekérdezése étkezés szerint: ${mealType.key}")
        return recipeDao.getRecipesByMealType(mealType.key).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveRecipe(recipe: Recipe): Long {
        // Új recept beszúrása vagy meglévő frissítése
        val recipeId: Long
        if (recipe.id == 0L) {
            recipeId = recipeDao.insertRecipe(recipe.toEntity().copy(id = 0L))
            Logger.i(Logger.Tags.RECIPE, "Recipe inserted – új recept mentve, id=$recipeId, név='${recipe.name}'")
        } else {
            recipeId = recipe.id
            recipeDao.updateRecipe(recipe.toEntity())
            recipeDao.deleteIngredientsForRecipe(recipeId)
            Logger.i(Logger.Tags.RECIPE, "Recipe updated – recept frissítve, id=$recipeId, név='${recipe.name}'")
        }
        // Hozzávalók (újra)írása az aktuális recepthez
        val ingredientEntities = recipe.ingredients.map { it.toEntity(recipeId).copy(id = 0L) }
        recipeDao.insertIngredients(ingredientEntities)
        Logger.d(Logger.Tags.RECIPE, "Hozzávalók mentve: ${ingredientEntities.size} db, recipeId=$recipeId")
        return recipeId
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        // A recepthez tartozó terv-hozzárendelések eltávolítása, majd a recept törlése
        mealPlanDao.deleteByRecipe(recipe.id)
        recipeDao.deleteRecipe(recipe.toEntity())
        Logger.i(Logger.Tags.RECIPE, "Recipe deleted – recept törölve, id=${recipe.id}, név='${recipe.name}'")
    }

    override suspend fun countRecipes(): Int {
        val count = recipeDao.countRecipes()
        Logger.d(Logger.Tags.REPOSITORY, "Receptek száma az adatbázisban: $count")
        return count
    }
}
