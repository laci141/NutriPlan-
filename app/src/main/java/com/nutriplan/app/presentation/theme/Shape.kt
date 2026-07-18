package com.nutriplan.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Az alkalmazás lekerekített forma-rendszere (Material 3).
 * A kártyák és gombok bőségesen lekerekítettek (12–24 dp) a modern, lágy megjelenésért.
 */
val NutriPlanShapes = Shapes(
    // Kis elemek (chipek, kis gombok)
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    // Alapértelmezett gombok és beviteli mezők
    medium = RoundedCornerShape(16.dp),
    // Kártyák, lapok
    large = RoundedCornerShape(20.dp),
    // Kiemelt felületek, alsó lapok
    extraLarge = RoundedCornerShape(28.dp)
)
