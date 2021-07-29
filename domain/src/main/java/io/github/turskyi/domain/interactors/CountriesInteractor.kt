package io.github.turskyi.domain.interactors

import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.CountryModel
import io.github.turskyi.domain.models.entities.VisitedCountryModel
import io.github.turskyi.domain.repository.CountryRepository
import io.github.turskyi.domain.repository.TravellerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CountriesInteractor(private val applicationScope: CoroutineScope) : KoinComponent {
    private val countryRepository: CountryRepository by inject()
    private val travellerRepository: TravellerRepository by inject()

    fun setCountriesByName(
        name: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ): Unit = countryRepository.setCountriesByName(name, onSuccess, onError)

    suspend fun updateSelfie(
        name: String,
        selfie: String,
        selfieName: String,
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = countryRepository.updateSelfie(name, selfie, selfieName, onSuccess, onError)

    fun setCountriesByRange(
        limit: Int,
        offset: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = countryRepository.setCountriesByRange(limit, offset, onSuccess, onError)

    suspend fun downloadCountries(onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        travellerRepository.saveTraveller(
            {
                applicationScope.launch(Dispatchers.IO) {
                    countryRepository.refreshCountriesInDb(onSuccess, onError)
                }
            },
            onError,
        )
    }

    suspend fun setNotVisitedCountriesNum(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = countryRepository.setCountNotVisitedCountries(onSuccess, onError)

    suspend fun setVisitedCountries(
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = countryRepository.setVisitedModelCountries(onSuccess, onError)

    suspend fun setCities(
        onSusses: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = countryRepository.setCities(onSusses, onError)

    suspend fun markAsVisitedCountryModel(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = countryRepository.markAsVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCountryModelFromVisitedList(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)? = null
    ) = countryRepository.removeFromVisited(country, onSuccess = onSuccess, onError = onError)

    suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = countryRepository.removeCity(city, onSuccess = onSuccess, onError = onError)

    suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = countryRepository.insertCity(city, onSuccess = onSuccess, onError = onError)
}