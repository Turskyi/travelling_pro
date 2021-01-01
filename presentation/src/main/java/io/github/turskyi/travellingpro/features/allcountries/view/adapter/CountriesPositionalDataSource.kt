package io.github.turskyi.travellingpro.features.allcountries.view.adapter

import android.view.View.GONE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactor.CountriesInteractor
import io.github.turskyi.travellingpro.extensions.mapModelListToCountryList
import io.github.turskyi.travellingpro.models.Country
import java.util.*
import kotlin.concurrent.schedule

internal class CountriesPositionalDataSource(private val interactor: CountriesInteractor) :
    PositionalDataSource<Country>() {

    private val _visibilityLoader = MutableLiveData<Int>()
    val visibilityLoader: MutableLiveData<Int>
        get() = _visibilityLoader

    override fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<Country>
    ) {
        interactor.getCountriesByRange(params.requestedLoadSize, params.requestedStartPosition,
            { initCountries ->
                callback.onResult(
                    initCountries.mapModelListToCountryList(),
                    params.requestedStartPosition
                )
                /* a little bit delay of stopping animation */
                Timer().schedule(2000) {
                    _visibilityLoader.postValue(GONE)
                }
            },
            {
                it.printStackTrace()
                callback.onResult(emptyList(), params.requestedStartPosition)
                _visibilityLoader.postValue(GONE)
            })
    }

    override fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<Country>
    ) {
        interactor.getCountriesByRange(params.startPosition + params.loadSize, params.startPosition,
            { allCountries ->
                callback.onResult(allCountries.mapModelListToCountryList())
            },
            {
                it.printStackTrace()
                callback.onResult(emptyList())
                _visibilityLoader.postValue(GONE)
            })
    }
}