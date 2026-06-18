package com.nutriplan.app.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nutriplan.app.presentation.dashboard.DashboardScreen
import com.nutriplan.app.presentation.navigation.BottomNavItem
import com.nutriplan.app.presentation.navigation.Routes
import com.nutriplan.app.presentation.nutrition.NutritionScreen
import com.nutriplan.app.presentation.planner.PlannerScreen
import com.nutriplan.app.presentation.recipe.RecipeEditScreen
import com.nutriplan.app.presentation.recipe.RecipeListScreen
import com.nutriplan.app.presentation.settings.SettingsScreen
import com.nutriplan.app.presentation.shopping.ShoppingScreen
import com.nutriplan.app.util.Logger

/**
 * Az alkalmazás gyökér komponense: alsó navigáció + navigációs gráf.
 */
@Composable
fun NutriPlanApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Az alsó sávot csak a fő képernyőkön mutatjuk (a szerkesztőn nem)
            if (currentRoute in BottomNavItem.entries.map { it.route }) {
                NavigationBar {
                    BottomNavItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    Logger.d(Logger.Tags.APP, "Navigáció: ${item.route}")
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = {
                                Text(
                                    text = stringResource(item.labelRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Finom, natív képernyőváltó animációk (csúszás + áttűnés)
        val animDuration = 300
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(tween(animDuration)) { it / 6 } + fadeIn(tween(animDuration))
            },
            exitTransition = {
                fadeOut(tween(animDuration))
            },
            popEnterTransition = {
                slideInHorizontally(tween(animDuration)) { -it / 6 } + fadeIn(tween(animDuration))
            },
            popExitTransition = {
                slideOutHorizontally(tween(animDuration)) { it / 6 } + fadeOut(tween(animDuration))
            }
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onOpenNutrition = {
                        navController.navigate(Routes.NUTRITION) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Routes.PLANNER) { PlannerScreen() }

            composable(Routes.RECIPES) {
                RecipeListScreen(
                    onAddRecipe = { navController.navigate(Routes.recipeEdit(0L)) },
                    onEditRecipe = { id -> navController.navigate(Routes.recipeEdit(id)) }
                )
            }

            composable(Routes.SHOPPING) { ShoppingScreen() }

            composable(Routes.NUTRITION) { NutritionScreen() }

            composable(Routes.SETTINGS) { SettingsScreen() }

            composable(
                route = Routes.RECIPE_EDIT_ROUTE,
                arguments = listOf(
                    navArgument(Routes.ARG_RECIPE_ID) {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) {
                RecipeEditScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
