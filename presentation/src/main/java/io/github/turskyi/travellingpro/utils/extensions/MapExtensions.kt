package io.github.turskyi.travellingpro.utils.extensions

import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.CountryModel
import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.domain.models.entities.VisitedCountryModel
import io.github.turskyi.travellingpro.entities.*

fun List<CountryModel>.mapModelListToCountryList(): MutableList<Country> {
    return this.mapTo(mutableListOf()) { it.mapModelToCountry() }
}

fun List<VisitedCountryModel>.mapVisitedModelListToVisitedList(): MutableList<VisitedCountry> {
    return this.mapTo(mutableListOf()) { it.mapModelToCountry() }
}

fun VisitedCountryModel.mapModelToCountry(): VisitedCountry {
    return VisitedCountry(id, shortName, title, flag, selfie, selfieName)
}

fun CountryModel.mapModelToCountry(): Country {
    return Country(id, shortName, name, flag, isVisited, selfie, selfieName)
}

fun Country.mapToModel() = CountryModel(id, shortName, name, flag, isVisited, selfie, selfieName)
fun VisitedCountryNode.mapVisitedCountryNodeToCountry(): Country {
    return Country(
        id = id,
        shortName = shortName,
        isVisited = true,
        name = title,
        flag = flag,
        selfie = selfie,
        selfieName = selfieName,
    )
}

fun List<VisitedCountry>.mapVisitedListToVisitedNodeList(): MutableList<VisitedCountryNode> {
    return this.mapTo(mutableListOf()) { it.mapVisitedCountryToNode() }
}

fun VisitedCountry.mapVisitedCountryToNode() = VisitedCountryNode(
    id = id,
    shortName = shortName,
    title = title,
    flag = flag,
    selfie = selfie,
    selfieName = selfieName
)

fun CityModel.mapModelToBaseNode() = City(id = id, name = name, parentId = parentId, month = month)
fun List<CityModel>.mapModelListToBaseNodeList(): MutableList<City> {
    return this.mapTo(mutableListOf()) { it.mapModelToBaseNode() }
}

fun City.mapNodeToModel() = CityModel(id = id, name = name, parentId = parentId, month = month)

fun List<TravellerModel>.mapModelListToTravellerList(): MutableList<Traveller> {
    return this.mapTo(mutableListOf()) { it.mapModelToTraveller() }
}

private fun TravellerModel.mapModelToTraveller(): Traveller {
    return Traveller(
        id = id,
        name = name,
        avatar = avatar,
        counter = counter,
        isVisible = isVisible,
    )
}
