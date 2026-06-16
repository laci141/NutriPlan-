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
