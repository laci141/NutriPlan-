package com.nutriplan.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Hozzávaló adatbázis entitás (ingredients tábla).
 * Minden hozzávaló egy recepthez tartozik (recipeId idegen kulcs).
 * A recept törlésekor a hozzávalói is törlődnek (CASCADE).
 */
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val recipeId: Long,
    val name: String,
    val quantity: Double,
    // Mértékegység kulcsa szövegként (pl. "g")
    val unit: String,
    // Bevásárlólista kategória kulcsa szövegként (pl. "meat")
    val category: String
)
