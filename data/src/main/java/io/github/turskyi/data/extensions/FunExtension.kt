package io.github.turskyi.data.extensions

import android.util.Log
import io.github.turskyi.data.constants.Constants.LOG

fun log(message: String?) {
    Log.d(LOG, "" + message)
}