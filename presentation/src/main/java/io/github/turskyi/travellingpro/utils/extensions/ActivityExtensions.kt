package io.github.turskyi.travellingpro.utils.extensions

import android.app.Activity
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowMetrics
import android.widget.Toast
import androidx.annotation.StringRes
import io.github.turskyi.travellingpro.R

fun Activity.getScreenWidth() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
    val insets: Insets = windowMetrics.windowInsets
        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
    windowMetrics.bounds.width() - insets.left - insets.right
} else {
    val displayMetrics = DisplayMetrics()
    @Suppress("DEPRECATION")
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    displayMetrics.widthPixels
}

fun Activity.toastLong(@StringRes msg: Int) =
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun Activity.toastLong(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun Activity.toast(@StringRes message: Int) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Activity.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Activity.showSnackbar(@StringRes message: Int) {
    val viewGroup = (findViewById<ViewGroup>(R.id.content)).getChildAt(0) as ViewGroup
    viewGroup.showSnackBar(message)
}