package com.nutriplan.app.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.data.preferences.SecureKeyStore
import com.nutriplan.app.data.preferences.SettingsManager
import com.nutriplan.app.data.remote.AiProxyClient
import com.nutriplan.app.domain.model.Language
import com.nutriplan.app.domain.model.ThemeMode
import com.nutriplan.app.domain.usecase.ExportDataUseCase
import com.nutriplan.app.domain.usecase.ImportDataUseCase
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val secureKeyStore: SecureKeyStore,
    private val aiProxyClient: AiProxyClient,
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

    // --- AI proxy beállítások ---
    val aiEnabled: StateFlow<Boolean> = secureKeyStore.aiEnabled
    val proxyUrl: StateFlow<String> = secureKeyStore.proxyUrl
    val hasToken: StateFlow<Boolean> = secureKeyStore.hasToken
    val enabledProviders: StateFlow<Set<String>> = secureKeyStore.enabledProviders
    val defaultProvider: StateFlow<String> = secureKeyStore.defaultProvider

    private val _aiBusy = MutableStateFlow(false)
    val aiBusy: StateFlow<Boolean> = _aiBusy.asStateFlow()
    private val _aiResult = MutableStateFlow<String?>(null)
    /** Az utolsó AI-művelet (teszt/javaslat) eredménye dialógushoz; null = nincs. */
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

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

    // --- AI proxy (a kulcsok a Workerben; itt csak URL + token titkosítva) ---
    fun setAiEnabled(enabled: Boolean) = secureKeyStore.setAiEnabled(enabled)
    fun setProxyUrl(url: String) = secureKeyStore.setProxyUrl(url)
    fun setToken(token: String) = secureKeyStore.setToken(token)
    fun clearToken() = secureKeyStore.clearToken()
    fun setProviderEnabled(key: String, enabled: Boolean) = secureKeyStore.setProviderEnabled(key, enabled)
    fun setDefaultProvider(key: String) = secureKeyStore.setDefaultProvider(key)
    fun dismissAiResult() { _aiResult.value = null }

    /** Kapcsolat-teszt a proxyhoz (ping). */
    fun testConnection() {
        if (_aiBusy.value) return
        _aiBusy.value = true
        viewModelScope.launch {
            val result = aiProxyClient.ping()
            _aiResult.value = result.fold(
                onSuccess = { "OK · ${it.joinToString(", ").ifBlank { "—" }}" },
                onFailure = { "Hiba: ${it.message}" }
            )
            _aiBusy.value = false
        }
    }

    /** AI étrend-javaslat a célok alapján (a kiválasztott alapértelmezett providerrel). */
    fun suggestDiet() {
        if (_aiBusy.value) return
        _aiBusy.value = true
        viewModelScope.launch {
            val goal = settingsManager.calorieGoal.value.let { if (it > 0) it else 2000 }
            val p = settingsManager.proteinGoal.value
            val c = settingsManager.carbsGoal.value
            val f = settingsManager.fatGoal.value
            val macros = if (p > 0 || c > 0 || f > 0) " Makró-célok: fehérje ${p} g, szénhidrát ${c} g, zsír ${f} g." else ""
            val result = aiProxyClient.chat(
                systemPrompt = "Tapasztalt táplálkozási tanácsadó vagy. Tömören, magyarul válaszolj.",
                userPrompt = "Adj egy napi étrend-javaslatot kb. $goal kcal-ra.$macros " +
                    "Reggeli/ebéd/vacsora/snack bontásban, soronként becsült kcal-lal."
            )
            _aiResult.value = result.fold(
                onSuccess = { it },
                onFailure = { "Hiba: ${it.message}" }
            )
            _aiBusy.value = false
        }
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
