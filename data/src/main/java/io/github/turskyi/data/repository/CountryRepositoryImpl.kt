package io.github.turskyi.data.repository

import io.github.turskyi.data.network.datasource.NetSource
import io.github.turskyi.data.util.extensions.*
import io.github.turskyi.data.database.firestore.service.FirestoreDatabaseSource
import io.github.turskyi.domain.models.entities.CityModel
import io.github.turskyi.domain.models.entities.CountryModel
import io.github.turskyi.domain.models.entities.VisitedCountryModel
import io.github.turskyi.domain.repository.CountryRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CountryRepositoryImpl : CountryRepository, KoinComponent {

    private val netSource: NetSource by inject()
    private val databaseSource: FirestoreDatabaseSource by inject()

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
        onError: ((Exception) -> Unit?)?
    ) = databaseSource.updateSelfie(
        name = name,
        selfie = selfie,
        previousSelfieName = selfieName,
        {
            databaseSource.setVisitedCountries(
                { countries -> onSuccess(countries.mapVisitedCountriesToVisitedModelList()) },
                { exception -> onError?.invoke(exception) })
        },
        { exception -> onError?.invoke(exception) },
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
    ) = databaseSource.removeFromVisited(country.name, country.id, { onSuccess() },
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
    ) = databaseSource.insertAllCountries(
        countries.mapModelListToEntityList(),
        { onSuccess() },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun setVisitedModelCountries(
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setVisitedCountries({ countries ->
        onSuccess(countries.mapVisitedCountriesToVisitedModelList())
    }, { exception -> onError.invoke(exception) })

    override suspend fun setCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCities({ cities -> onSuccess(cities.mapEntitiesToModelList()) },
        { exception -> onError.invoke(exception) })

    override suspend fun setCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = databaseSource.setCountNotVisitedCountries(
        { count -> onSuccess(count) },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun getCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = databaseSource.setCountNotVisitedAndVisitedCountries({ notVisitedCount, visitedCount ->
        onSuccess(notVisitedCount, visitedCount)
    }, { exception -> onError?.invoke(exception) })

    override fun setCountriesByRange(
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

    override fun setCountriesByName(
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