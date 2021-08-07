package io.github.turskyi.travellingpro.features.allcountries.view.adapter

import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToCountryList
import io.github.turskyi.travellingpro.entities.Country
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class FilteredCountriesPositionalDataSource(
    private val countryName: String,
    private val interactor: CountriesInteractor,
    private val viewModelScope: CoroutineScope,
) : PositionalDataSource<Country>() {

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
        viewModelScope.launch {
            interactor.searchCountries(
                countryName,
                { allCountries ->
                    callback.onResult(
                        allCountries.mapModelListToCountryList(),
                        params.requestedStartPosition
                    )
                },
                { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList(), params.requestedStartPosition)
                },
            )
        }
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
        viewModelScope.launch {
            interactor.searchCountries(
                countryName,
                {
                    // on next call result returns nothing since only one page of countries required
                    callback.onResult(emptyList())
                },
                { exception ->
                    exception.printStackTrace()
                    callback.onResult(emptyList())
                },
            )
        }
    }
}