package com.nutriplan.app.presentation.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.MealAssignment
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.NutritionTotals
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.presentation.components.NutritionSummary
import com.nutriplan.app.presentation.util.displayName
import com.nutriplan.app.presentation.util.label

/**
 * Heti tervező képernyő – napokra és étkezésekre bontva.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(viewModel: PlannerViewModel = hiltViewModel()) {
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val recipes by viewModel.allRecipes.collectAsStateWithLifecycle()
    val dayTotals by viewModel.dayTotals.collectAsStateWithLifecycle()

    var showClearDialog by remember { mutableStateOf(false) }
    // A recept-választó állapota: melyik naphoz és étkezéshez választunk
    var picker by remember { mutableStateOf<Pair<WeekDay, MealType>?>(null) }
    // A nap-másoló forrásnapja (ha nem null, a cél-választó látszik)
    var copySource by remember { mutableStateOf<WeekDay?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.planner_title)) },
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = stringResource(R.string.clear_week))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(WeekDay.entries.toList(), key = { it.key }) { day ->
                DayCard(
                    day = day,
                    assignments = assignments.filter { it.weekDay == day },
                    totals = dayTotals[day] ?: NutritionTotals(),
                    onAdd = { mealType -> picker = day to mealType },
                    onRemove = { viewModel.remove(it) },
                    onCopy = { copySource = day }
                )
            }
        }
    }

    // Recept-választó párbeszéd
    picker?.let { (day, mealType) ->
        RecipePickerDialog(
            mealType = mealType,
            recipes = recipes.filter { it.mealType == mealType },
            onSelect = { recipe ->
                viewModel.assign(day, mealType, recipe.id)
                picker = null
            },
            onDismiss = { picker = null }
        )
    }

    // Nap-másoló cél-választó párbeszéd
    copySource?.let { source ->
        CopyDayDialog(
            source = source,
            onCopyTo = { target ->
                viewModel.copyDay(source, target)
                copySource = null
            },
            onCopyToAll = {
                viewModel.copyDayToAll(source)
                copySource = null
            },
            onDismiss = { copySource = null }
        )
    }

    // Hét törlése megerősítés
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_week)) },
            text = { Text(stringResource(R.string.clear_week_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearWeek()
                    showClearDialog = false
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/** Egy nap kártyája, lenyitható étkezés-listával. */
@Composable
private fun DayCard(
    day: WeekDay,
    assignments: List<MealAssignment>,
    totals: NutritionTotals,
    onAdd: (MealType) -> Unit,
    onRemove: (Long) -> Unit,
    onCopy: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.label(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${totals.calories} ${stringResource(R.string.kcal_unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Nap másolása másik napra
                IconButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.copy_day))
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    MealType.entries.forEach { mealType ->
                        MealSlot(
                            mealType = mealType,
                            assignments = assignments.filter { it.mealType == mealType },
                            onAdd = { onAdd(mealType) },
                            onRemove = onRemove
                        )
                    }
                    NutritionSummary(
                        totals = totals,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

/** Egy étkezés idősáv a nap kártyáján belül. */
@Composable
private fun MealSlot(
    mealType: MealType,
    assignments: List<MealAssignment>,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mealType.label(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_meal))
            }
        }
        assignments.forEach { assignment ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${assignment.recipe.displayName()} (${assignment.recipe.calories} ${stringResource(R.string.kcal_unit)})",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onRemove(assignment.id) }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** Nap-másoló párbeszéd: a forrásnap étkezéseit átmásolja a kiválasztott napra (vagy minden napra). */
@Composable
private fun CopyDayDialog(
    source: WeekDay,
    onCopyTo: (WeekDay) -> Unit,
    onCopyToAll: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${stringResource(R.string.copy_day)} · ${source.label()}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.copy_to_all_days),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopyToAll() }
                        .padding(vertical = 12.dp)
                )
                HorizontalDivider()
                WeekDay.entries.filter { it != source }.forEach { target ->
                    Text(
                        text = target.label(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCopyTo(target) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** Recept-választó párbeszéd egy adott étkezéshez. */
@Composable
private fun RecipePickerDialog(
    mealType: MealType,
    recipes: List<Recipe>,
    onSelect: (Recipe) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${stringResource(R.string.select_recipe)} · ${mealType.label()}") },
        text = {
            if (recipes.isEmpty()) {
                Text(stringResource(R.string.no_recipes_for_meal))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(recipes, key = { it.id }) { recipe ->
                        Text(
                            text = "${recipe.displayName()} · ${recipe.calories} ${stringResource(R.string.kcal_unit)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(recipe) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
