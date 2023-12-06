package me.codego.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

/**
 *
 * @author mengxn
 * @date 2023/12/1
 */

fun Bitmap?.toBytes(limit: Int): ByteArray? {
    if (this == null || this.isRecycled) {
        return null
    }
    val baos = ByteArrayOutputStream()
    val pixels = if (this.hasAlpha()) 4.0 else 2.0
    val maxSize: Int = sqrt(limit / pixels).toInt()
    val targetWidth: Int
    val targetHeight: Int
    if (this.width > this.height) {
        targetWidth = maxSize
        targetHeight = this.height * maxSize / this.width
    } else {
        targetHeight = maxSize
        targetWidth = this.width * maxSize / this.height
    }
    val bitmap = Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    if (bitmap.hasAlpha()) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    } else {
        val convertedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565)
        Canvas(convertedBitmap).drawBitmap(bitmap, 0f, 0f, null)
        convertedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    }
    return baos.toByteArray()
}

fun Context.toast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}