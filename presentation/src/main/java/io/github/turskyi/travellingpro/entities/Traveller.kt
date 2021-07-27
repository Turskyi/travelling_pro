package io.github.turskyi.travellingpro.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Traveller(
    val id: String,
    val name: String,
    val avatar: String,
    val countryList: List<VisitedCountry>
):Parcelable