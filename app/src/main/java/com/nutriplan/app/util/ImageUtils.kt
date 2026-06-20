package com.nutriplan.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Kép-segédfüggvények az AI fotó-felismeréshez. A kamera JPEG-képkockáját
 * lekicsinyítjük és tömörítjük, hogy a proxy-kérés kicsi maradjon, és base64-re
 * kódoljuk. A kép sosem kerül naplózásra vagy lemezre.
 */
object ImageUtils {

    /**
     * Egy CameraX JPEG [ImageProxy]-t lekicsinyített, JPEG-tömörített base64 sztringgé
     * alakít. A leghosszabb oldalt [maxDim] pixelre korlátozzuk.
     */
    fun imageProxyToBase64(image: ImageProxy, maxDim: Int = 768, quality: Int = 80): String {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val rotation = image.imageInfo.rotationDegrees
        if (rotation != 0) {
            val m = Matrix().apply { postRotate(rotation.toFloat()) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        }
        bitmap = downscale(bitmap, maxDim)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    private fun downscale(bitmap: Bitmap, maxDim: Int): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= maxDim) return bitmap
        val scale = maxDim.toFloat() / longest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )
    }
}
