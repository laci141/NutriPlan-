package com.nutriplan.app

import android.app.Application
import com.nutriplan.app.data.seed.DatabaseSeeder
import com.nutriplan.app.util.Logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Az alkalmazás belépési pontja (Application osztály).
 * Itt inicializáljuk a naplózást (Timber) és töltjük be az alapadatokat.
 */
@HiltAndroidApp
class NutriPlanApplication : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    // Háttér-hatókör az indítási feladatokhoz (alapadat-betöltés)
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Timber naplózó beültetése – debug build esetén részletes fa
        Timber.plant(Timber.DebugTree())
        Logger.i(Logger.Tags.APP, "Application started – NutriPlan alkalmazás elindult")

        // Alapadatok betöltése háttérszálon (első indításkor)
        applicationScope.launch {
            Logger.i(Logger.Tags.APP, "Indítási szekvencia – alapadat-betöltés indítása")
            try {
                databaseSeeder.seedIfNeeded()
                Logger.i(Logger.Tags.APP, "Indítási szekvencia befejeződött")
            } catch (e: Exception) {
                Logger.e(Logger.Tags.APP, "Hiba az alapadat-betöltés során", e)
            }
        }
    }
}
