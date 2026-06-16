package com.nutriplan.app.domain.model

/**
 * Étkezés típusok – a napi terv öt rögzített étkezési idősávja.
 * A [key] érték stabil, adatbázisban és JSON exportban ezt tároljuk
 * (a felhasználói felületen megjelenő szöveget külön fordítjuk).
 */
enum class MealType(val key: String) {
    BREAKFAST("breakfast"),
    MORNING_SNACK("morning_snack"),
    LUNCH("lunch"),
    AFTERNOON_SNACK("afternoon_snack"),
    DINNER("dinner");

    companion object {
        /** Kulcs alapján visszaadja az étkezés típust, ismeretlen esetén reggeli. */
        fun fromKey(key: String): MealType =
            entries.firstOrNull { it.key == key } ?: BREAKFAST
    }
}

/**
 * A hét napjai rögzített sorrendben (hétfőtől vasárnapig).
 */
enum class WeekDay(val key: String) {
    MONDAY("monday"),
    TUESDAY("tuesday"),
    WEDNESDAY("wednesday"),
    THURSDAY("thursday"),
    FRIDAY("friday"),
    SATURDAY("saturday"),
    SUNDAY("sunday");

    companion object {
        fun fromKey(key: String): WeekDay =
            entries.firstOrNull { it.key == key } ?: MONDAY
    }
}

/**
 * Mértékegységek a hozzávalókhoz.
 */
enum class MeasurementUnit(val key: String) {
    GRAM("g"),
    KILOGRAM("kg"),
    MILLILITER("ml"),
    LITER("l"),
    PIECE("piece");

    companion object {
        fun fromKey(key: String): MeasurementUnit =
            entries.firstOrNull { it.key == key } ?: GRAM
    }
}

/**
 * Bevásárlólista kategóriák – ezek szerint csoportosítjuk a tételeket.
 */
enum class IngredientCategory(val key: String) {
    VEGETABLES("vegetables"),
    FRUITS("fruits"),
    MEAT("meat"),
    FISH("fish"),
    DAIRY("dairy"),
    BAKERY("bakery"),
    DRY_GOODS("dry_goods"),
    NUTS("nuts"),
    OTHER("other");

    companion object {
        fun fromKey(key: String): IngredientCategory =
            entries.firstOrNull { it.key == key } ?: OTHER
    }
}

/**
 * Támogatott nyelvek a beállításokban.
 */
enum class Language(val code: String) {
    HUNGARIAN("hu"),
    ENGLISH("en"),
    ROMANIAN("ro");

    companion object {
        fun fromCode(code: String): Language =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

/**
 * Téma mód – világos, sötét vagy a rendszer beállítását követő.
 */
enum class ThemeMode(val key: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromKey(key: String): ThemeMode =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}
