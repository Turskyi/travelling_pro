package io.github.turskyi.travellingpro.models

data class Traveller(
    val id: String,
    val name: String,
    val avatar: String,
    val countryList: List<VisitedCountry>
)