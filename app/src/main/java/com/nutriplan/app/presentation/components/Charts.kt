package com.nutriplan.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Egyszerű oszlopdiagram (Canvas) opcionális cél-vonallal. A [values] és [labels]
 * azonos hosszúak; az értékeket a legnagyobbhoz (vagy a célhoz) normalizáljuk.
 */
@Composable
fun SimpleBarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    goal: Float? = null
) {
    val maxValue = max(values.maxOrNull() ?: 0f, goal ?: 0f).coerceAtLeast(1f)
    val goalColor = MaterialTheme.colorScheme.error
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val n = values.size.coerceAtLeast(1)
            val slot = size.width / n
            val barWidth = slot * 0.5f
            // Cél-vonal
            goal?.let { g ->
                val y = size.height * (1f - (g / maxValue))
                drawLine(
                    color = goalColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2f
                )
            }
            values.forEachIndexed { i, v ->
                val barHeight = size.height * (v / maxValue)
                val left = i * slot + (slot - barWidth) / 2f
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }
        }
        if (labels.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Egyszerű vonaldiagram (Canvas) a megadott pontokból (pl. testsúly trend).
 */
@Composable
fun SimpleLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (points.size < 2) return
    val minV = points.min()
    val maxV = points.max()
    val range = (maxV - minV).coerceAtLeast(0.1f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val stepX = size.width / (points.size - 1)
        var prev: Offset? = null
        points.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height * (1f - ((v - minV) / range))
            val current = Offset(x, y)
            prev?.let {
                drawLine(
                    color = lineColor,
                    start = it,
                    end = current,
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(lineColor, radius = 5f, center = current)
            prev = current
        }
    }
}
