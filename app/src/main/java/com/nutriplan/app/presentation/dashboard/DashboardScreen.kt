package com.nutriplan.app.presentation.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.FoodLogEntry
import com.nutriplan.app.domain.model.MealType
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

// A makró-gyűrűk színei (egészséget sugárzó tónusok)
private val ProteinColor = Color(0xFF34D399) // almazöld
private val CarbsColor = Color(0xFFF59E0B)   // narancs
private val FatColor = Color(0xFF3B82F6)      // élénk kék

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
    val context = LocalContext.current

    var showAddFood by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }

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
                fontWeight = FontWeight.Bold
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

        // Két kisebb kártya egymás mellett: Makrók | Víz
        Row(
            modifier = Modifier.fillMaxWidth().height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Makró aktivitási gyűrűk
            BentoCard(
                modifier = Modifier.weight(1f).fillMaxSize().clickable { onOpenNutrition() }
            ) {
                Text(
                    text = stringResource(R.string.dashboard_macros),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                // Gyűrűk felül, középen
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    ActivityRings(
                        rings = listOf(
                            RingData(fractionOf(state.todayTotals.protein, state.proteinTarget), ProteinColor),
                            RingData(fractionOf(state.todayTotals.carbs, state.carbsTarget), CarbsColor),
                            RingData(fractionOf(state.todayTotals.fat, state.fatTarget), FatColor)
                        ),
                        modifier = Modifier.fillMaxHeight().aspectRatio(1f),
                        strokeWidth = 7.dp,
                        gap = 4.dp
                    )
                }
                // A három makró egymás alatt, teljes szélességben, soronként egy sorban, külön színnel
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MacroLegend(
                        label = stringResource(R.string.protein),
                        value = macroValue(state.todayTotals.protein, state.proteinTarget),
                        color = ProteinColor
                    )
                    MacroLegend(
                        label = stringResource(R.string.carbs),
                        value = macroValue(state.todayTotals.carbs, state.carbsTarget),
                        color = CarbsColor
                    )
                    MacroLegend(
                        label = stringResource(R.string.fat),
                        value = macroValue(state.todayTotals.fat, state.fatTarget),
                        color = FatColor
                    )
                }
            }

            // Víz kártya +250 ml gombbal
            BentoCard(modifier = Modifier.weight(1f).fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = FatColor)
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = stringResource(R.string.dashboard_water),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.water}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "/ ${state.waterGoal} ml",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                FilledTonalButton(
                    onClick = viewModel::addWater,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.water_add))
                }
            }
        }

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
            onAdd = { showAddFood = true },
            onDelete = { viewModel.deleteFood(it) }
        )

        // Heti kalória oszlopdiagram
        WeeklyCaloriesCard(
            data = weekCalories,
            goal = state.calorieGoal
        )

        // Testsúly trend
        WeightCard(
            weights = weights,
            onAdd = { showWeightDialog = true }
        )
    }

    // Étel hozzáadása dialógus (kézi, gyakori, vagy vonalkódról adagolással)
    if (showAddFood) {
        AddFoodDialog(
            recentFoods = recentFoods,
            scanned = scannedProduct,
            onScan = {
                showAddFood = false
                openScanner()
            },
            onSubmit = { name, kcal, p, c, f, meal ->
                viewModel.addFood(name, kcal, p, c, f, meal)
                viewModel.consumeScan()
                showAddFood = false
            },
            onDismiss = {
                viewModel.consumeScan()
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

    // Testsúly megadása
    if (showWeightDialog) {
        WeightDialog(
            current = weights.lastOrNull()?.weightKg,
            onSubmit = { kg ->
                viewModel.addWeight(kg)
                showWeightDialog = false
            },
            onDismiss = { showWeightDialog = false }
        )
    }
    }
}

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

/** Testsúly trend kártya vonaldiagrammal és hozzáadás gombbal. */
@Composable
private fun WeightCard(weights: List<com.nutriplan.app.domain.model.WeightEntry>, onAdd: () -> Unit) {
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
                    text = "${it.weightKg} kg",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.weight_add))
            }
        }
        if (weights.size >= 2) {
            SimpleLineChart(points = weights.map { it.weightKg.toFloat() })
        } else {
            Text(
                text = stringResource(R.string.weight_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Testsúly megadó dialógus (kg). */
@Composable
private fun WeightDialog(current: Double?, onSubmit: (Double) -> Unit, onDismiss: () -> Unit) {
    var value by remember { mutableStateOf(current?.toString() ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.weight_add)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' }.take(6) },
                label = { Text(stringResource(R.string.weight_title)) },
                suffix = { Text("kg") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(value.toDoubleOrNull() ?: 0.0) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** A mai naplóbejegyzések kártyája hozzáadás és törlés gombbal. */
@Composable
private fun FoodLogCard(
    entries: List<FoodLogEntry>,
    onAdd: () -> Unit,
    onDelete: (Long) -> Unit
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
    onScan: () -> Unit,
    onSubmit: (String, Int, Double, Double, Double, MealType) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealType.LUNCH) }
    // 100 g-os alap a vonalkódról (skálázáshoz); null = kézi mód
    var basis by remember { mutableStateOf<com.nutriplan.app.data.remote.ScannedProduct?>(null) }

    fun applyGrams(g: Double) {
        val b = basis ?: return
        val factor = g / 100.0
        calories = (b.caloriesPer100g * factor).roundToInt().toString()
        protein = (b.proteinPer100g * factor).roundToInt().toString()
        carbs = (b.carbsPer100g * factor).roundToInt().toString()
        fat = (b.fatPer100g * factor).roundToInt().toString()
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
                    mealType
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
