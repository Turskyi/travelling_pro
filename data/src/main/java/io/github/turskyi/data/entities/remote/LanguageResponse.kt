package io.github.turskyi.data.entities.remote

import com.google.gson.annotations.SerializedName

data class LanguageResponse(
        @SerializedName("iso639_1")
        val iso6391: String, // en
        @SerializedName("iso639_2")
        val iso6392: String, // eng
        @SerializedName("name")
        val name: String, // English
        @SerializedName("nativeName")
        val nativeName: String // English
    )