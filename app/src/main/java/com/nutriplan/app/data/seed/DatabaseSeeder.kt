package com.nutriplan.app.data.seed

import com.nutriplan.app.domain.repository.RecipeRepository
import com.nutriplan.app.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adatbázis feltöltő.
 * Az első indításkor (ha még nincs recept) betölti az alapértelmezett recepteket.
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val recipeRepository: RecipeRepository
) {

    /** Lefuttatja a feltöltést, ha az adatbázis üres. */
    suspend fun seedIfNeeded() {
        Logger.i(Logger.Tags.SEED, "Adatbázis feltöltés ellenőrzése indul")
        val count = recipeRepository.countRecipes()
        if (count > 0) {
            Logger.i(Logger.Tags.SEED, "Az adatbázis már tartalmaz $count receptet, feltöltés kihagyva")
            return
        }

        Logger.i(Logger.Tags.SEED, "Üres adatbázis – alapértelmezett receptek betöltése")
        val recipes = DefaultRecipes.all()
        for (recipe in recipes) {
            recipeRepository.saveRecipe(recipe)
        }
        Logger.i(Logger.Tags.SEED, "Database seeded – ${recipes.size} alaprecept betöltve")
    }
}
