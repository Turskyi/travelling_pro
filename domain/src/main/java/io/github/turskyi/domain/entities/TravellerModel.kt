package io.github.turskyi.domain.entities

data class TravellerModel(
    val id: String,
    val name: String,
    val avatar: String,
    val countryList: List<VisitedCountryModel>
) {
    // required empty constructor for firestore serialization
    constructor():this("","","", emptyList())
}