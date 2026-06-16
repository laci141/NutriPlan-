package com.nutriplan.app.presentation.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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
