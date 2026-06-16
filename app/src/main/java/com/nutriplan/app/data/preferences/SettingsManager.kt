package com.nutriplan.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.nutriplan.app.domain.model.Language
import com.nutriplan.app.domain.model.ThemeMode
import com.nutriplan.app.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Beállításkezelő – a nyelvet és a téma módot tárolja SharedPreferences-ben.
 * A SharedPreferences szinkron olvasást tesz lehetővé, ami a nyelvi beállításhoz
 * (attachBaseContext) szükséges. Az aktuális értékeket StateFlow-ként is közzéteszi.
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(readTheme())
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _language = MutableStateFlow(readLanguage())
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _calorieGoal = MutableStateFlow(readCalorieGoal())
    val calorieGoal: StateFlow<Int> = _calorieGoal.asStateFlow()

    private fun readTheme(): ThemeMode =
        ThemeMode.fromKey(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.key) ?: ThemeMode.SYSTEM.key)

    private fun readLanguage(): Language =
        Language.fromCode(prefs.getString(KEY_LANGUAGE, Language.ENGLISH.code) ?: Language.ENGLISH.code)

    private fun readCalorieGoal(): Int =
        prefs.getInt(KEY_CALORIE_GOAL, DEFAULT_CALORIE_GOAL)

    /** A téma mód frissítése és tartós mentése. */
    fun setTheme(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.key).apply()
        _theme.value = mode
        Logger.i(Logger.Tags.SETTINGS, "Téma mód módosítva: ${mode.key}")
    }

    /** A nyelv frissítése és tartós mentése. */
    fun setLanguage(language: Language) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _language.value = language
        Logger.i(Logger.Tags.SETTINGS, "Nyelv módosítva: ${language.code}")
    }

    /**
     * A napi kalóriacél frissítése és tartós mentése.
     * Az értéket ésszerű tartományba szorítjuk (0 = nincs cél).
     */
    fun setCalorieGoal(value: Int) {
        val clamped = value.coerceIn(0, MAX_CALORIE_GOAL)
        prefs.edit().putInt(KEY_CALORIE_GOAL, clamped).apply()
        _calorieGoal.value = clamped
        Logger.i(Logger.Tags.SETTINGS, "Napi kalóriacél módosítva: $clamped kcal")
    }

    /** Szinkron nyelvi kód lekérés (a Context becsomagolásához használjuk). */
    fun currentLanguageCode(): String = readLanguage().code

    companion object {
        private const val PREFS_NAME = "nutriplan_settings"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CALORIE_GOAL = "calorie_goal"
        const val DEFAULT_CALORIE_GOAL = 2000
        const val MAX_CALORIE_GOAL = 10000

        /**
         * Statikus, gyors nyelvi kód olvasás Context alapján.
         * Az Activity.attachBaseContext esetén használjuk, ahol még nincs injektálás.
         */
        fun readLanguageCode(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_LANGUAGE, Language.ENGLISH.code) ?: Language.ENGLISH.code
        }
    }
}
