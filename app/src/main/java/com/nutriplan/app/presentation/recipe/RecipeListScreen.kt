package com.nutriplan.app.presentation.recipe

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.MealType
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
    val favoritesOnly by viewModel.favoritesOnly.collectAsStateWithLifecycle()
    val mealFilter by viewModel.mealFilter.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    // A megnyitott (kibontott) recept – ha nem null, a részletes nézet látszik
    var selected by remember { mutableStateOf<Recipe?>(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val importOkMsg = stringResource(R.string.recipe_import_ok)
    val importFailMsg = stringResource(R.string.recipe_import_fail)
    LaunchedEffect(Unit) {
        viewModel.importResult.collect { ok ->
            snackbarHostState.showSnackbar(if (ok) importOkMsg else importFailMsg)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importFromUri(it) } }

    // Egy recept megosztása JSON-szövegként
    fun shareRecipe(recipe: Recipe) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, recipe.name)
            putExtra(Intent.EXTRA_TEXT, viewModel.shareText(recipe))
        }
        context.startActivity(Intent.createChooser(sendIntent, recipe.name))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recipes_title)) },
                actions = {
                    if (selected == null) {
                        IconButton(onClick = {
                            importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        }) {
                            Icon(
                                Icons.Filled.FileUpload,
                                contentDescription = stringResource(R.string.import_recipe)
                            )
                        }
                    }
                }
            )
        },
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text(stringResource(R.string.search_hint)) }
                )
                RecipeFilterBar(
                    favoritesOnly = favoritesOnly,
                    mealFilter = mealFilter,
                    sort = sort,
                    onToggleFavorites = viewModel::toggleFavoritesOnly,
                    onMealFilter = viewModel::setMealFilter,
                    onToggleSort = {
                        viewModel.setSort(
                            if (sort == RecipeSort.NAME) RecipeSort.CALORIES else RecipeSort.NAME
                        )
                    }
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
                                        onToggleFavorite = { viewModel.toggleFavorite(recipe) },
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
                            onShare = { shareRecipe(sel) },
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
    onToggleFavorite: () -> Unit,
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
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(R.string.favorite),
                    tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
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

/** Szűrősáv: kedvencek, étkezéstípus és rendezés. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeFilterBar(
    favoritesOnly: Boolean,
    mealFilter: MealType?,
    sort: RecipeSort,
    onToggleFavorites: () -> Unit,
    onMealFilter: (MealType?) -> Unit,
    onToggleSort: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = favoritesOnly,
            onClick = onToggleFavorites,
            label = { Text(stringResource(R.string.favorites)) },
            leadingIcon = {
                Icon(
                    if (favoritesOnly) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null
                )
            }
        )
        FilterChip(
            selected = false,
            onClick = onToggleSort,
            label = {
                Text(
                    stringResource(
                        if (sort == RecipeSort.NAME) R.string.sort_by_name else R.string.sort_by_calories
                    )
                )
            },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) }
        )
        MealType.entries.forEach { type ->
            FilterChip(
                selected = mealFilter == type,
                onClick = { onMealFilter(if (mealFilter == type) null else type) },
                label = { Text(type.label()) }
            )
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
    onShare: () -> Unit,
    sharedScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val sharedModifier = with(sharedScope) {
        Modifier.sharedBounds(
            rememberSharedContentState(key = "recipe-${recipe.id}"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
    // Adagolás: a hozzávalók és a tápérték ezzel a szorzóval skálázódnak
    var servings by remember(recipe.id) { mutableStateOf(1) }
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
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_recipe))
                }
            }
            Text(
                text = recipe.mealType.label(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            // Adagolás (porció) léptető
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.servings),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { if (servings > 1) servings-- }, enabled = servings > 1) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }
                Text(
                    text = "$servings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { if (servings < 20) servings++ }, enabled = servings < 20) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
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
                totals = NutritionTotals(
                    recipe.calories * servings,
                    recipe.protein * servings,
                    recipe.carbs * servings,
                    recipe.fat * servings
                )
            )
            HorizontalDivider()
            Text(
                text = stringResource(R.string.ingredients),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            recipe.ingredients.forEach { ingredient ->
                Text(
                    text = "• ${ingredient.displayName()} – ${formatQuantity(ingredient.quantity * servings)} ${ingredient.unit.label()}",
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
