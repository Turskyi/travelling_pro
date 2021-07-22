package io.github.turskyi.travellingpro.features.travellers.view.adapter

import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.models.Traveller

internal class FilteredTravellersPositionalDataSource(
    private val userName: String,
    private val interactor: TravellersInteractor
) : PositionalDataSource<Traveller>() {

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Traveller>
    ) {
//TODO: set filtered travellers by range
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Traveller>
    ) {
//TODO: set filtered travellers by range
    }
}