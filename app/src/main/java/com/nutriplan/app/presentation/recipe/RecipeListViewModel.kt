package com.nutriplan.app.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.usecase.DeleteRecipeUseCase
import com.nutriplan.app.domain.usecase.SearchRecipesUseCase
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Recept lista ViewModel – keresés és törlés kezelése.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val searchRecipesUseCase: SearchRecipesUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // A keresési kifejezés változására frissülő receptlista
    val recipes: StateFlow<List<Recipe>> = _searchQuery
        .flatMapLatest { query -> searchRecipesUseCase(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** A keresési kifejezés frissítése. */
    fun onSearchChanged(query: String) {
        Logger.d(Logger.Tags.VIEWMODEL, "RecipeListViewModel – keresés módosult: '$query'")
        _searchQuery.value = query
    }

    /** Recept törlése. */
    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            Logger.i(Logger.Tags.VIEWMODEL, "RecipeListViewModel – recept törlése: id=${recipe.id}")
            deleteRecipeUseCase(recipe)
        }
    }
}
