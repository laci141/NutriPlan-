package com.nutriplan.app.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nutriplan.app.data.preferences.DashboardPreferences
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.first

/**
 * Kezdőképernyő-widget (Jetpack Glance) a vízfogyasztáshoz.
 * A widgeten egyetlen koppintással +250 ml víz adható hozzá az app megnyitása nélkül.
 */
class WaterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // A mai vízmennyiség kiolvasása ugyanabból a DataStore-ból, amit az app használ
        val water = DashboardPreferences(context).waterToday.first()
        provideContent {
            WidgetContent(water)
        }
    }

    @Composable
    private fun WidgetContent(water: Int) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.widgetBackground)
                    .cornerRadius(24.dp)
                    .padding(16.dp)
                    // A teljes widgetre koppintva is hozzáad 250 ml-t
                    .clickable(actionRunCallback<AddWaterAction>()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💧 ${water} ml",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "+250 ml",
                    modifier = GlanceModifier
                        .padding(top = 8.dp)
                        .background(ColorProvider(Color(0xFF059669)))
                        .cornerRadius(16.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

/** A widget +250 ml gombjának/koppintásának művelete. */
class AddWaterAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        DashboardPreferences(context).addWater(250)
        Logger.i(Logger.Tags.SETTINGS, "Víz hozzáadva a widgetről: +250 ml")
        // A widget frissítése az új értékkel
        WaterWidget().update(context, glanceId)
    }
}

/** A widget rendszer-fogadója (a manifestben regisztrálva). */
class WaterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WaterWidget()
}
