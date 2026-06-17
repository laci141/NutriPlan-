package com.nutriplan.app.presentation.recipe

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.presentation.components.EmptyState
import com.nutriplan.app.presentation.components.NutritionSummary
import com.nutriplan.app.domain.model.NutritionTotals
import com.nutriplan.app.presentation.util.ConfirmDeleteDialog
import com.nutriplan.app.presentation.util.displayName
import com.nutriplan.app.presentation.util.formatQuantity
import com.nutriplan.app.presentation.util.label
import java.io.File

/**
 * Recept lista képernyő – keresés, részletek (megosztott elem animációval),
 * szerkesztés, törlés és új recept hozzáadása.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onEditRecipe: (Long) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    // A megnyitott (kibontott) recept – ha nem null, a részletes nézet látszik
    var selected by remember { mutableStateOf<Recipe?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.recipes_title)) }) },
        floatingActionButton = {
            // A FAB csak a listán látszik, a részletes nézetben nem
            if (selected == null) {
                FloatingActionButton(onClick = onAddRecipe) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_recipe))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // A keresőmező csak a listán látszik
            if (selected == null) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onSearchChanged,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.search_hint)) }
                )
            }

            // Megosztott elem (shared element) átmenet a lista és a részletes nézet között
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                val sharedScope = this
                AnimatedContent(
                    targetState = selected,
                    label = "recipeDetail"
                ) { sel ->
                    val avScope = this
                    if (sel == null) {
                        if (recipes.isEmpty()) {
                            EmptyState(message = stringResource(R.string.no_recipes))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recipes, key = { it.id }) { recipe ->
                                    RecipeCard(
                                        recipe = recipe,
                                        onClick = { selected = recipe },
                                        onDelete = { recipeToDelete = recipe },
                                        sharedScope = sharedScope,
                                        animatedVisibilityScope = avScope
                                    )
                                }
                            }
                        }
                    } else {
                        RecipeDetail(
                            recipe = sel,
                            onBack = { selected = null },
                            onEdit = {
                                val id = sel.id
                                selected = null
                                onEditRecipe(id)
                            },
                            sharedScope = sharedScope,
                            animatedVisibilityScope = avScope
                        )
                    }
                }
            }
        }
    }

    // Törlés megerősítő párbeszéd
    recipeToDelete?.let { recipe ->
        ConfirmDeleteDialog(
            message = stringResource(R.string.delete_recipe_message),
            onConfirm = {
                viewModel.deleteRecipe(recipe)
                recipeToDelete = null
            },
            onDismiss = { recipeToDelete = null }
        )
    }
}

/** Egy recept kártyája a listában (megosztott elem forrása). */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val sharedModifier = with(sharedScope) {
        Modifier.sharedBounds(
            rememberSharedContentState(key = "recipe-${recipe.id}"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
    Card(
        modifier = sharedModifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bélyegkép, ha a recepthez tartozik fotó
            recipe.imagePath?.let { path ->
                AsyncImage(
                    model = File(path),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = recipe.mealType.label(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${recipe.calories} ${stringResource(R.string.kcal_unit)} · " +
                        "${stringResource(R.string.protein)} ${formatQuantity(recipe.protein)}g · " +
                        "${stringResource(R.string.carbs)} ${formatQuantity(recipe.carbs)}g · " +
                        "${stringResource(R.string.fat)} ${formatQuantity(recipe.fat)}g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/** Részletes recept nézet, amely a kártyából animálva tágul ki (megosztott elem cél). */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun RecipeDetail(
    recipe: Recipe,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val sharedModifier = with(sharedScope) {
        Modifier.sharedBounds(
            rememberSharedContentState(key = "recipe-${recipe.id}"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
    Card(
        modifier = sharedModifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text(
                    text = recipe.displayName(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_recipe))
                }
            }
            Text(
                text = recipe.mealType.label(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            // Recept-fotó, ha van
            recipe.imagePath?.let { path ->
                AsyncImage(
                    model = File(path),
                    contentDescription = recipe.displayName(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            NutritionSummary(
                totals = NutritionTotals(recipe.calories, recipe.protein, recipe.carbs, recipe.fat)
            )
            HorizontalDivider()
            Text(
                text = stringResource(R.string.ingredients),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            recipe.ingredients.forEach { ingredient ->
                Text(
                    text = "• ${ingredient.displayName()} – ${formatQuantity(ingredient.quantity)} ${ingredient.unit.label()}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            // Elkészítési útmutató, ha van
            recipe.instructions?.takeIf { it.isNotBlank() }?.let { instructions ->
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.instructions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = instructions,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
