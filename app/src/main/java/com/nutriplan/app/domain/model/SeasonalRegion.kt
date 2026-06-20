package com.nutriplan.app.domain.model

/**
 * Választható éghajlati régió az idény-termékek listájához.
 * A felhasználó a Beállításokban választja ki (nincs geolokáció / GPS-engedély).
 * Minden régióhoz hónaponkénti, jellemző friss zöldség/gyümölcs lista tartozik.
 */
enum class SeasonalRegion(val key: String) {
    CENTRAL_EUROPE("central_europe"),
    MEDITERRANEAN("mediterranean"),
    NORTHERN_EUROPE("northern_europe"),
    NORTH_AMERICA("north_america");

    /** Az adott hónaphoz (1–12) tartozó idény-termékek a régió szerint. */
    fun produceFor(month: Int): List<String> =
        SEASONAL_DATA[this]?.get(month).orEmpty()

    companion object {
        fun fromKey(key: String?): SeasonalRegion =
            entries.firstOrNull { it.key == key } ?: CENTRAL_EUROPE
    }
}

// Hónaponkénti idény-termékek régiónként (északi félteke). Tájékoztató jellegű.
private val SEASONAL_DATA: Map<SeasonalRegion, Map<Int, List<String>>> = mapOf(
    SeasonalRegion.CENTRAL_EUROPE to mapOf(
        1 to listOf("Apple", "Pear", "Cabbage", "Carrot", "Beetroot", "Potato"),
        2 to listOf("Apple", "Pear", "Cabbage", "Carrot", "Leek", "Potato"),
        3 to listOf("Spinach", "Radish", "Spring onion", "Leek", "Carrot"),
        4 to listOf("Spinach", "Radish", "Lettuce", "Asparagus", "Spring onion"),
        5 to listOf("Strawberry", "Lettuce", "Asparagus", "Radish", "Pea"),
        6 to listOf("Strawberry", "Cherry", "Pea", "Cucumber", "Zucchini", "Lettuce"),
        7 to listOf("Apricot", "Peach", "Tomato", "Cucumber", "Pepper", "Cherry"),
        8 to listOf("Plum", "Peach", "Tomato", "Pepper", "Melon", "Corn"),
        9 to listOf("Plum", "Pear", "Apple", "Grape", "Tomato", "Mushroom"),
        10 to listOf("Apple", "Pear", "Grape", "Pumpkin", "Corn", "Beetroot"),
        11 to listOf("Apple", "Pear", "Beetroot", "Carrot", "Cabbage", "Kale"),
        12 to listOf("Apple", "Pear", "Cabbage", "Carrot", "Beetroot", "Potato")
    ),
    SeasonalRegion.MEDITERRANEAN to mapOf(
        1 to listOf("Orange", "Lemon", "Mandarin", "Cabbage", "Fennel", "Spinach"),
        2 to listOf("Orange", "Lemon", "Artichoke", "Fennel", "Spinach", "Broccoli"),
        3 to listOf("Artichoke", "Asparagus", "Strawberry", "Lemon", "Spinach"),
        4 to listOf("Strawberry", "Asparagus", "Artichoke", "Lettuce", "Pea"),
        5 to listOf("Strawberry", "Cherry", "Apricot", "Tomato", "Zucchini"),
        6 to listOf("Apricot", "Peach", "Tomato", "Pepper", "Eggplant", "Melon"),
        7 to listOf("Peach", "Fig", "Tomato", "Pepper", "Eggplant", "Melon"),
        8 to listOf("Fig", "Grape", "Melon", "Tomato", "Pepper", "Eggplant"),
        9 to listOf("Grape", "Fig", "Pomegranate", "Pepper", "Tomato"),
        10 to listOf("Pomegranate", "Grape", "Olive", "Pumpkin", "Cauliflower"),
        11 to listOf("Orange", "Mandarin", "Olive", "Cauliflower", "Spinach"),
        12 to listOf("Orange", "Lemon", "Mandarin", "Cabbage", "Fennel", "Spinach")
    ),
    SeasonalRegion.NORTHERN_EUROPE to mapOf(
        1 to listOf("Apple", "Cabbage", "Carrot", "Potato", "Swede", "Leek"),
        2 to listOf("Apple", "Cabbage", "Carrot", "Potato", "Leek", "Kale"),
        3 to listOf("Leek", "Cabbage", "Carrot", "Spinach", "Rhubarb"),
        4 to listOf("Rhubarb", "Spinach", "Radish", "Spring onion", "Lettuce"),
        5 to listOf("Rhubarb", "Asparagus", "Lettuce", "Radish", "Spinach"),
        6 to listOf("Strawberry", "Pea", "New potato", "Lettuce", "Cucumber"),
        7 to listOf("Strawberry", "Blueberry", "Pea", "Cucumber", "Zucchini"),
        8 to listOf("Blueberry", "Raspberry", "Plum", "Tomato", "Bean", "Corn"),
        9 to listOf("Apple", "Plum", "Pear", "Mushroom", "Beetroot", "Kale"),
        10 to listOf("Apple", "Pear", "Pumpkin", "Beetroot", "Kale", "Leek"),
        11 to listOf("Apple", "Cabbage", "Carrot", "Kale", "Leek", "Swede"),
        12 to listOf("Apple", "Cabbage", "Carrot", "Potato", "Leek", "Kale")
    ),
    SeasonalRegion.NORTH_AMERICA to mapOf(
        1 to listOf("Orange", "Grapefruit", "Kale", "Cabbage", "Sweet potato"),
        2 to listOf("Orange", "Grapefruit", "Kale", "Cabbage", "Carrot"),
        3 to listOf("Spinach", "Radish", "Lettuce", "Broccoli", "Pea"),
        4 to listOf("Asparagus", "Spinach", "Strawberry", "Lettuce", "Pea"),
        5 to listOf("Strawberry", "Asparagus", "Spinach", "Radish", "Lettuce"),
        6 to listOf("Strawberry", "Blueberry", "Cherry", "Zucchini", "Tomato"),
        7 to listOf("Blueberry", "Peach", "Tomato", "Corn", "Cucumber", "Pepper"),
        8 to listOf("Peach", "Watermelon", "Tomato", "Corn", "Pepper", "Bean"),
        9 to listOf("Apple", "Grape", "Tomato", "Squash", "Pepper", "Corn"),
        10 to listOf("Apple", "Pumpkin", "Squash", "Sweet potato", "Beetroot"),
        11 to listOf("Apple", "Pumpkin", "Sweet potato", "Kale", "Cabbage"),
        12 to listOf("Orange", "Grapefruit", "Kale", "Cabbage", "Sweet potato")
    )
)
