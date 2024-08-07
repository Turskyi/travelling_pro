package io.github.turskyi.travellingpro.features.countries.viewmodel

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import io.github.turskyi.domain.interactors.CountriesInteractor
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.features.countries.view.adapter.CountriesPositionalDataSource
import io.github.turskyi.travellingpro.features.countries.view.adapter.FilteredCountriesPositionalDataSource
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.MainThreadExecutor
import io.github.turskyi.travellingpro.utils.extensions.mapToModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class AllCountriesActivityViewModel(private val interactor: CountriesInteractor) : ViewModel() {

    private val _notVisitedCountriesNumLiveData: MutableLiveData<Int> = MutableLiveData<Int>()
    val notVisitedCountriesNumLiveData: MutableLiveData<Int>
        get() = _notVisitedCountriesNumLiveData

    private var _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: LiveData<Int>
        get() = _visibilityLoader

    var pagedList: PagedList<Country>

    var searchQuery = ""
        set(value) {
            field = value
            pagedList = getCountryList(value)
        }

    private val _errorMessage: MutableLiveData<Event<String>> = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    init {
        _visibilityLoader.postValue(VISIBLE)
        setNotVisitedCountriesNum()
        pagedList = getCountryList(searchQuery)
    }

    private fun getCountryList(searchQuery: String): PagedList<Country> = if (searchQuery == "") {
        // PagedList
        val config: PagedList.Config = PagedList.Config.Builder()
            /* If "true", then it should be created another viewType in Adapter "onCreateViewHolder"
               while uploading */
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(20)
            .setPageSize(20)
            .build()
        // DataSource
        val dataSource = CountriesPositionalDataSource(interactor, viewModelScope)
        _visibilityLoader = dataSource.visibilityLoader
        PagedList.Builder(dataSource, config)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .setNotifyExecutor(MainThreadExecutor())
            .build()
    } else {
        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(1)
            .setPageSize(1)
            .build()
        val filteredDataSource =
            FilteredCountriesPositionalDataSource(
                countryName = searchQuery,
                interactor = interactor,
                viewModelScope = viewModelScope,
            )
        PagedList.Builder(filteredDataSource, config)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .setNotifyExecutor(MainThreadExecutor())
            .build()
    }

    private fun setNotVisitedCountriesNum() = viewModelScope.launch {
        interactor.setNotVisitedCountriesNum({ num ->
            _notVisitedCountriesNumLiveData.postValue(num)
        }, { exception ->
            _visibilityLoader.postValue(GONE)
            _errorMessage.run {
                exception.message?.let { message ->
                    // Trigger the event by setting a new Event as a new value
                    postValue(Event(message))
                }
            }
        })
    }

    fun markAsVisited(country: Country, onSuccess: () -> Unit): Job {
        return viewModelScope.launch(Dispatchers.Main) {
            _visibilityLoader.postValue(VISIBLE)
            interactor.markAsVisitedCountryModel(
                country = country.mapToModel(),
                onSuccess = { onSuccess() },
                onError = { exception: Exception /* = java.lang.Exception */ ->
                    showError(exception)
                },
            )
        }
    }

    private fun showError(exception: Exception) {
        _visibilityLoader.postValue(GONE)
        // Trigger the event by setting a new Event as a new value
        _errorMessage.postValue(
            Event(
                exception.localizedMessage ?: exception.stackTraceToString()
            ),
        )
    }
}