package io.github.turskyi.data.entities.network

import com.google.gson.annotations.SerializedName

typealias CountryListResponse = MutableList<CountryNet>
data class CountryNet(
    var id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("flag") val flag: String,
    var isVisited: Boolean?
)