package io.github.turskyi.domain.interactors

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import io.github.turskyi.domain.entities.CityModel
import io.github.turskyi.domain.entities.CountryModel
import io.github.turskyi.domain.entities.VisitedCountryModel
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
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = repository.updateSelfie(name, selfie, selfieName, onSuccess, onError)

    fun setCountriesByRange(
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.setCountriesByRange(limit, offset, onSuccess, onError)

    suspend fun downloadCountries(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.refreshCountriesInDb(onSuccess, onError)

    suspend fun setNotVisitedCountriesNum(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.setCountNotVisitedCountries(onSuccess, onError)

    suspend fun setVisitedModelCountries(
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.setVisitedModelCountries(onSuccess, onError)

    suspend fun setCities(
        onSusses: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.setCities(onSusses, onError)

    suspend fun markAsVisitedCountryModel(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.markAsVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCountryModelFromVisitedList(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = repository.removeFromVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.removeCity(city, onSuccess = onSuccess, onError = onError)

    suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = repository.insertCity(city, onSuccess = onSuccess, onError = onError)
}