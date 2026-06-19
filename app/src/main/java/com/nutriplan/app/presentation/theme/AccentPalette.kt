package com.nutriplan.app.presentation.theme

import androidx.compose.ui.graphics.Color
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.presentation.navigation.Routes

/**
 * Élénk, 2026-os akcentus-paletta: neon kiemelés sötét háttéren.
 *
 * A fő képernyőcímek neon zöldek, a hét napjai és az alsó navigáció füljei
 * pedig saját, jól megkülönböztethető színt kapnak – így a felület színesebb,
 * nem egyhangú, de a szín mindig csak kiemelésként (akcentusként) jelenik meg.
 */
object Accent {

    /** Neon zöld – a fő képernyőcímek és a Kezdőlap (Dashboard) fül színe. */
    val Neon = Color(0xFF2DD36F)

    // A hét napjainak szivárvány-színei (hétfőtől vasárnapig).
    private val coral = Color(0xFFFB7185)   // korall
    private val orange = Color(0xFFFB923C)  // narancs
    private val amber = Color(0xFFFBBF24)   // borostyán
    private val green = Color(0xFF34D399)   // zöld
    private val teal = Color(0xFF2DD4BF)    // türkiz
    private val blue = Color(0xFF60A5FA)    // kék
    private val violet = Color(0xFFA78BFA)  // lila

    private val dayColors = listOf(coral, orange, amber, green, teal, blue, violet)

    /** Egy adott naphoz tartozó akcentus-szín (a sorrend szerint). */
    fun forDay(day: WeekDay): Color = dayColors[day.ordinal % dayColors.size]

    // Az alsó navigáció füljeinek színei útvonal szerint.
    private val routeColors: Map<String, Color> = mapOf(
        Routes.DASHBOARD to Neon,
        Routes.PLANNER to violet,
        Routes.RECIPES to amber,
        Routes.SHOPPING to teal,
        Routes.NUTRITION to coral,
        Routes.SETTINGS to blue
    )

    /** Egy adott navigációs útvonalhoz tartozó szín (ismeretlen esetén neon zöld). */
    fun forRoute(route: String?): Color = routeColors[route] ?: Neon
}
