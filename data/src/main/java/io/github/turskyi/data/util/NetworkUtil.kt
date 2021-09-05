package io.github.turskyi.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

fun hasNetwork(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        return if (network != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)!!
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            )
        } else false
    } else {
        // Initial Value
        var isConnected: Boolean? = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @Suppress("DEPRECATION") val activeNetwork = connectivityManager.activeNetworkInfo
        @Suppress("DEPRECATION")
        if (activeNetwork != null && activeNetwork.isConnected) {
            isConnected = true
        }
        return isConnected ?: false
    }
}