package io.github.turskyi.domain.repository

import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.CountryModel
import io.github.turskyi.domain.models.entities.VisitedCountryModel

interface CountryRepository {

    suspend fun refreshCountriesInDb(onSuccess: () -> Unit, onError: (Exception) -> Unit)

    suspend fun updateSelfie(
        name: String,
        selfie: String,
        selfieName: String,
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setVisitedModelCountries(
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCountNotVisitedCountries(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)

    suspend fun getCountNotVisitedAndVisitedCountries(
        onSuccess: (Int, Int) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCountriesByName(
        name: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCities(onSuccess: (List<CityModel>) -> Unit, onError: (Exception) -> Unit)

    suspend fun setCitiesById(
        parentId: Int,
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCountNotVisitedCountriesById(
        id: String,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    )
}