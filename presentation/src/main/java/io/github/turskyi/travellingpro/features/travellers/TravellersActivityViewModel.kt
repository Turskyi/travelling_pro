package io.github.turskyi.travellingpro.features.travellers

import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import io.github.turskyi.domain.interactors.TravellersInteractor
import io.github.turskyi.travellingpro.features.travellers.view.adapter.FilteredTravellersPositionalDataSource
import io.github.turskyi.travellingpro.features.travellers.view.adapter.TravellersPositionalDataSource
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.MainThreadExecutor
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class TravellersActivityViewModel(private val interactor: TravellersInteractor) : ViewModel() {

    private val _topTravellersPercentLiveData: MutableLiveData<Int> = MutableLiveData<Int>()
    val topTravellersPercentLiveData: MutableLiveData<Int>
        get() = _topTravellersPercentLiveData

    private var _visibilityLoader: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityLoader: LiveData<Int>
        get() = _visibilityLoader

    private var _visibilityUser: MutableLiveData<Int> = MutableLiveData<Int>()
    val visibilityUser: LiveData<Int>
        get() = _visibilityUser

    var pagedList: PagedList<Traveller>

    var searchQuery = ""
        set(value) {
            field = value
            pagedList = getUserList(value)
        }

    private val _errorMessage: MutableLiveData<Event<String>> = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>>
        get() = _errorMessage

    init {
        _visibilityLoader.postValue(VISIBLE)

        viewModelScope.launch {
            setTopTravellersPercent()
        }

        interactor.setUserVisibility(
            onSuccess = { isVisible ->
                if (isVisible) {
                    _visibilityUser.postValue(VISIBLE)
                } else {
                    _visibilityUser.postValue(INVISIBLE)
                }
            },
            onError = { exception ->
                _visibilityLoader.postValue(View.GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        // Trigger the event by setting a new Event as a new value
                        postValue(Event(message))
                    }
                }
            },
        )

        pagedList = getUserList(searchQuery)
    }

    private fun getUserList(searchQuery: String): PagedList<Traveller> {
        return if (searchQuery == "") {
            // PagedList
            val config: PagedList.Config = PagedList.Config.Builder()
                /* If "true", then it should be created another viewType in Adapter "onCreateViewHolder"
                   while uploading */
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .build()
            // DataSource
            val dataSource = TravellersPositionalDataSource(interactor)
            _visibilityLoader = dataSource.visibilityLoader
            PagedList.Builder(dataSource, config)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()
        } else {
            val config: PagedList.Config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(10)
                .setPageSize(1)
                .build()
            val filteredDataSource =
                FilteredTravellersPositionalDataSource(
                    userName = searchQuery,
                    interactor = interactor
                )
            PagedList.Builder(filteredDataSource, config)
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .setNotifyExecutor(MainThreadExecutor())
                .build()
        }
    }

    private fun setTopTravellersPercent() = viewModelScope.launch {
        interactor.setTopTravellersPercent(
            { percent ->
                _topTravellersPercentLiveData.postValue(percent)
            },
            { exception ->
                _visibilityLoader.postValue(View.GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        // Trigger the event by setting a new Event as a new value
                        postValue(Event(message))
                    }
                }
            },
        )
    }

    fun onBecomingVisibleTriggered() {
        interactor.setUserVisibility(
            isVisible = true,
            onSuccess = {
                pagedList = getUserList(searchQuery)
                _visibilityUser.postValue(VISIBLE)
            },
            onError = { exception ->
                _visibilityLoader.postValue(View.GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        // Trigger the event by setting a new Event as a new value
                        postValue(Event(message))
                    }
                }
            },
        )
    }

    fun onVisibilityFabClicked() {
        interactor.setUserVisibility(
            isVisible = false,
            onSuccess = {
                pagedList = getUserList(searchQuery)
                _visibilityUser.postValue(INVISIBLE)
            },
            onError = { exception ->
                _visibilityLoader.postValue(View.GONE)
                _errorMessage.run {
                    exception.message?.let { message ->
                        // Trigger the event by setting a new Event as a new value
                        postValue(Event(message))
                    }
                }
            })
    }
}