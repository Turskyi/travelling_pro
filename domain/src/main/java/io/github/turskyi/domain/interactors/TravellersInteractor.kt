package io.github.turskyi.domain.interactors

import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.domain.repository.TravellerRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TravellersInteractor : KoinComponent {
    private val repository: TravellerRepository by inject()

    fun setTravellersByName(
        name: String,
        onSusses: (List<TravellerModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ): Unit = repository.setTravellersByName(name, onSusses, onError)

    fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit) {
        repository.setTopTravellersPercent(onSuccess, onError)
    }

    fun setTravellersByRange(
        requestedLoadSize: Int,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        repository.setTravellersByRange(
            requestedLoadSize,
            requestedStartPosition,
            onSuccess,
            onError,
        )
    }
}