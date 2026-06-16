package com.nutriplan.app.domain.usecase

import com.nutriplan.app.domain.model.MealAssignment
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.domain.repository.MealPlanRepository
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Heti tervező use case-ek.
 */

/** A teljes heti terv lekérése (napokra és étkezésekre bontva). */
class GetWeeklyPlanUseCase @Inject constructor(
    private val repository: MealPlanRepository
) {
    operator fun invoke(): Flow<List<MealAssignment>> {
        Logger.d(Logger.Tags.PLANNER, "GetWeeklyPlanUseCase meghívva")
        return repository.getWeeklyPlan()
    }
}

/** Recept hozzárendelése egy adott naphoz és étkezéshez. */
class AssignRecipeUseCase @Inject constructor(
    private val repository: MealPlanRepository
) {
    suspend operator fun invoke(weekDay: WeekDay, mealType: MealType, recipeId: Long) {
        Logger.i(
            Logger.Tags.PLANNER,
            "AssignRecipeUseCase meghívva: nap=${weekDay.key}, étkezés=${mealType.key}, recipeId=$recipeId"
        )
        repository.assignRecipe(weekDay, mealType, recipeId)
    }
}

/** Hozzárendelés eltávolítása a heti tervből. */
class RemoveAssignmentUseCase @Inject constructor(
    private val repository: MealPlanRepository
) {
    suspend operator fun invoke(id: Long) {
        Logger.i(Logger.Tags.PLANNER, "RemoveAssignmentUseCase meghívva, id=$id")
        repository.removeAssignment(id)
    }
}

/** A teljes heti terv törlése. */
class ClearWeekUseCase @Inject constructor(
    private val repository: MealPlanRepository
) {
    suspend operator fun invoke() {
        Logger.i(Logger.Tags.PLANNER, "ClearWeekUseCase meghívva")
        repository.clearWeek()
    }
}
