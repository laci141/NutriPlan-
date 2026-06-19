package com.nutriplan.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Egy elfogyasztott étel naplóbejegyzése (food_log tábla).
 * A naphoz az epochDay (LocalDate.toEpochDay()) köti, így gyorsan szűrhető.
 */
@Entity(tableName = "food_log", indices = [Index("epochDay")])
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val epochDay: Long,
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val mealType: String,
    // Mikrotápanyagok (0.0 = nincs adat)
    val fiberG: Double = 0.0,
    val vitaminCMg: Double = 0.0,
    val ironMg: Double = 0.0,
    val calciumMg: Double = 0.0,
    val vitaminDUg: Double = 0.0,
    val b12Ug: Double = 0.0,
    val magnesiumMg: Double = 0.0
)
