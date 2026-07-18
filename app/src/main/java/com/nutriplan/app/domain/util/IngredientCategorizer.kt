package com.nutriplan.app.domain.util

import com.nutriplan.app.domain.model.IngredientCategory

/**
 * Hozzávaló-kategorizáló segédobjektum.
 * A hozzávaló neve alapján megpróbálja kitalálni a bevásárlólista kategóriát.
 * Magyar és angol kulcsszavakat is felismer, mert az alapreceptek vegyesek.
 */
object IngredientCategorizer {

    // Kulcsszó -> kategória hozzárendelések (a név kisbetűs változatában keresünk)
    private val keywordMap: List<Pair<List<String>, IngredientCategory>> = listOf(
        listOf(
            "paprika", "paradicsom", "tomato", "uborka", "cucumber", "saláta", "salad",
            "broccoli", "brokkoli", "vegetable", "zöldség", "carrot", "sárgarépa",
            "spinach", "spenót", "onion", "hagyma", "avokádó", "avocado", "leves", "soup"
        ) to IngredientCategory.VEGETABLES,
        listOf(
            "alma", "apple", "banán", "banana", "áfonya", "berries", "berry", "körte",
            "pear", "narancs", "orange", "fruit", "gyümölcs"
        ) to IngredientCategory.FRUITS,
        listOf(
            "csirke", "chicken", "pulyka", "turkey", "marha", "beef", "darált", "minced",
            "hús", "meat", "sonka", "ham"
        ) to IngredientCategory.MEAT,
        listOf(
            "tonhal", "tuna", "lazac", "salmon", "hal", "fish"
        ) to IngredientCategory.FISH,
        listOf(
            "tej", "milk", "joghurt", "yogurt", "yoghurt", "túró", "skyr", "sajt", "cheese",
            "feta", "mozzarella", "cottage", "kefir", "tejföl", "sour cream", "tojás", "egg"
        ) to IngredientCategory.DAIRY,
        listOf(
            "kenyér", "bread", "tortilla", "pékáru", "bakery", "zsemle", "bun"
        ) to IngredientCategory.BAKERY,
        listOf(
            "zabpehely", "zab", "oat", "rizs", "rice", "bulgur", "lencse", "lentil",
            "csicseriborsó", "chickpea", "méz", "honey", "chia", "popcorn", "kukorica",
            "keksz", "biscuit", "hummus", "humusz", "müzli", "puffasztott"
        ) to IngredientCategory.DRY_GOODS,
        listOf(
            "dió", "walnut", "mandula", "almond", "mogyoró", "hazelnut", "mag", "seed",
            "nut", "tökmag", "pumpkin seed"
        ) to IngredientCategory.NUTS
    )

    /** Visszaadja a hozzávaló nevéhez illő kategóriát, ismeretlen esetén OTHER. */
    fun categorize(name: String): IngredientCategory {
        val lower = name.lowercase().trim()
        for ((keywords, category) in keywordMap) {
            if (keywords.any { lower.contains(it) }) {
                return category
            }
        }
        return IngredientCategory.OTHER
    }
}
