package com.nutriplan.app.data.remote

import com.nutriplan.app.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Egy beolvasott termék tápértékadatai (100 grammra / 100 ml-re vonatkoztatva),
 * ahogy az Open Food Facts adatbázisból érkeznek.
 */
data class ScannedProduct(
    val barcode: String,
    val name: String,
    val caloriesPer100g: Int,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double
)

/**
 * A termék-lekérdezés lehetséges kimenetei.
 */
sealed interface ProductLookupResult {
    data class Success(val product: ScannedProduct) : ProductLookupResult
    /** A vonalkód érvényes, de nincs ilyen termék az adatbázisban. */
    data object NotFound : ProductLookupResult
    /** Hálózati vagy feldolgozási hiba (nincs internet, időtúllépés stb.). */
    data object NetworkError : ProductLookupResult
}

/**
 * Az Open Food Facts nyilvános API egyszerű kliense.
 *
 * A vonalkód alapján lekérdezi a termék nevét és 100 g-ra vetített tápértékét.
 * Szándékosan minimális: a beépített [HttpURLConnection]-t használja, így nincs
 * szükség külön hálózati könyvtárra.
 */
@Singleton
class OpenFoodFactsDataSource @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /** A megadott vonalkódhoz tartozó termék lekérdezése. */
    suspend fun lookup(barcode: String): ProductLookupResult = withContext(Dispatchers.IO) {
        val url = URL(
            "https://world.openfoodfacts.org/api/v2/product/$barcode.json" +
                "?fields=code,product_name,product_name_hu,generic_name,brands,nutriments"
        )
        var connection: HttpURLConnection? = null
        try {
            Logger.d(Logger.Tags.NUTRITION, "Open Food Facts lekérdezés: $barcode")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                // Az Open Food Facts kéri az egyedi User-Agent megadását.
                setRequestProperty("User-Agent", "NutriPlan/1.0 (Android; nutriplan app)")
            }

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                Logger.w(Logger.Tags.NUTRITION, "Open Food Facts HTTP hiba: $code")
                return@withContext if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                    ProductLookupResult.NotFound
                } else {
                    ProductLookupResult.NetworkError
                }
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val response = json.decodeFromString<OffResponse>(body)

            if (response.status != 1 || response.product == null) {
                Logger.i(Logger.Tags.NUTRITION, "Termék nem található: $barcode")
                return@withContext ProductLookupResult.NotFound
            }

            val product = response.product
            val name = listOfNotNull(
                product.productName,
                product.productNameHu,
                product.genericName,
                product.brands
            ).firstOrNull { it.isNotBlank() }?.trim().orEmpty()

            val n = product.nutriments
            ProductLookupResult.Success(
                ScannedProduct(
                    barcode = barcode,
                    name = name.ifBlank { "" },
                    caloriesPer100g = (n?.energyKcal ?: 0.0).roundToInt(),
                    proteinPer100g = n?.proteins ?: 0.0,
                    carbsPer100g = n?.carbohydrates ?: 0.0,
                    fatPer100g = n?.fat ?: 0.0
                )
            )
        } catch (e: Exception) {
            Logger.w(Logger.Tags.NUTRITION, "Open Food Facts hiba: ${e.message}")
            ProductLookupResult.NetworkError
        } finally {
            connection?.disconnect()
        }
    }
}

// --- Az Open Food Facts API válaszának (részleges) szerializációs modellje ---

@Serializable
private data class OffResponse(
    val status: Int = 0,
    val product: OffProduct? = null
)

@Serializable
private data class OffProduct(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_name_hu") val productNameHu: String? = null,
    @SerialName("generic_name") val genericName: String? = null,
    val brands: String? = null,
    val nutriments: OffNutriments? = null
)

@Serializable
private data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal: Double? = null,
    @SerialName("proteins_100g") val proteins: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates: Double? = null,
    @SerialName("fat_100g") val fat: Double? = null
)
