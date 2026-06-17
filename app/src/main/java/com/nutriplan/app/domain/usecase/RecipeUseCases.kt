package com.nutriplan.app.domain.usecase

import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.repository.RecipeRepository
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Recept use case-ek – a recept funkcionalitás üzleti logikája.
 * Minden fontos művelet naplózódik.
 */

/** Összes recept lekérése. */
class GetRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> {
        Logger.d(Logger.Tags.RECIPE, "GetRecipesUseCase meghívva")
        return repository.getRecipes()
    }
}

/** Receptek keresése név alapján; üres keresésnél az összes recept. */
class SearchRecipesUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(query: String): Flow<List<Recipe>> {
        Logger.d(Logger.Tags.RECIPE, "SearchRecipesUseCase meghívva, kifejezés='$query'")
        return if (query.isBlank()) repository.getRecipes()
        else repository.searchRecipes(query.trim())
    }
}

/** Egy recept lekérése azonosító alapján (szerkesztéshez). */
class GetRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(id: Long): Flow<Recipe?> {
        Logger.d(Logger.Tags.RECIPE, "GetRecipeUseCase meghívva, id=$id")
        return repository.getRecipe(id)
    }
}

/** Receptek szűrése étkezés típus szerint (recept-választóhoz). */
class GetRecipesByMealTypeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    operator fun invoke(mealType: MealType): Flow<List<Recipe>> {
        Logger.d(Logger.Tags.RECIPE, "GetRecipesByMealTypeUseCase meghívva, étkezés=${mealType.key}")
        return repository.getRecipesByMealType(mealType)
    }
}

/** Recept mentése (létrehozás vagy frissítés). */
class SaveRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe): Long {
        Logger.i(Logger.Tags.RECIPE, "SaveRecipeUseCase meghívva, név='${recipe.name}'")
        return repository.saveRecipe(recipe)
    }
}

/** Recept kedvenc állapotának átváltása. */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe) {
        Logger.i(Logger.Tags.RECIPE, "ToggleFavoriteUseCase meghívva, id=${recipe.id}")
        repository.setFavorite(recipe.id, !recipe.isFavorite)
    }
}

/** Recept törlése. */
class DeleteRecipeUseCase @Inject constructor(
    private val repository: RecipeRepository
) {
    suspend operator fun invoke(recipe: Recipe) {
        Logger.i(Logger.Tags.RECIPE, "DeleteRecipeUseCase meghívva, id=${recipe.id}")
        repository.deleteRecipe(recipe)
    }
}
