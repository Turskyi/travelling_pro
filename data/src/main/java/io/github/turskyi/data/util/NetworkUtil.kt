package io.github.turskyi.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.IOException

fun hasNetwork(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        if (network != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)!!
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        } else false
    } else {
        isOnline()
    }
}

fun isOnline(): Boolean {
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