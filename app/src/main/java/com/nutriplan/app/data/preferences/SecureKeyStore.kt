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
 * Az AI-proxy (Cloudflare Worker) eléréséhez szükséges, érzékeny beállítások
 * biztonságos tárolása.
 *
 * Architektúra: Telefon → saját Cloudflare Worker → OpenAI/Gemini/Anthropic.
 * Az appban CSAK a proxy URL-je és egy app-token van; a valódi szolgáltató-kulcsok
 * a Workerben élnek. A tokent az Android Keystore-ban generált, eszközhöz kötött,
 * nem exportálható AES kulccsal (AES/GCM) titkosítjuk – nyílt szövegben sehol.
 */
@Singleton
class SecureKeyStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _aiEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    val aiEnabled: StateFlow<Boolean> = _aiEnabled.asStateFlow()

    private val _proxyUrl = MutableStateFlow(prefs.getString(KEY_PROXY_URL, "") ?: "")
    val proxyUrl: StateFlow<String> = _proxyUrl.asStateFlow()

    private val _hasToken = MutableStateFlow(prefs.contains(KEY_TOKEN))
    val hasToken: StateFlow<Boolean> = _hasToken.asStateFlow()

    private val _enabledProviders =
        MutableStateFlow(prefs.getStringSet(KEY_PROVIDERS, emptySet())?.toSet() ?: emptySet())
    /** Az engedélyezett szolgáltatók kulcsai (pl. "openai", "gemini", "anthropic"). */
    val enabledProviders: StateFlow<Set<String>> = _enabledProviders.asStateFlow()

    private val _defaultProvider = MutableStateFlow(prefs.getString(KEY_DEFAULT, "openai") ?: "openai")
    val defaultProvider: StateFlow<String> = _defaultProvider.asStateFlow()

    fun setAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _aiEnabled.value = enabled
    }

    fun setProxyUrl(url: String) {
        prefs.edit().putString(KEY_PROXY_URL, url.trim()).apply()
        _proxyUrl.value = url.trim()
    }

    fun setProviderEnabled(key: String, enabled: Boolean) {
        val set = _enabledProviders.value.toMutableSet()
        if (enabled) set.add(key) else set.remove(key)
        prefs.edit().putStringSet(KEY_PROVIDERS, set).apply()
        _enabledProviders.value = set
        // Ha a kikapcsolt volt az alapértelmezett, váltsunk egy elérhetőre
        if (!enabled && _defaultProvider.value == key) {
            setDefaultProvider(set.firstOrNull() ?: "openai")
        }
    }

    fun setDefaultProvider(key: String) {
        prefs.edit().putString(KEY_DEFAULT, key).apply()
        _defaultProvider.value = key
    }

    /** App-token titkosított mentése. Üres érték törli. */
    fun setToken(plain: String) {
        if (plain.isBlank()) {
            clearToken()
            return
        }
        runCatching {
            prefs.edit().putString(KEY_TOKEN, encrypt(plain)).apply()
            _hasToken.value = true
            Logger.i(Logger.Tags.SETTINGS, "AI proxy token titkosítva eltárolva")
        }.onFailure { Logger.w(Logger.Tags.SETTINGS, "Token mentési hiba: ${it.message}") }
    }

    /** A tárolt app-token visszafejtése a proxy-híváshoz. */
    fun getToken(): String? {
        val stored = prefs.getString(KEY_TOKEN, null) ?: return null
        return runCatching { decrypt(stored) }.getOrNull()
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
        _hasToken.value = false
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
        val combined = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
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
        private const val KEY_ENABLED = "ai_enabled"
        private const val KEY_PROXY_URL = "proxy_url"
        private const val KEY_TOKEN = "proxy_token_enc"
        private const val KEY_PROVIDERS = "ai_providers"
        private const val KEY_DEFAULT = "ai_default_provider"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val MASTER_ALIAS = "nutriplan_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_BITS = 128
    }
}
