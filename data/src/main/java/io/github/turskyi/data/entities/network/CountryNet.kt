package io.github.turskyi.data.entities.network

import com.google.gson.annotations.SerializedName

typealias CountryListResponse = MutableList<CountryNet>
data class CountryNet(
    val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("flag") val flag: String,
    val isVisited: Boolean
)