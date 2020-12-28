package io.github.turskyi.travellingpro.extensions

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

fun Bitmap.convertBitmapToFile(context: Context, fileName: String): File {
    val dirPath = context.externalCacheDir?.absolutePath + "/Screenshots"
    val dir = File(dirPath)
    if (!dir.exists()) dir.mkdirs()
    val file = File(dirPath, fileName)
    try {
        val fileOutputStream = FileOutputStream(file)
        this.compress(Bitmap.CompressFormat.PNG, 85, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return file
}