package io.github.turskyi.data.datasources.remote.entities

import com.google.gson.annotations.SerializedName

data class FlagsResponse(
        @SerializedName("png")
        val png: String, // https://flagcdn.com/w320/zw.png
        @SerializedName("svg")
        val svg: String // https://flagcdn.com/zw.svg
    )