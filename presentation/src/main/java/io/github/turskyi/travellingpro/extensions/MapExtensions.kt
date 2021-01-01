package io.github.turskyi.travellingpro.extensions

import io.github.turskyi.domain.model.CityModel
import io.github.turskyi.domain.model.CountryModel
import io.github.turskyi.travellingpro.models.City
import io.github.turskyi.travellingpro.models.Country
import io.github.turskyi.travellingpro.models.VisitedCountry

fun List<CountryModel>.mapModelListToActualList() = this.mapTo(
    mutableListOf(), { it.mapModelToActual() })

fun List<CountryModel>.mapModelListToNodeList() = this.mapTo(
    mutableListOf(), { model -> model.mapModelToNode() })

fun CountryModel.mapModelToNode() = VisitedCountry(
    id = id, title = name, img = flag, visited = isVisited, selfie = selfie
)

fun CountryModel.mapModelToActual() = Country(id, name, flag, isVisited, selfie)
fun Country.mapToModel() = CountryModel(id, name, flag, visited, selfie)
fun VisitedCountry.mapVisitedCountryNodeToCountry() = Country(
    id = id, visited = visited, name = title,
    flag = img, selfie = selfie
)

fun CityModel.mapModelToBaseNode() = City(id = id, name = name, parentId = parentId, month = month)
fun City.mapNodeToModel() = CityModel(id = id, name = name, parentId = parentId, month = month)
