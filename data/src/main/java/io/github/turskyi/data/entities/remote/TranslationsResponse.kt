package io.github.turskyi.data.entities.remote

import com.google.gson.annotations.SerializedName

data class TranslationsResponse(
        @SerializedName("br")
        val br: String, // Zimbabwe
        @SerializedName("de")
        val de: String, // Simbabwe
        @SerializedName("es")
        val es: String, // Zimbabue
        @SerializedName("fa")
        val fa: String, // زیمباوه
        @SerializedName("fr")
        val fr: String, // Zimbabwe
        @SerializedName("hr")
        val hr: String, // Zimbabve
        @SerializedName("hu")
        val hu: String, // Zimbabwe
        @SerializedName("ja")
        val ja: String, // ジンバブエ
        @SerializedName("nl")
        val nl: String, // Zimbabwe
        @SerializedName("pt")
        val pt: String // Zimbabué
    )