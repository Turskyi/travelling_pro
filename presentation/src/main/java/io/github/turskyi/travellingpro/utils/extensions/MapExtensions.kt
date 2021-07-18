package io.github.turskyi.travellingpro.utils.extensions

import io.github.turskyi.domain.model.CityModel
import io.github.turskyi.domain.model.CountryModel
import io.github.turskyi.travellingpro.models.City
import io.github.turskyi.travellingpro.models.Country
import io.github.turskyi.travellingpro.models.VisitedCountryNode

fun List<CountryModel>.mapModelListToCountryList() = this.mapTo(
    mutableListOf(), { it.mapModelToCountry() })

fun List<CountryModel>.mapModelListToNodeList() = this.mapTo(
    mutableListOf(), { model -> model.mapModelToNode() })

fun CountryModel.mapModelToNode() = VisitedCountryNode(
    id = id,
    title = name,
    flag = flag,
    selfie = selfie,
    selfieName = selfieName
)

fun CountryModel.mapModelToCountry() = Country(id, name, flag, isVisited, selfie, selfieName)
fun Country.mapToModel() = CountryModel(id, name, flag, visited, selfie, selfieName)
fun VisitedCountryNode.mapVisitedCountryNodeToCountry() = Country(
    id = id, visited = true, name = title,
    flag = flag, selfie = selfie, selfieName = selfieName
)

fun CityModel.mapModelToBaseNode() = City(name = name, parentId = parentId, month = month)
fun City.mapNodeToModel() = CityModel(name = name, parentId = parentId, month = month)
