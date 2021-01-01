package io.github.turskyi.domain.interactor

import org.koin.core.KoinComponent
import org.koin.core.inject
import io.github.turskyi.domain.model.CityModel
import io.github.turskyi.domain.model.CountryModel
import io.github.turskyi.domain.repository.CountriesRepository

class CountriesInteractor : KoinComponent {
    private val repository: CountriesRepository by inject()

    fun getCountriesByName(
        name: String?,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountriesByName(name, onSusses, onError)

    suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.updateSelfie(id, selfie, onSusses, onError)

    fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSusses: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountriesByRange(limit, offset, onSusses, onError)

    suspend fun downloadCountries(
        onSusses: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.refreshCountriesInDb(onSusses, onError)

    suspend fun getNotVisitedCountriesNum(
        onSusses: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountNotVisitedCountries(onSusses, onError)

    suspend fun getVisitedModelCountries(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getVisitedModelCountriesFromDb(onSuccess, onError)

    suspend fun getCities(
        onSusses: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCities(onSusses, onError)

    suspend fun markAsVisitedCountryModel(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.markAsVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCountryModelFromVisitedList(
        country: CountryModel,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeFromVisited(country, onError = onError)

    suspend fun removeCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeCity(city, onError = onError)

    suspend fun insertCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.insertCity(city, onError = onError)
}