package com.nutriplan.app.data.local

import android.content.Context
import com.nutriplan.app.data.remote.ScannedProduct
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Egy helyi (JSON-fájlból olvasott) étel 100 g-ra vonatkozó tápértékadatai.
 */
@Serializable
data class LocalFood(
    val name: String,
    val nameKey: String,
    val kcal: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val vitC: Double = 0.0,
    val iron: Double = 0.0,
    val calcium: Double = 0.0,
    val vitD: Double = 0.0,
    val b12: Double = 0.0,
    val magnesium: Double = 0.0
) {
    /** Átalakítja ScannedProduct-tá, hogy az applyGrams() logika újrafelhasználható legyen. */
    fun toScannedProduct() = ScannedProduct(
        barcode = "",
        name = name,
        caloriesPer100g = kcal,
        proteinPer100g = protein,
        carbsPer100g = carbs,
        fatPer100g = fat,
        fiberPer100g = fiber,
        vitaminCPer100gMg = vitC,
        ironPer100gMg = iron,
        calciumPer100gMg = calcium,
        vitaminDPer100gUg = vitD,
        b12Per100gUg = b12,
        magnesiumPer100gMg = magnesium
    )
}

/**
 * Magyar és román helyi ételek adatbázisa — az assets/local_foods.json fájlból olvas.
 * A lista egyszer töltődik be (lazy), azután memóriában marad.
 */
@Singleton
class LocalFoodDatabase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    val all: List<LocalFood> by lazy {
        runCatching {
            context.assets.open("local_foods.json").bufferedReader().readText()
                .let { json.decodeFromString<List<LocalFood>>(it) }
        }.getOrElse { emptyList() }
    }

    /** Kereső: ha a query üres, az első 15 ételt adja vissza; különben névtartalom szerint szűr. */
    fun search(query: String): List<LocalFood> {
        if (query.isBlank()) return all.take(15)
        val q = query.trim().lowercase()
        return all.filter { it.name.lowercase().contains(q) }.take(15)
    }

    /**
     * Hasonló tápértékű ételek keresése (fehérje/kalória arány alapján).
     * A naplózott étel alternatíváinak megjelenítéséhez.
     */
    fun findSimilar(calories: Int, protein: Double): List<LocalFood> {
        val targetRatio = if (calories > 0) protein / calories.toDouble() else 0.0
        return all
            .sortedBy { food ->
                val r = if (food.kcal > 0) food.protein / food.kcal.toDouble() else 0.0
                kotlin.math.abs(r - targetRatio)
            }
            .take(6)
    }
}
