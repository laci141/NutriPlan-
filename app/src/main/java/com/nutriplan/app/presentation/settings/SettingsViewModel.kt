package com.nutriplan.app.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.data.preferences.SettingsManager
import com.nutriplan.app.domain.model.Language
import com.nutriplan.app.domain.model.ThemeMode
import com.nutriplan.app.domain.usecase.ExportDataUseCase
import com.nutriplan.app.domain.usecase.ImportDataUseCase
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * A beállítások képernyő egyszeri eseményei (visszajelzések).
 */
sealed interface SettingsEvent {
    data object ExportSuccess : SettingsEvent
    data object ExportError : SettingsEvent
    data object ImportSuccess : SettingsEvent
    data object ImportError : SettingsEvent
    data object LanguageChanged : SettingsEvent
}

/**
 * Beállítások ViewModel – nyelv, téma és adat export/import kezelése.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
) : ViewModel() {

    val theme: StateFlow<ThemeMode> = settingsManager.theme
    val language: StateFlow<Language> = settingsManager.language
    val calorieGoal: StateFlow<Int> = settingsManager.calorieGoal
    val appLock: StateFlow<Boolean> = settingsManager.appLock
    val pinSet: StateFlow<Boolean> = settingsManager.pinSet
    val proteinGoal: StateFlow<Int> = settingsManager.proteinGoal
    val carbsGoal: StateFlow<Int> = settingsManager.carbsGoal
    val fatGoal: StateFlow<Int> = settingsManager.fatGoal
    val dynamicColor: StateFlow<Boolean> = settingsManager.dynamicColor

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    init {
        Logger.i(Logger.Tags.VIEWMODEL, "SettingsViewModel létrehozva")
    }

    /** Téma mód módosítása. */
    fun setTheme(mode: ThemeMode) {
        settingsManager.setTheme(mode)
    }

    /** A napi kalóriacél módosítása. */
    fun setCalorieGoal(value: Int) {
        settingsManager.setCalorieGoal(value)
    }

    /** Egyéni makró-célok módosítása grammban (0 = automatikus). */
    fun setMacroGoals(protein: Int, carbs: Int, fat: Int) {
        settingsManager.setMacroGoals(protein, carbs, fat)
    }

    /** A Material You dinamikus színek ki-/bekapcsolása. */
    fun setDynamicColor(enabled: Boolean) {
        settingsManager.setDynamicColor(enabled)
    }

    /** A biometrikus alkalmazászár ki-/bekapcsolása. */
    fun setAppLock(enabled: Boolean) {
        settingsManager.setAppLock(enabled)
    }

    /** Feloldó PIN beállítása vagy módosítása. */
    fun setPin(pin: String) {
        settingsManager.setPin(pin)
    }

    /** A beállított PIN törlése. */
    fun clearPin() {
        settingsManager.clearPin()
    }

    /** Nyelv módosítása – a hívó felület újraindítja az Activity-t. */
    fun setLanguage(language: Language) {
        settingsManager.setLanguage(language)
        viewModelScope.launch { _events.emit(SettingsEvent.LanguageChanged) }
    }

    /** Adatok exportálása a megadott fájl URI-ba. */
    fun export(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = exportDataUseCase()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    } ?: throw IllegalStateException("Nem sikerült megnyitni a kimeneti adatfolyamot")
                }
                Logger.i(Logger.Tags.BACKUP, "Export completed – fájl mentve")
                _events.emit(SettingsEvent.ExportSuccess)
            } catch (e: Exception) {
                Logger.e(Logger.Tags.BACKUP, "Hiba az exportálás során", e)
                _events.emit(SettingsEvent.ExportError)
            }
        }
    }

    /** Adatok importálása a megadott fájl URI-ból. */
    fun import(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    } ?: throw IllegalStateException("Nem sikerült megnyitni a bemeneti adatfolyamot")
                }
                importDataUseCase(json)
                Logger.i(Logger.Tags.BACKUP, "Import completed – fájl beolvasva")
                _events.emit(SettingsEvent.ImportSuccess)
            } catch (e: Exception) {
                Logger.e(Logger.Tags.BACKUP, "Hiba az importálás során", e)
                _events.emit(SettingsEvent.ImportError)
            }
        }
    }
}
