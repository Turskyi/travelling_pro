package io.github.turskyi.travellingpro.features.countries.view.adapter

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToCountryList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class FilteredCountriesPositionalDataSource(
    private val countryName: String,
    private val interactor: CountriesInteractor,
    private val viewModelScope: CoroutineScope,
) : PositionalDataSource<Country>() {

    private val _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
        _visibilityLoader.postValue(VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            interactor.searchCountries(
                countryName,
                { allCountries ->
                    callback.onResult(
                        allCountries.mapModelListToCountryList(),
                        params.requestedStartPosition
                    )
                    _visibilityLoader.postValue(GONE)
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
        callback: LoadRangeCallback<Country>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
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
