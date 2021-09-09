package io.github.turskyi.travellingpro.utils.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import java.io.IOException

fun Fragment.toast(@StringRes msgResId: Int) = requireContext().toast(msgResId)
fun Fragment.toast(msg: String) = requireContext().toast(msg)
fun Fragment.toastLong(@StringRes msgResId: Int) = requireContext().toastLong(msgResId)
fun Fragment.toastLong(msg: String) = requireContext().toastLong(msg)

/**
 * @Description
 * Checks if device is online or not
 */
fun Fragment.isOnline(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val connectivityManager: ConnectivityManager =
            this.requireActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network: Network? = connectivityManager.activeNetwork
        return if (network != null) {
            val networkCapabilities: NetworkCapabilities? =
                connectivityManager.getNetworkCapabilities(network)
            if (networkCapabilities != null) {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            } else {
                false
            }
        } else {
            false
        }
    } else {
        val runtime: Runtime = Runtime.getRuntime()
        try {
            // Pinging to Google server
            val ipProcess: Process = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue: Int = ipProcess.waitFor()
            return exitValue == 0
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        } catch (interruptedException: InterruptedException) {
            interruptedException.printStackTrace()
        }
        return false
    }
}