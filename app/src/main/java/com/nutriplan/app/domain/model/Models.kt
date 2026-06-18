package com.nutriplan.app.domain.model

import java.time.LocalDate

/**
 * Egy napi testsúly-bejegyzés.
 */
data class WeightEntry(
    val date: LocalDate,
    val weightKg: Double
)

/**
 * Egy elfogyasztott étel naplóbejegyzése egy adott napon.
 */
data class FoodLogEntry(
    val id: Long = 0L,
    val date: LocalDate,
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val mealType: MealType = MealType.LUNCH
)

/**
 * Hozzávaló domain modell.
 * Egy recepthez tartozó alapanyag mennyiséggel, mértékegységgel és kategóriával.
 */
data class Ingredient(
    val id: Long = 0L,
    val name: String,
    val quantity: Double,
    val unit: MeasurementUnit,
    val category: IngredientCategory = IngredientCategory.OTHER,
    // Fordítási kulcs (alaphozzávalóknál); ha null, a name literál szöveget jelenítjük meg
    val nameKey: String? = null
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
    val ingredients: List<Ingredient> = emptyList(),
    // Fordítási kulcs (alaprecepteknél); ha null, a name literál szöveget jelenítjük meg
    val nameKey: String? = null,
    // A recept fotójának helyi elérési útja (null = nincs kép)
    val imagePath: String? = null,
    // Elkészítési útmutató / leírás (null vagy üres = nincs)
    val instructions: String? = null,
    // Kedvencnek jelölve
    val isFavorite: Boolean = false
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
    val purchased: Boolean = false,
    // Fordítási kulcs az összevont hozzávalóhoz (ha alaphozzávalóból származik)
    val nameKey: String? = null
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
