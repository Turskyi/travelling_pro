package io.github.turskyi.domain.model

data class VisitedCountryModel(
    var id: Int,
    val title: String,
    var flag: String,
    var selfie: String?,
    var selfieName: String?,
    var cities: List<CityModel>,
)
