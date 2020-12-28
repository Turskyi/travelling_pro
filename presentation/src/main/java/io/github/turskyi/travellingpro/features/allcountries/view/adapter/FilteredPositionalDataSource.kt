package io.github.turskyi.travellingpro.features.allcountries.view.adapter

import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactor.CountriesInteractor
import io.github.turskyi.travellingpro.extensions.mapModelListToActualList
import io.github.turskyi.travellingpro.models.Country

internal class FilteredPositionalDataSource(
    private val countryName: String?,
    private val interactor: CountriesInteractor
) : PositionalDataSource<Country>() {

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
        interactor.getCountriesByName(
            countryName, { allCountries ->
                callback.onResult(
                    allCountries.mapModelListToActualList(),
                    params.requestedStartPosition
                )
            },
            {
                it.printStackTrace()
                callback.onResult(emptyList(), params.requestedStartPosition)
            })
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
        interactor.getCountriesByName(countryName, {
                /* on next call result returns nothing since only one page of countries required */
                callback.onResult(emptyList())
            },
            {
                it.printStackTrace()
                callback.onResult(emptyList())
            })
    }
}