package io.github.turskyi.domain.repository

import io.github.turskyi.domain.entities.CityModel
import io.github.turskyi.domain.entities.CountryModel
import io.github.turskyi.domain.entities.VisitedCountryModel

interface CountriesRepository {

    suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    suspend fun updateSelfie(
        name: String,
        selfie: String,
        selfieName: String,
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
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

    suspend fun setCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    )

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

    fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)? = null
    )

    fun setCountriesByName(
        name: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )
}