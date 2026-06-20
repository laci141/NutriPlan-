package com.nutriplan.app.presentation.util

import android.content.Context
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.domain.model.ShoppingItem

/**
 * A bevásárlólista szöveges (megosztható) formátumának előállítása.
 * Kategóriánként csoportosít, és a kiválasztott nyelven adja vissza a neveket/egységeket.
 * Nem @Composable, hogy a megosztó Intenthez közvetlenül használható legyen.
 */
object ShoppingShare {

    fun buildText(context: Context, items: List<ShoppingItem>): String {
        val sb = StringBuilder()
        sb.append(context.getString(R.string.shopping_title)).append("\n\n")
        val grouped = items.groupBy { it.category }
        IngredientCategory.entries.forEach { category ->
            val list = grouped[category].orEmpty()
            if (list.isNotEmpty()) {
                sb.append(context.getString(categoryRes(category))).append(":\n")
                list.forEach { item ->
                    val name = localizedName(context, item.nameKey, item.name)
                    val mark = if (item.purchased) "☑ " else "☐ "
                    sb.append("  ").append(mark).append(name)
                        .append(" – ").append(formatQuantity(item.quantity)).append(' ')
                        .append(context.getString(unitRes(item.unit))).append('\n')
                }
                sb.append('\n')
            }
        }
        return sb.toString().trimEnd()
    }

    private fun localizedName(context: Context, nameKey: String?, fallback: String): String {
        if (nameKey.isNullOrBlank()) return fallback
        val resId = context.resources.getIdentifier(nameKey, "string", context.packageName)
        return if (resId != 0) context.getString(resId) else fallback
    }

    private fun categoryRes(category: IngredientCategory): Int = when (category) {
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

    private fun unitRes(unit: MeasurementUnit): Int = when (unit) {
        MeasurementUnit.GRAM -> R.string.unit_g
        MeasurementUnit.KILOGRAM -> R.string.unit_kg
        MeasurementUnit.MILLILITER -> R.string.unit_ml
        MeasurementUnit.LITER -> R.string.unit_l
        MeasurementUnit.PIECE -> R.string.unit_piece
    }
}
