package io.github.turskyi.travellingpro.features.countries.view.adapter

import android.view.View.GONE
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.utils.extensions.mapModelListToCountryList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.schedule

internal class CountriesPositionalDataSource(
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
        viewModelScope.launch {
            interactor.setCountries(
                params.requestedLoadSize,
                params.requestedStartPosition,
                { initCountries ->
                    callback.onResult(
                        initCountries.mapModelListToCountryList(),
                        params.requestedStartPosition
                    )
                    // a little bit delay of stopping animation
                    Timer().schedule(1000) { _visibilityLoader.postValue(GONE) }
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
        viewModelScope.launch {
            interactor.setCountries(
                params.startPosition + params.loadSize,
                params.startPosition,
                { allCountries ->
                    callback.onResult(allCountries.mapModelListToCountryList())
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