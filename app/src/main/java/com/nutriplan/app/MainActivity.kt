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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

            val pinSet by settingsManager.pinSet.collectAsState()

            NutriPlanTheme(themeMode = themeMode) {
                if (appLock && !unlocked.value) {
                    LockScreen(
                        pinEnabled = pinSet,
                        biometricAvailable = canAuthenticateBiometric(),
                        onSubmitPin = { pin ->
                            val ok = settingsManager.verifyPin(pin)
                            if (ok) unlocked.value = true
                            ok
                        },
                        onUseBiometric = { authenticate() }
                    )
                    LaunchedEffect(pinSet) {
                        when {
                            // PIN beállítva: a felhasználó kézzel ír be (nincs automatikus biometria)
                            pinSet -> Unit
                            // Nincs PIN, de van biometria: automatikus kérés
                            canAuthenticateBiometric() -> authenticate()
                            // Sem PIN, sem biometria: ne zárjuk ki a felhasználót
                            else -> unlocked.value = true
                        }
                    }
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

    /** Igaz, ha az eszközön elérhető (beállított) biometrikus azonosítás. */
    private fun canAuthenticateBiometric(): Boolean =
        BiometricManager.from(this).canAuthenticate(Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS

    /** Biometrikus azonosítás indítása. */
    private fun authenticate() {
        val authenticators = Authenticators.BIOMETRIC_WEAK
        if (!canAuthenticateBiometric()) {
            // Nincs beállított biometria – PIN nélkül ne zárjuk ki a felhasználót
            Logger.w(Logger.Tags.SETTINGS, "Nincs elérhető biometrikus azonosítás")
            if (!settingsManager.pinSet.value) unlocked.value = true
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

/**
 * Zárképernyő. Ha [pinEnabled], PIN-kódot kér (ez mindig működik, biometriától
 * függetlenül); emellett – ha elérhető – felajánlja a biometrikus feloldást is.
 * PIN nélkül a régi viselkedés marad: egy „Feloldás" gomb biometriához.
 */
@Composable
private fun LockScreen(
    pinEnabled: Boolean,
    biometricAvailable: Boolean,
    onSubmitPin: (String) -> Boolean,
    onUseBiometric: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

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

            if (pinEnabled) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { new -> pin = new.filter { it.isDigit() }.take(8); error = false },
                    label = { Text(stringResource(R.string.enter_pin)) },
                    singleLine = true,
                    isError = error,
                    supportingText = if (error) {
                        { Text(stringResource(R.string.wrong_pin)) }
                    } else null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (!onSubmitPin(pin)) { error = true; pin = "" }
                    }),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = { if (!onSubmitPin(pin)) { error = true; pin = "" } },
                    enabled = pin.length >= 4,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(stringResource(R.string.unlock))
                }
                if (biometricAvailable) {
                    TextButton(onClick = onUseBiometric, modifier = Modifier.padding(top = 4.dp)) {
                        Text(stringResource(R.string.use_biometric))
                    }
                }
            } else {
                Button(onClick = onUseBiometric, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.unlock))
                }
            }
        }
    }
}
