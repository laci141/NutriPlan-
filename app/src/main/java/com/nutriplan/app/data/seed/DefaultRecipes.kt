package com.nutriplan.app.data.seed

import com.nutriplan.app.domain.model.Ingredient
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.util.IngredientCategorizer

/**
 * Alapértelmezett receptek, amelyeket az alkalmazás az első indításkor betölt.
 * Összesen 50 recept: 10 reggeli, 10 délelőtti snack, 10 ebéd, 10 délutáni snack, 10 vacsora.
 * A tápértékek becsült értékek.
 */
object DefaultRecipes {

    // Segédfüggvény hozzávaló létrehozásához – a kategóriát automatikusan meghatározzuk
    private fun ing(name: String, quantity: Double, unit: MeasurementUnit): Ingredient =
        Ingredient(
            name = name,
            quantity = quantity,
            unit = unit,
            category = IngredientCategorizer.categorize(name)
        )

    // Rövidítések a gyakori mértékegységekhez
    private val G = MeasurementUnit.GRAM
    private val ML = MeasurementUnit.MILLILITER
    private val PC = MeasurementUnit.PIECE

    // Segédfüggvény recept létrehozásához
    private fun recipe(
        name: String,
        mealType: MealType,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        ingredients: List<Ingredient>
    ): Recipe = Recipe(
        name = name,
        mealType = mealType,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        isDefault = true,
        ingredients = ingredients
    )

    /** Visszaadja az összes alapértelmezett receptet. */
    fun all(): List<Recipe> = breakfast() + morningSnack() + lunch() + afternoonSnack() + dinner()

    // --- REGGELI ---
    private fun breakfast(): List<Recipe> = listOf(
        recipe("Zabkása áfonyával", MealType.BREAKFAST, 480, 18.0, 62.0, 16.0, listOf(
            ing("Zabpehely", 80.0, G),
            ing("Tej", 250.0, ML),
            ing("Áfonya", 100.0, G),
            ing("Dió", 20.0, G)
        )),
        recipe("Zöldséges omlett", MealType.BREAKFAST, 360, 24.0, 22.0, 18.0, listOf(
            ing("Tojás", 3.0, PC),
            ing("Paprika", 100.0, G),
            ing("Paradicsom", 100.0, G),
            ing("Teljes kiőrlésű kenyér", 80.0, G)
        )),
        recipe("Görög joghurt", MealType.BREAKFAST, 330, 22.0, 38.0, 9.0, listOf(
            ing("Görög joghurt", 250.0, G),
            ing("Zabpehely", 50.0, G),
            ing("Méz", 10.0, G)
        )),
        recipe("Túrókrém", MealType.BREAKFAST, 300, 30.0, 28.0, 6.0, listOf(
            ing("Túró", 200.0, G),
            ing("Paprika", 100.0, G),
            ing("Teljes kiőrlésű kenyér", 80.0, G)
        )),
        recipe("Avokádós tojáskrém", MealType.BREAKFAST, 420, 18.0, 24.0, 28.0, listOf(
            ing("Avokádó", 1.0, PC),
            ing("Tojás", 2.0, PC),
            ing("Teljes kiőrlésű kenyér", 80.0, G)
        )),
        recipe("Cottage cheese paradicsommal", MealType.BREAKFAST, 210, 24.0, 12.0, 7.0, listOf(
            ing("Cottage cheese", 200.0, G),
            ing("Paradicsom", 150.0, G)
        )),
        recipe("Csirkés tortilla", MealType.BREAKFAST, 330, 32.0, 30.0, 8.0, listOf(
            ing("Csirkemell", 120.0, G),
            ing("Tortilla", 1.0, PC),
            ing("Saláta", 50.0, G)
        )),
        recipe("Chia puding", MealType.BREAKFAST, 380, 14.0, 48.0, 15.0, listOf(
            ing("Chia mag", 30.0, G),
            ing("Tej", 250.0, ML),
            ing("Banán", 1.0, PC)
        )),
        recipe("Skyr almával", MealType.BREAKFAST, 230, 26.0, 28.0, 1.0, listOf(
            ing("Skyr", 250.0, G),
            ing("Alma", 150.0, G)
        )),
        recipe("Tonhalkrémes szendvics", MealType.BREAKFAST, 290, 30.0, 26.0, 6.0, listOf(
            ing("Tonhal", 120.0, G),
            ing("Kenyér", 80.0, G),
            ing("Uborka", 100.0, G)
        ))
    )

    // --- DÉLELŐTTI SNACK ---
    private fun morningSnack(): List<Recipe> = listOf(
        recipe("Apple with almond", MealType.MORNING_SNACK, 220, 6.0, 28.0, 12.0, listOf(
            ing("Apple", 1.0, PC),
            ing("Almond", 25.0, G)
        )),
        recipe("Banana with walnut", MealType.MORNING_SNACK, 250, 5.0, 30.0, 14.0, listOf(
            ing("Banana", 1.0, PC),
            ing("Walnut", 25.0, G)
        )),
        recipe("Pear with pumpkin seed", MealType.MORNING_SNACK, 230, 7.0, 30.0, 11.0, listOf(
            ing("Pear", 1.0, PC),
            ing("Pumpkin seed", 25.0, G)
        )),
        recipe("Yogurt", MealType.MORNING_SNACK, 120, 10.0, 12.0, 4.0, listOf(
            ing("Yogurt", 200.0, G)
        )),
        recipe("Skyr", MealType.MORNING_SNACK, 120, 22.0, 8.0, 0.0, listOf(
            ing("Skyr", 200.0, G)
        )),
        recipe("Hummus with carrot", MealType.MORNING_SNACK, 180, 6.0, 20.0, 9.0, listOf(
            ing("Hummus", 50.0, G),
            ing("Carrot", 150.0, G)
        )),
        recipe("Paprika", MealType.MORNING_SNACK, 45, 2.0, 8.0, 0.0, listOf(
            ing("Paprika", 150.0, G)
        )),
        recipe("Cottage cheese with cucumber", MealType.MORNING_SNACK, 180, 22.0, 8.0, 6.0, listOf(
            ing("Cottage cheese", 150.0, G),
            ing("Cucumber", 100.0, G)
        )),
        recipe("Orange", MealType.MORNING_SNACK, 62, 1.0, 15.0, 0.0, listOf(
            ing("Orange", 1.0, PC)
        )),
        recipe("Berries", MealType.MORNING_SNACK, 75, 1.0, 18.0, 0.0, listOf(
            ing("Berries", 150.0, G)
        ))
    )

    // --- EBÉD ---
    private fun lunch(): List<Recipe> = listOf(
        recipe("Chicken breast with brown rice", MealType.LUNCH, 520, 52.0, 60.0, 8.0, listOf(
            ing("Chicken breast", 180.0, G),
            ing("Brown rice", 80.0, G),
            ing("Broccoli", 200.0, G)
        )),
        recipe("Salmon with salad", MealType.LUNCH, 380, 38.0, 6.0, 22.0, listOf(
            ing("Salmon", 180.0, G),
            ing("Salad", 150.0, G)
        )),
        recipe("Turkey breast with bulgur", MealType.LUNCH, 470, 50.0, 55.0, 6.0, listOf(
            ing("Turkey breast", 180.0, G),
            ing("Bulgur", 80.0, G),
            ing("Paprika", 150.0, G)
        )),
        recipe("Chicken breast with mixed vegetables", MealType.LUNCH, 360, 44.0, 20.0, 8.0, listOf(
            ing("Chicken breast", 180.0, G),
            ing("Mixed vegetables", 250.0, G)
        )),
        recipe("Lentils with eggs", MealType.LUNCH, 320, 24.0, 30.0, 10.0, listOf(
            ing("Lentils", 100.0, G),
            ing("Egg", 2.0, PC)
        )),
        recipe("Tuna salad", MealType.LUNCH, 250, 40.0, 8.0, 5.0, listOf(
            ing("Tuna", 150.0, G),
            ing("Salad", 200.0, G)
        )),
        recipe("Beef with vegetables", MealType.LUNCH, 480, 46.0, 18.0, 24.0, listOf(
            ing("Beef", 180.0, G),
            ing("Vegetables", 250.0, G)
        )),
        recipe("Chickpeas with rice", MealType.LUNCH, 420, 18.0, 72.0, 6.0, listOf(
            ing("Chickpeas", 150.0, G),
            ing("Rice", 80.0, G)
        )),
        recipe("Broccoli with minced meat", MealType.LUNCH, 470, 40.0, 18.0, 28.0, listOf(
            ing("Broccoli", 300.0, G),
            ing("Minced meat", 150.0, G),
            ing("Sour cream", 50.0, G)
        )),
        recipe("Chicken breast with tomato and cucumber", MealType.LUNCH, 320, 44.0, 12.0, 7.0, listOf(
            ing("Chicken breast", 180.0, G),
            ing("Tomato", 150.0, G),
            ing("Cucumber", 150.0, G)
        ))
    )

    // --- DÉLUTÁNI SNACK ---
    private fun afternoonSnack(): List<Recipe> = listOf(
        recipe("Apple with walnut", MealType.AFTERNOON_SNACK, 200, 4.0, 26.0, 11.0, listOf(
            ing("Apple", 1.0, PC),
            ing("Walnut", 20.0, G)
        )),
        recipe("Kefir", MealType.AFTERNOON_SNACK, 150, 9.0, 13.0, 8.0, listOf(
            ing("Kefir", 250.0, ML)
        )),
        recipe("Skyr", MealType.AFTERNOON_SNACK, 120, 22.0, 8.0, 0.0, listOf(
            ing("Skyr", 200.0, G)
        )),
        recipe("Cottage cheese", MealType.AFTERNOON_SNACK, 135, 18.0, 6.0, 5.0, listOf(
            ing("Cottage cheese", 150.0, G)
        )),
        recipe("Pear", MealType.AFTERNOON_SNACK, 100, 1.0, 27.0, 0.0, listOf(
            ing("Pear", 1.0, PC)
        )),
        recipe("Whole grain biscuit", MealType.AFTERNOON_SNACK, 160, 4.0, 28.0, 4.0, listOf(
            ing("Whole grain biscuit", 40.0, G)
        )),
        recipe("Almond", MealType.AFTERNOON_SNACK, 145, 5.0, 5.0, 13.0, listOf(
            ing("Almond", 25.0, G)
        )),
        recipe("Popcorn", MealType.AFTERNOON_SNACK, 115, 3.0, 23.0, 1.0, listOf(
            ing("Popcorn", 30.0, G)
        )),
        recipe("Berries", MealType.AFTERNOON_SNACK, 75, 1.0, 18.0, 0.0, listOf(
            ing("Berries", 150.0, G)
        )),
        recipe("Hummus with cucumber", MealType.AFTERNOON_SNACK, 150, 6.0, 16.0, 8.0, listOf(
            ing("Hummus", 50.0, G),
            ing("Cucumber", 150.0, G)
        ))
    )

    // --- VACSORA ---
    private fun dinner(): List<Recipe> = listOf(
        recipe("Tomato cucumber feta salad", MealType.DINNER, 250, 12.0, 14.0, 16.0, listOf(
            ing("Tomato", 150.0, G),
            ing("Cucumber", 150.0, G),
            ing("Feta", 80.0, G)
        )),
        recipe("Tuna with salad", MealType.DINNER, 220, 34.0, 6.0, 5.0, listOf(
            ing("Tuna", 120.0, G),
            ing("Salad", 150.0, G)
        )),
        recipe("Eggs with bread", MealType.DINNER, 320, 22.0, 30.0, 12.0, listOf(
            ing("Egg", 3.0, PC),
            ing("Bread", 80.0, G)
        )),
        recipe("Cottage cheese with paprika", MealType.DINNER, 230, 28.0, 16.0, 7.0, listOf(
            ing("Cottage cheese", 200.0, G),
            ing("Paprika", 150.0, G)
        )),
        recipe("Turkey breast with salad", MealType.DINNER, 230, 40.0, 6.0, 4.0, listOf(
            ing("Turkey breast", 150.0, G),
            ing("Salad", 150.0, G)
        )),
        recipe("Vegetable soup", MealType.DINNER, 120, 5.0, 20.0, 2.0, listOf(
            ing("Vegetable soup", 300.0, G)
        )),
        recipe("Salmon with salad", MealType.DINNER, 330, 32.0, 6.0, 19.0, listOf(
            ing("Salmon", 150.0, G),
            ing("Salad", 150.0, G)
        )),
        recipe("Chicken breast with tortilla", MealType.DINNER, 300, 32.0, 28.0, 6.0, listOf(
            ing("Chicken breast", 120.0, G),
            ing("Tortilla", 1.0, PC)
        )),
        recipe("Cottage cheese with paprika and cucumber", MealType.DINNER, 240, 30.0, 16.0, 7.0, listOf(
            ing("Cottage cheese", 200.0, G),
            ing("Paprika", 100.0, G),
            ing("Cucumber", 100.0, G)
        )),
        recipe("Mozzarella with tomato", MealType.DINNER, 320, 22.0, 12.0, 20.0, listOf(
            ing("Mozzarella", 125.0, G),
            ing("Tomato", 200.0, G)
        ))
    )
}
