package io.github.turskyi.travellingpro.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VisitedCountry(
    val id: Int,
    val shortName: String,
    val title: String,
    var flag: String,
    var selfie: String,
    /* [selfieName] is the id of the file that we later will be using to store picture in
     * firebase storage */
    var selfieName: String,
    var cities: List<City>,
) : Parcelable {
    constructor(id: Int, shortName: String, title: String, flag: String, selfie: String, selfieName: String) : this(
        id,
        shortName,
        title,
        flag,
        selfie,
        selfieName,
        emptyList(),
    )
}
