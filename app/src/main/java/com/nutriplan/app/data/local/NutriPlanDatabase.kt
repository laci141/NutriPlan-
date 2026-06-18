package com.nutriplan.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nutriplan.app.data.local.dao.FoodLogDao
import com.nutriplan.app.data.local.dao.MealPlanDao
import com.nutriplan.app.data.local.dao.RecipeDao
import com.nutriplan.app.data.local.dao.ShoppingDao
import com.nutriplan.app.data.local.entity.FoodLogEntity
import com.nutriplan.app.data.local.entity.IngredientEntity
import com.nutriplan.app.data.local.entity.MealPlanEntity
import com.nutriplan.app.data.local.entity.RecipeEntity
import com.nutriplan.app.data.local.entity.ShoppingItemEntity

/**
 * Az alkalmazás Room adatbázisa.
 * Entitások: recept, hozzávaló, heti terv, bevásárlólista tétel és étkezés-napló.
 */
@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        MealPlanEntity::class,
        ShoppingItemEntity::class,
        FoodLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class NutriPlanDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun foodLogDao(): FoodLogDao

    companion object {
        const val DATABASE_NAME = "nutriplan.db"
    }
}
