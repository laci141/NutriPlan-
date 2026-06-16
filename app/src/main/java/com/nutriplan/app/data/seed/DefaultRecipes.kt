package com.nutriplan.app.data.seed

import com.nutriplan.app.domain.model.Ingredient
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.util.IngredientCategorizer

/**
 * Alapértelmezett receptek, amelyeket az alkalmazás az első indításkor betölt.
 * Összesen 50 recept: 10 reggeli, 10 délelőtti snack, 10 ebéd, 10 délutáni snack, 10 vacsora.
 *
 * Minden recept és hozzávaló kap egy fordítási kulcsot (nameKey), így a felületen a
 * kiválasztott nyelven jelennek meg (HU/EN/RO). A tárolt literál szöveg mindig angol,
 * ez a tartalék, ha egy fordítás hiányozna. A tápértékek becsült értékek.
 */
object DefaultRecipes {

    // Segédfüggvény hozzávaló létrehozásához – fordítási kulccsal és automatikus kategóriával
    private fun ing(key: String, name: String, quantity: Double, unit: MeasurementUnit): Ingredient =
        Ingredient(
            name = name,
            quantity = quantity,
            unit = unit,
            category = IngredientCategorizer.categorize(name),
            nameKey = key
        )

    // Rövidítések a gyakori mértékegységekhez
    private val G = MeasurementUnit.GRAM
    private val ML = MeasurementUnit.MILLILITER
    private val PC = MeasurementUnit.PIECE

    // Segédfüggvény recept létrehozásához fordítási kulccsal
    private fun recipe(
        key: String,
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
        ingredients = ingredients,
        nameKey = key
    )

    /** Visszaadja az összes alapértelmezett receptet. */
    fun all(): List<Recipe> = breakfast() + morningSnack() + lunch() + afternoonSnack() + dinner()

    // --- REGGELI ---
    private fun breakfast(): List<Recipe> = listOf(
        recipe("rcp_b1", "Oatmeal with blueberries", MealType.BREAKFAST, 480, 18.0, 62.0, 16.0, listOf(
            ing("ing_oats", "Oats", 80.0, G),
            ing("ing_milk", "Milk", 250.0, ML),
            ing("ing_blueberry", "Blueberry", 100.0, G),
            ing("ing_walnut", "Walnut", 20.0, G)
        )),
        recipe("rcp_b2", "Vegetable omelette", MealType.BREAKFAST, 360, 24.0, 22.0, 18.0, listOf(
            ing("ing_egg", "Egg", 3.0, PC),
            ing("ing_bell_pepper", "Bell pepper", 100.0, G),
            ing("ing_tomato", "Tomato", 100.0, G),
            ing("ing_wholegrain_bread", "Wholegrain bread", 80.0, G)
        )),
        recipe("rcp_b3", "Greek yogurt bowl", MealType.BREAKFAST, 330, 22.0, 38.0, 9.0, listOf(
            ing("ing_greek_yogurt", "Greek yogurt", 250.0, G),
            ing("ing_oats", "Oats", 50.0, G),
            ing("ing_honey", "Honey", 10.0, G)
        )),
        recipe("rcp_b4", "Quark spread", MealType.BREAKFAST, 300, 30.0, 28.0, 6.0, listOf(
            ing("ing_quark", "Quark", 200.0, G),
            ing("ing_bell_pepper", "Bell pepper", 100.0, G),
            ing("ing_wholegrain_bread", "Wholegrain bread", 80.0, G)
        )),
        recipe("rcp_b5", "Avocado egg spread", MealType.BREAKFAST, 420, 18.0, 24.0, 28.0, listOf(
            ing("ing_avocado", "Avocado", 1.0, PC),
            ing("ing_egg", "Egg", 2.0, PC),
            ing("ing_wholegrain_bread", "Wholegrain bread", 80.0, G)
        )),
        recipe("rcp_b6", "Cottage cheese with tomato", MealType.BREAKFAST, 210, 24.0, 12.0, 7.0, listOf(
            ing("ing_cottage_cheese", "Cottage cheese", 200.0, G),
            ing("ing_tomato", "Tomato", 150.0, G)
        )),
        recipe("rcp_b7", "Chicken tortilla", MealType.BREAKFAST, 330, 32.0, 30.0, 8.0, listOf(
            ing("ing_chicken_breast", "Chicken breast", 120.0, G),
            ing("ing_tortilla", "Tortilla", 1.0, PC),
            ing("ing_lettuce", "Salad", 50.0, G)
        )),
        recipe("rcp_b8", "Chia pudding", MealType.BREAKFAST, 380, 14.0, 48.0, 15.0, listOf(
            ing("ing_chia", "Chia seeds", 30.0, G),
            ing("ing_milk", "Milk", 250.0, ML),
            ing("ing_banana", "Banana", 1.0, PC)
        )),
        recipe("rcp_b9", "Skyr with apple", MealType.BREAKFAST, 230, 26.0, 28.0, 1.0, listOf(
            ing("ing_skyr", "Skyr", 250.0, G),
            ing("ing_apple", "Apple", 150.0, G)
        )),
        recipe("rcp_b10", "Tuna sandwich", MealType.BREAKFAST, 290, 30.0, 26.0, 6.0, listOf(
            ing("ing_tuna", "Tuna", 120.0, G),
            ing("ing_bread", "Bread", 80.0, G),
            ing("ing_cucumber", "Cucumber", 100.0, G)
        ))
    )

    // --- DÉLELŐTTI SNACK ---
    private fun morningSnack(): List<Recipe> = listOf(
        recipe("rcp_ms1", "Apple with almond", MealType.MORNING_SNACK, 220, 6.0, 28.0, 12.0, listOf(
            ing("ing_apple", "Apple", 1.0, PC),
            ing("ing_almond", "Almond", 25.0, G)
        )),
        recipe("rcp_ms2", "Banana with walnut", MealType.MORNING_SNACK, 250, 5.0, 30.0, 14.0, listOf(
            ing("ing_banana", "Banana", 1.0, PC),
            ing("ing_walnut", "Walnut", 25.0, G)
        )),
        recipe("rcp_ms3", "Pear with pumpkin seed", MealType.MORNING_SNACK, 230, 7.0, 30.0, 11.0, listOf(
            ing("ing_pear", "Pear", 1.0, PC),
            ing("ing_pumpkin_seed", "Pumpkin seed", 25.0, G)
        )),
        recipe("rcp_ms4", "Yogurt", MealType.MORNING_SNACK, 120, 10.0, 12.0, 4.0, listOf(
            ing("ing_yogurt", "Yogurt", 200.0, G)
        )),
        recipe("rcp_ms5", "Skyr", MealType.MORNING_SNACK, 120, 22.0, 8.0, 0.0, listOf(
            ing("ing_skyr", "Skyr", 200.0, G)
        )),
        recipe("rcp_ms6", "Hummus with carrot", MealType.MORNING_SNACK, 180, 6.0, 20.0, 9.0, listOf(
            ing("ing_hummus", "Hummus", 50.0, G),
            ing("ing_carrot", "Carrot", 150.0, G)
        )),
        recipe("rcp_ms7", "Bell pepper", MealType.MORNING_SNACK, 45, 2.0, 8.0, 0.0, listOf(
            ing("ing_bell_pepper", "Bell pepper", 150.0, G)
        )),
        recipe("rcp_ms8", "Cottage cheese with cucumber", MealType.MORNING_SNACK, 180, 22.0, 8.0, 6.0, listOf(
            ing("ing_cottage_cheese", "Cottage cheese", 150.0, G),
            ing("ing_cucumber", "Cucumber", 100.0, G)
        )),
        recipe("rcp_ms9", "Orange", MealType.MORNING_SNACK, 62, 1.0, 15.0, 0.0, listOf(
            ing("ing_orange", "Orange", 1.0, PC)
        )),
        recipe("rcp_ms10", "Berries", MealType.MORNING_SNACK, 75, 1.0, 18.0, 0.0, listOf(
            ing("ing_berries", "Berries", 150.0, G)
        ))
    )

    // --- EBÉD ---
    private fun lunch(): List<Recipe> = listOf(
        recipe("rcp_l1", "Chicken breast with brown rice", MealType.LUNCH, 520, 52.0, 60.0, 8.0, listOf(
            ing("ing_chicken_breast", "Chicken breast", 180.0, G),
            ing("ing_brown_rice", "Brown rice", 80.0, G),
            ing("ing_broccoli", "Broccoli", 200.0, G)
        )),
        recipe("rcp_l2", "Salmon with salad", MealType.LUNCH, 380, 38.0, 6.0, 22.0, listOf(
            ing("ing_salmon", "Salmon", 180.0, G),
            ing("ing_lettuce", "Salad", 150.0, G)
        )),
        recipe("rcp_l3", "Turkey breast with bulgur", MealType.LUNCH, 470, 50.0, 55.0, 6.0, listOf(
            ing("ing_turkey_breast", "Turkey breast", 180.0, G),
            ing("ing_bulgur", "Bulgur", 80.0, G),
            ing("ing_bell_pepper", "Bell pepper", 150.0, G)
        )),
        recipe("rcp_l4", "Chicken breast with mixed vegetables", MealType.LUNCH, 360, 44.0, 20.0, 8.0, listOf(
            ing("ing_chicken_breast", "Chicken breast", 180.0, G),
            ing("ing_mixed_vegetables", "Mixed vegetables", 250.0, G)
        )),
        recipe("rcp_l5", "Lentils with eggs", MealType.LUNCH, 320, 24.0, 30.0, 10.0, listOf(
            ing("ing_lentils", "Lentils", 100.0, G),
            ing("ing_egg", "Egg", 2.0, PC)
        )),
        recipe("rcp_l6", "Tuna salad", MealType.LUNCH, 250, 40.0, 8.0, 5.0, listOf(
            ing("ing_tuna", "Tuna", 150.0, G),
            ing("ing_lettuce", "Salad", 200.0, G)
        )),
        recipe("rcp_l7", "Beef with vegetables", MealType.LUNCH, 480, 46.0, 18.0, 24.0, listOf(
            ing("ing_beef", "Beef", 180.0, G),
            ing("ing_vegetables", "Vegetables", 250.0, G)
        )),
        recipe("rcp_l8", "Chickpeas with rice", MealType.LUNCH, 420, 18.0, 72.0, 6.0, listOf(
            ing("ing_chickpeas", "Chickpeas", 150.0, G),
            ing("ing_rice", "Rice", 80.0, G)
        )),
        recipe("rcp_l9", "Broccoli with minced meat", MealType.LUNCH, 470, 40.0, 18.0, 28.0, listOf(
            ing("ing_broccoli", "Broccoli", 300.0, G),
            ing("ing_minced_meat", "Minced meat", 150.0, G),
            ing("ing_sour_cream", "Sour cream", 50.0, G)
        )),
        recipe("rcp_l10", "Chicken breast with tomato and cucumber", MealType.LUNCH, 320, 44.0, 12.0, 7.0, listOf(
            ing("ing_chicken_breast", "Chicken breast", 180.0, G),
            ing("ing_tomato", "Tomato", 150.0, G),
            ing("ing_cucumber", "Cucumber", 150.0, G)
        ))
    )

    // --- DÉLUTÁNI SNACK ---
    private fun afternoonSnack(): List<Recipe> = listOf(
        recipe("rcp_as1", "Apple with walnut", MealType.AFTERNOON_SNACK, 200, 4.0, 26.0, 11.0, listOf(
            ing("ing_apple", "Apple", 1.0, PC),
            ing("ing_walnut", "Walnut", 20.0, G)
        )),
        recipe("rcp_as2", "Kefir", MealType.AFTERNOON_SNACK, 150, 9.0, 13.0, 8.0, listOf(
            ing("ing_kefir", "Kefir", 250.0, ML)
        )),
        recipe("rcp_as3", "Skyr", MealType.AFTERNOON_SNACK, 120, 22.0, 8.0, 0.0, listOf(
            ing("ing_skyr", "Skyr", 200.0, G)
        )),
        recipe("rcp_as4", "Cottage cheese", MealType.AFTERNOON_SNACK, 135, 18.0, 6.0, 5.0, listOf(
            ing("ing_cottage_cheese", "Cottage cheese", 150.0, G)
        )),
        recipe("rcp_as5", "Pear", MealType.AFTERNOON_SNACK, 100, 1.0, 27.0, 0.0, listOf(
            ing("ing_pear", "Pear", 1.0, PC)
        )),
        recipe("rcp_as6", "Whole grain biscuit", MealType.AFTERNOON_SNACK, 160, 4.0, 28.0, 4.0, listOf(
            ing("ing_wholegrain_biscuit", "Whole grain biscuit", 40.0, G)
        )),
        recipe("rcp_as7", "Almond", MealType.AFTERNOON_SNACK, 145, 5.0, 5.0, 13.0, listOf(
            ing("ing_almond", "Almond", 25.0, G)
        )),
        recipe("rcp_as8", "Popcorn", MealType.AFTERNOON_SNACK, 115, 3.0, 23.0, 1.0, listOf(
            ing("ing_popcorn", "Popcorn", 30.0, G)
        )),
        recipe("rcp_as9", "Berries", MealType.AFTERNOON_SNACK, 75, 1.0, 18.0, 0.0, listOf(
            ing("ing_berries", "Berries", 150.0, G)
        )),
        recipe("rcp_as10", "Hummus with cucumber", MealType.AFTERNOON_SNACK, 150, 6.0, 16.0, 8.0, listOf(
            ing("ing_hummus", "Hummus", 50.0, G),
            ing("ing_cucumber", "Cucumber", 150.0, G)
        ))
    )

    // --- VACSORA ---
    private fun dinner(): List<Recipe> = listOf(
        recipe("rcp_d1", "Tomato cucumber feta salad", MealType.DINNER, 250, 12.0, 14.0, 16.0, listOf(
            ing("ing_tomato", "Tomato", 150.0, G),
            ing("ing_cucumber", "Cucumber", 150.0, G),
            ing("ing_feta", "Feta", 80.0, G)
        )),
        recipe("rcp_d2", "Tuna with salad", MealType.DINNER, 220, 34.0, 6.0, 5.0, listOf(
            ing("ing_tuna", "Tuna", 120.0, G),
            ing("ing_lettuce", "Salad", 150.0, G)
        )),
        recipe("rcp_d3", "Eggs with bread", MealType.DINNER, 320, 22.0, 30.0, 12.0, listOf(
            ing("ing_egg", "Egg", 3.0, PC),
            ing("ing_bread", "Bread", 80.0, G)
        )),
        recipe("rcp_d4", "Cottage cheese with bell pepper", MealType.DINNER, 230, 28.0, 16.0, 7.0, listOf(
            ing("ing_cottage_cheese", "Cottage cheese", 200.0, G),
            ing("ing_bell_pepper", "Bell pepper", 150.0, G)
        )),
        recipe("rcp_d5", "Turkey breast with salad", MealType.DINNER, 230, 40.0, 6.0, 4.0, listOf(
            ing("ing_turkey_breast", "Turkey breast", 150.0, G),
            ing("ing_lettuce", "Salad", 150.0, G)
        )),
        recipe("rcp_d6", "Vegetable soup", MealType.DINNER, 120, 5.0, 20.0, 2.0, listOf(
            ing("ing_vegetable_soup", "Vegetable soup", 300.0, G)
        )),
        recipe("rcp_d7", "Salmon with salad", MealType.DINNER, 330, 32.0, 6.0, 19.0, listOf(
            ing("ing_salmon", "Salmon", 150.0, G),
            ing("ing_lettuce", "Salad", 150.0, G)
        )),
        recipe("rcp_d8", "Chicken breast with tortilla", MealType.DINNER, 300, 32.0, 28.0, 6.0, listOf(
            ing("ing_chicken_breast", "Chicken breast", 120.0, G),
            ing("ing_tortilla", "Tortilla", 1.0, PC)
        )),
        recipe("rcp_d9", "Cottage cheese with pepper and cucumber", MealType.DINNER, 240, 30.0, 16.0, 7.0, listOf(
            ing("ing_cottage_cheese", "Cottage cheese", 200.0, G),
            ing("ing_bell_pepper", "Bell pepper", 100.0, G),
            ing("ing_cucumber", "Cucumber", 100.0, G)
        )),
        recipe("rcp_d10", "Mozzarella with tomato", MealType.DINNER, 320, 22.0, 12.0, 20.0, listOf(
            ing("ing_mozzarella", "Mozzarella", 125.0, G),
            ing("ing_tomato", "Tomato", 200.0, G)
        ))
    )
}
