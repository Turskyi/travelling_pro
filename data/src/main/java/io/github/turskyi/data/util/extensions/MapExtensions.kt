package io.github.turskyi.data.util.extensions

import io.github.turskyi.data.datasources.local.entities.CityEntity
import io.github.turskyi.data.datasources.local.entities.CountryEntity
import io.github.turskyi.data.datasources.local.entities.TravellerEntity
import io.github.turskyi.data.datasources.local.entities.VisitedCountryEntity
import io.github.turskyi.data.datasources.remote.entities.CountryResponse
import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.CountryModel
import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.domain.models.entities.VisitedCountryModel

fun List<CountryModel>.mapModelListToEntityList(): MutableList<CountryEntity> {
    return mapTo(mutableListOf()) { countryModel -> countryModel.mapModelToEntity() }
}

fun CountryModel.mapModelToEntity() = CountryEntity(id, shortName, name, flag, isVisited)
fun CityModel.mapModelToEntity(): CityEntity {
    return CityEntity(id = id, name = name, parentId = parentId, month = month)
}

fun CityEntity.mapEntityToModel(): CityModel {
    return CityModel(id = id, name = name, parentId = parentId, month = month)
}

fun List<CountryResponse>.mapNetListToModelList(): MutableList<CountryModel> {
    return this.mapTo(mutableListOf()) { countryNet -> countryNet.mapNetToDomain() }
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
    return CountryModel(
        shortName = cca3,
        name = name.common,
        flag = "https://flagcdn.com/${cca2.lowercase()}.svg"
    )
}

fun List<CityEntity>.mapEntitiesToModelList(): MutableList<CityModel> {
    return mapTo(mutableListOf()) { cityEntity -> cityEntity.mapEntityToModel() }
}

fun List<CountryEntity>.mapEntityListToModelList(): MutableList<CountryModel> {
    return mapTo(mutableListOf()) { countryEntity -> countryEntity.mapEntityToModel() }
}

fun List<TravellerEntity>.mapFirestoreListToModelList(): MutableList<TravellerModel> {
    return mapTo(mutableListOf()) { entity -> entity.mapEntityToModel() }
}

fun CountryEntity.mapCountryToVisitedCountry(): VisitedCountryEntity {
    return VisitedCountryEntity(id = id, shortName = shortName, name = name, flag = flag)
}

fun Map<String, Any?>.mapFirestoreToVisitedCountryEntity(): VisitedCountryEntity {
    fun stringValue(key: String, legacyKey: String? = null): String =
        (this[key] as? String) ?: legacyKey?.let { this[it] as? String }.orEmpty()

    return VisitedCountryEntity(
        id = (this["id"] as? Number)?.toInt() ?: 0,
        shortName = stringValue("shortName", "countryCode"),
        name = stringValue("name", "title"),
        flag = stringValue("flag"),
        selfie = stringValue("selfie"),
        selfieName = stringValue("selfieName"),
    )
}

fun Map<String, Any?>.mapFirestoreToCityEntity(documentId: String): CityEntity {
    val cityId = when (val value = this["id"]) {
        is String -> value
        is Number -> value.toString()
        null -> documentId
        else -> throw IllegalArgumentException("City id must be a string or number")
    }
    val cityName = this["name"] as? String
        ?: throw IllegalArgumentException("City name is missing or invalid")
    val parentId = (this["parentId"] as? Number)?.toInt()
        ?: throw IllegalArgumentException("City parentId is missing or invalid")
    val month = this["month"] as? String
        ?: throw IllegalArgumentException("City month is missing or invalid")

    return CityEntity(id = cityId, name = cityName, parentId = parentId, month = month)
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
    return mapTo(mutableListOf()) { countryEntity ->
        countryEntity.mapVisitedCountryEntityToVisitedCountry()
    }
}
