package io.github.turskyi.data.repository

import io.github.turskyi.data.network.datasource.NetSource
import io.github.turskyi.data.util.extensions.*
import io.github.turskyi.data.database.firestore.service.FirestoreDatabaseSource
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

    override suspend fun setCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCountNotVisitedCountries(
        { count -> onSuccess(count) },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun setCountNotVisitedCountriesById(
        id: String,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCountNotVisitedCountriesById(
        id,
        { count -> onSuccess(count) },
        { exception -> onError.invoke(exception) },
    )


    override suspend fun refreshCountriesInDb(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = netSource.getCountryNetList(
        { countryNetList ->
            addResponseListToDb(
                countryNetList.mapNetListToModelList(),
                { onSuccess() },
                { exception -> onError.invoke(exception) })
        },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun updateSelfie(
        name: String,
        selfie: String,
        selfieName: String,
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.updateSelfie(
        name = name,
        selfie = selfie,
        previousSelfieName = selfieName,
        {
            applicationScope.launch(Dispatchers.IO) {
                databaseSource.setVisitedCountries(
                    { countries -> onSuccess(countries.mapVisitedCountriesToVisitedModelList()) },
                    { exception -> onError.invoke(exception) })
            }
        },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.markAsVisited(
        country.mapModelToEntity(), { onSuccess() }, { exception -> onError.invoke(exception) })

    override suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = databaseSource.removeCountryFromVisited(country.name, country.id, { onSuccess() },
        { exception -> onError?.invoke(exception) })

    override suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.insertCity(
        city.mapModelToEntity(),
        { onSuccess() },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.removeCityById(
        city.id,
        { onSuccess() },
        { exception -> onError.invoke(exception) },
    )

    private fun addResponseListToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        applicationScope.launch(Dispatchers.IO) {
            databaseSource.insertAllCountries(
                countries.mapModelListToEntityList(),
                { onSuccess() },
                { exception -> onError.invoke(exception) },
            )
        }
    }

    override suspend fun setVisitedCountries(
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setVisitedCountries(
        onSuccess = { countries ->
            onSuccess(countries.mapVisitedCountriesToVisitedModelList())
        },
        onError = { exception -> onError.invoke(exception) },
    )

    override suspend fun setVisitedCountries(
        id: String,
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    )  = databaseSource.setVisitedCountriesById(
        id = id,
        onSuccess = { countries ->
            onSuccess(countries.mapVisitedCountriesToVisitedModelList())
        },
        onError = { exception -> onError.invoke(exception) },
    )

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
    ) = databaseSource.setCountNotVisitedAndVisitedCountries({ notVisitedCount, visitedCount ->
        onSuccess(notVisitedCount, visitedCount)
    }, { exception -> onError.invoke(exception) })

    override suspend fun setCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCountriesByRange(
        to,
        from,
        { list -> onSuccess(list.mapEntityListToModelList()) },
        { onError.invoke(it) },
    )

    override suspend fun setCountriesByName(
        name: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ) {
        databaseSource.setCountriesByName(
            name,
            { list -> onSuccess(list.mapEntityListToModelList()) },
            { onError.invoke(it) },
        )
    }
}