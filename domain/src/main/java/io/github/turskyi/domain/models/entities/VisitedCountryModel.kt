package io.github.turskyi.domain.models.entities

data class VisitedCountryModel(
    var id: Int,
    val title: String,
    val flag: String,
    var selfie: String,
    var selfieName: String,
)
