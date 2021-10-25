package io.github.turskyi.data.database.firestore.service

import io.github.turskyi.data.entities.local.CityEntity
import io.github.turskyi.data.entities.local.CountryEntity
import io.github.turskyi.data.entities.local.TravellerEntity
import io.github.turskyi.data.entities.local.VisitedCountryEntity

interface FirestoreDatabaseSource {
    suspend fun insertAllCountries(
        countries: List<CountryEntity>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun insertAllFlags(
        countries: List<CountryEntity>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun markAsVisited(
        countryEntity: CountryEntity,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun removeCountryFromVisited(
        shortName: String,
        parentId: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun updateSelfie(
        shortName: String,
        selfie: String,
        previousSelfieName: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun insertCity(city: CityEntity, onSuccess: () -> Unit, onError: (Exception) -> Unit)

    suspend fun removeCityById(id: String, onSuccess: () -> Unit, onError: (Exception) -> Unit)

    suspend fun setVisitedCountries(
        onSuccess: (List<VisitedCountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setVisitedCountriesById(
        id: String,
        onSuccess: (List<VisitedCountryEntity>) -> Unit,
        onError: (Exception) -> Unit,
    )

    suspend fun setAllVisitedCities(
        onSuccess: (List<CityEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCities(
        userId: String,
        countryId: Int,
        onSuccess: (List<CityEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCitiesByParentId(
        parentId: Int,
        onSuccess: (List<CityEntity>) -> Unit,
        onError: (Exception) -> Unit,
    )

    suspend fun getCountNotVisitedCountries(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)

    suspend fun getCityCount(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)


    suspend fun getCityCount(userId: String, onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)

    suspend fun setCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setCountriesByName(
        nameQuery: String,
        onSuccess: (List<CountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setTravellersByRange(
        to: Long,
        from: Int,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setTravellersByName(
        nameQuery: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)
    suspend fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit)
    suspend fun setUserVisibility(
        visible: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun setUserVisibility(onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit)
    suspend fun getCountNotVisitedCountriesById(
        id: String,
        onSuccess: (Int) -> Unit,
        onError: (Exception) -> Unit,
    )
}
