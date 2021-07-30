package io.github.turskyi.domain.repository

import io.github.turskyi.domain.models.entities.TravellerModel

interface TravellerRepository {
    fun setTravellersByName(
        name: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)
    fun saveTraveller(onSuccess: () -> Unit, onError: (Exception) -> Unit)
    fun setTravellersByRange(
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setUserVisibility(visible: Boolean, onSuccess: () -> Unit, onError: (Exception) -> Unit)
}