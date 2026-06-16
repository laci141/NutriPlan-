package com.nutriplan.app.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.model.Language
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.domain.model.ThemeMode
import com.nutriplan.app.domain.model.WeekDay

/**
 * Megjelenítési címke-leképezések.
 * Az enumokat a megfelelő, lefordított szöveges erőforrásra fordítják a felhasználói felülethez.
 */

@Composable
fun MealType.label(): String = stringResource(
    when (this) {
        MealType.BREAKFAST -> R.string.meal_breakfast
        MealType.MORNING_SNACK -> R.string.meal_morning_snack
        MealType.LUNCH -> R.string.meal_lunch
        MealType.AFTERNOON_SNACK -> R.string.meal_afternoon_snack
        MealType.DINNER -> R.string.meal_dinner
    }
)

@Composable
fun WeekDay.label(): String = stringResource(
    when (this) {
        WeekDay.MONDAY -> R.string.day_monday
        WeekDay.TUESDAY -> R.string.day_tuesday
        WeekDay.WEDNESDAY -> R.string.day_wednesday
        WeekDay.THURSDAY -> R.string.day_thursday
        WeekDay.FRIDAY -> R.string.day_friday
        WeekDay.SATURDAY -> R.string.day_saturday
        WeekDay.SUNDAY -> R.string.day_sunday
    }
)

@Composable
fun MeasurementUnit.label(): String = stringResource(
    when (this) {
        MeasurementUnit.GRAM -> R.string.unit_g
        MeasurementUnit.KILOGRAM -> R.string.unit_kg
        MeasurementUnit.MILLILITER -> R.string.unit_ml
        MeasurementUnit.LITER -> R.string.unit_l
        MeasurementUnit.PIECE -> R.string.unit_piece
    }
)

@Composable
fun IngredientCategory.label(): String = stringResource(
    when (this) {
        IngredientCategory.VEGETABLES -> R.string.cat_vegetables
        IngredientCategory.FRUITS -> R.string.cat_fruits
        IngredientCategory.MEAT -> R.string.cat_meat
        IngredientCategory.FISH -> R.string.cat_fish
        IngredientCategory.DAIRY -> R.string.cat_dairy
        IngredientCategory.BAKERY -> R.string.cat_bakery
        IngredientCategory.DRY_GOODS -> R.string.cat_dry_goods
        IngredientCategory.NUTS -> R.string.cat_nuts
        IngredientCategory.OTHER -> R.string.cat_other
    }
)

@Composable
fun ThemeMode.label(): String = stringResource(
    when (this) {
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
        ThemeMode.SYSTEM -> R.string.theme_system
    }
)

@Composable
fun Language.label(): String = stringResource(
    when (this) {
        Language.HUNGARIAN -> R.string.lang_hungarian
        Language.ENGLISH -> R.string.lang_english
        Language.ROMANIAN -> R.string.lang_romanian
    }
)

/** Szám formázása felesleges tizedesek nélkül (pl. 540.0 -> "540", 1.5 -> "1.5"). */
fun formatQuantity(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else value.toString().trimEnd('0').trimEnd('.')
