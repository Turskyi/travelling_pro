package io.github.turskyi.travellingpro.extensions

import android.util.Log
import io.github.turskyi.travellingpro.common.Constants.LOG

fun log(message: String?) {
    Log.d(LOG, "\n\n\n\n==> $message <==\n\n\n")
}