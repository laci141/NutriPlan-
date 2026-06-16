package com.nutriplan.app.data.backup

import kotlinx.serialization.Serializable

/**
 * JSON mentés/visszatöltés adatszerkezetei (kotlinx.serialization).
 * Ezekből épül fel a teljes biztonsági mentés fájl.
 */

@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = 0L,
    val recipes: List<RecipeDto> = emptyList(),
    val mealPlans: List<MealPlanDto> = emptyList(),
    val shoppingItems: List<ShoppingItemDto> = emptyList()
)

@Serializable
data class RecipeDto(
    val id: Long,
    val name: String,
    val mealType: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val isDefault: Boolean,
    val nameKey: String? = null,
    val ingredients: List<IngredientDto>
)

@Serializable
data class IngredientDto(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val nameKey: String? = null
)

@Serializable
data class MealPlanDto(
    val weekDay: String,
    val mealType: String,
    val recipeId: Long
)

@Serializable
data class ShoppingItemDto(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val purchased: Boolean,
    val nameKey: String? = null
)
