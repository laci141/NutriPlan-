package com.nutriplan.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nutriplan.app.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// A Dashboard adatait külön DataStore-ban tároljuk (víz). Napi nullázás dátum alapján.
private val Context.dashboardDataStore by preferencesDataStore(name = "dashboard")

/**
 * Dashboard beállítások és napi adatok DataStore-ral (Jetpack DataStore Preferences).
 * A vízfogyasztás minden nap automatikusan nulláról indul (a tárolt dátum alapján).
 */
@Singleton
class DashboardPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val WATER_ML = intPreferencesKey("water_ml")
        val WATER_DATE = stringPreferencesKey("water_date")
    }

    /** A mai elfogyasztott víz (ml). Ha a tárolt dátum nem ma, nullát ad vissza. */
    val waterToday: Flow<Int> = context.dashboardDataStore.data.map { prefs ->
        if (prefs[Keys.WATER_DATE] == today()) prefs[Keys.WATER_ML] ?: 0 else 0
    }

    /** Hozzáad a mai vízmennyiséghez (új nap esetén nulláról indul). */
    suspend fun addWater(milliliters: Int) {
        context.dashboardDataStore.edit { prefs ->
            val isToday = prefs[Keys.WATER_DATE] == today()
            val current = if (isToday) prefs[Keys.WATER_ML] ?: 0 else 0
            prefs[Keys.WATER_ML] = (current + milliliters).coerceAtLeast(0)
            prefs[Keys.WATER_DATE] = today()
        }
        Logger.i(Logger.Tags.SETTINGS, "Víz hozzáadva: +$milliliters ml")
    }

    private fun today(): String = LocalDate.now().toString()
}
