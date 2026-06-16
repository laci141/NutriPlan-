package com.nutriplan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bevásárlólista tétel entitás (shopping_items tábla).
 * A heti tervből automatikusan generálódik, az azonos hozzávalók összevonva.
 */
@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val purchased: Boolean = false,
    // Fordítási kulcs az összevont hozzávalóhoz (null = literál név)
    val nameKey: String? = null
)
