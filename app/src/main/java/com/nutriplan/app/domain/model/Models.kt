package com.nutriplan.app.domain.model

/**
 * Hozzávaló domain modell.
 * Egy recepthez tartozó alapanyag mennyiséggel, mértékegységgel és kategóriával.
 */
data class Ingredient(
    val id: Long = 0L,
    val name: String,
    val quantity: Double,
    val unit: MeasurementUnit,
    val category: IngredientCategory = IngredientCategory.OTHER
)

/**
 * Recept domain modell a hozzá tartozó hozzávalókkal és tápértékadatokkal.
 */
data class Recipe(
    val id: Long = 0L,
    val name: String,
    val mealType: MealType,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val isDefault: Boolean = false,
    val ingredients: List<Ingredient> = emptyList()
)

/**
 * Egy heti terv bejegyzés: melyik naphoz és étkezéshez melyik recept tartozik.
 */
data class MealAssignment(
    val id: Long = 0L,
    val weekDay: WeekDay,
    val mealType: MealType,
    val recipe: Recipe
)

/**
 * Bevásárlólista tétel.
 */
data class ShoppingItem(
    val id: Long = 0L,
    val name: String,
    val quantity: Double,
    val unit: MeasurementUnit,
    val category: IngredientCategory,
    val purchased: Boolean = false
)

/**
 * Tápérték összesítés (kalória, fehérje, szénhidrát, zsír).
 */
data class NutritionTotals(
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
) {
    /** Két összesítés összeadása – napi és heti aggregációhoz. */
    operator fun plus(other: NutritionTotals): NutritionTotals = NutritionTotals(
        calories = calories + other.calories,
        protein = protein + other.protein,
        carbs = carbs + other.carbs,
        fat = fat + other.fat
    )
}
