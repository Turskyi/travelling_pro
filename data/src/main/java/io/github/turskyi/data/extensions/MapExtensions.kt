package io.github.turskyi.data.extensions

import io.github.turskyi.data.entities.network.CountryNet
import io.github.turskyi.data.entities.firestore.CityEntity
import io.github.turskyi.data.entities.firestore.CountryEntity
import io.github.turskyi.data.entities.firestore.VisitedCountryEntity
import io.github.turskyi.domain.model.CityModel
import io.github.turskyi.domain.model.CountryModel

fun List<CountryModel>.mapModelListToEntityList() =
    mapTo(mutableListOf(), { countryModel -> countryModel.mapModelToEntity() })

fun CountryModel.mapModelToEntity() = CountryEntity(id, name, flag, isVisited, null)
fun CityModel.mapModelToEntity() =
    CityEntity(name = name, parentId = parentId, month = month)

fun CityEntity.mapEntityToModel() =
    CityModel(name = name, parentId = parentId, month = month)

fun List<CountryNet>.mapNetListToModelList() = this.mapTo(
    mutableListOf(), { countryNet -> countryNet.mapNetToEntity() })

fun CountryEntity.mapEntityToModel() = CountryModel(id, name, flag, isVisited, selfie)
fun CountryNet.mapNetToEntity() = CountryModel(id, name, flag, visited, selfie = null)
fun List<CityEntity>.mapEntitiesToModelList() = mapTo(
    mutableListOf(), { cityEntity -> cityEntity.mapEntityToModel() })

fun List<CountryEntity>.mapEntityListToModelList() = mapTo(
    mutableListOf(), { countryEntity -> countryEntity.mapEntityToModel() })

fun CountryEntity.mapCountryToVisitedCountry() =
    VisitedCountryEntity(id = id, name = name, flag = flag, selfie = selfie)

fun VisitedCountryEntity.mapVisitedCountryToCountry() =
    CountryModel(id = id, name = name, flag = flag, selfie = selfie)

fun List<VisitedCountryEntity>.mapVisitedCountriesToModelList() = mapTo(
    mutableListOf(), { countryEntity -> countryEntity.mapVisitedCountryToCountry() })

