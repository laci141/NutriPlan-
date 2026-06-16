package com.nutriplan.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/** Egyetlen tápérték adat (címke + érték) egy kis kártyában. */
@Composable
fun MacroChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** A négy fő tápérték (kalória, fehérje, szénhidrát, zsír) vízszintes összesítője. */
@Composable
fun NutritionSummary(totals: NutritionTotals, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MacroChip(
            label = stringResource(R.string.calories),
            value = "${totals.calories} ${stringResource(R.string.kcal_unit)}",
            modifier = Modifier.weight(1f)
        )
        MacroChip(
            label = stringResource(R.string.protein),
            value = "${totals.protein.toInt()}g",
            modifier = Modifier.weight(1f)
        )
        MacroChip(
            label = stringResource(R.string.carbs),
            value = "${totals.carbs.toInt()}g",
            modifier = Modifier.weight(1f)
        )
        MacroChip(
            label = stringResource(R.string.fat),
            value = "${totals.fat.toInt()}g",
            modifier = Modifier.weight(1f)
        )
    }
}
