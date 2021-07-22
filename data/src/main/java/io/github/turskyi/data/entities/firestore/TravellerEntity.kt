package io.github.turskyi.data.entities.firestore

data class TravellerEntity(
    val id: String,
    val name: String,
    val avatar: String,
    val countryList: List<VisitedCountryEntity>
) {
    // required empty constructor for firestore serialization
    constructor():this("","","", emptyList())
}