package com.nutriplan.app.data.repository

import com.nutriplan.app.data.local.dao.MealPlanDao
import com.nutriplan.app.data.local.dao.RecipeDao
import com.nutriplan.app.data.local.entity.MealPlanEntity
import com.nutriplan.app.data.mapper.toDomain
import com.nutriplan.app.domain.model.MealAssignment
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.domain.repository.MealPlanRepository
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Heti terv tároló megvalósítás.
 * A terv bejegyzéseket összekapcsolja a receptekkel, hogy teljes hozzárendeléseket adjon vissza.
 */
@Singleton
class MealPlanRepositoryImpl @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val recipeDao: RecipeDao
) : MealPlanRepository {

    override fun getWeeklyPlan(): Flow<List<MealAssignment>> {
        Logger.d(Logger.Tags.PLANNER, "Heti terv lekérdezése (terv + receptek összekapcsolása)")
        // A terv bejegyzéseket és a recepteket összevonjuk egyetlen folyamba
        return combine(
            mealPlanDao.getAll(),
            recipeDao.getRecipesWithIngredients()
        ) { plans, recipes ->
            val recipeMap = recipes.associateBy { it.recipe.id }
            plans.mapNotNull { plan ->
                val recipe = recipeMap[plan.recipeId]?.toDomain() ?: return@mapNotNull null
                MealAssignment(
                    id = plan.id,
                    weekDay = WeekDay.fromKey(plan.weekDay),
                    mealType = MealType.fromKey(plan.mealType),
                    recipe = recipe
                )
            }
        }
    }

    override suspend fun assignRecipe(weekDay: WeekDay, mealType: MealType, recipeId: Long) {
        val id = mealPlanDao.insert(
            MealPlanEntity(
                weekDay = weekDay.key,
                mealType = mealType.key,
                recipeId = recipeId
            )
        )
        Logger.i(
            Logger.Tags.PLANNER,
            "Meal assigned – recept hozzárendelve: nap=${weekDay.key}, étkezés=${mealType.key}, recipeId=$recipeId, tervId=$id"
        )
    }

    override suspend fun removeAssignment(id: Long) {
        mealPlanDao.deleteById(id)
        Logger.i(Logger.Tags.PLANNER, "Heti terv hozzárendelés törölve, id=$id")
    }

    override suspend fun clearWeek() {
        mealPlanDao.clearAll()
        Logger.i(Logger.Tags.PLANNER, "Teljes heti terv törölve")
    }
}
