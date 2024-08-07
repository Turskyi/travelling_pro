package io.github.turskyi.travellingpro.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.net.*
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import io.github.turskyi.travellingpro.R


fun Context.isFacebookInstalled(): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                getString(R.string.facebook_package),
                PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            @Suppress("DEPRECATION") packageManager.getPackageInfo(
                getString(R.string.facebook_package),
                PackageManager.GET_META_DATA
            )
        }
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.spToPix(@DimenRes sizeRes: Int): Float {
    return resources.getDimension(sizeRes) / resources.displayMetrics.density
}

fun Context.getAppCompatActivity(): AppCompatActivity? {
    var context: Context = this
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

fun Context.getFragmentActivity(): FragmentActivity? {
    var context: Context = this
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

fun <T> Context.openActivityWithObject(destination: Class<T>, argId: String, extra: Parcelable) {
    val intent = Intent(this, destination)
    intent.putExtra(argId, extra)
    startActivity(intent)
}

fun <T> Context.openActivityWithArgs(destination: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, destination)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun <T> Context.openActivity(destination: Class<T>) {
    val intent = Intent(this, destination)
    startActivity(intent)
}

fun Context.getToolbarHeight(): Int {
    val styledAttributes: TypedArray =
        theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
    val toolbarHeight = styledAttributes.getDimension(0, 0f).toInt()
    styledAttributes.recycle()
    return toolbarHeight
}

fun Context.toast(@StringRes msgResId: Int) {
    Toast.makeText(this, msgResId, Toast.LENGTH_SHORT).show()
}

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.toastLong(@StringRes msgResId: Int) {
    Toast.makeText(this, msgResId, Toast.LENGTH_LONG).show()
}

fun Context.toastLong(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

/**
 * @Description
 * Checks if device is online or not
 */
@Suppress("BooleanMethodIsAlwaysInverted")
fun Context.isOnline(): Boolean {
    if (this.getSystemService(Context.CONNECTIVITY_SERVICE) is ConnectivityManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager: ConnectivityManager =
                this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

//            TODO: Handle warning "Missing permissions required by ConnectivityManager.getActiveNetwork: android.permission.ACCESS_NETWORK_STATE"
            @SuppressLint("MissingPermission")
            val network: Network? = connectivityManager.activeNetwork
            return if (network != null) {
                //TODO: Handle warning "Missing permissions required by ConnectivityManager.getNetworkCapabilities: android.permission.ACCESS_NETWORK_STATE"
                @SuppressLint("MissingPermission")
                val networkCapabilities: NetworkCapabilities? =
                    connectivityManager.getNetworkCapabilities(network)
                return if (networkCapabilities != null) {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                } else false
            } else false
        } else {
            // Initial Value
            var isConnected = false

            val connectivityManager: ConnectivityManager =
                this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            @Suppress("DEPRECATION")
            //TODO: Handle warning "Missing permissions required by ConnectivityManager.getActiveNetwork: android.permission.ACCESS_NETWORK_STATE"
            @SuppressLint("MissingPermission")
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            if (activeNetwork != null && activeNetwork.isConnected) {
                isConnected = true
            }
            return isConnected
        }
    } else {
        return false
    }
}

fun Context.showReportDialog() {
    val lastDialog: AlertDialog = AlertDialog.Builder(this)
        .setTitle(getString(R.string.alert_error_title))
        .setMessage(getString(R.string.alert_error_message))
        .setCancelable(false)
        .setPositiveButton(
            getString(R.string.yes)
        ) { _: DialogInterface?, _: Int ->
            val intent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts(
                    getString(R.string.scheme_mailto),
                    getString(R.string.email),
                    /* Gets the decoded fragment part of this URI,
                     * everything after the '#', null if undefined. */
                    null
                )
            )
            intent.putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.error_message_intro) + this.toString(),
            )
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }.setNegativeButton(
            getString(R.string.no)
        ) { dialog: DialogInterface, _: Int ->
            toast(R.string.error_message_try_tomorrow)
            dialog.cancel()
            val newIntent = Intent(this, javaClass)
            startActivity(newIntent)
        }.create()
    lastDialog.show()
}