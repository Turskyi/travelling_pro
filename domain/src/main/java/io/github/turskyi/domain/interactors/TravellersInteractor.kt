package io.github.turskyi.domain.interactors

import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.domain.repository.TravellerRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TravellersInteractor : KoinComponent {
    private val repository: TravellerRepository by inject()

    fun setTravellersByName(
        name: String,
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSusses: (List<TravellerModel>) -> Unit,
        onError: ((Exception) -> Unit)
    ): Unit = repository.setTravellersByName(
        name,
        requestedLoadSize,
        requestedStartPosition,
        onSusses,
        onError,
    )

    fun setTopTravellersPercent(onSuccess: (Int) -> Unit, onError: (Exception) -> Unit) {
        repository.setTopTravellersPercent(onSuccess, onError)
    }

    fun setTravellersByRange(
        requestedLoadSize: Long,
        requestedStartPosition: Int,
        onSuccess: (List<TravellerModel>) -> Unit,
        onError: (Exception) -> Unit
    ) = repository.setTravellersByRange(
        requestedLoadSize,
        requestedStartPosition,
        onSuccess,
        onError,
    )

    fun setUserVisibility(isVisible: Boolean, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        repository.setUserVisibility(
            isVisible,
            onSuccess,
            onError,
        )
    }
    fun setUserVisibility( onSuccess: (Boolean) -> Unit, onError: (Exception) -> Unit) {
//       TODO: implement setting current user visibility
    }
}