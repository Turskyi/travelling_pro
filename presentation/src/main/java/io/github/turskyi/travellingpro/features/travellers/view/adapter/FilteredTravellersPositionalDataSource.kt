package io.github.turskyi.travellingpro.features.travellers.view.adapter

import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToTravellerList

internal class FilteredTravellersPositionalDataSource(
    private val userName: String,
    private val interactor: TravellersInteractor
) : PositionalDataSource<Traveller>() {

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Traveller>
    ) {
        interactor.setTravellersByName(
            userName,
            params.requestedLoadSize.toLong(),
            params.requestedStartPosition,
            { travellers: List<TravellerModel> ->
                callback.onResult(
                    travellers.mapModelListToTravellerList(),
                    params.requestedStartPosition
                )
            },
            { exception: Exception ->
                exception.printStackTrace()
                callback.onResult(emptyList(), params.requestedStartPosition)
            },
        )
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Traveller>
    ) {
        interactor.setTravellersByName(
            name = userName,
            requestedLoadSize = params.loadSize.toLong(),
            requestedStartPosition = params.startPosition,
            onSusses = { travellers: List<TravellerModel> ->
                callback.onResult(travellers.mapModelListToTravellerList())
            },
            onError = { exception: Exception ->
                exception.printStackTrace()
                callback.onResult(emptyList())
            },
        )
    }
}