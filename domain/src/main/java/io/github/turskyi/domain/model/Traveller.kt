package io.github.turskyi.domain.model

data class Traveller(
    val id: String,
    val name: String,
    val avatar: String,
    val countryList: List<CountryModel>
)