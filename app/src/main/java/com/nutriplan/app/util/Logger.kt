package com.nutriplan.app.util

import timber.log.Timber

/**
 * Központi naplózó segédosztály.
 * Az egész alkalmazás ezen keresztül naplóz a Timber könyvtár segítségével,
 * így a logcat címkék következetesek és könnyen szűrhetők maradnak.
 */
object Logger {

    /** Egységes alap címke az összes naplóbejegyzéshez. */
    const val BASE_TAG: String = "NutriPlan"

    // Alrendszerenkénti címkék – minden fontos modulhoz külön, jól szűrhető címke tartozik
    object Tags {
        const val APP = "NutriPlan/App"
        const val DATABASE = "NutriPlan/Database"
        const val REPOSITORY = "NutriPlan/Repository"
        const val RECIPE = "NutriPlan/Recipe"
        const val PLANNER = "NutriPlan/Planner"
        const val SHOPPING = "NutriPlan/Shopping"
        const val NUTRITION = "NutriPlan/Nutrition"
        const val BACKUP = "NutriPlan/Backup"
        const val SETTINGS = "NutriPlan/Settings"
        const val SEED = "NutriPlan/Seed"
        const val VIEWMODEL = "NutriPlan/ViewModel"
    }

    /** Részletes (debug) szintű naplóüzenet írása megadott címkével. */
    fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    /** Információs szintű naplóüzenet írása megadott címkével. */
    fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }

    /** Figyelmeztető szintű naplóüzenet írása megadott címkével. */
    fun w(tag: String, message: String) {
        Timber.tag(tag).w(message)
    }

    /** Hibaszintű naplóüzenet írása megadott címkével, opcionális kivétellel. */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.tag(tag).e(throwable, message)
        } else {
            Timber.tag(tag).e(message)
        }
    }
}
