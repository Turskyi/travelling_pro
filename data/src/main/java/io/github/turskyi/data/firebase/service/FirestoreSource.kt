package io.github.turskyi.data.firebase.service

import io.github.turskyi.data.entities.firestore.CityEntity
import io.github.turskyi.data.entities.firestore.CountryEntity
import io.github.turskyi.data.entities.firestore.VisitedCountryEntity

interface FirestoreSource {
    fun insertAllCountries(
        countries: List<CountryEntity>,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun markAsVisited(
        countryEntity: CountryEntity,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun removeFromVisited(
        name: String,
        parentId: Int,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun updateSelfie(
        name: String,
        selfie: String,
        previousSelfieName: String?,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun insertCity(
        city: CityEntity,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun removeCity(
        name: String,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun getVisitedCountries(
        onSuccess: (List<VisitedCountryEntity>) -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun getCities(
        onSuccess: (List<CityEntity>) -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun getCountNotVisitedCountries(
        onSuccess: (Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun getCountNotVisitedAndVisitedCountries(
        onSuccess: (notVisited: Int, visited: Int) -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun getCountriesByRange(
        to: Int, from: Int, onSuccess: (List<CountryEntity>) -> Unit,
        onError: ((Exception) -> Unit?)?
    )

    fun getCountriesByName(
        nameQuery: String?, onSuccess: (List<CountryEntity>) -> Unit,
        onError: ((Exception) -> Unit?)?
    )
}
