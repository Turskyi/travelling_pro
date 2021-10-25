package io.github.turskyi.data.entities.remote

import com.google.gson.annotations.SerializedName

data class FlagsResponse(
        @SerializedName("png")
        val png: String, // https://flagcdn.com/w320/zw.png
        @SerializedName("svg")
        val svg: String // https://flagcdn.com/zw.svg
    )