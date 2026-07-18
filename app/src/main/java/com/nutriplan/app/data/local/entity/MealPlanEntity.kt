package com.nutriplan.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Heti terv bejegyzés entitás (meal_plans tábla).
 * Egy adott naphoz és étkezéshez rendel egy receptet.
 */
@Entity(
    tableName = "meal_plans",
    indices = [Index("recipeId"), Index(value = ["weekDay", "mealType"])]
)
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    // A hét napjának kulcsa (pl. "monday")
    val weekDay: String,
    // Az étkezés típus kulcsa (pl. "lunch")
    val mealType: String,
    val recipeId: Long
)
