package com.nutriplan.app.data.mapper

import com.nutriplan.app.data.local.entity.IngredientEntity
import com.nutriplan.app.data.local.entity.RecipeEntity
import com.nutriplan.app.data.local.entity.ShoppingItemEntity
import com.nutriplan.app.data.local.relation.RecipeWithIngredients
import com.nutriplan.app.domain.model.Ingredient
import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.model.ShoppingItem

/**
 * Leképező függvények az adatbázis entitások és a domain modellek között.
 */

fun IngredientEntity.toDomain(): Ingredient = Ingredient(
    id = id,
    name = name,
    quantity = quantity,
    unit = MeasurementUnit.fromKey(unit),
    category = IngredientCategory.fromKey(category)
)

fun Ingredient.toEntity(recipeId: Long): IngredientEntity = IngredientEntity(
    id = id,
    recipeId = recipeId,
    name = name,
    quantity = quantity,
    unit = unit.key,
    category = category.key
)

fun RecipeWithIngredients.toDomain(): Recipe = Recipe(
    id = recipe.id,
    name = recipe.name,
    mealType = MealType.fromKey(recipe.mealType),
    calories = recipe.calories,
    protein = recipe.protein,
    carbs = recipe.carbs,
    fat = recipe.fat,
    isDefault = recipe.isDefault,
    ingredients = ingredients.map { it.toDomain() }
)

fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    name = name,
    mealType = mealType.key,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    isDefault = isDefault
)

fun ShoppingItemEntity.toDomain(): ShoppingItem = ShoppingItem(
    id = id,
    name = name,
    quantity = quantity,
    unit = MeasurementUnit.fromKey(unit),
    category = IngredientCategory.fromKey(category),
    purchased = purchased
)

fun ShoppingItem.toEntity(): ShoppingItemEntity = ShoppingItemEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit.key,
    category = category.key,
    purchased = purchased
)
