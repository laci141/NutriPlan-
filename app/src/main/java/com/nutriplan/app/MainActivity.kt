package com.nutriplan.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nutriplan.app.data.preferences.SettingsManager
import com.nutriplan.app.presentation.NutriPlanApp
import com.nutriplan.app.presentation.theme.NutriPlanTheme
import com.nutriplan.app.util.LocaleHelper
import com.nutriplan.app.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Az alkalmazás fő (és egyetlen) Activity-je.
 * A Jetpack Compose felületet hosztolja, és kezeli a nyelvi/téma beállításokat.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    // A kiválasztott nyelv alkalmazása a Context becsomagolásával
    override fun attachBaseContext(newBase: Context) {
        val languageCode = SettingsManager.readLanguageCode(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Logger.i(Logger.Tags.APP, "MainActivity létrehozva, felület megjelenítése")

        setContent {
            // A téma mód figyelése – azonnali váltáshoz
            val themeMode by settingsManager.theme.collectAsState()
            NutriPlanTheme(themeMode = themeMode) {
                NutriPlanApp()
            }
        }
    }
}
