package com.nutriplan.app.presentation.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.data.remote.OpenFoodFactsDataSource
import com.nutriplan.app.data.remote.ProductLookupResult
import com.nutriplan.app.data.remote.ScannedProduct
import com.nutriplan.app.domain.model.Ingredient
import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.usecase.GetRecipeUseCase
import com.nutriplan.app.domain.usecase.SaveRecipeUseCase
import com.nutriplan.app.domain.util.IngredientCategorizer
import com.nutriplan.app.presentation.navigation.Routes
import com.nutriplan.app.util.ImageStorage
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
    val category: IngredientCategory = IngredientCategory.OTHER,
    // Fordítási kulcs (alaphozzávalónál); a név szerkesztésekor töröljük
    val nameKey: String? = null
)

/**
 * A vonalkód-beolvasás visszajelzése (a képernyő fordítja szöveggé).
 */
enum class ScanFeedback { SUCCESS, NOT_FOUND, ERROR }

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
    val saved: Boolean = false,
    // Fordítási kulcs (alaprecept); a név szerkesztésekor töröljük
    val nameKey: String? = null,
    // A recept fotójának helyi elérési útja (null = nincs kép)
    val imagePath: String? = null,
    // Elkészítési útmutató
    val instructions: String = "",
    // Folyamatban lévő termék-lekérdezés (vonalkód után)
    val isLookingUp: Boolean = false,
    // Egyszer megjelenítendő visszajelzés a beolvasásról
    val scanFeedback: ScanFeedback? = null
)

/**
 * Recept szerkesztő ViewModel – új recept létrehozása vagy meglévő szerkesztése.
 */
@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeUseCase: GetRecipeUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val openFoodFacts: OpenFoodFactsDataSource
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
                        IngredientForm(it.name, it.quantity.toString(), it.unit, it.category, it.nameKey)
                    }.ifEmpty { listOf(IngredientForm()) },
                    isEditing = true,
                    nameKey = recipe.nameKey,
                    imagePath = recipe.imagePath,
                    instructions = recipe.instructions.orEmpty()
                )
            } else {
                Logger.w(Logger.Tags.VIEWMODEL, "A betöltendő recept nem található: id=$id")
            }
        }
    }

    // A név szerkesztésekor töröljük a fordítási kulcsot (egyedi recept lesz a megadott szöveggel)
    fun onNameChange(value: String) { _state.value = _state.value.copy(name = value, nameError = false, nameKey = null) }
    fun onMealTypeChange(value: MealType) { _state.value = _state.value.copy(mealType = value) }
    fun onCaloriesChange(value: String) { _state.value = _state.value.copy(calories = value.filterNumeric()) }
    fun onProteinChange(value: String) { _state.value = _state.value.copy(protein = value.filterDecimal()) }
    fun onCarbsChange(value: String) { _state.value = _state.value.copy(carbs = value.filterDecimal()) }
    fun onFatChange(value: String) { _state.value = _state.value.copy(fat = value.filterDecimal()) }
    fun onInstructionsChange(value: String) { _state.value = _state.value.copy(instructions = value) }

    /** Új recept-fotó beállítása (kamera vagy galéria). A korábbi képet töröljük. */
    fun onImageSelected(path: String?) {
        if (path == null) return
        val previous = _state.value.imagePath
        if (previous != null && previous != path) ImageStorage.delete(previous)
        _state.value = _state.value.copy(imagePath = path)
        Logger.i(Logger.Tags.RECIPE, "Recept-fotó beállítva")
    }

    /** A recept-fotó eltávolítása. */
    fun onImageRemoved() {
        ImageStorage.delete(_state.value.imagePath)
        _state.value = _state.value.copy(imagePath = null)
        Logger.i(Logger.Tags.RECIPE, "Recept-fotó eltávolítva")
    }

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
            // A név szerkesztésekor töröljük a fordítási kulcsot
            current.copy(name = name, category = IngredientCategorizer.categorize(name), nameKey = null)
        )
    }

    /**
     * Beolvasott vonalkód feldolgozása: az Open Food Facts adatbázisból lekérdezzük
     * a termék nevét és 100 g-ra vetített tápértékét, majd kitöltjük az űrlapot.
     */
    fun onBarcodeScanned(barcode: String) {
        if (barcode.isBlank() || _state.value.isLookingUp) return
        Logger.i(Logger.Tags.VIEWMODEL, "Vonalkód beolvasva: $barcode")
        _state.value = _state.value.copy(isLookingUp = true)
        viewModelScope.launch {
            when (val result = openFoodFacts.lookup(barcode)) {
                is ProductLookupResult.Success -> applyScannedProduct(result.product)
                ProductLookupResult.NotFound ->
                    _state.value = _state.value.copy(isLookingUp = false, scanFeedback = ScanFeedback.NOT_FOUND)
                ProductLookupResult.NetworkError ->
                    _state.value = _state.value.copy(isLookingUp = false, scanFeedback = ScanFeedback.ERROR)
            }
        }
    }

    /** A megtalált termék adatainak betöltése az űrlapba (100 g-os értékek). */
    private fun applyScannedProduct(product: ScannedProduct) {
        val current = _state.value
        // A nevet csak akkor írjuk felül, ha még üres, és a terméknek van neve
        val fillName = current.name.isBlank() && product.name.isNotBlank()
        _state.value = current.copy(
            name = if (fillName) product.name else current.name,
            nameKey = if (fillName) null else current.nameKey,
            nameError = false,
            calories = product.caloriesPer100g.toString(),
            protein = product.proteinPer100g.trimDecimalString(),
            carbs = product.carbsPer100g.trimDecimalString(),
            fat = product.fatPer100g.trimDecimalString(),
            isLookingUp = false,
            scanFeedback = ScanFeedback.SUCCESS
        )
    }

    /** A beolvasási visszajelzés nyugtázása (a snackbar megjelenítése után). */
    fun consumeScanFeedback() {
        _state.value = _state.value.copy(scanFeedback = null)
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
                        category = it.category,
                        nameKey = it.nameKey
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
                ingredients = ingredients,
                nameKey = current.nameKey,
                imagePath = current.imagePath,
                instructions = current.instructions.trim().ifBlank { null }
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

    // Egész értéknél elhagyja a tizedesjegyet (5.0 -> "5"), különben egy tizedesre kerekít
    private fun Double.trimDecimalString(): String =
        if (this == kotlin.math.floor(this)) toInt().toString()
        else "%.1f".format(this).replace(',', '.')
}
