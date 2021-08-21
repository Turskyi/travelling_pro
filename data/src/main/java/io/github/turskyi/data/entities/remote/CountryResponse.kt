package io.github.turskyi.data.entities.remote

import com.google.gson.annotations.SerializedName

typealias CountryListResponse = MutableList<CountryResponse>
data class CountryResponse(
    @SerializedName("name") val name: String,
    @SerializedName("flag") val flag: String,
)