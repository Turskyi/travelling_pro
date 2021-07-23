package io.github.turskyi.domain.model

data class VisitedCountryModel(
    var id: Int,
    val title: String,
    var flag: String,
    var selfie: String,
    var selfieName: String,
    var cities: List<CityModel>,
) {
    constructor(id: Int, title: String, flag: String, selfie: String, selfieName: String) : this(
        id = id,
        title = title,
        flag = flag,
        selfie = selfie,
        selfieName = selfieName,
        emptyList(),
    )
}
