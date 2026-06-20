package com.nutriplan.app.domain.model

import kotlin.math.roundToInt

/**
 * Tömeg-mértékegység (testsúlyhoz). A tárolás mindig kilogrammban történik,
 * csak a megjelenítés és a bevitel váltódik át a választott egységre.
 */
enum class MassUnit(val key: String) {
    KG("kg"),
    LB("lb");

    companion object {
        fun fromKey(key: String?): MassUnit = entries.firstOrNull { it.key == key } ?: KG
    }
}

/**
 * Hossz-mértékegység (testmagassághoz / méretekhez). A tárolás mindig
 * centiméterben történik, csak a megjelenítés váltódik át.
 */
enum class LengthUnit(val key: String) {
    CM("cm"),
    INCH("inch");

    companion object {
        fun fromKey(key: String?): LengthUnit = entries.firstOrNull { it.key == key } ?: CM
    }
}

/**
 * Központi mértékegység-átváltó. Minden belső adat metrikus (kg, cm);
 * ez a segéd alakítja a megjelenítendő/beolvasott értékeket a választott egységre.
 */
object UnitFormatter {

    private const val KG_TO_LB = 2.2046226218
    private const val CM_TO_INCH = 0.3937007874

    /** A választott egység rövid címkéje (pl. "kg" vagy "lb"). */
    fun massLabel(unit: MassUnit): String = unit.key

    /** A választott egység rövid címkéje (pl. "cm" vagy "in"). */
    fun lengthLabel(unit: LengthUnit): String = if (unit == LengthUnit.INCH) "in" else "cm"

    /** Kilogrammból a választott egység szerinti számérték. */
    fun massValue(kg: Double, unit: MassUnit): Double =
        if (unit == MassUnit.LB) kg * KG_TO_LB else kg

    /** A választott egységben megadott értékből kilogramm (tároláshoz). */
    fun massToKg(value: Double, unit: MassUnit): Double =
        if (unit == MassUnit.LB) value / KG_TO_LB else value

    /** Formázott testsúly a választott egységgel, pl. "72.5 kg" / "159.8 lb". */
    fun formatMass(kg: Double, unit: MassUnit): String =
        "${trim(massValue(kg, unit))} ${massLabel(unit)}"

    /** Csak a számérték (egység nélkül), pl. súlykülönbség kijelzéséhez. */
    fun formatMassNumber(kg: Double, unit: MassUnit): String = trim(massValue(kg, unit))

    /** Centiméterből a választott egység szerinti számérték. */
    fun lengthValue(cm: Double, unit: LengthUnit): Double =
        if (unit == LengthUnit.INCH) cm * CM_TO_INCH else cm

    /** Formázott hossz a választott egységgel, pl. "180 cm" / "70.9 in". */
    fun formatLength(cm: Double, unit: LengthUnit): String =
        "${trim(lengthValue(cm, unit))} ${lengthLabel(unit)}"

    /** Egy tizedesre kerekít, de egész értéknél elhagyja a ",0"-t. */
    private fun trim(value: Double): String {
        val rounded = (value * 10).roundToInt() / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
        else String.format("%.1f", rounded)
    }
}
