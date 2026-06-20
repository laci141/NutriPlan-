package com.nutriplan.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/**
 * A recept-fotók helyi tárolásának segédosztálya.
 * A képeket az alkalmazás privát tárhelyén (filesDir/recipe_images) tartjuk,
 * így nincs szükség tárhely-engedélyre, és a fájlok az appal együtt törlődnek.
 */
object ImageStorage {

    private const val DIR_NAME = "recipe_images"

    /** A FileProvider authority – mindig az aktuális csomagnévhez igazodik (debug/release). */
    fun authority(context: Context): String = "${context.packageName}.fileprovider"

    private fun imagesDir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    /** Új, egyedi nevű képfájl létrehozása (a kamera ide ír majd). */
    fun newImageFile(context: Context): File =
        File(imagesDir(context), "recipe_${UUID.randomUUID()}.jpg")

    /** Content URI egy fájlhoz, amit átadhatunk a kamera alkalmazásnak. */
    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, authority(context), file)

    /**
     * Egy galériából választott kép bemásolása a privát tárhelyre.
     * @return az új, állandó fájl elérési útja, vagy null hiba esetén.
     */
    fun importFrom(context: Context, source: Uri): String? = try {
        val target = newImageFile(context)
        context.contentResolver.openInputStream(source)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        Logger.i(Logger.Tags.RECIPE, "Recept-kép importálva: ${target.name}")
        target.absolutePath
    } catch (e: Exception) {
        Logger.w(Logger.Tags.RECIPE, "Kép importálása sikertelen: ${e.message}")
        null
    }

    /** Egy korábban mentett kép törlése (ha létezik). */
    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
}
