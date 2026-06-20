package com.nutriplan.app.domain.usecase

import com.nutriplan.app.domain.model.NutritionTotals
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.util.Logger
import javax.inject.Inject

/**
 * Tápérték-számító use case.
 * Egy recepthalmaz (étkezés, nap vagy hét) összesített tápértékét számolja ki.
 */
class CalculateNutritionUseCase @Inject constructor() {

    /** Kiszámolja a megadott receptek összesített tápértékét. */
    operator fun invoke(recipes: List<Recipe>): NutritionTotals {
        var totals = NutritionTotals()
        for (recipe in recipes) {
            totals += NutritionTotals(
                calories = recipe.calories,
                protein = recipe.protein,
                carbs = recipe.carbs,
                fat = recipe.fat
            )
        }
        Logger.i(
            Logger.Tags.NUTRITION,
            "Calculation completed – ${recipes.size} recept, ${totals.calories} kcal, " +
                "F:${totals.protein}g Sz:${totals.carbs}g Zs:${totals.fat}g"
        )
        return totals
    }
}
