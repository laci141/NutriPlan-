package com.nutriplan.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.NutritionTotals
import androidx.compose.ui.res.stringResource

/**
 * Újrahasznosítható megjelenítési komponensek.
 */

/** Üres állapot középre igazított üzenettel. */
@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** A fő tápértékek (kalória, fehérje, szénhidrát, zsír, rost) egymás alatti, színes összesítője. */
@Composable
fun NutritionSummary(totals: NutritionTotals, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MacroLine(
            label = stringResource(R.string.calories),
            value = "${totals.calories} ${stringResource(R.string.kcal_unit)}",
            color = NutriCalorieColor
        )
        MacroLine(
            label = stringResource(R.string.protein),
            value = "${totals.protein.toInt()} g",
            color = NutriProteinColor
        )
        MacroLine(
            label = stringResource(R.string.carbs),
            value = "${totals.carbs.toInt()} g",
            color = NutriCarbsColor
        )
        MacroLine(
            label = stringResource(R.string.fat),
            value = "${totals.fat.toInt()} g",
            color = NutriFatColor
        )
        MacroLine(
            label = stringResource(R.string.fiber),
            value = "${totals.fiberG.toInt()} g",
            color = NutriFiberColor
        )
    }
}

// A kezdőlapi makró-kártyával egyező színek, hogy egységes legyen az egész app.
private val NutriCalorieColor = Color(0xFFF97316) // narancs – kalória
private val NutriProteinColor = Color(0xFF34D399) // almazöld – fehérje
private val NutriCarbsColor = Color(0xFFF59E0B)   // borostyán – szénhidrát
private val NutriFatColor = Color(0xFF3B82F6)     // kék – zsír
private val NutriFiberColor = Color(0xFF22C55E)   // zöld – rost

/**
 * Egyetlen tápérték-sor: színes pötty + címke (balra) + érték (jobbra).
 * A kezdőlapon használt elrendezést tükrözi, hogy egymás alatt, jól olvashatóan jelenjen meg.
 */
@Composable
private fun MacroLine(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
