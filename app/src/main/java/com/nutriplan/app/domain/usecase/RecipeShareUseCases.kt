package com.nutriplan.app.domain.usecase

import com.nutriplan.app.data.backup.IngredientDto
import com.nutriplan.app.data.backup.RecipeDto
import com.nutriplan.app.domain.model.Ingredient
import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.repository.RecipeRepository
import com.nutriplan.app.util.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Egyetlen recept JSON-szöveggé alakítása megosztáshoz (a helyi képútvonalat és a
 * fordítási kulcsokat szándékosan kihagyjuk – más eszközön nem értelmesek).
 */
class ShareRecipeUseCase @Inject constructor() {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    operator fun invoke(recipe: Recipe): String {
        val dto = RecipeDto(
            id = 0L,
            name = recipe.name,
            mealType = recipe.mealType.key,
            calories = recipe.calories,
            protein = recipe.protein,
            carbs = recipe.carbs,
            fat = recipe.fat,
            isDefault = false,
            nameKey = null,
            imagePath = null,
            instructions = recipe.instructions,
            isFavorite = false,
            ingredients = recipe.ingredients.map {
                IngredientDto(it.name, it.quantity, it.unit.key, it.category.key, null)
            }
        )
        return json.encodeToString(dto)
    }
}

/**
 * Megosztott recept JSON beolvasása és mentése új receptként.
 */
class ImportRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** @return true, ha a beolvasás sikeres volt. */
    suspend operator fun invoke(content: String): Boolean = try {
        val dto = json.decodeFromString<RecipeDto>(content)
        val recipe = Recipe(
            id = 0L,
            name = dto.name,
            mealType = MealType.fromKey(dto.mealType),
            calories = dto.calories,
            protein = dto.protein,
            carbs = dto.carbs,
            fat = dto.fat,
            ingredients = dto.ingredients.map {
                Ingredient(
                    name = it.name,
                    quantity = it.quantity,
                    unit = MeasurementUnit.fromKey(it.unit),
                    category = IngredientCategory.fromKey(it.category)
                )
            },
            instructions = dto.instructions
        )
        recipeRepository.saveRecipe(recipe)
        Logger.i(Logger.Tags.RECIPE, "Recept importálva: '${dto.name}'")
        true
    } catch (e: Exception) {
        Logger.w(Logger.Tags.RECIPE, "Recept importálási hiba: ${e.message}")
        false
    }
}
