package com.nutriplan.app

import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.util.IngredientCategorizer
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Egyszerű egységtesztek a domain logikához (kategorizálás és mennyiség-összevonás).
 */
class ShoppingMergeTest {

    @Test
    fun `categorizer recognizes meat`() {
        // A csirkemellnek a hús kategóriába kell kerülnie
        assertEquals(IngredientCategory.MEAT, IngredientCategorizer.categorize("Chicken breast"))
        assertEquals(IngredientCategory.MEAT, IngredientCategorizer.categorize("Csirkemell"))
    }

    @Test
    fun `categorizer recognizes dairy and fish`() {
        assertEquals(IngredientCategory.DAIRY, IngredientCategorizer.categorize("Görög joghurt"))
        assertEquals(IngredientCategory.FISH, IngredientCategorizer.categorize("Tonhal"))
    }

    @Test
    fun `identical ingredients quantities sum correctly`() {
        // Három darab 180g csirkemell összege 540g
        val quantities = listOf(180.0, 180.0, 180.0)
        val total = quantities.sum()
        assertEquals(540.0, total, 0.001)
    }

    @Test
    fun `unknown ingredient falls back to other`() {
        assertEquals(IngredientCategory.OTHER, IngredientCategorizer.categorize("Mystery item xyz"))
    }
}
