package io.github.turskyi.travellingpro.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.toast(@StringRes msgResId: Int) = context?.toast(msgResId)
fun Fragment.toast(msg: String?) = context?.toast(msg)
fun Fragment.toastLong(@StringRes msgResId: Int) = context?.toastLong(msgResId)
fun Fragment.toastLong(msg: String?) = context?.toastLong(msg)

/**
 * @Description
 * Checks if device is online or not
 */
fun Fragment.isOnline(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val connectivityManager =
            this.activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        return if (network != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)!!
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        } else false
    } else {
        /* Initial Value */
        var isConnected: Boolean? = false
        val connectivityManager =
            this.activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @Suppress("DEPRECATION") val activeNetwork = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        if (activeNetwork != null && activeNetwork.isConnected) {
            isConnected = true
        }
        return isConnected ?: false
    }
}