package com.nutriplan.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.nutriplan.app.domain.model.Language
import com.nutriplan.app.domain.model.LengthUnit
import com.nutriplan.app.domain.model.MassUnit
import com.nutriplan.app.domain.model.SeasonalRegion
import com.nutriplan.app.domain.model.ThemeMode
import com.nutriplan.app.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
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

    private val _appLock = MutableStateFlow(readAppLock())
    val appLock: StateFlow<Boolean> = _appLock.asStateFlow()

    private val _proteinGoal = MutableStateFlow(prefs.getInt(KEY_PROTEIN_GOAL, 0))
    /** Egyéni fehérje-cél grammban (0 = automatikus, a kalóriacélból). */
    val proteinGoal: StateFlow<Int> = _proteinGoal.asStateFlow()

    private val _carbsGoal = MutableStateFlow(prefs.getInt(KEY_CARBS_GOAL, 0))
    /** Egyéni szénhidrát-cél grammban (0 = automatikus). */
    val carbsGoal: StateFlow<Int> = _carbsGoal.asStateFlow()

    private val _fatGoal = MutableStateFlow(prefs.getInt(KEY_FAT_GOAL, 0))
    /** Egyéni zsír-cél grammban (0 = automatikus). */
    val fatGoal: StateFlow<Int> = _fatGoal.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    /** Material You (rendszer háttérképből generált) színek használata. */
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _pinSet = MutableStateFlow(readPinSet())
    /** Igaz, ha a felhasználó beállított feloldó PIN-kódot. */
    val pinSet: StateFlow<Boolean> = _pinSet.asStateFlow()

    private val _massUnit = MutableStateFlow(MassUnit.fromKey(prefs.getString(KEY_MASS_UNIT, null)))
    /** Tömeg-mértékegység (kg/lb) a testsúly megjelenítéséhez és beviteléhez. */
    val massUnit: StateFlow<MassUnit> = _massUnit.asStateFlow()

    private val _lengthUnit = MutableStateFlow(LengthUnit.fromKey(prefs.getString(KEY_LENGTH_UNIT, null)))
    /** Hossz-mértékegység (cm/inch) a méretek megjelenítéséhez. */
    val lengthUnit: StateFlow<LengthUnit> = _lengthUnit.asStateFlow()

    private val _seasonalRegion = MutableStateFlow(SeasonalRegion.fromKey(prefs.getString(KEY_SEASONAL_REGION, null)))
    /** Az idény-termékek listájához választott éghajlati régió. */
    val seasonalRegion: StateFlow<SeasonalRegion> = _seasonalRegion.asStateFlow()

    private fun readAppLock(): Boolean = prefs.getBoolean(KEY_APP_LOCK, false)

    private fun readPinSet(): Boolean = !prefs.getString(KEY_PIN_HASH, null).isNullOrEmpty()

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

    /** A biometrikus alkalmazászár ki-/bekapcsolása. */
    fun setAppLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()
        _appLock.value = enabled
        Logger.i(Logger.Tags.SETTINGS, "Alkalmazászár módosítva: $enabled")
    }

    /**
     * Feloldó PIN-kód beállítása. A kódot sózott SHA-256 hash-ként tároljuk,
     * sosem nyílt szövegként.
     */
    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_SALT, salt.toHex())
            .putString(KEY_PIN_HASH, hash)
            .apply()
        _pinSet.value = true
        Logger.i(Logger.Tags.SETTINGS, "Feloldó PIN beállítva")
    }

    /** A beállított PIN törlése. */
    fun clearPin() {
        prefs.edit().remove(KEY_PIN_SALT).remove(KEY_PIN_HASH).apply()
        _pinSet.value = false
        Logger.i(Logger.Tags.SETTINGS, "Feloldó PIN törölve")
    }

    /** Igaz, ha a megadott PIN megegyezik a tárolt kóddal. */
    fun verifyPin(pin: String): Boolean {
        val saltHex = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val stored = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return hashPin(pin, saltHex.fromHex()) == stored
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(pin.toByteArray(Charsets.UTF_8)).toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    /**
     * Egyéni makró-célok beállítása grammban. A 0 érték „automatikus" módot jelent
     * (a kalóriacélból 30/40/30 arány szerint számolva).
     */
    fun setMacroGoals(protein: Int, carbs: Int, fat: Int) {
        val p = protein.coerceIn(0, 1000)
        val c = carbs.coerceIn(0, 1000)
        val f = fat.coerceIn(0, 1000)
        prefs.edit()
            .putInt(KEY_PROTEIN_GOAL, p)
            .putInt(KEY_CARBS_GOAL, c)
            .putInt(KEY_FAT_GOAL, f)
            .apply()
        _proteinGoal.value = p
        _carbsGoal.value = c
        _fatGoal.value = f
        Logger.i(Logger.Tags.SETTINGS, "Makró-célok módosítva: F$p Sz$c Zs$f g")
    }

    /** A Material You dinamikus színek ki-/bekapcsolása. */
    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
        Logger.i(Logger.Tags.SETTINGS, "Dinamikus színek: $enabled")
    }

    /** Tömeg-mértékegység (kg/lb) beállítása. A tárolás kilogrammban marad. */
    fun setMassUnit(unit: MassUnit) {
        prefs.edit().putString(KEY_MASS_UNIT, unit.key).apply()
        _massUnit.value = unit
        Logger.i(Logger.Tags.SETTINGS, "Tömeg-mértékegység: ${unit.key}")
    }

    /** Hossz-mértékegység (cm/inch) beállítása. A tárolás centiméterben marad. */
    fun setLengthUnit(unit: LengthUnit) {
        prefs.edit().putString(KEY_LENGTH_UNIT, unit.key).apply()
        _lengthUnit.value = unit
        Logger.i(Logger.Tags.SETTINGS, "Hossz-mértékegység: ${unit.key}")
    }

    /** Az idény-termékekhez tartozó régió beállítása. */
    fun setSeasonalRegion(region: SeasonalRegion) {
        prefs.edit().putString(KEY_SEASONAL_REGION, region.key).apply()
        _seasonalRegion.value = region
        Logger.i(Logger.Tags.SETTINGS, "Idény-régió: ${region.key}")
    }

    /** Szinkron nyelvi kód lekérés (a Context becsomagolásához használjuk). */
    fun currentLanguageCode(): String = readLanguage().code

    companion object {
        private const val PREFS_NAME = "nutriplan_settings"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CALORIE_GOAL = "calorie_goal"
        private const val KEY_APP_LOCK = "app_lock"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PROTEIN_GOAL = "protein_goal"
        private const val KEY_CARBS_GOAL = "carbs_goal"
        private const val KEY_FAT_GOAL = "fat_goal"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_MASS_UNIT = "mass_unit"
        private const val KEY_LENGTH_UNIT = "length_unit"
        private const val KEY_SEASONAL_REGION = "seasonal_region"
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
