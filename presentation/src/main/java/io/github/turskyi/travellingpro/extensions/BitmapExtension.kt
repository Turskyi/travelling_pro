package io.github.turskyi.travellingpro.extensions

import android.content.Context
import android.graphics.Bitmap
import io.github.turskyi.travellingpro.R
import java.io.File
import java.io.FileOutputStream

fun Bitmap.convertBitmapToFile(context: Context, fileName: String): File {
    val directoryFile = File(context.filesDir, context.resources.getString(R.string.dir_screenshots))
    if (!directoryFile.exists()) {
        directoryFile.mkdirs()
    }
    val file = File(directoryFile, fileName)
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