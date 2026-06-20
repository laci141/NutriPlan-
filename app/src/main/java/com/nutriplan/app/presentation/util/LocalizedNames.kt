package com.nutriplan.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.nutriplan.app.domain.model.Ingredient
import com.nutriplan.app.domain.model.Recipe
import com.nutriplan.app.domain.model.ShoppingItem

/**
 * Fordítási kulcs alapján a megfelelő nyelvű nevet adja vissza.
 * Az alapreceptek és -hozzávalók kulccsal (nameKey) rendelkeznek, így a kiválasztott
 * nyelven jelennek meg. A felhasználó által létrehozott elemeknél nincs kulcs,
 * ilyenkor a literál nevet mutatjuk.
 */
@Composable
fun localizedName(nameKey: String?, fallback: String): String {
    if (nameKey.isNullOrBlank()) return fallback
    val context = LocalContext.current
    // A kulcsból string erőforrás-azonosítót keresünk; ha nincs, marad a literál név
    val resId = context.resources.getIdentifier(nameKey, "string", context.packageName)
    return if (resId != 0) context.getString(resId) else fallback
}

/** A recept megjelenítendő (lefordított) neve. */
@Composable
fun Recipe.displayName(): String = localizedName(nameKey, name)

/** A hozzávaló megjelenítendő (lefordított) neve. */
@Composable
fun Ingredient.displayName(): String = localizedName(nameKey, name)

/** A bevásárlólista tétel megjelenítendő (lefordított) neve. */
@Composable
fun ShoppingItem.displayName(): String = localizedName(nameKey, name)
