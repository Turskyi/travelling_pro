package io.github.turskyi.travellingpro.common

import io.github.turskyi.travellingpro.BuildConfig.APPLICATION_ID

/**
 * @Description Constants for the app.
 */
object Constants {
    const val LOG = "===>"

    /* # milliseconds, desired time passed between two back presses. */
    const val TIME_INTERVAL = 2000

    const val ACCESS_LOCATION_AND_EXTERNAL_STORAGE = 10001

    const val ACCESS_LOCATION = 10002

    const val GOOGLE_PLAY_ADDRESS = "https://play.google.com/store/apps/details?id=$APPLICATION_ID"
}