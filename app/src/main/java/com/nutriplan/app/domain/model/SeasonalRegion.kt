package com.nutriplan.app.domain.model

/**
 * Egy idény-termék (zöldség/gyümölcs) a megjelenített nevével mindhárom nyelven.
 * Így a kezdőlapi idény-kártya a beállított nyelven mutatja a neveket
 * (magyar / román / angol), nem csak angolul.
 */
enum class Produce(val en: String, val hu: String, val ro: String) {
    // Gyümölcsök
    APPLE("Apple", "Alma", "Mere"),
    PEAR("Pear", "Körte", "Pere"),
    CHERRY("Cherry", "Cseresznye", "Cireșe"),
    SOUR_CHERRY("Sour cherry", "Meggy", "Vișine"),
    STRAWBERRY("Strawberry", "Eper", "Căpșuni"),
    RASPBERRY("Raspberry", "Málna", "Zmeură"),
    BLACKBERRY("Blackberry", "Szeder", "Mure"),
    BLUEBERRY("Blueberry", "Áfonya", "Afine"),
    REDCURRANT("Redcurrant", "Ribizli", "Coacăze"),
    GOOSEBERRY("Gooseberry", "Egres", "Agrișe"),
    APRICOT("Apricot", "Kajszibarack", "Caise"),
    PEACH("Peach", "Őszibarack", "Piersici"),
    PLUM("Plum", "Szilva", "Prune"),
    GRAPE("Grape", "Szőlő", "Struguri"),
    MELON("Melon", "Sárgadinnye", "Pepene galben"),
    WATERMELON("Watermelon", "Görögdinnye", "Pepene roșu"),
    FIG("Fig", "Füge", "Smochine"),
    QUINCE("Quince", "Birsalma", "Gutui"),
    WALNUT("Walnut", "Dió", "Nuci"),
    CHESTNUT("Chestnut", "Gesztenye", "Castane"),
    ORANGE("Orange", "Narancs", "Portocale"),
    LEMON("Lemon", "Citrom", "Lămâi"),
    MANDARIN("Mandarin", "Mandarin", "Mandarine"),
    GRAPEFRUIT("Grapefruit", "Grapefruit", "Grepfrut"),
    POMEGRANATE("Pomegranate", "Gránátalma", "Rodii"),
    OLIVE("Olive", "Olíva", "Măsline"),
    RHUBARB("Rhubarb", "Rebarbara", "Rubarbă"),

    // Zöldségek
    CABBAGE("Cabbage", "Káposzta", "Varză"),
    CARROT("Carrot", "Sárgarépa", "Morcovi"),
    BEETROOT("Beetroot", "Cékla", "Sfeclă roșie"),
    POTATO("Potato", "Burgonya", "Cartofi"),
    NEW_POTATO("New potato", "Újkrumpli", "Cartofi noi"),
    SWEET_POTATO("Sweet potato", "Édesburgonya", "Cartofi dulci"),
    LEEK("Leek", "Póréhagyma", "Praz"),
    SPRING_ONION("Spring onion", "Újhagyma", "Ceapă verde"),
    ONION("Onion", "Vöröshagyma", "Ceapă"),
    GARLIC("Garlic", "Fokhagyma", "Usturoi"),
    SPINACH("Spinach", "Spenót", "Spanac"),
    SORREL("Sorrel", "Sóska", "Măcriș"),
    RADISH("Radish", "Retek", "Ridichi"),
    LETTUCE("Lettuce", "Saláta", "Salată verde"),
    ASPARAGUS("Asparagus", "Spárga", "Sparanghel"),
    PEA("Pea", "Zöldborsó", "Mazăre"),
    GREEN_BEAN("Green bean", "Zöldbab", "Fasole verde"),
    BEAN("Bean", "Bab", "Fasole"),
    CUCUMBER("Cucumber", "Uborka", "Castraveți"),
    ZUCCHINI("Zucchini", "Cukkini", "Dovlecel"),
    TOMATO("Tomato", "Paradicsom", "Roșii"),
    PEPPER("Pepper", "Paprika", "Ardei"),
    EGGPLANT("Eggplant", "Padlizsán", "Vinete"),
    CORN("Corn", "Kukorica", "Porumb"),
    PUMPKIN("Pumpkin", "Sütőtök", "Dovleac"),
    SQUASH("Squash", "Tök", "Dovlecei"),
    MUSHROOM("Mushroom", "Gomba", "Ciuperci"),
    CAULIFLOWER("Cauliflower", "Karfiol", "Conopidă"),
    BROCCOLI("Broccoli", "Brokkoli", "Broccoli"),
    KALE("Kale", "Fodros kel", "Kale"),
    KOHLRABI("Kohlrabi", "Karalábé", "Gulie"),
    CELERIAC("Celeriac", "Zeller", "Țelină"),
    SWEDE("Swede", "Karórépa", "Nap suedez"),
    FENNEL("Fennel", "Édeskömény", "Fenicul"),
    ARTICHOKE("Artichoke", "Articsóka", "Anghinare");

    /** A termék neve a megadott nyelven (alapértelmezés: angol). */
    fun localized(language: Language): String = when (language) {
        Language.HUNGARIAN -> hu
        Language.ROMANIAN -> ro
        Language.ENGLISH -> en
    }
}

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

    /**
     * Az adott hónaphoz (1–12) tartozó idény-termékek a régió szerint,
     * a megadott nyelvre lefordított nevekkel.
     */
    fun produceFor(month: Int, language: Language): List<String> =
        SEASONAL_DATA[this]?.get(month).orEmpty().map { it.localized(language) }

    companion object {
        fun fromKey(key: String?): SeasonalRegion =
            entries.firstOrNull { it.key == key } ?: CENTRAL_EUROPE
    }
}

// Hónaponkénti idény-termékek régiónként (északi félteke). Tájékoztató jellegű.
// A nevek a Produce enumból jönnek, így minden nyelven helyesen jelennek meg.
private val SEASONAL_DATA: Map<SeasonalRegion, Map<Int, List<Produce>>> = mapOf(
    // Közép-Európa (Magyarország / Románia) – bővített, helyi idénynaptár alapján.
    SeasonalRegion.CENTRAL_EUROPE to mapOf(
        1 to listOf(Produce.APPLE, Produce.PEAR, Produce.CABBAGE, Produce.CARROT, Produce.BEETROOT, Produce.POTATO, Produce.ONION, Produce.WALNUT),
        2 to listOf(Produce.APPLE, Produce.PEAR, Produce.CABBAGE, Produce.CARROT, Produce.LEEK, Produce.POTATO, Produce.ONION, Produce.CELERIAC),
        3 to listOf(Produce.SPINACH, Produce.RADISH, Produce.SPRING_ONION, Produce.LEEK, Produce.CARROT, Produce.SORREL, Produce.LETTUCE),
        4 to listOf(Produce.SPINACH, Produce.RADISH, Produce.LETTUCE, Produce.ASPARAGUS, Produce.SPRING_ONION, Produce.SORREL, Produce.KOHLRABI, Produce.RHUBARB),
        5 to listOf(Produce.STRAWBERRY, Produce.LETTUCE, Produce.ASPARAGUS, Produce.RADISH, Produce.PEA, Produce.KOHLRABI, Produce.SPRING_ONION, Produce.RHUBARB, Produce.SPINACH),
        6 to listOf(Produce.STRAWBERRY, Produce.SOUR_CHERRY, Produce.CHERRY, Produce.RASPBERRY, Produce.REDCURRANT, Produce.GOOSEBERRY, Produce.PEA, Produce.GREEN_BEAN, Produce.CUCUMBER, Produce.ZUCCHINI, Produce.LETTUCE, Produce.KOHLRABI, Produce.NEW_POTATO),
        7 to listOf(Produce.APRICOT, Produce.PEACH, Produce.SOUR_CHERRY, Produce.RASPBERRY, Produce.PLUM, Produce.MELON, Produce.TOMATO, Produce.CUCUMBER, Produce.PEPPER, Produce.ZUCCHINI, Produce.GREEN_BEAN, Produce.CORN),
        8 to listOf(Produce.PLUM, Produce.PEACH, Produce.GRAPE, Produce.WATERMELON, Produce.MELON, Produce.TOMATO, Produce.PEPPER, Produce.EGGPLANT, Produce.CORN, Produce.GREEN_BEAN, Produce.ZUCCHINI, Produce.APPLE),
        9 to listOf(Produce.PLUM, Produce.PEAR, Produce.APPLE, Produce.GRAPE, Produce.QUINCE, Produce.TOMATO, Produce.PEPPER, Produce.MUSHROOM, Produce.WALNUT, Produce.CORN, Produce.PUMPKIN),
        10 to listOf(Produce.APPLE, Produce.PEAR, Produce.GRAPE, Produce.PUMPKIN, Produce.QUINCE, Produce.WALNUT, Produce.CHESTNUT, Produce.CORN, Produce.BEETROOT, Produce.CABBAGE),
        11 to listOf(Produce.APPLE, Produce.PEAR, Produce.QUINCE, Produce.BEETROOT, Produce.CARROT, Produce.CABBAGE, Produce.KALE, Produce.LEEK, Produce.WALNUT, Produce.CHESTNUT),
        12 to listOf(Produce.APPLE, Produce.PEAR, Produce.CABBAGE, Produce.CARROT, Produce.BEETROOT, Produce.POTATO, Produce.ONION, Produce.WALNUT)
    ),
    SeasonalRegion.MEDITERRANEAN to mapOf(
        1 to listOf(Produce.ORANGE, Produce.LEMON, Produce.MANDARIN, Produce.CABBAGE, Produce.FENNEL, Produce.SPINACH),
        2 to listOf(Produce.ORANGE, Produce.LEMON, Produce.ARTICHOKE, Produce.FENNEL, Produce.SPINACH, Produce.BROCCOLI),
        3 to listOf(Produce.ARTICHOKE, Produce.ASPARAGUS, Produce.STRAWBERRY, Produce.LEMON, Produce.SPINACH),
        4 to listOf(Produce.STRAWBERRY, Produce.ASPARAGUS, Produce.ARTICHOKE, Produce.LETTUCE, Produce.PEA),
        5 to listOf(Produce.STRAWBERRY, Produce.CHERRY, Produce.APRICOT, Produce.TOMATO, Produce.ZUCCHINI),
        6 to listOf(Produce.APRICOT, Produce.PEACH, Produce.TOMATO, Produce.PEPPER, Produce.EGGPLANT, Produce.MELON),
        7 to listOf(Produce.PEACH, Produce.FIG, Produce.TOMATO, Produce.PEPPER, Produce.EGGPLANT, Produce.MELON),
        8 to listOf(Produce.FIG, Produce.GRAPE, Produce.MELON, Produce.TOMATO, Produce.PEPPER, Produce.EGGPLANT),
        9 to listOf(Produce.GRAPE, Produce.FIG, Produce.POMEGRANATE, Produce.PEPPER, Produce.TOMATO),
        10 to listOf(Produce.POMEGRANATE, Produce.GRAPE, Produce.OLIVE, Produce.PUMPKIN, Produce.CAULIFLOWER),
        11 to listOf(Produce.ORANGE, Produce.MANDARIN, Produce.OLIVE, Produce.CAULIFLOWER, Produce.SPINACH),
        12 to listOf(Produce.ORANGE, Produce.LEMON, Produce.MANDARIN, Produce.CABBAGE, Produce.FENNEL, Produce.SPINACH)
    ),
    SeasonalRegion.NORTHERN_EUROPE to mapOf(
        1 to listOf(Produce.APPLE, Produce.CABBAGE, Produce.CARROT, Produce.POTATO, Produce.SWEDE, Produce.LEEK),
        2 to listOf(Produce.APPLE, Produce.CABBAGE, Produce.CARROT, Produce.POTATO, Produce.LEEK, Produce.KALE),
        3 to listOf(Produce.LEEK, Produce.CABBAGE, Produce.CARROT, Produce.SPINACH, Produce.RHUBARB),
        4 to listOf(Produce.RHUBARB, Produce.SPINACH, Produce.RADISH, Produce.SPRING_ONION, Produce.LETTUCE),
        5 to listOf(Produce.RHUBARB, Produce.ASPARAGUS, Produce.LETTUCE, Produce.RADISH, Produce.SPINACH),
        6 to listOf(Produce.STRAWBERRY, Produce.PEA, Produce.NEW_POTATO, Produce.LETTUCE, Produce.CUCUMBER),
        7 to listOf(Produce.STRAWBERRY, Produce.BLUEBERRY, Produce.PEA, Produce.CUCUMBER, Produce.ZUCCHINI),
        8 to listOf(Produce.BLUEBERRY, Produce.RASPBERRY, Produce.PLUM, Produce.TOMATO, Produce.BEAN, Produce.CORN),
        9 to listOf(Produce.APPLE, Produce.PLUM, Produce.PEAR, Produce.MUSHROOM, Produce.BEETROOT, Produce.KALE),
        10 to listOf(Produce.APPLE, Produce.PEAR, Produce.PUMPKIN, Produce.BEETROOT, Produce.KALE, Produce.LEEK),
        11 to listOf(Produce.APPLE, Produce.CABBAGE, Produce.CARROT, Produce.KALE, Produce.LEEK, Produce.SWEDE),
        12 to listOf(Produce.APPLE, Produce.CABBAGE, Produce.CARROT, Produce.POTATO, Produce.LEEK, Produce.KALE)
    ),
    SeasonalRegion.NORTH_AMERICA to mapOf(
        1 to listOf(Produce.ORANGE, Produce.GRAPEFRUIT, Produce.KALE, Produce.CABBAGE, Produce.SWEET_POTATO),
        2 to listOf(Produce.ORANGE, Produce.GRAPEFRUIT, Produce.KALE, Produce.CABBAGE, Produce.CARROT),
        3 to listOf(Produce.SPINACH, Produce.RADISH, Produce.LETTUCE, Produce.BROCCOLI, Produce.PEA),
        4 to listOf(Produce.ASPARAGUS, Produce.SPINACH, Produce.STRAWBERRY, Produce.LETTUCE, Produce.PEA),
        5 to listOf(Produce.STRAWBERRY, Produce.ASPARAGUS, Produce.SPINACH, Produce.RADISH, Produce.LETTUCE),
        6 to listOf(Produce.STRAWBERRY, Produce.BLUEBERRY, Produce.CHERRY, Produce.ZUCCHINI, Produce.TOMATO),
        7 to listOf(Produce.BLUEBERRY, Produce.PEACH, Produce.TOMATO, Produce.CORN, Produce.CUCUMBER, Produce.PEPPER),
        8 to listOf(Produce.PEACH, Produce.WATERMELON, Produce.TOMATO, Produce.CORN, Produce.PEPPER, Produce.BEAN),
        9 to listOf(Produce.APPLE, Produce.GRAPE, Produce.TOMATO, Produce.SQUASH, Produce.PEPPER, Produce.CORN),
        10 to listOf(Produce.APPLE, Produce.PUMPKIN, Produce.SQUASH, Produce.SWEET_POTATO, Produce.BEETROOT),
        11 to listOf(Produce.APPLE, Produce.PUMPKIN, Produce.SWEET_POTATO, Produce.KALE, Produce.CABBAGE),
        12 to listOf(Produce.ORANGE, Produce.GRAPEFRUIT, Produce.KALE, Produce.CABBAGE, Produce.SWEET_POTATO)
    )
)
