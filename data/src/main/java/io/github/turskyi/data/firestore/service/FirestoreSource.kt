package io.github.turskyi.data.firestore.service

import io.github.turskyi.data.entities.firestore.CityEntity
import io.github.turskyi.data.entities.firestore.CountryEntity
import io.github.turskyi.data.entities.firestore.TravellerEntity
import io.github.turskyi.data.entities.firestore.VisitedCountryEntity

interface FirestoreSource {
    fun insertAllCountries(
        countries: List<CountryEntity>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    fun markAsVisited(
        countryEntity: CountryEntity,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    fun removeFromVisited(
        name: String,
        parentId: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    fun updateSelfie(
        name: String,
        selfie: String,
        previousSelfieName: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    fun insertCity(city: CityEntity, onSuccess: () -> Unit, onError: (Exception) -> Unit)

    fun removeCityById(id: String, onSuccess: () -> Unit, onError: (Exception) -> Unit)

    fun setVisitedCountries(
        onSuccess: (List<VisitedCountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setCities(
        onSuccess: (List<CityEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setCountNotVisitedCountries(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)

    fun setCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setCountriesByRange(
        to: Int,
        from: Int,
        onSuccess: (List<CountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setCountriesByName(
        nameQuery: String,
        onSuccess: (List<CountryEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setTravellersByName(
        nameQuery: String,
        onSuccess: (List<TravellerEntity>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)
}
