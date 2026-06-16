package com.nutriplan.app.data.repository

import com.nutriplan.app.data.backup.BackupData
import com.nutriplan.app.data.backup.IngredientDto
import com.nutriplan.app.data.backup.MealPlanDto
import com.nutriplan.app.data.backup.RecipeDto
import com.nutriplan.app.data.backup.ShoppingItemDto
import com.nutriplan.app.data.local.dao.MealPlanDao
import com.nutriplan.app.data.local.dao.RecipeDao
import com.nutriplan.app.data.local.dao.ShoppingDao
import com.nutriplan.app.data.local.entity.IngredientEntity
import com.nutriplan.app.data.local.entity.MealPlanEntity
import com.nutriplan.app.data.local.entity.RecipeEntity
import com.nutriplan.app.data.local.entity.ShoppingItemEntity
import com.nutriplan.app.domain.repository.BackupRepository
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mentés/visszatöltés tároló megvalósítás JSON formátummal.
 */
@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val mealPlanDao: MealPlanDao,
    private val shoppingDao: ShoppingDao
) : BackupRepository {

    // Olvasható, formázott JSON, ismeretlen kulcsokat figyelmen kívül hagyva importnál
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override suspend fun exportToJson(): String {
        Logger.i(Logger.Tags.BACKUP, "Export started – adatok JSON exportja indul")
        val recipes = recipeDao.getRecipesWithIngredients().first()
        val plans = mealPlanDao.getAllOnce()
        val shopping = shoppingDao.getAll().first()

        val backup = BackupData(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            recipes = recipes.map { rwi ->
                RecipeDto(
                    id = rwi.recipe.id,
                    name = rwi.recipe.name,
                    mealType = rwi.recipe.mealType,
                    calories = rwi.recipe.calories,
                    protein = rwi.recipe.protein,
                    carbs = rwi.recipe.carbs,
                    fat = rwi.recipe.fat,
                    isDefault = rwi.recipe.isDefault,
                    ingredients = rwi.ingredients.map {
                        IngredientDto(it.name, it.quantity, it.unit, it.category)
                    }
                )
            },
            mealPlans = plans.map { MealPlanDto(it.weekDay, it.mealType, it.recipeId) },
            shoppingItems = shopping.map {
                ShoppingItemDto(it.name, it.quantity, it.unit, it.category, it.purchased)
            }
        )

        val result = json.encodeToString(BackupData.serializer(), backup)
        Logger.i(
            Logger.Tags.BACKUP,
            "Export completed – ${backup.recipes.size} recept, ${backup.mealPlans.size} terv, ${backup.shoppingItems.size} bevásárló tétel"
        )
        return result
    }

    override suspend fun importFromJson(json: String) {
        Logger.i(Logger.Tags.BACKUP, "Import started – adatok JSON importja indul")
        val backup = this.json.decodeFromString(BackupData.serializer(), json)

        // A meglévő adatok törlése, hogy a visszatöltés tiszta állapotból induljon
        mealPlanDao.clearAll()
        shoppingDao.clearAll()
        // A recepteket egyenként töröljük – a régi azonosítók nem feltétlen egyeznek
        recipeDao.getRecipesWithIngredients().first().forEach {
            recipeDao.deleteRecipe(it.recipe)
        }

        // Receptek visszatöltése – a régi id-t megtartjuk, hogy a terv hivatkozások működjenek
        for (recipeDto in backup.recipes) {
            val recipeId = recipeDao.insertRecipe(
                RecipeEntity(
                    id = recipeDto.id,
                    name = recipeDto.name,
                    mealType = recipeDto.mealType,
                    calories = recipeDto.calories,
                    protein = recipeDto.protein,
                    carbs = recipeDto.carbs,
                    fat = recipeDto.fat,
                    isDefault = recipeDto.isDefault
                )
            )
            recipeDao.insertIngredients(
                recipeDto.ingredients.map {
                    IngredientEntity(
                        recipeId = recipeId,
                        name = it.name,
                        quantity = it.quantity,
                        unit = it.unit,
                        category = it.category
                    )
                }
            )
        }

        // Heti terv visszatöltése
        for (planDto in backup.mealPlans) {
            mealPlanDao.insert(
                MealPlanEntity(
                    weekDay = planDto.weekDay,
                    mealType = planDto.mealType,
                    recipeId = planDto.recipeId
                )
            )
        }

        // Bevásárlólista visszatöltése
        shoppingDao.insertAll(
            backup.shoppingItems.map {
                ShoppingItemEntity(
                    name = it.name,
                    quantity = it.quantity,
                    unit = it.unit,
                    category = it.category,
                    purchased = it.purchased
                )
            }
        )

        Logger.i(
            Logger.Tags.BACKUP,
            "Import completed – ${backup.recipes.size} recept, ${backup.mealPlans.size} terv, ${backup.shoppingItems.size} bevásárló tétel visszatöltve"
        )
    }
}
