package com.nutriplan.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Egyetlen körív alapú haladásjelző gyűrű, lekerekített végekkel.
 * Középre tetszőleges tartalom (pl. szám) helyezhető.
 */
@Composable
fun CalorieRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    ringColor: Color = Color(0xFF34D399),
    trackColor: Color = Color(0x3334D399),
    strokeWidth: Dp = 14.dp,
    content: @Composable () -> Unit = {}
) {
    // Sima animáció a haladás változásakor
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "calorieRing"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
            val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
            // Háttérgyűrű
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Haladás
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        content()
    }
}

/** Egy makró-gyűrű adatai (arány + szín). */
data class RingData(val fraction: Float, val color: Color)

/**
 * Egymásba ágyazott aktivitási gyűrűk (Apple Watch stílus) a makrók követésére.
 * A lista első eleme a legkülső gyűrű.
 */
@Composable
fun ActivityRings(
    rings: List<RingData>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    gap: Dp = 6.dp,
    content: @Composable () -> Unit = {}
) {
    val animatedFractions = rings.map { ring ->
        animateFloatAsState(
            targetValue = ring.fraction.coerceIn(0f, 1f),
            animationSpec = tween(700),
            label = "activityRing"
        )
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val gapPx = gap.toPx()
            rings.forEachIndexed { index, ring ->
                // Minden következő gyűrű beljebb kerül
                val inset = stroke / 2 + index * (stroke + gapPx)
                val arcSize = androidx.compose.ui.geometry.Size(
                    size.width - inset * 2,
                    size.height - inset * 2
                )
                val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)
                // Halvány háttér
                drawArc(
                    color = ring.color.copy(alpha = 0.20f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // Haladás
                drawArc(
                    color = ring.color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedFractions[index].value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}
