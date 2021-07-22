package io.github.turskyi.domain.repository

import io.github.turskyi.domain.model.TravellerModel

interface TravellersRepository {
    fun setTravellersByName(
        name: String,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    )
}