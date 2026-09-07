package io.github.turskyi.data.datasources.remote.entities

import com.google.gson.annotations.SerializedName

typealias CountryListResponse = ArrayList<CountryResponse>

data class CountryResponse(
    @SerializedName("cca2")
    val cca2: String,
    @SerializedName("cca3")
    val cca3: String,
    @SerializedName("name")
    val name: NameResponse,
)

data class NameResponse(
    @SerializedName("common")
    val common: String,
)
