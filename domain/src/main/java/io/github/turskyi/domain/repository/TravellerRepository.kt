package io.github.turskyi.domain.repository

import io.github.turskyi.domain.models.entities.TravellerModel

interface TravellerRepository {
    fun setTravellersByName(
        name: String,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)
    fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit)
    fun setTravellersByRange(
        requestedLoadSize: Int,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    )
}