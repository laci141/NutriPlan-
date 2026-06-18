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
import com.nutriplan.app.data.remote.OpenFoodFactsDataSource
import com.nutriplan.app.data.remote.ProductLookupResult
import com.nutriplan.app.data.remote.ScannedProduct
import com.nutriplan.app.domain.model.FoodLogEntry
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.NutritionTotals
import com.nutriplan.app.domain.model.WeightEntry
import com.nutriplan.app.domain.repository.FoodLogRepository
import com.nutriplan.app.domain.repository.WeightRepository
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
    val todayEntries: List<FoodLogEntry> = emptyList(),
    val streak: Int = 0,
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
    settingsManager: SettingsManager,
    private val dashboardPreferences: DashboardPreferences,
    private val foodLogRepository: FoodLogRepository,
    private val weightRepository: WeightRepository,
    private val openFoodFacts: OpenFoodFactsDataSource
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    // Egyéni makró-célok (0 = automatikus) egyetlen flow-ba kötve
    private val macroGoals = combine(
        settingsManager.proteinGoal,
        settingsManager.carbsGoal,
        settingsManager.fatGoal
    ) { p, c, f -> Triple(p, c, f) }

    // A mai naplóbejegyzések
    private val todayEntries = foodLogRepository.entriesForDay(today)

    // Az elmúlt ~60 nap a sorozat (streak) számításához
    private val recentRange =
        foodLogRepository.entriesForRange(today.minusDays(60), today)

    /** A gyakran/utoljára naplózott ételek a gyors újra-hozzáadáshoz. */
    val recentFoods: StateFlow<List<FoodLogEntry>> = foodLogRepository.recent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Az utolsó 7 nap kalória-összege (régebbitől a maiig) a heti diagramhoz. */
    val weekCalories: StateFlow<List<Pair<LocalDate, Int>>> = recentRange
        .map { entries ->
            val sums = entries.groupBy { it.date }
                .mapValues { (_, list) -> list.sumOf { it.calories } }
            (6 downTo 0).map { offset ->
                val d = today.minusDays(offset.toLong())
                d to (sums[d] ?: 0)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** A testsúly-bejegyzések időrendben (a trendgrafikonhoz). */
    val weights: StateFlow<List<WeightEntry>> = weightRepository.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Mai (vagy a megadott) testsúly mentése. */
    fun addWeight(kg: Double) {
        if (kg <= 0) return
        viewModelScope.launch {
            weightRepository.set(WeightEntry(date = today, weightKg = kg))
            Logger.i(Logger.Tags.VIEWMODEL, "Testsúly mentve: $kg kg")
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        todayEntries,
        settingsManager.calorieGoal,
        dashboardPreferences.waterToday,
        macroGoals,
        recentRange
    ) { entries, goal, water, macros, range ->
        val totals = entries.fold(NutritionTotals()) { acc, e ->
            acc + NutritionTotals(e.calories, e.protein, e.carbs, e.fat)
        }
        val effectiveGoal = if (goal > 0) goal else 2000
        val (customP, customC, customF) = macros
        DashboardUiState(
            todayTotals = totals,
            todayEntries = entries,
            streak = computeStreak(range, goal),
            calorieGoal = goal,
            proteinTarget = if (customP > 0) customP else (effectiveGoal * 0.30 / 4).roundToInt(),
            carbsTarget = if (customC > 0) customC else (effectiveGoal * 0.40 / 4).roundToInt(),
            fatTarget = if (customF > 0) customF else (effectiveGoal * 0.30 / 9).roundToInt(),
            water = water
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    /**
     * Folyamatos napi sorozat: hány egymást követő napon (ma vagy tegnaptól visszafelé)
     * érte el a napló a kalóriacél 80–120%-át. Cél nélkül bármely naplózott nap számít.
     */
    private fun computeStreak(entries: List<FoodLogEntry>, goal: Int): Int {
        val byDay = entries.groupBy { it.date }
            .mapValues { (_, list) -> list.sumOf { it.calories } }
        var day = today
        if ((byDay[day] ?: 0) == 0) day = today.minusDays(1)
        var streak = 0
        while (true) {
            val cal = byDay[day] ?: 0
            val hit = if (goal > 0) cal in (goal * 0.8).toInt()..(goal * 1.2).toInt() else cal > 0
            if (hit) {
                streak++
                day = day.minusDays(1)
            } else break
        }
        return streak
    }

    /** Új naplóbejegyzés hozzáadása a mai naphoz. */
    fun addFood(name: String, calories: Int, protein: Double, carbs: Double, fat: Double, mealType: MealType) {
        if (name.isBlank()) return
        viewModelScope.launch {
            foodLogRepository.add(
                FoodLogEntry(
                    date = today,
                    name = name.trim(),
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    mealType = mealType
                )
            )
            Logger.i(Logger.Tags.VIEWMODEL, "Étel naplózva: '$name' ($calories kcal)")
        }
    }

    /** Egy naplóbejegyzés törlése. */
    fun deleteFood(id: Long) {
        viewModelScope.launch { foodLogRepository.delete(id) }
    }

    // --- Vonalkód a naplóhoz: a beolvasott termék 100 g-os adatai ---
    private val _scannedProduct = MutableStateFlow<ScannedProduct?>(null)
    val scannedProduct: StateFlow<ScannedProduct?> = _scannedProduct.asStateFlow()

    private val _scanLookupFailed = MutableStateFlow(false)
    val scanLookupFailed: StateFlow<Boolean> = _scanLookupFailed.asStateFlow()

    /** Vonalkód lekérdezése az Open Food Facts-ből (a napló-dialógus tölti ki belőle a mezőket). */
    fun lookupBarcode(code: String) {
        viewModelScope.launch {
            when (val result = openFoodFacts.lookup(code)) {
                is ProductLookupResult.Success -> _scannedProduct.value = result.product
                else -> _scanLookupFailed.value = true
            }
        }
    }

    /** A beolvasott termék / hiba nyugtázása a dialógus után. */
    fun consumeScan() {
        _scannedProduct.value = null
        _scanLookupFailed.value = false
    }

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
