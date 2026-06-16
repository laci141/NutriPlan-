package com.nutriplan.app.presentation.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.domain.model.NutritionTotals
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.domain.usecase.CalculateNutritionUseCase
import com.nutriplan.app.domain.usecase.GetWeeklyPlanUseCase
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * A heti és napi tápérték-összesítések állapota.
 */
data class NutritionUiState(
    val weekTotals: NutritionTotals = NutritionTotals(),
    val dayTotals: Map<WeekDay, NutritionTotals> = emptyMap(),
    val hasData: Boolean = false
)

/**
 * Tápérték ViewModel – a heti terv alapján számol napi és heti összesítést.
 */
@HiltViewModel
class NutritionViewModel @Inject constructor(
    getWeeklyPlanUseCase: GetWeeklyPlanUseCase,
    private val calculateNutritionUseCase: CalculateNutritionUseCase
) : ViewModel() {

    val uiState: StateFlow<NutritionUiState> = getWeeklyPlanUseCase()
        .map { assignments ->
            // Napi összesítések kiszámolása
            val perDay = WeekDay.entries.associateWith { day ->
                val recipes = assignments.filter { it.weekDay == day }.map { it.recipe }
                calculateNutritionUseCase(recipes)
            }
            // Heti összesítés a napi értékek összegeként
            val week = perDay.values.fold(NutritionTotals()) { acc, totals -> acc + totals }
            Logger.i(
                Logger.Tags.NUTRITION,
                "Calculation completed – heti összesítés: ${week.calories} kcal"
            )
            NutritionUiState(
                weekTotals = week,
                dayTotals = perDay,
                hasData = assignments.isNotEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NutritionUiState())

    init {
        Logger.i(Logger.Tags.VIEWMODEL, "NutritionViewModel létrehozva")
    }
}
