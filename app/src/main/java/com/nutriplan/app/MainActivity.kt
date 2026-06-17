package com.nutriplan.app

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.nutriplan.app.data.preferences.SettingsManager
import com.nutriplan.app.presentation.NutriPlanApp
import com.nutriplan.app.presentation.theme.NutriPlanTheme
import com.nutriplan.app.util.LocaleHelper
import com.nutriplan.app.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Az alkalmazás fő (és egyetlen) Activity-je.
 * A Jetpack Compose felületet hosztolja, kezeli a nyelvi/téma beállításokat,
 * valamint az opcionális biometrikus alkalmazászárat.
 *
 * FragmentActivity-ből származik, mert a BiometricPrompt ezt igényli.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    // Feloldott állapot – a zár mögött lévő tartalom csak ekkor látszik
    private val unlocked = mutableStateOf(false)

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
            val themeMode by settingsManager.theme.collectAsState()
            val appLock by settingsManager.appLock.collectAsState()

            NutriPlanTheme(themeMode = themeMode) {
                if (appLock && !unlocked.value) {
                    // Zárolt állapot: zárképernyő + automatikus biometrikus kérés
                    LockScreen(onUnlock = { authenticate() })
                    LaunchedEffect(Unit) { authenticate() }
                } else {
                    NutriPlanApp()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Háttérbe kerüléskor újra zárolunk, ha a zár be van kapcsolva
        if (settingsManager.appLock.value) {
            unlocked.value = false
        }
    }

    /** Biometrikus (vagy eszközhitelesítési) azonosítás indítása. */
    private fun authenticate() {
        val authenticators = Authenticators.BIOMETRIC_WEAK
        val manager = BiometricManager.from(this)
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            // Nincs beállított biometria – ne zárjuk ki a felhasználót
            Logger.w(Logger.Tags.SETTINGS, "Nincs elérhető biometrikus azonosítás, feloldás")
            unlocked.value = true
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Logger.i(Logger.Tags.SETTINGS, "Biometrikus azonosítás sikeres")
                unlocked.value = true
            }
        })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_lock))
            .setSubtitle(getString(R.string.app_lock_prompt))
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(info)
    }
}

/** Egyszerű zárképernyő lakat ikonnal és feloldó gombbal. */
@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.app_locked),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(onClick = onUnlock, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.unlock))
            }
        }
    }
}
