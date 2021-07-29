package io.github.turskyi.travellingpro.features.travellers.view.adapter

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToTravellerList
import java.util.*
import kotlin.concurrent.schedule

internal class TravellersPositionalDataSource(private val interactor: TravellersInteractor) :
    PositionalDataSource<Traveller>() {

    private val _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Traveller>
    ) {
        interactor.setTravellersByRange(
            params.requestedLoadSize,
            params.requestedStartPosition,
            { initTravellers ->
                callback.onResult(
                    initTravellers.mapModelListToTravellerList(),
                    params.requestedStartPosition
                )
                // a little bit delay of stopping animation
                Timer().schedule(2000) { _visibilityLoader.postValue(View.GONE) }
            },
            { exception ->
                exception.printStackTrace()
                callback.onResult(emptyList(), params.requestedStartPosition)
                _visibilityLoader.postValue(View.GONE)
            },
        )
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Traveller>
    ) {
        interactor.setTravellersByRange(
            params.startPosition + params.loadSize,
            params.startPosition,
            { travellers ->
                callback.onResult(travellers.mapModelListToTravellerList())
            },
            { exception ->
                exception.printStackTrace()
                callback.onResult(emptyList())
                _visibilityLoader.postValue(View.GONE)
            },
        )
    }
}
