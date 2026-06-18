package com.nutriplan.app.domain.usecase

import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.domain.repository.MealPlanRepository
import com.nutriplan.app.domain.repository.RecipeRepository
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Szabály-alapú automatikus heti terv generátor.
 *
 * A meglévő receptekből tölti fel a hetet úgy, hogy a napi kalória közelítse a
 * megadott célt. Először főétkezéseket oszt ki (reggeli/ebéd/vacsora), majd ha a
 * nap kalóriája a cél alatt marad, snackekkel egészíti ki. (Nem igényel AI-t.)
 */
class GenerateWeekPlanUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository
) {
    /** @return a kiosztott étkezések száma; 0, ha nincs egyetlen recept sem. */
    suspend operator fun invoke(calorieGoal: Int): Int {
        val recipes = recipeRepository.getRecipes().first()
        if (recipes.isEmpty()) {
            Logger.w(Logger.Tags.VIEWMODEL, "Auto-terv: nincs recept")
            return 0
        }

        val target = if (calorieGoal > 0) calorieGoal else 2000
        val byMeal: Map<MealType, List<Recipe>> = recipes.groupBy { it.mealType }
        fun pick(meal: MealType): Recipe? = byMeal[meal]?.randomOrNull() ?: recipes.randomOrNull()

        mealPlanRepository.clearWeek()
        var assigned = 0

        for (day in WeekDay.entries) {
            var dayCalories = 0
            // Főétkezések
            for (meal in listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)) {
                val recipe = pick(meal) ?: continue
                mealPlanRepository.assignRecipe(day, meal, recipe.id)
                dayCalories += recipe.calories
                assigned++
            }
            // Kiegészítés snackekkel, amíg a nap a cél ~90%-a alatt van
            val snackMeals = listOf(MealType.MORNING_SNACK, MealType.AFTERNOON_SNACK)
            for (meal in snackMeals) {
                if (dayCalories >= (target * 0.9)) break
                val snack = byMeal[meal]?.randomOrNull() ?: byMeal[MealType.BREAKFAST]?.randomOrNull() ?: continue
                mealPlanRepository.assignRecipe(day, meal, snack.id)
                dayCalories += snack.calories
                assigned++
            }
        }
        Logger.i(Logger.Tags.VIEWMODEL, "Auto-terv kész: $assigned étkezés kiosztva (cél $target kcal)")
        return assigned
    }
}
