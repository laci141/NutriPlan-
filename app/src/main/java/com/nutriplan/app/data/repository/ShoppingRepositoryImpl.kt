package com.nutriplan.app.data.repository

import com.nutriplan.app.data.local.dao.MealPlanDao
import com.nutriplan.app.data.local.dao.RecipeDao
import com.nutriplan.app.data.local.dao.ShoppingDao
import com.nutriplan.app.data.local.entity.ShoppingItemEntity
import com.nutriplan.app.data.mapper.toDomain
import com.nutriplan.app.domain.model.ShoppingItem
import com.nutriplan.app.domain.repository.ShoppingRepository
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bevásárlólista tároló megvalósítás.
 * A heti tervből automatikusan generálja a listát, az azonos hozzávalókat összevonva.
 */
@Singleton
class ShoppingRepositoryImpl @Inject constructor(
    private val shoppingDao: ShoppingDao,
    private val mealPlanDao: MealPlanDao,
    private val recipeDao: RecipeDao
) : ShoppingRepository {

    override fun getShoppingList(): Flow<List<ShoppingItem>> {
        Logger.d(Logger.Tags.SHOPPING, "Bevásárlólista lekérdezése (Flow)")
        return shoppingDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun generateFromPlan(): Int {
        Logger.i(Logger.Tags.SHOPPING, "Shopping list generation started – generálás indítása a heti tervből")
        val plans = mealPlanDao.getAllOnce()
        Logger.d(Logger.Tags.SHOPPING, "Heti terv bejegyzések száma: ${plans.size}")

        // A receptek azonosító szerinti gyors elérése (az aktuális pillanatkép)
        val recipes = recipeDao.getRecipesWithIngredients().first()
        val recipeMap = recipes.associateBy { it.recipe.id }

        // Hozzávalók összegyűjtése és összevonása kulcs (név + mértékegység) szerint
        val merged = LinkedHashMap<String, ShoppingItemEntity>()
        var totalIngredients = 0
        for (plan in plans) {
            val recipe = recipeMap[plan.recipeId] ?: continue
            for (ingredient in recipe.ingredients) {
                totalIngredients++
                val key = ingredient.name.lowercase().trim() + "|" + ingredient.unit
                val existing = merged[key]
                if (existing == null) {
                    merged[key] = ShoppingItemEntity(
                        name = ingredient.name,
                        quantity = ingredient.quantity,
                        unit = ingredient.unit,
                        category = ingredient.category,
                        purchased = false
                    )
                } else {
                    // Azonos hozzávaló mennyiségének összeadása
                    merged[key] = existing.copy(quantity = existing.quantity + ingredient.quantity)
                }
            }
        }

        // A korábbi lista törlése, majd az új, összevont tételek mentése
        shoppingDao.clearAll()
        val items = merged.values.toList()
        shoppingDao.insertAll(items)
        Logger.i(
            Logger.Tags.SHOPPING,
            "Shopping list generated – $totalIngredients hozzávalóból ${items.size} összevont tétel keletkezett"
        )
        return items.size
    }

    override suspend fun setPurchased(id: Long, purchased: Boolean) {
        shoppingDao.updatePurchased(id, purchased)
        Logger.i(Logger.Tags.SHOPPING, "Bevásárlólista tétel megvásárolt állapota: id=$id, purchased=$purchased")
    }

    override suspend fun deleteItem(id: Long) {
        shoppingDao.deleteById(id)
        Logger.i(Logger.Tags.SHOPPING, "Bevásárlólista tétel törölve: id=$id")
    }

    override suspend fun clearPurchased() {
        shoppingDao.clearPurchased()
        Logger.i(Logger.Tags.SHOPPING, "Megvásárolt tételek törölve a bevásárlólistáról")
    }

    override suspend fun clearAll() {
        shoppingDao.clearAll()
        Logger.i(Logger.Tags.SHOPPING, "Teljes bevásárlólista törölve")
    }
}
