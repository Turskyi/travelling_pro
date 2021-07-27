package io.github.turskyi.domain.repository

import io.github.turskyi.domain.entities.TravellerModel

interface TravellersRepository {
    fun setTravellersByName(
        name: String,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    )

    fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit)
}