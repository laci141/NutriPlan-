package com.nutriplan.app.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Nyelvi (locale) segédobjektum.
 * Becsomagolja a Context-et a kiválasztott nyelv szerint, hogy az erőforrások
 * (string-ek) a megfelelő nyelven töltődjenek be – offline, alkalmazáson belüli nyelvváltáshoz.
 */
object LocaleHelper {

    /** A megadott nyelvi kód szerint új, lokalizált Context-et ad vissza. */
    fun wrap(context: Context, languageCode: String): Context {
        Logger.d(Logger.Tags.SETTINGS, "Context becsomagolása nyelvvel: $languageCode")
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
