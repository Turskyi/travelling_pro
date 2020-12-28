package io.github.turskyi.domain.repository

import io.github.turskyi.domain.model.CityModel
import io.github.turskyi.domain.model.CountryModel

interface CountriesRepository {

    suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun markAsVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getVisitedModelCountriesFromDb(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun removeFromVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun removeCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun insertCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    )

    fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    fun getCountriesByName(
        name: String?,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )
}