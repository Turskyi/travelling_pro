package io.github.turskyi.data.entities.firestore

data class CountryEntity(
        var id: Int,
        val name: String,
        val flag: String,
        var visited: Boolean?,
        var selfie: String?,
)
