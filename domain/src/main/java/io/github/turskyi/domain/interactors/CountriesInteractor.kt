package io.github.turskyi.domain.interactors

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.turskyi.domain.model.CityModel
import io.github.turskyi.domain.model.CountryModel
import io.github.turskyi.domain.repository.CountriesRepository

class CountriesInteractor : KoinComponent {
    private val repository: CountriesRepository by inject()

    fun setCountriesByName(
        name: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ): Unit = repository.setCountriesByName(name, onSuccess, onError)

    suspend fun updateSelfie(
        name: String,
        selfie: String,
        selfieName: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.updateSelfie(name, selfie, selfieName, onSuccess, onError)

    fun getCountriesByRange(
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountriesByRange(limit, offset, onSuccess, onError)

    suspend fun downloadCountries(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.refreshCountriesInDb(onSuccess, onError)

    suspend fun setNotVisitedCountriesNum(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getCountNotVisitedCountries(onSuccess, onError)

    suspend fun setVisitedModelCountries(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.getVisitedModelCountries(onSuccess, onError)

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
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeFromVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeCity(city, onSuccess = onSuccess, onError = onError)

    suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.insertCity(city, onSuccess = onSuccess, onError = onError)
}