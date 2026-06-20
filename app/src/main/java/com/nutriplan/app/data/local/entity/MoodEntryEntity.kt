package com.nutriplan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Egy napi hangulat-bejegyzés (mood_log tábla). Naponta egy érték (az epochDay
 * a kulcs), így az ismételt mentés felülírja az aznapit. A [mood] a MoodLevel kulcsa.
 */
@Entity(tableName = "mood_log")
data class MoodEntryEntity(
    @PrimaryKey val epochDay: Long,
    val mood: String
)
