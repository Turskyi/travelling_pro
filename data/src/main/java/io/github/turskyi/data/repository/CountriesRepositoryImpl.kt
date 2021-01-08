package io.github.turskyi.data.repository

import io.github.turskyi.data.api.datasource.CountriesNetSource
import io.github.turskyi.data.extensions.*
import io.github.turskyi.data.firebase.service.FirestoreSource
import io.github.turskyi.domain.model.CityModel
import io.github.turskyi.domain.model.CountryModel
import io.github.turskyi.domain.repository.CountriesRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class CountriesRepositoryImpl : CountriesRepository, KoinComponent {

    private val netSource: CountriesNetSource by inject()
    private val firebaseSource: FirestoreSource by inject()

    override suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = netSource.getCountryNetList({ countryNetList ->
        countryNetList?.mapNetListToModelList()?.let { modelList ->
            addModelsToDb(modelList, { onSuccess() }, { exception -> onError?.invoke(exception) })
        }
    }, { exception -> onError?.invoke(exception) })

    override suspend fun updateSelfie(
        name: String,
        selfie: String,
        selfieName: String?,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.updateSelfie(name, selfie, previousSelfieName = selfieName, {
        firebaseSource.getVisitedCountries(
            { countries -> onSuccess(countries.mapVisitedCountriesToModelList()) },
            { exception -> onError?.invoke(exception) })
    }, { exception -> onError?.invoke(exception) })

    override suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.markAsVisited(
        country.mapModelToEntity(), { onSuccess() }, { exception -> onError?.invoke(exception) })

    override suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.removeFromVisited(country.name, country.id, { onSuccess() },
        { exception -> onError?.invoke(exception) })

    override suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.insertCity(city.mapModelToEntity(), { onSuccess() },
        { exception -> onError?.invoke(exception) })

    override suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.removeCity(city.name, { onSuccess() },
        { exception -> onError?.invoke(exception) })

    private fun addModelsToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.insertAllCountries(
        countries.mapModelListToEntityList(), { onSuccess() },
        { exception -> onError?.invoke(exception) })

    override suspend fun getVisitedModelCountries(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getVisitedCountries({ countries ->
        onSuccess(countries.mapVisitedCountriesToModelList())
    }, { exception -> onError?.invoke(exception) })

    override suspend fun getCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCities({ cities -> onSuccess(cities.mapEntitiesToModelList()) },
        { exception -> onError?.invoke(exception) })

    override suspend fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCountNotVisitedCountries({ count -> onSuccess(count) },
        { exception -> onError?.invoke(exception) })

    override suspend fun getCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCountNotVisitedAndVisitedCountries({ notVisitedCount, visitedCount ->
        onSuccess(notVisitedCount, visitedCount)
    }, { exception -> onError?.invoke(exception) })

    override fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCountriesByRange(to, from,
        { list -> onSuccess(list.mapEntityListToModelList()) }, { onError?.invoke(it) })

    override fun getCountriesByName(
        name: String?,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCountriesByName(name,
        { list -> onSuccess(list.mapEntityListToModelList()) }, { onError?.invoke(it) })
}