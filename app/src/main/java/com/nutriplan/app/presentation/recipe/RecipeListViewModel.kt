package com.nutriplan.app.presentation.recipe

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.usecase.DeleteRecipeUseCase
import com.nutriplan.app.domain.usecase.ImportRecipeUseCase
import com.nutriplan.app.domain.usecase.SearchRecipesUseCase
import com.nutriplan.app.domain.usecase.ShareRecipeUseCase
import com.nutriplan.app.domain.usecase.ToggleFavoriteUseCase
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** A recept lista rendezési módja. */
enum class RecipeSort { NAME, CALORIES }

/**
 * Recept lista ViewModel – keresés, szűrés (kedvenc, étkezés), rendezés, kedvencek és törlés.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val shareRecipeUseCase: ShareRecipeUseCase,
    private val importRecipeUseCase: ImportRecipeUseCase
) : ViewModel() {

    // Az importálás eredménye (siker/hiba) snackbarhoz
    private val _importResult = MutableSharedFlow<Boolean>()
    val importResult: SharedFlow<Boolean> = _importResult.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    private val _mealFilter = MutableStateFlow<MealType?>(null)
    val mealFilter: StateFlow<MealType?> = _mealFilter.asStateFlow()

    private val _sort = MutableStateFlow(RecipeSort.NAME)
    val sort: StateFlow<RecipeSort> = _sort.asStateFlow()

    // A keresés eredménye, amelyre a szűrőket és a rendezést alkalmazzuk
    private val baseRecipes = _searchQuery.flatMapLatest { query -> searchRecipesUseCase(query) }

    val recipes: StateFlow<List<Recipe>> =
        combine(baseRecipes, _favoritesOnly, _mealFilter, _sort) { list, favOnly, meal, sort ->
            list.asSequence()
                .filter { !favOnly || it.isFavorite }
                .filter { meal == null || it.mealType == meal }
                .let { seq ->
                    when (sort) {
                        RecipeSort.NAME -> seq.sortedBy { it.name.lowercase() }
                        RecipeSort.CALORIES -> seq.sortedByDescending { it.calories }
                    }
                }
                .toList()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** A keresési kifejezés frissítése. */
    fun onSearchChanged(query: String) {
        Logger.d(Logger.Tags.VIEWMODEL, "RecipeListViewModel – keresés módosult: '$query'")
        _searchQuery.value = query
    }

    /** Csak a kedvencek mutatása ki-/bekapcsolása. */
    fun toggleFavoritesOnly() {
        _favoritesOnly.value = !_favoritesOnly.value
    }

    /** Étkezéstípus szerinti szűrő (null = mind). */
    fun setMealFilter(mealType: MealType?) {
        _mealFilter.value = mealType
    }

    /** Rendezési mód beállítása. */
    fun setSort(sort: RecipeSort) {
        _sort.value = sort
    }

    /** Egy recept kedvenc állapotának átváltása. */
    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            toggleFavoriteUseCase(recipe)
        }
    }

    /** Recept törlése. */
    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            Logger.i(Logger.Tags.VIEWMODEL, "RecipeListViewModel – recept törlése: id=${recipe.id}")
            deleteRecipeUseCase(recipe)
        }
    }

    /** A recept megosztható JSON-szövege. */
    fun shareText(recipe: Recipe): String = shareRecipeUseCase(recipe)

    /** Recept importálása egy megnyitott fájl URI-ból. */
    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    }
                }.getOrNull()
            }
            val ok = content != null && importRecipeUseCase(content)
            _importResult.emit(ok)
        }
    }
}
