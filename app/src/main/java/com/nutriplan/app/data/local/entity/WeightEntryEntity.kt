package com.nutriplan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Egy napi testsúly-bejegyzés (weight_log tábla). Naponta egy érték (az epochDay
 * a kulcs), így az ismételt mentés felülírja az aznapit.
 */
@Entity(tableName = "weight_log")
data class WeightEntryEntity(
    @PrimaryKey val epochDay: Long,
    val weightKg: Double
)
