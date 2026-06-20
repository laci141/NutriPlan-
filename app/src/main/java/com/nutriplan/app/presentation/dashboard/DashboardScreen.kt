package com.nutriplan.app.presentation.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import com.nutriplan.app.presentation.components.NumericKeypad
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutriplan.app.R
import com.nutriplan.app.presentation.theme.Accent
import com.nutriplan.app.data.local.LocalFood
import com.nutriplan.app.domain.model.FoodLogEntry
import com.nutriplan.app.domain.model.MassUnit
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.NutritionTotals
import com.nutriplan.app.domain.model.SeasonalRegion
import com.nutriplan.app.domain.model.UnitFormatter
import com.nutriplan.app.presentation.components.ActivityRings
import com.nutriplan.app.presentation.components.CalorieRing
import com.nutriplan.app.presentation.components.LabeledDropdown
import com.nutriplan.app.presentation.components.RingData
import com.nutriplan.app.presentation.components.SimpleBarChart
import com.nutriplan.app.presentation.components.SimpleLineChart
import com.nutriplan.app.presentation.scanner.BarcodeScannerOverlay
import com.nutriplan.app.presentation.util.label
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

// A makró-gyűrűk tompított, szemkímélő színei
private val ProteinColor = Color(0xFF2A9E76) // mély zöld
private val CarbsColor = Color(0xFFB87A10)   // mély borostyán
private val FatColor = Color(0xFF2D61C2)      // mély kék

/**
 * Kezdőlap (Dashboard) – modern "Bento Box" elrendezés:
 * idő-alapú üdvözlés, kalória-gyűrű, makró aktivitási gyűrűk, víz és lépés kártyák.
 *
 * @param onOpenNutrition a kalória/makró kártyákról a részletes tápérték nézet megnyitása
 */
@Composable
fun DashboardScreen(
    onOpenNutrition: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val recentFoods by viewModel.recentFoods.collectAsStateWithLifecycle()
    val scannedProduct by viewModel.scannedProduct.collectAsStateWithLifecycle()
    val weekCalories by viewModel.weekCalories.collectAsStateWithLifecycle()
    val weights by viewModel.weights.collectAsStateWithLifecycle()
    val massUnit by viewModel.massUnit.collectAsStateWithLifecycle()
    val seasonalRegion by viewModel.seasonalRegion.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddFood by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var voiceInitialText by remember { mutableStateOf("") }
    var swapEntry by remember { mutableStateOf<FoodLogEntry?>(null) }

    // Hangnaplózás – rendszer speech intent (nem kell RECORD_AUDIO engedély)
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull().orEmpty()
            if (text.isNotBlank()) {
                voiceInitialText = text
                showAddFood = true
            }
        }
    }
    fun startVoiceInput() {
        runCatching {
            speechLauncher.launch(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
            )
        }
    }

    // Kamera-engedély a napló vonalkód-olvasójához
    val scannerPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) showScanner = true }
    fun openScanner() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) showScanner = true else scannerPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    // Ha érkezik beolvasott termék, nyíljon meg a hozzáadó dialógus
    LaunchedEffect(scannedProduct) {
        if (scannedProduct != null) showAddFood = true
    }

    // Lépésszámláló engedély (Android 10+ esetén ACTIVITY_RECOGNITION)
    val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    var hasStepPermission by remember {
        mutableStateOf(
            !needsPermission || ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasStepPermission = granted
        if (granted) viewModel.startStepTracking()
    }

    // Lépéskövetés indítása/leállítása az engedély függvényében
    DisposableEffect(hasStepPermission) {
        if (hasStepPermission) viewModel.startStepTracking()
        onDispose { viewModel.stopStepTracking() }
    }

    // Napszaknak megfelelő üdvözlés
    val hour = remember { LocalTime.now().hour }
    val greetingRes = when (hour) {
        in 5..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Üdvözlő fejléc
        Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Accent.Title
            )
            Text(
                text = stringResource(greetingRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Napi sorozat (streak), ha van
            if (state.streak > 0) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = CarbsColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.streak_days, state.streak),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = CarbsColor
                    )
                }
            }
        }

        // Nagy kalória kártya (2x2) – kalória-gyűrűvel
        val goal = if (state.calorieGoal > 0) state.calorieGoal else 2000
        val consumed = state.todayTotals.calories
        val left = (goal - consumed).coerceAtLeast(0)
        BentoCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clickable { onOpenNutrition() }
        ) {
            Text(
                text = stringResource(R.string.dashboard_calories_today),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CalorieRing(
                    fraction = if (goal > 0) consumed.toFloat() / goal else 0f,
                    modifier = Modifier.size(150.dp),
                    ringColor = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$consumed",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "/ $goal ${stringResource(R.string.kcal_unit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(Modifier.size(2.dp))
                        Text(
                            text = stringResource(R.string.left_short, left),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Tápérték (makrók) kártya – egymás alatti színes sorok, napi/heti váltóval
        MacroNutritionCard(
            state = state,
            weekCalories = weekCalories,
            onOpenNutrition = onOpenNutrition
        )

        // Mikrotápanyag kártya (ma elfogyasztott mikrotápanyagok az ajánlott napi bevitellel)
        MicroNutrientCard(totals = state.todayTotals)

        // Víz panel – teljes szélességű, nagy, +250 / -250 ml gombok
        WaterCard(
            water = state.water,
            waterGoal = state.waterGoal,
            onAdd = { viewModel.changeWater(250) },
            onRemove = { viewModel.changeWater(-250) }
        )

        // Lépés kártya (teljes szélesség)
        BentoCard(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dashboard_steps),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.steps_progress, steps, state.stepGoal),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Ha kell engedély a szenzorhoz, gomb kéri be
                if (viewModel.stepSensorAvailable && !hasStepPermission) {
                    FilledTonalButton(onClick = {
                        permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }) {
                        Text(stringResource(R.string.enable))
                    }
                }
            }
        }

        // Mai étkezés-napló
        FoodLogCard(
            entries = state.todayEntries,
            onAdd = { voiceInitialText = ""; showAddFood = true },
            onVoiceLog = { startVoiceInput() },
            onDelete = { viewModel.deleteFood(it) },
            onSwap = { entry -> swapEntry = entry }
        )

        // Heti kalória oszlopdiagram
        WeeklyCaloriesCard(
            data = weekCalories,
            goal = state.calorieGoal
        )

        // Testsúly trend (dátumos bejegyzések + grafikon felül)
        WeightCard(
            weights = weights,
            massUnit = massUnit,
            onAdd = { showWeightDialog = true },
            onDelete = { date -> viewModel.deleteWeight(date) }
        )

        // Szezonális ételek az aktuális hónapban, a választott régió szerint
        SeasonalCard(region = seasonalRegion)

        // Böjt-időzítő (időszakos böjt, pl. 16:8)
        FastingCard(
            fastingStartMs = state.fastingStartMs,
            onStart = { viewModel.startFasting() },
            onStop = { viewModel.stopFasting() }
        )
    }

    // Étel hozzáadása dialógus (kézi, gyakori, vagy vonalkódról adagolással)
    if (showAddFood) {
        AddFoodDialog(
            recentFoods = recentFoods,
            scanned = scannedProduct,
            initialText = voiceInitialText,
            localFoodSearch = { query -> viewModel.searchLocalFoods(query) },
            onScan = {
                showAddFood = false
                openScanner()
            },
            onSubmit = { name, kcal, p, c, f, meal, fiber, vitC, iron, calcium, vitD, b12, mg ->
                viewModel.addFood(name, kcal, p, c, f, meal, fiber, vitC, iron, calcium, vitD, b12, mg)
                viewModel.consumeScan()
                voiceInitialText = ""
                showAddFood = false
            },
            onDismiss = {
                viewModel.consumeScan()
                voiceInitialText = ""
                showAddFood = false
            }
        )
    }

    // Vonalkód-olvasó réteg a naplóhoz
    if (showScanner) {
        BarcodeScannerOverlay(
            onBarcode = { code ->
                showScanner = false
                viewModel.lookupBarcode(code)
            },
            onClose = { showScanner = false }
        )
    }

    // Testsúly megadása (dátummal, custom billentyűzet)
    if (showWeightDialog) {
        WeightDialog(
            previous = weights.lastOrNull(),
            massUnit = massUnit,
            onSubmit = { kg, date ->
                viewModel.addWeight(kg, date)
                showWeightDialog = false
            },
            onDismiss = { showWeightDialog = false }
        )
    }

    // Csere-javaslat dialógus
    swapEntry?.let { entry ->
        SwapFoodDialog(
            entry = entry,
            alternatives = remember(entry) { viewModel.findSimilarFoods(entry.calories, entry.protein) },
            onDismiss = { swapEntry = null }
        )
    }
    }
}

// ── Víz panel (teljes szélesség, ~25% magasság) ─────────────────────────────

@Composable
private fun WaterCard(
    water: Int,
    waterGoal: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val fraction = if (waterGoal > 0) (water.toFloat() / waterGoal).coerceIn(0f, 1f) else 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fejléc
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = FatColor, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_water),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$water / $waterGoal ml",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FatColor
                )
            }
            // Progress sáv
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(10.dp),
                color = FatColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            // Gombok egy sorban
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onAdd,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = FatColor.copy(alpha = 0.15f),
                        contentColor = FatColor
                    )
                ) {
                    Text(
                        "+250 ml",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                FilledTonalButton(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        "−250 ml",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ── Tápérték (makrók) – napi/heti váltóval, egymás alatti színes sorok ───────

private val CalorieColor = Color(0xFFC2622A)   // tompított narancs – kalória

@Composable
private fun MacroNutritionCard(
    state: DashboardUiState,
    weekCalories: List<Pair<java.time.LocalDate, Int>>,
    onOpenNutrition: () -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    val isWeekly = tabIndex == 1
    val weekKcal = weekCalories.sumOf { it.second }
    val goal = if (state.calorieGoal > 0) state.calorieGoal else 2000
    BentoCard(modifier = Modifier.fillMaxWidth().clickable { onOpenNutrition() }) {
        Text(
            text = stringResource(R.string.dashboard_macros),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }) {
                Text(stringResource(R.string.tab_daily), modifier = Modifier.padding(vertical = 8.dp))
            }
            Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }) {
                Text(stringResource(R.string.tab_weekly), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Spacer(Modifier.size(8.dp))
        // Kalória sor
        if (isWeekly) {
            MacroRow(
                label = stringResource(R.string.dashboard_calories_today),
                value = "$weekKcal kcal",
                fraction = if (goal > 0) (weekKcal.toFloat() / (goal * 7)).coerceIn(0f, 1f) else 0f,
                color = CalorieColor
            )
        } else {
            MacroRow(
                label = stringResource(R.string.dashboard_calories_today),
                value = "${state.todayTotals.calories} kcal",
                fraction = if (goal > 0) (state.todayTotals.calories.toFloat() / goal).coerceIn(0f, 1f) else 0f,
                color = CalorieColor
            )
        }
        MacroRow(
            label = stringResource(R.string.protein),
            value = "${(if (isWeekly) state.todayTotals.protein * 7 else state.todayTotals.protein).roundToInt()} g",
            fraction = fractionOf(state.todayTotals.protein * (if (isWeekly) 7.0 else 1.0), state.proteinTarget * if (isWeekly) 7 else 1),
            color = ProteinColor
        )
        MacroRow(
            label = stringResource(R.string.carbs),
            value = "${(if (isWeekly) state.todayTotals.carbs * 7 else state.todayTotals.carbs).roundToInt()} g",
            fraction = fractionOf(state.todayTotals.carbs * (if (isWeekly) 7.0 else 1.0), state.carbsTarget * if (isWeekly) 7 else 1),
            color = CarbsColor
        )
        MacroRow(
            label = stringResource(R.string.fat),
            value = "${(if (isWeekly) state.todayTotals.fat * 7 else state.todayTotals.fat).roundToInt()} g",
            fraction = fractionOf(state.todayTotals.fat * (if (isWeekly) 7.0 else 1.0), state.fatTarget * if (isWeekly) 7 else 1),
            color = FatColor
        )
        // Tányér-összetétel: a mai kalóriák makró-eloszlása (cél 30/40/30)
        if (!isWeekly && state.todayTotals.calories > 0) {
            PlateCompositionRow(state.todayTotals)
        }
    }
}

/**
 * "Tányér-validátor": a ténylegesen elfogyasztott kalóriák fehérje/szénhidrát/zsír
 * arányát mutatja a javasolt 30/40/30 eloszláshoz képest.
 */
@Composable
private fun PlateCompositionRow(totals: NutritionTotals) {
    val proteinKcal = totals.protein * 4.0
    val carbsKcal = totals.carbs * 4.0
    val fatKcal = totals.fat * 9.0
    val sum = proteinKcal + carbsKcal + fatKcal
    if (sum <= 0) return
    val pPct = (proteinKcal / sum * 100).roundToInt()
    val cPct = (carbsKcal / sum * 100).roundToInt()
    val fPct = (fatKcal / sum * 100).roundToInt()

    Spacer(Modifier.size(10.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    Spacer(Modifier.size(8.dp))
    Text(
        text = stringResource(R.string.plate_composition),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.size(6.dp))
    // Egyetlen, arányosan kitöltött sáv a három makróval
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
    ) {
        if (pPct > 0) Box(Modifier.weight(pPct.toFloat()).fillMaxHeight().background(ProteinColor))
        if (cPct > 0) Box(Modifier.weight(cPct.toFloat()).fillMaxHeight().background(CarbsColor))
        if (fPct > 0) Box(Modifier.weight(fPct.toFloat()).fillMaxHeight().background(FatColor))
    }
    Spacer(Modifier.size(8.dp))
    PlatePctLine(stringResource(R.string.protein), pPct, 30, ProteinColor)
    PlatePctLine(stringResource(R.string.carbs), cPct, 40, CarbsColor)
    PlatePctLine(stringResource(R.string.fat), fPct, 30, FatColor)
}

@Composable
private fun PlatePctLine(label: String, actualPct: Int, targetPct: Int, color: Color) {
    val off = kotlin.math.abs(actualPct - targetPct) > 10
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, shape = RoundedCornerShape(2.dp)))
        Spacer(Modifier.size(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$actualPct%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = if (off) MaterialTheme.colorScheme.error else color
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = "/ $targetPct%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MacroRow(label: String, value: String, fraction: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, shape = RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(5.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

// ── Csere-javaslat dialógus ──────────────────────────────────────────────────

/** Hasonló tápértékű ételek javaslata a naplózott étel alternatíváiként. */
@Composable
private fun SwapFoodDialog(
    entry: FoodLogEntry,
    alternatives: List<LocalFood>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.swap_food)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.swap_desc, entry.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                alternatives.forEach { food ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(food.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                "${food.kcal} kcal · F${food.protein.roundToInt()} Sz${food.carbs.roundToInt()} Zs${food.fat.roundToInt()} g/100g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
        }
    )
}

// ── Heti kalória diagram ──────────────────────────────────────────────────────

/** Heti kalória oszlopdiagram kártya (utolsó 7 nap, cél-vonallal). */
@Composable
private fun WeeklyCaloriesCard(data: List<Pair<java.time.LocalDate, Int>>, goal: Int) {
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.weekly_calories),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (data.all { it.second == 0 }) {
            Text(
                text = stringResource(R.string.food_log_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            SimpleBarChart(
                values = data.map { it.second.toFloat() },
                labels = data.map {
                    it.first.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
                },
                goal = if (goal > 0) goal.toFloat() else null
            )
        }
    }
}

// ── Mikrotápanyag kártya ──────────────────────────────────────────────────────

private val FiberColor   = Color(0xFF1A9447)   // erdőzöld
private val VitCColor    = Color(0xFFC2622A)   // tompított narancs
private val IronColor    = Color(0xFFB83535)   // mély rózsapiros
private val CalciumColor = Color(0xFF2D61C2)   // mély kék
private val VitDColor    = Color(0xFFB87A10)   // mély arany
private val B12Color     = Color(0xFF6A45C2)   // mély lila
private val MagColor     = Color(0xFF0F8C7E)   // mély türkiz

@Composable
private fun MicroNutrientCard(totals: NutritionTotals) {
    val rdi = NutritionTotals.DAILY_RDI
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.micro_nutrients),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        MicroRow(stringResource(R.string.fiber),      totals.fiberG,     rdi.fiberG,     "g",  FiberColor)
        MicroRow(stringResource(R.string.vitamin_c),  totals.vitaminCMg, rdi.vitaminCMg, "mg", VitCColor)
        MicroRow(stringResource(R.string.iron),       totals.ironMg,     rdi.ironMg,     "mg", IronColor)
        MicroRow(stringResource(R.string.calcium),    totals.calciumMg,  rdi.calciumMg,  "mg", CalciumColor)
        MicroRow(stringResource(R.string.vitamin_d),  totals.vitaminDUg, rdi.vitaminDUg, "μg", VitDColor)
        MicroRow(stringResource(R.string.vitamin_b12),totals.b12Ug,      rdi.b12Ug,      "μg", B12Color)
        MicroRow(stringResource(R.string.magnesium),  totals.magnesiumMg,rdi.magnesiumMg,"mg", MagColor)
    }
}

@Composable
private fun MicroRow(label: String, value: Double, rdiVal: Double, unit: String, color: Color) {
    val fraction = if (rdiVal > 0) (value / rdiVal).toFloat().coerceIn(0f, 1f) else 0f
    val low = value > 0 && fraction < 0.5f
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp)))
            Spacer(Modifier.size(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (low) {
                Text(
                    text = "▲",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.size(4.dp))
            }
            Text(
                text = "${formatMicro(value)} / ${formatMicro(rdiVal)} $unit",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

private fun formatMicro(value: Double): String =
    if (value < 10.0) String.format("%.1f", value) else value.toInt().toString()

// ── Testsúly kártya + dialógus ────────────────────────────────────────────────

/** Testsúly kártya: grafikon felül, dátumos bejegyzések lista, törlés hosszú nyomásra. */
@Composable
private fun WeightCard(
    weights: List<com.nutriplan.app.domain.model.WeightEntry>,
    massUnit: MassUnit,
    onAdd: () -> Unit,
    onDelete: (LocalDate) -> Unit
) {
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.weight_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            weights.lastOrNull()?.let {
                Text(
                    text = UnitFormatter.formatMass(it.weightKg, massUnit),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.weight_add))
            }
        }
        // Grafikon (ha van legalább 2 adat)
        if (weights.size >= 2) {
            SimpleLineChart(points = weights.takeLast(30).map { it.weightKg.toFloat() })
            Spacer(Modifier.size(8.dp))
        } else {
            Text(
                text = stringResource(R.string.weight_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Bejegyzések listája (legutóbbi 5, legújabb elöl): dátum fejléc, alatta a súly
        val recent = weights.sortedByDescending { it.date }.take(5)
        if (recent.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            recent.forEachIndexed { idx, entry ->
                val prev = recent.getOrNull(idx + 1)
                val diff = prev?.let { entry.weightKg - it.weightKg }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    // Dátum fejléc-sor (akcentus színnel) + törlés
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDelete(entry.date) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    // Súly nagyobb betűvel, mellette a változás
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = UnitFormatter.formatMass(entry.weightKg, massUnit),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        diff?.let {
                            val arrow = if (it < 0) "▼" else "▲"
                            val color = if (it < 0) ProteinColor else MaterialTheme.colorScheme.error
                            Text(
                                text = "$arrow ${UnitFormatter.formatMassNumber(kotlin.math.abs(it), massUnit)} ${UnitFormatter.massLabel(massUnit)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = color,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (idx < recent.lastIndex) HorizontalDivider()
            }
        }
    }
}

/** Testsúly megadó dialógus – egyedi billentyűzet, dátum választó, előző érték. */
@Composable
private fun WeightDialog(
    previous: com.nutriplan.app.domain.model.WeightEntry?,
    massUnit: MassUnit,
    onSubmit: (Double, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val unitLabel = UnitFormatter.massLabel(massUnit)
    var value by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = remember { LocalDate.now() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.weight_add), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                // Előző érték referencia
                previous?.let {
                    Text(
                        text = "${stringResource(R.string.weight_previous)}: ${UnitFormatter.formatMass(it.weightKg, massUnit)}  (${it.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Dátum picker (ma / tegnap / válasz)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(today to stringResource(R.string.date_today), today.minusDays(1) to stringResource(R.string.date_yesterday)).forEach { (date, label) ->
                        OutlinedButton(
                            onClick = { selectedDate = date },
                            modifier = Modifier.weight(1f),
                            colors = if (selectedDate == date)
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else ButtonDefaults.outlinedButtonColors()
                        ) { Text(label, fontSize = 12.sp) }
                    }
                }
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Kijelző
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (value.isEmpty()) "– $unitLabel" else "$value $unitLabel",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Egyedi billentyűzet
                NumericKeypad(
                    onDigit = { d ->
                        if (d == "." && value.contains(".")) return@NumericKeypad
                        if (value.length < 6) value += d
                    },
                    onBackspace = { if (value.isNotEmpty()) value = value.dropLast(1) },
                    onConfirm = {
                        val entered = value.toDoubleOrNull() ?: 0.0
                        val kg = UnitFormatter.massToKg(entered, massUnit)
                        if (kg > 0) onSubmit(kg, selectedDate)
                    },
                    allowDecimal = true,
                    confirmEnabled = (value.toDoubleOrNull() ?: 0.0) > 0,
                    accentColor = MaterialTheme.colorScheme.primary
                )

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

/** A mai naplóbejegyzések kártyája hozzáadás, hangbevitel, törlés és csere gombbal. */
@Composable
private fun FoodLogCard(
    entries: List<FoodLogEntry>,
    onAdd: () -> Unit,
    onVoiceLog: () -> Unit,
    onDelete: (Long) -> Unit,
    onSwap: (FoodLogEntry) -> Unit = {}
) {
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(8.dp))
            Text(
                text = stringResource(R.string.food_log_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onVoiceLog) {
                Icon(Icons.Filled.Mic, contentDescription = stringResource(R.string.voice_log),
                    tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.log_food))
            }
        }
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.food_log_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            entries.forEachIndexed { index, e ->
                if (index > 0) HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = e.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${e.calories} ${stringResource(R.string.kcal_unit)} · " +
                                "F ${e.protein.roundToInt()} · Sz ${e.carbs.roundToInt()} · Zs ${e.fat.roundToInt()} g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onSwap(e) }) {
                        Icon(
                            Icons.Filled.SwapHoriz,
                            contentDescription = stringResource(R.string.swap_food),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = { onDelete(e.id) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Étel naplózó dialógus: kézi megadás, gyakori ételek gyorsválasztása, illetve
 * vonalkódról betöltött 100 g-os érték, amelyet a megadott gramm szerint skáláz.
 */
@Composable
private fun AddFoodDialog(
    recentFoods: List<FoodLogEntry>,
    scanned: com.nutriplan.app.data.remote.ScannedProduct?,
    initialText: String = "",
    localFoodSearch: (String) -> List<LocalFood>,
    onScan: () -> Unit,
    onSubmit: (String, Int, Double, Double, Double, MealType, Double, Double, Double, Double, Double, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealType.LUNCH) }
    var basis by remember { mutableStateOf<com.nutriplan.app.data.remote.ScannedProduct?>(null) }
    var localQuery by remember { mutableStateOf("") }
    val localResults = remember(localQuery) { localFoodSearch(localQuery) }
    // Mikrotápanyagok (opcionális, 0 = nincs adat)
    var fiberG by remember { mutableStateOf(0.0) }
    var vitaminCMg by remember { mutableStateOf(0.0) }
    var ironMg by remember { mutableStateOf(0.0) }
    var calciumMg by remember { mutableStateOf(0.0) }
    var vitaminDUg by remember { mutableStateOf(0.0) }
    var b12Ug by remember { mutableStateOf(0.0) }
    var magnesiumMg by remember { mutableStateOf(0.0) }

    fun applyGrams(g: Double) {
        val b = basis ?: return
        val factor = g / 100.0
        calories = (b.caloriesPer100g * factor).roundToInt().toString()
        protein = (b.proteinPer100g * factor).roundToInt().toString()
        carbs = (b.carbsPer100g * factor).roundToInt().toString()
        fat = (b.fatPer100g * factor).roundToInt().toString()
        fiberG = b.fiberPer100g * factor
        vitaminCMg = b.vitaminCPer100gMg * factor
        ironMg = b.ironPer100gMg * factor
        calciumMg = b.calciumPer100gMg * factor
        vitaminDUg = b.vitaminDPer100gUg * factor
        b12Ug = b.b12Per100gUg * factor
        magnesiumMg = b.magnesiumPer100gMg * factor
    }

    // Beolvasott termék betöltése (100 g-os alapként)
    LaunchedEffect(scanned) {
        if (scanned != null) {
            basis = scanned
            if (name.isBlank()) name = scanned.name
            grams = "100"
            applyGrams(100.0)
        }
    }

    // Hangbevitel feldolgozása: gramm kinyerése + helyi adatbázis-keresés
    LaunchedEffect(initialText) {
        if (initialText.isBlank()) return@LaunchedEffect
        val gramsRegex = Regex("""(\d+)\s*(?:g(?:ramm?)?|dkg?|kg)""", RegexOption.IGNORE_CASE)
        val gramsMatch = gramsRegex.find(initialText)
        val parsedGrams = gramsMatch?.groupValues?.get(1)?.toDoubleOrNull()
        val cleanedName = initialText.replace(gramsRegex, "").trim()
        val lookupText = if (cleanedName.isNotBlank()) cleanedName else initialText
        val match = localFoodSearch(lookupText).firstOrNull()
        if (match != null) {
            val prod = match.toScannedProduct()
            basis = prod
            name = match.name
            val g = parsedGrams ?: 100.0
            grams = g.toInt().toString()
            applyGrams(g)
        } else {
            name = initialText
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.log_food)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onScan, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Text("  ${stringResource(R.string.scan_barcode)}")
                }
                // Gyakori ételek gyorsválasztása
                if (recentFoods.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recentFoods.take(8).forEach { food ->
                            AssistChip(
                                onClick = {
                                    basis = null
                                    name = food.name
                                    grams = ""
                                    calories = food.calories.toString()
                                    protein = food.protein.roundToInt().toString()
                                    carbs = food.carbs.roundToInt().toString()
                                    fat = food.fat.roundToInt().toString()
                                    mealType = food.mealType
                                },
                                label = { Text(food.name, maxLines = 1) }
                            )
                        }
                    }
                }
                // Helyi ételek (HU/RO adatbázis)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = stringResource(R.string.local_foods),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = localQuery,
                    onValueChange = { localQuery = it },
                    placeholder = { Text(stringResource(R.string.search_foods)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (localResults.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        localResults.forEach { food ->
                            AssistChip(
                                onClick = {
                                    val prod = food.toScannedProduct()
                                    basis = prod
                                    name = food.name
                                    grams = "100"
                                    applyGrams(100.0)
                                },
                                label = { Text("${food.name} (${food.kcal})", maxLines = 1) }
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.food_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (basis != null) {
                    OutlinedTextField(
                        value = grams,
                        onValueChange = {
                            grams = it.filter { c -> c.isDigit() }.take(4)
                            applyGrams(grams.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text(stringResource(R.string.grams)) },
                        suffix = { Text("g") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.calories), calories, { calories = it; basis = null }, Modifier.weight(1f))
                    NumberField(stringResource(R.string.protein), protein, { protein = it; basis = null }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.carbs), carbs, { carbs = it; basis = null }, Modifier.weight(1f))
                    NumberField(stringResource(R.string.fat), fat, { fat = it; basis = null }, Modifier.weight(1f))
                }
                LabeledDropdown(
                    label = stringResource(R.string.meal_type),
                    selected = mealType,
                    options = MealType.entries,
                    optionLabel = { it.label() },
                    onSelected = { mealType = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSubmit(
                    name,
                    calories.toIntOrNull() ?: 0,
                    protein.toDoubleOrNull() ?: 0.0,
                    carbs.toDoubleOrNull() ?: 0.0,
                    fat.toDoubleOrNull() ?: 0.0,
                    mealType,
                    fiberG, vitaminCMg, ironMg, calciumMg, vitaminDUg, b12Ug, magnesiumMg
                )
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** Rövid numerikus mező a makró-bevitelhez. */
@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

/** Arány számítása nullával szembeni védelemmel. */
private fun fractionOf(value: Double, target: Int): Float =
    if (target > 0) (value / target).toFloat() else 0f

/** Makró-érték rövid szövege: "aktuális/cél g". */
private fun macroValue(value: Double, target: Int): String =
    "${value.roundToInt()}/$target g"

/** Egységes, erősen lekerekített Bento kártya. */
@Composable
private fun BentoCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

// ── Böjt-időzítő kártya ──────────────────────────────────────────────────────

/** Időszakos böjt (IF) időzítő kártya. 16h böjt / 8h étkezési ablak az alapértelmezett. */
@Composable
private fun FastingCard(
    fastingStartMs: Long,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val isFasting = fastingStartMs > 0L
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(isFasting) {
        if (isFasting) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                nowMs = System.currentTimeMillis()
            }
        }
    }

    val fastWindowMs = 16L * 3600_000L
    val elapsedMs = if (isFasting) (nowMs - fastingStartMs).coerceAtLeast(0L) else 0L
    val remainingMs = (fastWindowMs - elapsedMs).coerceAtLeast(0L)
    val fraction = if (isFasting) (elapsedMs.toFloat() / fastWindowMs).coerceIn(0f, 1f) else 0f
    val fastDone = isFasting && elapsedMs >= fastWindowMs

    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.fasting_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (!isFasting) stringResource(R.string.fasting_idle)
                    else if (fastDone) stringResource(R.string.fasting_complete)
                    else stringResource(R.string.fasting_active, formatHms(elapsedMs), formatHms(remainingMs)),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (fastDone) ProteinColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isFasting) {
                FilledTonalButton(
                    onClick = onStop,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) { Text(stringResource(R.string.fasting_stop)) }
            } else {
                FilledTonalButton(
                    onClick = onStart,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ProteinColor.copy(alpha = 0.15f),
                        contentColor = ProteinColor
                    )
                ) { Text(stringResource(R.string.fasting_start)) }
            }
        }
        if (isFasting) {
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (fastDone) ProteinColor else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = stringResource(R.string.fasting_window, "16:8"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatHms(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format("%dh %02dm", h, m) else String.format("%dm %02ds", m, s)
}

// ── Szezonális ételek kártya ──────────────────────────────────────────────────

@Composable
private fun SeasonalCard(region: SeasonalRegion) {
    val month = remember { LocalDate.now().monthValue }
    val foods = remember(region, month) { region.produceFor(month) }
    if (foods.isEmpty()) return
    BentoCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.seasonal_foods),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.seasonal_tip),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            foods.forEach { food ->
                AssistChip(
                    onClick = {},
                    label = { Text(food, maxLines = 1) }
                )
            }
        }
    }
}

/**
 * Egysoros makró-jelmagyarázat: színes pötty + címke + érték.
 * A címke és az érték a makró saját színét kapja, és garantáltan egy sorban marad.
 */
@Composable
private fun MacroLegend(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color)
            }
        }
        Spacer(Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
