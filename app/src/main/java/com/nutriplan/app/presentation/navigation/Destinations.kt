package com.nutriplan.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.nutriplan.app.R

/**
 * Az alkalmazás navigációs útvonalai.
 */
object Routes {
    const val PLANNER = "planner"
    const val RECIPES = "recipes"
    const val SHOPPING = "shopping"
    const val NUTRITION = "nutrition"
    const val SETTINGS = "settings"

    // Recept szerkesztő útvonal opcionális recept azonosítóval
    const val RECIPE_EDIT = "recipe_edit"
    const val ARG_RECIPE_ID = "recipeId"
    const val RECIPE_EDIT_ROUTE = "$RECIPE_EDIT?$ARG_RECIPE_ID={$ARG_RECIPE_ID}"

    /** Szerkesztő útvonal felépítése adott (vagy új) recepthez. */
    fun recipeEdit(recipeId: Long = 0L): String = "$RECIPE_EDIT?$ARG_RECIPE_ID=$recipeId"
}

/**
 * Az alsó navigációs sáv elemei.
 */
enum class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    PLANNER(Routes.PLANNER, R.string.nav_planner, Icons.Filled.CalendarMonth),
    RECIPES(Routes.RECIPES, R.string.nav_recipes, Icons.AutoMirrored.Filled.MenuBook),
    SHOPPING(Routes.SHOPPING, R.string.nav_shopping, Icons.Filled.ShoppingCart),
    NUTRITION(Routes.NUTRITION, R.string.nav_nutrition, Icons.Filled.PieChart),
    SETTINGS(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings)
}
