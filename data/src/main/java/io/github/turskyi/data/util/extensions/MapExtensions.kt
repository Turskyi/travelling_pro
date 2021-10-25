package io.github.turskyi.data.util.extensions

import io.github.turskyi.data.entities.remote.CountryResponse
import io.github.turskyi.data.entities.local.CityEntity
import io.github.turskyi.data.entities.local.CountryEntity
import io.github.turskyi.data.entities.local.TravellerEntity
import io.github.turskyi.data.entities.local.VisitedCountryEntity
import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.CountryModel
import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.domain.models.entities.VisitedCountryModel

fun List<CountryModel>.mapModelListToEntityList(): MutableList<CountryEntity> {
    return mapTo(mutableListOf(), { countryModel -> countryModel.mapModelToEntity() })
}

fun CountryModel.mapModelToEntity() = CountryEntity(id, shortName, name, flag, isVisited)
fun CityModel.mapModelToEntity(): CityEntity {
    return CityEntity(id = id, name = name, parentId = parentId, month = month)
}

fun CityEntity.mapEntityToModel(): CityModel {
    return CityModel(id = id, name = name, parentId = parentId, month = month)
}

fun List<CountryResponse>.mapNetListToModelList(): MutableList<CountryModel> {
    return this.mapTo(mutableListOf(), { countryNet -> countryNet.mapNetToDomain() })
}

fun CountryEntity.mapEntityToModel(): CountryModel {
    return CountryModel(id, shortName, name, flag, isVisited, "", "")
}

fun TravellerEntity.mapEntityToModel(): TravellerModel {
    return TravellerModel(
        id = id,
        name = name,
        avatar = avatar,
        isVisible = isVisible,
        counter = counter.toInt()
    )
}

fun CountryResponse.mapNetToDomain(): CountryModel {
    return CountryModel(shortName = alphaThreeCode, name = name, flag = flag)
}

fun List<CityEntity>.mapEntitiesToModelList(): MutableList<CityModel> {
    return mapTo(mutableListOf(), { cityEntity -> cityEntity.mapEntityToModel() })
}

fun List<CountryEntity>.mapEntityListToModelList(): MutableList<CountryModel> {
    return mapTo(mutableListOf(), { countryEntity -> countryEntity.mapEntityToModel() })
}

fun List<TravellerEntity>.mapFirestoreListToModelList(): MutableList<TravellerModel> {
    return mapTo(mutableListOf(), { entity -> entity.mapEntityToModel() })
}

fun CountryEntity.mapCountryToVisitedCountry(): VisitedCountryEntity {
    return VisitedCountryEntity(id = id, shortName = shortName, name = name, flag = flag)
}

fun VisitedCountryEntity.mapVisitedCountryEntityToVisitedCountry(): VisitedCountryModel {
    return VisitedCountryModel(
        id = id,
        shortName = shortName,
        title = name,
        flag = flag,
        selfie = selfie,
        selfieName = selfieName,
    )
}

fun List<VisitedCountryEntity>.mapVisitedCountriesToVisitedModelList(): MutableList<VisitedCountryModel> {
    return mapTo(
        mutableListOf(),
        { countryEntity -> countryEntity.mapVisitedCountryEntityToVisitedCountry() },
    )
}

