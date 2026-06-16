package com.nutriplan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Recept adatbázis entitás (recipes tábla).
 * A tápértékek magán a recepten tárolódnak, a hozzávalók külön táblában (IngredientEntity).
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    // Az étkezés típus kulcsa szövegként (pl. "breakfast")
    val mealType: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    // Jelzi, hogy ez az első indításkor betöltött alaprecept-e
    val isDefault: Boolean = false
)
