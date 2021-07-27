package io.github.turskyi.data.util.extensions

import io.github.turskyi.data.entities.network.CountryResponse
import io.github.turskyi.data.entities.local.CityEntity
import io.github.turskyi.data.entities.local.CountryEntity
import io.github.turskyi.data.entities.local.TravellerEntity
import io.github.turskyi.data.entities.local.VisitedCountryEntity
import io.github.turskyi.domain.entities.CityModel
import io.github.turskyi.domain.entities.CountryModel
import io.github.turskyi.domain.entities.TravellerModel
import io.github.turskyi.domain.entities.VisitedCountryModel

fun List<CountryModel>.mapModelListToEntityList() =
    mapTo(mutableListOf(), { countryModel -> countryModel.mapModelToEntity() })

fun CountryModel.mapModelToEntity() = CountryEntity(id, name, flag, isVisited)
fun CityModel.mapModelToEntity(): CityEntity {
    return CityEntity(id = id, name = name, parentId = parentId, month = month)
}

fun CityEntity.mapEntityToModel(): CityModel {
    return CityModel(id = id, name = name, parentId = parentId, month = month)
}

fun List<CountryResponse>.mapNetListToModelList() = this.mapTo(
    mutableListOf(), { countryNet -> countryNet.mapNetToEntity() })

fun CountryEntity.mapEntityToModel() = CountryModel(
    id, name, flag, isVisited, "", ""
)

fun VisitedCountryEntity.mapEntityToModel() = VisitedCountryModel(
    id = id,
    title = name,
    flag = flag,
    selfie = selfie,
    selfieName = selfieName,
)

fun TravellerEntity.mapEntityToModel(): TravellerModel = TravellerModel(id, name, avatar)

fun CountryResponse.mapNetToEntity() = CountryModel(id, name, flag, isVisited)
fun List<CityEntity>.mapEntitiesToModelList() = mapTo(
    mutableListOf(),
    { cityEntity -> cityEntity.mapEntityToModel() },
)

fun List<CountryEntity>.mapEntityListToModelList() = mapTo(
    mutableListOf(),
    { countryEntity -> countryEntity.mapEntityToModel() }
)

fun List<TravellerEntity>.mapFirestoreListToModelList() = mapTo(
    mutableListOf(),
    { entity -> entity.mapEntityToModel() }
)

fun CountryEntity.mapCountryToVisitedCountry(): VisitedCountryEntity {
    return VisitedCountryEntity(id = id, name = name, flag = flag)
}

fun VisitedCountryEntity.mapVisitedCountryToCountry(): CountryModel {
    return CountryModel(id = id, name = name, flag = flag, selfie = selfie, selfieName = selfieName)
}

fun VisitedCountryEntity.mapVisitedCountryEntityToVisitedCountry(): VisitedCountryModel {
    return VisitedCountryModel(
        id = id,
        title = name,
        flag = flag,
        selfie = selfie,
        selfieName = selfieName,
    )
}

fun List<VisitedCountryEntity>.mapVisitedCountriesToModelList(): MutableList<CountryModel> {
    return mapTo(mutableListOf(), { countryEntity -> countryEntity.mapVisitedCountryToCountry() })
}

fun List<VisitedCountryEntity>.mapVisitedCountriesToVisitedModelList(): MutableList<VisitedCountryModel> {
    return mapTo(
        mutableListOf(),
        { countryEntity -> countryEntity.mapVisitedCountryEntityToVisitedCountry() })
}

