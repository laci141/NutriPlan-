package com.nutriplan.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.nutriplan.app.data.local.entity.IngredientEntity
import com.nutriplan.app.data.local.entity.RecipeEntity

/**
 * Recept és a hozzá tartozó hozzávalók Room kapcsolat (egy-a-többhöz).
 * A recept id-ja köti össze a hozzávalók recipeId mezőjével.
 */
data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<IngredientEntity>
)
