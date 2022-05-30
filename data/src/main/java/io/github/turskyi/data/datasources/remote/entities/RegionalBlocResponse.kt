package io.github.turskyi.data.datasources.remote.entities

import com.google.gson.annotations.SerializedName

data class RegionalBlocResponse(
        @SerializedName("acronym")
        val acronym: String, // AU
        @SerializedName("name")
        val name: String, // African Union
        @SerializedName("otherNames")
        val otherNames: List<String>
    )