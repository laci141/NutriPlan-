package com.nutriplan.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.nutriplan.app.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Érzékeny adatok (pl. szinkron-/AI-szolgáltató API-kulcs) biztonságos tárolása.
 *
 * A kulcsot az Android Keystore-ban generált, eszközhöz kötött, nem exportálható
 * AES kulccsal titkosítjuk (AES/GCM), és csak a titkosított szöveget mentjük el.
 * Így a kulcs nyílt szövegként sehol nem szerepel, és a (Keystore-beli) mesterkulcs
 * az eszközről nem menthető ki, így a biztonsági mentésbe sem kerül használható alak.
 */
@Singleton
class SecureKeyStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _syncEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    val syncEnabled: StateFlow<Boolean> = _syncEnabled.asStateFlow()

    private val _hasApiKey = MutableStateFlow(prefs.contains(KEY_API))
    /** Igaz, ha van eltárolt (titkosított) API-kulcs. */
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    private val _provider = MutableStateFlow(prefs.getString(KEY_PROVIDER, "") ?: "")
    /** A kiválasztott szolgáltató megnevezése (szabad szöveg). */
    val provider: StateFlow<String> = _provider.asStateFlow()

    fun setSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _syncEnabled.value = enabled
    }

    fun setProvider(name: String) {
        prefs.edit().putString(KEY_PROVIDER, name).apply()
        _provider.value = name
    }

    /** API-kulcs titkosított mentése. Üres érték törli a tárolt kulcsot. */
    fun setApiKey(plain: String) {
        if (plain.isBlank()) {
            clearApiKey()
            return
        }
        runCatching {
            val encrypted = encrypt(plain)
            prefs.edit().putString(KEY_API, encrypted).apply()
            _hasApiKey.value = true
            Logger.i(Logger.Tags.SETTINGS, "API-kulcs titkosítva eltárolva")
        }.onFailure { Logger.w(Logger.Tags.SETTINGS, "API-kulcs mentési hiba: ${it.message}") }
    }

    /** A tárolt API-kulcs visszafejtése (a tényleges szinkron/AI hívásokhoz). */
    fun getApiKey(): String? {
        val stored = prefs.getString(KEY_API, null) ?: return null
        return runCatching { decrypt(stored) }.getOrNull()
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_API).apply()
        _hasApiKey.value = false
    }

    // --- AES/GCM az Android Keystore mesterkulccsal ---

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(MASTER_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                MASTER_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val combined = iv + ciphertext
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(stored: String): String {
        val combined = Base64.decode(stored, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    companion object {
        private const val PREFS_NAME = "nutriplan_secure"
        private const val KEY_ENABLED = "sync_enabled"
        private const val KEY_API = "api_key_enc"
        private const val KEY_PROVIDER = "provider"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_ALIAS = "nutriplan_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_BITS = 128
    }
}
