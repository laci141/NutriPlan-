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
 * Egy fotóról felismert étel becsült tápértéke (100 grammra vonatkoztatva),
 * a [estimatedGrams] az AI által becsült adagméret.
 */
data class AiFoodEstimate(
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val estimatedGrams: Double
)

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

    /**
     * Étel felismerése fotóról (vision). A képet base64 data-URI-ként küldjük a
     * proxynak; a kép és a prompt sosem kerül naplózásra. A modelltől szigorúan
     * JSON-választ kérünk: étel neve + 100 g-ra vonatkozó tápérték + becsült adag.
     *
     * Megjegyzés: a Workernek támogatnia kell a multimodális (vision) továbbítást a
     * választott szolgáltató felé (pl. OpenAI gpt-4o, Anthropic, Gemini).
     */
    suspend fun recognizeFood(
        imageBase64: String,
        mimeType: String = "image/jpeg",
        provider: String? = null
    ): Result<AiFoodEstimate> = withContext(Dispatchers.IO) {
        val base = secureKeyStore.proxyUrl.value
        val token = secureKeyStore.getToken()
        if (base.isBlank() || token.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("not_configured"))
        }
        val prov = provider ?: secureKeyStore.defaultProvider.value

        val system = "You are a nutrition assistant. Identify the food in the photo and " +
            "estimate its nutrition. Reply with ONLY a compact JSON object, no prose, no code fences. " +
            "Schema: {\"name\":string, \"calories_per_100g\":number, \"protein_per_100g\":number, " +
            "\"carbs_per_100g\":number, \"fat_per_100g\":number, \"estimated_grams\":number}. " +
            "If no food is visible, set name to \"\"."
        val userContent = JSONArray()
            .put(JSONObject().put("type", "text").put("text", "Identify this food and estimate nutrition."))
            .put(
                JSONObject().put("type", "image_url").put(
                    "image_url",
                    JSONObject().put("url", "data:$mimeType;base64,$imageBase64")
                )
            )
        val messages = JSONArray()
            .put(JSONObject().put("role", "system").put("content", system))
            .put(JSONObject().put("role", "user").put("content", userContent))
        val payload = JSONObject()
            .put("provider", prov)
            .put("max_tokens", 400)
            .put("messages", messages)

        runCatching {
            val text = post(base, token, payload).optString("text", "")
            parseEstimate(text) ?: throw IllegalStateException("parse_failed")
        }.onFailure { Logger.w(Logger.Tags.NUTRITION, "AI fotó-felismerés hiba: ${it.message}") }
    }

    /** A modell JSON-válaszának megengedő feldolgozása (kódkeret-jelölők leszedésével). */
    private fun parseEstimate(raw: String): AiFoodEstimate? {
        if (raw.isBlank()) return null
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        val json = runCatching { JSONObject(raw.substring(start, end + 1)) }.getOrNull() ?: return null
        val name = json.optString("name", "").trim()
        if (name.isBlank()) return null
        return AiFoodEstimate(
            name = name,
            caloriesPer100g = json.optDouble("calories_per_100g", 0.0),
            proteinPer100g = json.optDouble("protein_per_100g", 0.0),
            carbsPer100g = json.optDouble("carbs_per_100g", 0.0),
            fatPer100g = json.optDouble("fat_per_100g", 0.0),
            estimatedGrams = json.optDouble("estimated_grams", 100.0)
        )
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
