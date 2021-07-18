package io.github.turskyi.travellingpro.features.travellers.view.adapter

import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.models.Country

internal class FilteredTravellersPositionalDataSource(
    private val userName: String?,
    private val interactor: TravellersInteractor
) : PositionalDataSource<Country>() {

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
//TODO: set filtered travellers by range
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
//TODO: set filtered travellers by range
    }
}