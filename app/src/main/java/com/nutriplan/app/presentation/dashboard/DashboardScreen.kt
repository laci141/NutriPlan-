package com.nutriplan.app.presentation.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutriplan.app.R
import com.nutriplan.app.presentation.components.ActivityRings
import com.nutriplan.app.presentation.components.CalorieRing
import com.nutriplan.app.presentation.components.RingData
import java.time.LocalTime

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
    val context = LocalContext.current

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
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/ $goal ${stringResource(R.string.kcal_unit)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.left_short, left),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
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
                // Gyűrűk + jelmagyarázat egymás mellett, hogy elférjen a keskeny kártyán
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActivityRings(
                        rings = listOf(
                            RingData(fractionOf(state.todayTotals.protein, state.proteinTarget), ProteinColor),
                            RingData(fractionOf(state.todayTotals.carbs, state.carbsTarget), CarbsColor),
                            RingData(fractionOf(state.todayTotals.fat, state.fatTarget), FatColor)
                        ),
                        modifier = Modifier.fillMaxHeight().aspectRatio(1f)
                    )
                    // A három makró egymás alatt, külön színnel, soronként egy sorban
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    Text(
                        text = stringResource(R.string.ml_progress, state.water, state.waterGoal),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
    }
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
