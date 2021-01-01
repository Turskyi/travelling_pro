package io.github.turskyi.data.repository

import io.github.turskyi.data.api.datasource.CountriesNetSource
import io.github.turskyi.data.extensions.mapNetListToModelList
import io.github.turskyi.data.firestoreSource.FirestoreSource
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
    ) {
        try {
            netSource.getCountryNetList({ countryNetList ->
                countryNetList?.mapNetListToModelList()?.let { modelList ->
                    addModelsToDb(modelList, {
                        onSuccess()
                    }, {
                        onError?.invoke(it)
                    })
                }
            }, {
                onError?.invoke(it)
            })
        } catch (e: Exception) {
            onError?.invoke(e)
        }
    }

    override suspend fun updateSelfie(
        id: Int,
        selfie: String,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.updateSelfie(id.toString(), selfie)

    override suspend fun markAsVisited(
        country: CountryModel,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.markAsVisited(country.name, { onSuccess() }, { onError?.invoke(it) })

    override suspend fun removeFromVisited(
        country: CountryModel,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.removeFromVisited(country.id.toString())

    override suspend fun insertCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.insertCity(city)

    override suspend fun removeCity(
        city: CityModel,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.removeCity(city.id.toString())

    private fun addModelsToDb(
        countries: MutableList<CountryModel>,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = try {
        firebaseSource.insertAllCountries(countries, { onSuccess() }, { onError?.invoke(it) })
    } catch (e: Exception) {
        onError?.invoke(e)
    }

    override suspend fun getVisitedModelCountriesFromDb(
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getVisitedCountries({ countries ->
        onSuccess(countries)
    }, {
        onError?.invoke(it)
    })

    override suspend fun getCities(
        onSuccess: (List<CityModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCities({ cities ->
        onSuccess(cities)
    }, {
        onError?.invoke(it)
    })


    override suspend fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCountNotVisitedCountries({ count -> onSuccess(count) },
        { onError?.invoke(it) })

    override fun getCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCountriesByRange(to, from, { list -> onSuccess(list) },
        { onError?.invoke(it) })

    override fun getCountriesByName(
        name: String?,
        onSuccess: (List<CountryModel>) -> Unit,
        onError: ((Exception) -> Unit?)?
    ) = firebaseSource.getCountriesByName(name,
        { list -> onSuccess(list) },
        { onError?.invoke(it) })
}