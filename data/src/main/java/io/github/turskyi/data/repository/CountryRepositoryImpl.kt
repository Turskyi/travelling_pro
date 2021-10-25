package io.github.turskyi.data.repository

import io.github.turskyi.data.network.datasource.NetSource
import io.github.turskyi.data.util.extensions.*
import io.github.turskyi.data.database.firestore.service.FirestoreDatabaseSource
import io.github.turskyi.data.entities.local.VisitedCountryEntity
import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.CountryModel
import io.github.turskyi.domain.models.entities.VisitedCountryModel
import io.github.turskyi.domain.repository.CountryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CountryRepositoryImpl(private val applicationScope: CoroutineScope) : CountryRepository,
    KoinComponent {

    private val netSource: NetSource by inject()
    private val databaseSource: FirestoreDatabaseSource by inject()

    override suspend fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.getCountNotVisitedCountries(
        onSuccess = { count -> onSuccess(count) },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun setCityCount(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit) {
        databaseSource.getCityCount(
            onSuccess = { count -> onSuccess(count) },
            onError = { exception -> onError.invoke(exception) },
        )
    }

    override suspend fun setCityCount(
        userId: String,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) {
        databaseSource.getCityCount(
            userId = userId,
            onSuccess = { count -> onSuccess(count) },
            onError = { exception -> onError.invoke(exception) },
        )
    }

    override suspend fun setCountNotVisitedCountriesById(
        id: String,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.getCountNotVisitedCountriesById(
        id = id,
        onSuccess = { count -> onSuccess(count) },
        onError = { exception -> onError.invoke(exception) },
    )


    override suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        netSource.getCountryNetList(
            onComplete = { countryNetList ->
                addResponseListToDb(
                    countries = countryNetList.mapNetListToModelList(),
                    onSuccess = { onSuccess() },
                    onError = { exception -> onError.invoke(exception) },
                )
            },
            onError = { exception -> onError.invoke(exception) },
        )
    }

    private fun refreshFlagsInDb(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        netSource.getCountryNetList(
            onComplete = { countryNetList ->
                addResponseFlagsToDb(
                    countries = countryNetList.mapNetListToModelList(),
                    onSuccess = { onSuccess() },
                    onError = { exception -> onError.invoke(exception) },
                )
            },
            onError = { exception -> onError.invoke(exception) },
        )
    }

    override suspend fun updateSelfie(
        shortName: String,
        selfie: String,
        selfieName: String,
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.updateSelfie(
        shortName = shortName,
        selfie = selfie,
        previousSelfieName = selfieName,
        onSuccess = {
            applicationScope.launch(Dispatchers.IO) {
                databaseSource.setVisitedCountries(
                    onSuccess = { countries ->
                        onSuccess(countries.mapVisitedCountriesToVisitedModelList())
                    },
                    onError = { exception -> onError.invoke(exception) })
            }
        },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.markAsVisited(
        countryEntity = country.mapModelToEntity(),
        onSuccess = { onSuccess() },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = databaseSource.removeCountryFromVisited(
        shortName = country.shortName,
        parentId = country.id,
        onSuccess = { onSuccess() },
        onError = { exception -> onError?.invoke(exception) },
    )

    override suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.insertCity(
        city = city.mapModelToEntity(),
        onSuccess = { onSuccess() },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.removeCityById(
        id = city.id,
        onSuccess = { onSuccess() },
        onError = { exception -> onError.invoke(exception) },
    )

    private fun addResponseListToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch(Dispatchers.IO) {
            databaseSource.insertAllCountries(
                countries = countries.mapModelListToEntityList(),
                onSuccess = { onSuccess() },
                onError = { exception -> onError.invoke(exception) },
            )
        }
    }

    private fun addResponseFlagsToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch(Dispatchers.IO) {
            databaseSource.insertAllFlags(
                countries = countries.mapModelListToEntityList(),
                onSuccess = { onSuccess() },
                onError = { exception -> onError.invoke(exception) },
            )
        }
    }

    override suspend fun setVisitedCountries(
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        databaseSource.setVisitedCountries(
            onSuccess = { countries: List<VisitedCountryEntity> ->
                // check if in database still flags from previous api
                if (countries.any { it.flag.contains("restcountries.eu") }) {
                    refreshFlagsInDb(
                        onSuccess = { onSuccess(countries.mapVisitedCountriesToVisitedModelList()) },
                        onError = { exception -> onError.invoke(exception) },
                    )
                } else {
                    onSuccess(countries.mapVisitedCountriesToVisitedModelList())
                }
            },
            onError = { exception -> onError.invoke(exception) },
        )
    }

    override suspend fun setVisitedCountries(
        id: String,
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        databaseSource.setVisitedCountriesById(
            id = id,
            onSuccess = { countries: List<VisitedCountryEntity> ->
                onSuccess(countries.mapVisitedCountriesToVisitedModelList())
            },
            onError = { exception -> onError.invoke(exception) },
        )
    }

    override suspend fun setCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setAllVisitedCities(
        onSuccess = { cities -> onSuccess(cities.mapEntitiesToModelList()) },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun setCities(
        userId: String,
        countryId: Int,
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCities(
        userId = userId,
        countryId = countryId,
        onSuccess = { cities -> onSuccess(cities.mapEntitiesToModelList()) },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun setCitiesById(
        parentId: Int,
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCitiesByParentId(
        parentId = parentId,
        onSuccess = { cities -> onSuccess(cities.mapEntitiesToModelList()) },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun setCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCountNotVisitedAndVisitedCountries(
        onSuccess = { notVisitedCount, visitedCount -> onSuccess(notVisitedCount, visitedCount) },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun setCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCountriesByRange(
        to = to,
        from = from,
        onSuccess = { list -> onSuccess(list.mapEntityListToModelList()) },
        onError = { onError.invoke(it) },
    )

    override suspend fun setCountriesByName(
        name: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ) {
        databaseSource.setCountriesByName(
            nameQuery = name,
            onSuccess = { list -> onSuccess(list.mapEntityListToModelList()) },
            onError = { onError.invoke(it) },
        )
    }
}