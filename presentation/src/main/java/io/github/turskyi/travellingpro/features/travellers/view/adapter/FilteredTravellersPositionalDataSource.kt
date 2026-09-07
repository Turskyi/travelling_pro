package io.github.turskyi.travellingpro.features.travellers.view.adapter

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.domain.models.entities.TravellerModel
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToTravellerList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class FilteredTravellersPositionalDataSource(
    private val userName: String,
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
            interactor.setTravellersByName(
                userName,
                params.requestedLoadSize.toLong(),
                params.requestedStartPosition,
                { travellers: List<TravellerModel> ->
                    callback.onResult(
                        travellers.mapModelListToTravellerList(),
                        params.requestedStartPosition
                    )
                    _visibilityLoader.postValue(GONE)
                },
                { exception: Exception ->
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
}
