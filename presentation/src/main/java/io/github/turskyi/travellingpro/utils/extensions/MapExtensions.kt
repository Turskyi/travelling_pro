package io.github.turskyi.travellingpro.utils.extensions

import io.github.turskyi.domain.entities.CityModel
import io.github.turskyi.domain.entities.CountryModel
import io.github.turskyi.domain.entities.VisitedCountryModel
import io.github.turskyi.travellingpro.entities.City
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.entities.VisitedCountryNode

fun List<CountryModel>.mapModelListToCountryList(): MutableList<Country> {
    return this.mapTo(
        mutableListOf(),
        { it.mapModelToCountry() },
    )
}

fun List<VisitedCountryModel>.mapVisitedModelListToVisitedList(): MutableList<VisitedCountry> {
    return this.mapTo(
        mutableListOf(),
        { it.mapModelToCountry() },
    )
}

fun VisitedCountryModel.mapModelToCountry() = VisitedCountry(id, title, flag, selfie, selfieName)

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

fun List<VisitedCountry>.mapVisitedListToVisitedNodeList(): MutableList<VisitedCountryNode> {
    return this.mapTo(
        mutableListOf(),
        { it.mapVisitedCountryToNode() },
    )
}

fun VisitedCountry.mapVisitedCountryToNode() = VisitedCountryNode(
    id = id,
    title = title,
    flag = flag,
    selfie = selfie,
    selfieName = selfieName
)

fun CityModel.mapModelToBaseNode() = City(id = id, name = name, parentId = parentId, month = month)
fun City.mapNodeToModel() = CityModel(id = id, name = name, parentId = parentId, month = month)
