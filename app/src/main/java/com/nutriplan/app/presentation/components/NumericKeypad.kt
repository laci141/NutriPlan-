package com.nutriplan.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Egyedi numerikus billentyűzet — a rendszer-billentyűzet soha nem ugrik fel.
 * Numerikus és decimális módban is használható.
 *
 * @param onDigit    0–9 gomb megnyomva
 * @param onBackspace törlés (utolsó karakter)
 * @param onConfirm  ✓ gomb (ha [confirmEnabled])
 * @param allowDecimal ha igaz, megjelenik a „." gomb; ha hamis, a „." helyén üres gomb van
 * @param confirmEnabled a ✓ gomb aktív-e
 * @param buttonHeight egy gomb magassága
 */
@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    allowDecimal: Boolean = false,
    confirmEnabled: Boolean = true,
    accentColor: Color = Color.Unspecified,
    buttonHeight: Dp = 56.dp
) {
    val accent = if (accentColor == Color.Unspecified) MaterialTheme.colorScheme.primary else accentColor
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (allowDecimal) "." else "", "0", "⌫")
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> KeypadKey(
                            label = "",
                            onClick = {},
                            enabled = false,
                            containerColor = Color.Transparent,
                            contentColor = Color.Transparent,
                            modifier = Modifier.weight(1f),
                            height = buttonHeight
                        )
                        "⌫" -> KeypadKey(
                            label = "⌫",
                            onClick = onBackspace,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f),
                            height = buttonHeight
                        )
                        else -> KeypadKey(
                            label = key,
                            onClick = { onDigit(key) },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            height = buttonHeight
                        )
                    }
                }
            }
        }
        // Confirm row (full width)
        KeypadKey(
            label = "✓",
            onClick = onConfirm,
            enabled = confirmEnabled,
            containerColor = accent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxWidth(),
            height = buttonHeight,
            fontSize = 22
        )
    }
}

@Composable
private fun KeypadKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    height: Dp = 56.dp,
    fontSize: Int = 20
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.3f),
            disabledContentColor = contentColor.copy(alpha = 0.3f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = label,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
