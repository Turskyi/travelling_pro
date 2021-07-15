package io.github.turskyi.travellingpro.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ImageSpan
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.features.home.view.ui.HomeActivity

fun Context.isFacebookInstalled() = try {
    packageManager.getPackageInfo(
        getString(R.string.facebook_package),
        PackageManager.GET_META_DATA
    )
    true
} catch (e: PackageManager.NameNotFoundException) {
    false
}

fun Context.spToPix(@DimenRes sizeRes: Int) =
    resources.getDimension(sizeRes) / resources.displayMetrics.density

fun Context.getHomeActivity(): HomeActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is HomeActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Context.getAppCompatActivity(): AppCompatActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Context.getFragmentActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Context.convertPictureToSpannableString(imgRes: Int): SpannableString {
    val imageSpan = ImageSpan(this, imgRes)
    val spannableString = SpannableString(" ")
    spannableString.setSpan(imageSpan, " ".length - 1, " ".length, 0)
    return spannableString
}

fun <T> Context.openActivityWithArgs(destination: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, destination)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun Context.getToolbarHeight(): Int {
    val styledAttributes: TypedArray =
        theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
    val toolbarHeight = styledAttributes.getDimension(0, 0f).toInt()
    styledAttributes.recycle()
    return toolbarHeight
}

fun Context.toast(
    @StringRes msgResId: Int
) = Toast.makeText(this, msgResId, Toast.LENGTH_SHORT).show()

fun Context.toast(
    msg: String?
) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.toastLong(
    @StringRes msgResId: Int
) = Toast.makeText(this, msgResId, Toast.LENGTH_LONG).show()

fun Context.toastLong(
    msg: String?
) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

/**
 * @Description
 * Checks if device is online or not
 */
fun Context.isOnline(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        return if (network != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)!!
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || networkCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        } else false
    } else {
        // Initial Value
        var isConnected: Boolean? = false
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @Suppress("DEPRECATION") val activeNetwork = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        if (activeNetwork != null && activeNetwork.isConnected) {
            isConnected = true
        }
        return isConnected ?: false
    }
}
