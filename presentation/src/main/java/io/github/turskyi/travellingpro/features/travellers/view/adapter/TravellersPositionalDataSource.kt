package io.github.turskyi.travellingpro.features.travellers.view.adapter

import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.models.Country

internal class TravellersPositionalDataSource(private val interactor: TravellersInteractor) :
    PositionalDataSource<Country>() {

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
//        TODO: set Travellers by range
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
//        TODO: set travellers by range
    }
}