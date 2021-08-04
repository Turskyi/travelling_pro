package io.github.turskyi.travellingpro.features.travellers.view.adapter

import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
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
            { travellers ->
                callback.onResult(
                    travellers.mapModelListToTravellerList(),
                    params.requestedStartPosition
                )
            },
            { exception ->
                exception.printStackTrace()
                callback.onResult(emptyList(), params.requestedStartPosition)
            })
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Traveller>
    ) {
        interactor.setTravellersByName(userName,
            params.loadSize.toLong(),
            params.startPosition,
            { travellers ->
                callback.onResult(travellers.mapModelListToTravellerList())
            },
            { exception ->
                exception.printStackTrace()
                callback.onResult(emptyList())
            })
    }
}