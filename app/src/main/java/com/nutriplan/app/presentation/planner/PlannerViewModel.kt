package com.nutriplan.app.presentation.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.domain.model.MealAssignment
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.NutritionTotals
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.domain.usecase.AssignRecipeUseCase
import com.nutriplan.app.domain.usecase.CalculateNutritionUseCase
import com.nutriplan.app.domain.usecase.ClearWeekUseCase
import com.nutriplan.app.domain.usecase.GetRecipesUseCase
import com.nutriplan.app.domain.usecase.GetWeeklyPlanUseCase
import com.nutriplan.app.domain.usecase.RemoveAssignmentUseCase
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Heti tervező ViewModel.
 * Kezeli a hozzárendeléseket és kiszámolja a napi tápérték-összesítéseket.
 */
@HiltViewModel
class PlannerViewModel @Inject constructor(
    getWeeklyPlanUseCase: GetWeeklyPlanUseCase,
    getRecipesUseCase: GetRecipesUseCase,
    private val assignRecipeUseCase: AssignRecipeUseCase,
    private val removeAssignmentUseCase: RemoveAssignmentUseCase,
    private val clearWeekUseCase: ClearWeekUseCase,
    private val calculateNutritionUseCase: CalculateNutritionUseCase
) : ViewModel() {

    // A teljes heti terv hozzárendelései
    val assignments: StateFlow<List<MealAssignment>> = getWeeklyPlanUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Az összes recept (a recept-választóhoz)
    val allRecipes: StateFlow<List<Recipe>> = getRecipesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Napi tápérték-összesítések a hozzárendelésekből számolva
    val dayTotals: StateFlow<Map<WeekDay, NutritionTotals>> = getWeeklyPlanUseCase()
        .map { list ->
            WeekDay.entries.associateWith { day ->
                val recipesForDay = list.filter { it.weekDay == day }.map { it.recipe }
                calculateNutritionUseCase(recipesForDay)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    init {
        Logger.i(Logger.Tags.VIEWMODEL, "PlannerViewModel létrehozva")
    }

    /** Recept hozzárendelése egy naphoz és étkezéshez. */
    fun assign(weekDay: WeekDay, mealType: MealType, recipeId: Long) {
        viewModelScope.launch {
            Logger.i(
                Logger.Tags.VIEWMODEL,
                "PlannerViewModel – hozzárendelés: ${weekDay.key}/${mealType.key}, recipeId=$recipeId"
            )
            assignRecipeUseCase(weekDay, mealType, recipeId)
        }
    }

    /** Hozzárendelés eltávolítása. */
    fun remove(assignmentId: Long) {
        viewModelScope.launch {
            Logger.i(Logger.Tags.VIEWMODEL, "PlannerViewModel – hozzárendelés törlése: id=$assignmentId")
            removeAssignmentUseCase(assignmentId)
        }
    }

    /** A teljes hét törlése. */
    fun clearWeek() {
        viewModelScope.launch {
            Logger.i(Logger.Tags.VIEWMODEL, "PlannerViewModel – teljes hét törlése")
            clearWeekUseCase()
        }
    }
}
