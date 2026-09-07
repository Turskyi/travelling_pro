package io.github.turskyi.travellingpro.features.travellers.view.adapter

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToTravellerList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

internal class TravellersPositionalDataSource(
    private val interactor: TravellersInteractor,
    private val viewModelScope: CoroutineScope,
) : PositionalDataSource<Traveller>() {

    private val _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Traveller>
    ) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            interactor.setTravellersByRange(
                params.requestedLoadSize.toLong(),
                params.requestedStartPosition,
                { initTravellers ->
                    callback.onResult(
                        initTravellers.mapModelListToTravellerList(),
                        params.requestedStartPosition
                    )
                    // a little bit delay of stopping animation
                    Timer().schedule(2000) { _visibilityLoader.postValue(GONE) }
                },
                { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList(), params.requestedStartPosition)
                    _visibilityLoader.postValue(GONE)
                },
            )
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Traveller>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            interactor.setTravellersByRange(
                (params.startPosition + params.loadSize).toLong(),
                params.startPosition,
                { travellers ->
                    callback.onResult(travellers.mapModelListToTravellerList())
                },
                { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList())
                    _visibilityLoader.postValue(GONE)
                },
            )
        }
    }
}
