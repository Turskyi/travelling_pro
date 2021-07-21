package io.github.turskyi.travellingpro.features.travellers.view.adapter

import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.models.Traveller

internal class TravellersPositionalDataSource(private val interactor: TravellersInteractor) :
    PositionalDataSource<Traveller>() {

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Traveller>
    ) {
//        TODO: set Travellers by range
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Traveller>
    ) {
//        TODO: set travellers by range
    }
}