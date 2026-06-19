package com.nutriplan.app.data.remote

import com.nutriplan.app.data.preferences.SecureKeyStore
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A saját Cloudflare Worker AI-proxy kliense.
 *
 * Az appból CSAK a proxy URL-jét és a (titkosított) app-tokent használjuk; a valódi
 * szolgáltató-kulcsok a Workerben élnek. Híváskor megadjuk a kiválasztott providert
 * ("openai" / "gemini" / "anthropic"), a Worker oda továbbít.
 */
@Singleton
class AiProxyClient @Inject constructor(
    private val secureKeyStore: SecureKeyStore
) {
    /** Üzenetküldés a proxynak. Visszaadja az AI válaszszövegét. */
    suspend fun chat(
        systemPrompt: String,
        userPrompt: String,
        provider: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val base = secureKeyStore.proxyUrl.value
        val token = secureKeyStore.getToken()
        if (base.isBlank() || token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("not_configured"))
        }
        val prov = provider ?: secureKeyStore.defaultProvider.value

        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", systemPrompt))
            .put(JSONObject().put("role", "user").put("content", userPrompt))
        val payload = JSONObject()
            .put("provider", prov)
            .put("max_tokens", 800)
            .put("messages", messages)

        runCatching {
            val text = post(base, token, payload).optString("text", "")
            if (text.isBlank()) throw IllegalStateException("empty_response")
            text
        }.onFailure { Logger.w(Logger.Tags.NUTRITION, "AI proxy hiba: ${it.message}") }
    }

    /** Kapcsolat-teszt: a Worker visszaadja az elérhető szolgáltatókat. */
    suspend fun ping(): Result<List<String>> = withContext(Dispatchers.IO) {
        val base = secureKeyStore.proxyUrl.value
        val token = secureKeyStore.getToken()
        if (base.isBlank() || token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("not_configured"))
        }
        runCatching {
            val obj = post(base, token, JSONObject().put("ping", true))
            val arr = obj.optJSONArray("providers") ?: JSONArray()
            (0 until arr.length()).map { arr.getString(it) }
        }
    }

    private fun post(base: String, token: String, payload: JSONObject): JSONObject {
        val connection = (URL(base).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }
        try {
            connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() } ?: "{}"
            if (code !in 200..299) throw IllegalStateException("http_$code")
            return JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }
}
