package com.nutriplan.app.presentation.dashboard

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.data.preferences.DashboardPreferences
import com.nutriplan.app.data.preferences.SettingsManager
import com.nutriplan.app.domain.model.NutritionTotals
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.domain.usecase.CalculateNutritionUseCase
import com.nutriplan.app.domain.usecase.GetWeeklyPlanUseCase
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * A Dashboard (kezdőlap) állapota: mai tápérték, célok, víz, makró-célok.
 */
data class DashboardUiState(
    val todayTotals: NutritionTotals = NutritionTotals(),
    val calorieGoal: Int = 0,
    val proteinTarget: Int = 0,
    val carbsTarget: Int = 0,
    val fatTarget: Int = 0,
    val water: Int = 0,
    val waterGoal: Int = WATER_GOAL_ML,
    val stepGoal: Int = STEP_GOAL
) {
    companion object {
        const val WATER_GOAL_ML = 2000
        const val STEP_GOAL = 10000
    }
}

/**
 * Dashboard ViewModel. Összegyűjti a mai napra a tápértéket a heti tervből,
 * a kalória- és makró-célokat, a vízfogyasztást (DataStore) és a lépésszámot (szenzor).
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext context: Context,
    getWeeklyPlanUseCase: GetWeeklyPlanUseCase,
    settingsManager: SettingsManager,
    private val dashboardPreferences: DashboardPreferences,
    private val calculateNutritionUseCase: CalculateNutritionUseCase
) : ViewModel() {

    // A mai nap a saját WeekDay enumunkra leképezve
    private val today: WeekDay
        get() = WeekDay.valueOf(LocalDate.now().dayOfWeek.name)

    val uiState: StateFlow<DashboardUiState> = combine(
        getWeeklyPlanUseCase(),
        settingsManager.calorieGoal,
        dashboardPreferences.waterToday
    ) { assignments, goal, water ->
        val todayRecipes = assignments.filter { it.weekDay == today }.map { it.recipe }
        val totals = calculateNutritionUseCase(todayRecipes)
        // Makró-célok a kalóriacélból: 30% fehérje, 40% szénhidrát, 30% zsír
        val effectiveGoal = if (goal > 0) goal else 2000
        DashboardUiState(
            todayTotals = totals,
            calorieGoal = goal,
            proteinTarget = (effectiveGoal * 0.30 / 4).roundToInt(),
            carbsTarget = (effectiveGoal * 0.40 / 4).roundToInt(),
            fatTarget = (effectiveGoal * 0.30 / 9).roundToInt(),
            water = water
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    // --- Lépésszámláló (hardveres szenzor, biztonságos kezeléssel) ---
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    /** Elérhető-e egyáltalán lépésszámláló szenzor az eszközön. */
    val stepSensorAvailable: Boolean = stepSensor != null

    private var baseline = -1f
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // A szenzor a rendszerindítás óta összesített lépést adja; a megnyitáskori
            // értéket alapvonalként vesszük, így a munkamenet alatt megtett lépést mutatjuk.
            val total = event.values.firstOrNull() ?: return
            if (baseline < 0f) baseline = total
            _steps.value = (total - baseline).toInt().coerceAtLeast(0)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    /** Lépésszámlálás indítása (engedély megadása után hívandó). */
    fun startStepTracking() {
        val sensor = stepSensor ?: return
        sensorManager?.registerListener(stepListener, sensor, SensorManager.SENSOR_DELAY_UI)
        Logger.i(Logger.Tags.VIEWMODEL, "Lépésszámlálás elindítva")
    }

    /** Lépésszámlálás leállítása. */
    fun stopStepTracking() {
        sensorManager?.unregisterListener(stepListener)
    }

    /** +250 ml víz hozzáadása a mai naphoz. */
    fun addWater() {
        viewModelScope.launch { dashboardPreferences.addWater(250) }
    }

    init {
        Logger.i(Logger.Tags.VIEWMODEL, "DashboardViewModel létrehozva")
    }

    override fun onCleared() {
        stopStepTracking()
        super.onCleared()
    }
}
