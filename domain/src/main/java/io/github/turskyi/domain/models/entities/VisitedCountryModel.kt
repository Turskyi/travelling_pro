package io.github.turskyi.domain.models.entities

data class VisitedCountryModel(
    val id: Int,
    val shortName: String,
    val title: String,
    val flag: String,
    var selfie: String,
    var selfieName: String,
)
