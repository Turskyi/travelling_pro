package io.github.turskyi.travellingpro.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VisitedCountry(
    var id: Int,
    val title: String,
    var flag: String,
    var selfie: String?,
    var selfieName: String,
    var cities: List<City>,
) : Parcelable
