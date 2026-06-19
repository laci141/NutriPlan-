package com.nutriplan.app.presentation.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.Language
import com.nutriplan.app.domain.model.ThemeMode
import com.nutriplan.app.presentation.util.label

/**
 * Beállítások képernyő – nyelv, téma és adatkezelés (export/import).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val calorieGoal by viewModel.calorieGoal.collectAsStateWithLifecycle()
    val appLock by viewModel.appLock.collectAsStateWithLifecycle()
    val pinSet by viewModel.pinSet.collectAsStateWithLifecycle()
    val proteinGoal by viewModel.proteinGoal.collectAsStateWithLifecycle()
    val carbsGoal by viewModel.carbsGoal.collectAsStateWithLifecycle()
    val fatGoal by viewModel.fatGoal.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
    val aiEnabled by viewModel.aiEnabled.collectAsStateWithLifecycle()
    val proxyUrl by viewModel.proxyUrl.collectAsStateWithLifecycle()
    val hasToken by viewModel.hasToken.collectAsStateWithLifecycle()
    val enabledProviders by viewModel.enabledProviders.collectAsStateWithLifecycle()
    val defaultProvider by viewModel.defaultProvider.collectAsStateWithLifecycle()
    val aiBusy by viewModel.aiBusy.collectAsStateWithLifecycle()
    val aiResult by viewModel.aiResult.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showPinDialog by remember { mutableStateOf(false) }

    val exportSuccessMsg = stringResource(R.string.export_success)
    val exportErrorMsg = stringResource(R.string.export_error)
    val importSuccessMsg = stringResource(R.string.import_success)
    val importErrorMsg = stringResource(R.string.import_error)

    // Fájl létrehozó és megnyitó indítók az export/importhoz
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.export(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.import(it) } }

    // Események kezelése (visszajelzések, nyelvváltáskor újraindítás)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.ExportSuccess -> snackbarHostState.showSnackbar(exportSuccessMsg)
                SettingsEvent.ExportError -> snackbarHostState.showSnackbar(exportErrorMsg)
                SettingsEvent.ImportSuccess -> snackbarHostState.showSnackbar(importSuccessMsg)
                SettingsEvent.ImportError -> snackbarHostState.showSnackbar(importErrorMsg)
                SettingsEvent.LanguageChanged -> (context as? Activity)?.recreate()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nyelv szekció
            SettingsSection(title = stringResource(R.string.language)) {
                Language.entries.forEach { lang ->
                    OptionRow(
                        text = lang.label(),
                        selected = lang == language,
                        onClick = { if (lang != language) viewModel.setLanguage(lang) }
                    )
                }
            }

            // Téma szekció
            SettingsSection(title = stringResource(R.string.theme)) {
                ThemeMode.entries.forEach { mode ->
                    OptionRow(
                        text = mode.label(),
                        selected = mode == theme,
                        onClick = { viewModel.setTheme(mode) }
                    )
                }
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.dynamic_color),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.dynamic_color_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = dynamicColor, onCheckedChange = viewModel::setDynamicColor)
                }
            }

            // Napi kalóriacél szekció
            SettingsSection(title = stringResource(R.string.daily_calorie_goal)) {
                CalorieGoalStepper(
                    value = calorieGoal,
                    onValueChange = viewModel::setCalorieGoal
                )
                Text(
                    text = stringResource(R.string.calorie_goal_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Makró-célok szekció (0 = automatikus a kalóriacélból)
            SettingsSection(title = stringResource(R.string.macro_goals)) {
                MacroGoalField(
                    label = stringResource(R.string.protein),
                    value = proteinGoal,
                    onValueChange = { viewModel.setMacroGoals(it, carbsGoal, fatGoal) }
                )
                MacroGoalField(
                    label = stringResource(R.string.carbs),
                    value = carbsGoal,
                    onValueChange = { viewModel.setMacroGoals(proteinGoal, it, fatGoal) }
                )
                MacroGoalField(
                    label = stringResource(R.string.fat),
                    value = fatGoal,
                    onValueChange = { viewModel.setMacroGoals(proteinGoal, carbsGoal, it) }
                )
                Text(
                    text = stringResource(R.string.macro_goals_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // AI szolgáltatók (proxy a Cloudflare Workeren át; kulcsok a szerveren)
            SettingsSection(title = stringResource(R.string.ai_providers)) {
                AiProvidersContent(
                    enabled = aiEnabled,
                    proxyUrl = proxyUrl,
                    hasToken = hasToken,
                    enabledProviders = enabledProviders,
                    defaultProvider = defaultProvider,
                    busy = aiBusy,
                    onEnabledChange = viewModel::setAiEnabled,
                    onProxyUrlChange = viewModel::setProxyUrl,
                    onSaveToken = viewModel::setToken,
                    onClearToken = viewModel::clearToken,
                    onProviderToggle = viewModel::setProviderEnabled,
                    onDefaultChange = viewModel::setDefaultProvider,
                    onTest = viewModel::testConnection,
                    onSuggest = viewModel::suggestDiet
                )
            }

            // Biztonság szekció – biometrikus zár
            SettingsSection(title = stringResource(R.string.security)) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_lock),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.app_lock_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = appLock, onCheckedChange = viewModel::setAppLock)
                }

                // PIN-kód kezelése – csak bekapcsolt zár mellett
                if (appLock) {
                    Text(
                        text = stringResource(
                            if (pinSet) R.string.pin_set_summary else R.string.pin_not_set_summary
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPinDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                stringResource(
                                    if (pinSet) R.string.change_pin else R.string.set_pin
                                )
                            )
                        }
                        if (pinSet) {
                            OutlinedButton(
                                onClick = { viewModel.clearPin() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.remove_pin))
                            }
                        }
                    }
                }
            }

            if (showPinDialog) {
                PinSetupDialog(
                    onDismiss = { showPinDialog = false },
                    onConfirm = { pin ->
                        viewModel.setPin(pin)
                        showPinDialog = false
                    }
                )
            }

            // AI művelet eredménye (teszt vagy javaslat)
            aiResult?.let { result ->
                AlertDialog(
                    onDismissRequest = viewModel::dismissAiResult,
                    confirmButton = {
                        TextButton(onClick = viewModel::dismissAiResult) { Text(stringResource(R.string.ok)) }
                    },
                    title = { Text(stringResource(R.string.ai_providers)) },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(result, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                )
            }

            // Adatkezelés szekció
            SettingsSection(title = stringResource(R.string.data_management)) {
                OutlinedButton(
                    onClick = { exportLauncher.launch("nutriplan_backup.json") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null)
                    Text(text = "  ${stringResource(R.string.export_data)}")
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.FileUpload, contentDescription = null)
                    Text(text = "  ${stringResource(R.string.import_data)}")
                }
            }

            // Névjegy szekció
            SettingsSection(title = stringResource(R.string.about)) {
                Text(
                    text = stringResource(R.string.about_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Egy beállítási szekció kártyában, címmel. */
@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) { content() }
        }
    }
}

/** Napi kalóriacél beállító léptető (− érték +), 50 kcal-os lépésekkel. */
@Composable
private fun CalorieGoalStepper(value: Int, onValueChange: (Int) -> Unit) {
    val step = 50
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilledTonalIconButton(onClick = { onValueChange(value - step) }) {
            Icon(Icons.Filled.Remove, contentDescription = "-$step")
        }
        Text(
            text = "$value ${stringResource(R.string.kcal_unit)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        FilledTonalIconButton(onClick = { onValueChange(value + step) }) {
            Icon(Icons.Filled.Add, contentDescription = "+$step")
        }
    }
}

/** Egy választható sor rádiógombbal. */
@Composable
private fun OptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/** Egy makró-cél beviteli mező grammban (üres = automatikus). */
@Composable
private fun MacroGoalField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { input ->
            onValueChange(input.filter { it.isDigit() }.take(4).toIntOrNull() ?: 0)
        },
        label = { Text(label) },
        placeholder = { Text(stringResource(R.string.macro_auto)) },
        suffix = { Text("g") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

// Az elérhető AI-szolgáltatók (kulcs → megjelenített név). Később bővíthető.
private val AI_PROVIDERS = listOf(
    "openai" to "ChatGPT (OpenAI)",
    "gemini" to "Gemini (Google)",
    "anthropic" to "Claude (Anthropic)"
)

/** AI szolgáltatók szekció: proxy URL + token (titkosítva), provider-kapcsolók, teszt. */
@Composable
private fun AiProvidersContent(
    enabled: Boolean,
    proxyUrl: String,
    hasToken: Boolean,
    enabledProviders: Set<String>,
    defaultProvider: String,
    busy: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onProxyUrlChange: (String) -> Unit,
    onSaveToken: (String) -> Unit,
    onClearToken: () -> Unit,
    onProviderToggle: (String, Boolean) -> Unit,
    onDefaultChange: (String) -> Unit,
    onTest: () -> Unit,
    onSuggest: () -> Unit
) {
    var tokenInput by remember { mutableStateOf("") }
    val configured = proxyUrl.isNotBlank() && hasToken
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ai_enable), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = stringResource(R.string.ai_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        if (enabled) {
            OutlinedTextField(
                value = proxyUrl,
                onValueChange = onProxyUrlChange,
                label = { Text(stringResource(R.string.proxy_url)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tokenInput,
                onValueChange = { tokenInput = it },
                label = { Text(stringResource(R.string.proxy_token)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onSaveToken(tokenInput); tokenInput = "" },
                    enabled = tokenInput.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.save)) }
                if (hasToken) {
                    OutlinedButton(onClick = onClearToken, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.clear_token))
                    }
                }
            }
            Text(
                text = stringResource(if (hasToken) R.string.token_set else R.string.token_none),
                style = MaterialTheme.typography.bodySmall,
                color = if (hasToken) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Szolgáltatók kapcsolói + alapértelmezett választás
            Text(
                text = stringResource(R.string.providers),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
            AI_PROVIDERS.forEach { (key, label) ->
                val on = key in enabledProviders
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = defaultProvider == key,
                        onClick = { onDefaultChange(key) },
                        enabled = on
                    )
                    Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = on, onCheckedChange = { onProviderToggle(key, it) })
                }
            }

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTest,
                    enabled = configured && !busy,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.test_connection)) }
                OutlinedButton(
                    onClick = onSuggest,
                    enabled = configured && !busy && enabledProviders.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.ai_suggest)) }
            }
            if (busy) {
                Text(
                    text = stringResource(R.string.ai_working),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = stringResource(R.string.ai_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** PIN beállító párbeszéd – egyedi numerikus billentyűzet, két lépéses megerősítés. */
@Composable
private fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<Int?>(null) }

    val activeValue = if (isConfirmStep) confirmPin else pin
    val dots = "●".repeat(activeValue.length) + "○".repeat((6 - activeValue.length).coerceAtLeast(0))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(if (isConfirmStep) R.string.pin_confirm_label else R.string.set_pin),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Pontok kijelző
                Text(
                    text = dots,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                // Hiba
                error?.let {
                    Text(
                        text = stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Egyedi billentyűzet
                com.nutriplan.app.presentation.components.NumericKeypad(
                    onDigit = { d ->
                        error = null
                        if (isConfirmStep) { if (confirmPin.length < 8) confirmPin += d }
                        else { if (pin.length < 8) pin += d }
                    },
                    onBackspace = {
                        error = null
                        if (isConfirmStep) confirmPin = confirmPin.dropLast(1)
                        else pin = pin.dropLast(1)
                    },
                    onConfirm = {
                        if (!isConfirmStep) {
                            if (pin.length < 4) { error = R.string.pin_too_short; return@NumericKeypad }
                            isConfirmStep = true
                        } else {
                            if (confirmPin != pin) {
                                error = R.string.pin_mismatch
                                confirmPin = ""
                                pin = ""
                                isConfirmStep = false
                            } else {
                                onConfirm(pin)
                            }
                        }
                    },
                    allowDecimal = false,
                    confirmEnabled = activeValue.length >= 4,
                    accentColor = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
