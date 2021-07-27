package io.github.turskyi.data.repository

import io.github.turskyi.data.network.datasource.CountriesNetSource
import io.github.turskyi.data.util.extensions.*
import io.github.turskyi.data.firestore.service.FirestoreSource
import io.github.turskyi.domain.entities.CityModel
import io.github.turskyi.domain.entities.CountryModel
import io.github.turskyi.domain.entities.VisitedCountryModel
import io.github.turskyi.domain.repository.CountriesRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CountriesRepositoryImpl : CountriesRepository, KoinComponent {

    private val netSource: CountriesNetSource by inject()
    private val firestoreSource: FirestoreSource by inject()

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
        selfieName: String,
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firestoreSource.updateSelfie(
        name = name,
        selfie = selfie,
        previousSelfieName = selfieName,
        {
            firestoreSource.setVisitedCountries(
                { countries -> onSuccess(countries.mapVisitedCountriesToVisitedModelList()) },
                { exception -> onError?.invoke(exception) })
        },
        { exception -> onError?.invoke(exception) })

    override suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = firestoreSource.markAsVisited(
        country.mapModelToEntity(), { onSuccess() }, { exception -> onError.invoke(exception) })

    override suspend fun removeFromVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firestoreSource.removeFromVisited(country.name, country.id, { onSuccess() },
        { exception -> onError?.invoke(exception) })

    override suspend fun insertCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = firestoreSource.insertCity(
        city.mapModelToEntity(),
        { onSuccess() },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun removeCity(
        city: CityModel,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = firestoreSource.removeCityById(
        city.id,
        { onSuccess() },
        { exception -> onError.invoke(exception) },
    )

    private fun addModelsToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = firestoreSource.insertAllCountries(
        countries.mapModelListToEntityList(),
        { onSuccess() },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun setVisitedModelCountries(
        onSuccess: (List<VisitedCountryModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = firestoreSource.setVisitedCountries({ countries ->
        onSuccess(countries.mapVisitedCountriesToVisitedModelList())
    }, { exception -> onError.invoke(exception) })

    override suspend fun setCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = firestoreSource.setCities({ cities -> onSuccess(cities.mapEntitiesToModelList()) },
        { exception -> onError.invoke(exception) })

    override suspend fun setCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit
    ) = firestoreSource.setCountNotVisitedCountries(
        { count -> onSuccess(count) },
        { exception -> onError.invoke(exception) },
    )

    override suspend fun getCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firestoreSource.setCountNotVisitedAndVisitedCountries({ notVisitedCount, visitedCount ->
        onSuccess(notVisitedCount, visitedCount)
    }, { exception -> onError?.invoke(exception) })

    override fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firestoreSource.setCountriesByRange(to, from,
        { list -> onSuccess(list.mapEntityListToModelList()) }, { onError?.invoke(it) })

    override fun setCountriesByName(
        name: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ) {
        firestoreSource.setCountriesByName(
            name,
            { list -> onSuccess(list.mapEntityListToModelList()) },
            { onError.invoke(it) },
        )
    }
}