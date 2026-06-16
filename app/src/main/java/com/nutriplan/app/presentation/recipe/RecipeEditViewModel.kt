package com.nutriplan.app.presentation.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.domain.model.Ingredient
import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.usecase.GetRecipeUseCase
import com.nutriplan.app.domain.usecase.SaveRecipeUseCase
import com.nutriplan.app.domain.util.IngredientCategorizer
import com.nutriplan.app.presentation.navigation.Routes
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Egy szerkeszthető hozzávaló sor a recept űrlapon.
 */
data class IngredientForm(
    val name: String = "",
    val quantity: String = "",
    val unit: MeasurementUnit = MeasurementUnit.GRAM,
    val category: IngredientCategory = IngredientCategory.OTHER
)

/**
 * A recept szerkesztő űrlap teljes állapota.
 */
data class RecipeFormState(
    val id: Long = 0L,
    val name: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val ingredients: List<IngredientForm> = listOf(IngredientForm()),
    val isEditing: Boolean = false,
    val nameError: Boolean = false,
    val saved: Boolean = false
)

/**
 * Recept szerkesztő ViewModel – új recept létrehozása vagy meglévő szerkesztése.
 */
@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeUseCase: GetRecipeUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase
) : ViewModel() {

    private val recipeId: Long = savedStateHandle[Routes.ARG_RECIPE_ID] ?: 0L

    private val _state = MutableStateFlow(RecipeFormState(isEditing = recipeId != 0L))
    val state: StateFlow<RecipeFormState> = _state.asStateFlow()

    init {
        Logger.i(Logger.Tags.VIEWMODEL, "RecipeEditViewModel létrehozva, recipeId=$recipeId")
        if (recipeId != 0L) {
            loadRecipe(recipeId)
        }
    }

    /** Meglévő recept betöltése az űrlapba. */
    private fun loadRecipe(id: Long) {
        viewModelScope.launch {
            val recipe = getRecipeUseCase(id).first()
            if (recipe != null) {
                Logger.d(Logger.Tags.VIEWMODEL, "Recept betöltve szerkesztésre: '${recipe.name}'")
                _state.value = RecipeFormState(
                    id = recipe.id,
                    name = recipe.name,
                    mealType = recipe.mealType,
                    calories = recipe.calories.toString(),
                    protein = recipe.protein.toString(),
                    carbs = recipe.carbs.toString(),
                    fat = recipe.fat.toString(),
                    ingredients = recipe.ingredients.map {
                        IngredientForm(it.name, it.quantity.toString(), it.unit, it.category)
                    }.ifEmpty { listOf(IngredientForm()) },
                    isEditing = true
                )
            } else {
                Logger.w(Logger.Tags.VIEWMODEL, "A betöltendő recept nem található: id=$id")
            }
        }
    }

    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value, nameError = false) }
    fun onMealTypeChange(value: MealType) { _state.value = _state.value.copy(mealType = value) }
    fun onCaloriesChange(value: String) { _state.value = _state.value.copy(calories = value.filterNumeric()) }
    fun onProteinChange(value: String) { _state.value = _state.value.copy(protein = value.filterDecimal()) }
    fun onCarbsChange(value: String) { _state.value = _state.value.copy(carbs = value.filterDecimal()) }
    fun onFatChange(value: String) { _state.value = _state.value.copy(fat = value.filterDecimal()) }

    /** Új, üres hozzávaló sor hozzáadása. */
    fun addIngredient() {
        _state.value = _state.value.copy(ingredients = _state.value.ingredients + IngredientForm())
    }

    /** Hozzávaló sor eltávolítása index alapján. */
    fun removeIngredient(index: Int) {
        val list = _state.value.ingredients.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _state.value = _state.value.copy(ingredients = list.ifEmpty { listOf(IngredientForm()) })
        }
    }

    /** Hozzávaló sor frissítése. A névből automatikusan kategóriát javasolunk. */
    fun updateIngredient(index: Int, form: IngredientForm) {
        val list = _state.value.ingredients.toMutableList()
        if (index in list.indices) {
            list[index] = form
            _state.value = _state.value.copy(ingredients = list)
        }
    }

    /** A hozzávaló nevének módosítása + automatikus kategorizálás. */
    fun onIngredientNameChange(index: Int, name: String) {
        val current = _state.value.ingredients.getOrNull(index) ?: return
        updateIngredient(
            index,
            current.copy(name = name, category = IngredientCategorizer.categorize(name))
        )
    }

    /** Recept mentése validáció után. */
    fun save() {
        val current = _state.value
        if (current.name.isBlank()) {
            Logger.w(Logger.Tags.VIEWMODEL, "Mentés elutasítva – a recept neve üres")
            _state.value = current.copy(nameError = true)
            return
        }

        viewModelScope.launch {
            // Csak a kitöltött nevű hozzávalókat mentjük
            val ingredients = current.ingredients
                .filter { it.name.isNotBlank() }
                .map {
                    Ingredient(
                        name = it.name.trim(),
                        quantity = it.quantity.toDoubleOrNull() ?: 0.0,
                        unit = it.unit,
                        category = it.category
                    )
                }

            val recipe = Recipe(
                id = current.id,
                name = current.name.trim(),
                mealType = current.mealType,
                calories = current.calories.toIntOrNull() ?: 0,
                protein = current.protein.toDoubleOrNull() ?: 0.0,
                carbs = current.carbs.toDoubleOrNull() ?: 0.0,
                fat = current.fat.toDoubleOrNull() ?: 0.0,
                ingredients = ingredients
            )

            Logger.i(Logger.Tags.VIEWMODEL, "RecipeEditViewModel – recept mentése: '${recipe.name}'")
            saveRecipeUseCase(recipe)
            _state.value = _state.value.copy(saved = true)
        }
    }

    // Csak egész számokat enged
    private fun String.filterNumeric(): String = filter { it.isDigit() }

    // Tizedes számokat enged (pont vagy vessző)
    private fun String.filterDecimal(): String =
        replace(',', '.').filter { it.isDigit() || it == '.' }
}
