package com.nutriplan.app.presentation.theme

import androidx.compose.ui.graphics.Color
import com.nutriplan.app.domain.model.WeekDay
import com.nutriplan.app.presentation.navigation.Routes

/**
 * Visszafogott, szemkímélő ("kék fény szűrő" jellegű) akcentus-paletta sötét háttéren.
 *
 * A fő képernyőcímek mindig fehérek (tiszta, jól olvasható), a hét napjai és az
 * alsó navigáció füljei pedig saját, de tompított (kevésbé élénk) színt kapnak –
 * így a felület színes és nem egyhangú, de hosszú használat mellett sem fárasztja a szemet.
 */
object Accent {

    /** A fő képernyőcímek színe – mindig tiszta fehér. */
    val Title = Color(0xFFFFFFFF)

    /** Tompított zöld – a Kezdőlap (Dashboard) fül színe. */
    val Neon = Color(0xFF2A9E76)

    // A hét napjainak tompított szivárvány-színei (hétfőtől vasárnapig).
    private val coral = Color(0xFFC25067)   // mély korall
    private val orange = Color(0xFFC2682A)  // terrakotta
    private val amber = Color(0xFFB8921A)   // mély borostyán
    private val green = Color(0xFF2A9E76)   // mély zöld
    private val teal = Color(0xFF22A396)    // mély türkiz
    private val blue = Color(0xFF4279CC)    // mély kék
    private val violet = Color(0xFF7A65C2)  // tompított lila

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

    /** Egy adott navigációs útvonalhoz tartozó szín (ismeretlen esetén tompított zöld). */
    fun forRoute(route: String?): Color = routeColors[route] ?: Neon
}
